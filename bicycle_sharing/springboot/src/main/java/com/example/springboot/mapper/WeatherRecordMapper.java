package com.example.springboot.mapper;

import com.example.springboot.entity.WeatherRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List; // 导入必要的类

/**
 * WeatherRecordMapper接口空壳
 * 用于定义对数据库 'weather_record' 表的操作方法框架
 */
@Mapper // 标记这是一个MyBatis Mapper接口
public interface WeatherRecordMapper {
    /**
     * 根据 geohash_area 和指定日期查询天气记录。
     * @param geohashArea 地理哈希区域
     * @param recordDate 记录日期
     * @return 对应的 WeatherRecord 对象，如果不存在则返回 null
     */
    @Select("SELECT weather_record_id, record_date, geohash_area, temp_max_c, temp_min_c, " +
            "wind_direction, wind_level, has_precipitation_text_indicator " +
            "FROM weather_record " +
            "WHERE geohash_area = #{geohashArea} AND record_date = #{recordDate}")
    WeatherRecord selectByGeohashAndDate(@Param("geohashArea") String geohashArea,
                                         @Param("recordDate") LocalDate recordDate);


}