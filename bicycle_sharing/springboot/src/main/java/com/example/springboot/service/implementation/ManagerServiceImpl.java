package com.example.springboot.service.implementation;

import cn.hutool.crypto.SecureUtil;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.common.response.LoginResponse;
import com.example.springboot.entity.Manager;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.ManagerMapper;
import com.example.springboot.service.Interface.IManagerService;
import com.example.springboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ManagerServiceImpl implements IManagerService {

    @Resource
    private ManagerMapper managerMapper;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Manager dbManager = managerMapper.findByUsername(loginRequest.getUsername());
        if (dbManager == null) {
            throw new CustomException("用户名或密码错误", "401");
        }
        String inputHashedPassword = SecureUtil.sha256(loginRequest.getPassword());
        if (!inputHashedPassword.equals(dbManager.getPasswordHash())) {
            throw new CustomException("用户名或密码错误", "401");
        }
        String token = jwtTokenUtil.generateToken(String.valueOf(dbManager.getManagerId()), dbManager.getUsername(), "admin");
        dbManager.setPasswordHash(null);
        return new LoginResponse(dbManager, token, "admin");
    }

    @Override
    public Manager register(RegisterRequest registerRequest) {
        Manager existingManager = managerMapper.findByUsername(registerRequest.getUsername());
        if (existingManager != null) {
            throw new CustomException("用户名已存在", "409");
        }
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致", "400");
        }
        String hashedPassword = SecureUtil.sha256(registerRequest.getPassword());
        Manager newManager = new Manager();
        newManager.setUsername(registerRequest.getUsername());
        newManager.setPasswordHash(hashedPassword);
        managerMapper.insert(newManager);
        newManager.setPasswordHash(null);
        return newManager;
    }
}