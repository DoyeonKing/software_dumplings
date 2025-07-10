package com.example.springboot.controller;

import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.common.request.ChangePasswordRequest;
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

/**
 * 获取用户个人信息
 * Endpoint: GET /user/profile
 * @param token 认证令牌，从请求头中获取用户ID
 * @return 统一响应结果，包含用户个人信息（已脱敏）
 */
@GetMapping("/profile")
public Result getUserProfile(@RequestHeader("Authorization") String token) { // 假设通过Header传递Token
    // TODO: 从Token中解析出用户ID
    String userId = "parsedUserIdFromToken"; // 占位符，需要实际解析逻辑
    try {
        User userProfile = userService.getUserProfile(userId);
        return Result.success(userProfile);
    } catch (CustomException e) {
        return Result.error(e.getCode(), e.getMsg());
    } catch (Exception e) {
        e.printStackTrace();
        return Result.error("500", "获取用户资料失败: " + e.getMessage());
    }
}

/**
 * 修改用户个人信息
 * Endpoint: PUT /user/profile
 * @param token 认证令牌，从请求头中获取用户ID
 * @param user 包含待更新字段的用户对象
 * @return 统一响应结果
 */
@PutMapping("/profile")
public Result updateUserProfile(@RequestHeader("Authorization") String token, @RequestBody User user) {
    // TODO: 从Token中解析出用户ID，并设置到 user 对象中，确保更新的是当前用户
    String userId = "parsedUserIdFromToken"; // 占位符
    user.setUserid(userId); // 确保 user 对象的 userid 被设置
    try {
        userService.updateUserProfile(user);
        return Result.success("个人信息更新成功");
    } catch (CustomException e) {
        return Result.error(e.getCode(), e.getMsg());
    } catch (Exception e) {
        e.printStackTrace();
        return Result.error("500", "更新个人信息失败: " + e.getMessage());
    }
}

/**
 * 修改用户密码
 * Endpoint: POST /user/change-password
 * @param token 认证令牌，从请求头中获取用户ID
 * @param request 包含旧密码和新密码的请求 DTO
 * @return 统一响应结果
 */
@PostMapping("/change-password")
public Result changePassword(@RequestHeader("Authorization") String token, @RequestBody ChangePasswordRequest request) {
    // TODO: 从Token中解析出用户ID
    String userId = "parsedUserIdFromToken"; // 占位符
    try {
        // 校验新密码和确认新密码是否一致 (前端已做，后端可再次校验)
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new CustomException("两次输入的新密码不一致", "400");
        }
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功");
    } catch (CustomException e) {
        return Result.error(e.getCode(), e.getMsg());
    } catch (Exception e) {
        e.printStackTrace();
        return Result.error("500", "修改密码失败: " + e.getMessage());
    }
}
}
