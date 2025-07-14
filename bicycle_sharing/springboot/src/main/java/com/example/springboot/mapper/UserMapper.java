package com.example.springboot.mapper;

import com.example.springboot.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

 /**
  * 根据用户ID查询用户
  *
  * @param userid 用户唯一标识符
  * @return 匹配的用户对象或 null
  */
 User findByUserId(@Param("userid") String userid);

 /**
  * 更新用户信息
  *
  * @param user 待更新的用户对象
  */
 void update(User user);

/**
     * 根据ID查找用户。
     * 核心修正：确保 @Param 的值为 "id"，与参数名保持一致。
     * 并且ID类型为String，与数据库和业务逻辑一致。
     */
    User getById(@Param("id") String id);
}
