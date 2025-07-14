package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.UpdatePasswordRequest;
import com.example.springboot.entity.Manager;
import com.example.springboot.service.Interface.IManagerService;
import com.example.springboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * @class ManagerController
 * @description 管理员相关接口的控制器
 *
 * 这个控制器处理所有与管理员账户自身相关的操作，例如获取个人信息、修改密码等。
 * 所有接口都需要在请求头中携带有效的JWT Token进行身份验证。
 */
@RestController
@RequestMapping("/managers") // 定义所有接口的基础路径为 /managers
public class ManagerController {

    // 自动注入ManagerService，用于处理业务逻辑
    @Resource
    private IManagerService managerService;

    // 自动注入JwtTokenUtil，用于处理JWT Token
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 从HTTP请求头中提取并解析Token，获取管理员ID。
     * 这是一个私有辅助方法，用于简化代码。
     *
     * @param request HttpServletRequest对象，包含请求信息
     * @return 解析出的管理员ID (Integer类型)
     */
    private Integer getManagerIdFromRequest(HttpServletRequest request) {
        // 从请求头 "Authorization" 中获取Token字符串
        String token = request.getHeader("Authorization");
        // 使用工具类从Token中解析出用户ID
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        // 将字符串类型的ID转换为Integer
        return Integer.parseInt(userId);
    }

    /**
     * 获取当前登录管理员的个人信息。
     *
     * @param request HttpServletRequest对象，用于获取Token
     * @return 返回包含管理员信息的结果对象。为安全起见，密码哈希值已被清除。
     */
    @GetMapping("/profile")
    public Result getProfile(HttpServletRequest request) {
        // 从Token中获取当前管理员的ID
        Integer managerId = getManagerIdFromRequest(request);
        // 调用Service层的方法，根据ID从数据库中查找管理员信息
        Manager manager = managerService.getById(managerId);
        // 如果找到了管理员，为了安全，在返回给前端前将密码哈希值设置为空
        if (manager != null) {
            manager.setPasswordHash(null);
        }
        // 返回成功结果，并附上管理员信息
        return Result.success(manager);
    }

    /**
     * 更新当前登录管理员的密码。
     *
     * @param passwordRequest 包含旧密码、新密码和确认密码的请求体
     * @param request HttpServletRequest对象，用于获取Token
     * @return 返回操作成功或失败的结果对象。
     */
    @PutMapping("/password")
    public Result updatePassword(@RequestBody UpdatePasswordRequest passwordRequest, HttpServletRequest request) {
        // 从Token中获取当前管理员的ID
        Integer managerId = getManagerIdFromRequest(request);
        // 调用Service层的方法，执行密码更新逻辑
        managerService.updatePassword(managerId, passwordRequest);
        // 返回成功结果
        return Result.success("密码更新成功");
    }
}
