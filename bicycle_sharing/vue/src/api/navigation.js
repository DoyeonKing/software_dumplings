import request from '@/utils/request'
import { getDrivingRoute } from '@/utils/amap'

/**
 * 根据停车区域编码获取中心经纬度
 * @param {string} geohash - 停车区域的区域编码 (如: ws105ys)
 * @returns {Promise} 返回包含中心点经纬度的Promise对象
 * 返回数据格式: { centerLat: number, centerLon: number }
 */
export function getParkingAreaCenter(geohash) {
  return request({
    url: '/elitesites/centerLatLon',
    method: 'get',
    params: {
      geohash: geohash
    }
  })
}

/**
 * 批量获取多个停车区域的中心经纬度
 * @param {string[]} geohashList - 停车区域编码数组
 * @returns {Promise[]} 返回Promise数组
 */
export function getBatchParkingAreaCenters(geohashList) {
  const promises = geohashList.map(geohash => getParkingAreaCenter(geohash))
  return Promise.all(promises)
}

/**
 * 获取工作人员调度任务的导航路径
 * @param {Object} workerPosition - 工作人员当前位置 {lat: number, lng: number}
 * @param {string} startGeohash - 调度起点区域编码
 * @param {string} endGeohash - 调度终点区域编码
 * @returns {Promise} 返回包含两条路径的Promise对象
 */
export async function getTaskNavigationRoutes(workerPosition, startGeohash, endGeohash) {
  try {
    console.log('开始获取导航路径:', { workerPosition, startGeohash, endGeohash });

    // 1. 获取起点和终点的经纬度
    const [startLocationResponse, endLocationResponse] = await Promise.all([
      getParkingAreaCenter(startGeohash),
      getParkingAreaCenter(endGeohash)
    ]);

    console.log('起点位置响应:', startLocationResponse);
    console.log('终点位置响应:', endLocationResponse);

    // 检查响应格式并提取数据
    const startLocation = startLocationResponse.data || startLocationResponse;
    const endLocation = endLocationResponse.data || endLocationResponse;

    if (!startLocation || !endLocation) {
      throw new Error('无法获取停车区域位置信息');
    }

    // 转换为高德API需要的格式 [lng, lat]
    const workerCoords = [workerPosition.lng, workerPosition.lat];
    const startCoords = [startLocation.centerLon, startLocation.centerLat];
    const endCoords = [endLocation.centerLon, endLocation.centerLat];

    console.log('坐标转换结果:', { workerCoords, startCoords, endCoords });

    // 2. 获取两条驾车路径
    const [route1Response, route2Response] = await Promise.all([
      getDrivingRoute(workerCoords, startCoords, 'base'), // 工作人员位置 → 起点
      getDrivingRoute(startCoords, endCoords, 'base')     // 起点 → 终点
    ]);

    console.log('路径1响应 (工作人员→起点):', route1Response);
    console.log('路径2响应 (起点→终点):', route2Response);

    // 3. 处理路径数据
    const processRouteData = (routeResponse, routeName) => {
      if (!routeResponse || routeResponse.status !== '1' || !routeResponse.route) {
        console.error(`${routeName}路径规划失败:`, routeResponse);
        return null;
      }

      const route = routeResponse.route;
      const path = route.paths && route.paths[0];
      
      if (!path) {
        console.error(`${routeName}路径数据无效:`, route);
        return null;
      }

             // 提取路径点坐标
       const coordinates = [];
       if (path.steps && Array.isArray(path.steps)) {
         path.steps.forEach(step => {
           if (step.polyline) {
             // polyline格式: "lng1,lat1;lng2,lat2;lng3,lat3"
             const points = step.polyline.split(';')
               .filter(point => point && point.includes(','))
               .map(point => {
                 const coords = point.split(',');
                 if (coords.length >= 2) {
                   const lng = parseFloat(coords[0]);
                   const lat = parseFloat(coords[1]);
                   if (!isNaN(lng) && !isNaN(lat)) {
                     return [lng, lat];
                   }
                 }
                 return null;
               })
               .filter(point => point !== null);
             
             coordinates.push(...points);
           }
         });
       }

       console.log(`${routeName} - 提取的坐标点数量:`, coordinates.length);
       console.log(`${routeName} - 前5个坐标点:`, coordinates.slice(0, 5));

      return {
        name: routeName,
        distance: parseInt(path.distance) || 0, // 距离（米）
        duration: parseInt(path.duration) || 0, // 时间（秒）
        coordinates: coordinates, // 路径坐标点数组
        origin: route.origin,
        destination: route.destination,
        rawData: path // 保留原始数据以备需要
      };
    };

    const route1 = processRouteData(route1Response, '工作人员到起点');
    const route2 = processRouteData(route2Response, '起点到终点');

    // 4. 检查路径是否都成功获取
    if (!route1 || !route2) {
      const errorMsg = [];
      if (!route1) errorMsg.push('工作人员到起点路径规划失败');
      if (!route2) errorMsg.push('起点到终点路径规划失败');
      throw new Error(errorMsg.join('，'));
    }

    // 5. 返回结果
    const result = {
      success: true,
      routes: [route1, route2],
      summary: {
        totalDistance: route1.distance + route2.distance,
        totalDuration: route1.duration + route2.duration,
        startLocation: {
          geohash: startGeohash,
          coordinates: startCoords,
          centerLat: startLocation.centerLat,
          centerLon: startLocation.centerLon
        },
        endLocation: {
          geohash: endGeohash,
          coordinates: endCoords,
          centerLat: endLocation.centerLat,
          centerLon: endLocation.centerLon
        }
      }
    };

    console.log('导航路径获取成功:', result);
    return result;

  } catch (error) {
    console.error('获取导航路径失败:', error);
    return {
      success: false,
      error: error.message,
      routes: []
    };
  }
}

/**
 * 格式化距离显示
 * @param {number} distance - 距离（米）
 * @returns {string} 格式化后的距离字符串
 */
export function formatDistance(distance) {
  if (distance < 1000) {
    return `${distance}米`;
  }
  return `${(distance / 1000).toFixed(1)}公里`;
}

/**
 * 格式化时间显示
 * @param {number} duration - 时间（秒）
 * @returns {string} 格式化后的时间字符串
 */
export function formatDuration(duration) {
  const minutes = Math.floor(duration / 60);
  const seconds = duration % 60;
  
  if (minutes === 0) {
    return `${seconds}秒`;
  } else if (seconds === 0) {
    return `${minutes}分钟`;
  } else {
    return `${minutes}分${seconds}秒`;
  }
}
