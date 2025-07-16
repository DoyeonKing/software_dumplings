package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.*;
import com.example.springboot.entity.DailyDispatchSuggestion;
import com.example.springboot.entity.DailySimulationReport;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.service.Interface.IDailyDispatchSuggestionService;
import com.example.springboot.service.Interface.IDailySimulationReportService;
import com.example.springboot.service.Interface.IEliteSitesService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predict")
public class PredictionController {

    private final IDailySimulationReportService dailySimulationReportService;
    private final IEliteSitesService eliteSitesService;
    private final IDailyDispatchSuggestionService dailyDispatchSuggestionService; // 【新增注入】

    public PredictionController(IDailySimulationReportService dailySimulationReportService,
                                IEliteSitesService eliteSitesService,
                                IDailyDispatchSuggestionService dailyDispatchSuggestionService) { // 【修改构造函数】
        this.dailySimulationReportService = dailySimulationReportService;
        this.eliteSitesService = eliteSitesService;
        this.dailyDispatchSuggestionService = dailyDispatchSuggestionService; // 【新增赋值】
    }
    @PostMapping("/map_area")
    public Result getPredictionsByMapArea(
            @RequestBody MapBoundsRequest request,
            @RequestParam String reportDateStr,
            @RequestParam(required = false) Integer predictionTimeHour) {

        LocalDate reportDate;
        try {
            reportDate = LocalDate.parse(reportDateStr);
        } catch (Exception e) {
            return Result.error("400", "报告日期格式不正确，请使用 yyyy-MM-dd 格式。");
        }

        // 1. 查询指定日期和范围的区域状态数据
        List<DailySimulationReport> storedAreaReports = dailySimulationReportService.getReportsByDateAndBounds(
                reportDate,
                request.getMinLat(), request.getMaxLat(),
                request.getMinLon(), request.getMaxLon()
        );

        // 2. 严格过滤同一时段（精确到小时）的区域状态
        final LocalDateTime targetTime;
        List<DailySimulationReport> sameHourReports = new ArrayList<>();

        if (predictionTimeHour != null) {
            // 显式指定小时
            targetTime = LocalDateTime.of(reportDate, LocalTime.of(predictionTimeHour, 0));
            sameHourReports = storedAreaReports.stream()
                    .filter(report -> report.getPredictionTargetTime() != null
                            && report.getPredictionTargetTime().truncatedTo(ChronoUnit.HOURS)
                            .equals(targetTime))
                    .collect(Collectors.toList());
        } else {
            // 未指定小时时的处理逻辑
            if (!storedAreaReports.isEmpty()) {
                // 取第一个报告的小时作为目标时段
                targetTime = storedAreaReports.get(0).getPredictionTargetTime().truncatedTo(ChronoUnit.HOURS);
                sameHourReports = storedAreaReports.stream()
                        .filter(report -> report.getPredictionTargetTime() != null
                                && report.getPredictionTargetTime().truncatedTo(ChronoUnit.HOURS)
                                .equals(targetTime))
                        .collect(Collectors.toList());
            } else {
                // 当没有数据时，targetTime设为null
                targetTime = null;
            }
        }

        // 3. 转换区域状态为DTO
        List<AreaStatusDTO> filteredAreaStatuses = sameHourReports.stream()
                .map(report -> {
                    AreaStatusDTO dto = new AreaStatusDTO();
                    dto.setGeohash(report.getGeohash());
                    dto.setLatitude(report.getLatitude().doubleValue());
                    dto.setLongitude(report.getLongitude().doubleValue());
                    dto.setStatus(report.getStatus());
                    dto.setUtilizationRate(report.getUtilizationRate().doubleValue());
                    dto.setCurrentBikes(report.getCurrentBikes());
                    dto.setParkingCapacity(report.getParkingCapacity());
                    dto.setPredictedPickups(report.getPredictedPickups());
                    dto.setPredictedDropoffs(report.getPredictedDropoffs());
                    dto.setFutureBikes(report.getFutureBikes());
                    dto.setFutureRemainingSpots(report.getFutureRemainingSpots());
                    dto.setPredictionTime(report.getPredictionTargetTime());
                    return dto;
                })
                .collect(Collectors.toList());

        // 4. 实时生成同一时段的调度建议
        List<DispatchSuggestionDTO> dispatchSuggestions = generateDispatchSuggestions(
                targetTime,
                filteredAreaStatuses,
                eliteSitesService.getAllEliteSites()
        );

        // 5. 封装响应
        MapPredictionResponseDTO responseDTO = new MapPredictionResponseDTO();
        responseDTO.setAreaStatuses(filteredAreaStatuses);
        responseDTO.setDispatchSuggestions(dispatchSuggestions);

        return Result.success(responseDTO);
    }

