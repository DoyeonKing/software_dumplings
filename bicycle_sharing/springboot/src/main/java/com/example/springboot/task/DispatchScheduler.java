package com.example.springboot.task;

import com.example.springboot.dto.AreaStatusDTO;
import com.example.springboot.dto.DispatchSuggestionDTO;
import com.example.springboot.dto.HourlySimulationReportDTO;
import com.example.springboot.entity.DailySimulationReport;
import com.example.springboot.entity.DailyDispatchSuggestion;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.service.Interface.IBikesService;
import com.example.springboot.service.Interface.IDailySimulationReportService;
import com.example.springboot.service.Interface.IDailyDispatchSuggestionService;
import com.example.springboot.service.Interface.IDispatchTasksService;
import com.example.springboot.service.Interface.IEliteSitesService;
import com.example.springboot.service.Interface.IPredictionService;
import com.example.springboot.service.ReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode; // 【新增导入】RoundingMode
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DispatchScheduler {

    private final IPredictionService predictionService;
    private final IBikesService bikesService;
    private final IEliteSitesService eliteSitesService;
    private final IDispatchTasksService dispatchTasksService;
    private final ReportService reportService;
    private final IDailySimulationReportService dailySimulationReportService;
    private final IDailyDispatchSuggestionService dailyDispatchSuggestionService;

    @Value("${dispatch.shortage-threshold}")
    private double shortageThreshold;
    @Value("${dispatch.surplus-threshold}")
    private double surplusThreshold;

    public DispatchScheduler(IPredictionService predictionService, IBikesService bikesService,
                             IEliteSitesService eliteSitesService, IDispatchTasksService dispatchTasksService,
                             ReportService reportService, IDailySimulationReportService dailySimulationReportService,
                             IDailyDispatchSuggestionService dailyDispatchSuggestionService) {
        this.predictionService = predictionService;
        this.bikesService = bikesService;
        this.eliteSitesService = eliteSitesService;
        this.dispatchTasksService = dispatchTasksService;
        this.reportService = reportService;
        this.dailySimulationReportService = dailySimulationReportService;
        this.dailyDispatchSuggestionService = dailyDispatchSuggestionService;
    }

    private HourlySimulationReportDTO processHourlyData(LocalDateTime predictionTargetTime) {
        StringBuilder reportContentBuilder = new StringBuilder();
        reportContentBuilder.append("当前自行车共享系统在各个精英站点的状态分析及调度建议报告：\n\n");
        reportContentBuilder.append("报告生成时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        reportContentBuilder.append("预测目标时间：").append(predictionTargetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        List<AreaStatusDTO> areaStatusList = new ArrayList<>();
        List<DispatchSuggestionDTO> dispatchSuggestionList = new ArrayList<>();


        // 1. 获取所有精英站点信息 (包含停车容量)
        List<EliteSites> allEliteSites = eliteSitesService.getAllEliteSites();
        if (allEliteSites.isEmpty()) {
            System.out.println("没有配置精英站点，跳过调度任务生成。");
            reportContentBuilder.append("警告：未找到任何精英站点信息，无法进行调度分析。\n");
            reportService.setLatestReport(reportContentBuilder.toString());
            HourlySimulationReportDTO emptyReport = new HourlySimulationReportDTO();
            emptyReport.setReportGeneratedTime(LocalDateTime.now());
            emptyReport.setPredictionTargetTime(predictionTargetTime);
            emptyReport.setAreaStatuses(new ArrayList<>());
            emptyReport.setDispatchSuggestions(new ArrayList<>());
            emptyReport.setRawReportContent(reportContentBuilder.toString());
            return emptyReport;
        }
        reportContentBuilder.append("### 区域状态分析：\n");

        // 2. 获取所有区域的当前自行车数量
        Map<String, Long> currentBikesByGeohash = new HashMap<>();
        List<String> allGeohashes = allEliteSites.stream()
                .map(EliteSites::getGeohash)
                .collect(Collectors.toList());

        Map<String, Long> realCurrentBikes = bikesService.countBikesByGeohashes(allGeohashes);

        for (String geohash : allGeohashes) {
            currentBikesByGeohash.put(geohash, realCurrentBikes.getOrDefault(geohash, 0L));
        }


        List<String> scarceAreas = new ArrayList<>();
        List<String> surplusAreas = new ArrayList<>();
        List<String> stableAreas = new ArrayList<>();

        // 3. 遍历所有站点，调用预测模型获取预测结果并进行状态判断
        for (EliteSites site : allEliteSites) {
            String geohash = site.getGeohash();
            Long currentBikes = currentBikesByGeohash.getOrDefault(geohash, 0L);
            Integer parkingCapacity = site.getParkingCapacity();

            if (parkingCapacity == null || parkingCapacity <= 0) {
                System.err.println("区域 " + geohash + " 停车容量无效，跳过状态判断。");
                reportContentBuilder.append(String.format(" - 区域 %s: 停车容量无效，无法进行分析。\n", geohash));
                continue;
            }

            try {
                // 调用取车量预测API并阻塞获取结果
                Integer predictedPickups = predictionService.getPredictedPickupCount(geohash, predictionTargetTime).block();
                // 调用停车量预测API并阻塞获取结果
                Integer predictedDropoffs = predictionService.getPredictedParkingCount(geohash, predictionTargetTime).block();

                if (predictedPickups == null || predictedDropoffs == null) {
                    System.err.println("区域 " + geohash + " 未能获取到完整的预测数据，跳过状态判断。");
                    reportContentBuilder.append(String.format(" - 区域 %s: 未能获取到完整的预测数据，无法进行分析。\n", geohash));
                    continue;
                }

                // 计算未来车辆数 = 当前车辆数 - 预测取车量 + 预测停库量
                long futureBikes = currentBikes - predictedPickups + predictedDropoffs;
                futureBikes = Math.max(0, futureBikes); // 确保不为负数

                // 计算未来剩余车位 = 预估车位位数 - 未来车辆数
                long futureRemainingSpots = parkingCapacity - futureBikes;

                double utilizationRate = (double) futureBikes / parkingCapacity;

                String areaStatus;
                if (utilizationRate < shortageThreshold) {
                    scarceAreas.add(geohash);
                    areaStatus = "稀缺区";
                } else if (utilizationRate > surplusThreshold) {
                    surplusAreas.add(geohash);
                    areaStatus = "富余区";
                } else {
                    stableAreas.add(geohash);
                    areaStatus = "稳定区";
                }

                reportContentBuilder.append(String.format(" - 区域 %s (容量 %d): 当前车辆 %d, 预测取车 %d, 预测停库 %d, 未来车辆 %d, 预测剩余车位 %d, 状态: %s (利用率 %.2f%%)\n",
                        geohash, parkingCapacity, currentBikes, predictedPickups, predictedDropoffs, futureBikes, futureRemainingSpots, areaStatus, utilizationRate * 100));

                // 将区域状态添加到列表中
                AreaStatusDTO areaStatusDTO = new AreaStatusDTO();
                areaStatusDTO.setGeohash(geohash);
                areaStatusDTO.setLatitude(site.getCenterLat().doubleValue());
                areaStatusDTO.setLongitude(site.getCenterLon().doubleValue());
                areaStatusDTO.setStatus(areaStatus);
                areaStatusDTO.setUtilizationRate(utilizationRate);
                areaStatusDTO.setCurrentBikes(currentBikes);
                areaStatusDTO.setParkingCapacity(parkingCapacity);
                areaStatusDTO.setPredictedPickups(predictedPickups);
                areaStatusDTO.setPredictedDropoffs(predictedDropoffs);
                areaStatusDTO.setFutureBikes(futureBikes);
                areaStatusDTO.setFutureRemainingSpots(futureRemainingSpots);
                areaStatusDTO.setPredictionTime(predictionTargetTime);
                areaStatusList.add(areaStatusDTO);


            } catch (Exception e) {
                System.err.println("调用预测API或处理区域 " + geohash + " 数据时发生错误: " + e.getMessage());
                reportContentBuilder.append(String.format(" - 区域 %s: 预测或处理数据失败：%s\n", geohash, e.getMessage()));
            }
        }

        reportContentBuilder.append("\n### 调度任务建议：\n");

        // 4. 基于就近原则生成调度任务
        if (!scarceAreas.isEmpty() && !surplusAreas.isEmpty()) {
            reportContentBuilder.append("系统检测到车辆分布不均，建议进行以下调度：\n");
            Map<String, EliteSites> eliteSitesMap = allEliteSites.stream()
                                                        .collect(Collectors.toMap(EliteSites::getGeohash, site -> site));

            for (String scarceGeohash : scarceAreas) {
                String closestSurplusGeohash = surplusAreas.get((int) (Math.random() * surplusAreas.size()));
                int suggestedBikeCount = 10;

                try {
                    // TODO: 实际创建任务，这里仅为模拟调用，确保接口存在
                    // dispatchTasksService.createDispatchTask(
                    //         scarceGeohash,
                    //         closestSurplusGeohash,
                    //         suggestedBikeCount,
                    //         null
                    // );
                    reportContentBuilder.append(String.format(" - 从 %s 调配 %d 辆自行车到 %s。\n", closestSurplusGeohash, suggestedBikeCount, scarceGeohash));
                    System.out.println("已模拟创建从 " + closestSurplusGeohash + " 到 " + scarceGeohash + " 的调度任务，数量：" + suggestedBikeCount);

                    // 将调度建议添加到列表中
                    DispatchSuggestionDTO suggestionDTO = new DispatchSuggestionDTO();
                    suggestionDTO.setSourceGeohash(closestSurplusGeohash);
                    EliteSites sourceSite = eliteSitesMap.get(closestSurplusGeohash);
                    if (sourceSite != null) {
                        suggestionDTO.setSourceLatitude(sourceSite.getCenterLat().doubleValue());
                        suggestionDTO.setSourceLongitude(sourceSite.getCenterLon().doubleValue());
                    }

                    suggestionDTO.setTargetGeohash(scarceGeohash);
                    EliteSites targetSite = eliteSitesMap.get(scarceGeohash);
                    if (targetSite != null) {
                        suggestionDTO.setTargetLatitude(targetSite.getCenterLat().doubleValue());
                        suggestionDTO.setTargetLongitude(targetSite.getCenterLon().doubleValue());
                    }

                    suggestionDTO.setSuggestedBikeCount(suggestedBikeCount);
                    suggestionDTO.setSuggestionTime(LocalDateTime.now());
                    suggestionDTO.setPredictionTargetTime(predictionTargetTime);
                    dispatchSuggestionList.add(suggestionDTO);

                } catch (Exception e) {
                    System.err.println("模拟创建调度任务失败：从 " + closestSurplusGeohash + " 到 " + scarceGeohash + "。错误：" + e.getMessage());
                    reportContentBuilder.append(String.format(" - 无法创建从 %s 到 %s 的调度任务：%s\n", closestSurplusGeohash, scarceGeohash, e.getMessage()));
                }
            }
        } else {
            reportContentBuilder.append("当前车辆分布良好，无需大规模调度任务。\n");
            if (scarceAreas.isEmpty()) {
                reportContentBuilder.append("未发现稀缺区域。\n");
            }
            if (surplusAreas.isEmpty()) {
                reportContentBuilder.append("未发现富余区域。\n");
            }
        }

        reportContentBuilder.append("\n### 总结：\n");
        reportContentBuilder.append("请根据以上分析和建议，考虑是否需要人工干预或进一步优化调度策略。");

        // 大模型调用部分已注释，直接将构建的原始分析数据作为报告
        reportService.setLatestReport(reportContentBuilder.toString());

        // 构建并返回 HourlySimulationReportDTO
        HourlySimulationReportDTO hourlyReport = new HourlySimulationReportDTO();
        hourlyReport.setReportGeneratedTime(LocalDateTime.now());
        hourlyReport.setPredictionTargetTime(predictionTargetTime);
        hourlyReport.setAreaStatuses(areaStatusList);
        hourlyReport.setDispatchSuggestions(dispatchSuggestionList);
        hourlyReport.setRawReportContent(reportContentBuilder.toString());
        return hourlyReport;
    }

    // 修改后的 simulateDayData 方法：每小时计算后立即保存
    public List<HourlySimulationReportDTO> simulateDayData(int year, int month, int day) {
        List<HourlySimulationReportDTO> dailyReports = new ArrayList<>();
        LocalDate reportDate = LocalDate.of(year, month, day);

        // 【关键修改】：在开始处理一天的预测前，先删除该日期已有的报告
        dailySimulationReportService.deleteReportsByDate(reportDate);
        dailyDispatchSuggestionService.deleteSuggestionsByDate(reportDate);

        LocalDateTime startOfDay = LocalDateTime.of(year, month, day, 0, 0, 0);

        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime predictionTargetTime = startOfDay.plusHours(hour);
            System.out.println("--- 正在处理 " + predictionTargetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " 的预测 ---");
            HourlySimulationReportDTO hourlyReport = processHourlyData(predictionTargetTime); // 处理当前小时数据
            dailyReports.add(hourlyReport); // 将结果添加到总列表

            // 【关键修改】：每处理完一个小时，就将该小时的数据保存到数据库
            List<DailySimulationReport> entitiesToSaveThisHour = new ArrayList<>();
            for (AreaStatusDTO areaStatus : hourlyReport.getAreaStatuses()) {
                DailySimulationReport entity = new DailySimulationReport();
                entity.setReportDate(reportDate);
                entity.setPredictionTargetTime(areaStatus.getPredictionTime());
                entity.setGeohash(areaStatus.getGeohash());

                // 【修正纬度赋值】：确保值在数据库列范围内 (DECIMAL(10,8) 允许 -99.99999999 到 99.99999999)
                double latDouble = areaStatus.getLatitude();
                if (latDouble > 99.99999999) latDouble = 99.99999999;
                if (latDouble < -99.99999999) latDouble = -99.99999999;
                entity.setLatitude(new BigDecimal(latDouble).setScale(8, RoundingMode.HALF_UP));

                // 【修正经度赋值】：如果数据库列是 DECIMAL(11,8) 允许 -999.99999999 到 999.99999999
                // 经度有效范围是 -180 到 +180，通常不会超过两位整数，但如果数据源有异常，也可类似处理
                double lonDouble = areaStatus.getLongitude();
                if (lonDouble > 999.99999999) lonDouble = 999.99999999; // 假设允许三位整数
                if (lonDouble < -999.99999999) lonDouble = -999.99999999;
                entity.setLongitude(new BigDecimal(lonDouble).setScale(8, RoundingMode.HALF_UP));

                entity.setStatus(areaStatus.getStatus());

                // 【修正利用率赋值】：确保值在数据库列范围内 (DECIMAL(5,4) 允许 -9.9999 到 9.9999)
                double utilRateDouble = areaStatus.getUtilizationRate();
                if (utilRateDouble > 9.9999) utilRateDouble = 9.9999;
                if (utilRateDouble < 0.0) utilRateDouble = 0.0; // 利用率通常不为负
                entity.setUtilizationRate(new BigDecimal(utilRateDouble).setScale(4, RoundingMode.HALF_UP));

                entity.setCurrentBikes(areaStatus.getCurrentBikes());
                entity.setParkingCapacity(areaStatus.getParkingCapacity());
                entity.setPredictedPickups(areaStatus.getPredictedPickups());
                entity.setPredictedDropoffs(areaStatus.getPredictedDropoffs());
                entity.setFutureBikes(areaStatus.getFutureBikes());
                entity.setFutureRemainingSpots(areaStatus.getFutureRemainingSpots());
                entitiesToSaveThisHour.add(entity);
            }
            if (!entitiesToSaveThisHour.isEmpty()) {
                dailySimulationReportService.saveDailyReports(entitiesToSaveThisHour); // 每次只插入当前小时的区域状态数据
                System.out.println("   已保存 " + entitiesToSaveThisHour.size() + " 条 " + predictionTargetTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " 时刻的区域状态数据。");
            }

            // 【新增】保存每小时的调度建议
            List<DailyDispatchSuggestion> suggestionsToSaveThisHour = new ArrayList<>();
            for (DispatchSuggestionDTO suggestionDTO : hourlyReport.getDispatchSuggestions()) {
                DailyDispatchSuggestion entity = new DailyDispatchSuggestion();
                entity.setReportDate(reportDate);
                entity.setPredictionTargetTime(suggestionDTO.getPredictionTargetTime());
                entity.setSourceGeohash(suggestionDTO.getSourceGeohash());

                // 【修正源纬度赋值】：确保值在数据库列范围内
                double sourceLatDouble = suggestionDTO.getSourceLatitude();
                if (sourceLatDouble > 99.99999999) sourceLatDouble = 99.99999999;
                if (sourceLatDouble < -99.99999999) sourceLatDouble = -99.99999999;
                entity.setSourceLatitude(new BigDecimal(sourceLatDouble).setScale(8, RoundingMode.HALF_UP));

                // 【修正源经度赋值】：确保值在数据库列范围内
                double sourceLonDouble = suggestionDTO.getSourceLongitude();
                if (sourceLonDouble > 999.99999999) sourceLonDouble = 999.99999999;
                if (sourceLonDouble < -999.99999999) sourceLonDouble = -999.99999999;
                entity.setSourceLongitude(new BigDecimal(sourceLonDouble).setScale(8, RoundingMode.HALF_UP));

                entity.setTargetGeohash(suggestionDTO.getTargetGeohash());

                // 【修正目标纬度赋值】：确保值在数据库列范围内
                double targetLatDouble = suggestionDTO.getTargetLatitude();
                if (targetLatDouble > 99.99999999) targetLatDouble = 99.99999999;
                if (targetLatDouble < -99.99999999) targetLatDouble = -99.99999999;
                entity.setTargetLatitude(new BigDecimal(targetLatDouble).setScale(8, RoundingMode.HALF_UP));

                // 【修正目标经度赋值】：确保值在数据库列范围内
                double targetLonDouble = suggestionDTO.getTargetLongitude();
                if (targetLonDouble > 999.99999999) targetLonDouble = 999.99999999;
                if (targetLonDouble < -999.99999999) targetLonDouble = -999.99999999;
                entity.setTargetLongitude(new BigDecimal(targetLonDouble).setScale(8, RoundingMode.HALF_UP));

                entity.setSuggestedBikeCount(suggestionDTO.getSuggestedBikeCount());
                entity.setSuggestionTime(suggestionDTO.getSuggestionTime());
                suggestionsToSaveThisHour.add(entity);
            }
            if (!suggestionsToSaveThisHour.isEmpty()) {
                dailyDispatchSuggestionService.saveDailySuggestions(suggestionsToSaveThisHour); // 每次只插入当前小时的调度建议数据
                System.out.println("   已保存 " + suggestionsToSaveThisHour.size() + " 条 " + predictionTargetTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " 时刻的调度建议数据。");
            }


            System.out.println("--- " + predictionTargetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " 处理完成 ---");
        }
        return dailyReports; // 仍然返回给 SimulationController
    }
}