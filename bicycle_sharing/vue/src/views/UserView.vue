<template>
  <div class="dashboard">
    <!-- 功能按钮 -->
    <div class="feature-button" @click="toggleFeatureBar" :class="{ hidden: hideUI }">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="3" y1="12" x2="21" y2="12"></line>
        <line x1="3" y1="6" x2="21" y2="6"></line>
        <line x1="3" y1="18" x2="21" y2="18"></line>
      </svg>
    </div>

    <!-- 功能栏 -->
    <div class="feature-bar" :class="{ hidden: !showFeatureBar || hideUI }">
      <div class="feature-item" @click="handleFeature('bikes')">单车位置</div>
      <div class="feature-item" @click="handleFeature('stations')">停车点位置</div>
      <div class="feature-item" @click="handleFeature('navigation')">导航</div>
      <div class="feature-item" @click="handleFeature('return')">还车</div>
      <div class="feature-item" @click="toggleMapSettings">地图设置</div>
    </div>

    <!-- 地图设置面板 -->
    <div class="map-settings" v-if="showMapSettings" :class="{ hidden: hideUI }">
      <div class="settings-header">
        <h3>地图设置</h3>
        <div class="close-button" @click="toggleMapSettings">×</div>
      </div>
      <div class="settings-content">
        <!-- 地图类型 -->
        <div class="settings-section">
          <h4>地图类型</h4>
          <div class="radio-group">
            <label>
              <input type="radio" v-model="mapType" value="normal" @change="updateMapType">
              标准地图
            </label>
            <label>
              <input type="radio" v-model="mapType" value="satellite" @change="updateMapType">
              卫星地图
            </label>
          </div>
        </div>

        <!-- 地图样式 -->
        <div class="settings-section">
          <h4>地图样式</h4>
          <div class="style-options">
            <div class="style-option" 
                 v-for="style in mapStyles" 
                 :key="style.value"
                 :class="{ active: currentMapStyle === style.value }"
                 @click="updateMapStyle(style.value)">
              {{ style.label }}
            </div>
          </div>
        </div>

        <!-- 缩放控制 -->
        <div class="settings-section">
          <h4>缩放控制</h4>
          <div class="zoom-controls">
            <button @click="handleZoom('in')">放大</button>
            <button @click="handleZoom('out')">缩小</button>
          </div>
        </div>
      </div>
    </div>

    <div class="avatar-dropdown">
      <!-- 无UI按钮 -->
      <div class="no-ui-button" @click="toggleUI">
        <svg v-if="!hideUI" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
          <circle cx="12" cy="12" r="3"></circle>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
          <line x1="1" y1="1" x2="23" y2="23"></line>
        </svg>
      </div>
      <div class="avatar" @click="showDropdown = !showDropdown">
        <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath fill='%23666' d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z'/%3E%3C/svg%3E" alt="Avatar" />
      </div>
      <div v-if="showDropdown" class="dropdown-menu">
        <router-link to="/profile" class="dropdown-item">个人信息</router-link>
        <router-link to="/login" class="dropdown-item">切换账号</router-link>
      </div>
    </div>
    <UserMapComponent 
      :hideUI="hideUI" 
      :mapType="mapType"
      :mapStyle="currentMapStyle"
      :showBicycles="showBicycles"
      :showParkingAreas="showParkingAreas"
      :showNavigation="showNavigation"
      @update:showNavigation="showNavigation = $event"
      ref="mapComponentRef"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import UserMapComponent from '@/components/map/UserMapComponent.vue';

const showDropdown = ref(false);
const showFeatureBar = ref(false);
const hideUI = ref(false);
const showMapSettings = ref(false);
const mapType = ref('normal');
const currentMapStyle = ref('normal');
const mapComponentRef = ref(null);
const showBicycles = ref(false);
const showParkingAreas = ref(false);
const showNavigation = ref(false);

const mapStyles = [
  { label: '标准', value: 'normal' },
  { label: '幻影黑', value: 'dark' },
  { label: '月光银', value: 'light' },
  { label: '远山黛', value: 'whitesmoke' },
  { label: '草色青', value: 'fresh' },
  { label: '雅士灰', value: 'grey' }
];

const toggleFeatureBar = () => {
  showFeatureBar.value = !showFeatureBar.value;
};

const toggleUI = () => {
  hideUI.value = !hideUI.value;
  if (hideUI.value) {
    showFeatureBar.value = false;
    showMapSettings.value = false;
  }
};

const toggleMapSettings = () => {
  showMapSettings.value = !showMapSettings.value;
};

const handleFeature = (feature) => {
  if (feature === 'settings') {
    toggleMapSettings();
    return;
  }
  if (feature === 'bikes') {
    showBicycles.value = !showBicycles.value;
    return;
  }
  if (feature === 'stations') {
    showParkingAreas.value = !showParkingAreas.value;
    return;
  }
  if (feature === 'navigation') {
    showNavigation.value = !showNavigation.value;
    return;
  }
  // 这里添加各个功能的处理逻辑
  console.log('Selected feature:', feature);
};

const updateMapType = () => {
  // 地图类型更新会通过 prop 传递到 MapComponent
};

const updateMapStyle = (style) => {
  currentMapStyle.value = style;
};

const handleZoom = (type) => {
  if (!mapComponentRef.value) return;
  if (type === 'in') {
    mapComponentRef.value.zoomIn();
  } else {
    mapComponentRef.value.zoomOut();
  }
};
</script>

<style scoped>
.dashboard {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

/* 地图设置面板样式 */
.map-settings {
  position: absolute;
  top: 80px;
  left: 100px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 300px;
  z-index: 1000;
  transition: all 0.3s ease;
}

.map-settings.hidden {
  display: none;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.settings-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-button {
  cursor: pointer;
  font-size: 20px;
  color: #666;
}

.settings-content {
  padding: 16px;
}

.settings-section {
  margin-bottom: 20px;
}

.settings-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.style-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.style-option {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
}

.style-option:hover {
  background-color: #f5f5f5;
}

.style-option.active {
  background-color: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.zoom-controls {
  display: flex;
  gap: 8px;
}

.zoom-controls button {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.zoom-controls button:hover {
  background-color: #f5f5f5;
}

/* 原有样式保持不变 */
.feature-button {
  position: absolute;
  top: 20px;
  left: 40px;
  z-index: 1000;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.feature-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.feature-button.hidden {
  display: none;
}

.feature-bar {
  position: absolute;
  top: 20px;
  left: 100px;
  z-index: 1000;
  background: white;
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  padding: 8px;
  display: flex;
  gap: 8px;
  transition: all 0.3s ease;
  transform-origin: left center;
}

.feature-bar.hidden {
  transform: scaleX(0);
  opacity: 0;
}

.feature-item {
  padding: 8px 16px;
  border-radius: 16px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap;
}

.feature-item:hover {
  background-color: #f5f5fa;
}

.avatar-dropdown {
  position: absolute;
  top: 20px;
  right: 40px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.avatar img {
  width: 32px;
  height: 32px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 120px;
}

.dropdown-item {
  display: block;
  padding: 8px 16px;
  color: #333;
  text-decoration: none;
  transition: background-color 0.3s;
}

.dropdown-item:hover {
  background-color: #f5f5fa;
}

.no-ui-button {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.no-ui-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
</style> 