package com.example.springboot.controller;

import com.example.springboot.entity.Orders;
import com.example.springboot.service.Interface.IOrdersService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Resource
    private IOrdersService ordersService;

    /**
     * 获取用户当前的所有未完成骑车记录
     * @param userId 用户ID
     * @return 包含订单列表的响应实体
     */
    @GetMapping("/current-ride")
    public ResponseEntity<Map<String, Object>> getCurrentRideRecord(@RequestParam String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户ID不能为空。"));
        }

        // 获取订单列表
        List<Orders> orders = ordersService.getCurrentRideRecord(userId);

        if (!orders.isEmpty()) {
            return ResponseEntity.ok(createSuccessResponse("获取成功！", orders));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorCurrentRideResponse("未找到未完成的骑行记录"));
        }
    }

    @GetMapping("/rent")
    public ResponseEntity<Map<String, Object>> rentBike(@RequestParam String userId, @RequestParam String bikeId) {
        if (userId == null || userId.isEmpty() || bikeId == null || bikeId.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户ID和单车ID不能为空。"));
        }

        Orders order = ordersService.rentBike(userId, bikeId);

        if (order != null) {
            return ResponseEntity.ok(createSuccessResponse("租借成功！", order));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse("租借失败，请检查单车状态或用户是否有活跃订单。"));
        }
    }

    @GetMapping("/rent-lonlat")
    public ResponseEntity<Map<String, Object>> rentBike(@RequestParam String userId, @RequestParam String bikeId,
                                                        @RequestParam BigDecimal userLat, @RequestParam BigDecimal userLon) {
        if (userId == null || userId.isEmpty() || bikeId == null || bikeId.isEmpty() ||
                userLat == null || userLon == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户ID、单车ID、用户经纬度不能为空。"));
        }

        Orders order = ordersService.rentBikeLonLat(userId, bikeId, userLat, userLon);

        if (order != null) {
            return ResponseEntity.ok(createSuccessResponse("租借成功！", order));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse("租借失败，请检查单车状态、用户是否有活跃订单或所在区域是否匹配。"));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> returnBike(@RequestParam String userId, @RequestParam String bikeId,
                                                          @RequestParam BigDecimal endLat, @RequestParam BigDecimal endLon) {
        if (userId == null || userId.isEmpty() || bikeId == null || bikeId.isEmpty() ||
                endLat == null || endLon == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户ID、单车ID、还车经纬度不能为空。"));
        }

        Orders order = ordersService.returnBike(userId, bikeId, endLat, endLon);

        if (order != null) {
            return ResponseEntity.ok(createSuccessResponse("还车成功！", order));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse("还车失败，请检查还车地点或订单状态。"));
        }
    }

    private Map<String, Object> createSuccessResponse(String message, List<Orders> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message, Orders data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400); // 默认使用400表示客户端请求错误
        response.put("message", message);
        response.put("data", null);
        return response;
    }

    private Map<String, Object> createErrorCurrentRideResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", null);
        return response;
    }


}