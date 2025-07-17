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

/**
 * 更新用户个人信息
 * @param {string} token - Authorization token
 * @param {Object} userData - 用户信息数据
 * @param {string} userData.userid - 用户ID
 * @param {string} userData.username - 用户名
 * @param {string} userData.phoneNumber - 手机号码
 * @param {number} userData.totalRides - 总骑行次数
 * @param {number} userData.totalDurationMinutes - 总骑行时长（分钟）
 * @param {number} userData.totalCost - 总消费金额
 * @returns {Promise} 更新结果
 */
export function updateUserProfile(token, userData) {
  // 给用户数据添加passwordHash属性
  const dataWithPasswordHash = {
    ...userData,
    passwordHash: 'placeholder_hash_value' // 按要求随便设个值
  }

  return request({
    url: '/user/profile',
    method: 'put',
    headers: {
      'Authorization': 'Bearer ' + token
    },
    data: dataWithPasswordHash
  })

  // 返回数据格式
  // {
  //   "code": "string",
  //   "msg": "string",
  //   "data": "string"
  // }
} 