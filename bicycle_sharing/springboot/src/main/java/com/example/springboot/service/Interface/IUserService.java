package com.example.springboot.service.Interface;

import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.common.response.LoginResponse;
import com.example.springboot.entity.User;
import com.example.springboot.exception.CustomException;
import com.github.pagehelper.PageInfo; // 如果你计划添加分页功能，可以保留

import java.util.List;

public interface IUserService {

    /**
     * 处理普通用户登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的用户响应对象（包含用户信息和Token）
     * @throws CustomException 如果登录失败（如用户名密码错误）
     */
    LoginResponse login(LoginRequest loginRequest); // 返回类型改为 LoginResponse

    /**
     * 处理普通用户注册逻辑
     * @param registerRequest 注册请求 DTO
     * @return 注册成功的用户对象（已脱敏）
     * @throws CustomException 如果注册失败（如用户名/手机号已存在）
     */
    User register(RegisterRequest registerRequest);

    /**
     * 获取用户个人信息
     * @param userId 用户ID
     * @return 用户对象（已脱敏）
     * @throws CustomException 如果用户不存在
     */
    User getUserProfile(String userId);

    /**
     * 更新用户个人信息
     * @param user 包含待更新字段的用户对象
     * @throws CustomException 如果更新失败（如用户名/手机号已存在）
     */
    void updateUserProfile(User user);

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @throws CustomException 如果旧密码不正确或修改失败
     */
    void changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 根据ID获取用户信息 (ID类型为String)。
     * 这是拦截器需要调用的方法。
     */
    User getById(String id);
}

