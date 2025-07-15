package com.example.springboot.util;

import java.math.BigDecimal;

public class LocationUtil {

    private static final double EARTH_RADIUS = 6371000; // 地球平均半径，单位米

    /**
     * 计算两个经纬度点之间的直线距离（米）。
     * 使用Haversine公式。
     * @param lat1 点1纬度
     * @param lon1 点1经度
     * @param lat2 点2纬度
     * @param lon2 点2经度
     * @return 距离（米）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * 判断给定经纬度是否在某个Geohash区域的边界框内。
     * 注意：这只是一个简单的边界框检查，Geohash的形状并非严格的矩形。
     * 更精确的判断需要解码Geohash的边界。
     * @param lat 待检查纬度
     * @param lon 待检查经度
     * @param site EliteSites对象，包含Geohash的边界信息
     * @return 如果在边界框内返回true，否则false
     */
    public static boolean isWithinGeohashBounds(double lat, double lon, com.example.springboot.entity.EliteSites site) {
        if (site == null || site.getTopLeftLat() == null || site.getTopLeftLon() == null ||
                site.getBottomRightLat() == null || site.getBottomRightLon() == null) {
            return false;
        }

        // 获取Geohash边界的四个角，并确定其最小/最大经纬度
        BigDecimal minLat = site.getBottomRightLat().min(site.getBottomLeftLat());
        BigDecimal maxLat = site.getTopLeftLat().max(site.getTopRightLat());
        BigDecimal minLon = site.getTopLeftLon().min(site.getBottomLeftLon());
        BigDecimal maxLon = site.getTopRightLon().max(site.getBottomRightLon());

        return lat >= minLat.doubleValue() && lat <= maxLat.doubleValue() &&
                lon >= minLon.doubleValue() && lon <= maxLon.doubleValue();
    }
}
