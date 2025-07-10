package com.example.springboot.service.Interface;

import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.entity.Staff;
import com.example.springboot.exception.CustomException;

public interface IStaffService {
    /**
     * 处理工作人员登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的工作人员对象（已脱敏）
     * @throws CustomException 如果登录失败（如用户名密码错误）
     */
    Staff login(LoginRequest loginRequest);

    /**
     * 处理工作人员注册逻辑
     * @param registerRequest 注册请求 DTO
     * @return 注册成功的工作人员对象（已脱敏）
     * @throws CustomException 如果注册失败（如用户名已存在）
     */
    Staff register(RegisterRequest registerRequest);
}