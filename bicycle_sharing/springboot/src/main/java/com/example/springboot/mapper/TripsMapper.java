package com.example.springboot.mapper;

import com.example.springboot.entity.Trips; // 导入实体类
import org.apache.ibatis.annotations.Mapper;

import java.util.List; // 导入必要的类 (如果需要)

/**
 * TripsMapper接口空壳
 * 用于定义对数据库 'trips' 表的操作方法框架
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface TripsMapper { // 接口名与实体类名保持一致，命名为 TripsMapper
    // 空壳：不在此处定义任何方法签名
}