package com.example.springboot.service.implementation;

import cn.hutool.crypto.SecureUtil;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.entity.User;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.Interface.IUserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 导入密码编码器
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID; // 用于生成唯一的用户ID

@Service // 标记这是一个Spring Service组件
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper; // 注入UserMapper，用于数据库操作

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder; // 注入密码编码器

   /**
     * 处理普通用户登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的用户对象（已脱敏）
     * @throws CustomException 如果登录失败（如用户名密码错误）
     */
    @Override
    public User login(LoginRequest loginRequest) {
        // 1. 根据用户名查询用户
        User dbUser = userMapper.findByUsername(loginRequest.getUsername());
        if (dbUser == null) {
            throw new CustomException("用户名或密码错误", "401");
        }

        // 2. 密码比对：使用 SHA256 对输入的明文密码进行哈希，然后与数据库中的哈希值比对
        String inputHashedPassword = SecureUtil.sha256(loginRequest.getPassword()); // 使用 Hutool 的 SHA256
        if (!inputHashedPassword.equals(dbUser.getPasswordHash())) {
            throw new CustomException("用户名或密码错误", "401");
        }

        // 3. 返回脱敏后的 User 对象 (不包含密码哈希)
        dbUser.setPasswordHash(null);
        return dbUser;
    }

    /**
     * 处理普通用户注册逻辑
     * @param registerRequest 注册请求 DTO
     * @return 注册成功的用户对象（已脱敏）
     * @throws CustomException 如果注册失败（如用户名/手机号已存在）
     */
    @Override
    public User register(RegisterRequest registerRequest) {
        // 1. 校验用户名是否已存在 (username 对应前端输入的“姓名”)
        User existingUserByUsername = userMapper.findByUsername(registerRequest.getUsername());
        if (existingUserByUsername != null) {
            throw new CustomException("用户名已存在", "409");
        }

        // 2. 校验手机号是否已存在
        User existingUserByPhoneNumber = userMapper.findByPhoneNumber(registerRequest.getPhoneNumber());
        if (existingUserByPhoneNumber != null) {
            throw new CustomException("手机号已注册", "409");
        }

        // 3. 校验密码和确认密码是否一致 (前端已做，后端可再次校验)
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致", "400");
        }

        // 4. 密码哈希：使用 SHA256 对明文密码进行哈希，准备存储
        String hashedPassword = SecureUtil.sha256(registerRequest.getPassword()); // 使用 Hutool 的 SHA256

        // 5. 构建 User 实体并设置字段
        User newUser = new User();
        newUser.setUserid(UUID.randomUUID().toString()); // 生成唯一的用户ID
        newUser.setUsername(registerRequest.getUsername()); // 前端“用户名输入”（姓名）映射到 username
        newUser.setPasswordHash(hashedPassword);
        newUser.setPhoneNumber(registerRequest.getPhoneNumber()); // 前端“手机号”映射到 phoneNumber
        newUser.setTotalRides(0); // 设置默认值
        newUser.setTotalDurationMinutes(0); // 设置默认值
        newUser.setTotalCost(BigDecimal.ZERO); // 设置默认值

        // 6. 调用Mapper插入用户
        userMapper.insert(newUser);

        // 返回脱敏后的用户信息
        newUser.setPasswordHash(null);
        return newUser;
    }
}
