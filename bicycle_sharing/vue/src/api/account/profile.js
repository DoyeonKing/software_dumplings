import request from '@/utils/request'

/**
 * 获取用户个人信息
 * @param {string} token - Authorization token
 * @returns {Promise} 用户个人信息数据
 */
export function getUserProfile(token) {
  return request({
    url: '/user/profile',
    method: 'get',
    headers: {
      'Authorization': 'Bearer ' + token
    }
  })

  // 返回数据格式
  // {
  //   "code": 200,
  //   "msg": "string",
  //   "data": {
  //       "userid": "string",
  //       "username": "string",
  //       "passwordHash": null,
  //       "phoneNumber": "string",
  //       "totalRides": 1,
  //       "totalDurationMinutes": 6,
  //       "totalCost": 1.5
  //      }
  //   }
} 

/**
 * 获取工作人员个人信息
 * @param {string} token - Authorization token
 * @returns {Promise} 工作人员个人信息数据
 */
export function getStaffProfile(token) {
  return request({
    url: '/staff/profile',
    method: 'get',
    headers: {
      'Authorization': 'Bearer ' + token
    }
  })

  // 返回数据格式
  // {
  //   "code": "200",
  //   "msg": "string",
  //   "data": {
  //       "staffId": "string",
  //       "username": "string",
  //       "phoneNumber": "string",
  //       "workArea": "string",
  //       "completedTasks": 0,
  //       "workingHours": 0.0,
  //       "performanceRating": 0.0
  //      }
  //   }
} 