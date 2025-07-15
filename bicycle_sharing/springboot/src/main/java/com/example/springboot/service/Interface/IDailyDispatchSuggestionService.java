package com.example.springboot.service.Interface;

import com.example.springboot.entity.DailyDispatchSuggestion;
import java.time.LocalDate;
import java.util.List;

public interface IDailyDispatchSuggestionService {
    void saveDailySuggestions(List<DailyDispatchSuggestion> suggestions);
    List<DailyDispatchSuggestion> getSuggestionsByDateAndBounds(LocalDate reportDate, double minLat, double maxLat, double minLon, double maxLon);
    void deleteSuggestionsByDate(LocalDate reportDate);
}