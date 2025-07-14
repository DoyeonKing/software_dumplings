// src/main/java/com/example/springboot/entity/GeohashInfo.java
package com.example.springboot.entity;

import java.math.BigDecimal;

/**
 * GeohashInfo 实体类
 * 对应数据库中的 'elite_sites' 表
 * 用于存储Geohash区域的详细地理信息
 */
public class EliteSites {
    private String geohash; // 区域编码，PRIMARY KEY
    private BigDecimal centerLat; // 中心点纬度
    private BigDecimal centerLon; // 中心点经度
    private BigDecimal topLeftLat;     // 东北角纬度
    private BigDecimal topLeftLon;     // 东北角经度
    private BigDecimal topRightLat;     // 西北角纬度
    private BigDecimal topRightLon;     // 西北角经度
    private BigDecimal bottomRightLat;     // 西南角纬度
    private BigDecimal bottomRightLon;     // 西南角经度
    private BigDecimal bottomLeftLat;     // 东南角纬度
    private BigDecimal bottomLeftLon;     // 东南角经度
    private Integer regionGroupId; // 区域组ID
    private Integer parkingCapacity; // 精英站点ID

    public Integer getParkingCapacity() {
        return parkingCapacity;
    }

    public void setParkingCapacity(Integer parkingCapacity) {
        this.parkingCapacity = parkingCapacity;
    }

    public Integer getRegionGroupId() {
        return regionGroupId;
    }

    public void setRegionGroupId(Integer regionGroupId) {
        this.regionGroupId = regionGroupId;
    }


    // --- 构造函数 ---
    public EliteSites() {}

    public EliteSites(String geohash, BigDecimal centerLat, BigDecimal centerLon,
                      BigDecimal topLeftLat, BigDecimal topLeftLon, BigDecimal topRightLat, BigDecimal topRightLon,
                      BigDecimal bottomRightLat, BigDecimal bottomRightLon, BigDecimal bottomLeftLat, BigDecimal bottomLeftLon) {
        this.geohash = geohash;
        this.centerLat = centerLat;
        this.centerLon = centerLon;
        this.topLeftLat = topLeftLat;
        this.topLeftLon = topLeftLon;
        this.topRightLat = topRightLat;
        this.topRightLon = topRightLon;
        this.bottomRightLat = bottomRightLat;
        this.bottomRightLon = bottomRightLon;
        this.bottomLeftLat = bottomLeftLat;
        this.bottomLeftLon = bottomLeftLon;
    }

    // --- Getter 和 Setter 方法 ---
    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
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

    public BigDecimal getTopLeftLat() {
        return topLeftLat;
    }

    public void setTopLeftLat(BigDecimal topLeftLat) {
        this.topLeftLat = topLeftLat;
    }

    public BigDecimal getTopLeftLon() {
        return topLeftLon;
    }

    public void setTopLeftLon(BigDecimal topLeftLon) {
        this.topLeftLon = topLeftLon;
    }

    public BigDecimal getTopRightLat() {
        return topRightLat;
    }

    public void setTopRightLat(BigDecimal topRightLat) {
        this.topRightLat = topRightLat;
    }

    public BigDecimal getTopRightLon() {
        return topRightLon;
    }

    public void setTopRightLon(BigDecimal topRightLon) {
        this.topRightLon = topRightLon;
    }

    public BigDecimal getBottomRightLat() {
        return bottomRightLat;
    }

    public void setBottomRightLat(BigDecimal bottomRightLat) {
        this.bottomRightLat = bottomRightLat;
    }

    public BigDecimal getBottomRightLon() {
        return bottomRightLon;
    }

    public void setBottomRightLon(BigDecimal bottomRightLon) {
        this.bottomRightLon = bottomRightLon;
    }

    public BigDecimal getBottomLeftLat() {
        return bottomLeftLat;
    }

    public void setBottomLeftLat(BigDecimal bottomLeftLat) {
        this.bottomLeftLat = bottomLeftLat;
    }

    public BigDecimal getBottomLeftLon() {
        return bottomLeftLon;
    }

    public void setBottomLeftLon(BigDecimal bottomLeftLon) {
        this.bottomLeftLon = bottomLeftLon;
    }

    @Override
    public String toString() {
        return "GeohashInfo{" +
               "geohash='" + geohash + '\'' +
               ", centerLat=" + centerLat +
               ", centerLon=" + centerLon +
               ", neLat=" + topLeftLat +
               ", neLon=" + topLeftLon +
               ", nwLat=" + topRightLat +
               ", nwLon=" + topRightLon +
               ", swLat=" + bottomRightLat +
               ", swLon=" + bottomRightLon +
               ", seLat=" + bottomLeftLat +
               ", seLon=" + bottomLeftLon +
               '}';
    }
}
