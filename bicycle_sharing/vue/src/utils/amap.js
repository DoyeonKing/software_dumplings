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