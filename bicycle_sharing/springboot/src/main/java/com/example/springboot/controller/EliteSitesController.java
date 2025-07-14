// src/main/java/com/example/springboot/controller/GeohashInfoController.java
package com.example.springboot.controller;

import com.example.springboot.entity.EliteSites;
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
@RequestMapping("/geohashInfo") // 定义这个控制器的基础URL路径
public class EliteSitesController {

    @Resource
    private IEliteSitesService geohashInfoService;


    @Resource
    private IEliteSitesService eliteSitesService;

    // 根据经纬度范围查询停车区域信息
    @GetMapping("/parkingAreasByLatLng")
    public List<EliteSites> findByLatLngRange(
            @RequestParam BigDecimal minLat,
            @RequestParam BigDecimal maxLat,
            @RequestParam BigDecimal minLon,
            @RequestParam BigDecimal maxLon) {
        return eliteSitesService.findByLatLngRange(minLat, maxLat, minLon, maxLon);
    }
}
