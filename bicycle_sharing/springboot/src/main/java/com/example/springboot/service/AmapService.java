package com.example.springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AmapService {

    @Value("${amap.key}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 获取地址
    public String getAddress(String location) {
        String url = String.format(
                "https://restapi.amap.com/v3/geocode/regeo?location=%s&key=%s",
                location, amapKey
        );
        String response = restTemplate.getForObject(url, String.class);
        try {
            JsonNode json = objectMapper.readTree(response);
            return json.path("regeocode").path("formatted_address").asText();
        } catch (Exception e) {
            System.err.println("地址解析失败: " + e.getMessage()); // 打印错误日志
            return "地址解析失败";
        }
    }

    // 获取路径规划数据
    public JsonNode getRoute(String origin, String destination) {
        String url = String.format(
                "https://restapi.amap.com/v3/direction/driving?origin=%s&destination=%s&key=%s",
                origin, destination, amapKey
        );
        String response = restTemplate.getForObject(url, String.class);
        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.err.println("路径规划API调用失败: " + e.getMessage()); // 打印错误日志
            return null;
        }
    }

    // 合并逻辑：返回最多指定数量的路线信息
    public Map<String, Object> getRouteWithAddress(String origin, String destination) {
        Map<String, Object> result = new HashMap<>();

        // 获取起点和终点地址
        String originAddress = getAddress(origin);
        String destinationAddress = getAddress(destination);

        Map<String, Object> originInfo = Map.of("location", origin, "address", originAddress);
        Map<String, Object> destinationInfo = Map.of("location", destination, "address", destinationAddress);

        result.put("origin", originInfo);
        result.put("destination", destinationInfo);

        // 获取路线规划数据
        JsonNode routeJson = getRoute(origin, destination);

        // 检查API返回状态
        if (routeJson == null || !"1".equals(routeJson.path("status").asText())) {
            result.put("routes", Map.of("error", "路径规划失败或无可用路线")); // 修改键名为 "routes"
        } else {
            JsonNode paths = routeJson.path("route").path("paths");
            List<Map<String, Object>> selectedRoutes = new ArrayList<>(); // 存储选择的路线
            int routeLimit = 5; // 设置你想要的路线数量限制
            int routesProcessed = 0; // 计数器

            if (paths.isArray()) { // 确保 paths 是一个数组
                for (JsonNode path : paths) {
                    if (routesProcessed >= routeLimit) { // 如果已达到限制数量，则跳出循环
                        break;
                    }

                    Map<String, Object> routeData = new HashMap<>();
                    routeData.put("distance", path.path("distance").asText());
                    routeData.put("duration", path.path("duration").asText());
                    // 包含完整的步骤信息
                    routeData.put("steps", path.path("steps"));

                    // (可选) 如果你还想为每条路线提供一个简化的步骤概述，可以保留这个逻辑
                    // List<Map<String, String>> simplifiedSteps = new ArrayList<>();
                    // int stepSummaryLimit = 5; // 每条路线的步骤概述限制
                    // int currentStepCount = 0;
                    // for (JsonNode step : path.path("steps")) {
                    //     if (currentStepCount >= stepSummaryLimit) {
                    //         break;
                    //     }
                    //     Map<String, String> simplifiedStep = new HashMap<>();
                    //     simplifiedStep.put("instruction", step.path("instruction").asText());
                    //     if (step.has("road")) {
                    //         simplifiedStep.put("road", step.path("road").asText());
                    //     }
                    //     simplifiedStep.put("distance", step.path("distance").asText() + "米");
                    //     simplifiedSteps.add(simplifiedStep);
                    //     currentStepCount++;
                    // }
                    // routeData.put("simplifiedSteps", simplifiedSteps); // 添加简化后的步骤

                    selectedRoutes.add(routeData);
                    routesProcessed++; // 增加已处理路线的数量
                }
            }
            // 将选择的路线列表放入结果Map，键名改为 "routes"
            result.put("routes", selectedRoutes);
        }

        return result;
    }
}
