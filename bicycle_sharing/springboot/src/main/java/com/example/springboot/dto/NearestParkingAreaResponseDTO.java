package com.example.springboot.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 最近停车区域响应DTO
 * 用于返回最近停车区域的关键信息，包括总容量、当前自行车数量和可用车位
 */
@Data
public class NearestParkingAreaResponseDTO {
    private String geohash;
    private BigDecimal centerLat;
    private BigDecimal centerLon;
    private Integer totalParkingCapacity; // 【修改】总停车容量
    private Long currentBikeCount;        // 【新增】该区域实际自行车数量
    private Integer availableSpots;       // 【新增】该区域可用停车位数量 (totalParkingCapacity - currentBikeCount)

    // 如果您没有使用Lombok，请手动添加 Getter/Setter
}