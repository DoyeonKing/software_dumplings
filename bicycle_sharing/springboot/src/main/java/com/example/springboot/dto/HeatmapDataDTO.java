// doyeonking/software_dumplings/software_dumplings-jintaoran/bicycle_sharing/springboot/src/main/java/com/example/springboot/dto/HeatmapDataDTO.java
package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // 确保导入

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDataDTO {
    private String parkingSpot; // 停车点名称或geohash
    private double latitude;    // 纬度
    private double longitude;   // 经度
    private Long weight;        // 预测车辆数量，作为权重
}