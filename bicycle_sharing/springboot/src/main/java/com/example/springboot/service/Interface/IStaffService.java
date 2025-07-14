package com.example.springboot.service.Interface;

import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.entity.Staff;
import com.example.springboot.exception.CustomException;

import java.util.List;

public interface IStaffService {
    /**
     * 处理工作人员登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的响应对象（包含工作人员信息和Token）
     * @throws CustomException 如果登录失败（如用户名密码错误）
     */
    Object login(LoginRequest loginRequest);

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

    /**
     * 获取所有工作人员信息（不包括管理员）
     * @return 所有工作人员对象的列表
     */
    List<Staff> getAllWorkers();

    /**
     * 更新工作人员的用户名
     * @param staffId 工作人员ID (从Token解析而来)
     * @param newUsername 新用户名
     * @return 更新影响的行数
     */
    int updateUsername(Integer staffId, String newUsername);

    /**
     * 更新工作人员的密码
     * @param staffId 工作人员ID (从Token解析而来)
     * @param oldPassword 明文旧密码
     * @param newPassword 明文新密码 (Service层负责哈希)
     * @return 更新影响的行数
     */
    int updatePassword(Integer staffId, String oldPassword, String newPassword);

    /**
     * 根据用户ID查找员工信息
     * @param staffId 用户ID
     * @return 员工对象或 null
     */
    Staff findByStaffId(Integer staffId);
}