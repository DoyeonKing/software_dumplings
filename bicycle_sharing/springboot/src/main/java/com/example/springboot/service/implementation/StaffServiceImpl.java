package com.example.springboot.service.implementation;

import cn.hutool.crypto.SecureUtil;
import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.common.response.LoginResponse;
import com.example.springboot.entity.Staff;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.StaffMapper;
import com.example.springboot.service.Interface.IStaffService;
import com.example.springboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffServiceImpl implements IStaffService {

    @Resource
    private StaffMapper staffMapper;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 处理工作人员登录逻辑
     * @param loginRequest 登录请求 DTO
     * @return 登录成功的响应对象（包含工作人员信息和Token）
     * @throws CustomException 如果登录失败（如用户名密码错误）
     */
    @Override
    public Object login(LoginRequest loginRequest) {
        // 1. 根据用户名查询工作人员
        Staff dbStaff = staffMapper.findByUsername(loginRequest.getUsername());
        if (dbStaff == null) {
            throw new CustomException("用户名或密码错误", "401");
        }

        // 2. 密码比对：使用 SHA256 对输入的明文密码进行哈希，然后与数据库中的哈希值比对
        String inputHashedPassword = SecureUtil.sha256(loginRequest.getPassword());
        if (!inputHashedPassword.equals(dbStaff.getPasswordHash())) {
            throw new CustomException("用户名或密码错误", "401");
        }

        // 3. 登录成功，生成 JWT Token
        String token = jwtTokenUtil.generateToken(String.valueOf(dbStaff.getStaffId()), dbStaff.getUsername(), dbStaff.getStaffType());

        // 4. 返回脱敏后的 Staff 对象和 Token
        dbStaff.setPasswordHash(null);
        // 注意：这里的LoginResponse第一个参数传null，因为用户信息在dbStaff对象中，可以根据前端需要调整
        return new LoginResponse(null, token);
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

    @Override
    public List<Staff> getAllWorkers() {
        return staffMapper.findAllWorkers();
    }

    /**
     * 更新工作人员的用户名
     * @param staffId 工作人员ID (从Token解析而来)
     * @param newUsername 新用户名
     * @return 更新影响的行数
     * @throws CustomException 如果新用户名已存在或用户不存在
     */
    @Override
    @Transactional
    public int updateUsername(Integer staffId, String newUsername) {
        // 1. 确保要更新的staffId存在
        Staff staffToUpdate = staffMapper.selectById(staffId);
        if (staffToUpdate == null) {
            throw new CustomException("要更新的员工不存在", "404");
        }

        // 2. 检查新用户名是否已被占用，且不是当前用户的旧用户名
        // 如果新用户名与当前用户的旧用户名相同，则无需更新，直接返回成功
        if (newUsername.equals(staffToUpdate.getUsername())) {
            return 1; // 视为成功更新，因为结果是一样的
        }

        // 检查新用户名是否被其他用户占用
        Staff existingStaffWithNewUsername = staffMapper.findByUsername(newUsername);
        if (existingStaffWithNewUsername != null) {
            throw new CustomException("新用户名 '" + newUsername + "' 已被占用", "409");
        }

        // 3. 执行更新
        return staffMapper.updateUsername(staffId, newUsername);
    }

    /**
     * 更新工作人员的密码
     * @param staffId 工作人员ID (从Token解析而来)
     * @param oldPassword 明文旧密码
     * @param newPassword 明文新密码
     * @return 更新影响的行数
     * @throws CustomException 如果旧密码不正确或用户不存在
     */
    @Override
    @Transactional
    public int updatePassword(Integer staffId, String oldPassword, String newPassword) {
        // 1. 根据 staffId 查询用户，获取其当前密码哈希
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw new CustomException("用户不存在", "404");
        }

        // 2. 校验旧密码
        String hashedOldPassword = SecureUtil.sha256(oldPassword);
        if (!hashedOldPassword.equals(staff.getPasswordHash())) {
            throw new CustomException("旧密码不正确", "400");
        }

        // 3. 哈希新密码
        String hashedNewPassword = SecureUtil.sha256(newPassword);

        // 4. 执行密码更新
        return staffMapper.updatePassword(staffId, hashedNewPassword);
    }
}