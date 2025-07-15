package com.example.springboot.mapper;

import com.example.springboot.entity.DailyDispatchSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
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
}