// src/main/java/com/example/springboot/mapper/GeohashInfoMapper.java
package com.example.springboot.mapper;

import com.example.springboot.entity.EliteSites;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * GeohashInfoMapper接口
 * 用于定义对数据库 'geohash_info' 表的操作方法
 */
@Mapper
public interface EliteSitesMapper {

    /**
     * 根据 geohashArea 查询停车点信息。
     * 【作用】：用于在调度时，通过目标 geohash 查找对应的精确经纬度。
     * @param geohashArea 停车点的地理哈希信息。
     * @return 对应的 EliteSites 实体，如果找不到则返回 null。
     */
    EliteSites findByGeohashInfo(@Param("geohashArea") String geohashArea);

}
