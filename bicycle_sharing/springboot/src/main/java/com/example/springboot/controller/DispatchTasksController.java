package com.example.springboot.controller;

import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.dto.DispatchTaskRequest;
import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.service.Interface.IDispatchTasksService; // 导入纠正后的Service接口名
import com.github.pagehelper.PageInfo; // 导入分页PageInfo
import com.example.springboot.util.JwtTokenUtil;
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.time.LocalDate;
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
     * 创建新的调度任务。
     * POST /api/dispatchTasks/create
     * 请求体示例:
     * {
     *     "startGeohash": "wx4er",
     *     "endGeohash": "wx4ez",
     *     "assignedTo": 101,
     *     "bikeCount": 5
     * }
     * @param request 调度任务请求 DTO
     * @return 响应实体，包含成功信息或错误信息
     */
    @PostMapping("/create")
    public ResponseEntity<?> createDispatchTask(@RequestBody DispatchTaskRequest request) {
        try {
            DispatchTasks createdTask = dispatchTasksService.createDispatchTask(request);
            // 返回的taskId是Long类型，确保前端能正确接收
            return ResponseEntity.status(HttpStatus.CREATED).body("调度任务创建成功，任务ID：" + createdTask.getTaskId());
        } catch (IllegalArgumentException e) {
            // 参数校验失败或调度数量不足的业务异常
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 其他未知异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建调度任务失败：" + e.getMessage());
        }
    }

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

    /**
     * 根据日期范围和工作人员ID获取任务信息
     * URL: GET /dispatchTasks/tasks-by-date-range-and-staff
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param assignedTo 工作人员ID
     * @return ResponseEntity 包含任务信息的列表或错误信息
     */
    @GetMapping("/tasks-by-date-range-and-staff")
    public ResponseEntity<?> getTasksByDateRangeAndStaff(@RequestParam LocalDate startDate,
                                                         @RequestParam LocalDate endDate,
                                                         @RequestParam Integer assignedTo) {
        try {
            List<DispatchTasks> tasks = dispatchTasksService.getTasksByDateRangeAndAssignedTo(startDate, endDate, assignedTo);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取任务信息失败: " + e.getMessage());
        }
    }


    /**
     * API: PUT /dispatchTasks/{taskId}/start
     * 作用：触发调度任务的开始，选择具体的自行车并关联到任务。
     * @param taskId 调度任务的ID
     * @return 实际被调度的自行车ID列表
     */
    @PutMapping("/{taskId}/start")
    public Result startDispatch(@PathVariable Long taskId) {
        try {
            List<String> dispatchedBikeIds = dispatchTasksService.startDispatch(taskId);
            return Result.success("调度任务开始成功，已选择自行车", dispatchedBikeIds);
        } catch (IllegalArgumentException e) {
            return Result.error(Result.CODE_PARAM_ERROR, e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(Result.CODE_BIZ_ERROR, e.getMessage()); // 业务逻辑错误
        } catch (Exception e) {
            return Result.error(Result.CODE_SYS_ERROR, "开始调度任务失败: " + e.getMessage());
        }
    }

    /**
     * API: PUT /dispatchTasks/{taskId}/complete
     * 作用：完成调度任务，更新关联自行车的最终位置和状态。
     * @param taskId 调度任务的ID
     * @return 成功信息
     */
    @PutMapping("/{taskId}/complete")
    public Result completeDispatch(@PathVariable Long taskId) {
        try {
            dispatchTasksService.completeDispatch(taskId);
            return Result.success("调度任务完成成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(Result.CODE_PARAM_ERROR, e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(Result.CODE_BIZ_ERROR, e.getMessage()); // 业务逻辑错误
        } catch (Exception e) {
            return Result.error(Result.CODE_SYS_ERROR, "完成调度任务失败: " + e.getMessage());
        }
    }
}
