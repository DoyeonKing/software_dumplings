package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.AreaStatusDTO;
import com.example.springboot.dto.DispatchSuggestionDTO;
import com.example.springboot.dto.MapBoundsRequest;
import com.example.springboot.dto.MapPredictionResponseDTO;
import com.example.springboot.entity.DailyDispatchSuggestion;
import com.example.springboot.entity.DailySimulationReport;
import com.example.springboot.service.Interface.IDailyDispatchSuggestionService;
import com.example.springboot.service.Interface.IDailySimulationReportService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地图预测控制器
 * 用于根据地图范围获取预测数据 (现在是从数据库读取预计算数据)
 */
@RestController
@RequestMapping("/api/predict")
public class PredictionController {

    private final IDailySimulationReportService dailySimulationReportService;
    private final IDailyDispatchSuggestionService dailyDispatchSuggestionService; // 【新增注入】

    // 【修改构造器】注入 dailySimulationReportService 和 dailyDispatchSuggestionService
    public PredictionController(IDailySimulationReportService dailySimulationReportService,
                                IDailyDispatchSuggestionService dailyDispatchSuggestionService) {
        this.dailySimulationReportService = dailySimulationReportService;
        this.dailyDispatchSuggestionService = dailyDispatchSuggestionService;
    }

    /**
     * 根据地图显示范围获取预计算的预测自行车数量和状态，以及调度建议。
     * 数据将从数据库中读取，该数据由 DispatchScheduler 预生成。
     * 示例：POST /api/predict/map_area
     * Body: { "minLat": 39.9, "maxLat": 40.0, "minLon": 116.3, "maxLon": 116.4, "reportDateStr": "2019-01-01", "predictionTimeHour": 10 }
     *
     * @param request            包含地图经纬度范围的请求体
     * @param reportDateStr      报告所属日期，格式：yyyy-MM-dd （必填，因为要查询预计算数据）
     * @param predictionTimeHour 可选参数，如果需要过滤到某一小时的预测数据 (0-23)
     * @return 范围内的预测信息列表和调度建议列表
     */
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

        // 1. 根据日期和地图范围从数据库中查询预计算的区域状态数据
        List<DailySimulationReport> storedAreaReports = dailySimulationReportService.getReportsByDateAndBounds(
                reportDate,
                request.getMinLat(), request.getMaxLat(),
                request.getMinLon(), request.getMaxLon()
        );

        // 2. 根据日期和地图范围从数据库中查询预计算的调度建议数据
        List<DailyDispatchSuggestion> storedDispatchSuggestions = dailyDispatchSuggestionService.getSuggestionsByDateAndBounds(
                reportDate,
                request.getMinLat(), request.getMaxLat(),
                request.getMinLon(), request.getMaxLon()
        );

        // 3. (可选) 如果指定了小时，则进行过滤
        List<AreaStatusDTO> filteredAreaStatuses = storedAreaReports.stream()
                .filter(report -> predictionTimeHour == null || report.getPredictionTargetTime().getHour() == predictionTimeHour)
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

        List<DispatchSuggestionDTO> filteredDispatchSuggestions = storedDispatchSuggestions.stream()
                .filter(suggestion -> predictionTimeHour == null || suggestion.getPredictionTargetTime().getHour() == predictionTimeHour)
                .map(suggestion -> {
                    DispatchSuggestionDTO dto = new DispatchSuggestionDTO();
                    dto.setSourceGeohash(suggestion.getSourceGeohash());
                    dto.setSourceLatitude(suggestion.getSourceLatitude().doubleValue());
                    dto.setSourceLongitude(suggestion.getSourceLongitude().doubleValue());
                    dto.setTargetGeohash(suggestion.getTargetGeohash());
                    dto.setTargetLatitude(suggestion.getTargetLatitude().doubleValue());
                    dto.setTargetLongitude(suggestion.getTargetLongitude().doubleValue());
                    dto.setSuggestedBikeCount(suggestion.getSuggestedBikeCount());
                    dto.setSuggestionTime(suggestion.getSuggestionTime());
                    dto.setPredictionTargetTime(suggestion.getPredictionTargetTime());
                    return dto;
                })
                .collect(Collectors.toList());


        if (filteredAreaStatuses.isEmpty() && filteredDispatchSuggestions.isEmpty()) {
            return Result.success("该地图范围内或该日期指定小时没有预计算的预测数据或调度建议。", new MapPredictionResponseDTO()); // 返回空对象
        }

        // 4. 将筛选后的数据封装到 MapPredictionResponseDTO 并传输给前端
        MapPredictionResponseDTO responseDTO = new MapPredictionResponseDTO();
        responseDTO.setAreaStatuses(filteredAreaStatuses);
        responseDTO.setDispatchSuggestions(filteredDispatchSuggestions);

        return Result.success(responseDTO);
    }
}