package com.example.springboot.mapper;

import com.example.springboot.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

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
}