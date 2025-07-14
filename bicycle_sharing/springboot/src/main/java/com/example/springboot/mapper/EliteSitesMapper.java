// src/main/java/com/example/springboot/mapper/GeohashInfoMapper.java
package com.example.springboot.mapper;

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

    // 根据经纬度范围查询停车区域信息
    @Select("SELECT * FROM elite_sites " +
            "WHERE center_lat BETWEEN #{minLat} AND #{maxLat} " +
            "AND center_lon BETWEEN #{minLon} AND #{maxLon}")
    List<EliteSites> findByLatLngRange(@Param("minLat") BigDecimal minLat,
                                       @Param("maxLat") BigDecimal maxLat,
                                       @Param("minLon") BigDecimal minLon,
                                       @Param("maxLon") BigDecimal maxLon);
}
