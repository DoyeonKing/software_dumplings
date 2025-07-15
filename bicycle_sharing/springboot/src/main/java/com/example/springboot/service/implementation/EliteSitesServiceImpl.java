// src/main/java/com/example/springboot/service/implementation/GeohashInfoServiceImpl.java
package com.example.springboot.service.implementation;

import com.example.springboot.dto.CenterLatLonResponse;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.mapper.EliteSitesMapper;
import com.example.springboot.service.Interface.IEliteSitesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * GeohashInfoServiceImpl类
 * 实现IGeohashInfoService接口，包含Geohash区域信息业务逻辑的具体实现
 */
@Service
public class EliteSitesServiceImpl implements IEliteSitesService {

    @Autowired
    private EliteSitesMapper eliteSitesMapper;

    @Override
    public CenterLatLonResponse findCenterLatLonByGeohash(String geohash) {
        return eliteSitesMapper.findCenterLatLonByGeohash(geohash);
    }

    @Override
    public List<EliteSites> findByLatLngRange(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLon, BigDecimal maxLon) {
        return eliteSitesMapper.findByLatLngRange(minLat, maxLat, minLon, maxLon);
    }

     @Override
    public List<EliteSites> getAllEliteSites() {
        // 调用Mapper获取所有精英站点数据
        return eliteSitesMapper.findAll(); // 假设您的EliteSitesMapper有一个findAll方法
    }

    @Override
    public List<EliteSites> getEliteSitesInBounds(double minLat, double maxLat, double minLon, double maxLon) {
        return eliteSitesMapper.selectEliteSitesByBounds(minLat, maxLat, minLon, maxLon);
    }
}
