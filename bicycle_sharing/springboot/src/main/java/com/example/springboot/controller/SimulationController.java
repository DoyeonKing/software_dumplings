package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.task.DispatchScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * @return 模拟结果的提示信息
     */
    @GetMapping("/day")
    public Result simulateDay(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        try {
            dispatchScheduler.simulateDayData(year, month, day);
            return Result.success("成功触发模拟一天数据处理，请查看后端控制台日志。");
        } catch (Exception e) {
            System.err.println("模拟一天数据处理失败: " + e.getMessage());
            return Result.error("500", "模拟一天数据处理失败: " + e.getMessage());
        }
    }
}
