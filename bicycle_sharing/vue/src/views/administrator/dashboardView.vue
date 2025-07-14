<template>
  <div class="dashboard-view-root">
    <div id="mapContainer"></div>

    <MenuComponent @profile-saved="handleProfileSaved" />

    <div class="info-panel">
      <div class="location-selector info-card">
        <h3>地区选择</h3>
        <div class="select-group">
          <select v-model="selectedCity" @change="updateLocation">
            <option value="">选择城市</option>
            <option value="深圳市">深圳市</option>
            <option value="广州市">广州市</option>
            <option value="北京市">北京市</option>
            <option value="上海市">上海市</option>
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

      <div class="bike-stats-card info-card">
        <div class="card-header">
          <h3>车辆统计</h3>
          <span class="stats-icon">🚲</span>
        </div>
        <div class="stats-content">
          <div class="stats-main">
            <div class="stats-number">{{ bikeStats.totalBikes }}</div>
            <div class="stats-label">总车辆数</div>
          </div>
          <div class="stats-details">
            <div class="stats-item">
              <span class="label">正常：</span>
              <span class="value normal">{{ bikeStats.normalBikes }}</span>
            </div>
            <div class="stats-item">
              <span class="label">故障：</span>
              <span class="value fault">{{ bikeStats.faultBikes }}</span>
            </div>
            <div class="stats-item">
              <span class="label">维修中：</span>
              <span class="value repair">{{ bikeStats.repairBikes }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="usage-card info-card">
        <div class="card-header">
          <h3>使用率</h3>
          <span class="usage-icon">📊</span>
        </div>
        <div class="usage-content">
          <div class="usage-main">
            <div class="usage-circle">
              <div class="usage-percentage">{{ usageData.usageRate }}%</div>
              <div class="usage-label">当前使用率</div>
            </div>
          </div>
          <div class="usage-details">
            <div class="usage-item">
              <span class="label">在线车辆：</span>
              <span class="value">{{ usageData.onlineBikes }}</span>
            </div>
            <div class="usage-item">
              <span class="label">使用中：</span>
              <span class="value">{{ usageData.inUseBikes }}</span>
            </div>
            <div class="usage-item">
              <span class="label">空闲：</span>
              <span class="value">{{ usageData.idleBikes }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="top-right-btn-group btn-group">
      <button class="yellow-btn" @click="onToggleBikes">
        {{ showBikes ? '隐藏单车' : '显示单车' }}
      </button>
      <button class="yellow-btn" @click="onToggleHeatmap">
        {{ showHeatmap ? '显示普通地图' : '显示热力图' }}
      </button>
      <button class="yellow-btn" @click="goHome">
        返回主页
      </button>
    </div>
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue'
import { mapMixin } from '@/utils/mapMixin.js'
import AMapLoader from '@/utils/loadAMap.js'
import bicycleIcon from '@/components/icons/bicycle.png';
import { getMapAreaBicycles } from '@/api/map/bicycle';
// 【新增】导入停车区域相关的API函数
import { getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking.js';

export default {
  name: "DashboardView",
  components: {
    MenuComponent
  },
  mixins: [mapMixin],
  data() {
    return {
      selectedCity: "深圳市",
      selectedDistrict: "福田区",
      selectedRoad: "福华三路",
      weatherData: {
        temperature: 28,
        description: "多云",
        humidity: 65,
        windSpeed: 12,
        airQuality: "优"
      },
      bikeStats: {
        totalBikes: 1200,
        normalBikes: 1100,
        faultBikes: 60,
        repairBikes: 40
      },
      usageData: {
        usageRate: 76,
        onlineBikes: 1000,
        inUseBikes: 760,
        idleBikes: 240
      },
      cityDistrictRoad: {
        "深圳市": {
          "福田区": ["福华三路", "金田路", "滨河大道"],
          "南山区": ["科技园", "深南大道", "南海大道"]
        },
        "广州市": { "天河区": ["体育西路", "珠江新城"], "越秀区": ["中山路", "北京路"] },
        "北京市": { "朝阳区": ["建国路", "三里屯"], "海淀区": ["中关村", "学院路"] },
        "上海市": { "浦东新区": ["世纪大道", "张江路"], "徐汇区": ["漕溪北路", "肇嘉浜路"] }
      },
      // 【修改】初始化为空数组
      parkingAreas: [],
      parkingPolygons: [],
      bikes: [],
      showBikes: true,
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
      this.initMap();
      this.map.setZoomAndCenter(17, [114.0580, 22.5390]);

      // 【修改】调用新的方法来加载真实数据
      this.loadBicycles();
      this.showParkingAreas(); // 使用新的主方法

      // 【修改】添加对停车区域的动态加载
      this.map.on('moveend', () => {
        this.loadBicycles();
        this.showParkingAreas();
      });
      this.map.on('zoomend', () => {
        this.loadBicycles();
        this.showParkingAreas();
      });

    }).catch(err => {
      this.$message && this.$message.error
          ? this.$message.error('地图加载失败: ' + err.message)
          : alert('地图加载失败: ' + err.message);
    });
  },
  beforeUnmount() {
    if (this.map) {
      // 【修改】移除所有监听器
      this.map.off('moveend', this.loadBicycles);
      this.map.off('zoomend', this.loadBicycles);
      this.map.off('moveend', this.showParkingAreas);
      this.map.off('zoomend', this.showParkingAreas);
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
    async showParkingAreas() {
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
      this.toggleHeatmap(this.bikes);

      if (!this.showHeatmap && !this.showBikes) {
        this.markers.forEach(marker => marker.hide());
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
        this.map.add(polygon);
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
      });
    },
    handleProfileSaved(formData) {
      console.log('个人资料已保存:', formData);
    },
    updateLocation() {
      this.weatherData = {
        temperature: 28 + Math.floor(Math.random() * 5),
        description: ["多云", "晴", "小雨", "阴"][Math.floor(Math.random() * 4)],
        humidity: 60 + Math.floor(Math.random() * 20),
        windSpeed: 10 + Math.floor(Math.random() * 8),
        airQuality: ["优", "良", "轻度污染"][Math.floor(Math.random() * 3)]
      };
      this.bikeStats = {
        totalBikes: 1000 + Math.floor(Math.random() * 500),
        normalBikes: 900 + Math.floor(Math.random() * 100),
        faultBikes: 30 + Math.floor(Math.random() * 40),
        repairBikes: 20 + Math.floor(Math.random() * 30)
      };
      this.usageData = {
        usageRate: 60 + Math.floor(Math.random() * 30),
        onlineBikes: 800 + Math.floor(Math.random() * 200),
        inUseBikes: 500 + Math.floor(Math.random() * 300),
        idleBikes: 200 + Math.floor(Math.random() * 100)
      };
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
.bike-stats-card h3,
.usage-card h3 {
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

.weather-icon, .stats-icon, .usage-icon {
  font-size: 1.2rem;
}

.weather-content, .stats-content, .usage-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
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

.stats-main {
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
  gap: 10px;
  font-size: 0.9rem;
  color: #555;
}

.stats-item .label {
  color: #888;
}

.stats-item .value.normal {
  color: #43a047;
}

.stats-item .value.fault {
  color: #e53935;
}

.stats-item .value.repair {
  color: #ffb300;
}

.usage-main {
  display: flex;
  align-items: center;
  justify-content: center;
}

.usage-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.usage-percentage {
  font-size: 1.8rem;
  font-weight: 700;
  color: #FFD600;
}

.usage-label {
  font-size: 0.9rem;
  color: #666;
}

.usage-details {
  display: flex;
  gap: 10px;
  font-size: 0.9rem;
  color: #555;
  justify-content: center;
}

.usage-item .label {
  color: #888;
}

.top-right-btn-group {
  position: fixed;
  top: 20px;
  right: 30px;
  z-index: 30;
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

  .top-right-btn-group {
    right: 10px;
  }
}
</style>