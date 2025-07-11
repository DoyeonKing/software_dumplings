package com.example.springboot.service.Interface;

import com.example.springboot.entity.WeatherRecord;
import com.example.springboot.exception.CustomException;
import com.github.pagehelper.PageInfo;

import java.time.LocalDate;
import java.util.List; // 导入List

/**
 * IWeatherRecordService接口空壳
 * 定义天气数据相关的业务操作契约框架
 */
public interface IWeatherRecordService {
    /**
     * 根据 geohash_area 获取当天天气记录。
     * @param geohashArea 地理哈希区域
     * @return 当天的天气记录，如果不存在则返回 null
     */
    WeatherRecord getTodayWeatherByGeohash(String geohashArea); // 保留原有接口

    /**
     * 根据 geohash_area 和指定日期获取天气记录。
     * @param geohashArea 地理哈希区域
     * @param recordDate 指定日期
     * @return 对应的天气记录，如果不存在则返回 null
     */
    WeatherRecord getWeatherByGeohashAndDate(String geohashArea, LocalDate recordDate);
}