package com.example.springboot.service.implementation;

import com.example.springboot.entity.DailySimulationReport;
import com.example.springboot.mapper.DailySimulationReportMapper;
import com.example.springboot.service.Interface.IDailySimulationReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            // 【关键修改】：移除了这里对该日期所有报告进行删除的逻辑。
            // LocalDate reportDate = reports.get(0).getReportDate();
            // dailySimulationReportMapper.deleteByDate(reportDate); // Removed this line
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

     @Override
    @Transactional
    public void upsertDailyReport(DailySimulationReport report) {
        // 尝试查找现有记录
        DailySimulationReport existingReport = dailySimulationReportMapper.findByDateAndTimeAndGeohash(
                report.getReportDate(),
                report.getPredictionTargetTime(),
                report.getGeohash()
        );

        if (existingReport != null) {
            // 如果记录存在，更新它
            report.setId(existingReport.getId()); // 确保更新时使用正确的ID
            dailySimulationReportMapper.update(report);
            System.out.println("DEBUG: Updated DailySimulationReport for geohash: " + report.getGeohash() + " at " + report.getPredictionTargetTime());
        } else {
            // 如果记录不存在，插入它
            System.out.println("记录不存在");
        }
    }
}