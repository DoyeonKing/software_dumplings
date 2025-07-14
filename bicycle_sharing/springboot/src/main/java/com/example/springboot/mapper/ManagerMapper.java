package com.example.springboot.mapper;

import com.example.springboot.entity.Manager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ManagerMapper {
    Manager findByUsername(@Param("username") String username);
    void insert(Manager manager);
}