    // 调度建议生成逻辑
    private List<DispatchSuggestionDTO> generateDispatchSuggestions(
            LocalDateTime predictionTargetTime,
            List<AreaStatusDTO> areaStatusList,
            List<EliteSites> allEliteSites) {

        List<DispatchSuggestionDTO> suggestions = new ArrayList<>();
        if (predictionTargetTime == null || areaStatusList.isEmpty() || allEliteSites.isEmpty()) {
            return suggestions;
        }

        // 筛选同一时段的稀缺区和富余区
        List<String> scarceAreas = areaStatusList.stream()
                .filter(dto -> "稀缺区".equals(dto.getStatus()))
                .map(AreaStatusDTO::getGeohash)
                .collect(Collectors.toList());

        List<String> surplusAreas = areaStatusList.stream()
                .filter(dto -> "富余区".equals(dto.getStatus()))
                .map(AreaStatusDTO::getGeohash)
                .collect(Collectors.toList());

        if (scarceAreas.isEmpty() || surplusAreas.isEmpty()) {
            return suggestions;
        }

        // 构建精英站点映射（geohash -> 站点信息）
        Map<String, EliteSites> eliteSitesMap = allEliteSites.stream()
                .collect(Collectors.toMap(EliteSites::getGeohash, site -> site));

        // 为每个稀缺区匹配最近的富余区
        for (String scarceGeohash : scarceAreas) {
            EliteSites scarceSite = eliteSitesMap.get(scarceGeohash);
            if (scarceSite == null) continue;

            // 查找最近的富余区
            String nearestSurplusGeohash = findNearestSurplusGeohash(scarceSite, surplusAreas, eliteSitesMap);
            if (nearestSurplusGeohash == null) continue;

            EliteSites surplusSite = eliteSitesMap.get(nearestSurplusGeohash);
            AreaStatusDTO scarceStatus = areaStatusList.stream()
                    .filter(dto -> dto.getGeohash().equals(scarceGeohash))
                    .findFirst().orElse(null);
            AreaStatusDTO surplusStatus = areaStatusList.stream()
                    .filter(dto -> dto.getGeohash().equals(nearestSurplusGeohash))
                    .findFirst().orElse(null);
            if (scarceStatus == null || surplusStatus == null) continue;

            // 计算可调度数量（修正为整数计算）
            long needed = Math.max(0, (long) Math.ceil(scarceStatus.getParkingCapacity() * 0.75 - scarceStatus.getFutureBikes()));
            long available = Math.max(0, surplusStatus.getFutureBikes() - (long) Math.floor(surplusStatus.getParkingCapacity() * 0.95));
            int dispatchCount = (int) Math.min(needed, available);  // 转换为int类型

            if (dispatchCount > 0) {
                DispatchSuggestionDTO suggestion = new DispatchSuggestionDTO();
                suggestion.setSourceGeohash(nearestSurplusGeohash);
                suggestion.setSourceLatitude(surplusSite.getCenterLat().doubleValue());
                suggestion.setSourceLongitude(surplusSite.getCenterLon().doubleValue());
                suggestion.setTargetGeohash(scarceGeohash);
                suggestion.setTargetLatitude(scarceSite.getCenterLat().doubleValue());
                suggestion.setTargetLongitude(scarceSite.getCenterLon().doubleValue());
                suggestion.setSuggestedBikeCount(dispatchCount);  // 现在参数类型匹配
                suggestion.setSuggestionTime(LocalDateTime.now());
                suggestion.setPredictionTargetTime(predictionTargetTime);
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }

    // 计算最近的富余区
    private String findNearestSurplusGeohash(EliteSites scarceSite, List<String> surplusAreas, Map<String, EliteSites> eliteSitesMap) {
        String nearestGeohash = null;
        double minDistance = Double.MAX_VALUE;

        double scarceLat = scarceSite.getCenterLat().doubleValue();
        double scarceLon = scarceSite.getCenterLon().doubleValue();

        for (String surplusGeohash : surplusAreas) {
            EliteSites surplusSite = eliteSitesMap.get(surplusGeohash);
            if (surplusSite == null) continue;

            double surplusLat = surplusSite.getCenterLat().doubleValue();
            double surplusLon = surplusSite.getCenterLon().doubleValue();

            // 计算直线距离（使用更精确的距离计算方式）
            double distance = haversineDistance(scarceLat, scarceLon, surplusLat, surplusLon);

            if (distance < minDistance) {
                minDistance = distance;
                nearestGeohash = surplusGeohash;
            }
        }
        return nearestGeohash;
    }

    // 新增：更精确的哈夫曼距离计算（地球表面两点距离，单位：米）
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    /**
     * 获取所有调度建议信息
     * 返回全部调度建议的详细信息
     */
    @GetMapping("/suggestions/all")
    public Result getAllSuggestions() {
        try {
            List<DailyDispatchSuggestion> suggestions = dailyDispatchSuggestionService.getAllSuggestions();
            // 可以在这里对返回的数据进行进一步处理或脱敏，例如只返回部分字段
            return Result.success(suggestions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取调度建议失败: " + e.getMessage());
        }
    }

    /**
     * 更改调度建议的状态
     * @param request 包含suggestionId和newStatus的请求体
     */
    @PutMapping("/suggestions/status")
    public Result updateSuggestionStatus(@RequestBody UpdateSuggestionStatusRequest request) {
        try {
            if (request.getSuggestionId() == null || request.getNewStatus() == null || request.getNewStatus().isEmpty()) {
                return Result.error("400", "调度建议ID和新状态不能为空。");
            }
            dailyDispatchSuggestionService.updateSuggestionStatus(request.getSuggestionId(), request.getNewStatus());
            return Result.success("调度建议状态更新成功。");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "更新调度建议状态失败: " + e.getMessage());
        }
    }
}