package com.example.springboot.controller;

import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.service.Interface.IDispatchTasksService; // 导入纠正后的Service接口名
import com.github.pagehelper.PageInfo; // 导入分页PageInfo
import com.util.JwtTokenUtil;
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.util.List; // 导入List
import java.util.Map; // 导入Map

/**
 * DispatchTasksController类空壳
 * 负责接收和处理与调度任务相关的HTTP请求框架
 */
@RestController // 标记这是一个RESTful控制器
@RequestMapping("/dispatchTasks") // 基础URL路径通常与资源名复数形式保持一致
public class DispatchTasksController { // 控制器类名与资源名复数形式保持一致

    @Autowired // 自动注入 DispatchTasksService
    private IDispatchTasksService dispatchTasksService;

    /**
     * 获取所有调度任务的 RESTful API 接口
     * URL: GET /api/dispatch-tasks/all
     * @return 返回所有 DispatchTasks 对象的 JSON 列表
     */
    @GetMapping("/all")
    public List<DispatchTasks> getAllTasks() {
        // 调用 Service 层获取数据
        return dispatchTasksService.getAllTasks();
    }

    /**
     * 获取所有未处理调度任务的 RESTful API 接口
     * URL: GET /api/dispatch-tasks/unprocessed
     * @return 返回所有未处理 DispatchTasks 对象的 JSON 列表
     */
    @GetMapping("/unprocessed") // 新增端点
    public List<DispatchTasks> getUnprocessedTasks() {
        return dispatchTasksService.getUnprocessedTasks();
    }

    /**
     * 获取所有处理中调度任务的 RESTful API 接口
     * URL: GET /api/dispatch-tasks/processing
     * @return 返回所有处理中 DispatchTasks 对象的 JSON 列表
     */
    @GetMapping("/processing") // 新增端点
    public List<DispatchTasks> getProcessingTasks() {
        return dispatchTasksService.getProcessingTasks();
    }

    /**
     * 获取所有处理完成调度任务的 RESTful API 接口
     * URL: GET /api/dispatch-tasks/completed
     * @return 返回所有处理完成 DispatchTasks 对象的 JSON 列表
     */
    @GetMapping("/completed") // 新增端点
    public List<DispatchTasks> getCompletedTasks() {
        return dispatchTasksService.getCompletedTasks();
    }

    /**
     * 根据工作人员ID获取其所有分配到的调度任务的 RESTful API 接口
     * URL: GET /api/dispatch-tasks/by-staff/{assignedToId}
     * @param assignedToId 工作人员ID
     * @return 包含该工作人员所有分配任务的 JSON 列表
     */
    @GetMapping("/by-staff/{assignedToId}") // 新增端点，使用 PathVariable
    public List<DispatchTasks> getTasksByAssignedTo(@PathVariable Integer assignedToId) {
        // 调用 Service 层获取数据
        return dispatchTasksService.getTasksByAssignedTo(assignedToId);
    }
}