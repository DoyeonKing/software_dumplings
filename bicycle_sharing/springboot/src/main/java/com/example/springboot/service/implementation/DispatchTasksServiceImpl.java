package com.example.springboot.service.implementation;

import com.example.springboot.dto.DispatchTaskRequest;
import com.example.springboot.entity.Bikes;
import com.example.springboot.entity.BikesInTasks;
import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import com.example.springboot.entity.EliteSites;
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.mapper.BikesInTasksMapper;
import com.example.springboot.mapper.BikesMapper;
import com.example.springboot.mapper.DispatchTasksMapper; // 导入纠正后的Mapper接口名
import com.example.springboot.mapper.EliteSitesMapper;
import com.example.springboot.service.Interface.IDispatchTasksService; // 导入纠正后的Service接口名
import com.github.pagehelper.PageHelper; // 导入PageHelper
import com.github.pagehelper.PageInfo; // 导入PageInfo
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // 导入Service注解
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private BikesInTasksMapper bikesInTasksMapper; // 注入新的 BikesInTasksMapper
    @Autowired
    private EliteSitesMapper eliteSitesMapper; // 注入 EliteSitesMapper
    @Autowired
    private BikesServiceImpl bikesService; // 注入 BikesServiceImpl 实例

    /**
     * 创建新的调度任务。
     * @param request 调度任务请求 DTO
     * @param createdAt 任务的创建时间点（可为模拟时间）
     * @return 创建的 DispatchTasks 实体
     */
    /**
     * 创建新的调度任务。
     * @param request 调度任务请求 DTO
     * @param createdAt 任务的创建时间点（可为模拟时间）
     * @return 创建的 DispatchTasks 实体
     */
    @Override
    @Transactional
    public DispatchTasks createDispatchTask(DispatchTaskRequest request, LocalDateTime createdAt) { // 【修改】接收 createdAt 参数
        // 1. 校验参数
        if (request.getStartGeohash() == null || request.getEndGeohash() == null ||
                request.getBikeCount() == null || request.getBikeCount() <= 0) {
            throw new IllegalArgumentException("起始区域、结束区域和调度数量不能为空且数量需大于0。");
        }

        // 2. 验证源区域是否存在且有足够可用车辆 (仅做数量检查，不实际选择或锁定车辆)
        Map<String, Long> availableBikesMap = bikesService.countBikesByGeohashes(Collections.singletonList(request.getStartGeohash()));
        Long availableBikes = availableBikesMap.getOrDefault(request.getStartGeohash(), 0L);
        if (availableBikes < request.getBikeCount()) {
            // 在任务创建时，我们仅检查理论上是否有足够车辆，不实际锁定。
            // 如果这里显示不足，意味着即使未来开始调度，也可能没有足够车辆。
            // 根据业务需求，可以改为警告或允许创建但后续在startDispatch中处理实际不足。
            // 这里沿用原逻辑，但在任务创建时就进行严格检查。
            throw new IllegalArgumentException("源区域 " + request.getStartGeohash() + " 可用单车数量不足。当前可用: " + availableBikes + ", 需调度: " + request.getBikeCount());
        }

        // 3. 验证目标区域是否存在
        EliteSites endSite = eliteSitesMapper.getEliteSiteByGeohash(request.getEndGeohash());
        if (endSite == null) {
            throw new IllegalArgumentException("目标区域 " + request.getEndGeohash() + " 不存在或非精英站点。");
        }

        // --- 移除：原第4步（选择自行车并更新状态为“调度中”） ---
        // --- 移除：原第6步（关联自行车与调度任务） ---

        // 4. 创建调度任务实体并保存
        DispatchTasks dispatchTask = new DispatchTasks();
        dispatchTask.setStartGeohash(request.getStartGeohash());
        dispatchTask.setEndGeohash(request.getEndGeohash());
        dispatchTask.setBikeCount(request.getBikeCount());
        dispatchTask.setAssignedTo(request.getAssignedTo()); // 工作人员ID
        dispatchTask.setStatus("未处理"); // 初始状态为“未处理”
        dispatchTask.setCreatedAt(createdAt); // 使用传入的创建时间
        // completedAt 字段在任务完成时设置

        dispatchTasksMapper.insertDispatchTask(dispatchTask); // 假设 insertDispatchTask 方法存在且返回ID

        return dispatchTask; // 返回创建的任务实体，包含生成的taskId
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

        /**
     * 开始一个调度任务，执行自行车的选择、去重和任务关联。
     * 自行车位置在此阶段不改变，只更新状态为“调度中”。
     */
    @Override
    @Transactional
    public List<String> startDispatch(Long taskId) {
        // --- 1. 获取调度任务 ---
        DispatchTasks task = dispatchTasksMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("调度任务ID: " + taskId + " 不存在。");
        }
        if (!"未处理".equals(task.getStatus())) {
            throw new IllegalArgumentException("调度任务ID: " + taskId + " 状态不正确，无法开始调度 (当前状态: " + task.getStatus() + ")。");
        }

        // --- 2. 获取起始区域所有 "待使用" 状态的自行车 ---
        List<Bikes> availableBikesInStartArea = bikesMapper.findAvailableBikesByGeohash(task.getStartGeohash());
        if (availableBikesInStartArea.isEmpty()) {
            throw new IllegalArgumentException("起始区域 (" + task.getStartGeohash() + ") 暂无可用自行车，无法开始调度。");
        }

        // --- 3. 获取目标区域所有 "待使用" 状态的自行车ID (用于去重) ---
        List<Bikes> bikesInTargetArea = bikesMapper.findAvailableBikesByGeohash(task.getEndGeohash());
        Set<String> bikeIdsInTargetArea = bikesInTargetArea.stream()
                .map(Bikes::getBikeId)
                .collect(Collectors.toSet());

        // --- 4. 从起始区域的可用自行车中筛选掉已经在目标区域的自行车 ---
        List<Bikes> filteredBikesForDispatch = availableBikesInStartArea.stream()
                .filter(bike -> !bikeIdsInTargetArea.contains(bike.getBikeId()))
                .collect(Collectors.toList());

        // --- 5. 校验调度数量是否足够 ---
        if (task.getBikeCount() > filteredBikesForDispatch.size()) {
            throw new IllegalArgumentException(
                    "调度数量 (" + task.getBikeCount() +
                            ") 超过起始区域可用自行车数 (排除目标区域重复车后，实际可调度数量为: " + filteredBikesForDispatch.size() + ")。"
            );
        }

        // --- 6. 随机选择指定数量的自行车 ---
        Collections.shuffle(filteredBikesForDispatch);
        List<Bikes> selectedBikesForDispatch = filteredBikesForDispatch.stream()
                .limit(task.getBikeCount())
                .collect(Collectors.toList());

        // --- 7. 更新被选中自行车的状态，并记录到 bikes_in_tasks 表 ---
        List<BikesInTasks> bikesInTasksEntries = new ArrayList<>();
        List<String> dispatchedBikeIds = new ArrayList<>(); // 用于返回给前端
        for (Bikes bike : selectedBikesForDispatch) {
            String newBikeStatus = "调度中"; // 自行车状态更新为“调度中”

            // 更新自行车的状态，但**不更新其经纬度**。位置保持在起始地。
            bikesMapper.updateBikeStatusAndLocation(
                    bike.getBikeId(),
                    newBikeStatus,
                    null, // 经度不更新
                    null, // 纬度不更新
                    null  // geohash不更新
            );

            // 记录到 bikes_in_tasks 表
            bikesInTasksEntries.add(new BikesInTasks(task.getTaskId(), bike.getBikeId()));
            dispatchedBikeIds.add(bike.getBikeId());
        }

        if (!bikesInTasksEntries.isEmpty()) {
            bikesInTasksMapper.insertBatch(bikesInTasksEntries);
        }

        // --- 8. 更新调度任务状态为“进行中” ---
        task.setStatus("处理中");
        dispatchTasksMapper.updateDispatchTask(task); // 假设您有updateDispatchTask方法

        return dispatchedBikeIds; // 返回实际被调度的自行车ID列表
    }

    /**
     * 完成一个调度任务，更新关联自行车的最终位置和状态。
     */
    @Override
    @Transactional
    public void completeDispatch(Long taskId, LocalDateTime completionTime) {
        // --- 1. 获取调度任务 ---
        DispatchTasks task = dispatchTasksMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("调度任务ID: " + taskId + " 不存在。");
        }
        if (!"处理中".equals(task.getStatus())) {
            throw new IllegalArgumentException("调度任务ID: " + taskId + " 状态不正确，无法完成调度 (当前状态: " + task.getStatus() + ")。");
        }

        // --- 2. 获取目标停车点精确经纬度 ---
        EliteSites targetEliteSite = eliteSitesMapper.findByGeohashInfo(task.getEndGeohash());
        if (targetEliteSite == null) {
            throw new IllegalArgumentException("任务目标区域 (endGeohash: " + task.getEndGeohash() + ") 对应的停车点不存在。无法完成调度。");
        }
        BigDecimal targetLat = targetEliteSite.getCenterLat();
        BigDecimal targetLon = targetEliteSite.getCenterLon();

        // --- 3. 获取所有与此任务关联的自行车ID ---
        List<String> bikeIdsInTask = bikesInTasksMapper.findBikeIdsByTaskId(taskId);
        if (bikeIdsInTask.isEmpty()) {
            // 这通常不应该发生，除非 startDispatch 未正确执行
            throw new IllegalStateException("调度任务ID: " + taskId + " 未关联任何自行车，无法完成调度。");
        }

        // --- 4. 逐一更新自行车的最终位置和状态 ---
        for (String bikeId : bikeIdsInTask) {
            // 更新自行车的经纬度为目标停车点的精确经纬度，状态为“待使用”
            bikesMapper.updateBikeStatusAndLocation(
                    bikeId,
                    "待使用", // 完成调度后，自行车重新变为“待使用”状态
                    targetLat,
                    targetLon,
                    task.getEndGeohash() // 更新 geohash 到目标区域
            );
        }

        // --- 5. 更新调度任务状态为“已完成” ---
        task.setStatus("处理完成");
        task.setCompletedAt(LocalDateTime.now()); // 记录任务完成时间
        dispatchTasksMapper.updateDispatchTask(task); // 假设您有updateDispatchTask方法


        // --- 6. 触发受影响区域的实时报告更新 ---
        if (bikesService instanceof BikesServiceImpl) {
            ((BikesServiceImpl) bikesService).recalculateAndSaveHourlyReport(task.getStartGeohash(), completionTime); // 使用传入的 completionTime
            ((BikesServiceImpl) bikesService).recalculateAndSaveHourlyReport(task.getEndGeohash(), completionTime); // 使用传入的 completionTime
            System.out.println("DEBUG: Triggered real-time report update for source: " + task.getStartGeohash() + " and target: " + task.getEndGeohash() + " at simulated time: " + completionTime);
        } else {
            System.err.println("ERROR: bikesService 不是 BikesServiceImpl 类型，无法触发实时报告更新。");
        }
    }


    /**
     * 获取特定调度任务关联的所有自行车ID。
     */
    @Override
    public List<String> getBikesForDispatchTask(Long taskId) {
        // 1. 校验任务是否存在 (可选，但推荐，确保获取的ID是有效的任务)
        // 您可能需要 DispatchTasksMapper 中有一个 findById 方法
        // DispatchTasks task = dispatchTasksMapper.findById(taskId);
        // if (task == null) {
        //    throw new IllegalArgumentException("调度任务ID: " + taskId + " 不存在。");
        // }
        // 2. 调用 BikesInTasksMapper 获取关联的自行车ID列表
        return bikesInTasksMapper.findBikeIdsByTaskId(taskId);
    }
}