package com.example.springboot.mapper;

import com.example.springboot.entity.DailySimulationReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapper 接口：用于操作 daily_simulation_reports 表
 */
@Mapper
public interface DailySimulationReportMapper {
    // 批量插入每日模拟报告数据
    int insertBatch(List<DailySimulationReport> reports);

    // 根据日期和地图范围查询模拟报告数据
    List<DailySimulationReport> selectReportsByDateAndBounds(
            @Param("reportDate") LocalDate reportDate,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );

    // 根据日期删除已有报告 (用于重新生成某天的报告)
    int deleteByDate(@Param("reportDate") LocalDate reportDate);


    // 根据日期、时间点和geohash查找单个报告
    DailySimulationReport findByDateAndTimeAndGeohash(
            @Param("reportDate") LocalDate reportDate,
            @Param("predictionTargetTime") LocalDateTime predictionTargetTime,
            @Param("geohash") String geohash);

    // 更新单个报告
    void update(DailySimulationReport report);

    // 查找某个geohash在数据库中最晚的prediction_target_time
    LocalDateTime findLatestPredictionTargetTimeByGeohash(@Param("geohash") String geohash);

}