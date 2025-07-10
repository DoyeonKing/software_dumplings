package com.example.springboot.service.implementation;

import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.mapper.DispatchTasksMapper; // 导入纠正后的Mapper接口名
import com.example.springboot.service.Interface.IDispatchTasksService; // 导入纠正后的Service接口名
import com.github.pagehelper.PageHelper; // 导入PageHelper
import com.github.pagehelper.PageInfo; // 导入PageInfo
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // 导入Service注解

import java.util.List; // 导入List

/**
 * DispatchTasksServiceImpl类空壳
 * 实现IDispatchTasksService接口，包含调度任务业务逻辑的具体实现框架
 */
@Service // 标记这是一个Spring管理的Service组件
public class DispatchTasksServiceImpl implements IDispatchTasksService { // 实现接口名纠正为IDispatchTasksService

    // 空壳：不在此处定义任何方法实现
    @Autowired // 自动注入 DispatchTasksMapper
    private DispatchTasksMapper dispatchTasksMapper;

    @Override
    public List<DispatchTasks> getAllTasks() {
        // 直接调用 Mapper 获取所有任务数据
        return dispatchTasksMapper.selectAllTasks();
    }
    @Override
    public List<DispatchTasks> getUnprocessedTasks() { // 新增实现
        return dispatchTasksMapper.selectUnprocessedTasks();
    }

    @Override
    public List<DispatchTasks> getProcessingTasks() { // 新增实现
        return dispatchTasksMapper.selectProcessingTasks();
    }

    @Override
    public List<DispatchTasks> getCompletedTasks() { // 新增实现
        return dispatchTasksMapper.selectCompletedTasks();
    }

    /**
     * 根据工作人员ID查询所有调度任务，支持分页和状态筛选。
     * 如果 assignedTo 为 null，则查询所有任务（不按工作人员筛选）。
     * @param assignedTo 工作人员ID (如果为null则查询所有任务)
     * @return 包含分页信息的调度任务列表
     * @throws CustomException 如果获取失败
     */
    @Override
    public List<DispatchTasks> getTasksByAssignedTo(Integer assignedTo) { // 新增实现
        // 调用 Mapper 获取指定工作人员的任务数据
        return dispatchTasksMapper.selectTasksByAssignedTo(assignedTo);
    }
}