package com.example.springboot.service.implementation;

import com.example.springboot.entity.WeatherRecord;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.WeatherRecordMapper;
import com.example.springboot.service.Interface.IWeatherRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List; // 导入List

/**
 * WeatherRecordServiceImpl类空壳
 * 实现IWeatherRecordService接口，包含天气数据业务逻辑的具体实现框架
 */
@Service
public class WeatherRecordServiceImpl implements IWeatherRecordService {
    @Autowired
    private WeatherRecordMapper weatherRecordMapper;

    @Override
    public WeatherRecord getTodayWeatherByGeohash(String geohashArea) {
        LocalDate today = LocalDate.now();
        return weatherRecordMapper.selectByGeohashAndDate(geohashArea, today);
    }

    @Override
    public WeatherRecord getWeatherByGeohashAndDate(String geohashArea, LocalDate recordDate) {
        // 直接调用 Mapper 层的方法，传入 geohashArea 和 recordDate
        return weatherRecordMapper.selectByGeohashAndDate(geohashArea, recordDate);
    }
}