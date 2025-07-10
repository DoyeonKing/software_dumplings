package com.example.springboot.service.implementation;

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

    // 如果BCryptPasswordEncoder没有作为Bean注入，也可以在这里手动实例化：
    // private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(User user) {
        // 1. 数据校验：确保基本信息不为空
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new CustomException("400", "用户名不能为空");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new CustomException("400", "密码不能为空");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            throw new CustomException("400", "电话号码不能为空");
        }

        // 2. 检查用户名是否已存在
        User existingUserByUsername = userMapper.selectByUsername(user.getUsername());
        if (existingUserByUsername != null) {
            // 抛出自定义异常，状态码409表示冲突（资源已存在）
            throw new CustomException("409", "用户名已被注册，请更换");
        }

        // 3. 检查电话号码是否已存在
        User existingUserByPhoneNumber = userMapper.selectByPhoneNumber(user.getPhoneNumber());
        if (existingUserByPhoneNumber != null) {
            // 抛出自定义异常，状态码409表示冲突（资源已存在）
            throw new CustomException("409", "电话号码已被注册，请更换");
        }

        // 4. 生成唯一的用户ID (UUID)
        user.setUserid(UUID.randomUUID().toString());

        // 5. 加密密码
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(encodedPassword); // 将加密后的密码设置回User对象

        // 6. 设置默认值 (如果数据库表没有设置默认值，这里进行兜底)
        if (user.getTotalRides() == null) {
            user.setTotalRides(0);
        }
        if (user.getTotalDurationMinutes() == null) {
            user.setTotalDurationMinutes(0);
        }
        if (user.getTotalCost() == null) {
            user.setTotalCost(BigDecimal.ZERO); // BigDecimal.ZERO表示值为0.00
        }

        // 7. 保存用户到数据库
        try {
            int rowsAffected = userMapper.insert(user);
            if (rowsAffected == 0) {
                throw new CustomException("500", "用户注册失败，请稍后再试");
            }
        } catch (Exception e) {
            // 捕获数据库插入异常，可能是更深层次的唯一性约束（例如，如果Mapper没有做查重）或其他数据库问题
            throw new CustomException("500", "注册过程中发生数据库错误: " + e.getMessage());
        }

        // 8. 返回注册成功后的用户对象 (出于安全考虑，不返回密码哈希)
        user.setPasswordHash(null); // 清除敏感信息
        return user;
    }

    @Override
    public User login(User user) {
        // 1. 数据校验：确保登录凭证不为空
        // 允许通过用户名或手机号登录，但至少要提供一个
        if ((user.getUsername() == null || user.getUsername().isEmpty()) &&
                (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty())) {
            throw new CustomException("400", "用户名或电话号码不能同时为空");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new CustomException("400", "密码不能为空");
        }

        // 2. 根据用户名或电话号码查询数据库中的用户
        User dbUser = null;
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            dbUser = userMapper.selectByUsername(user.getUsername());
        } else if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            dbUser = userMapper.selectByPhoneNumber(user.getPhoneNumber());
        }

        // 3. 检查用户是否存在
        if (dbUser == null) {
            // 状态码401表示未授权（认证失败）
            throw new CustomException("401", "用户不存在或用户名/电话号码不正确");
        }

        // 4. 验证密码
        // 使用BCryptPasswordEncoder的matches方法来比较明文密码和哈希密码
        if (!bCryptPasswordEncoder.matches(user.getPasswordHash(), dbUser.getPasswordHash())) {
            // 状态码401表示未授权（认证失败）
            throw new CustomException("401", "密码错误");
        }

        // 5. 登录成功，清除敏感信息后返回用户对象
        dbUser.setPasswordHash(null); // 清除密码哈希
        return dbUser;
    }
}
