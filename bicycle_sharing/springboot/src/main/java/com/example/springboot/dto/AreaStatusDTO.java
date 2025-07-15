package com.example.springboot.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AreaStatusDTO {
    private String geohash;
    private double latitude;
    private double longitude;
    private String status; // 例如："稀缺区", "富余区", "稳定区"
    private double utilizationRate; // 利用率
    private Long currentBikes; // 当前车辆数
    private Integer parkingCapacity; // 停车容量
    private Integer predictedPickups; // 预测取车量
    private Integer predictedDropoffs; // 预测停车量
    private Long futureBikes; // 预测未来车辆数
    private Long futureRemainingSpots; // 预测未来剩余车位
    private LocalDateTime predictionTime; // 预测的目标时间点
}