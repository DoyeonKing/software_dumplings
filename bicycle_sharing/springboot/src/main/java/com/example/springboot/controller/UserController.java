package com.example.springboot.controller;

import com.example.springboot.common.Result; // 导入统一响应结果类
import com.example.springboot.entity.User; // 导入实体类
import com.example.springboot.exception.CustomException; // 导入自定义异常
import com.example.springboot.service.Interface.IUserService; // 导入Service接口
import com.github.pagehelper.PageInfo; // 导入分页PageInfo (如果需要)
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.util.List; // 导入List (如果需要)
import java.util.Map; // 导入Map (如果需要)

/**
 * UserController
 * 负责接收和处理与用户相关的HTTP请求
 */
@RestController // 标记这是一个RESTful控制器，返回JSON或XML数据
@RequestMapping("/user") // 定义这个控制器的基础URL路径，所有方法都会以此为前缀
public class UserController {

    @Resource // 注入IUserService接口的实现类 (Spring会自动找到UserServiceImpl)
    private IUserService userService;

    /**
     * 用户注册接口
     * 请求方式: POST
     * 请求路径: /user/register
     * 请求体: JSON格式的User对象 (至少包含 username, passwordHash, phoneNumber)
     * 响应: Result对象，包含注册结果信息
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            // 调用Service层进行用户注册业务逻辑
            User registeredUser = userService.register(user);
            // 注册成功，返回成功的Result，并附带注册后的用户信息（不含密码）
            // 注意：你提供的Result.success(Object data)方法默认msg是"请求成功"
            return Result.success(registeredUser);
        } catch (CustomException e) {
            // 捕获Service层抛出的自定义业务异常，并返回带有特定错误码和消息的Result
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            // 捕获其他未知异常，返回通用错误信息
            e.printStackTrace(); // 在生产环境中可能需要更详细的日志记录
            // 注意：你提供的Result.error(String code, String msg)方法
            return Result.error("500", "注册失败，服务器内部错误：" + e.getMessage());
        }
    }

}
