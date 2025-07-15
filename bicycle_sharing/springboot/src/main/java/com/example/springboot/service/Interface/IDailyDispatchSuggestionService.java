package com.example.springboot.service.Interface;

import com.example.springboot.entity.DailyDispatchSuggestion;
import java.time.LocalDate;
import java.util.List;

public interface IDailyDispatchSuggestionService {
    void saveDailySuggestions(List<DailyDispatchSuggestion> suggestions);
    List<DailyDispatchSuggestion> getSuggestionsByDateAndBounds(LocalDate reportDate, double minLat, double maxLat, double minLon, double maxLon);
    void deleteSuggestionsByDate(LocalDate reportDate);

    /**
     * 获取所有调度建议信息
     * @return 所有DailyDispatchSuggestion对象的列表
     */
    List<DailyDispatchSuggestion> getAllSuggestions();

    /**
     * 更新调度建议的状态
     * @param suggestionId 调度建议的ID
     * @param newStatus 新的状态
     */
    void updateSuggestionStatus(Integer suggestionId, String newStatus);


}