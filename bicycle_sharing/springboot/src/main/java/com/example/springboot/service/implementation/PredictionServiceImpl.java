package com.example.springboot.service.implementation;

import com.example.springboot.dto.ModelPredictionRequest;
import com.example.springboot.service.Interface.IPredictionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.env.Environment; // 导入 Environment

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// 预测服务实现类
@Service
public class PredictionServiceImpl implements IPredictionService {

    private final RestTemplate restTemplate;
    private final WebClient bikePredictionWebClient;
    private final ObjectMapper objectMapper;
    private final Environment environment; // 注入 Environment

    // 从 application.yml 中读取自行车预测模型API的基础URL
    @Value("${model.api.base-url}")
    private String modelApiBaseUrl;

    public PredictionServiceImpl(RestTemplate restTemplate, WebClient.Builder webClientBuilder, ObjectMapper objectMapper, Environment environment) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.environment = environment; // 初始化 Environment

        // ****** 添加更详细的调试日志 ******
        System.out.println("DEBUG: PredictionServiceImpl 构造函数开始。");
        System.out.println("DEBUG: modelApiBaseUrl (@Value 注入值): " + modelApiBaseUrl);

        // 尝试直接从 Environment 获取属性
        String modelApiBaseUrlFromEnv = environment.getProperty("model.api.base-url");
        System.out.println("DEBUG: model.api.base-url (从 Environment 获取): " + modelApiBaseUrlFromEnv);

        if (modelApiBaseUrl == null || modelApiBaseUrl.trim().isEmpty()) {
            System.err.println("ERROR: @Value 注解未能正确注入 model.api.base-url。尝试使用 Environment 获取的值。");
            // 如果 @Value 注入失败，我们尝试使用 Environment 获取的值来构建 WebClient
            if (modelApiBaseUrlFromEnv != null && !modelApiBaseUrlFromEnv.trim().isEmpty()) {
                this.modelApiBaseUrl = modelApiBaseUrlFromEnv; // 用 Environment 的值覆盖 @Value 的 null
                System.out.println("DEBUG: 已使用 Environment 的值 '" + this.modelApiBaseUrl + "' 覆盖 modelApiBaseUrl。");
            } else {
                System.err.println("CRITICAL ERROR: model.api.base-url 在 Environment 中也未找到或为空。WebClient将连接到默认地址。");
                // 此时，如果 modelApiBaseUrl 仍然为 null，WebClient.builder().baseUrl(null) 会导致连接到 localhost
                // 更好的做法是抛出异常，阻止应用启动，直到配置正确
                // throw new IllegalStateException("Required property 'model.api.base-url' not found in environment.");
            }
        }
        // ************************

        this.bikePredictionWebClient = webClientBuilder.baseUrl(this.modelApiBaseUrl).build();
        System.out.println("DEBUG: bikePredictionWebClient 已使用基础URL: " + this.modelApiBaseUrl + " 构建。");
    }

    @Override
    public Mono<Integer> getPredictedPickupCount(String geohash, LocalDateTime predictionTime) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("geohash", geohash);
        requestBody.put("time", predictionTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return bikePredictionWebClient.post()
                .uri("/predict_pickup_count/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(responseMap -> {
                    Object count = responseMap.get("predicted_pickup_count");
                    if (count instanceof Integer) {
                        return (Integer) count;
                    } else if (count instanceof Number) {
                        return ((Number) count).intValue();
                    }
                    throw new RuntimeException("预测取车量响应格式错误或值为空");
                })
                .onErrorResume(e -> {
                    System.err.println("调用取车量预测API失败: " + e.getMessage());
                    return Mono.error(new RuntimeException("无法获取取车量预测结果", e));
                });
    }

    @Override
    public Mono<Integer> getPredictedParkingCount(String geohash, LocalDateTime predictionTime) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("geohash", geohash);
        requestBody.put("time", predictionTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return bikePredictionWebClient.post()
                .uri("/predict_parking_count/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(responseMap -> {
                    Object count = responseMap.get("predicted_parking_count");
                    if (count instanceof Integer) {
                        return (Integer) count;
                    } else if (count instanceof Number) {
                        return ((Number) count).intValue();
                    }
                    throw new RuntimeException("预测停车量响应格式错误或值为空");
                })
                .onErrorResume(e -> {
                    System.err.println("调用停车量预测API失败: " + e.getMessage());
                    return Mono.error(new RuntimeException("无法获取停车量预测结果", e));
                });
    }

//    @Override
//    public String generateTextWithQwen(String prompt) {
//        System.out.println("通义千问模型调用已禁用。");
//        return "通义千问模型调用已禁用。";
//    }
}
