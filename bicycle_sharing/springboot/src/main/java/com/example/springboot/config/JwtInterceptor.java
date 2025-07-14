package com.example.springboot.config;

import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IManagerService;
import com.example.springboot.service.Interface.IStaffService;
import com.example.springboot.service.Interface.IUserService;
import com.example.springboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @class JwtInterceptor
 * @description JWT拦截器，用于在请求到达Controller前验证Token。
 *
 * 1. 这是整个认证系统的核心守卫。
 * 2. 已更新为可以智能处理不同角色（User/String, Staff/Integer, Manager/Integer）的不同ID类型。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /**
     * 3. 注入JWT工具类和所有角色的服务。
     */
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private IUserService userService;
    @Resource
    private IStaffService staffService;
    @Resource
    private IManagerService managerService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 4. 从请求头中获取Token字符串。
        String tokenHeader = request.getHeader("Authorization");

        // 5. 如果请求的不是一个Controller方法，或者是一个跨域预检请求，直接放行。
        if (!(handler instanceof HandlerMethod) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 6. 检查Token是否存在且格式是否正确。
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new CustomException("认证失败：缺少有效的Bearer Token", "401");
        }

        // 7. 提取 "Bearer " 前缀后面的Token值。
        String token = tokenHeader.substring(7);

        try {
            // 8. 从Token中获取ID（此时还是字符串）和角色。
            String userIdStr = jwtTokenUtil.getUserIdFromToken(token);
            String role = jwtTokenUtil.getRoleFromToken(token);

            if (userIdStr == null || role == null) {
                throw new CustomException("认证失败：Token信息不完整", "401");
            }

            Object userInDb;
            Object verifiedId; // 用于存储类型转换后的ID

            // 9. 根据角色，进行不同的ID处理和数据库验证。
            switch (role) {
                case "user":
                    // 9.1. User的ID是String，直接使用。
                    userInDb = userService.getById(userIdStr);
                    verifiedId = userIdStr; // ID保持String类型。
                    break;
                case "worker":
                    // 9.2. Staff的ID是Integer，需要转换。
                    Integer staffId = Integer.parseInt(userIdStr);
                    userInDb = staffService.findByStaffId(staffId);
                    verifiedId = staffId; // ID是Integer类型。
                    break;
                case "admin":
                    // 9.3. Manager的ID是Integer，需要转换。
                    Integer managerId = Integer.parseInt(userIdStr);
                    userInDb = managerService.getById(managerId);
                    verifiedId = managerId; // ID是Integer类型。
                    break;
                default:
                    throw new CustomException("无效的角色", "401");
            }

            // 10. 检查用户是否存在于数据库中。
            if (userInDb == null) {
                throw new CustomException("用户不存在或已被删除，请重新登录", "401");
            }

            // 11. 将验证过的、类型正确的ID存入request，供Controller使用。
            request.setAttribute("userId", verifiedId);

            return true;

        } catch (Exception e) {
            // 12. 如果在验证或解析过程中出现任何异常，都视为认证失败。
            throw new CustomException("认证失败：Token无效或已过期", "401");
        }
    }
}
