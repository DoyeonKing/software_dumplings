package com.example.springboot.dto;

import lombok.Data;

// 模型预测请求体
@Data // Lombok注解，自动生成getter, setter, toString, equals, hashCode方法
public class ModelPredictionRequest {
    private String geohash; // 目标区域的 GeoHash 编码
    private String time;      // 预测时间点，严格遵循 ISO 8601 格式 (例如 "2020-01-01T08:00:00")
}
