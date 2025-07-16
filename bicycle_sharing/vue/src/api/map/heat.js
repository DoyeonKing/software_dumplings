import request from '@/utils/request'
import { getMapAreaBicycles } from './bicycle'

// 获取地图区域内的热力图数据（基于单车位置）
export function getHeatMapData(params) {
  // 直接使用单车API获取区域内的单车数据
  return getMapAreaBicycles({
    minLat: params.minLat,
    maxLat: params.maxLat,
    minLng: params.minLon, // 注意：单车API使用的是minLng而不是minLon
    maxLng: params.maxLon  // 注意：单车API使用的是maxLng而不是maxLon
  })
}

// 数据格式转换函数：将单车数据转换为高德地图热力图需要的格式
export function convertHeatMapData(bicycleData) {
  // 添加安全检查
  if (!bicycleData || !Array.isArray(bicycleData)) {
    console.error('convertHeatMapData: 输入数据无效', bicycleData);
    return [];
  }

  return bicycleData.map(bike => {
    // 验证必需字段 - 根据单车数据结构调整字段名
    if (!bike || typeof bike.currentLat !== 'number' || typeof bike.currentLon !== 'number') {
      console.warn('convertHeatMapData: 跳过无效的单车数据点', bike);
      return null;
    }

    // 高德地图热力图数据格式：{ lng: number, lat: number, count: number }
    return {
      lng: bike.currentLon,  // 单车当前经度
      lat: bike.currentLat,  // 单车当前纬度
      count: 1  // 每个单车的权重设为1
    };
  }).filter(item => item !== null); // 过滤掉无效数据
}

// 兼容旧版本的导出（如果有其他地方还在使用）
export const getHeatMapDataLegacy = () => {
  console.warn('getHeatMapDataLegacy is deprecated, please use getHeatMapData with proper parameters');
  
  // 返回空数据的Promise，避免报错
  return Promise.resolve({
    code: 200,
    msg: "请使用新的API接口",
    data: []
  });
};
