package com.example.springboot.entity;

import lombok.Data; // 确保您的pom.xml中已引入lombok依赖

import java.math.BigDecimal; // 用于精确处理经纬度和小数
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实体类：每日模拟报告数据
 * 对应数据库中的 'daily_simulation_reports' 表
 */
@Data
public class DailySimulationReport {
    private Long id;
    private LocalDate reportDate; // 报告所属日期
    private LocalDateTime predictionTargetTime; // 预测的目标时间点
    private String geohash; // 区域geohash
    private BigDecimal latitude; // 纬度
    private BigDecimal longitude; // 经度
    private String status; // 区域状态
    private BigDecimal utilizationRate; // 利用率
    private Long currentBikes; // 当前车辆数
    private Integer parkingCapacity; // 停车容量
    private Integer predictedPickups; // 预测取车量
    private Integer predictedDropoffs; // 预测停车量
    private Long futureBikes; // 未来车辆数
    private Long futureRemainingSpots; // 未来剩余车位
    private LocalDateTime createdAt; // 记录创建时间

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCurrentBikes() {
        return currentBikes;
    }

    public void setCurrentBikes(Long currentBikes) {
        this.currentBikes = currentBikes;
    }

    public Long getFutureBikes() {
        return futureBikes;
    }

    public void setFutureBikes(Long futureBikes) {
        this.futureBikes = futureBikes;
    }

    public Long getFutureRemainingSpots() {
        return futureRemainingSpots;
    }

    public void setFutureRemainingSpots(Long futureRemainingSpots) {
        this.futureRemainingSpots = futureRemainingSpots;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getParkingCapacity() {
        return parkingCapacity;
    }

    public void setParkingCapacity(Integer parkingCapacity) {
        this.parkingCapacity = parkingCapacity;
    }

    public Integer getPredictedDropoffs() {
        return predictedDropoffs;
    }

    public void setPredictedDropoffs(Integer predictedDropoffs) {
        this.predictedDropoffs = predictedDropoffs;
    }

    public Integer getPredictedPickups() {
        return predictedPickups;
    }

    public void setPredictedPickups(Integer predictedPickups) {
        this.predictedPickups = predictedPickups;
    }

    public LocalDateTime getPredictionTargetTime() {
        return predictionTargetTime;
    }

    public void setPredictionTargetTime(LocalDateTime predictionTargetTime) {
        this.predictionTargetTime = predictionTargetTime;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(BigDecimal utilizationRate) {
        this.utilizationRate = utilizationRate;
    }
}