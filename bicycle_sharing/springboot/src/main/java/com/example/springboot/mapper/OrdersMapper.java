package com.example.springboot.mapper;

import com.example.springboot.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * OrdersMapper接口
 * 用于定义对数据库 'orders' 表的操作方法
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface OrdersMapper {
    /**
     * 插入新的订单记录
     * @param order 订单实体
     * @return 影响的行数
     */
    int insert(Orders order);

    /**
     * 更新订单记录
     * @param order 订单实体
     * @return 影响的行数
     */
    int update(Orders order);

    /**
     * 根据用户ID和单车ID查找正在进行的订单 (通常endTime为空的订单)
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @return 订单实体，如果不存在则返回null
     */
    Orders findActiveOrderByUserAndBike(@Param("userid") String userId, @Param("bikeid") String bikeId);

    /**
     * 根据订单ID查找订单
     * @param orderId 订单ID
     * @return 订单实体，如果不存在则返回null
     */
    Orders findByOrderId(@Param("orderid") String orderId);
}
