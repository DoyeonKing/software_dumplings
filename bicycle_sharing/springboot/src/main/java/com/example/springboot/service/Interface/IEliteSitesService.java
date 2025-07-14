// src/main/java/com/example/springboot/service/Interface/IGeohashInfoService.java
package com.example.springboot.service.Interface;

import com.example.springboot.entity.EliteSites;

import java.math.BigDecimal;
import java.util.List;

/**
 * IGeohashInfoService接口
 * 定义Geohash区域信息相关的业务操作契约
 */
public interface IEliteSitesService {

    List<EliteSites> findByLatLngRange(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLon, BigDecimal maxLon);
}
