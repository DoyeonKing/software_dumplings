// src/main/java/com/example/springboot/mapper/GeohashInfoMapper.java
package com.example.springboot.mapper;

import com.example.springboot.dto.CenterLatLonResponse;
import com.example.springboot.entity.EliteSites;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * GeohashInfoMapper接口
 * 用于定义对数据库 'geohash_info' 表的操作方法
 */
@Mapper
public interface EliteSitesMapper {
    /**
     * 根据 geohashArea 查询停车点信息。
     * 【作用】：用于在调度时，通过目标 geohash 查找对应的精确经纬度。
     * @param geohash 停车点的地理哈希信息。
     * @return 对应的 EliteSites 实体，如果找不到则返回 null。
     */
    EliteSites findByGeohashInfo(@Param("geohash") String geohash);

    // 根据经纬度范围查询停车区域信息
    @Select("SELECT * FROM elite_sites " +
            "WHERE center_lat BETWEEN #{minLat} AND #{maxLat} " +
            "AND center_lon BETWEEN #{minLon} AND #{maxLon}")
    List<EliteSites> findByLatLngRange(@Param("minLat") BigDecimal minLat,
                                       @Param("maxLat") BigDecimal maxLat,
                                       @Param("minLon") BigDecimal minLon,
                                       @Param("maxLon") BigDecimal maxLon);


    /**
     * 查询所有精英站点
     * @return 所有精英站点的列表
     */
    @Select("SELECT * FROM elite_sites")
    List<EliteSites> findAll();


    // 根据经纬度范围查询精英站点
    List<EliteSites> selectEliteSitesByBounds(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );

    /**
     * 根据 geohash 查询停车区域中心点的经纬度
     * @param geohash 停车区域编号
     * @return 对应的 CenterLatLonResponse 实体，如果找不到则返回 null
     */
    @Select("SELECT center_lat, center_lon FROM elite_sites WHERE geohash = #{geohash}")
    CenterLatLonResponse findCenterLatLonByGeohash(@Param("geohash") String geohash);
}
