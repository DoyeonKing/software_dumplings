// src/main/java/com/example/springboot/service/Interface/IGeohashInfoService.java
package com.example.springboot.service.Interface;

import com.example.springboot.dto.CenterLatLonResponse;
import com.example.springboot.entity.EliteSites;

import java.math.BigDecimal;
import java.util.List;

/**
 * IGeohashInfoService接口
 * 定义Geohash区域信息相关的业务操作契约
 */
public interface IEliteSitesService {

    List<EliteSites> findByLatLngRange(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLon, BigDecimal maxLon);

    /**
     * 根据 geohash 查询停车区域中心点的经纬度
     * @param geohash 停车区域编号
     * @return 对应的 CenterLatLonResponse 实体，如果找不到则返回 null
     */
    CenterLatLonResponse findCenterLatLonByGeohash(String geohash);

    /**
     * 获取所有精英站点信息
     * @return 所有精英站点的列表
     */
    List<EliteSites> getAllEliteSites();

    //根据经纬度范围获取精英站点
    List<EliteSites> getEliteSitesInBounds(double minLat, double maxLat, double minLon, double maxLon);

    EliteSites getNearestEliteSite(BigDecimal currentLat, BigDecimal currentLon);
    EliteSites getEliteSiteByGeohash(String geohash);

}
