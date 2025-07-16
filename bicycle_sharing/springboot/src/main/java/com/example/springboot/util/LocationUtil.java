package com.example.springboot.util;

import com.example.springboot.entity.EliteSites;

import java.math.BigDecimal;

public class LocationUtil {

    private static final BigDecimal EARTH_RADIUS = new BigDecimal("6371000"); // 地球平均半径，单位米

    /**
     * 计算两个经纬度点之间的直线距离（米）。
     * 使用Haversine公式。
     * @param lat1 点1纬度
     * @param lon1 点1经度
     * @param lat2 点2纬度
     * @param lon2 点2经度
     * @return 距离（米）
     */
    public static BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double dLon = Math.toRadians(lon2.subtract(lon1).doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS.multiply(new BigDecimal(c)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 判断给定经纬度是否在某个停车区域的边界框内。
     * @param lat 待检查纬度
     * @param lon 待检查经度
     * @param site EliteSites对象，包含停车区域的边界信息
     * @return 如果在边界框内返回true，否则false
     */
    public static boolean isWithinParkingArea(BigDecimal lat, BigDecimal lon, EliteSites site) {
        // 检查停车区域对象是否有效
        if (site == null) {
            throw new IllegalArgumentException("停车区域对象不能为空。");
        }


        BigDecimal topLeftLat = site.getTopLeftLat();
        BigDecimal topLeftLon = site.getTopLeftLon();
        BigDecimal topRightLat = site.getTopRightLat();
        BigDecimal topRightLon = site.getTopRightLon();
        BigDecimal bottomRightLat = site.getBottomRightLat();
        BigDecimal bottomRightLon = site.getBottomRightLon();
        BigDecimal bottomLeftLat = site.getBottomLeftLat();
        BigDecimal bottomLeftLon = site.getBottomLeftLon();

        // 检查停车区域的边界框信息是否完整
        if (topLeftLat == null || topLeftLon == null || topRightLat == null || topRightLon == null ||
                bottomRightLat == null || bottomRightLon == null || bottomLeftLat == null || bottomLeftLon == null) {
            throw new IllegalArgumentException("停车区域的边界框信息不完整。");
        }

        // 获取停车区域边界的最小/最大经纬度
        BigDecimal minLat = site.getBottomRightLat().min(site.getBottomLeftLat());
        BigDecimal maxLat = site.getTopLeftLat().max(site.getTopRightLat());
        BigDecimal minLon = site.getTopLeftLon().min(site.getBottomLeftLon());
        BigDecimal maxLon = site.getTopRightLon().max(site.getBottomRightLon());



        return lat.compareTo(minLat) >= 0 && lat.compareTo(maxLat) <= 0 &&
                lon.compareTo(minLon) >= 0 && lon.compareTo(maxLon) <= 0;
    }
}
