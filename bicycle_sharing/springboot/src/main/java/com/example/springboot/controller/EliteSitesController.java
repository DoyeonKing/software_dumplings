// src/main/java/com/example/springboot/controller/GeohashInfoController.java
package com.example.springboot.controller;

import com.example.springboot.dto.CenterLatLonResponse;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.service.Interface.IEliteSitesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.example.springboot.util.LocationUtil.isWithinParkingArea;

/**
 * GeohashInfoController类
 * 负责接收和处理与Geohash区域信息相关的HTTP请求
 */
@RestController
@RequestMapping("/elitesites") // 定义这个控制器的基础URL路径
public class EliteSitesController {
    @Resource
    private IEliteSitesService eliteSitesService;

    private static final String NOT_IN_ANY_PARKING_POINT = "xxxxxxx";

    /**
     * 判断当前位置所处的停车点编号
     * @param lat 纬度
     * @param lon 经度
     * @return 当前位置所处的停车点编号，若不处于任何一个停车点，返回固定编号 "xxxxxxx"
     */
    @GetMapping("/checkParkingPoint")
    public String checkParkingPoint(@RequestParam BigDecimal lat, @RequestParam BigDecimal lon) {
        List<EliteSites> allEliteSites = eliteSitesService.getAllEliteSites();
        for (EliteSites site : allEliteSites) {
            if (isWithinParkingArea(lat, lon, site)) {
                return site.getGeohash();
            }
        }
        return NOT_IN_ANY_PARKING_POINT;
    }

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
}
