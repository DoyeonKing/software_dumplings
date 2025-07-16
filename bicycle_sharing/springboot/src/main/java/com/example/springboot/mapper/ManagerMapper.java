package com.example.springboot.mapper;

import com.example.springboot.common.PasswordResetPair;
import com.example.springboot.entity.Manager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @interface ManagerMapper
 * @description Manager数据访问层接口 (MyBatis Mapper)
 *
 * 定义了对数据库manager表的操作方法。
 * MyBatis框架会自动根据这个接口和对应的XML文件生成实现。
 */
@Mapper
public interface ManagerMapper {

    /**
     * 批量更新用户密码
     * @param resetPairs 密码重置对列表
     * @return 更新成功的记录数
     */
    int batchUpdatePassword(@Param("resetPairs") List<PasswordResetPair> resetPairs);

    /**
     * 3. 根据用户名查找管理员。
     * 根据用户名查找管理员。
     * @param username 要查找的用户名
     * @return 找到的Manager实体，如果不存在则返回null
     */
    Manager findByUsername(@Param("username") String username);

    /**
     * 插入一个新的管理员记录。
     * @param manager 包含新管理员信息的实体对象
     */
    void insert(Manager manager);

    /**
     * 根据ID查找管理员。
     * @param id 要查找的管理员ID
     * @return 找到的Manager实体，如果不存在则返回null
     */
    Manager getById(@Param("id") Integer id);

    /**
     * 更新管理员信息。
     * @param manager 包含要更新的管理员信息的实体对象
     */
    void update(Manager manager);
}
