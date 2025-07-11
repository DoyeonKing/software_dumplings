package com.example.springboot.dto; // 请替换为你的实际包名

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizationResponse {
    private double utilizationRatePercentage; // 使用率百分比，例如 76.0
    private int onlineVehicles;           // 在线车辆总数
    private int inUseVehicles;            // 使用中车辆数
    private int idleVehicles;             // 空闲车辆数
}
