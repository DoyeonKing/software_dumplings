import request from '@/utils/request'

/**
 * 用车接口 - 开始骑行
 * @param {string} userId - 用户ID
 * @param {string} bikeId - 单车ID
 * @returns {Promise} 用车结果
 */
export function rentBike(userId, bikeId) {
  return request({
    url: '/orders/rent',
    method: 'get',
    params: {
      userId: userId,
      bikeId: bikeId
    }
  })
  
  // 返回数据格式
  // {
  //   "code": 200,
  //   "data": {
  //     "orderId": "49680631-ed58-4ef6-b528-ff5b88004842",
  //     "bikeId": "97226db7-4e77-4f8c-9619-78a2aa0ac639",
  //     "userId": "d13908b5-aab8-48f8-ad00-0bc26a4a2076",
  //     "startTime": "2025-07-15 21:26:20",
  //     "endTime": null,
  //     "startLat": 22.513805705,
  //     "startLon": 114.042425744,
  //     "endLat": null,
  //     "endLon": null,
  //     "distance": null,
  //     "cost": null,
  //     "startGeohash": "ws1054e",
  //     "endGeohash": null,
  //     "startweekday": 2,
  //     "starthour": 21,
  //     "isweekend": 0,
  //     "durationMinutes": null
  //   },
  //   "message": "租借成功！"
  // }
}

/**
 * 用车接口 - 开始骑行（带位置验证）
 * @param {string} userId - 用户ID
 * @param {string} bikeId - 单车ID
 * @param {number} userLat - 用户位置纬度
 * @param {number} userLon - 用户位置经度
 * @returns {Promise} 用车结果
 */
export function rentBikeWithLocation(userId, bikeId, userLat, userLon) {
  return request({
    url: '/orders/rent-lonlat',
    method: 'get',
    params: {
      userId: userId,
      bikeId: bikeId,
      userLat: userLat,
      userLon: userLon
    }
  })
  
  // 返回数据格式与rentBike相同
}

/**
 * 还车接口 - 结束骑行
 * @param {string} userId - 用户ID
 * @param {string} bikeId - 单车ID
 * @param {number} endLat - 还车位置纬度
 * @param {number} endLon - 还车位置经度
 * @returns {Promise} 还车结果
 */
export function returnBike(userId, bikeId, endLat, endLon) {
  return request({
    url: '/orders/return',
    method: 'get',
    params: {
      userId: userId,
      bikeId: bikeId,
      endLat: endLat,
      endLon: endLon
    }
  })
  
  // 返回数据格式
  // {
  //   "code": 200,
  //   "data": {
  //     "orderId": "49680631-ed58-4ef6-b528-ff5b88004842",
  //     "bikeId": "97226db7-4e77-4f8c-9619-78a2aa0ac639",
  //     "userId": "d13908b5-aab8-48f8-ad00-0bc26a4a2076",
  //     "startTime": "2025-07-15 21:26:21",
  //     "endTime": "2025-07-15 21:27:49",
  //     "startLat": 22.513805705,
  //     "startLon": 114.042425744,
  //     "endLat": 22.514269,
  //     "endLon": 114.044249,
  //     "distance": 194.25,
  //     "cost": null,
  //     "startGeohash": "ws1054e",
  //     "endGeohash": "ws1054s",
  //     "startweekday": 2,
  //     "starthour": 21,
  //     "isweekend": 0,
  //     "durationMinutes": 1
  //   },
  //   "message": "还车成功！"
  // }
}

/**
 * 获取用户当前未完成的骑行记录
 * @param {string} userId - 用户ID
 * @returns {Promise} 未完成骑行记录列表
 * 
 * 返回数据格式说明：
 * 1. 有未完成记录时：
 * {
 *   "code": 200,
 *   "data": [
 *     {
 *       "orderid": "164185a4-d4a0-4540-837a-d8b9997481b8",
 *       "bikeid": "00074911-2446-4da3-9242-f88d04d2fb5c", 
 *       "userid": "000184e8-1bf5-4e5e-9233-595aac79a3a6",
 *       "startTime": "2025-07-15 08:24:34",
 *       "endTime": null,
 *       "startLat": 22.4694441258,
 *       "startLon": 113.8723440824,
 *       "endLat": null,
 *       "endLon": null,
 *       "distanceM": null,
 *       "cost": null,
 *       "startGeohash": "webzz4r",
 *       "endGeohash": null,
 *       "startWeekday": 2,
 *       "startHour": 16,
 *       "isWeekend": 0,
 *       "durationMinutes": null
 *     }
 *   ],
 *   "message": "获取成功！"
 * }
 * 
 * 2. 无未完成记录时：
 * {
 *   "code": 400,
 *   "data": null,
 *   "message": "未找到未完成的骑行记录"
 * }
 * 
 * 注意：判断是否有未完成记录应检查 data 是否为 null，而不是判断 code
 */
export function getCurrentRideOrders(userId) {
  return request({
    url: '/orders/current-ride',
    method: 'get',
    params: {
      userId: userId
    }
  })
}
