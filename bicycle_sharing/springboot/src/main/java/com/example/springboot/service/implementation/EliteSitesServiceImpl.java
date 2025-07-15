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


     /**
     * 根据当前经纬度查找最近的精英站点
     * @param currentLat 当前纬度
     * @param currentLon 当前经度
     * @return 最近的EliteSites对象，如果没有找到则返回null
     */
    @Override
    public EliteSites getNearestEliteSite(BigDecimal currentLat, BigDecimal currentLon) {
        List<EliteSites> allSites = eliteSitesMapper.findAll(); // 获取所有精英站点
        if (allSites == null || allSites.isEmpty()) {
            return null;
        }

        EliteSites nearestSite = null;
        double minDistance = Double.MAX_VALUE;

        double lat1 = currentLat.doubleValue();
        double lon1 = currentLon.doubleValue();

        for (EliteSites site : allSites) {
            // 确保站点有中心点经纬度信息和停车容量
            if (site.getCenterLat() == null || site.getCenterLon() == null || site.getParkingCapacity() == null) {
                continue;
            }

            double lat2 = site.getCenterLat().doubleValue();
            double lon2 = site.getCenterLon().doubleValue();

            double distance = haversineDistance(lat1, lon1, lat2, lon2);

            if (distance < minDistance) {
                minDistance = distance;
                nearestSite = site;
            }
        }

        // 这里不计算自行车数量和可用车位，因为Service层返回的是EliteSites实体，
        // 这些计算应该放在Controller层或单独的DTO转换逻辑中，以保持EliteSitesService的职责单一。
        return nearestSite;
    }

    /**
     * 哈弗赛因距离计算（地球表面两点距离，单位：米）
     * 这是一个辅助方法，用于计算两个经纬度点之间的距离。
     * @param lat1 点1纬度
     * @param lon1 点1经度
     * @param lat2 点2纬度
     * @param lon2 点2经度
     * @return 两点之间的距离（米）
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
