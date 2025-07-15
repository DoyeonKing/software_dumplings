package com.example.springboot.util;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class GeohashUtil {

    /**
     * 将经纬度转换为指定精度的Geohash编码。
     * 精度通常为6位，覆盖约0.6km x 0.6km的区域。
     * @param latitude 纬度
     * @param longitude 经度
     * @param precision Geohash精度 (通常为6)
     * @return Geohash字符串
     */
    public static String encode(double latitude, double longitude, int precision) {
        // 检查坐标值是否在有效范围内
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude is out of range. It should be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude is out of range. It should be between -180 and 180.");
        }

        // 使用withCharacterPrecision方法生成GeoHash编码字符串
        return GeoHash.withCharacterPrecision(latitude, longitude, precision).toBase32();
    }

    /**
     * 根据Geohash字符串获取其中心点的纬度。
     * @param geohash Geohash字符串
     * @return 中心点纬度
     */
    public static double decodeLatitude(String geohash) {
        BoundingBox bbox = GeoHash.fromGeohashString(geohash).getBoundingBox();
        return (bbox.getNorthLatitude() + bbox.getSouthLatitude()) / 2;
    }

    /**
     * 根据Geohash字符串获取其中心点的经度。
     * @param geohash Geohash字符串
     * @return 中心点经度
     */
    public static double decodeLongitude(String geohash) {
        BoundingBox bbox = GeoHash.fromGeohashString(geohash).getBoundingBox();
        return (bbox.getEastLongitude() + bbox.getWestLongitude()) / 2;
    }
}
