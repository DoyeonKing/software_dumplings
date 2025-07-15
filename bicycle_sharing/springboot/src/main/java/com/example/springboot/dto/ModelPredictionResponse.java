        package com.example.springboot.dto;

        import lombok.Data;

        // 模型预测响应体
        @Data // Lombok注解
        public class ModelPredictionResponse {
            private String geohash; // 预测对应的区域地理哈希值
            private int predictedPickups; // 预测的取车量
            private int predictedDropoffs; // 预测的停库量
            // 如果模型返回其他信息，也可以在这里添加
            // private String modelVersion;
        }
        