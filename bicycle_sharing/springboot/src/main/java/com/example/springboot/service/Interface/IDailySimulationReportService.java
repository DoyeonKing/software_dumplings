package com.example.springboot.service.Interface;

import com.example.springboot.entity.DailySimulationReport;
import java.time.LocalDate;
import java.util.List;

/**
 * Service 接口：每日模拟报告服务
 */
public interface IDailySimulationReportService {
    // 保存每日报告列表到数据库
    void saveDailyReports(List<DailySimulationReport> reports);
    // 根据日期和地图范围获取报告数据
    List<DailySimulationReport> getReportsByDateAndBounds(LocalDate reportDate, double minLat, double maxLat, double minLon, double maxLon);
    // 根据日期删除报告
    void deleteReportsByDate(LocalDate reportDate);
}