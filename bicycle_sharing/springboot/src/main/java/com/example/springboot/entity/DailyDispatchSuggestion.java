package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实体类：每日调度建议数据
 * 对应数据库中的 'daily_dispatch_suggestions' 表
 */
@Data
public class DailyDispatchSuggestion {

    private Long id;
    private LocalDate reportDate; // 报告所属日期
    private LocalDateTime predictionTargetTime; // 预测的目标时间点
    private String sourceGeohash; // 建议调出区域geohash
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private String targetGeohash; // 建议调入区域geohash
    private BigDecimal targetLatitude;
    private BigDecimal targetLongitude;
    private Integer suggestedBikeCount; // 建议调配数量
    private LocalDateTime suggestionTime; // 建议生成时间
    private LocalDateTime createdAt; // 记录创建时间
    private String suggestionStatus; // 【新增字段】建议状态：PENDING, ADOPTED, REJECTED

    public String getSuggestionStatus() {
        return suggestionStatus;
    }

    public void setSuggestionStatus(String suggestionStatus) {
        this.suggestionStatus = suggestionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSourceGeohash() {
        return sourceGeohash;
    }

    public void setSourceGeohash(String sourceGeohash) {
        this.sourceGeohash = sourceGeohash;
    }

    public BigDecimal getSourceLatitude() {
        return sourceLatitude;
    }

    public void setSourceLatitude(BigDecimal sourceLatitude) {
        this.sourceLatitude = sourceLatitude;
    }

    public BigDecimal getSourceLongitude() {
        return sourceLongitude;
    }

    public void setSourceLongitude(BigDecimal sourceLongitude) {
        this.sourceLongitude = sourceLongitude;
    }

    public Integer getSuggestedBikeCount() {
        return suggestedBikeCount;
    }

    public void setSuggestedBikeCount(Integer suggestedBikeCount) {
        this.suggestedBikeCount = suggestedBikeCount;
    }

    public LocalDateTime getSuggestionTime() {
        return suggestionTime;
    }

    public void setSuggestionTime(LocalDateTime suggestionTime) {
        this.suggestionTime = suggestionTime;
    }

    public String getTargetGeohash() {
        return targetGeohash;
    }

    public void setTargetGeohash(String targetGeohash) {
        this.targetGeohash = targetGeohash;
    }

    public BigDecimal getTargetLatitude() {
        return targetLatitude;
    }

    public void setTargetLatitude(BigDecimal targetLatitude) {
        this.targetLatitude = targetLatitude;
    }

    public BigDecimal getTargetLongitude() {
        return targetLongitude;
    }

    public void setTargetLongitude(BigDecimal targetLongitude) {
        this.targetLongitude = targetLongitude;
    }


}