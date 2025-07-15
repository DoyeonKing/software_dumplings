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

    /**
     * 获取用户个人信息
     * @param userId 用户ID
     * @return 用户对象（已脱敏）
     * @throws CustomException 如果用户不存在
     */
    @Override
    public User getUserProfile(String userId) {
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new CustomException("用户不存在", "404");
        }
        // 脱敏处理，不返回密码哈希
        user.setPasswordHash(null);
        return user;
    }

    /**
     * 更新用户个人信息
     * @param user 包含待更新字段的用户对象
     * @throws CustomException 如果更新失败（如用户名/手机号已存在）
     */
    @Override
    public void updateUserProfile(User user) {
        if (user.getUserid() == null) {
            throw new CustomException("用户ID不能为空", "400");
        }
        User existingUser = userMapper.findByUserId(user.getUserid());
        if (existingUser == null) {
            throw new CustomException("用户不存在", "404");
        }

        // 2. 校验更新的 username 或 phoneNumber 是否已被其他用户占用
        // 只有当传入的字段值与数据库中不同时才进行唯一性校验
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

        // 3. 调用 Mapper 更新用户
        // 确保只更新允许更新的字段，例如 username, phoneNumber。密码不允许在此处修改。
        // 传入的 user 对象中的 passwordHash 字段应该被忽略或设为 null，以防意外覆盖
        user.setPasswordHash(null); // 防止前端可能意外传入密码导致覆盖
        userMapper.update(user);
    }

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @throws CustomException 如果旧密码不正确或修改失败
     */
    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new CustomException("用户不存在", "404");
        }

        // 1. 验证旧密码：使用 SHA256 对旧密码进行哈希，然后与数据库中的哈希值比对
        String hashedOldPassword = SecureUtil.sha256(oldPassword);
        if (!hashedOldPassword.equals(user.getPasswordHash())) {
            throw new CustomException("旧密码不正确", "401");
        }

        // 2. 更新新密码：使用 SHA256 对新密码进行哈希
        String hashedNewPassword = SecureUtil.sha256(newPassword);
        user.setPasswordHash(hashedNewPassword);
        // 调用 userMapper.update 方法来更新 password_hash 字段
        userMapper.update(user); // 假设 update 方法会根据传入的 User 对象更新其非空字段，包括 passwordHash
    }

    /**
     * 根据ID获取用户信息 (ID类型为String)。
     * 实现接口中定义的方法，直接调用Mapper。
     */
    @Override
    public User getById(String id) {
        return userMapper.getById(id);
    }
}
