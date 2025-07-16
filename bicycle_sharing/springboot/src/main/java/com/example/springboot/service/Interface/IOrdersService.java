package com.example.springboot.service.Interface;

import com.example.springboot.entity.Orders;

import java.math.BigDecimal;
import java.util.List;

/**
 * ITripsService接口空壳
 * 定义用户行程记录相关的业务操作契约框架
 */
public interface IOrdersService { // 接口名与实体类名保持一致，命名为 ITripsService

    /**
     * 获取用户当前的骑车记录
     * @param userId 用户ID
     * @return 未完成的订单信息，如果没有则返回 null
     */
    List<Orders> getCurrentRideRecord(String userId);

    /**
     * 用户租借单车。
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @return 创建的订单信息，如果失败则返回null
     */
    Orders rentBike(String userId, String bikeId);

    /**
     * 用户归还单车。
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @param endLat 结束纬度
     * @param endLon 结束经度
     * @return 完成的订单信息，如果失败则返回null
     */
    Orders returnBike(String userId, String bikeId, BigDecimal endLat, BigDecimal endLon);

    /**
     * 用户租借单车。
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @param userLat 用户纬度
     * @param userLon 用户经度
     * @return 完成的订单信息，如果失败则返回null
     */
    Orders rentBikeLonLat(String userId, String bikeId, BigDecimal userLat, BigDecimal userLon);
}