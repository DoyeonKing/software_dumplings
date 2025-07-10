package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.WeatherRecord;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IWeatherRecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 导入List
import java.util.Map; // 导入Map

/**
 * WeatherRecordController类空壳
 * 负责接收和处理与天气数据相关的HTTP请求框架
 */
@RestController
@RequestMapping("/weatherRecord") // 基础URL路径通常与资源名驼峰形式保持一致
public class WeatherRecordController {

    @Resource
    private IWeatherRecordService weatherRecordService;

    // 空壳：不在此处定义任何方法

}