package com.example.springboot.mapper;

import com.example.springboot.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface StaffMapper {
    /**
     * 根据用户名查询工作人员
     * @param username 用户名
     * @return 匹配的工作人员对象或 null
     */
    Staff findByUsername(@Param("username") String username);

    /**
     * 插入新工作人员
     * @param staff 待插入的工作人员对象
     */
    void insert(Staff staff);

    /**
     * 根据用户名查询员工信息
     * @param username 用户名
     * @return 对应的 Staff 对象，如果不存在则返回 null
     */
    @Select("SELECT staff_id, username, password_hash, staff_type FROM staff WHERE username = #{username}")
    Staff selectByUsername(@Param("username") String username);

    // 如果你有注册功能，也可以添加一个检查用户名是否存在的方法
    @Select("SELECT COUNT(*) FROM staff WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 查询所有工作人员（不包括管理员）
     * @return 所有工作人员对象的列表
     */
    @Select("SELECT staff_id, username, password_hash, staff_type FROM staff WHERE staff_type = '工作人员'")
    List<Staff> findAllWorkers();
}