package com.example.springboot.service.implementation;

import cn.hutool.crypto.SecureUtil;
import com.example.springboot.common.PasswordResetPair;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class StaffServiceImpl implements IStaffService {

    @Resource
    private StaffMapper staffMapper;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 批量重置指定范围内用户的密码为默认密码
     * @param startId 起始ID（包含）
     * @param endId 结束ID（包含）
     * @return 更新成功的用户数量
     */
    @Transactional
    @Override
    public int batchResetPasswords(int startId, int endId) {
        List<PasswordResetPair> resetPairs = new ArrayList<>();

        // 生成ID和对应默认密码的哈希
        for (int id = startId; id <= endId; id++) {
            String defaultPassword = "default_password_" + id;
            String hashedPassword = SecureUtil.sha256(defaultPassword);
            resetPairs.add(new PasswordResetPair(id, hashedPassword));
        }

        // 执行批量更新
        return staffMapper.batchUpdatePassword(resetPairs);
    }


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
        // 重要：去除输入密码前后的空格，避免因空格导致哈希不匹配
        String inputPasswordTrimmed = loginRequest.getPassword().trim();
        String inputHashedPassword = SecureUtil.sha256(inputPasswordTrimmed);

        if (!inputHashedPassword.equals(dbStaff.getPasswordHash())) {
            // 调试信息（可以在上线前移除或改为日志）
            // System.out.println("Input Hashed: " + inputHashedPassword);
            // System.out.println("DB Hashed:    " + dbStaff.getPasswordHash());
            throw new CustomException("用户名或密码错误", "401");
        }

        // 4. 登录成功，生成 JWT Token
        // 移除 staffType 参数，Token 只包含 staffId 和 username
        String token = jwtTokenUtil.generateToken(String.valueOf(dbStaff.getStaffId()), dbStaff.getUsername(),"worker");

        // 5. 返回脱敏后的 Staff 对象和 Token
        dbStaff.setPasswordHash(null);
        // LoginResponse 构造函数可能需要调整以移除 staffType
        // 假设 LoginResponse 只需要 Staff 对象和 Token
        return new LoginResponse(dbStaff, token);
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
        // 重要：去除密码前后的空格，确保比较的准确性
        String password = registerRequest.getPassword().trim();
        String confirmPassword = registerRequest.getConfirmPassword().trim();

        if (!password.equals(confirmPassword)) {
            throw new CustomException("两次输入的密码不一致", "400");
        }

        // 3. 密码哈希：使用 SHA256 对明文密码进行哈希，准备存储
        String hashedPassword = SecureUtil.sha256(password); // 使用去除空格后的密码进行哈希

        // 4. 构建 Staff 实体并设置字段
        Staff newStaff = new Staff();
        newStaff.setUsername(registerRequest.getUsername().trim()); // 用户名也最好trim一下
        newStaff.setPasswordHash(hashedPassword);

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
        // 直接调用 Mapper 查询所有工作人员信息
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

        // 对新用户名进行trim
        String trimmedNewUsername = newUsername.trim();

        // 2. 检查新用户名是否已被占用，且不是当前用户的旧用户名
        // 如果新用户名与当前用户的旧用户名相同，则无需更新，直接返回成功
        if (trimmedNewUsername.equals(staffToUpdate.getUsername())) {
            return 1; // 视为成功更新，因为结果是一样的
        }

        // 检查新用户名是否被其他用户占用
        Staff existingStaffWithNewUsername = staffMapper.findByUsername(trimmedNewUsername); // 使用findByUsername
        if (existingStaffWithNewUsername != null) {
            throw new CustomException("新用户名 '" + trimmedNewUsername + "' 已被占用", "409");
        }

        // 3. 执行更新
        return staffMapper.updateUsername(staffId, trimmedNewUsername);
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


        String hashedOldPassword = SecureUtil.sha256(oldPassword.trim()); // trim旧密码
        if (!hashedOldPassword.equals(staff.getPasswordHash())) {
            throw new CustomException("旧密码不正确", "400");
        }

        // 3. 哈希新密码
        String hashedNewPassword = SecureUtil.sha256(newPassword.trim()); // trim新密码


        // 4. 执行密码更新
        return staffMapper.updatePassword(staffId, hashedNewPassword);
    }

    @Override
    public Staff findByStaffId(Integer staffId) {
        // 调用 Mapper 查询用户信息
        return staffMapper.selectById(staffId);
    }


    /**
     * 根据管理员ID获取其管理的所有工作人员信息
     * @param managerId 管理员ID
     * @return 该管理员管理的所有工作人员对象的列表
     */
    @Override
    public List<Staff> getStaffByManagerId(Integer managerId) {
        // 直接调用 StaffMapper 来根据 managerId 查询工作人员列表
        return staffMapper.selectStaffByManagerId(managerId);
    }
}
