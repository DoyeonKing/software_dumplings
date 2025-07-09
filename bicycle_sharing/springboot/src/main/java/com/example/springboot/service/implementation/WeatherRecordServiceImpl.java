package com.example.springboot.service.implementation;

import com.example.springboot.entity.WeatherRecord;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.WeatherRecordMapper;
import com.example.springboot.service.Interface.IWeatherRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List; // 导入List

/**
 * WeatherRecordServiceImpl类空壳
 * 实现IWeatherRecordService接口，包含天气数据业务逻辑的具体实现框架
 */
@Service
public class WeatherRecordServiceImpl implements IWeatherRecordService {

    @Resource
    private WeatherRecordMapper weatherRecordMapper;

    // 空壳：不在此处定义任何方法实现
}