import request from '@/utils/request'

// 获取管理员手下的所有工作人员
export function getManagedStaff() {
    const token = sessionStorage.getItem('authToken'); // 修正键名为authToken
    return request({
        url: '/managers/managed-staff',
        method: 'get',
        headers: {
            'Authorization': token ? 'Bearer ' + token : '' // 使用Bearer格式
        }
    });
}

// 获取所有工作人员
export function getAllWorkers() {
    const token = sessionStorage.getItem('authToken');
    return request({
        url: '/staff/workers',
        method: 'get',
        headers: {
            'Authorization': token ? 'Bearer ' + token : ''
        }
    });
}

// 获取工作人员信息
export function getStaffWorkers() {
    const token = sessionStorage.getItem('authToken');
    return request({
        url: '/staff/workers',
        method: 'get',
        headers: {
            'Authorization': token ? 'Bearer ' + token : ''
        }
    });
} 