package com.example.springboot.mapper;

import com.example.springboot.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * UserMapper接口
 * 用于定义对数据库 'user' 表的操作方法
 */
@Mapper // 标记这是一个MyBatis Mapper接口，Spring Boot会自动扫描并创建其实现
public interface UserMapper {

   /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 匹配的用户对象或 null
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     * @param phoneNumber 手机号
     * @return 匹配的用户对象或 null
     */
    User findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * 插入新用户
     * @param user 待插入的用户对象
     */
    void insert(User user);
}
