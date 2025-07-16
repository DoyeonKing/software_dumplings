package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.*;
import com.example.springboot.entity.DailyDispatchSuggestion;
import com.example.springboot.entity.DailySimulationReport;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.service.Interface.IDailyDispatchSuggestionService;
import com.example.springboot.service.Interface.IDailySimulationReportService;
import com.example.springboot.service.Interface.IEliteSitesService;
import com.github.pagehelper.PageInfo; // 确保导入 PageInfo
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // 确保导入
import java.math.RoundingMode; // 确保导入
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap; // 确保导入 HashMap
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predict")
public class PredictionController {

    private final IDailySimulationReportService dailySimulationReportService;
    private final IEliteSitesService eliteSitesService;
    private final IDailyDispatchSuggestionService dailyDispatchSuggestionService; // 仍然需要注入，但不再在这里直接调用保存

    public PredictionController(IDailySimulationReportService dailySimulationReportService,
                                IEliteSitesService eliteSitesService,
                                IDailyDispatchSuggestionService dailyDispatchSuggestionService) {
        this.dailySimulationReportService = dailySimulationReportService;
        this.eliteSitesService = eliteSitesService;
        this.dailyDispatchSuggestionService = dailyDispatchSuggestionService;
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

        List<DailySimulationReport> storedAreaReports = dailySimulationReportService.getReportsByDateAndBounds(
                reportDate,
                request.getMinLat(), request.getMaxLat(),
                request.getMinLon(), request.getMaxLon()
        );

        final LocalDateTime targetTime;
        List<DailySimulationReport> sameHourReports = new ArrayList<>();

        if (predictionTimeHour != null) {
            targetTime = LocalDateTime.of(reportDate, LocalTime.of(predictionTimeHour, 0));
            sameHourReports = storedAreaReports.stream()
                    .filter(report -> report.getPredictionTargetTime() != null
                            && report.getPredictionTargetTime().truncatedTo(ChronoUnit.HOURS)
                            .equals(targetTime))
                    .collect(Collectors.toList());
        } else {
            if (!storedAreaReports.isEmpty()) {
                targetTime = storedAreaReports.get(0).getPredictionTargetTime().truncatedTo(ChronoUnit.HOURS);
                sameHourReports = storedAreaReports.stream()
                        .filter(report -> report.getPredictionTargetTime() != null
                                && report.getPredictionTargetTime().truncatedTo(ChronoUnit.HOURS)
                                .equals(targetTime))
                        .collect(Collectors.toList());
            } else {
                targetTime = null;
            }
        }

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

        List<DispatchSuggestionDTO> dispatchSuggestionsDTO = generateDispatchSuggestions(
                targetTime,
                filteredAreaStatuses,
                eliteSitesService.getAllEliteSites()
        );

        // 【关键修改：删除将调度建议保存到数据库的代码块】
        // List<DailyDispatchSuggestion> dispatchSuggestionsEntities = dispatchSuggestionsDTO.stream()
        //         .map(dto -> {
        //             DailyDispatchSuggestion entity = new DailyDispatchSuggestion();
        //             entity.setReportDate(reportDate);
        //             entity.setPredictionTargetTime(dto.getPredictionTargetTime());
        //             entity.setSourceGeohash(dto.getSourceGeohash());
        //             entity.setSourceLatitude(new BigDecimal(dto.getSourceLatitude()).setScale(8, RoundingMode.HALF_UP));
        //             entity.setSourceLongitude(new BigDecimal(dto.getSourceLongitude()).setScale(8, RoundingMode.HALF_UP));
        //             entity.setTargetGeohash(dto.getTargetGeohash());
        //             entity.setTargetLatitude(new BigDecimal(dto.getTargetLatitude()).setScale(8, RoundingMode.HALF_UP));
        //             entity.setTargetLongitude(new BigDecimal(dto.getTargetLongitude()).setScale(8, RoundingMode.HALF_UP));
        //             entity.setSuggestedBikeCount(dto.getSuggestedBikeCount());
        //             entity.setSuggestionTime(dto.getSuggestionTime());
        //             entity.setCreatedAt(LocalDateTime.now());
        //             entity.setSuggestionStatus("PENDING");
        //             return entity;
        //         })
        //         .collect(Collectors.toList());

        // if (!dispatchSuggestionsEntities.isEmpty()) {
        //     dailyDispatchSuggestionService.saveDailySuggestions(dispatchSuggestionsEntities);
        //     System.out.println("DEBUG: 已保存 " + dispatchSuggestionsEntities.size() + " 条调度建议数据到数据库。");
        // }
        // 【删除结束】

        MapPredictionResponseDTO responseDTO = new MapPredictionResponseDTO();
        responseDTO.setAreaStatuses(filteredAreaStatuses);
        responseDTO.setDispatchSuggestions(dispatchSuggestionsDTO); // 依然返回给前端 DTO 列表

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

        Map<String, EliteSites> eliteSitesMap = allEliteSites.stream()
                .collect(Collectors.toMap(EliteSites::getGeohash, site -> site));

        for (String scarceGeohash : scarceAreas) {
            EliteSites scarceSite = eliteSitesMap.get(scarceGeohash);
            if (scarceSite == null) continue;

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
            // 这里使用 0.65 和 0.85，请根据您实际需要调整，或从配置文件读取
            long needed = Math.max(0, (long) Math.ceil(scarceStatus.getParkingCapacity() * 0.65 - scarceStatus.getFutureBikes()));
            long available = Math.max(0, surplusStatus.getFutureBikes() - (long) Math.floor(surplusStatus.getParkingCapacity() * 0.85));
            int dispatchCount = (int) Math.min(needed, available);

            if (dispatchCount > 0) {
                DispatchSuggestionDTO suggestion = new DispatchSuggestionDTO();
                suggestion.setSourceGeohash(nearestSurplusGeohash);
                suggestion.setSourceLatitude(surplusSite.getCenterLat().doubleValue());
                suggestion.setSourceLongitude(surplusSite.getCenterLon().doubleValue());
                suggestion.setTargetGeohash(scarceGeohash);
                suggestion.setTargetLatitude(scarceSite.getCenterLat().doubleValue());
                suggestion.setTargetLongitude(scarceSite.getCenterLon().doubleValue()); // 【修正】确保这里是 getCenterLon()
                suggestion.setSuggestedBikeCount(dispatchCount);
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

            double distance = haversineDistance(scarceLat, scarceLon, surplusLat, surplusLon);

            if (distance < minDistance) {
                minDistance = distance;
                nearestGeohash = surplusGeohash;
            }
        }
        return nearestGeohash;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    /**
     * 获取所有调度建议信息，支持分页。
     * 返回全部调度建议的详细信息
     */
    @GetMapping("/suggestions/all")
    public Result getAllSuggestions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 【注意】这里仍会查询并返回 DailyDispatchSuggestion，如果您完全不希望持久化，
            // 那么这个接口可能需要修改或移除。
        List<DailyDispatchSuggestion> suggestions = dailyDispatchSuggestionService.getAllSuggestions();
        return Result.success();
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

    /**
     * 预测未来某个时间的各个停车点的数据，用于热力图展示。
     * 返回这个时间段各个停车区域的车辆数量（作为权重）。
     *
     * @param reportDateStr      日期字符串，格式 yyyy-MM-dd
     * @param predictionTimeHour 精确到小时的时间，例如 10 表示 10:00
     * @return Result.data 为 (停车点geohash、纬度、经度、权重) 的数组，权重为预测车辆数量。
     */
    @GetMapping("/heatmap_data")
    public Result getHeatmapData(
            @RequestParam String reportDateStr,
            @RequestParam Integer predictionTimeHour) {

        LocalDate reportDate;
        try {
            reportDate = LocalDate.parse(reportDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return Result.error("400", "日期格式不正确，请使用 yyyy-MM-dd 格式。");
        }

        LocalDateTime predictionTargetTime;
        try {
            predictionTargetTime = LocalDateTime.of(reportDate, LocalTime.of(predictionTimeHour, 0));
        } catch (Exception e) {
            return Result.error("400", "预测小时不正确，请提供 0-23 之间的整数。");
        }

        List<DailySimulationReport> reports = dailySimulationReportService.getReportsByDateAndTime(reportDate, predictionTargetTime);

        if (reports.isEmpty()) {
            return Result.success("未找到指定时间点的预测数据。", new ArrayList<>());
        }

        List<HeatmapDataDTO> heatmapData = reports.stream()
                .map(report -> new HeatmapDataDTO(
                        report.getGeohash(),
                        report.getLatitude().doubleValue(),
                        report.getLongitude().doubleValue(),
                        report.getFutureBikes()
                ))
                .collect(Collectors.toList());

        return Result.success(heatmapData);
    }
}