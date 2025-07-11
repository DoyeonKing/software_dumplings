import request from '@/utils/request'

/**
 * 根据地理哈希和日期获取天气记录
 * @param {Object} params - 请求参数
 * @param {string} params.geohashArea - 地理哈希区域
 * @param {string} params.recordDate - 记录日期 (格式: YYYY-MM-DD)
 * @returns {Promise} 天气记录数据
 */
export function getWeatherRecord(params) {
  return request({
    url: '/weatherRecord/by-geohash-date',
    method: 'get',
    params: {
      geohashArea: params.geohashArea,
      recordDate: params.recordDate
    }
  }).then(response => {
    // 将后端直接返回的数据封装成统一的Result格式
    if (response && typeof response === 'object') {
      return {
        code: 200,
        msg: '获取天气数据成功',
        data: response
      }
    } else {
      return {
        code: 404,
        msg: '未找到天气数据',
        data: null
      }
    }
  }).catch(error => {
    console.error('天气API请求失败:', error)
    return {
      code: 500,
      msg: '获取天气数据失败',
      data: null
    }
  })
  // 返回数据格式示例
  // {
  //   "code": 200,
  //   "msg": "string",
  //   "data": {
        //   "weatherRecordId": 0,
        //   "recordDate": "2025-07-11",
        //   "geohashArea": "string",
        //   "tempMaxC": 0,
        //   "tempMinC": 0,
        //   "windDirection": "string",
        //   "windLevel": 0,
        //   "hasPrecipitationTextIndicator": 0    
  //   }
  // }
}   
