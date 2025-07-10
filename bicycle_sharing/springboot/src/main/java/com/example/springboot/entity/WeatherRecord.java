package com.example.springboot.entity;

import java.math.BigDecimal;
import java.time.LocalDate; // 用于DATE类型

/**
 * WeatherRecord实体类
 * 对应数据库中的 'weather_record' 表
 * 用于存储特定日期和区域的天气数据
 */
public class WeatherRecord {
    // 天气记录唯一标识符，PRIMARY KEY AUTO_INCREMENT
    private Long weatherRecordId; // 对应BIGINT

    // 天气记录日期，DATE，NOT NULL
    private LocalDate recordDate;

    // 对应区域的Geohash（选定所有区域），VARCHAR(6)，NOT NULL
    private String geohashArea;

    // 最高气温 (℃)，DECIMAL(5, 2)，NULL
    private BigDecimal tempMaxC;

    // 最低气温 (℃)，DECIMAL(5, 2)，NULL
    private BigDecimal tempMinC;

    // 风向，VARCHAR(50)，NULL
    private String windDirection;

    // 风力数字等级，DECIMAL(5, 2)，NULL
    private BigDecimal windLevel;

    // 是否有降水指示器 (0:无, 1:有)，INT，NULL
    private Integer hasPrecipitationTextIndicator;

    // --- 构造函数 ---
    public WeatherRecord() {
    }

    public WeatherRecord(Long weatherRecordId, LocalDate recordDate, String geohashArea, BigDecimal tempMaxC, BigDecimal tempMinC, String windDirection, BigDecimal windLevel, Integer hasPrecipitationTextIndicator) {
        this.weatherRecordId = weatherRecordId;
        this.recordDate = recordDate;
        this.geohashArea = geohashArea;
        this.tempMaxC = tempMaxC;
        this.tempMinC = tempMinC;
        this.windDirection = windDirection;
        this.windLevel = windLevel;
        this.hasPrecipitationTextIndicator = hasPrecipitationTextIndicator;
    }

    // --- Getter 和 Setter 方法 ---
    public Long getWeatherRecordId() {
        return weatherRecordId;
    }

    public void setWeatherRecordId(Long weatherRecordId) {
        this.weatherRecordId = weatherRecordId;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getGeohashArea() {
        return geohashArea;
    }

    public void setGeohashArea(String geohashArea) {
        this.geohashArea = geohashArea;
    }

    public BigDecimal getTempMaxC() {
        return tempMaxC;
    }

    public void setTempMaxC(BigDecimal tempMaxC) {
        this.tempMaxC = tempMaxC;
    }

    public BigDecimal getTempMinC() {
        return tempMinC;
    }

    public void setTempMinC(BigDecimal tempMinC) {
        this.tempMinC = tempMinC;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public BigDecimal getWindLevel() {
        return windLevel;
    }

    public void setWindLevel(BigDecimal windLevel) {
        this.windLevel = windLevel;
    }

    public Integer getHasPrecipitationTextIndicator() {
        return hasPrecipitationTextIndicator;
    }

    public void setHasPrecipitationTextIndicator(Integer hasPrecipitationTextIndicator) {
        this.hasPrecipitationTextIndicator = hasPrecipitationTextIndicator;
    }

    @Override
    public String toString() {
        return "WeatherRecord{" +
               "weatherRecordId=" + weatherRecordId +
               ", recordDate=" + recordDate +
               ", geohashArea='" + geohashArea + '\'' +
               ", tempMaxC=" + tempMaxC +
               ", tempMinC=" + tempMinC +
               ", windDirection='" + windDirection + '\'' +
               ", windLevel=" + windLevel +
               ", hasPrecipitationTextIndicator=" + hasPrecipitationTextIndicator +
               '}';
    }
}