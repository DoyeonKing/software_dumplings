import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/login',
    method: 'post',
    data: {
      username: data.username,
      password: data.password,
      role: data.role
    }
  })
  // 返回数据格式
  // {
  //   "code": "200",
  //   "msg": "请求成功",
  //   "data": {
  //     "userInfo": {
  //       "userid": "d13908b5-aab8-48f8-ad00-0bc26a4a2076",
  //       "username": "user_wzm",
  //       "passwordHash": null,
  //       "phoneNumber": "19933332222",
  //       "totalRides": 0,
  //       "totalDurationMinutes": 0,
  //       "totalCost": 0
  //     },
  //     "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6ImQxMzkwOGI1LWFhYjgtNDhmOC1hZDAwLTBiYzI2YTRhMjA3NiIsInN1YiI6InVzZXJfd3ptIiwiaWF0IjoxNzUyNTU4NzgxLCJleHAiOjE3NTI1NzY3ODF9.UtGtJk9cDXmTeoxWrmlZB5RTWpD_-_3Cer6MrC3kt9w",
  //     "role": null
  //   }
  // }
}


/**
 * 修改用户密码
 * @param {string} token - Authorization token
 * @param {Object} data - 修改密码数据
 * @param {string} data.oldPassword - 原密码
 * @param {string} data.newPassword - 新密码
 * @param {string} data.confirmNewPassword - 确认新密码
 * @returns {Promise} 修改密码结果
 */
export function changePassword(token, data) {
  return request({
    url: '/user/change-password',
    method: 'post',
    headers: {
      'Authorization': 'Bearer ' + token
    },
    data: {
      oldPassword: data.oldPassword,
      newPassword: data.newPassword,
      confirmNewPassword: data.confirmNewPassword
    }
  })

  // 返回数据格式
  // {
  //   "code": 200 或 500,
  //   "msg": "string",
  //   "data": "修改密码成功" 或 "修改密码失败"
  // }
}





