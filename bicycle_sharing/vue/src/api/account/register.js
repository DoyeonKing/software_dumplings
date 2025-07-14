import request from '@/utils/request'

/**
 * 用户注册（统一接口）
 * @param {Object} data - 注册数据
 * @param {string} data.username - 用户名
 * @param {string} data.phoneNumber - 手机号码
 * @param {string} data.password - 密码
 * @param {string} data.confirmPassword - 确认密码
 * @param {string} data.role - 角色 (user/admin/worker)
 * @returns {Promise} 注册结果
 */
export function register(data) {
  return request({
    url: '/register',
    method: 'post',
    data: {
      username: data.username,
      phoneNumber: data.phoneNumber,
      password: data.password,
      confirmPassword: data.confirmPassword,
      role: data.role
    }
  })

// 返回数据格式示例
// {
//   "code": 200 或 400/500,
//   "msg": "string", 
//   "data": {
//   "userid": "4e46cd74-9dd8-4606-a533-4f8dba2892b9",
//   "username": "wzm",
//   "passwordHash": null,
//   "phoneNumber": "19933312031",
//   "totalRides": 0,
//   "totalDurationMinutes": 0,
//   "totalCost": 0
//   }
// }

}


