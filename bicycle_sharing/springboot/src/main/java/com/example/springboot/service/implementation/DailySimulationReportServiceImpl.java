package com.example.springboot.service.implementation;

import com.example.springboot.entity.DailySimulationReport;
import com.example.springboot.mapper.DailySimulationReportMapper;
import com.example.springboot.service.Interface.IDailySimulationReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 导入 @Transactional
import java.time.LocalDate;
import java.util.List;

/**
 * Service 实现类：每日模拟报告服务
 */
@Service
public class DailySimulationReportServiceImpl implements IDailySimulationReportService {

    private final DailySimulationReportMapper dailySimulationReportMapper;

    public DailySimulationReportServiceImpl(DailySimulationReportMapper dailySimulationReportMapper) {
        this.dailySimulationReportMapper = dailySimulationReportMapper;
    }

    @Override
    @Transactional // 确保批量插入在事务中，保证数据一致性
    public void saveDailyReports(List<DailySimulationReport> reports) {
        if (reports != null && !reports.isEmpty()) {
            // 在插入前，先删除该日期已有的报告，确保数据唯一性并避免重复插入
            // 假设所有报告都属于同一个日期
            LocalDate reportDate = reports.get(0).getReportDate();
            dailySimulationReportMapper.deleteByDate(reportDate);
            dailySimulationReportMapper.insertBatch(reports);
        }
    }

    @Override
    public List<DailySimulationReport> getReportsByDateAndBounds(LocalDate reportDate, double minLat, double maxLat, double minLon, double maxLon) {
        return dailySimulationReportMapper.selectReportsByDateAndBounds(reportDate, minLat, maxLat, minLon, maxLon);
    }

    @Override
    @Transactional
    public void deleteReportsByDate(LocalDate reportDate) {
        dailySimulationReportMapper.deleteByDate(reportDate);
    }
}