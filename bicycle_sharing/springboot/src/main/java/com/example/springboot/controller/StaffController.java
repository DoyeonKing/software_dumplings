package com.example.springboot.controller;

import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.dto.ProfileResponse;
import com.example.springboot.entity.Staff; // 导入实体类
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.service.Interface.IStaffService; // 导入Service接口
import com.github.pagehelper.PageInfo; // 导入分页PageInfo (如果需要)
import jakarta.annotation.Resource; // 导入Resource注解
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.util.List; // 导入List (如果需要)
import java.util.Map; // 导入Map (如果需要)

/**
 * StaffController类空壳
 * 负责接收和处理与工作人员/管理人员相关的HTTP请求框架
 */
@RestController // 标记这是一个RESTful控制器
@RequestMapping("/staff") // 定义这个控制器的基础URL路径
public class StaffController {

    @Resource
    private IStaffService staffService;

    /**
     * 查询当前登录用户的个人信息。
     * 需要用户已登录，用户名存储在 Session 中。
     * URL: GET /staff/profile
     * @param request HttpServletRequest，用于获取 Session 中的用户名
     * @return ResponseEntity 包含用户的个人资料或错误信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        // 1. 从 Session 中获取当前登录的用户名
        String username = (String) request.getSession().getAttribute("username");

        // 2. 检查用户是否已登录 (Session 中是否有用户名)
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录或会话已过期。");
        }

        // 3. 根据用户名从数据库查询员工详细信息
        Staff staff = staffService.findByUsername(username);

        // 4. 检查用户是否存在（理论上已登录的用户应该存在）
        if (staff == null) {
            // 这通常不应该发生，除非数据库数据与 Session 不一致
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户信息不存在。");
        }

        // 5. 构建响应 DTO，只包含需要返回的个人信息（不包括密码哈希）
        ProfileResponse profile = new ProfileResponse(
                staff.getStaffId(),
                staff.getUsername(),
                staff.getStaffType()
        );

        // 6. 返回成功响应
        return ResponseEntity.ok(profile);
    }

    // --- 新增的、根据传输用户名获取信息的接口 ---
    /**
     * 根据指定的用户名查询员工的个人信息。
     * 这个接口是临时的，因为它直接通过URL暴露了查询参数，且没有额外的权限检查。
     * 在生产环境中，这样的接口通常需要更严格的认证和授权。
     * URL: GET /staff/profile/by-username/{username}
     * @param username 要查询的员工用户名，从URL路径中获取
     * @return ResponseEntity 包含用户的个人资料或错误信息
     */
//    @GetMapping("/profile/by-username/{username}")
//    // 注意：如果你的 Spring Security 配置了 `anyRequest().authenticated()`，
//    // 那么这个接口默认也会被保护。如果希望它在没有登录的情况下也能访问 (临时测试目的)，
//    // 你需要在 SecurityConfig 中为 `/staff/profile/by-username/**` 配置 `permitAll()`。
//    public ResponseEntity<?> getProfileByUsername(@PathVariable String username) {
//        // 1. 检查传入的用户名是否为空
//        if (username == null || username.trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("用户名不能为空。");
//        }
//
//        // 2. 根据用户名从数据库查询员工详细信息
//        Staff staff = staffService.findByUsername(username);
//
//        // 3. 检查用户是否存在
//        if (staff == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户名 '" + username + "' 的用户信息不存在。");
//        }
//
//        // 4. 构建响应 DTO，只包含需要返回的个人信息（不包括密码哈希）
//        ProfileResponse profile = new ProfileResponse(
//                staff.getStaffId(),
//                staff.getUsername(),
//                staff.getStaffType()
//        );
//
//        // 5. 返回成功响应
//        return ResponseEntity.ok(profile);
//    }
}