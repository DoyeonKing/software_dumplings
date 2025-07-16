package com.example.springboot.service.implementation;

import com.example.springboot.dto.HeatCell;
import com.example.springboot.dto.UtilizationResponse;
import com.example.springboot.entity.Bikes;
import com.example.springboot.entity.DailySimulationReport;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.BikesInTasksMapper;
import com.example.springboot.mapper.BikesMapper;
import com.example.springboot.mapper.DispatchTasksMapper;
import com.example.springboot.mapper.EliteSitesMapper;
import com.example.springboot.service.Interface.IBikesService;
import com.example.springboot.service.Interface.IDailySimulationReportService;
import com.example.springboot.service.Interface.IEliteSitesService;
import com.example.springboot.service.Interface.IPredictionService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * BikesServiceImpl类空壳
 * 实现IBikesService接口，包含自行车业务逻辑的具体实现框架
 */
@Service // 标记这是一个Spring管理的Service组件

public class BikesServiceImpl implements IBikesService { // 实现接口名纠正为IBikesService
    @Value("${dispatch.shortage-threshold}")
private double shortageThreshold;
@Value("${dispatch.surplus-threshold}")
private double surplusThreshold;
    @Autowired
    private  IPredictionService predictionService;
    @Autowired
    private  IEliteSitesService eliteSitesService;
    @Autowired
    private  IDailySimulationReportService dailySimulationReportService;
    @Resource
    private BikesMapper bikesMapper;



    public int getAllBikeCountByGeohash(String geohash) {
        return bikesMapper.countAllByCurrentGeohash(geohash);
    }

