package com.example.springboot.dto;

import java.math.BigDecimal;

/**
 * HeatCell DTO
 * 热力图单元格数据传输对象，包含其坐标和单车数量。
 */
public class HeatCell {
    private BigDecimal latitude;  // 纬度
    private BigDecimal longitude; // 经度
    private int bikeCount;        // 单车数量

    public HeatCell() {
    }

    public HeatCell(BigDecimal latitude, BigDecimal longitude, int bikeCount) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bikeCount = bikeCount;
    }

    // Getter 和 Setter 方法
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public int getBikeCount() {
        return bikeCount;
    }

    public void setBikeCount(int bikeCount) {
        this.bikeCount = bikeCount;
    }

    @Override
    public String toString() {
        return "HeatCell{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", bikeCount=" + bikeCount +
                '}';
    }
}
