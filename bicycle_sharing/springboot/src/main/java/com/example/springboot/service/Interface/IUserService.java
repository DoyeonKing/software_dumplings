package com.example.springboot.service.Interface;

import com.example.springboot.entity.User;
import com.github.pagehelper.PageInfo; // 如果你计划添加分页功能，可以保留

import java.util.List;

public interface IUserService {

    /**
     * 用户注册接口
     * 接收用户的注册信息 (用户名、密码、手机号)。
     * 在Service层进行数据校验、密码加密、用户ID生成、唯一性检查等业务逻辑。
     * @param user 包含注册信息的User对象（至少包含username, passwordHash, phoneNumber）
     * @return 注册成功后的User对象（通常不包含敏感的passwordHash）
     */
    User register(User user);

    /**
     * 用户登录接口
     * 接收用户的登录凭证 (用户名/手机号、密码)。
     * 在Service层进行用户查找、密码比对等业务逻辑。
     * @param user 包含登录信息的User对象（至少包含username或phoneNumber，以及passwordHash）
     * @return 登录成功后的User对象（通常不包含敏感的passwordHash），如果失败则抛出CustomException
     */
    User login(User user);

    // 其他可能的CRUD或业务方法可以在此接口中定义
    // User getById(String userId);
    // List<User> listAll();
    // PageInfo<User> findPage(Integer pageNum, Integer pageSize, User searchUser);
    // Integer update(User user);
    // Integer delete(String userId);
}
