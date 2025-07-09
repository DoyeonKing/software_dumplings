package com.example.springboot.controller;

import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.entity.Staff; // 导入实体类
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.service.Interface.IStaffService; // 导入Service接口
import com.github.pagehelper.PageInfo; // 导入分页PageInfo (如果需要)
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.util.List; // 导入List (如果需要)
import java.util.Map; // 导入Map (如果需要)

/**
 * StaffController类空壳
 * 负责接收和处理与工作人员/管理人员相关的HTTP请求框架
 */
@RestController // 标记这是一个RESTful控制器
@RequestMapping("/staff") // 定义这个控制器的基础URL路径
public class StaffController {

    @Resource // 注入IStaffService接口的实现类
    private IStaffService staffService;

    // 空壳：不在此处定义任何方法

}