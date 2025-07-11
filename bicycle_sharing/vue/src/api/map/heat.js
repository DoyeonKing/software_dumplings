// 模拟热力图数据
// 以深圳为中心 (经度 113.8, 纬度 22.4)
const mockHeatData = {
  code: 200,
  msg: "success",
  data: []
};

// 生成模拟数据
const generateMockData = () => {
  const centerLng = 114.1351;
  const centerLat = 22.552938;
  const data = [];

  // 生成200个测试点，其中部分点会集中在几个热点区域
  for (let i = 0; i < 200; i++) {
    let lng, lat, count;

    if (i % 3 === 0) {
      // 1/3的点集中在热点区域
      const hotspotIndex = Math.floor(Math.random() * 3); // 3个热点区域
      const hotspots = [
        { lng: centerLng + 0.002, lat: centerLat + 0.002 }, // 深圳市区某热点
        { lng: centerLng - 0.002, lat: centerLat - 0.002 }, // 深圳市区另一热点
        { lng: centerLng, lat: centerLat }                  // 市中心
      ];
      
      // 在热点周围生成点，范围更小，权重更大
      lng = hotspots[hotspotIndex].lng + (Math.random() - 0.5) * 0.002;
      lat = hotspots[hotspotIndex].lat + (Math.random() - 0.5) * 0.002;
      count = Math.floor(Math.random() * 50) + 50; // 50-100的权重
    } else {
      // 2/3的点随机分布
      lng = centerLng + (Math.random() - 0.5) * 0.01;
      lat = centerLat + (Math.random() - 0.5) * 0.01;
      count = Math.floor(Math.random() * 30) + 1; // 1-30的权重
    }
    
    data.push({
      lng,
      lat,
      count
    });
  }

  return data;
};

// 获取热力图数据的方法
export const getHeatMapData = () => {
  return new Promise((resolve) => {
    // 模拟异步请求
    setTimeout(() => {
      mockHeatData.data = generateMockData();
      resolve(mockHeatData);
    }, 500);
  });
};
