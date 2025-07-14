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
 * @description IManagerService接口的实现类。
 *
 * 1. 实现了管理员相关的所有业务逻辑。
 */
@Service
public class ManagerServiceImpl implements IManagerService {

    /**
     * 2. 自动注入ManagerMapper，用于数据库操作。
     */
    @Resource
    private ManagerMapper managerMapper;

    /**
     * 3. 自动注入JwtTokenUtil，用于生成Token。
     */
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 4. 处理管理员登录逻辑。
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 4.1. 根据用户名从数据库查找管理员。
        Manager dbManager = managerMapper.findByUsername(loginRequest.getUsername());
        if (dbManager == null) {
            throw new CustomException("用户名或密码错误", "401");
        }
        // 4.2. 将用户输入的密码进行加密，并与数据库中的哈希值比对。
        String inputHashedPassword = SecureUtil.sha256(loginRequest.getPassword());
        if (!inputHashedPassword.equals(dbManager.getPasswordHash())) {
            throw new CustomException("用户名或密码错误", "401");
        }
        // 4.3. 验证通过，生成JWT Token。
        String token = jwtTokenUtil.generateToken(String.valueOf(dbManager.getManagerId()), dbManager.getUsername(), "admin");
        dbManager.setPasswordHash(null);
        return new LoginResponse(dbManager, token, "admin");
    }

    /**
     * 5. 处理管理员注册逻辑。
     */
    @Override
    public Manager register(RegisterRequest registerRequest) {
        // 5.1. 检查用户名是否已存在。
        Manager existingManager = managerMapper.findByUsername(registerRequest.getUsername());
        if (existingManager != null) {
            throw new CustomException("用户名已存在", "409");
        }
        // 5.2. 检查两次输入的密码是否一致。
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致", "400");
        }
        // 5.3. 创建新对象并插入数据库。
        Manager newManager = new Manager();
        newManager.setUsername(registerRequest.getUsername());
        String hashedPassword = SecureUtil.sha256(registerRequest.getPassword());
        newManager.setPasswordHash(hashedPassword);
        managerMapper.insert(newManager);
        newManager.setPasswordHash(null);
        return newManager;
    }

    /**
     * 6. 根据ID获取管理员信息。
     */
    @Override
    public Manager getById(Integer id) {
        return managerMapper.getById(id);
    }

    /**
     * 7. 更新管理员密码。
     */
    @Override
    public void updatePassword(Integer managerId, UpdatePasswordRequest passwordRequest) {
        // 7.1. 校验新密码和确认密码是否一致。
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmNewPassword())) {
            throw new CustomException("两次输入的新密码不一致", "400");
        }
        // 7.2. 从数据库获取当前管理员信息。
        Manager manager = managerMapper.getById(managerId);
        if (manager == null) {
            throw new CustomException("用户不存在", "404");
        }
        // 7.3. 验证旧密码是否正确。
        String oldPasswordHash = SecureUtil.sha256(passwordRequest.getOldPassword());
        if (!oldPasswordHash.equals(manager.getPasswordHash())) {
            throw new CustomException("旧密码错误", "400");
        }
        // 7.4. 将新密码加密并更新到数据库。
        String newPasswordHash = SecureUtil.sha256(passwordRequest.getNewPassword());
        manager.setPasswordHash(newPasswordHash);
        managerMapper.updatePassword(manager);
    }

    /**
     * 8. 更新管理员个人信息。
     */
    @Override
    public void updateProfile(Manager manager) {
        // 8.1. 检查新用户名是否已被其他管理员使用。
        Manager existingManager = managerMapper.findByUsername(manager.getUsername());
        if (existingManager != null && !existingManager.getManagerId().equals(manager.getManagerId())) {
            throw new CustomException("用户名已被占用", "409");
        }
        // 8.2. 调用Mapper更新信息。
        managerMapper.updateProfile(manager);
    }
}
