package com.example.springboot.mapper;

import com.example.springboot.entity.DispatchTasks; // 导入纠正后的实体类名
import org.apache.ibatis.annotations.Mapper;

import java.util.List; // 导入必要的类

/**
 * DispatchTasksMapper接口空壳
 * 用于定义对数据库 'dispatch_tasks' 表的操作方法框架
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface DispatchTasksMapper { // 接口名与实体类名保持一致，改为DispatchTasksMapper
    // 空壳：不在此处定义任何方法签名
}