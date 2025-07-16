package com.example.springboot.mapper;

import com.example.springboot.entity.DailyDispatchSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DailyDispatchSuggestionMapper {
    // 批量插入每日调度建议数据
    int insertBatch(List<DailyDispatchSuggestion> suggestions);

    // 根据日期和地图范围查询调度建议数据
    List<DailyDispatchSuggestion> selectSuggestionsByDateAndBounds(
            @Param("reportDate") LocalDate reportDate,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );

    // 根据日期删除已有建议 (用于重新生成)
    int deleteByDate(@Param("reportDate") LocalDate reportDate);


    /**
     * 查询所有调度建议
     * @return 所有DailyDispatchSuggestion对象的列表
     */
    @Select("SELECT id, report_date, prediction_target_time, source_geohash, source_latitude, source_longitude, target_geohash, target_latitude, target_longitude, suggested_bike_count, suggestion_time, created_at, suggestion_status FROM daily_dispatch_suggestions")
    List<DailyDispatchSuggestion> selectAllSuggestions();

    /**
     * 更新指定ID的调度建议的状态
     * @param suggestionId 调度建议ID
     * @param newStatus 新的状态
     */
    @Update("UPDATE daily_dispatch_suggestions SET suggestion_status = #{newStatus} WHERE id = #{suggestionId}")
    void updateStatus(@Param("suggestionId") Integer suggestionId, @Param("newStatus") String newStatus);

}