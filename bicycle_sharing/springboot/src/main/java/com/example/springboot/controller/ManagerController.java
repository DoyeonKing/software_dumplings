package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.UpdatePasswordRequest;
import com.example.springboot.entity.Manager;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IManagerService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest; // 导入 HttpServletRequest
import org.springframework.web.bind.annotation.*;

/**
 * @class ManagerController
 * @description 负责接收和处理与管理员相关的HTTP请求。
 */
@RestController
@RequestMapping("/managers")
public class ManagerController {

    @Resource
    private IManagerService managerService;

    /**
     * 获取当前登录管理员的个人信息。
     */
    @GetMapping("/profile")
    public Result getProfile(HttpServletRequest request) { // 参数改为 HttpServletRequest
        try {
            // 从请求中获取由拦截器设置好的、已验证的管理员ID
            Integer managerId = (Integer) request.getAttribute("userId");
            Manager manager = managerService.getById(managerId);
            if (manager != null) {
                manager.setPasswordHash(null);
            }
            return Result.success(manager);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取管理员资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前登录管理员的个人信息。
     */
    @PutMapping("/profile")
    public Result updateProfile(HttpServletRequest request, @RequestBody Manager manager) {
        try {
            Integer managerId = (Integer) request.getAttribute("userId");
            manager.setManagerId(managerId); // 确保更新的是当前用户
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
     */
    @PutMapping("/password")
    public Result updatePassword(HttpServletRequest request, @RequestBody UpdatePasswordRequest passwordRequest) {
        try {
            Integer managerId = (Integer) request.getAttribute("userId");
            managerService.updatePassword(managerId, passwordRequest);
            return Result.success("密码更新成功");
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "修改密码失败: " + e.getMessage());
        }
    }
}
