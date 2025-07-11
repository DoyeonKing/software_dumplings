package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.WeatherRecord;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IWeatherRecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List; // 导入List
import java.util.Map; // 导入Map

/**
 * WeatherRecordController类空壳
 * 负责接收和处理与天气数据相关的HTTP请求框架
 */
@RestController
@RequestMapping("/weatherRecord") // 基础URL路径通常与资源名驼峰形式保持一致
public class WeatherRecordController {
    @Autowired
    private IWeatherRecordService weatherRecordService;
    /**
     * 根据 geohash_area 和指定日期获取天气信息。
     * URL: GET /weather/by-geohash-date?geohashArea={geohash_value}&recordDate={YYYY-MM-DD}
     *
     * @param geohashArea 地理哈希区域，例如 "wx4g0d"
     * @param recordDate 指定的日期，格式为 YYYY-MM-DD
     * @return ResponseEntity 包含 WeatherRecord 对象或错误信息
     */
    @GetMapping("/by-geohash-date") // 新的接口路径
    public ResponseEntity<WeatherRecord> getWeatherByGeohashAndDate(
            @RequestParam("geohashArea") String geohashArea,
            @RequestParam("recordDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate) {
        // 1. 验证输入参数
        if (geohashArea == null || geohashArea.trim().isEmpty() || recordDate == null) {
            return ResponseEntity.badRequest().build(); // 返回 400 Bad Request
        }

        // 2. 调用 Service 层方法获取天气记录
        WeatherRecord weather = weatherRecordService.getWeatherByGeohashAndDate(geohashArea, recordDate);

        // 3. 根据查询结果返回不同的 HTTP 状态码和数据
        if (weather != null) {
            return ResponseEntity.ok(weather); // 找到记录，返回 200 OK 和天气数据
        } else {
            // 没有找到匹配指定 geohash_area 和日期的天气记录
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 返回 404 Not Found
        }
    }

}