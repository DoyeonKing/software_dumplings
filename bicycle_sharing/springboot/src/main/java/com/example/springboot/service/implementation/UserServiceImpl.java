package com.example.springboot.service.implementation;

import com.example.springboot.entity.User; // 导入实体类
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.mapper.UserMapper; // 导入Mapper接口
import com.example.springboot.service.Interface.IUserService; // 导入Service接口
import com.github.pagehelper.PageHelper; // 导入PageHelper (如果需要)
import com.github.pagehelper.PageInfo; // 导入PageInfo (如果需要)
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.stereotype.Service; // 导入Service注解

import java.util.List; // 导入List (如果需要)

/**
 * UserServiceImpl类空壳
 * 实现IUserService接口，包含用户业务逻辑的具体实现框架
 */
@Service // 标记这是一个Spring管理的Service组件
public class UserServiceImpl implements IUserService {

    @Resource // 注入UserMapper
    private UserMapper userMapper;

    // 空壳：不在此处定义任何方法实现
}