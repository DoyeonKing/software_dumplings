package com.example.springboot.service.Interface;

import com.example.springboot.entity.DailySimulationReport;
import java.time.LocalDate;
import java.util.List;

public interface IDailySimulationReportService {
    void saveDailyReports(List<DailySimulationReport> reports);
    List<DailySimulationReport> getReportsByDateAndBounds(LocalDate reportDate, double minLat, double maxLat, double minLon, double maxLon);
    void deleteReportsByDate(LocalDate reportDate);
}