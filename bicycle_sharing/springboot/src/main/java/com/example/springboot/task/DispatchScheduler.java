package com.example.springboot.task;

import com.example.springboot.dto.ModelPredictionRequest;
import com.example.springboot.entity.Bikes;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.service.Interface.IBikesService;
import com.example.springboot.service.Interface.IDispatchTasksService;
import com.example.springboot.service.Interface.IEliteSitesService;
import com.example.springboot.service.Interface.IPredictionService;
import com.example.springboot.service.ReportService;
import org.springframework.scheduling.annotation.Scheduled; // 仍然需要导入，但暂时不用于主任务
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// 调度任务定时器
@Component
public class DispatchScheduler {

    private final IPredictionService predictionService;
    private final IBikesService bikesService;
    private final IEliteSitesService eliteSitesService;
    private final IDispatchTasksService dispatchTasksService;
    private final ReportService reportService;

    @Value("${dispatch.shortage-threshold}")
    private double shortageThreshold;
    @Value("${dispatch.surplus-threshold}")
    private double surplusThreshold;

    public DispatchScheduler(IPredictionService predictionService,
                             IBikesService bikesService,
                             IEliteSitesService eliteSitesService,
                             IDispatchTasksService dispatchTasksService,
                             ReportService reportService) {
        this.predictionService = predictionService;
        this.bikesService = bikesService;
        this.eliteSitesService = eliteSitesService;
        this.dispatchTasksService = dispatchTasksService;
        this.reportService = reportService;
    }

//    // 原始的定时任务方法，现在可以保留或注释掉，根据需要决定是否仍然需要周期性执行
//    // 如果您只希望通过模拟接口触发，可以注释掉此处的 @Scheduled
//    @Scheduled(cron = "0 0 * * * ?") // 示例：恢复为每小时整点执行，或根据实际生产需求调整
//    public void generateDispatchTasksAndReportScheduled() {
//        System.out.println("【定时任务】开始执行定时任务：生成调度任务和报告 - " + LocalDateTime.now());
//        processHourlyData(LocalDateTime.now().plusHours(1)); // 处理下一小时的数据
//        System.out.println("【定时任务】定时任务执行完毕。");
//    }


    /**
     * 模拟一整天的数据调用和处理
     * @param year 模拟年份
     * @param month 模拟月份
     * @param day 模拟日期
     */
    public void simulateDayData(int year, int month, int day) {
        System.out.println("【模拟任务】开始模拟一天的数据调用和处理，日期: " + year + "-" + month + "-" + day);
        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime predictionTime = LocalDateTime.of(year, month, day, hour, 0, 0);
            System.out.println("【模拟任务】正在处理模拟时间点: " + predictionTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            processHourlyData(predictionTime); // 调用核心处理逻辑
            System.out.println("---------------------------------------------------------");
        }
        System.out.println("【模拟任务】一天数据模拟完成。");
    }


    /**
     * 核心业务逻辑：获取数据、调用预测模型、判断区域状态、生成调度建议、存储报告
     * 这是一个私有方法，供定时任务和模拟任务调用
     * @param predictionTargetTime 预测的目标时间点
     */
    private void processHourlyData(LocalDateTime predictionTargetTime) {
        StringBuilder reportContentBuilder = new StringBuilder();
        reportContentBuilder.append("当前自行车共享系统在各个精英站点的状态分析及调度建议报告：\n\n");
        reportContentBuilder.append("报告生成时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        reportContentBuilder.append("预测目标时间：").append(predictionTargetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");


        // 1. 获取所有精英站点信息 (包含停车容量)
        List<EliteSites> allEliteSites = eliteSitesService.getAllEliteSites();
        if (allEliteSites.isEmpty()) {
            System.out.println("没有配置精英站点，跳过调度任务生成。");
            reportContentBuilder.append("警告：未找到任何精英站点信息，无法进行调度分析。\n");
            reportService.setLatestReport(reportContentBuilder.toString());
            return;
        }
        reportContentBuilder.append("### 区域状态分析：\n");

        // 2. 获取所有区域的当前自行车数量
        // TODO: 实际应调用 bikesService.countBikesByGeohash(geohash) 来获取真实数据
        Map<String, Long> currentBikesByGeohash = new HashMap<>();
        for (EliteSites site : allEliteSites) {
            // 暂时模拟一个随机值，确保有数据进行后续计算
            currentBikesByGeohash.put(site.getGeohash(), (long) (Math.random() * 50));
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

            } catch (Exception e) {
                System.err.println("调用预测API或处理区域 " + geohash + " 数据时发生错误: " + e.getMessage());
                reportContentBuilder.append(String.format(" - 区域 %s: 预测或处理数据失败：%s\n", geohash, e.getMessage()));
            }
        }

        reportContentBuilder.append("\n### 调度任务建议：\n");

        // 4. 基于就近原则生成调度任务
        if (!scarceAreas.isEmpty() && !surplusAreas.isEmpty()) {
            reportContentBuilder.append("系统检测到车辆分布不均，建议进行以下调度：\n");
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
    }
}