    /**
     * 根据区域编号获取单车使用率
     *
     * @param geohash 区域编码
     * @return UtilizationResponse 对象，包含使用率、在线数、使用中数和空闲数。
     */
    @Override
    public UtilizationResponse getVehicleUtilizationByGeohash(String geohash) {
        // 1. 获取该区域内的所有车辆总数
        int totalVehicles = bikesMapper.countAllByCurrentGeohash(geohash);

        // 2. 获取该区域内 '使用中' 车辆数
        int inUseVehicles = bikesMapper.countByCurrentGeohashAndBikeStatus(geohash, "使用中");

        // 3. 计算 '空闲' 车辆数
        int idleVehicles = totalVehicles - inUseVehicles;
        if (idleVehicles < 0) {
            idleVehicles = 0;
        }

        // 4. 计算使用率
        double utilizationRatePercentage;
        if (totalVehicles == 0) {
            utilizationRatePercentage = 0.0;
        } else {
            BigDecimal inUseBd = new BigDecimal(inUseVehicles);
            BigDecimal totalBd = new BigDecimal(totalVehicles);
            utilizationRatePercentage = inUseBd.divide(totalBd, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .doubleValue();
        }

        // 5. 构建并返回响应 DTO
        return new UtilizationResponse(
                utilizationRatePercentage,
                totalVehicles,
                inUseVehicles,
                idleVehicles
        );
    }


    /**
     * 获取所有可用（状态为“待使用”）的单车列表
     *
     * @return 可用单车列表
     * @throws CustomException 如果获取失败
     */
    @Override
    public List<Bikes> getAllAvailableBikes() {
        try {
            // 假设 bikesMapper 有一个 findAllByStatus 方法
            // 这里的 "待使用" 应该与数据库中 ENUM('使用中', '待使用') 的值严格匹配
            return bikesMapper.findAllByStatus("待使用");
        } catch (Exception e) {
            throw new CustomException("获取所有可用单车失败: " + e.getMessage(), "500");
        }
    }

/**
 * 获取单车列表，支持按状态筛选和分页
 * @param pageNum 页码
 * @param pageSize 每页数量
 * @param bikeStatus 单车状态 (可选，如果为null则查询所有状态)
 * @return 包含分页信息的单车列表
 * @throws CustomException 如果获取失败
 */
@Override
public PageInfo<Bikes> getBikesByPage(Integer pageNum, Integer pageSize, String bikeStatus) {
    try {
        // 启动 PageHelper 分页
        PageHelper.startPage(pageNum, pageSize);

        List<Bikes> bikesList;
        if (bikeStatus != null && !bikeStatus.isEmpty()) {
            // 如果指定了状态，则按状态查询
            bikesList = bikesMapper.findAllByStatus(bikeStatus);
        } else {
            // 如果未指定状态，则查询所有单车
            bikesList = bikesMapper.findAlltoPages(); // 假设 BikesMapper 有一个 findAll 方法
        }

        // 使用 PageInfo 包装查询结果，它会自动计算总数、总页数等
        return new PageInfo<>(bikesList);
    } catch (Exception e) {
        throw new CustomException("获取单车列表失败: " + e.getMessage(), "500");
    }
}

    /**
     * 获取单个单车的详细信息（处理后不包含bikeId）
     *
     * @param bikeId 单车唯一标识符
     * @return 单车详情对象，如果未找到则为 null
     * @throws CustomException 如果获取失败
     */
    @Override
    public Bikes getBikeDetails(String bikeId) {
        try {
            Bikes bike = bikesMapper.findByBikeId(bikeId);
            if (bike != null) {
                // 根据需求，不显示单车ID，可以在返回前将其置空
                bike.setBikeId(null);
            }
            return bike;
        } catch (Exception e) {
            throw new CustomException("获取单车详情失败: " + e.getMessage(), "500");
        }
    }
    /**
     * 获取指定地理范围内（视口内）的单车列表，支持按状态筛选
     * @param minLat 最小纬度
     * @param maxLat 最大纬度
     * @param minLon 最小经度
     * @param maxLon 最大经度
     * @param bikeStatus 单车状态 (可选，如果为null则查询所有状态)
     * @return 指定范围内的单车列表
     * @throws CustomException 如果获取失败
     */
    @Override
    public List<Bikes> getBikesInViewport(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLon, BigDecimal maxLon, String bikeStatus) {
        try {
            // 在这里可以添加参数校验，确保经纬度范围有效
            if (minLat == null || maxLat == null || minLon == null || maxLon == null) {
                throw new CustomException("地理范围参数不能为空", "400");
            }

            return bikesMapper.findInViewport(minLat, maxLat, minLon, maxLon, bikeStatus);
        } catch (CustomException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            throw new CustomException("获取视口内单车失败: " + e.getMessage(), "500");
        }
    }

    /**
     * 生成指定区域内的单车分布热力图数据 (统计每个网格单元内的所有单车)
     * @param minLat 整个大区域的最小纬度
     * @param maxLat 整个大区域的最大纬度
     * @param minLon 整个大区域的最小经度
     * @param maxLon 整个大区域的最大经度
     * @param gridCellsX 横向网格单元数量 (例如 50)
     * @param gridCellsY 纵向网格单元数量 (例如 50)
     * @return 包含热力图数据的列表
     * @throws CustomException 如果获取失败
     */
    @Override
    public List<HeatCell> generateBikeHeatmap(
            BigDecimal minLat,
            BigDecimal maxLat,
            BigDecimal minLon,
            BigDecimal maxLon,
            Integer gridCellsX,
            Integer gridCellsY) { // 修改：移除 searchRadiusDegrees 参数
        try {
            // 参数校验
            if (minLat == null || maxLat == null || minLon == null || maxLon == null ||
                    gridCellsX == null || gridCellsY == null || gridCellsX <= 0 || gridCellsY <= 0) {
                throw new CustomException("热力图参数不完整或无效", "400");
            }

            List<HeatCell> heatmapData = new ArrayList<>();

            // 计算纬度和经度的总范围
            BigDecimal latRange = maxLat.subtract(minLat);
            BigDecimal lonRange = maxLon.subtract(minLon);

            // 避免除以零：如果范围为0，则假设一个非常小的非零值，或者根据实际需求进行特殊处理
            // 对于单点或单线区域，我们将其视为一个唯一的“网格”，并统计其内部的单车数
            if (latRange.compareTo(BigDecimal.ZERO) == 0 && lonRange.compareTo(BigDecimal.ZERO) == 0) {
                // 如果是单个点，直接查询该点区域内的单车数量
                int count = bikesMapper.countInViewport(minLat, maxLat, minLon, maxLon);
                heatmapData.add(new HeatCell(minLat, minLon, count));
                return heatmapData;
            } else if (latRange.compareTo(BigDecimal.ZERO) == 0) {
                // 如果纬度范围为零，将它视为一个非常小的非零范围，以避免除以零
                latRange = new BigDecimal("0.0000001");
                minLat = maxLat.subtract(latRange.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP)); // 确保minLat和maxLat有微小差异
                maxLat = maxLat.add(latRange.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP));
            } else if (lonRange.compareTo(BigDecimal.ZERO) == 0) {
                // 如果经度范围为零，同理处理
                lonRange = new BigDecimal("0.0000001");
                minLon = maxLon.subtract(lonRange.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP));
                maxLon = maxLon.add(lonRange.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP));
            }


            // 计算每个网格单元的步长（纬度步长和经度步长）
            BigDecimal latStep = latRange.divide(new BigDecimal(gridCellsY), 10, RoundingMode.HALF_UP);
            BigDecimal lonStep = lonRange.divide(new BigDecimal(gridCellsX), 10, RoundingMode.HALF_UP);

            // 遍历每个网格单元
            for (int i = 0; i < gridCellsY; i++) { // 遍历纬度方向（行）
                for (int j = 0; j < gridCellsX; j++) { // 遍历经度方向（列）

                    // 计算当前网格单元的精确边界
                    BigDecimal cellMinLat = minLat.add(latStep.multiply(new BigDecimal(i)));
                    BigDecimal cellMaxLat = minLat.add(latStep.multiply(new BigDecimal(i + 1)));
                    BigDecimal cellMinLon = minLon.add(lonStep.multiply(new BigDecimal(j)));
                    BigDecimal cellMaxLon = minLon.add(lonStep.multiply(new BigDecimal(j + 1)));

                    // 为了热力图渲染通常需要一个中心点坐标，我们仍然计算单元格的中心点作为 HeatCell 的坐标
                    BigDecimal centerLat = cellMinLat.add(latStep.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP));
                    BigDecimal centerLon = cellMinLon.add(lonStep.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP));

                    // 查询这个小区域（当前网格单元）内的单车数量
                    int bikeCount = bikesMapper.countInViewport(cellMinLat, cellMaxLat, cellMinLon, cellMaxLon);

                    // 如果该区域有单车，则添加到热力图数据列表
                    if (bikeCount > 0) {
                        heatmapData.add(new HeatCell(centerLat, centerLon, bikeCount));
                    }
                }
            }
            return heatmapData;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("生成单车热力图失败: " + e.getMessage(), "500");
        }
    }

    @Override
    public List<Bikes> getAllBikeLocations() throws CustomException {
        try {
            // 假设 BikesMapper 有一个方法可以获取所有单车的经纬度信息
            return bikesMapper.getAllBikeLocations();
        } catch (Exception e) {
            throw new CustomException("获取单车位置信息失败: " + e.getMessage(), "500");
        }
    }

