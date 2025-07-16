// src/main/java/com/example/springboot/controller/GeohashInfoController.java
package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.NearestParkingAreaResponseDTO;
import com.example.springboot.dto.CenterLatLonResponse;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.service.Interface.IBikesService;
import com.example.springboot.service.Interface.IEliteSitesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * GeohashInfoController类
 * 负责接收和处理与Geohash区域信息相关的HTTP请求
 */
@RestController
@RequestMapping("/elitesites") // 定义这个控制器的基础URL路径
public class EliteSitesController {
    @Resource
    private IEliteSitesService eliteSitesService;

    @Resource
    private IBikesService bikesService;

    /**
     * 根据停车区域编号获取停车区域中心点的纬度和经度
     * @param geohash 停车区域编号
     * @return 包含中心点纬度和经度的 CenterLatLonResponse 实体
     */
    @GetMapping("/centerLatLon")
    public CenterLatLonResponse getCenterLatLonByGeohash(@RequestParam String geohash) {
        return eliteSitesService.findCenterLatLonByGeohash(geohash);
    }

    // 根据经纬度范围查询停车区域信息
    @GetMapping("/parkingAreasByLatLng")
    public List<EliteSites> findByLatLngRange(
            @RequestParam BigDecimal minLat,
            @RequestParam BigDecimal maxLat,
            @RequestParam BigDecimal minLon,
            @RequestParam BigDecimal maxLon) {
        return eliteSitesService.findByLatLngRange(minLat, maxLat, minLon, maxLon);
    }


    /**
     * 根据当前经纬度查找最近的停车区域信息
     * @param latitude 当前纬度
     * @param longitude 当前经度
     * @return Result，data 为最近停车区域的 geohash, centerLat, centerLon, totalParkingCapacity, currentBikeCount, availableSpots
     */
    @GetMapping("/nearest-parking-area")
    public Result getNearestParkingArea(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude) {
        try {
            EliteSites nearestSite = eliteSitesService.getNearestEliteSite(latitude, longitude);

            if (nearestSite == null) {
                return Result.error("404", "未找到任何停车区域或最近的停车区域信息不完整。");
            }

            // 获取该区域的实际自行车数量 (使用您提供的getAllBikeCountByGeohash方法)
            int currentBikes = bikesService.getAllBikeCountByGeohash(nearestSite.getGeohash());
            // 计算可用车位
            Integer availableSpots = nearestSite.getParkingCapacity() - currentBikes;

            NearestParkingAreaResponseDTO responseDTO = new NearestParkingAreaResponseDTO();
            responseDTO.setGeohash(nearestSite.getGeohash());
            responseDTO.setCenterLat(nearestSite.getCenterLat());
            responseDTO.setCenterLon(nearestSite.getCenterLon());
            responseDTO.setTotalParkingCapacity(nearestSite.getParkingCapacity()); // 总容量
            responseDTO.setCurrentBikeCount((long) currentBikes);                     // 当前自行车数量 (转换为Long以匹配DTO)
            responseDTO.setAvailableSpots(availableSpots);                     // 可用停车位

            return Result.success(responseDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取最近停车区域失败: " + e.getMessage());
        }
    }
}
