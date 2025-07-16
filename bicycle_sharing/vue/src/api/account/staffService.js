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