/**
     * 根据地理哈希列表实现统计每个区域的自行车数量。
     * 该方法调用 BikesMapper 从数据库中批量获取指定 geohash 区域的自行车数量，
     * 并在 Service 层手动构建为 Map<String, Long> 返回。
     *
     * @param geohashes 包含需要统计自行车数量的地理哈希编码的列表。
     * @return 一个 Map，其中 key 是地理哈希编码 (String)，value 是该区域的自行车数量 (Long)。
     */
    @Override
    public Map<String, Long> countBikesByGeohashes(List<String> geohashes) {
        // 【关键修改】：Mapper 返回 List<Map<String, Object>>，然后在这里手动进行转换
        List<Map<String, Object>> counts = bikesMapper.selectBikesCountByGeohashes(geohashes);

        // 将 List<Map<String, Object>> 转换为 Map<String, Long>
        return counts.stream()
                     .collect(Collectors.toMap(
                         map -> (String) map.get("geohash"), // Map 的键是 'geohash' 列的值
                         map -> ((Number) map.get("count")).longValue() // Map 的值是 'count' 列的值，转换为 Long
                     ));
    }

    /**
     * 【核心修改】重新计算并保存受影响区域的每小时报告，并连锁更新后续小时的报告。
     * 从变化发生后的下一个整点小时开始，**一直更新到数据库中最晚的预测时间点**。
     *
     * @param geohash 发生变化的区域 geohash
     * @param changeTime 变化发生的具体时间 (例如：调度任务完成时间)
     */
    @Transactional
    public void recalculateAndSaveHourlyReport(String geohash, LocalDateTime changeTime) {
        System.out.println("DEBUG: Triggered recalculateAndSaveHourlyReport for geohash: " + geohash + " at " + changeTime);

        // 1. 获取该 geohash 下最新的“待使用”自行车数量 (作为连锁计算的起点)
        Map<String, Long> currentBikesMap = this.countBikesByGeohashes(Collections.singletonList(geohash));
        long currentBikesForPrediction = currentBikesMap.getOrDefault(geohash, 0L);
        System.out.println("DEBUG: Initial current bikes for " + geohash + " (from DB): " + currentBikesForPrediction);

        // 2. 获取站点容量信息
        EliteSites site = eliteSitesService.getEliteSiteByGeohash(geohash);
        if (site == null || site.getParkingCapacity() == null || site.getParkingCapacity() <= 0) {
            System.err.println("ERROR: 站点 " + geohash + " 信息无效或容量为零，无法重新计算报告。");
            return;
        }
        Integer parkingCapacity = site.getParkingCapacity();
        System.out.println("DEBUG: Parking capacity for " + geohash + ": " + parkingCapacity);

        // 3. 确定开始重新计算的第一个预测时间点 (下一个整点小时)
        LocalDateTime startPredictionTime = changeTime.truncatedTo(ChronoUnit.HOURS).plusHours(1);
        System.out.println("DEBUG: Start prediction time for recalculation loop: " + startPredictionTime);

        // 4. 【新增】获取该geohash在数据库中最晚的预测时间点
        // 如果数据库中没有该geohash的记录，或者最晚时间早于startPredictionTime，则以startPredictionTime为结束点
        LocalDateTime latestDbPredictionTime = dailySimulationReportService.findLatestPredictionTargetTimeByGeohash(geohash);
        LocalDateTime endPredictionTime = latestDbPredictionTime != null && latestDbPredictionTime.isAfter(startPredictionTime)
                                            ? latestDbPredictionTime
                                            : startPredictionTime; // 如果没有数据或数据更早，就只预测startPredictionTime这一个点

        System.out.println("DEBUG: Latest DB prediction time for " + geohash + ": " + latestDbPredictionTime + ", calculated loop end time: " + endPredictionTime);


        // 5. 循环从起始预测时间到数据库中已有的最晚预测时间点 (或当天23:00，如果数据库中时间更早)
        for (LocalDateTime predictionTargetTime = startPredictionTime;
             !predictionTargetTime.isAfter(endPredictionTime); // 循环直到等于或超过最晚时间点
             predictionTargetTime = predictionTargetTime.plusHours(1)) {

            System.out.println("DEBUG: Processing recalculation for " + geohash + " at " + predictionTargetTime + " with currentBikes: " + currentBikesForPrediction);

            // a. 调用预测API获取预测的取车量和停库量 (对于每个预测时间点都需调用)
            Integer predictedPickups = null;
            Integer predictedDropoffs = null;
            try {
                predictedPickups = predictionService.getPredictedPickupCount(geohash, predictionTargetTime).block();
                predictedDropoffs = predictionService.getPredictedParkingCount(geohash, predictionTargetTime).block();
                System.out.println("DEBUG: Predicted pickups for " + geohash + " at " + predictionTargetTime + ": " + predictedPickups);
                System.out.println("DEBUG: Predicted dropoffs for " + geohash + " at " + predictionTargetTime + ": " + predictedDropoffs);
            } catch (Exception e) {
                System.err.println("ERROR: 调用预测API失败，无法更新报告 for geohash " + geohash + " at " + predictionTargetTime + ": " + e.getMessage());
                return; // 预测失败则停止连锁更新
            }

            if (predictedPickups == null || predictedDropoffs == null) {
                System.err.println("ERROR: 区域 " + geohash + " 在 " + predictionTargetTime + " 未能获取到完整的预测数据，无法更新报告。");
                return; // 数据缺失则停止连锁更新
            }

            // b. 重新计算未来车辆数、利用率和状态
            long futureBikes = currentBikesForPrediction - predictedPickups + predictedDropoffs;
            futureBikes = Math.max(0, futureBikes);
            double utilizationRate = (double) futureBikes / parkingCapacity;

            String areaStatus;
            if (utilizationRate < shortageThreshold) {
                areaStatus = "稀缺区";
            } else if (utilizationRate > surplusThreshold) {
                areaStatus = "富余区";
            } else {
                areaStatus = "稳定区";
            }
            System.out.println("DEBUG: New status for " + geohash + " at " + predictionTargetTime + ": " + areaStatus + ", Utilization: " + utilizationRate);


            // c. 构建 DailySimulationReport 实体并保存到数据库 (使用 upsert 逻辑)
            DailySimulationReport updatedReport = new DailySimulationReport();
            updatedReport.setReportDate(predictionTargetTime.toLocalDate());
            updatedReport.setPredictionTargetTime(predictionTargetTime);
            updatedReport.setGeohash(geohash);
            updatedReport.setLatitude(site.getCenterLat().setScale(8, RoundingMode.HALF_UP));
            updatedReport.setLongitude(site.getCenterLon().setScale(8, RoundingMode.HALF_UP));
            updatedReport.setStatus(areaStatus);
            updatedReport.setUtilizationRate(new BigDecimal(utilizationRate).setScale(4, RoundingMode.HALF_UP));
            updatedReport.setCurrentBikes(currentBikesForPrediction); // 这个小时的起始车辆数
            updatedReport.setParkingCapacity(parkingCapacity);
            updatedReport.setPredictedPickups(predictedPickups);
            updatedReport.setPredictedDropoffs(predictedDropoffs);
            updatedReport.setFutureBikes(futureBikes);
            updatedReport.setFutureRemainingSpots(parkingCapacity - futureBikes);

            dailySimulationReportService.upsertDailyReport(updatedReport);
            System.out.println("DEBUG: DailySimulationReport for " + geohash + " at " + predictionTargetTime + " successfully updated/inserted.");

            // 【关键】为下一个小时的预测，更新 currentBikesForPrediction 为当前小时的 futureBikes
            currentBikesForPrediction = futureBikes;
        }
    }

}