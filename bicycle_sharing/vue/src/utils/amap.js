// 高德地图 Web 服务 API 工具函数

// Web服务 Key，用于服务端API调用 [[memory:2538380]]
const WEB_SERVICE_KEY = '4c4409cdbe818ceb94f8660e2e111563';

/**
 * 获取骑行路线规划
 * @param {Array} start 起点坐标 [lng, lat]
 * @param {Array} end 终点坐标 [lng, lat]
 * @returns {Promise} 返回路线规划结果
 */
export const getRidingRoute = async (start, end) => {
  try {
    console.log('开始规划路线，起点:', start, '终点:', end);

    // 构建API URL
    const url = `https://restapi.amap.com/v4/direction/bicycling?key=${WEB_SERVICE_KEY}&origin=${start.join(',')}&destination=${end.join(',')}`;
    
    console.log('请求URL:', url);
    const response = await fetch(url);
    const data = await response.json();
    
    console.log('API返回数据:', data);

    if (data.errcode === 0 && data.data && data.data.paths && data.data.paths.length > 0) {
      const path = data.data.paths[0];
      return {
        status: 'complete',
        route: {
          distance: path.distance,
          duration: path.duration,
          polyline: path.steps.reduce((points, step) => {
            // polyline格式为 "X,Y;X,Y;X,Y"
            const stepPoints = step.polyline.split(';').map(point => {
              const [lng, lat] = point.split(',').map(Number);
              return [lng, lat];
            });
            return points.concat(stepPoints);
          }, [])
        }
      };
    } else {
      console.error('路线规划失败:', data);
      throw new Error(data.errmsg || '路线规划失败');
    }
  } catch (error) {
    console.error('骑行路线规划请求失败：', error);
    return {
      status: 'error',
      error: error.message
    };
  }
}; 


/**
 * 获取驾车路线规划
 * @param {Array} start 起点坐标 [lng, lat]
 * @param {Array} end 终点坐标 [lng, lat]
 * @param {string} extensions 返回结果详细程度，默认为base，可选all
 * @returns {Promise} 返回路线规划结果
 */
export const getDrivingRoute = async (start, end, extensions = 'base') => {
  try {
    console.log('开始规划驾车路线，起点:', start, '终点:', end);

    // 构建API URL
    const url = `https://restapi.amap.com/v3/direction/driving?key=${WEB_SERVICE_KEY}&origin=${start.join(',')}&destination=${end.join(',')}&extensions=${extensions}`;
    
    console.log('请求URL:', url);
    const response = await fetch(url);
    const data = await response.json();
    
    console.log('驾车路线规划API返回数据:', data);

    // 直接返回原始数据，不做处理
    return data;
  } catch (error) {
    console.error('驾车路线规划请求失败：', error);
    return {
      status: 'error',
      error: error.message
    };
  }
}; 


/**
 * 获取步行路线规划
 * @param {Array} start 起点坐标 [lng, lat]
 * @param {Array} end 终点坐标 [lng, lat]
 * @param {Object} options 可选参数
 * @param {string} options.origin_id 起点POI ID
 * @param {string} options.destination_id 目的地POI ID
 * @param {string} options.output 返回数据格式类型，默认JSON
 * @returns {Promise} 返回步行路线规划原始结果
 */
export const getWalkingRoute = async (start, end, options = {}) => {
  try {
    console.log('开始规划步行路线，起点:', start, '终点:', end);

    // 构建基础参数
    const params = new URLSearchParams({
      key: WEB_SERVICE_KEY,
      origin: start.join(','),
      destination: end.join(',')
    });

    // 添加可选参数
    if (options.origin_id) {
      params.append('origin_id', options.origin_id);
    }
    if (options.destination_id) {
      params.append('destination_id', options.destination_id);
    }
    if (options.output) {
      params.append('output', options.output);
    }

    // 构建API URL
    const url = `https://restapi.amap.com/v3/direction/walking?${params.toString()}`;
    
    console.log('请求URL:', url);
    const response = await fetch(url);
    const data = await response.json();
    
    console.log('步行路线规划API返回数据:', data);

    // 直接返回原始数据，不做处理
    return data;
  } catch (error) {
    console.error('步行路线规划请求失败：', error);
    return {
      status: 'error',
      error: error.message
    };
  }
};