package com.example.springboot.service.Interface;

import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.entity.Staff;
import com.example.springboot.exception.CustomException;

import java.util.Map;

public interface IStaffService {

    /**
     * 处理 Staff 用户登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的工作人员响应 Map (包含 Staff 信息和Token)
     * @throws CustomException 如果登录失败
     */
    Map<String, Object> login(LoginRequest loginRequest);

    /**
     * 处理工作人员注册逻辑
     * @param registerRequest 注册请求 DTO
     * @return 注册成功的工作人员对象（已脱敏）
     * @throws CustomException 如果注册失败（如用户名已存在）
     */
    Staff register(RegisterRequest registerRequest);

    /**
     * 根据用户名查找员工信息
     * @param username 用户名
     * @return 员工对象或 null
     */
    Staff findByUsername(String username);
}