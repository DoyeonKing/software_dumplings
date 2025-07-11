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
  //     "user": {
  //       "userid": "eeaf930b6643ed567ccd2a",
  //       "username": "eeaf930b6643ed567ccd2a",
  //       "passwordHash": null,
  //       "phoneNumber": "18806141291",
  //       "totalRides": 1,
  //       "totalDurationMinutes": 5,
  //       "totalCost": 1.25
  //     },
  //     "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6ImVlYWY5MzBiNjY0M2VkNTY3Y2NkMmEiLCJzdWIiOiJlZWFmOTMwYjY2NDNlZDU2N2NjZDJhIiwiaWF0IjoxNzUyMjIzNDE1LCJleHAiOjE3NTIyNDE0MTV9.MCinoSIVV5m4RHG6rc_gY45u5R730hEPnfjT1Bitb-s"
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





