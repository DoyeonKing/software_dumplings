package com.example.springboot.service.Interface;

import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

// 预测服务接口
public interface IPredictionService {

    /**
     * 调用取车量预测API
     * @param geohash 区域的 GeoHash
     * @param predictionTime 预测时间点
     * @return 预测的取车数量的Mono
     */
    Mono<Integer> getPredictedPickupCount(String geohash, LocalDateTime predictionTime);

    /**
     * 调用停车量预测API
     * @param geohash 区域的 GeoHash
     * @param predictionTime 预测时间点
     * @return 预测的停车数量的Mono
     */
    Mono<Integer> getPredictedParkingCount(String geohash, LocalDateTime predictionTime);

    // 移除通义千问模型调用方法
    // String generateTextWithQwen(String prompt);
}
