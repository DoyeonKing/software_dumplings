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
     * 插入新用户到数据库
     * @param user 要插入的用户对象，其属性会映射到数据库列
     * @return 影响的行数，通常为1表示插入成功
     */
    @Insert("INSERT INTO user (userid, username, password_hash, phone_number, total_rides, total_duration_minutes, total_cost) " +
            "VALUES (#{userid}, #{username}, #{passwordHash}, #{phoneNumber}, #{totalRides}, #{totalDurationMinutes}, #{totalCost})")
    int insert(User user);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 匹配的用户对象，如果不存在则返回null
     */
    @Select("SELECT userid, username, password_hash, phone_number, total_rides, total_duration_minutes, total_cost FROM user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username); // @Param注解用于给SQL中的参数命名

    /**
     * 根据电话号码查询用户
     * @param phoneNumber 电话号码
     * @return 匹配的用户对象，如果不存在则返回null
     */
    @Select("SELECT userid, username, password_hash, phone_number, total_rides, total_duration_minutes, total_cost FROM user WHERE phone_number = #{phoneNumber}")
    User selectByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // 你也可以在这里添加其他CRUD操作，例如：
    // @Select("SELECT * FROM user WHERE userid = #{userid}")
    // User selectById(String userid);
    //
    // @Update("UPDATE user SET username = #{username}, password_hash = #{passwordHash}, phone_number = #{phoneNumber} WHERE userid = #{userid}")
    // int update(User user);
    //
    // @Delete("DELETE FROM user WHERE userid = #{userid}")
    // int delete(String userid);
    //
    // @Select("SELECT * FROM user")
    // List<User> selectAll();
}
