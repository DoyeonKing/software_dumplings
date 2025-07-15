package com.example.springboot.service.Interface;

import com.example.springboot.dto.HeatCell;
import com.example.springboot.dto.UtilizationResponse;
import com.example.springboot.entity.Bikes;
import com.example.springboot.exception.CustomException;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * IBikesService接口空壳
 * 定义自行车相关的业务操作契约框架
 */
public interface IBikesService { // 接口名与实体类名保持一致，改为IBikesService/
    /**
     * 根据区域编号获取单车使用率
     *
     * @param geohash 区域编码
     * @return UtilizationResponse 对象，包含使用率、在线数、使用中数和空闲数。
     */
    UtilizationResponse getVehicleUtilizationByGeohash(String geohash);

    /**
     * 根据 geohash 获取该区域内的所有车辆数量
     * @param geohash 区域编码
     * @return 车辆数量
     */
    int getAllBikeCountByGeohash(String geohash);

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


    /**
     * 生成指定区域内的单车分布热力图数据 (统计每个网格单元内的所有单车)
     * @param minLat 整个大区域的最小纬度
     * @param maxLat 整个大区域的最大纬度
     * @param minLon 整个大区域的最小经度
     * @param maxLon 整个大区域的最大经度
     * @param gridCellsX 横向网格单元数量 (例如 50)
     * @param gridCellsY 纵向网格单元数量 (例如 50)
     * @return 包含热力图数据的列表
     * @throws CustomException 如果获取失败
     */
    List<HeatCell> generateBikeHeatmap(
            BigDecimal minLat,
            BigDecimal maxLat,
            BigDecimal minLon,
            BigDecimal maxLon,
            Integer gridCellsX,
            Integer gridCellsY);

    /**
     * 根据地理哈希列表统计每个区域的自行车数量。
     * 该方法旨在获取指定 geohash 区域内当前可用的自行车总数。
     *
     * @param geohashes 包含需要统计自行车数量的地理哈希编码的列表。
     * @return 一个 Map，其中 key 是地理哈希编码 (String)，value 是该区域的自行车数量 (Long)。
     * 如果某个 geohash 区域没有自行车，它可能不会出现在 Map 中，或者其值为 0。
     */
    Map<String, Long> countBikesByGeohashes(List<String> geohashes);
}