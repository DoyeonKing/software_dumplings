package com.example.springboot.entity;

import java.math.BigDecimal; // 用于DECIMAL类型
import java.time.LocalDateTime; // 用于DATETIME类型

/**
 * Bikes实体类
 * 对应数据库中的 'bikes' 表
 * 用于表示共享单车的当前状态和位置信息
 */
public class Bikes { // 实体类名纠正为 Bikes
    // 自行车唯一标识符，PRIMARY KEY, NOT NULL
    private String bikeId;

    // 当前纬度，DECIMAL(13, 10)，NOT NULL
    private BigDecimal currentLat;

    // 当前经度，DECIMAL(13, 10)，NOT NULL
    private BigDecimal currentLon;

    // 当前所在区域Geohash，VARCHAR(6)，NOT NULL
    private String currentGeohash;

    // 自行车当前状态，ENUM('使用中', '待使用')，NOT NULL
    // 实际项目中可以考虑更细致的状态，如 '维护中', '故障' 等
    private String bikeStatus;

    // 最后状态更新时间，DATETIME，NOT NULL
    private LocalDateTime lastUpdatedTime;

    // --- 构造函数 ---
    public Bikes() { // 构造函数名与类名一致
    }

    public Bikes(String bikeId, BigDecimal currentLat, BigDecimal currentLon, String currentGeohash, String bikeStatus, LocalDateTime lastUpdatedTime) {
        this.bikeId = bikeId;
        this.currentLat = currentLat;
        this.currentLon = currentLon;
        this.currentGeohash = currentGeohash;
        this.bikeStatus = bikeStatus;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    // --- Getter 和 Setter 方法 ---
    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public BigDecimal getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(BigDecimal currentLat) {
        this.currentLat = currentLat;
    }

    public BigDecimal getCurrentLon() {
        return currentLon;
    }

    public void setCurrentLon(BigDecimal currentLon) {
        this.currentLon = currentLon;
    }

    public String getCurrentGeohash() {
        return currentGeohash;
    }

    public void setCurrentGeohash(String currentGeohash) {
        this.currentGeohash = currentGeohash;
    }

    public String getBikeStatus() {
        return bikeStatus;
    }

    public void setBikeStatus(String bikeStatus) {
        this.bikeStatus = bikeStatus;
    }

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(LocalDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Override
    public String toString() {
        return "Bikes{" +
               "bikeId='" + bikeId + '\'' +
               ", currentLat=" + currentLat +
               ", currentLon=" + currentLon +
               ", currentGeohash='" + currentGeohash + '\'' +
               ", bikeStatus='" + bikeStatus + '\'' +
               ", lastUpdatedTime=" + lastUpdatedTime +
               '}';
    }
}