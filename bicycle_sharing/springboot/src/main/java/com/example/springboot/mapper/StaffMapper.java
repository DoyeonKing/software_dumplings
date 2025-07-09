package com.example.springboot.mapper;

import com.example.springboot.entity.Staff; // 导入实体类
import org.apache.ibatis.annotations.Mapper;

import java.util.List; // 导入必要的类 (如果需要)

/**
 * StaffMapper接口空壳
 * 用于定义对数据库 'staff' 表的操作方法框架
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface StaffMapper {
    // 空壳：不在此处定义任何方法签名
}