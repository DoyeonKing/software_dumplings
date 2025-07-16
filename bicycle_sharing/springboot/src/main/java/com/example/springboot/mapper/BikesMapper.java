package com.example.springboot.mapper;

import com.example.springboot.entity.Bikes;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * BikesMapper接口空壳
 * 用于定义对数据库 'bikes' 表的操作方法框架
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface BikesMapper { // 接口名与实体类名保持一致，改为BikesMapper
    /**
     * 根据 geohash统计自行车数量。
     */
    @Select("SELECT COUNT(*) FROM bikes WHERE current_geohash = #{currentGeohash}")
    int countAllByCurrentGeohash(@Param("currentGeohash") String currentGeohash);

    /**
     * 根据 geohash 和车辆状态统计自行车数量。
     * 注意：Bikes实体中的bikeStatus现在是String类型，Mapper方法参数也应为String。
     */
    @Select("SELECT COUNT(*) FROM bikes WHERE current_geohash = #{currentGeohash} AND bike_status = #{bikeStatus}")
    int countByCurrentGeohashAndBikeStatus(@Param("currentGeohash") String currentGeohash, @Param("bikeStatus") String bikeStatus);

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


    /**
     * 统计指定地理范围内（视口内）的单车数量 (不区分状态)
     * @param minLat 最小纬度
     * @param maxLat 最大纬度
     * @param minLon 最小经度
     * @param maxLon 最大经度
     * @return 指定范围内的单车数量
     */
    @Select({
            "SELECT COUNT(*) FROM bikes",
            "WHERE current_lat BETWEEN #{minLat} AND #{maxLat}",
            "AND current_lon BETWEEN #{minLon} AND #{maxLon}"
    })
    int countInViewport(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon);

    /**
     * 查询指定 geohash 区域内所有 "待使用" 状态的自行车。
     * 【作用】：用于获取起始区域的可用自行车列表和目标区域当前已有的自行车列表。
     * @param geohash 区域的地理哈希值。
     * @return 符合条件的自行车实体列表。
     */
    List<Bikes> findAvailableBikesByGeohash(@Param("geohash") String geohash); // SQL 移至 XML

    /**
     * 更新自行车的状态和位置信息。
     * 【作用】：在调度过程中更新自行车的状态为 "调度中"，并将其逻辑位置更新为目标停车点的精确经纬度。
     * @param bikeId 自行车ID。
     * @param newStatus 自行车的新状态 (例如："调度中")。
     * @param newLat 可选：新的纬度。
     * @param newLon 可选：新的经度。
     * @param newGeohash 可选：新的 geohash。
     * @return 数据库受影响的行数。
     */
    int updateBikeStatusAndLocation(
            @Param("bikeId") String bikeId,
            @Param("newStatus") String newStatus,
            @Param("newLat") BigDecimal newLat,
            @Param("newLon") BigDecimal newLon,
            @Param("newGeohash") String newGeohash); // SQL 移至 XML

    /**
     * 获取所有单车的经纬度信息
     * @return 单车的经纬度信息列表
     */
    @Select("SELECT bike_id AS bikeId, current_lat AS lat, current_lon AS lon FROM bikes")
    List<Bikes> getAllBikeLocations();

/**
     * 根据地理哈希列表，从数据库中统计每个区域的自行车数量。
     * 返回一个列表，其中每个 Map 包含 'geohash' (String) 和 'count' (Long) 两个键值对。
     *
     * @param geohashes 包含需要统计自行车数量的地理哈希编码的列表。
     * @return 一个 List，其中每个 Map 包含 'geohash' (String) 和 'count' (Long) 两个键值对。
     */
    List<Map<String, Object>> selectBikesCountByGeohashes(@Param("list") List<String> geohashes);
}