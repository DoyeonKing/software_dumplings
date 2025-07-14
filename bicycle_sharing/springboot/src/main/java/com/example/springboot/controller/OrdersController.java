package com.example.springboot.controller;

import com.example.springboot.service.Interface.IOrdersService; // 导入Service接口
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解


/**
 * TripsController类空壳
 * 负责接收和处理与用户行程记录相关的HTTP请求框架
 */
@RestController // 标记这是一个RESTful控制器
@RequestMapping("/trips") // 基础URL路径通常与资源名复数形式保持一致
public class OrdersController { // 控制器类名命名为 TripsController

    @Resource // 注入ITripsService接口的实现类
    private IOrdersService tripsService; // 注入的Service类型命名为 ITripsService

    // 空壳：不在此处定义任何方法

}