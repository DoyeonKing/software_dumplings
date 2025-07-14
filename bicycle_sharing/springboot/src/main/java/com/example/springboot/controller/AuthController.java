package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IManagerService; // 引入IManagerService
import com.example.springboot.service.Interface.IStaffService;
import com.example.springboot.service.Interface.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class AuthController
 * @description 统一处理所有角色的认证（登录/注册）请求
 */
@RestController
@RequestMapping // 基础路径为空，直接使用 /login, /register
public class AuthController {

    @Resource
    private IUserService userService;

    @Resource
    private IStaffService staffService;

    @Resource
    private IManagerService managerService; // 注入ManagerService

    /**
     * 统一登录接口。
     * 根据请求体中的 'role' 字段，分发给不同的Service处理。
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest) {
        try {
            String role = loginRequest.getRole();
            Object loggedInEntity;

            switch (role.toLowerCase()) {
                case "user":
                    loggedInEntity = userService.login(loginRequest);
                    break;
                case "admin": // 如果角色是 'admin'
                    loggedInEntity = managerService.login(loginRequest);
                    break;
                case "worker": // 如果角色是 'worker'
                    loggedInEntity = staffService.login(loginRequest);
                    break;
                default:
                    throw new CustomException("无效的角色类型: " + role, "400");
            }
            return Result.success(loggedInEntity);
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            // 捕获其他未知异常
            e.printStackTrace();
            return Result.error("500", "登录失败: " + e.getMessage());
        }
    }

    /**
     * 统一注册接口。
     * 根据请求体中的 'role' 字段，分发给不同的Service处理。
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest registerRequest) {
        try {
            String role = registerRequest.getRole();
            Object registeredEntity;

            switch (role.toLowerCase()) {
                case "user":
                    registeredEntity = userService.register(registerRequest);
                    break;
                case "admin": // 如果角色是 'admin'
                    registeredEntity = managerService.register(registerRequest);
                    break;
                case "worker": // 如果角色是 'worker'
                    registeredEntity = staffService.register(registerRequest);
                    break;
                default:
                    throw new CustomException("无效的角色类型: " + role, "400");
            }
            return Result.success(registeredEntity);
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            // 捕获其他未知异常
            e.printStackTrace();
            return Result.error("500", "注册失败: " + e.getMessage());
        }
    }
}
