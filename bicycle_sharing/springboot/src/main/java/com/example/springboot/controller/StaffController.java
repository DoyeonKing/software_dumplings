package com.example.springboot.controller;

import cn.hutool.crypto.SecureUtil; // 虽然这里没有直接使用，但依赖仍然可能存在
import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.common.request.ChangePasswordRequest;
import com.example.springboot.common.request.PasswordUpdateRequest;
import com.example.springboot.common.response.LoginResponse; // 假设LoginResponse的构造函数已调整
import com.example.springboot.dto.ProfileResponse;
import com.example.springboot.dto.UpdatePasswordRequest;
import com.example.springboot.dto.UpdateUsernameRequest;
import com.example.springboot.dto.WorkerInfoResponse;
import com.example.springboot.entity.Staff; // 导入实体类
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.service.Interface.IStaffService; // 导入Service接口
import com.example.springboot.util.JwtTokenUtil;
import com.github.pagehelper.PageInfo; // 导入分页PageInfo (如果需要)
import jakarta.annotation.Resource; // 导入Resource注解
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.util.List; // 导入List (如果需要)
import java.util.Map; // 导入Map (如果需要)
import java.util.stream.Collectors;

/**
 * StaffController类空壳
 * 负责接收和处理与工作人员/管理人员相关的HTTP请求框架 (移除角色概念)
 */
@RestController // 标记这是一个RESTful控制器
@RequestMapping("/staff") // 定义这个控制器的基础URL路径
public class StaffController {

    @Resource
    private IStaffService staffService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/batch-reset-passwords")
    public ResponseEntity<?> batchResetPasswords(
            @RequestParam(required = false, defaultValue = "51") int startId,
            @RequestParam(required = false, defaultValue = "100") int endId
    ) {
        int updatedCount = staffService.batchResetPasswords(startId, endId);
        return ResponseEntity.ok(Map.of("message", "成功重置" + updatedCount + "个用户的密码"));
    }

