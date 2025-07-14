package com.example.springboot.service.Interface;

import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.common.response.LoginResponse;
import com.example.springboot.dto.UpdatePasswordRequest;
import com.example.springboot.entity.Manager;

/**
 * @interface IManagerService
 * @description 管理员服务接口
 *
 * 定义了管理员相关的业务逻辑操作，如登录、注册、根据ID获取信息和更新密码。
 * 具体的实现由 ManagerServiceImpl 类完成。
 */
public interface IManagerService {

    /**
     * 管理员登录。
     * @param loginRequest 包含用户名和密码的登录请求对象
     * @return 包含管理员信息和Token的登录响应对象
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 管理员注册。
     * @param registerRequest 包含用户名、密码和确认密码的注册请求对象
     * @return 注册成功后的管理员实体对象（密码哈希值为空）
     */
    Manager register(RegisterRequest registerRequest);

    /**
     * 根据ID获取管理员信息。
     * @param id 管理员的唯一标识ID
     * @return 查找到的管理员实体对象，如果不存在则返回null
     */
    Manager getById(Integer id);

    /**
     * 更新管理员密码。
     * @param managerId 要更新密码的管理员ID
     * @param passwordRequest 包含旧密码和新密码的请求对象
     */
    void updatePassword(Integer managerId, UpdatePasswordRequest passwordRequest);
}
