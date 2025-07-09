<template>
  <div class="dashboard-view-root">
    <div id="mapContainer"></div>

    <!-- 使用菜单组件 -->
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
import MenuComponent from '@/views/administrator/component/MenuComponent.vue'
import { mapMixin } from '@/utils/mapMixin.js'

export default {
  name: "DashboardView",
  components: {
    MenuComponent
  },
  mixins: [mapMixin],
  data() {
    return {
      // 地区选择
      selectedCity: "深圳市",
      selectedDistrict: "福田区",
      selectedRoad: "福华三路",
      // 天气数据
      weatherData: {
        temperature: 28,
        description: "多云",
        humidity: 65,
        windSpeed: 12,
        airQuality: "优"
      },
      // 车辆统计
      bikeStats: {
        totalBikes: 1200,
        normalBikes: 1100,
        faultBikes: 60,
        repairBikes: 40
      },
      // 使用率
      usageData: {
        usageRate: 76,
        onlineBikes: 1000,
        inUseBikes: 760,
        idleBikes: 240
      },
      // 地区数据
      cityDistrictRoad: {
        "深圳市": {
          "福田区": ["福华三路", "金田路", "滨河大道"],
          "南山区": ["科技园", "深南大道", "南海大道"]
        },
        "广州市": {
          "天河区": ["体育西路", "珠江新城", "天河北路"],
          "越秀区": ["中山路", "北京路", "东风路"]
        },
        "北京市": {
          "朝阳区": ["建国路", "三里屯", "望京"],
          "海淀区": ["中关村", "学院路", "知春路"]
        },
        "上海市": {
          "浦东新区": ["世纪大道", "张江路", "花木路"],
          "徐汇区": ["漕溪北路", "肇嘉浜路", "虹桥路"]
        }
      },
      // 自行车数据
      bikeList: [
        {id: "SZ1001", lng: 114.057868, lat: 22.53445, status: "正常", address: "深圳市-福田区-福华三路"},
        {id: "SZ1002", lng: 114.060868, lat: 22.53495, status: "故障", address: "深圳市-福田区-金田路"},
        {id: "SZ1003", lng: 114.058868, lat: 22.53645, status: "待维修", address: "深圳市-福田区-滨河大道"},
        {id: "SZ1004", lng: 114.061868, lat: 22.53445, status: "正常", address: "深圳市-福田区-会展中心"},
        {id: "SZ1005", lng: 114.061867, lat: 22.53545, status: "正常", address: "深圳市-福田区-福华一路"},
        {id: "SZ1006", lng: 114.057000, lat: 22.53400, status: "正常", address: "深圳市-福田区-福华三路附近"},
        {id: "SZ1007", lng: 114.058500, lat: 22.53500, status: "正常", address: "深圳市-福田区-金田路附近"},
        {id: "SZ1008", lng: 114.059500, lat: 22.53600, status: "故障", address: "深圳市-福田区-滨河大道附近"},
        {id: "SZ1009", lng: 114.060500, lat: 22.53400, status: "正常", address: "深圳市-福田区-会展中心附近"},
        {id: "SZ1010", lng: 114.062500, lat: 22.53500, status: "待维修", address: "深圳市-福田区-福华一路附近"},
        {id: "SZ1011", lng: 114.057200, lat: 22.53460, status: "正常", address: "深圳市-福田区-中心区"},
        {id: "SZ1012", lng: 114.060200, lat: 22.53520, status: "正常", address: "深圳市-福田区-益田路"},
        {id: "SZ1013", lng: 114.058000, lat: 22.53680, status: "正常", address: "深圳市-福田区-彩田路"},
        {id: "SZ1014", lng: 114.061000, lat: 22.53480, status: "故障", address: "深圳市-福田区-民田路"},
        {id: "SZ1015", lng: 114.061500, lat: 22.53580, status: "正常", address: "深圳市-福田区-福中路"},
        {id: "SZ1016", lng: 114.059000, lat: 22.53420, status: "正常", address: "深圳市-福田区-新闻路"},
        {id: "SZ1017", lng: 114.058200, lat: 22.53540, status: "正常", address: "深圳市-福田区-景田路"},
        {id: "SZ1018", lng: 114.060000, lat: 22.53620, status: "待维修", address: "深圳市-福田区-华强北"},
        {id: "SZ1019", lng: 114.062000, lat: 22.53460, status: "正常", address: "深圳市-福田区-八卦岭"},
        {id: "SZ1020", lng: 114.062200, lat: 22.53560, status: "正常", address: "深圳市-福田区-上步路"}
      ]
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
    // 使用混入中的地图初始化方法
    const {yellowBikeIcon} = this.initMap();
    this.addBikeMarkers(this.bikeList, yellowBikeIcon);
  },
  methods: {
    handleProfileSaved(formData) {
      console.log('个人资料已保存:', formData);
    },
    updateLocation() {
      // 更新天气、车辆等数据
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
    onToggleHeatmap() {
      // 使用混入中的热力图切换方法
      this.toggleHeatmap(this.bikeList);
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

.info-panel {
  position: fixed;
  top: 90px; /* 调整位置避免与菜单重叠 */
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