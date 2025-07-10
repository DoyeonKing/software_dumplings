// src/main/java/com/example/springboot/controller/GeohashInfoController.java
package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.GeohashInfo;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.Interface.IGeohashInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GeohashInfoController类
 * 负责接收和处理与Geohash区域信息相关的HTTP请求
 */
@RestController
@RequestMapping("/geohashInfo") // 定义这个控制器的基础URL路径
public class GeohashInfoController {

    @Resource
    private IGeohashInfoService geohashInfoService;

}
