package com.example.springboot.service.implementation;

import cn.hutool.crypto.SecureUtil;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.entity.Staff;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.StaffMapper;
import com.example.springboot.service.Interface.IStaffService;
import com.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StaffServiceImpl implements IStaffService {

    @Resource
    private StaffMapper staffMapper;

    /**
     * 处理 Staff 用户登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的工作人员响应 Map (包含 Staff 信息和Token)
     * @throws CustomException 如果登录失败
     */
    @Override
    public Map<String, Object> login(LoginRequest loginRequest) {
        // 1. 根据用户名查询 Staff
        Staff dbStaff = staffMapper.findByUsername(loginRequest.getUsername());
        if (dbStaff == null) {
            throw new CustomException("用户名或密码错误", "401");
        }

        // 2. 密码比对：使用 SHA256 对输入的明文密码进行哈希，然后与数据库中的哈希值比对
        String inputHashedPassword = SecureUtil.sha256(loginRequest.getPassword());
        if (!inputHashedPassword.equals(dbStaff.getPasswordHash())) {
            throw new CustomException("用户名或密码错误", "401");
        }

        // 3. 校验 staff_type 是否与请求的 role 匹配// 前端传入的 role 可能是 "admin" 或 "worker"// 数据库中的 staff_type 是 "管理员" 或 "工作人员"// 这里需要一个匹配逻辑，例如：// 如果前端 role 是 "admin"，数据库 staff_type 必须是 "管理员"// 如果前端 role 是 "worker"，数据库 staff_type 必须是 "工作人员"
        if (("admin".equalsIgnoreCase(loginRequest.getRole()) && !"管理员".equals(dbStaff.getStaffType())) ||
        ("worker".equalsIgnoreCase(loginRequest.getRole()) && !"工作人员".equals(dbStaff.getStaffType()))) {
            throw new CustomException("角色不匹配，请选择正确的角色登录", "403");
        }

        // 4. 登录成功，生成 JWT Token// 注意：staff_id 是 Integer，需要转换为 String 传递给 generateToken
        String token = JwtTokenUtil.generateToken(String.valueOf(dbStaff.getStaffId()), dbStaff.getUsername(), dbStaff.getStaffType());

        // 5. 返回脱敏后的 Staff 对象和 Token
        dbStaff.setPasswordHash(null); // 脱敏
        Map<String, Object> response = new HashMap<>();
        response.put("staff", dbStaff); // 将 Staff 对象放入 Map
        response.put("token", token); // 将 Token 放入 Map
        return response;
    }

    /**
     * 处理工作人员注册逻辑
     * @param registerRequest 注册请求 DTO
     * @return 注册成功的工作人员对象（已脱敏）
     * @throws CustomException 如果注册失败（如用户名已存在）
     */
    @Override
    public Staff register(RegisterRequest registerRequest) {

        // 1. 校验用户名是否已存在
        Staff existingStaff = staffMapper.findByUsername(registerRequest.getUsername());
        if (existingStaff != null) {
            throw new CustomException("用户名已存在", "409");
        }

        // 2. 校验密码和确认密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致", "400");
        }

        // 3. 密码哈希：使用 SHA256 对明文密码进行哈希，准备存储
        String hashedPassword = SecureUtil.sha256(registerRequest.getPassword());

        // 4. 构建 Staff 实体并设置字段
        Staff newStaff = new Staff();
        newStaff.setUsername(registerRequest.getUsername());
        newStaff.setPasswordHash(hashedPassword);
        newStaff.setStaffType(registerRequest.getRole()); // 根据角色设置 staffType

        //转为中文
        if ("admin".equalsIgnoreCase(registerRequest.getRole())) {
            newStaff.setStaffType("管理员");
        } else if ("worker".equalsIgnoreCase(registerRequest.getRole())) {
            newStaff.setStaffType("工作人员");
        }

        // 5. 调用Mapper插入工作人员
        staffMapper.insert(newStaff);


        // 返回脱敏后的工作人员信息
        newStaff.setPasswordHash(null);
        return newStaff;
    }

    @Override
    public Staff findByUsername(String username) {
        return staffMapper.selectByUsername(username);
    }
}