package com.example.springboot.dto;

import lombok.Data;
import java.util.List;

/**
 * 地图预测响应 DTO
 * 聚合了地图范围内查询到的区域状态和调度建议
 */
@Data
public class MapPredictionResponseDTO {
    private List<AreaStatusDTO> areaStatuses;
    private List<DispatchSuggestionDTO> dispatchSuggestions;
}