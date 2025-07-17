import request from '@/utils/request'

// 创建调度任务
export function createDispatchTask(taskData) {
    return request({
        url: '/dispatchTasks/create',
        method: 'post',
        data: {
            startGeohash: taskData.startGeohash,
            endGeohash: taskData.endGeohash,
            assignedTo: taskData.assignedTo,
            bikeCount: taskData.bikeCount
        }
    });
}

// 获取调度任务列表（如果将来需要的话）
export function getDispatchTasks() {
    return request({
        url: '/dispatchTasks',
        method: 'get'
    });
} 