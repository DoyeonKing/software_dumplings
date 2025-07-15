package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.task.DispatchScheduler;
import com.example.springboot.dto.HourlySimulationReportDTO; // 新增导入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; // 确保导入

/**
 * 模拟数据控制器
 * 用于手动触发 DispatchScheduler 中的一天数据模拟。
 */
@RestController
@RequestMapping("/api/simulate")
public class SimulationController {

    private final DispatchScheduler dispatchScheduler;

    public SimulationController(DispatchScheduler dispatchScheduler) {
        this.dispatchScheduler = dispatchScheduler;
    }

    /**
     * 触发模拟一天的数据调用和处理
     * 示例：GET /api/simulate/day?year=2019&month=1&day=1
     * @param year 模拟年份
     * @param month 模拟月份
     * @param day 模拟日期
     * @return 包含所有模拟数据的Result对象。Result的data字段将是 List<HourlySimulationReportDTO>
     */
    @GetMapping("/day")
    public Result simulateDay( // <-- 关键修正：这里不再使用泛型参数 List<HourlySimulationReportDTO>
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        try {
            List<HourlySimulationReportDTO> dailyReports = dispatchScheduler.simulateDayData(year, month, day);
            // 这里 Result.success(dailyReports) 会正确调用 Result.java 中的 success(Object data) 方法
            // dailyReports 是 List<HourlySimulationReportDTO> 类型，它是一个 Object
            return Result.success(dailyReports);
        } catch (Exception e) {
            System.err.println("模拟一天数据处理失败: " + e.getMessage());
            e.printStackTrace(); // 打印详细堆栈信息，便于调试
            return Result.error("500", "模拟一天数据处理失败: " + e.getMessage());
        }
    }
}