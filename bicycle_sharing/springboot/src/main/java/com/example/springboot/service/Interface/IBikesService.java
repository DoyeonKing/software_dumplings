package com.example.springboot.service.Interface;

import com.example.springboot.dto.UtilizationResponse;
import com.example.springboot.entity.Bikes;
import com.example.springboot.exception.CustomException;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * IBikesService接口空壳
 * 定义自行车相关的业务操作契约框架
 */
public interface IBikesService { // 接口名与实体类名保持一致，改为IBikesService/
    /**
     * 获取车辆使用率及其相关统计数据。
     * @return UtilizationResponse 对象，包含使用率、在线数、使用中数和空闲数。
     */
    UtilizationResponse getVehicleUtilization();

    /**
     * 获取所有可用（状态为“待使用”）的单车列表
     *
     * @return 可用单车列表
     * @throws CustomException 如果获取失败
     */
    List<Bikes> getAllAvailableBikes();

    /**
     * 获取单车列表，支持按状态筛选和分页
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param bikeStatus 单车状态 (可选，如果为null则查询所有状态)
     * @return 包含分页信息的单车列表
     * @throws CustomException 如果获取失败
     */
    PageInfo<Bikes> getBikesByPage(Integer pageNum, Integer pageSize, String bikeStatus);


    /**
     * 获取单个单车的详细信息（处理后不包含bikeId）
     *
     * @param bikeId 单车唯一标识符
     * @return 单车详情对象，如果未找到则为 null
     * @throws CustomException 如果获取失败
     */
    Bikes getBikeDetails(String bikeId);


    /**
 * 获取指定地理范围内（视口内）的单车列表，支持按状态筛选
 * @param minLat 最小纬度
 * @param maxLat 最大纬度
 * @param minLon 最小经度
 * @param maxLon 最大经度
 * @param bikeStatus 单车状态 (可选，如果为null则查询所有状态)
 * @return 指定范围内的单车列表
 * @throws CustomException 如果获取失败
 */
List<Bikes> getBikesInViewport(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLon, BigDecimal maxLon, String bikeStatus);

}