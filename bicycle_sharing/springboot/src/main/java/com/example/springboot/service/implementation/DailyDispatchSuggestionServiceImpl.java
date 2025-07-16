package com.example.springboot.service.implementation;

import com.example.springboot.entity.DailyDispatchSuggestion;
import com.example.springboot.mapper.DailyDispatchSuggestionMapper;
import com.example.springboot.service.Interface.IDailyDispatchSuggestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyDispatchSuggestionServiceImpl implements IDailyDispatchSuggestionService {

    private final DailyDispatchSuggestionMapper dailyDispatchSuggestionMapper;

    public DailyDispatchSuggestionServiceImpl(DailyDispatchSuggestionMapper dailyDispatchSuggestionMapper) {
        this.dailyDispatchSuggestionMapper = dailyDispatchSuggestionMapper;
    }

    // DailyDispatchSuggestionServiceImpl.java
    @Override
    @Transactional
    public void saveDailySuggestions(List<DailyDispatchSuggestion> suggestions) {
        if (suggestions != null && !suggestions.isEmpty()) {
            // LocalDate reportDate = suggestions.get(0).getReportDate(); // 这行也不再需要
            // dailyDispatchSuggestionMapper.deleteByDate(reportDate); // 移除这行
            dailyDispatchSuggestionMapper.insertBatch(suggestions);
        }
    }

    @Override
    public List<DailyDispatchSuggestion> getSuggestionsByDateAndBounds(LocalDate reportDate, double minLat, double maxLat, double minLon, double minLon2) {
        return dailyDispatchSuggestionMapper.selectSuggestionsByDateAndBounds(reportDate, minLat, maxLat, minLon, minLon2);
    }

    @Override
    @Transactional
    public void deleteSuggestionsByDate(LocalDate reportDate) {
        dailyDispatchSuggestionMapper.deleteByDate(reportDate);
    }

    @Override
    public List<DailyDispatchSuggestion> getAllSuggestions() {
        return dailyDispatchSuggestionMapper.selectAllSuggestions();
    }

    @Override
    @Transactional
    public void updateSuggestionStatus(Integer suggestionId, String newStatus) {
        dailyDispatchSuggestionMapper.updateStatus(suggestionId, newStatus);
    }

}