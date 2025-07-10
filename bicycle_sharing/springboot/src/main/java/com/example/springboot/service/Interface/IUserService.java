package com.example.springboot.service.Interface;

import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.entity.User;
import com.example.springboot.exception.CustomException;
import com.github.pagehelper.PageInfo; // 如果你计划添加分页功能，可以保留

import java.util.List;

public interface IUserService {

    /**
     * 处理普通用户登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的用户对象（已脱敏）
     * @throws CustomException 如果登录失败（如用户名密码错误）
     */
    User login(LoginRequest loginRequest);

    /**
     * 处理普通用户注册逻辑
     * @param registerRequest 注册请求 DTO
     * @return 注册成功的用户对象（已脱敏）
     * @throws CustomException 如果注册失败（如用户名/手机号已存在）
     */
    User register(RegisterRequest registerRequest);
}
