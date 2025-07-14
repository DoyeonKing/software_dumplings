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
      <div class="feature-item" @click="handleFeature('heatmap')">热力图</div>
      <div class="feature-item" @click="toggleMapSettings">地图设置</div>
    </div>

    <!-- 地图设置面板 -->
    <div class="map-settings" v-if="showMapSettings" :class="{ hidden: hideUI }">
      <div class="settings-header">
        <h3>地图设置</h3>
        <div class="close-button" @click="toggleMapSettings">×</div>
      </div>
      <div class="settings-content">
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
        <div class="dropdown-item" @click="showProfileModal">个人信息</div>
        <router-link to="/login" class="dropdown-item">切换账号</router-link>
      </div>
    </div>

    <!-- 个人信息弹窗 -->
    <div v-if="showProfile" class="profile-modal-overlay" @click="closeProfileModal">
      <div class="profile-modal" @click.stop>
        <div class="profile-card">
          <div class="profile-header">
            <div class="profile-avatar">
              <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath fill='%23666' d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z'/%3E%3C/svg%3E" alt="用户头像" />
            </div>
            <div>
              <div class="profile-name">{{ profileData?.username || '用户' }}</div>
              <div class="profile-username">用户ID：{{ profileData?.userid || '-' }}</div>
            </div>
            <button class="close-btn" @click="closeProfileModal">×</button>
          </div>

          <div v-if="profileLoading" class="loading-section">
            <div class="loading-spinner"></div>
            <p>正在加载个人信息...</p>
          </div>

          <div v-else-if="profileData" class="profile-content">
            <!-- 用户基本信息 -->
            <div class="info-section">
              <h4>用户基本信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">用户ID</span>
                  <span class="info-value primary">{{ profileData.userid }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">用户名</span>
                  <span class="info-value success">{{ profileData.username }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">手机号码</span>
                  <span class="info-value info">{{ profileData.phoneNumber }}</span>
                </div>
              </div>
            </div>

            <!-- 骑行统计信息 -->
            <div class="info-section">
              <h4>骑行统计信息</h4>
              <div class="stat-grid">
                <div class="stat-item">
                  <div class="stat-icon">🚴</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ profileData.totalRides }}</div>
                    <div class="stat-label">总骑行次数</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">⏱️</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ profileData.totalDurationMinutes }}</div>
                    <div class="stat-label">总骑行时长(分钟)</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">💰</div>
                  <div class="stat-content">
                    <div class="stat-value">¥{{ profileData.totalCost?.toFixed(2) }}</div>
                    <div class="stat-label">总消费金额</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 数据统计卡片 -->
            <div class="info-section">
              <h4>平均数据统计</h4>
              <div class="stat-cards">
                <div class="stat-card">
                  <div class="stat-card-value">{{ getAverageRideDuration() }}</div>
                  <div class="stat-card-label">平均每次骑行时长(分钟)</div>
                </div>
                <div class="stat-card">
                  <div class="stat-card-value">¥{{ getAverageCost() }}</div>
                  <div class="stat-card-label">平均每次消费</div>
                </div>
                <div class="stat-card">
                  <div class="stat-card-value">¥{{ getCostPerMinute() }}</div>
                  <div class="stat-card-label">平均每分钟费用</div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="error-section">
            <p>{{ profileError || '获取个人信息失败' }}</p>
            <button class="retry-btn" @click="fetchUserProfile">重试</button>
          </div>

          <div class="button-row">
            <button class="action-btn" @click="closeProfileModal">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <UserMapComponent 
      :hideUI="hideUI" 
      :mapType="mapType"
      :mapStyle="currentMapStyle"
      :showBicycles="showBicycles"
      :showParkingAreas="showParkingAreas"
      :showNavigation="showNavigation"
      :showHeatmap="showHeatmap"
      @update:showNavigation="showNavigation = $event"
      ref="mapComponentRef"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import UserMapComponent from '@/components/map/UserMapComponent.vue';
import { getUserProfile } from '@/api/account/profile.js';

const router = useRouter()

// 用户认证信息
const authToken = ref('')
const userInfo = ref(null)
const userRole = ref('')

// 个人信息弹窗相关
const showProfile = ref(false)
const profileData = ref(null)
const profileLoading = ref(false)
const profileError = ref('')

