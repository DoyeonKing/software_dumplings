<template>
  <div class="dashboard-view-root">
    <div id="mapContainer"></div>

    <MenuComponent @profile-saved="handleProfileSaved" />

    <div class="info-panel">
      <div class="location-selector info-card">
        <h3>地区选择</h3>
        <div class="select-group">
          <select v-model="selectedCity" @change="updateLocation">
<!--            <option value="">选择城市</option>-->
            <option value="深圳市">深圳市</option>
<!--            <option value="广州市">广州市</option>
            <option value="北京市">北京市</option>
            <option value="上海市">上海市</option>-->
          </select>
          <select v-model="selectedDistrict" @change="updateLocation">
            <option value="">选择区</option>
            <option v-for="district in availableDistricts" :key="district" :value="district">
              {{ district }}
            </option>
          </select>
          <select v-model="selectedRoad" @change="updateLocation">
            <option value="">选择道路</option>
            <option v-for="road in availableRoads" :key="road" :value="road">
              {{ road }}
            </option>
          </select>
        </div>
        <div class="current-location">
          当前选择：{{ currentLocation }}
        </div>
      </div>

      <div class="weather-card info-card">
        <div class="card-header">
          <h3>天气预报</h3>
          <span class="weather-icon">🌤️️</span>
        </div>
        <div class="weather-content">
          <div class="weather-main">
            <div class="temperature">{{ weatherData.temperature }}°C</div>
            <div class="weather-desc">{{ weatherData.description }}</div>
          </div>
          <div class="weather-details">
            <div class="weather-item">
              <span class="label">湿度：</span>
              <span class="value">{{ weatherData.humidity }}%</span>
            </div>
            <div class="weather-item">
              <span class="label">风速：</span>
              <span class="value">{{ weatherData.windSpeed }}km/h</span>
            </div>
            <div class="weather-item">
              <span class="label">空气质量：</span>
              <span class="value">{{ weatherData.airQuality }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="vehicle-info-card info-card">
        <div class="card-header">
          <h3>车辆信息</h3>
          <span class="vehicle-icon">🚲</span>
        </div>
        <div class="vehicle-content">
          <div class="parking-area-selector">
            <div class="selector-label">选择停车区域</div>
            <div class="selected-area">
              {{ selectedParkingArea || '点击地图上的停车区域' }}
            </div>
          </div>
          <div class="vehicle-stats">
            <div class="stats-main">
              <div class="stats-number">{{ vehicleData.totalBikes }}</div>
              <div class="stats-label">车辆总数</div>
            </div>
            <div class="stats-details">
              <div class="stats-item">
                <span class="label">使用率：</span>
                <span class="value usage-rate">{{ vehicleData.utilization }}%</span>
              </div>
              <div class="stats-item">
                <span class="label">可用车辆：</span>
                <span class="value available">{{ vehicleData.availableBikes }}</span>
              </div>
              <div class="stats-item">
                <span class="label">使用中：</span>
                <span class="value in-use">{{ vehicleData.inUseBikes }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="top-right-controls">
      <div class="control-group">
        <button class="control-btn" @click="onToggleBikes" :class="{ active: showBikes }">
          <span class="btn-icon">🚲</span>
          <span class="btn-text">{{ showBikes ? '隐藏单车' : '显示单车' }}</span>
        </button>
        <button class="control-btn" @click="onToggleHeatmap" :class="{ active: showHeatmap }">
          <span class="btn-icon">🔥</span>
          <span class="btn-text">{{ showHeatmap ? '普通地图' : '热力图' }}</span>
        </button>
        <button class="control-btn" @click="onToggleParkingAreas" :class="{ active: showParkingAreas }">
          <span class="btn-icon">🅿️</span>
          <span class="btn-text">{{ showParkingAreas ? '隐藏区域' : '显示区域' }}</span>
        </button>
        <button class="control-btn" @click="goHome">
          <span class="btn-icon">🏠</span>
          <span class="btn-text">主页</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue'
import AMapLoader from '@/utils/loadAMap.js'
import bicycleIcon from '@/components/icons/bicycle.png';
import { getMapAreaBicycles, getBikeUtilization } from '@/api/map/bicycle';
import { getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking.js';

export default {
  name: "DashboardView",
  components: {
    MenuComponent
  },
  data() {
    return {
      selectedCity: "深圳市",
      selectedDistrict: "福田区",
      selectedRoad: "",
      weatherData: {
        temperature: 28,
        description: "多云",
        humidity: 65,
        windSpeed: 12,
        airQuality: "优"
      },
      selectedParkingArea: null,
      vehicleData: {
        totalBikes: 0,
        utilization: 0,
        availableBikes: 0,
        inUseBikes: 0
      },
      cityDistrictRoad: {
        "深圳市": {
          "福田区": ["金田路", "福华三路", "福荣路", "深南大道"]
        }
      },
      // 地图相关
      map: null,
      infoWindow: null,
      markers: [],
      heatmap: null,
      heatmapReady: false,
      showHeatmap: false,
      parkingAreas: [],
      parkingPolygons: [],
      bikes: [],
      showBikes: false, // 默认隐藏单车
      showParkingAreas: false, // 默认隐藏停车区域
      // 添加默认缩放级别
      defaultZoom: 18,
      // 添加特定区域的坐标映射
      locationCoordinates: {
        "金田路": {
          center: [114.0622479856, 22.5374765653],
          parkingArea: "ws105wc"
        },
        "福华三路": {
          center: [114.0648990521, 22.5333978834],
          parkingArea: "ws105w5"
        },
        "福荣路": {
          center: [114.0430866507, 22.5133931471],
          parkingArea: "ws10547"
        },
        "深南大道": {
          center: [114.0522947637, 22.5405770101],
          parkingArea: "ws105r6"
        }
      }
    };
  },
  computed: {
    availableDistricts() {
      return this.selectedCity && this.cityDistrictRoad[this.selectedCity]
          ? Object.keys(this.cityDistrictRoad[this.selectedCity])
          : [];
    },
    availableRoads() {
      return this.selectedCity && this.selectedDistrict &&
      this.cityDistrictRoad[this.selectedCity] &&
      this.cityDistrictRoad[this.selectedCity][this.selectedDistrict]
          ? this.cityDistrictRoad[this.selectedCity][this.selectedDistrict]
          : [];
    },
    currentLocation() {
      let loc = [];
      if (this.selectedCity) loc.push(this.selectedCity);
      if (this.selectedDistrict) loc.push(this.selectedDistrict);
      if (this.selectedRoad) loc.push(this.selectedRoad);
      return loc.length ? loc.join(" - ") : "未选择";
    }
  },
  mounted() {
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      // 初始化地图
      this.map = new window.AMap.Map("mapContainer", {
        center: [114.0610, 22.5395],
        zoom: 17, // 更高的缩放级别
        dragEnable: true,
        zoomEnable: true,
        doubleClickZoom: true,
        keyboardEnable: true,
        scrollWheel: true,
        touchZoom: true,
        mapStyle: 'amap://styles/normal'
      });

      // 初始化信息窗口
      this.infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });

      // 加载热力图插件
      window.AMap.plugin(['AMap.HeatMap'], () => {
        this.heatmap = new window.AMap.HeatMap(this.map, {
          radius: 20,
          opacity: [0.1, 0.9],
          gradient: {
             0.4: '#4575b4',   // 深蓝色 - 最低密度
            0.5: '#74add1',   // 浅蓝色
            0.6: '#abd9e9',   // 更浅的蓝色
            0.7: '#ffffbf',   // 淡黄色
            0.8: '#fdae61',   // 橙色
            0.9: '#f46d43',   // 深橙色
            1.0: '#d73027'    // 红色 - 最高密度
          }
        });
        this.heatmapReady = true;
      });

      // 加载初始数据
      this.loadBicycles();
      this.loadParkingAreas();

      // 监听地图移动和缩放事件，但使用防抖来减少API调用频率
      let timeout;
      const updateData = () => {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
          this.loadBicycles();
          this.loadParkingAreas();
        }, 500); // 500ms的防抖延迟
      };

      this.map.on('moveend', updateData);
      this.map.on('zoomend', updateData);

    }).catch(err => {
      this.$message && this.$message.error
          ? this.$message.error('地图加载失败: ' + err.message)
          : alert('地图加载失败: ' + err.message);
    });
  },

  beforeUnmount() {
    if (this.map) {
      this.map.destroy();
    }
  },

  methods: {
    // 【新增】获取停车区域数据的方法
    async fetchParkingAreas() {
      try {
        const bounds = this.map.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLon: bounds.getSouthWest().lng,
          maxLon: bounds.getNorthEast().lng
        };
        const response = await getParkingAreasInBounds(params);

        let rawData = null;
        if (response && response.data && Array.isArray(response.data)) {
          rawData = response.data;
        } else if (response && Array.isArray(response)) {
          rawData = response;
        }

        if (rawData) {
          this.parkingAreas = convertParkingAreaData(rawData);
        } else {
          console.warn('停车点数据格式异常:', response);
          this.parkingAreas = [];
        }
      } catch (error) {
        console.error('获取停车点数据失败:', error);
        this.parkingAreas = [];
      }
    },
    // 【新增】显示停车区域的主方法
    async loadParkingAreas() {
      if (!this.map) return;
      try {
        if (this.parkingPolygons && this.parkingPolygons.length > 0) {
          this.map.remove(this.parkingPolygons);
          this.parkingPolygons = [];
        }
        await this.fetchParkingAreas();
        this.drawParkingAreas();
      } catch (error) {
        console.error("显示停车区域失败:", error);
      }
    },
    async loadBicycles() {
      try {
        const bounds = this.map.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLng: bounds.getSouthWest().lng,
          maxLng: bounds.getNorthEast().lng
        };
        const response = await getMapAreaBicycles(params);

        const bikesForMixin = response.data.map(bike => ({
          ...bike,
          lng: bike.currentLon,
          lat: bike.currentLat,
          id: bike.bikeId,
        }));

        this.bikes = bikesForMixin;

        const bikeMarkerIcon = new window.AMap.Icon({
          image: bicycleIcon,
          size: new window.AMap.Size(32, 32),
          imageSize: new window.AMap.Size(32, 32)
        });

        this.addBikeMarkers(this.bikes, bikeMarkerIcon);

        if (!this.showBikes) {
          this.markers.forEach(marker => marker.hide());
        }
      } catch (error) {
        console.error("加载单车数据失败:", error);
      }
    },

    addBikeMarkers(bikeList, bikeIcon) {
      this.markers.forEach(marker => marker.setMap(null));
      this.markers = [];

      bikeList.forEach(bike => {
        const marker = new window.AMap.Marker({
          position: [bike.lng, bike.lat],
          map: this.map,
          icon: bikeIcon,
          title: `单车编号: ${bike.id}`
        });

        marker.on('mouseover', () => {
          this.infoWindow.setContent(`
                    <div style="padding: 8px 12px; font-size: 14px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
                        <b>单车编号：</b>${bike.id}
                    </div>
                `);
          this.infoWindow.open(this.map, marker.getPosition());
        });
        marker.on('mouseout', () => this.infoWindow.close());

        this.markers.push(marker);
      });
    },

    onToggleBikes() {
      this.showBikes = !this.showBikes;
      if (this.markers && this.markers.length > 0) {
        if (this.showBikes && this.showHeatmap) {
          this.toggleHeatmap(this.bikes);
        } else {
          this.markers.forEach(marker => {
            this.showBikes ? marker.show() : marker.hide();
          });
        }
      }
    },

    onToggleHeatmap() {
      if (!this.showHeatmap) {
        this.showBikes = false;
      }
      this.toggleHeatmap();

      if (!this.showHeatmap && !this.showBikes) {
        this.markers.forEach(marker => marker.hide());
      }
    },

    onToggleParkingAreas() {
      this.showParkingAreas = !this.showParkingAreas;
      
      if (this.parkingPolygons && this.parkingPolygons.length > 0) {
        if (this.showParkingAreas) {
          // 显示停车区域
          this.parkingPolygons.forEach(polygon => {
            polygon.setMap(this.map);
          });
        } else {
          // 隐藏停车区域
          this.parkingPolygons.forEach(polygon => {
            polygon.setMap(null);
          });
        }
      }
      
      console.log(`停车区域已${this.showParkingAreas ? '显示' : '隐藏'}`);
    },

    toggleHeatmap() {
      this.showHeatmap = !this.showHeatmap;

      if (this.showHeatmap) {
        this.markers.forEach(m => m.hide());
        const heatData = this.bikes.map(bike => ({
          lng: bike.lng,
          lat: bike.lat,
          count: 30  // 降低每个点的权重值
        }));
        if (this.heatmapReady && this.heatmap) {
          try {
            if (typeof this.heatmap.setDataSet === 'function') {
              this.heatmap.setDataSet({
                data: heatData,
                max: 50  // 降低最大值
              });
            } else if (typeof this.heatmap.setData === 'function') {
              this.heatmap.setData({
                data: heatData,
                max: 50
              });
            } else if (typeof this.heatmap.setPoints === 'function') {
              this.heatmap.setPoints(heatData);
            }
            this.heatmap.show();
          } catch (error) {
            console.error('设置热力图数据失败：', error);
          }
        }
      } else {
        this.markers.forEach(m => m.show());
        if (this.heatmap) this.heatmap.hide();
      }
    },

    // 【修改】更新 drawParkingAreas 方法以处理动态数据
    drawParkingAreas() {
      const infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });
      // 遍历从API获取的新数据进行绘制
      this.parkingAreas.forEach(area => {
        const polygon = new window.AMap.Polygon({
          // 使用转换后的 polygonPath 字段
          path: area.polygonPath,
          fillColor: "#FFD600",
          fillOpacity: 0.2,
          strokeColor: "#FFD600",
          strokeWeight: 2,
          zIndex: 40,
          cursor: "pointer"
        });
        
        // 根据showParkingAreas状态决定是否显示
        if (this.showParkingAreas) {
          this.map.add(polygon);
        }
        
        // 将新创建的多边形存起来，方便下次清除
        this.parkingPolygons.push(polygon);

        polygon.on("mouseover", (e) => {
          // 使用 geohash 作为区域标识
          infoWindow.setContent(`
            <div style="min-width:160px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
              <b>停车区域：</b>${area.geohash}
            </div>`);
          infoWindow.open(this.map, e.lnglat);
        });
        polygon.on("mouseout", () => infoWindow.close());
        
        // 添加点击事件来获取停车区域信息
        polygon.on("click", () => {
          this.selectedParkingArea = area.geohash;
          this.fetchVehicleUtilization(area.geohash);
        });
      });
    },
    
    // 获取车辆使用率数据
    async fetchVehicleUtilization(geohash) {
      try {
        console.log('获取车辆使用率数据，区域编号:', geohash);
        const response = await getBikeUtilization(geohash);
        console.log('车辆使用率API响应:', response);
        
        // 直接使用返回的数据，因为这个API直接返回数据对象
        if (response && typeof response === 'object') {
          this.vehicleData = {
            totalBikes: response.totalBikes || 0,
            utilization: response.utilization || 0,
            availableBikes: response.availableBikes || 0,
            inUseBikes: response.inUseBikes || 0
          };
          console.log('更新车辆数据:', this.vehicleData);
        } else {
          console.warn('获取车辆使用率失败:', response);
          // 设置默认数据
          this.vehicleData = {
            totalBikes: 0,
            utilization: 0,
            availableBikes: 0,
            inUseBikes: 0
          };
        }
      } catch (error) {
        console.error('获取车辆使用率出错:', error);
        // 设置默认数据
        this.vehicleData = {
          totalBikes: 0,
          utilization: 0,
          availableBikes: 0,
          inUseBikes: 0
        };
      }
    },
    
    handleProfileSaved(formData) {
      console.log('个人资料已保存:', formData);
    },
    // 优化地图中心点设置方法
    setMapCenter(coordinates) {
      if (!this.map) return;
      
      // 设置更快的动画速度
      this.map.setStatus({
        animateEnable: true,
        animateDuration: 300  // 减少动画时间到300毫秒
      });
      
      // 使用更快的动画速度设置中心点和缩放级别
      this.map.setZoomAndCenter(
        this.defaultZoom,
        coordinates,
        true,  // 启用动画
        300    // 动画持续时间（毫秒）
      );
    },

    // 优化位置更新方法
    updateLocation() {
      // 只在选择深圳市福田区时处理特定位置
      if (this.selectedCity === "深圳市" && this.selectedDistrict === "福田区" && this.selectedRoad) {
        const locationInfo = this.locationCoordinates[this.selectedRoad];
        if (locationInfo) {
          // 立即更新地图位置
          this.setMapCenter(locationInfo.center);
          
          // 使用 requestAnimationFrame 延迟加载其他数据，避免卡顿
          requestAnimationFrame(() => {
            // 更新天气数据
            this.weatherData = {
              temperature: 28 + Math.floor(Math.random() * 5),
              description: ["多云", "晴", "小雨", "阴"][Math.floor(Math.random() * 4)],
              humidity: 60 + Math.floor(Math.random() * 20),
              windSpeed: 10 + Math.floor(Math.random() * 8),
              airQuality: ["优", "良", "轻度污染"][Math.floor(Math.random() * 3)]
            };
          });

          // 使用 Promise 和 setTimeout 优化数据加载
          Promise.resolve().then(() => {
                      setTimeout(() => {
            this.loadBicycles();
            this.loadParkingAreas();
          }, 400); // 等地图动画结束后再加载数据
          });
        }
      }
    },
    goHome() {
      this.$router.push('/admin');
    }
  }
};
</script>

