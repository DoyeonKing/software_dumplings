// src/main/java/com/example/springboot/entity/GeohashInfo.java
package com.example.springboot.entity;

import java.math.BigDecimal;

/**
 * GeohashInfo 实体类
 * 对应数据库中的 'geohash_info' 表
 * 用于存储Geohash区域的详细地理信息
 */
public class GeohashInfo {
    private String geohashArea; // 区域编码，PRIMARY KEY
    private BigDecimal centerLat; // 中心点纬度
    private BigDecimal centerLon; // 中心点经度
    private BigDecimal neLat;     // 东北角纬度
    private BigDecimal neLon;     // 东北角经度
    private BigDecimal nwLat;     // 西北角纬度
    private BigDecimal nwLon;     // 西北角经度
    private BigDecimal swLat;     // 西南角纬度
    private BigDecimal swLon;     // 西南角经度
    private BigDecimal seLat;     // 东南角纬度
    private BigDecimal seLon;     // 东南角经度

    // --- 构造函数 ---
    public GeohashInfo() {}

    public GeohashInfo(String geohashArea, BigDecimal centerLat, BigDecimal centerLon,
                       BigDecimal neLat, BigDecimal neLon, BigDecimal nwLat, BigDecimal nwLon,
                       BigDecimal swLat, BigDecimal swLon, BigDecimal seLat, BigDecimal seLon) {
        this.geohashArea = geohashArea;
        this.centerLat = centerLat;
        this.centerLon = centerLon;
        this.neLat = neLat;
        this.neLon = neLon;
        this.nwLat = nwLat;
        this.nwLon = nwLon;
        this.swLat = swLat;
        this.swLon = swLon;
        this.seLat = seLat;
        this.seLon = seLon;
    }

    // --- Getter 和 Setter 方法 ---
    public String getGeohashArea() {
        return geohashArea;
    }

    public void setGeohashArea(String geohashArea) {
        this.geohashArea = geohashArea;
    }

    public BigDecimal getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(BigDecimal centerLat) {
        this.centerLat = centerLat;
    }

    public BigDecimal getCenterLon() {
        return centerLon;
    }

    public void setCenterLon(BigDecimal centerLon) {
        this.centerLon = centerLon;
    }

    public BigDecimal getNeLat() {
        return neLat;
    }

    public void setNeLat(BigDecimal neLat) {
        this.neLat = neLat;
    }

    public BigDecimal getNeLon() {
        return neLon;
    }

    public void setNeLon(BigDecimal neLon) {
        this.neLon = neLon;
    }

    public BigDecimal getNwLat() {
        return nwLat;
    }

    public void setNwLat(BigDecimal nwLat) {
        this.nwLat = nwLat;
    }

    public BigDecimal getNwLon() {
        return nwLon;
    }

    public void setNwLon(BigDecimal nwLon) {
        this.nwLon = nwLon;
    }

    public BigDecimal getSwLat() {
        return swLat;
    }

    public void setSwLat(BigDecimal swLat) {
        this.swLat = swLat;
    }

    public BigDecimal getSwLon() {
        return swLon;
    }

    public void setSwLon(BigDecimal swLon) {
        this.swLon = swLon;
    }

    public BigDecimal getSeLat() {
        return seLat;
    }

    public void setSeLat(BigDecimal seLat) {
        this.seLat = seLat;
    }

    public BigDecimal getSeLon() {
        return seLon;
    }

    public void setSeLon(BigDecimal seLon) {
        this.seLon = seLon;
    }

    @Override
    public String toString() {
        return "GeohashInfo{" +
               "geohashArea='" + geohashArea + '\'' +
               ", centerLat=" + centerLat +
               ", centerLon=" + centerLon +
               ", neLat=" + neLat +
               ", neLon=" + neLon +
               ", nwLat=" + nwLat +
               ", nwLon=" + nwLon +
               ", swLat=" + swLat +
               ", swLon=" + swLon +
               ", seLat=" + seLat +
               ", seLon=" + seLon +
               '}';
    }
}
