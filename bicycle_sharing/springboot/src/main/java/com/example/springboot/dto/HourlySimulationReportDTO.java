package com.example.springboot.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HourlySimulationReportDTO {
    private LocalDateTime reportGeneratedTime;
    private LocalDateTime predictionTargetTime; // 这份报告是针对哪个时间点的预测
    private List<AreaStatusDTO> areaStatuses; // 区域状态列表
    private List<DispatchSuggestionDTO> dispatchSuggestions; // 调度建议列表
    private String rawReportContent; // 可以保留原始的字符串报告作为补充
}