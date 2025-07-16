package com.example.springboot.service.Interface;

import com.example.springboot.entity.DailySimulationReport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IDailySimulationReportService {
    void saveDailyReports(List<DailySimulationReport> reports);
    List<DailySimulationReport> getReportsByDateAndBounds(LocalDate reportDate, double minLat, double maxLat, double minLon, double maxLon);
    void deleteReportsByDate(LocalDate reportDate);

    void upsertDailyReport(DailySimulationReport report);
    LocalDateTime findLatestPredictionTargetTimeByGeohash(String geohash);

    //根据日期和精确时间点查询报告
    List<DailySimulationReport> getReportsByDateAndTime(LocalDate reportDate, LocalDateTime predictionTargetTime);


}