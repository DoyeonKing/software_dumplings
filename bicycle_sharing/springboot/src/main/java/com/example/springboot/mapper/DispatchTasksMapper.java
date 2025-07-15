package com.example.springboot.mapper;

import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List; // 导入必要的类

/**
 * DispatchTasksMapper接口空壳
 * 用于定义对数据库 'dispatch_tasks' 表的操作方法框架
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface DispatchTasksMapper { // 接口名与实体类名保持一致，改为DispatchTasksMapper
    /**
     * 查询 dispatch_tasks 表中的所有任务信息
     * @return 包含所有 DispatchTasks 对象的列表
     */
    @Select("SELECT " +
            "task_id, start_geohash, end_geohash, bike_count, " +
            "assigned_to, status, created_at, completed_at " +
            "FROM dispatch_tasks")
    List<DispatchTasks> selectAllTasks();

    /**
     * 查询 dispatch_tasks 表中所有状态为 '未处理' 的任务信息
     * @return 包含所有未处理 DispatchTasks 对象的列表
     */
    @Select("SELECT " +
            "task_id, start_geohash, end_geohash, bike_count, " +
            "assigned_to, status, created_at, completed_at " +
            "FROM dispatch_tasks " +
            "WHERE status = '未处理'")
    List<DispatchTasks> selectUnprocessedTasks(); // 新增方法

    /**
     * 查询 dispatch_tasks 表中所有状态为 '处理中' 的任务信息
     * @return 包含所有处理中 DispatchTasks 对象的列表
     */
    @Select("SELECT " +
            "task_id, start_geohash, end_geohash, bike_count, " +
            "assigned_to, status, created_at, completed_at " +
            "FROM dispatch_tasks " +
            "WHERE status = '处理中'")
    List<DispatchTasks> selectProcessingTasks(); // 新增方法

    /**
     * 查询 dispatch_tasks 表中所有状态为 '处理完成' 的任务信息
     * @return 包含所有处理完成 DispatchTasks 对象的列表
     */
    @Select("SELECT " +
            "task_id, start_geohash, end_geohash, bike_count, " +
            "assigned_to, status, created_at, completed_at " +
            "FROM dispatch_tasks " +
            "WHERE status = '处理完成'")
    List<DispatchTasks> selectCompletedTasks(); // 新增方法

    /**
     * 根据工作人员ID查询其所有分配到的任务
     * @param assignedTo 工作人员ID
     * @return 包含该工作人员所有分配任务的列表
     */
    @Select("SELECT " +
            "task_id, start_geohash, end_geohash, bike_count, " +
            "assigned_to, status, created_at, completed_at " +
            "FROM dispatch_tasks " +
            "WHERE assigned_to = #{assignedTo}")
    List<DispatchTasks> selectTasksByAssignedTo(@Param("assignedTo") Integer assignedTo);

    /**
     * 插入新的调度任务。
     * SQL语句和keyProperty需要匹配DispatchTasks实体的新字段名。
     */
    @Insert("INSERT INTO dispatch_tasks (start_geohash, end_geohash, bike_count, assigned_to, status, created_at) " +
            "VALUES (#{startGeohash}, #{endGeohash}, #{bikeCount}, #{assignedTo}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "taskId") // 主键名从id改为taskId
    int insertDispatchTask(DispatchTasks task);

    /**
     * 根据日期范围和工作人员ID获取任务信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param assignedTo 工作人员ID
     * @return 匹配的任务列表
     */
    @Select("SELECT " +
            "task_id, start_geohash, end_geohash, bike_count, " +
            "assigned_to, status, created_at, completed_at " +
            "FROM dispatch_tasks " +
            "WHERE assigned_to = #{assignedTo} AND created_at BETWEEN #{startDate} AND #{endDate}")
    List<DispatchTasks> selectTasksByDateRangeAndAssignedTo(@Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate,
                                                            @Param("assignedTo") Integer assignedTo);


    /**
     * 【新增方法】
     * 根据任务ID查询单个调度任务。
     * @param taskId 调度任务的ID。
     * @return 对应的 DispatchTasks 实体，如果不存在则返回 null。
     */
    DispatchTasks findById(@Param("taskId") Long taskId); // 方法签名，SQL 在 XML 中定义

    /**
     * 【新增方法】
     * 更新调度任务的信息。
     * 主要用于更新任务的状态 (status) 和完成时间 (completed_at)。
     * @param task 包含要更新信息的 DispatchTasks 实体。
     * @return 数据库受影响的行数。
     */
    int updateDispatchTask(DispatchTasks task); // 方法签名，SQL 在 XML 中定义



}