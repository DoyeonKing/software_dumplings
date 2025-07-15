import request from '@/utils/request'

/**
 * 开始调度任务
 * @param {number} taskId - 任务ID
 * @returns {Promise} 返回选中的单车ID列表
 */
export function startDispatchTask(taskId) {
    return request({
        url: `/dispatchTasks/${taskId}/start`,
        method: 'put'
    }).then(response => {
        // 确保返回标准格式
        return {
            code: response.code || "200",
            msg: response.msg || "调度任务开始成功",
            data: response.data || []
        };
    }).catch(error => {
        console.error('开始调度任务失败:', error);
        return {
            code: "500",
            msg: error.message || "开始调度任务失败",
            data: []
        };
    });
}

/**
 * 完成调度任务
 * @param {number} taskId - 任务ID  
 * @returns {Promise} 返回完成状态
 */
export function completeDispatchTask(taskId) {
    return request({
        url: `/dispatchTasks/${taskId}/complete`,
        method: 'put'
    }).then(response => {
        // 确保返回标准格式
        return {
            code: response.code || "200",
            msg: response.msg || "调度任务完成成功",
            data: response.data || null
        };
    }).catch(error => {
        console.error('完成调度任务失败:', error);
        return {
            code: "500",
            msg: error.message || "完成调度任务失败",
            data: null
        };
    });
}

/**
 * 获取调度任务的单车列表
 * @param {number} taskId - 任务ID
 * @returns {Promise} 返回调度任务关联的单车ID列表
 */
export function getDispatchTaskBikes(taskId) {
    return request({
        url: `/dispatchTasks/${taskId}/bikes`,
        method: 'get'
    }).then(response => {
        // 确保返回标准格式
        return {
            code: response.code || "200",
            msg: response.msg || "成功获取调度任务单车列表",
            data: response.data || []
        };
    }).catch(error => {
        console.error('获取调度任务单车列表失败:', error);
        return {
            code: "500",
            msg: error.message || "获取调度任务单车列表失败",
            data: []
        };
    });
}
