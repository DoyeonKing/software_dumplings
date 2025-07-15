package com.example.springboot.controller;

import com.example.springboot.entity.Orders;
import com.example.springboot.service.Interface.IOrdersService; // 导入Service接口
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * TripsController类空壳
 * 负责接收和处理与用户行程记录相关的HTTP请求框架
 */
@RestController // 标记这是一个RESTful控制器
@RequestMapping("/orders") // 基础URL路径通常与资源名复数形式保持一致
public class OrdersController { // 控制器类名命名为 TripsController

    @Resource // 注入ITripsService接口的实现类
    private IOrdersService ordersService; // 注入的Service类型命名为 ITripsService

    /**
     * 用车接口。
     * 接收用户ID和单车ID，处理租借逻辑。
     * GET /orders/rent?userId=xxx&bikeId=yyy
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @return ResponseEntity 包含订单信息或错误消息
     */
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

    /**
     * 还车接口。
     * 接收用户ID、单车ID、还车经纬度，处理归还逻辑。
     * GET /orders/return?userId=xxx&bikeId=yyy&endLat=zzz&endLon=aaa
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @param endLat 还车纬度
     * @param endLon 还车经度
     * @return ResponseEntity 包含完成的订单信息或错误消息
     */
    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> returnBike(@RequestParam String userId, @RequestParam String bikeId,
                                                          @RequestParam double endLat, @RequestParam double endLon) {
        if (userId == null || userId.isEmpty() || bikeId == null || bikeId.isEmpty() ||
                endLat == 0 || endLon == 0) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户ID、单车ID、还车经纬度不能为空。"));
        }

        Orders order = ordersService.returnBike(userId, bikeId, endLat, endLon);

        if (order != null) {
            return ResponseEntity.ok(createSuccessResponse("还车成功！", order));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse("还车失败，请检查还车地点或订单状态。"));
        }
    }

    // 辅助方法，用于创建统一的成功响应格式
    private Map<String, Object> createSuccessResponse(String message, Orders data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    // 辅助方法，用于创建统一的错误响应格式
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400); // 默认使用400表示客户端请求错误
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}