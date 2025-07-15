package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.UpdatePasswordRequest;
import com.example.springboot.entity.Manager;
import com.example.springboot.entity.Staff;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IManagerService;
import com.example.springboot.service.Interface.IStaffService;
import com.example.springboot.util.JwtTokenUtil; // 【新增】导入 JwtTokenUtil
import jakarta.annotation.Resource;
// 移除 jakarta.servlet.http.HttpServletRequest，因为它将不再直接用于获取ID

import org.springframework.web.bind.annotation.*; // 导入所有必要的Spring Web注解

import java.util.List;

/**
 * @class ManagerController
 * @description 负责接收和处理与管理员相关的HTTP请求。
 */
@RestController
@RequestMapping("/managers") // 基础URL路径
public class ManagerController {

    @Resource
    private IManagerService managerService;

    @Resource // 【新增】注入 JwtTokenUtil
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private IStaffService staffService;

    /**
     * 获取当前登录管理员的个人信息。
     * 【修改】参数改为 @RequestHeader("Authorization") String token
     */
    @GetMapping("/profile")
    public Result getProfile(@RequestHeader("Authorization") String token) { // 【修改】获取Authorization头
        try {
            // 从 Token 中解析出管理员ID
            Integer managerId = getManagerIdFromToken(token); // 【修改】调用辅助方法解析 Token
            Manager manager = managerService.getById(managerId);
            if (manager != null) {
                manager.setPasswordHash(null); // 脱敏密码
            }
            return Result.success(manager);
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取管理员资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前登录管理员的个人信息。
     * 【修改】参数改为 @RequestHeader("Authorization") String token
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestHeader("Authorization") String token, @RequestBody Manager manager) { // 【修改】获取Authorization头
        try {
            Integer managerId = getManagerIdFromToken(token); // 【修改】调用辅助方法解析 Token
            manager.setManagerId(managerId); // 确保更新的是当前管理员
            managerService.updateProfile(manager);
            return Result.success("个人信息更新成功");
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "更新个人信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前登录管理员的密码。
     * 【修改】参数改为 @RequestHeader("Authorization") String token
     */
    @PutMapping("/password")
    public Result updatePassword(@RequestHeader("Authorization") String token, @RequestBody UpdatePasswordRequest passwordRequest) { // 【修改】获取Authorization头
        try {
            Integer managerId = getManagerIdFromToken(token); // 【修改】调用辅助方法解析 Token
            managerService.updatePassword(managerId, passwordRequest);
            return Result.success("密码更新成功");
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 【新增辅助方法】：从 Authorization 头中提取 Token 并解析出管理员ID。
     * 模仿 UserController 中的 getUserIdFromToken 方法。
     * @param authorizationHeader Authorization 请求头的值 (例如 "Bearer <token>")
     * @return 解析出的管理员ID (Integer 类型)
     * @throws CustomException 如果 Token 无效或解析失败
     */
    private Integer getManagerIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException("认证失败：缺少Bearer Token", "401");
        }
        String token = authorizationHeader.substring(7); // 提取 "Bearer " 后的 Token 字符串
        try {
            // JwtTokenUtil.getUserIdFromToken 方法通常返回 String 类型的 ID，需要转换为 Integer
            String managerIdStr = jwtTokenUtil.getUserIdFromToken(token); // 假设这个方法能获取到Manager ID
            if (!jwtTokenUtil.validateToken(token, managerIdStr)) { // 验证 Token 的有效性
                throw new CustomException("认证失败：Token无效或已过期", "401");
            }
            return Integer.parseInt(managerIdStr); // 将 String 类型的 ID 转换为 Integer
        } catch (NumberFormatException e) {
            throw new CustomException("认证失败：Token中的管理员ID格式不正确: " + e.getMessage(), "401");
        } catch (Exception e) {
            throw new CustomException("认证失败：Token解析异常或无效: " + e.getMessage(), "401");
        }
    }


     /**
     * 获取当前管理员管理的所有员工列表。
     * @param token Authorization 请求头，用于解析管理员ID
     * @return Result，data 为管理员管理的所有工作人员信息列表
     */
    @GetMapping("/managed-staff")
    public Result getManagedStaff(@RequestHeader("Authorization") String token) {
        try {
            // 从 Token 中解析出管理员ID
            Integer managerId = getManagerIdFromToken(token);
            // 调用 StaffService 获取该管理员管理的所有工作人员
            List<Staff> staffList = staffService.getStaffByManagerId(managerId);

            // 脱敏密码信息，确保不会泄露密码哈希
            if (staffList != null) {
                staffList.forEach(staff -> staff.setPasswordHash(null));
            }
            return Result.success(staffList);
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取管理员工列表失败: " + e.getMessage());
        }
    }
}