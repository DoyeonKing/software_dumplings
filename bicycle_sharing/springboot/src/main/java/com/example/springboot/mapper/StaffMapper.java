package com.example.springboot.mapper;

import com.example.springboot.entity.Staff;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StaffMapper {
    /**
     * 根据用户名查询工作人员
     * @param username 用户名
     * @return 匹配的工作人员对象或 null
     */
    // 移除 staff_type 字段的查询
    @Select("SELECT staff_id, username, password_hash, managerId, geohash FROM staff WHERE username = #{username}")
    Staff findByUsername(@Param("username") String username);

    /**
     * 插入新工作人员
     * @param staff 待插入的工作人员对象
     */
    // 移除 staff_type 字段的插入
    @Insert("INSERT INTO staff (username, password_hash) VALUES (#{username}, #{passwordHash})")
    @Options(useGeneratedKeys = true, keyProperty = "staffId", keyColumn = "staff_id") // 确保自增ID能返回给实体
    void insert(Staff staff);

    /**
     * 根据用户名查询员工信息 (与 findByUsername 功能相似，但明确带注解)
     * 保持与 findByUsername 一致，只查必要的字段
     * @param username 用户名
     * @return 对应的 Staff 对象，如果不存在则返回 null
     */
    // 移除 staff_type 字段的查询
    @Select("SELECT staff_id, username, password_hash, managerId, geohash FROM staff WHERE username = #{username}")
    Staff selectByUsername(@Param("username") String username);

    // 如果你有注册功能，也可以添加一个检查用户名是否存在的方法
    @Select("SELECT COUNT(*) FROM staff WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 查询所有工作人员 (现在将查询所有员工，不再区分角色)
     * @return 所有工作人员对象的列表
     */
    // 移除 staff_type 的过滤条件
    @Select("SELECT staff_id, username, managerId, geohash FROM staff")
    List<Staff> findAllWorkers();

    /**
     * 更新工作人员的用户名
     * @param staffId 工作人员ID
     * @param newUsername 新用户名
     * @return 更新影响的行数
     */
    @Update("UPDATE staff SET username = #{newUsername} WHERE staff_id = #{staffId}")
    int updateUsername(@Param("staffId") Integer staffId, @Param("newUsername") String newUsername);

    /**
     * 更新工作人员的密码
     * @param staffId 工作人员ID
     * @param newPasswordHash 新密码哈希值
     * @return 更新影响的行数
     */
    @Update("UPDATE staff SET password_hash = #{newPasswordHash} WHERE staff_id = #{staffId}")
    int updatePassword(@Param("staffId") Integer staffId, @Param("newPasswordHash") String newPasswordHash);


    //使用token码获得staff信息
    // 移除 staff_type 字段的查询
    @Select("SELECT staff_id, username, password_hash, managerId, geohash FROM staff WHERE staff_id = #{staffId}")
    Staff selectById(@Param("staffId") Integer staffId);


    /**
     * 根据管理员ID查询其管理的工作人员列表
     * @param managerId 管理员ID
     * @return 对应的 Staff 对象列表，如果不存在则返回空列表
     */
    @Select("SELECT staff_id, username, password_hash, managerId, geohash FROM staff WHERE managerId = #{managerId}")
    List<Staff> selectStaffByManagerId(@Param("managerId") Integer managerId);
}