// 获取存储的认证信息
onMounted(() => {
  authToken.value = sessionStorage.getItem('authToken') || ''
  const storedUserInfo = sessionStorage.getItem('userInfo')
  
  // 修复JSON解析错误 - 检查是否为有效的JSON字符串
  if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
    try {
      userInfo.value = JSON.parse(storedUserInfo)
    } catch (e) {
      console.error('解析用户信息失败:', e)
      userInfo.value = null
      // 清除无效的sessionStorage数据
      sessionStorage.removeItem('userInfo')
    }
  }
  
  userRole.value = sessionStorage.getItem('userRole') || ''
  
  // 如果没有token，重定向到登录页
  if (!authToken.value || authToken.value === 'undefined') {
    router.push('/login')
    return
  }
  
  // 检查用户角色是否为user
  if (userRole.value !== 'user') {
    alert('权限不足，请使用普通用户账号登录')
    router.push('/login')
    return
  }
})

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
const showHeatmap = ref(false);

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
  if (feature === 'heatmap') {
    showHeatmap.value = !showHeatmap.value;
    return;
  }
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

// 个人信息弹窗相关方法
const showProfileModal = () => {
  showProfile.value = true;
  showDropdown.value = false; // 关闭下拉菜单
  fetchUserProfile();
};

const closeProfileModal = () => {
  showProfile.value = false;
  profileData.value = null;
  profileError.value = '';
};

const fetchUserProfile = async () => {
  if (!authToken.value) {
    profileError.value = '未找到认证令牌，请重新登录';
    return;
  }

  profileLoading.value = true;
  profileError.value = '';
  
  try {
    const response = await getUserProfile(authToken.value);
    
    if (response.code === 200 || response.code === '200') {
      profileData.value = response.data;
    } else {
      profileError.value = response.msg || '获取个人信息失败';
    }
  } catch (error) {
    console.error('获取个人信息失败:', error);
    profileError.value = '网络错误，请稍后重试';
  } finally {
    profileLoading.value = false;
  }
};

// 计算平均每次骑行时长
const getAverageRideDuration = () => {
  if (!profileData.value || profileData.value.totalRides === 0) return '0';
  return (profileData.value.totalDurationMinutes / profileData.value.totalRides).toFixed(1);
};

// 计算平均每次消费
const getAverageCost = () => {
  if (!profileData.value || profileData.value.totalRides === 0) return '0.00';
  return (profileData.value.totalCost / profileData.value.totalRides).toFixed(2);
};

// 计算平均每分钟费用
const getCostPerMinute = () => {
  if (!profileData.value || profileData.value.totalDurationMinutes === 0) return '0.000';
  return (profileData.value.totalCost / profileData.value.totalDurationMinutes).toFixed(3);
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

/* 个人信息弹窗样式 */
.profile-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(2px);
}

.profile-modal {
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.profile-card {
  width: 600px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  padding: 24px;
  position: relative;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.profile-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  border: 2px solid #4F6EF7;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
}

.profile-name {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.profile-username {
  font-size: 0.9rem;
  color: #666;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #ff3742;
}

.loading-section {
  text-align: center;
  padding: 40px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4F6EF7;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.profile-content {
  max-height: 60vh;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 24px;
}

.info-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-label {
  font-weight: 500;
  color: #666;
  font-size: 0.9rem;
}

.info-value {
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.info-value.primary {
  background: #e3f2fd;
  color: #1976d2;
}

.info-value.success {
  background: #e8f5e8;
  color: #2e7d32;
}

.info-value.info {
  background: #e0f7fa;
  color: #0097a7;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid #dee2e6;
}

.stat-icon {
  font-size: 2rem;
  margin-right: 12px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-card-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #4F6EF7;
  margin-bottom: 4px;
}

.stat-card-label {
  font-size: 0.75rem;
  color: #666;
  font-weight: 500;
}

.error-section {
  text-align: center;
  padding: 40px 20px;
  color: #d32f2f;
}

.retry-btn {
  background: #4F6EF7;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 12px;
}

.retry-btn:hover {
  background: #3d5af5;
}

.button-row {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.action-btn {
  padding: 10px 24px;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  background: #6c757d;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #5a6268;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-card {
    width: 95vw;
    padding: 20px;
  }
  .info-grid {
    grid-template-columns: 1fr;
  }
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .stat-cards {
    grid-template-columns: 1fr;
  }
}
</style> 