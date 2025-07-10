package com.example.springboot.service.Interface;

import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.github.pagehelper.PageInfo; // 导入分页类

import java.util.List; // 导入List

/**
 * IDispatchTasksService接口空壳
 * 定义调度任务相关的业务操作契约框架
 */
public interface IDispatchTasksService { // 接口名与实体类名保持一致，改为IDispatchTasksService

    /**
     * 获取所有调度任务的业务逻辑接口
     * @return 包含所有调度任务的列表
     */
    List<DispatchTasks> getAllTasks();

    /**
     * 获取所有未处理调度任务的业务逻辑接口
     * @return 包含所有未处理调度任务的列表
     */
    List<DispatchTasks> getUnprocessedTasks(); // 新增方法

    /**
     * 获取所有处理中调度任务的业务逻辑接口
     * @return 包含所有处理中调度任务的列表
     */
    List<DispatchTasks> getProcessingTasks(); // 新增方法

    /**
     * 获取所有处理完成调度任务的业务逻辑接口
     * @return 包含所有处理完成调度任务的列表
     */
    List<DispatchTasks> getCompletedTasks(); // 新增方法

    /**
     * 根据工作人员ID获取其所有分配到的调度任务
     * @param assignedTo 工作人员ID
     * @return 包含该工作人员所有分配任务的列表
     */
    List<DispatchTasks> getTasksByAssignedTo(Integer assignedTo); // 新增方法
}