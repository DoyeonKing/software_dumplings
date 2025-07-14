package com.example.springboot.service.implementation;

import cn.hutool.crypto.SecureUtil;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.common.response.LoginResponse;
import com.example.springboot.dto.UpdatePasswordRequest;
import com.example.springboot.entity.Manager;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.ManagerMapper;
import com.example.springboot.service.Interface.IManagerService;
import com.example.springboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @class ManagerServiceImpl
 * @description IManagerService接口的实现类
 *
 * 实现了管理员相关的所有业务逻辑。
 */
@Service
public class ManagerServiceImpl implements IManagerService {

    // 自动注入ManagerMapper，用于数据库操作
    @Resource
    private ManagerMapper managerMapper;

    // 自动注入JwtTokenUtil，用于生成Token
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 处理管理员登录逻辑。
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. 根据用户名从数据库查找管理员
        Manager dbManager = managerMapper.findByUsername(loginRequest.getUsername());
        // 2. 如果找不到用户，抛出异常
        if (dbManager == null) {
            throw new CustomException("用户名或密码错误", "401");
        }
        // 3. 将用户输入的密码进行SHA-256加密
        String inputHashedPassword = SecureUtil.sha256(loginRequest.getPassword());
        // 4. 将加密后的密码与数据库中存储的哈希值进行比对
        if (!inputHashedPassword.equals(dbManager.getPasswordHash())) {
            throw new CustomException("用户名或密码错误", "401");
        }
        // 5. 验证通过，生成JWT Token
        String token = jwtTokenUtil.generateToken(String.valueOf(dbManager.getManagerId()), dbManager.getUsername(), "admin");
        // 6. 清空密码哈希值，准备返回给前端
        dbManager.setPasswordHash(null);
        // 7. 构建并返回LoginResponse对象
        return new LoginResponse(dbManager, token, "admin");
    }

    /**
     * 处理管理员注册逻辑。
     */
    @Override
    public Manager register(RegisterRequest registerRequest) {
        // 1. 检查用户名是否已存在
        Manager existingManager = managerMapper.findByUsername(registerRequest.getUsername());
        if (existingManager != null) {
            throw new CustomException("用户名已存在", "409");
        }
        // 2. 检查两次输入的密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致", "400");
        }
        // 3. 创建一个新的Manager对象
        Manager newManager = new Manager();
        newManager.setUsername(registerRequest.getUsername());
        // 4. 将密码进行SHA-256加密后存入对象
        String hashedPassword = SecureUtil.sha256(registerRequest.getPassword());
        newManager.setPasswordHash(hashedPassword);
        // 5. 调用Mapper将新管理员数据插入数据库
        managerMapper.insert(newManager);
        // 6. 清空密码哈希值，准备返回给前端
        newManager.setPasswordHash(null);
        return newManager;
    }

    /**
     * 根据ID获取管理员信息。
     */
    @Override
    public Manager getById(Integer id) {
        return managerMapper.getById(id);
    }

    /**
     * 更新管理员密码。
     */
    @Override
    public void updatePassword(Integer managerId, UpdatePasswordRequest passwordRequest) {
        // 1. 检查新密码和确认密码是否一致
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmNewPassword())) {
            throw new CustomException("两次输入的新密码不一致", "400");
        }
        // 2. 从数据库获取当前的管理员信息
        Manager manager = managerMapper.getById(managerId);
        if (manager == null) {
            throw new CustomException("用户不存在", "404");
        }
        // 3. 验证旧密码是否正确
        String oldPasswordHash = SecureUtil.sha256(passwordRequest.getOldPassword());
        if (!oldPasswordHash.equals(manager.getPasswordHash())) {
            throw new CustomException("旧密码错误", "400");
        }
        // 4. 将新密码加密
        String newPasswordHash = SecureUtil.sha256(passwordRequest.getNewPassword());
        manager.setPasswordHash(newPasswordHash);
        // 5. 调用Mapper更新数据库中的信息
        managerMapper.update(manager);
    }
}
