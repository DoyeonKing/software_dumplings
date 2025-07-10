import request from '@/utils/request'

// 获取所有单车信息
export function getAllBicycles() {
  return request({
    url: '/bicycle/list',
    method: 'get'
  })
}

// 获取地图指定区域内的单车
export function getMapAreaBicycles(params) {
  return request({
    url: '/bikes/viewport',
    method: 'get',
    params: {
      minLat: params.minLat,
      maxLat: params.maxLat,
      minLon: params.minLng,
      maxLon: params.maxLng,
      bikeStatus: params.bikeStatus
    }
  })
}

// 获取单车详细信息
export function getBikeDetails(bikeId) {
  return request({
    url: `/bikes/${bikeId}/details`,
    method: 'get'
  })
} 