    /**
     * 查询当前登录用户的个人信息。
     * 通过 JWT Token 获取用户信息。
     * URL: GET /staff/profile
     * @param authorizationHeader Authorization 请求头，包含 JWT Token
     * @return ResponseEntity 包含用户的个人资料或错误信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // 1. 从 Token 中提取用户ID
            String staffIdStr = getUserIdFromToken(authorizationHeader);
            Integer staffId = Integer.parseInt(staffIdStr);

            // 2. 根据用户ID从数据库查询员工详细信息
            Staff staff = staffService.findByStaffId(staffId);

            // 3. 检查用户是否存在
            if (staff == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户信息不存在。");
            }

            // 4. 构建响应 DTO，只包含需要返回的个人信息（不包括密码哈希和 staffType）
            ProfileResponse profile = new ProfileResponse(
                    staff.getStaffId(),
                    staff.getUsername(),
                    staff.getManagerId(),
                    staff.getGeohash()
            );

            // 5. 返回成功响应
            return ResponseEntity.ok(profile);
        } catch (CustomException e) {
            // 捕获业务异常（如Token无效等）
            return ResponseEntity.status(Integer.parseInt(e.getCode())).body(e.getMsg());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token中的用户ID格式不正确。");
        } catch (Exception e) {
            // 捕获其他未知系统异常
            return ResponseEntity.internalServerError().body("获取用户信息失败: 服务器内部错误。");
        }
    }

    /**
     * 获取所有工作人员信息
     * URL: GET /staff/workers
     * @return ResponseEntity 包含所有工作人员信息的列表或错误信息
     */
    @GetMapping("/workers")
    public ResponseEntity<?> getAllWorkers() {
        try {
            // 直接查询所有工作人员，无需从Token获取用户信息
            List<Staff> staffList = staffService.getAllWorkers();

            // 转换为DTO列表（隐藏敏感字段）
            List<WorkerInfoResponse> workerInfoList = staffList.stream()
                    .map(staff -> new WorkerInfoResponse(
                            staff.getStaffId(),
                            staff.getUsername(),
                            staff.getManagerId(),
                            staff.getGeohash()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(workerInfoList);
        } catch (Exception e) {
            // 捕获查询过程中的异常（如数据库连接错误）
            return ResponseEntity.internalServerError()
                    .body("获取工作人员信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前登录工作人员的用户名
     * URL: PUT /staff/updateUsername
     * **请求体示例：**
     *
     json
     * {
     *   "newUsername": "your_new_username"
     * }
     *
     * **请求头示例：**
     * Authorization: Bearer <your_jwt_token>
     *
     * @param authorizationHeader 认证令牌，从请求头中获取用户ID
     * @param request 包含新用户名的请求体 DTO
     * @return ResponseEntity 更新结果
     */
    @PutMapping("/updateUsername")
    public ResponseEntity<String> updateUsername(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateUsernameRequest request) {

        String newUsername = request.getNewUsername();

        try {
            // 从Token中获取当前登录用户的ID，这是您期望的“通过Token码修改”的核心
            String staffIdStr = getUserIdFromToken(authorizationHeader);
            Integer staffId = Integer.parseInt(staffIdStr);

            // 调用Service层进行用户名更新，Service层将处理业务逻辑和异常
            int rowsAffected = staffService.updateUsername(staffId, newUsername);

            if (rowsAffected > 0) {
                return ResponseEntity.ok("用户名修改成功");
            } else {
                // 通常这种情况应被Service层的CustomException捕获
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("用户名修改失败，请检查数据。");
            }
        } catch (CustomException e) {
            // 捕获业务异常（如Token无效，用户名已存在等）
            return ResponseEntity.status(Integer.parseInt(e.getCode())).body(e.getMsg());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token中的用户ID格式不正确。");
        } catch (Exception e) {
            // 捕获其他未知系统异常
            return ResponseEntity.internalServerError().body("用户名修改失败: 服务器内部错误。");
        }
    }

    /**
     * 更新当前登录工作人员的密码
     * URL: PUT /staff/updatePassword
     * **请求体示例：**
     *
     json
     * {
     *   "oldPassword": "your_old_password",
     *   "newPassword": "your_new_password",
     *   "confirmNewPassword": "confirm_new_password"
     * }
     *
     * **请求头示例：**
     * Authorization: Bearer <your_jwt_token>
     *
     * @param authorizationHeader 认证令牌，从请求头中获取用户ID
     * @param request 包含旧密码、新密码和确认新密码的请求体 DTO
     * @return ResponseEntity 更新结果
     */
    @PutMapping("/updatePassword")
    public ResponseEntity<String> updatePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdatePasswordRequest request) {

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        String confirmNewPassword = request.getConfirmNewPassword();

        if (!newPassword.equals(confirmNewPassword)) {
            return ResponseEntity.badRequest().body("新密码和确认密码不一致");
        }

        try {
            // 从Token中获取当前登录用户的ID
            String staffIdStr = getUserIdFromToken(authorizationHeader);
            Integer staffId = Integer.parseInt(staffIdStr);

            // 调用Service层进行密码更新，Service层将负责校验旧密码和哈希新密码
            int rowsAffected = staffService.updatePassword(staffId, oldPassword, newPassword);

            if (rowsAffected > 0) {
                return ResponseEntity.ok("密码修改成功");
            } else {
                // 通常这种情况应被Service层的CustomException捕获
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("密码修改失败，请检查数据。");
            }
        } catch (CustomException e) {
            // 捕获业务异常（如Token无效，旧密码不正确等）
            return ResponseEntity.status(Integer.parseInt(e.getCode())).body(e.getMsg());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token中的用户ID格式不正确。");
        } catch (Exception e) {
            // 捕获其他未知系统异常
            return ResponseEntity.internalServerError().body("密码修改失败: 服务器内部错误。");
        }
    }

    /**
     * 辅助方法：从 Authorization 头中提取 Token 并解析出用户ID
     * @param authorizationHeader Authorization 请求头的值 (例如 "Bearer <token>")
     * @return 解析出的用户ID
     * @throws CustomException 如果 Token 无效或解析失败
     */
    private String getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException("认证失败：缺少Bearer Token", "401");
        }
        String token = authorizationHeader.substring(7); // 提取 "Bearer " 后的 Token 字符串
        try {
            // getUserIdFromToken 通常会包含 validateToken 的逻辑
            // 注意：JwtTokenUtil.getUserIdFromToken() 和 validateToken() 可能需要调整，
            // 如果它们之前依赖 staffType 作为参数。我假设它们现在只依赖 userId 验证。
            String userId = jwtTokenUtil.getUserIdFromToken(token);
            // 额外安全检查：如果getUserIdFromToken不包含 validateToken 逻辑，这里加上
            if (!jwtTokenUtil.validateToken(token, userId)) {
                throw new CustomException("认证失败：Token无效或已过期", "401");
            }
            return userId;
        } catch (Exception e) {
            // 细化异常信息，例如 jwtTokenUtil 内部可能抛出 ExpiredJwtException 等
            String errorMessage = "认证失败：Token解析异常或无效。";
            if (e.getMessage().contains("ExpiredJwtException")) {
                errorMessage = "认证失败：Token已过期。请重新登录。";
            }
            throw new CustomException(errorMessage + " 详情: " + e.getMessage(), "401");
        }
    }
}
