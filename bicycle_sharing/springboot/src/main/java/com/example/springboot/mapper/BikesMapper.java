package com.example.springboot.mapper;

import com.example.springboot.entity.Bikes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * BikesMapper接口空壳
 * 用于定义对数据库 'bikes' 表的操作方法框架
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface BikesMapper { // 接口名与实体类名保持一致，改为BikesMapper
    /**
     * 获取所有车辆的总数
     * @return 车辆总数
     */
    @Select("SELECT COUNT(*) FROM bikes")
    int countAllBikes();

    /**
     * 根据车辆状态获取车辆数量
     * @param status 车辆状态，如 '使用中', '待使用'
     * @return 对应状态的车辆数量
     */
    @Select("SELECT COUNT(*) FROM bikes WHERE bike_status = #{status}")
    int countByStatus(@Param("status") String status);


    /**
     * 根据单车ID查询单车信息
     *
     * @param bikeId 单车ID
     * @return 匹配的单车对象或 null
     */
    Bikes findByBikeId(@Param("bikeId") String bikeId); // 这个方法在 BikesServiceImpl 中已被使用，确保它存在

    /**
     * 查询所有单车
     *
     * @return 所有单车的列表
     */
    List<Bikes> findAlltoPages();

    /**
     * 查询指定状态的所有单车
     *
     * @param status 单车状态 (例如 "待使用", "使用中")
     * @return 指定状态的单车列表
     */
    List<Bikes> findAllByStatus(@Param("status") String status);

/**
 * 查询指定地理范围内（视口内）的单车列表，支持按状态筛选
 * @param minLat 最小纬度
 * @param maxLat 最大纬度
 * @param minLon 最小经度
 * @param maxLon 最大经度
 * @param bikeStatus 单车状态 (可选，如果为null则查询所有状态)
 * @return 指定范围内的单车列表
 */
List<Bikes> findInViewport(
        @Param("minLat") BigDecimal minLat,
        @Param("maxLat") BigDecimal maxLat,
        @Param("minLon") BigDecimal minLon,
        @Param("maxLon") BigDecimal maxLon,
        @Param("bikeStatus") String bikeStatus); // 新增方法

}