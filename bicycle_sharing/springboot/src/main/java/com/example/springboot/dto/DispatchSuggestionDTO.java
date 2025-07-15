package com.example.springboot.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DispatchSuggestionDTO {
    private String sourceGeohash; // 建议从哪里调出
    private double sourceLatitude;
    private double sourceLongitude;
    private String targetGeohash; // 建议调配到哪里
    private double targetLatitude;
    private double targetLongitude;
    private int suggestedBikeCount; // 建议调配的自行车数量
    private LocalDateTime suggestionTime; // 建议生成的时间
    private LocalDateTime predictionTargetTime; // 这条建议是基于哪个预测时间点
}