package com.example.springboot.service.implementation;

import com.example.springboot.dto.DispatchTaskRequest;
import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.mapper.BikesMapper;
import com.example.springboot.mapper.DispatchTasksMapper; // 导入纠正后的Mapper接口名
import com.example.springboot.service.Interface.IDispatchTasksService; // 导入纠正后的Service接口名
import com.github.pagehelper.PageHelper; // 导入PageHelper
import com.github.pagehelper.PageInfo; // 导入PageInfo
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // 导入Service注解
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Autowired
    private BikesMapper bikesMapper; // 注入 MyBatis Mapper

    @Override
    @Transactional // 确保操作的原子性
    public DispatchTasks createDispatchTask(DispatchTaskRequest request) {
        // --- 1. 手动校验请求参数 ---
        if (request.getStartGeohash() == null || request.getStartGeohash().trim().isEmpty()) {
            throw new IllegalArgumentException("起始地点 (startGeohash) 不能为空");
        }
        if (request.getEndGeohash() == null || request.getEndGeohash().trim().isEmpty()) {
            throw new IllegalArgumentException("终止地点 (endGeohash) 不能为空");
        }
        if (request.getAssignedTo() == null || request.getAssignedTo() <= 0) { // 假设assignedTo是正整数
            throw new IllegalArgumentException("工人ID (assignedTo) 不能为空且必须是有效数字");
        }
        if (request.getBikeCount() == null || request.getBikeCount() <= 0) {
            throw new IllegalArgumentException("调度数量 (bikeCount) 必须大于0");
        }

        // --- 2. 获取起始区域的可用自行车数量 (状态为 '待使用') ---
        int availableBikes = getAvailableBikesInArea(request.getStartGeohash());

        // --- 3. 校验调度数量是否大于可用自行车数 ---
        if (request.getBikeCount() > availableBikes) {
            throw new IllegalArgumentException(
                    "调度数量 (" + request.getBikeCount() +
                            ") 超过起始区域 (" + request.getStartGeohash() +
                            ") 可用自行车数 (" + availableBikes + ")。"
            );
        }

        // --- 4. 构建 DispatchTasks 实体 ---
        DispatchTasks task = new DispatchTasks();
        task.setStartGeohash(request.getStartGeohash());
        task.setEndGeohash(request.getEndGeohash());
        task.setAssignedTo(request.getAssignedTo());
        task.setBikeCount(request.getBikeCount());
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("未处理"); // 初始状态

        // --- 5. 保存调度任务 ---
        dispatchTasksMapper.insertDispatchTask(task);
        // 此时 task 对象的 taskId 属性已被 MyBatis 填充 (因为使用了 @Options)

        return task;
    }

    @Override
    public int getAvailableBikesInArea(String geohash) {
        // 调用 BikesMapper 接口方法，bikeStatus使用字符串"待使用"
        return bikesMapper.countByCurrentGeohashAndBikeStatus(geohash, "待使用");
    }



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

    @Override
    public List<DispatchTasks> getTasksByDateRangeAndAssignedTo(LocalDate startDate, LocalDate endDate, Integer assignedTo) {
        return dispatchTasksMapper.selectTasksByDateRangeAndAssignedTo(startDate, endDate, assignedTo);
    }
}