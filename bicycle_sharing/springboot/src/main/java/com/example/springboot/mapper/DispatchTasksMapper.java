package com.example.springboot.mapper;

import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

}