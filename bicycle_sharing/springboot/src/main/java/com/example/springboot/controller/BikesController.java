package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Bikes;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IBikesService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * BikesController类空壳
 * 负责接收和处理与自行车相关的HTTP请求框架
 */
@RestController // 标记这是一个RESTful控制器
@RequestMapping("/bikes") // 基础URL路径通常与资源名复数形式保持一致
public class BikesController { // 控制器类名与资源名复数形式保持一致

    @Resource // 注入IBikesService接口的实现类
    private IBikesService bikesService; // 注入的Service类型纠正为IBikesService

    /**
     * 获取所有可用（状态为“待使用”）的单车列表
     * Endpoint: GET /bikes/available
     *
     * @return 统一响应结果，包含可用单车列表
     */
    @GetMapping("/available")
    public Result getAllAvailableBikes() {
        try {
            List<Bikes> availableBikes = bikesService.getAllAvailableBikes();
            return Result.success(availableBikes);
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取可用单车失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有单车列表，支持按状态筛选和分页
     * Endpoint: GET /bikes
     *
     * @param pageNum    页码 (可选，默认1)
     * @param pageSize   每页数量 (可选，默认10)
     * @param bikeStatus 单车状态 (可选，例如 "待使用", "使用中")
     * @return 统一响应结果，包含分页后的单车列表
     */
    @GetMapping("/pages")
    public Result getBikesByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String bikeStatus) { // bikeStatus 现在是可选参数
        try {
            PageInfo<Bikes> pageInfo = bikesService.getBikesByPage(pageNum, pageSize, bikeStatus);
            return Result.success(pageInfo);
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取单车列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个单车的详细信息（不显示单车ID）
     * Endpoint: GET /bikes/{bikeId}/details
     *
     * @param bikeId 单车唯一标识符
     * @return 统一响应结果，包含单车详情
     */
    @GetMapping("/{bikeId}/details")
    public Result getBikeDetails(@PathVariable String bikeId) {
        try {
            Bikes bikeDetails = bikesService.getBikeDetails(bikeId);
            if (bikeDetails == null) {
                return Result.error("404", "单车未找到");
            }
            return Result.success(bikeDetails);
        } catch (CustomException e) {
            return Result.error(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取单车详情失败: " + e.getMessage());
        }
    }


    /**
 * 获取指定地理范围内（视口内）的单车列表，支持按状态筛选
 * 这个接口主要用于地图显示，返回的是列表而不是分页信息
 * Endpoint: GET /bikes/viewport
 * @param minLat 最小纬度
 * @param maxLat 最大纬度
 * @param minLon 最小经度
 * @param maxLon 最大经度
 * @param bikeStatus 单车状态 (可选，例如 "待使用")
 * @return 统一响应结果，包含指定范围内的单车列表
 */
@GetMapping("/viewport")
public Result getBikesInViewport(
        @RequestParam BigDecimal minLat,
        @RequestParam BigDecimal maxLat,
        @RequestParam BigDecimal minLon,
        @RequestParam BigDecimal maxLon,
        @RequestParam(required = false) String bikeStatus) {
    try {
        List<Bikes> bikesInViewport = bikesService.getBikesInViewport(minLat, maxLat, minLon, maxLon, bikeStatus);
        return Result.success(bikesInViewport);
    } catch (CustomException e) {
        return Result.error(e.getCode(), e.getMsg());
    } catch (Exception e) {
        e.printStackTrace();
        return Result.error("500", "获取视口内单车失败: " + e.getMessage());
    }
}


}