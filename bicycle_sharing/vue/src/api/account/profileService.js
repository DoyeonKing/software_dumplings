import request from '@/utils/request'

/**
 * 管理员登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 登录结果，包含token和管理员信息
 */
export function adminLogin(data) {
    return request({
        url: '/managers/login', // 假设管理员登录URL
        method: 'post',
        data: {
            username: data.username,
            password: data.password,
            // 管理员登录可能不需要role字段，或者后端会验证
        }
    })
}

/**
 * 获取管理员个人信息
 * @param {string} token - Authorization token
 * @returns {Promise} 管理员信息
 */
export function getAdminProfile(token) {
    return request({
        url: '/managers/profile', // 假设获取管理员信息的URL
        method: 'get',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
}

