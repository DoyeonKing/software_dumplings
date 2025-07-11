import request from '@/utils/request'

// 模拟的调度任务数据
const mockTasks = [
    {
        task_id: 1,
        start_geohash: 'ws10ev',  // 深圳大学站区域
        end_geohash: 'ws10k2',    // 桃园站区域
        bike_count: 3,
        assigned_to: null,        // 未分配
        status: '未处理',         // ENUM('未处理','处理中','处理完成')
        created_at: '2024-03-20 09:30:00',
        completed_at: null
    },
    {
        task_id: 2,
        start_geohash: 'ws10k2',  // 桃园站区域
        end_geohash: 'ws10eu',    // 大冲站区域
        bike_count: 5,
        assigned_to: 1001,        // 已分配给工作人员1001
        status: '处理中',
        created_at: '2024-03-20 10:15:00',
        completed_at: null
    },
    {
        task_id: 3,
        start_geohash: 'ws10eu',  // 大冲站区域
        end_geohash: 'ws10ev',    // 深圳大学站区域
        bike_count: 2,
        assigned_to: 1002,
        status: '处理完成',
        created_at: '2024-03-20 08:00:00',
        completed_at: '2024-03-20 09:00:00'
    },
    {
        task_id: 4,
        start_geohash: 'ws10ev',  // 深圳大学站区域
        end_geohash: 'ws10k2',    // 桃园站区域
        bike_count: 4,
        assigned_to: null,
        status: '未处理',
        created_at: '2024-03-20 11:00:00',
        completed_at: null
    }
];

// 获取所有调度任务
export function getAllTasks() {
    return Promise.resolve({
        code: 200,
        data: mockTasks
    });
}

// 获取指定工作人员的调度任务
export function getWorkerTasks(workerId) {
    const tasks = mockTasks.filter(task => task.assigned_to === workerId);
    return Promise.resolve({
        code: 200,
        data: tasks
    });
}

// 接受任务
export function acceptTask(taskId, workerId) {
    const task = mockTasks.find(t => t.task_id === taskId);
    if (task && task.status === '未处理') {
        task.status = '处理中';
        task.assigned_to = workerId;
        return Promise.resolve({
            code: 200,
            message: '任务已接受'
        });
    }
    return Promise.resolve({
        code: 400,
        message: '任务不可接受'
    });
}

// 完成任务
export function completeTask(taskId) {
    const task = mockTasks.find(t => t.task_id === taskId);
    if (task && task.status === '处理中') {
        task.status = '处理完成';
        task.completed_at = new Date().toISOString().slice(0, 19).replace('T', ' ');
        return Promise.resolve({
            code: 200,
            message: '任务已完成'
        });
    }
    return Promise.resolve({
        code: 400,
        message: '任务不可完成'
    });
}