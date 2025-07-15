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
