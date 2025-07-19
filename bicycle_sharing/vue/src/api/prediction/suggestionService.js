import request from '@/utils/request'

// 根据地图区域获取调度建议
export function getMapAreaSuggestions(reportDateStr, predictionTimeHour, mapBounds) {
    console.log('suggestionService - 调用参数:', {
        reportDateStr,
        predictionTimeHour,
        mapBounds
    });
    
    return request({
        url: '/api/predict/map_area',
        method: 'post',
        params: {
            reportDateStr,
            predictionTimeHour
        },
        data: {
            minLat: mapBounds.minLat,
            maxLat: mapBounds.maxLat,
            minLon: mapBounds.minLon,
            maxLon: mapBounds.maxLon
        }
    });
}

// 更新调度建议状态
export function updateSuggestionStatus(suggestionId, newStatus) {
    console.log('suggestionService - 更新建议状态:', {
        suggestionId,
        newStatus
    });
    
    return request({
        url: '/predict/suggestions/status', // 保持与request.js的baseURL配置一致
        method: 'put',
        data: {
            suggestionId,
            newStatus
        }
    });
} 