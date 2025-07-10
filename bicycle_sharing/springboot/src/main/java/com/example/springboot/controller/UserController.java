package com.example.springboot.controller;

import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.entity.User; // 导入实体类
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.service.Interface.IUserService; // 导入Service接口
import com.github.pagehelper.PageInfo; // 导入分页PageInfo (如果需要)
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.util.List; // 导入List (如果需要)
import java.util.Map; // 导入Map (如果需要)

/**
 * UserController
 * 负责接收和处理与用户相关的HTTP请求
 */
@RestController // 标记这是一个RESTful控制器，返回JSON或XML数据
@RequestMapping("/user") // 定义这个控制器的基础URL路径，所有方法都会以此为前缀
public class UserController {

    @Resource // 注入IUserService接口的实现类 (Spring会自动找到UserServiceImpl)
    private IUserService userService;


}
