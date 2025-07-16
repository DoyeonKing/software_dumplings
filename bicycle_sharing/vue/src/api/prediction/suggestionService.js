import request from '@/utils/request'

// 获取所有调度建议
export function getAllSuggestions() {
    return request({
        url: '/api/predict/suggestions/all',
        method: 'get'
    });
}

// 更新调度建议状态
export function updateSuggestionStatus(suggestionId, newStatus) {
    return request({
        url: '/api/predict/suggestions/status',
        method: 'put',
        data: {
            suggestionId,
            newStatus
        }
    });
} 