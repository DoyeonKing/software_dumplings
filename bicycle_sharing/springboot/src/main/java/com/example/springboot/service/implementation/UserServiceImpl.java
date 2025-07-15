package com.example.springboot.service.implementation;

import cn.hutool.crypto.SecureUtil;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.common.response.LoginResponse;
import com.example.springboot.entity.User;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.Interface.IUserService;
import com.example.springboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
// 移除对 BCryptPasswordEncoder 的导入
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper;

    // 移除对 BCryptPasswordEncoder 的注入
    // @Resource
    // private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User dbUser = userMapper.findByUsername(loginRequest.getUsername());
        if (dbUser == null) {
            throw new CustomException("用户名或密码错误", "401");
        }

        String inputHashedPassword = SecureUtil.sha256(loginRequest.getPassword());
        if (!inputHashedPassword.equals(dbUser.getPasswordHash())) {
            throw new CustomException("用户名或密码错误", "401");
        }

        String token = jwtTokenUtil.generateToken(dbUser.getUserid(), dbUser.getUsername(), "user");
        dbUser.setPasswordHash(null);
        return new LoginResponse(dbUser, token);
    }

    @Override
    public User register(RegisterRequest registerRequest) {
        User existingUserByUsername = userMapper.findByUsername(registerRequest.getUsername());
        if (existingUserByUsername != null) {
            throw new CustomException("用户名已存在", "409");
        }

        User existingUserByPhoneNumber = userMapper.findByPhoneNumber(registerRequest.getPhoneNumber());
        if (existingUserByPhoneNumber != null) {
            throw new CustomException("手机号已注册", "409");
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致", "400");
        }

        String hashedPassword = SecureUtil.sha256(registerRequest.getPassword());

        User newUser = new User();
        newUser.setUserid(UUID.randomUUID().toString());
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPasswordHash(hashedPassword);
        newUser.setPhoneNumber(registerRequest.getPhoneNumber());
        newUser.setTotalRides(0);
        newUser.setTotalDurationMinutes(0);
        newUser.setTotalCost(BigDecimal.ZERO);

        userMapper.insert(newUser);

        newUser.setPasswordHash(null);
        return newUser;
    }

    @Override
    public User getUserProfile(String userId) {
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new CustomException("用户不存在", "404");
        }
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public void updateUserProfile(User user) {
        if (user.getUserid() == null) {
            throw new CustomException("用户ID不能为空", "400");
        }
        User existingUser = userMapper.findByUserId(user.getUserid());
        if (existingUser == null) {
            throw new CustomException("用户不存在", "404");
        }

        if (user.getUsername() != null && !user.getUsername().equals(existingUser.getUsername())) {
            if (userMapper.findByUsername(user.getUsername()) != null) {
                throw new CustomException("用户名已存在", "409");
            }
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
            if (userMapper.findByPhoneNumber(user.getPhoneNumber()) != null) {
                throw new CustomException("手机号已注册", "409");
            }
        }

        user.setPasswordHash(null);
        userMapper.update(user);
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new CustomException("用户不存在", "404");
        }

        String hashedOldPassword = SecureUtil.sha256(oldPassword);
        if (!hashedOldPassword.equals(user.getPasswordHash())) {
            throw new CustomException("旧密码不正确", "401");
        }

        String hashedNewPassword = SecureUtil.sha256(newPassword);
        user.setPasswordHash(hashedNewPassword);
        userMapper.update(user);
    }

    @Override
    public User getById(String id) {
        return userMapper.getById(id);
    }
}
