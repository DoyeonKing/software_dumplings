package com.example.springboot.dto;

import lombok.Data; // 确保您的pom.xml中已引入lombok依赖

// 用于接收前端传递的地图经纬度范围
@Data
public class MapBoundsRequest {
    private double minLat; // 地图最小纬度
    private double maxLat; // 地图最大纬度
    private double minLon; // 地图最小经度
    private double maxLon; // 地图最大经度
    // 您也可以根据需要在这里添加预测时间点等其他参数
    // private LocalDateTime predictionTime;
}