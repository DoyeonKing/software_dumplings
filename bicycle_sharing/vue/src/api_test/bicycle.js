import request from '@/utils/request'

// 获取所有单车信息
export function getAllBicycles() {
  return request({
    url: '/bicycle/list',
    method: 'get'
  })
}

// 获取地图指定区域内的单车
export function getMapAreaBicycles(bounds) {
  return request({
    url: '/bicycle/area_list',
    method: 'get',
    params: {
      minLat: bounds.minLat,
      maxLat: bounds.maxLat,
      minLng: bounds.minLng,
      maxLng: bounds.maxLng
    }
  })
} 