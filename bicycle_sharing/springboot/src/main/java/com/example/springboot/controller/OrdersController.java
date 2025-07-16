package com.example.springboot.controller;

import com.example.springboot.entity.Orders;
import com.example.springboot.service.Interface.IOrdersService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Resource
    private IOrdersService ordersService;

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
}