<style scoped>
@import '@/assets/globalStyles.css';

.dashboard-view-root {
  position: relative;
  height: 100vh;
  overflow: hidden;
}

#mapContainer {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1;
}

.info-panel {
  position: fixed;
  top: 90px;
  left: 30px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 240px;
  max-width: 300px;
}

.location-selector h3,
.weather-card h3,
.vehicle-info-card h3 {
  margin: 0 0 6px 0;
  font-size: 1rem;
  font-weight: 700;
  color: #222;
}

.select-group {
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
  margin: 0 auto 0 auto;
  width: 100%;
}

.select-group select {
  flex: 1;
  min-width: 0;
  padding: 3px 6px;
  border-radius: 6px;
  border: 1px solid #eee;
  font-size: 0.9rem;
  background: #f7f7f7;
  outline: none;
  transition: border 0.2s;
  text-align: center;
  max-width: 80px;
}

.select-group select:focus {
  border: 1.5px solid #FFD600;
  background: #fffbe6;
}

.current-location {
  font-size: 0.9rem;
  color: #888;
  margin-top: 3px;
  text-align: center;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.weather-icon, .vehicle-icon {
  font-size: 1.2rem;
}

.weather-content, .vehicle-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weather-main {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.temperature {
  font-size: 1.8rem;
  font-weight: 700;
  color: #ff9800;
}

.weather-desc {
  font-size: 1rem;
  color: #666;
}

.weather-details {
  display: flex;
  gap: 10px;
  font-size: 0.9rem;
  color: #555;
}

.weather-item .label {
  color: #888;
}

/* 车辆信息面板样式 */
.parking-area-selector {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 8px;
}

.selector-label {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 4px;
  font-weight: 500;
}

.selected-area {
  font-size: 0.9rem;
  color: #333;
  font-weight: 600;
  min-height: 20px;
  display: flex;
  align-items: center;
}

.vehicle-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stats-main {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.stats-number {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2196f3;
}

.stats-label {
  font-size: 0.9rem;
  color: #666;
}

.stats-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.9rem;
  color: #555;
}

.stats-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-item .label {
  color: #888;
}

.stats-item .value.usage-rate {
  color: #FFD600;
  font-weight: 600;
}

.stats-item .value.available {
  color: #43a047;
  font-weight: 600;
}

.stats-item .value.in-use {
  color: #ff9800;
  font-weight: 600;
}

.top-right-controls {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 30;
  display: flex;
  align-items: flex-end;
}

.control-group {
  display: flex;
  gap: 4px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 10px;
  padding: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 214, 0, 0.15);
}

.control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 8px 10px;
  border: none;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.8);
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.7rem;
  font-weight: 500;
  min-width: 60px;
  backdrop-filter: blur(5px);
}

.control-btn:hover {
  background: rgba(255, 214, 0, 0.15);
  color: #333;
  transform: translateY(-1px);
}

.control-btn.active {
  background: #FFD600;
  color: #333;
  box-shadow: 0 2px 8px rgba(255, 214, 0, 0.3);
}

.btn-icon {
  font-size: 1rem;
  line-height: 1;
}

.btn-text {
  font-size: 0.65rem;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
  font-weight: 500;
}

@media (max-width: 900px) {
  .info-panel {
    left: 10px;
    top: 80px;
    min-width: 160px;
    max-width: 98vw;
    gap: 8px;
  }

  .select-group select {
    max-width: 50px;
    font-size: 0.85rem;
  }

  .top-right-controls {
    right: 10px;
  }
}
</style>