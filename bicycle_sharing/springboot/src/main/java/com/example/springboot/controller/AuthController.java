// src/main/java/com/example/springboot/controller/AuthController.java
package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.exception.CustomException;
import com.example.springboot.entity.User; // 导入 User 实体
import com.example.springboot.entity.Staff; // 导入 Staff 实体 (假设存在，由其他同事负责)

import com.example.springboot.service.Interface.IUserService;
import com.example.springboot.service.Interface.IStaffService; // 假设存在 Staff Service 接口

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController类
 * 负责接收和处理认证相关的HTTP请求（如登录、注册）
 */
@RestController
@RequestMapping // 根路径，或者可以定义为 "/auth"
public class AuthController {

    @Resource
    private IUserService userService; // 注入 User Service

    @Resource
    private IStaffService staffService; // 注入 Staff Service，由负责 Admin/Worker 的同事实现

    /**
     * 统一的登录接口，根据角色分发到不同的Service处理
     * @param loginRequest 包含用户名、密码和角色
     * @return 登录结果（包含用户或工作人员信息）
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest) {
        try {
            String role = loginRequest.getRole();
            Object loggedInEntity = null; // 用于存储登录成功后的 User 或 Staff 对象

            if ("user".equalsIgnoreCase(role)) {
                // 调用 User Service 处理用户登录
                loggedInEntity = userService.login(loginRequest);
            } else if ("admin".equalsIgnoreCase(role) || "worker".equalsIgnoreCase(role)) {
                // 调用 Staff Service 处理管理员/工作人员登录
                // 注意：StaffService.login 方法的实现由负责 Admin/Worker 的同事完成
                loggedInEntity = staffService.login(loginRequest);
            } else {
                throw new CustomException("无效的角色类型: " + role, "400");
            }

            return Result.success(loggedInEntity);

        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            // 记录详细异常日志
            e.printStackTrace();
            return Result.error("500", "登录失败: " + e.getMessage());
        }
    }

    /**
     * 统一的注册接口，根据角色分发到不同的Service处理
     * @param registerRequest 包含用户名、手机号、密码和角色
     * @return 注册结果（包含新注册的用户或工作人员信息）
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest registerRequest) {
        try {
            String role = registerRequest.getRole();
            Object registeredEntity = null;

            if ("user".equalsIgnoreCase(role)) {
                // 调用 User Service 处理普通用户注册
                registeredEntity = userService.register(registerRequest);
            } else if ("admin".equalsIgnoreCase(role) || "worker".equalsIgnoreCase(role)) {
                // 调用 Staff Service 处理工作人员注册 (通常管理员/工作人员注册由后台管理，不直接对外开放)
                // 注意：StaffService.register 方法的实现由负责 Admin/Worker 的同事完成
                // 如果不允许前端直接注册，可以抛出异常
                registeredEntity = staffService.register(registerRequest);
                // registeredEntity = staffService.register(registerRequest); // 如果允许，则调用
            } else {
                throw new CustomException("无效的角色类型: " + role, "400");
            }

            return Result.success(registeredEntity);

        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "注册失败: " + e.getMessage());
        }
    }
}
