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
      <div class="feature-item" @click="handleFeature('ride')">骑车</div>
      <div class="feature-item" @click="handleFeature('heatmap')">热力图</div>
      <div class="feature-item" @click="toggleMapSettings">地图设置</div>
    </div>

    <!-- 热力图控制面板 -->
    <div v-if="showHeatmapPanel" class="heatmap-panel" :class="{ hidden: hideUI }">
      <div class="panel-header">
        <h3>热力图控制</h3>
        <div class="header-buttons">
          <el-button type="text" @click="closeHeatmapPanel" class="close-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
            关闭
          </el-button>
        </div>
      </div>
      <div class="panel-content">
        <!-- 热力图类型选择 -->
        <div class="heatmap-type-section">
          <h4>热力图类型</h4>
          <div class="type-options">
            <div class="type-option" 
                 :class="{ active: heatmapType === 'current' }"
                 @click="selectHeatmapType('current')">
              <div class="type-icon">📍</div>
              <div class="type-content">
                <div class="type-title">当前分布</div>
                <div class="type-desc">显示当前单车分布热力图</div>
              </div>
            </div>
            <div class="type-option" 
                 :class="{ active: heatmapType === 'prediction' }"
                 @click="selectHeatmapType('prediction')">
              <div class="type-icon">🔮</div>
              <div class="type-content">
                <div class="type-title">预测分布</div>
                <div class="type-desc">显示指定时间的预测热力图</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 预测时间选择（仅在预测模式下显示） -->
        <div v-if="heatmapType === 'prediction'" class="prediction-time-section">
          <h4>预测时间设置</h4>
          <div class="time-inputs">
            <div class="input-group">
              <label>日期：</label>
              <el-date-picker
                v-model="predictionDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="small"
                style="width: 100%;"
              />
            </div>
            <div class="input-group">
              <label>时间：</label>
              <el-select
                v-model="predictionHour"
                placeholder="选择小时"
                size="small"
                style="width: 100%;"
              >
                <el-option
                  v-for="hour in 24"
                  :key="hour - 1"
                  :label="`${hour - 1}:00`"
                  :value="hour - 1"
                />
              </el-select>
            </div>
          </div>
          <div class="prediction-actions">
            <el-button
              type="primary"
              size="small"
              :loading="isLoadingPrediction"
              @click="loadPredictionHeatmap"
            >
              {{ isLoadingPrediction ? '加载中...' : '加载预测数据' }}
            </el-button>
          </div>
        </div>

        <!-- 热力图控制按钮 -->
        <div class="heatmap-controls">
          <h4>热力图控制</h4>
          <div class="control-buttons">
            <el-button
              type="success"
              size="small"
              @click="showHeatmap = true"
              :disabled="showHeatmap"
            >
              显示热力图
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="showHeatmap = false"
              :disabled="!showHeatmap"
            >
              隐藏热力图
            </el-button>
          </div>
        </div>

        <!-- 当前状态显示 -->
        <div class="status-section">
          <h4>当前状态</h4>
          <div class="status-info">
            <div class="status-item">
              <span class="status-label">热力图类型：</span>
              <span class="status-value">{{ heatmapType === 'current' ? '当前分布' : '预测分布' }}</span>
            </div>
            <div v-if="heatmapType === 'prediction'" class="status-item">
              <span class="status-label">预测时间：</span>
              <span class="status-value">{{ predictionDate }} {{ predictionHour }}:00</span>
            </div>
            <div class="status-item">
              <span class="status-label">显示状态：</span>
              <span class="status-value" :class="{ 'active': showHeatmap }">
                {{ showHeatmap ? '已显示' : '已隐藏' }}
              </span>
            </div>
          </div>
        </div>
      </div>
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
      :showRide="showRide"
      :showHeatmap="showHeatmap"
      :userInfo="userInfo"
      :authToken="authToken"
      :unfinishedRideOrders="unfinishedRideOrders"
      @update:showNavigation="showNavigation = $event"
      @update:showRide="showRide = $event"
      @user-data-updated="handleUserDataUpdated"
      ref="mapComponentRef"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UserMapComponent from '@/components/map/UserMapComponent.vue';
import { getUserProfile } from '@/api/account/profile.js';
import { getCurrentRideOrders } from '@/api/riding.js';
import { getHeatMapPredictionData, convertPredictionHeatMapData } from '@/api/map/heat.js';

const router = useRouter()

// 用户登录/注册成功后的完整响应数据
const userLoginData = ref(null)

// 设置用户登录数据的函数
const setUserLoginData = (responseData) => {
  userLoginData.value = responseData
  // 同时更新现有的用户信息变量
  if (responseData && responseData.data) {
    authToken.value = responseData.data.token
    userInfo.value = responseData.data.userInfo
    userRole.value = responseData.data.role || 'user'
  }
}

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
  console.log('UserView.vue - 开始加载用户信息')
  
  // 优先从sessionStorage读取完整的用户登录数据
  const storedUserLoginData = sessionStorage.getItem('userLoginData')
  
  if (storedUserLoginData && storedUserLoginData !== 'undefined' && storedUserLoginData !== 'null') {
    try {
      const loginData = JSON.parse(storedUserLoginData)
      console.log('从userLoginData读取到数据:', loginData)
      setUserLoginData(loginData)
    } catch (e) {
      console.error('解析完整用户登录数据失败:', e)
      // 清除无效的sessionStorage数据
      sessionStorage.removeItem('userLoginData')
    }
  }
  
  // 如果没有完整数据，则从分散的存储中读取（向后兼容）
  if (!userLoginData.value) {
    console.log('没有完整登录数据，尝试读取分散存储的数据')
    authToken.value = sessionStorage.getItem('authToken') || ''
    const storedUserInfo = sessionStorage.getItem('userInfo')
    
    console.log('从sessionStorage读取:')
    console.log('- authToken:', authToken.value)
    console.log('- userInfo (raw):', storedUserInfo)
    
    // 修复JSON解析错误 - 检查是否为有效的JSON字符串
    if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
        console.log('解析后的userInfo:', userInfo.value)
      } catch (e) {
        console.error('解析用户信息失败:', e)
        userInfo.value = null
        // 清除无效的sessionStorage数据
        sessionStorage.removeItem('userInfo')
      }
    } else {
      console.warn('sessionStorage中的userInfo为空或无效:', storedUserInfo)
    }
    
    userRole.value = sessionStorage.getItem('userRole') || ''
    console.log('- userRole:', userRole.value)
  }
  
  // 如果没有token，重定向到登录页
  if (!authToken.value || authToken.value === 'undefined') {
    console.error('没有有效的authToken，重定向到登录页')
    router.push('/login')
    return
  }
  
  // 检查用户角色是否为user
  if (userRole.value !== 'user') {
    console.error('用户角色不匹配:', userRole.value)
    alert('权限不足，请使用普通用户账号登录')
    router.push('/login')
    return
  }
  
  console.log('UserView.vue - 用户信息加载完成')
  console.log('最终userInfo:', userInfo.value)
  console.log('最终authToken:', authToken.value)
  
  // 检查用户是否有未完成的骑行记录
  checkUnfinishedRideOrders()
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
const showRide = ref(false);
const showHeatmap = ref(false);

// 热力图控制相关
const showHeatmapPanel = ref(false);
const heatmapType = ref('current'); // 'current' 或 'prediction'
const predictionDate = ref(new Date().toISOString().split('T')[0]); // 默认为今天
const predictionHour = ref(new Date().getHours()); // 默认为当前小时
const isLoadingPrediction = ref(false);

// 未完成骑行记录状态
const unfinishedRideOrders = ref(null);

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
  if (feature === 'ride') {
    showRide.value = !showRide.value;
    return;
  }
  if (feature === 'heatmap') {
    showHeatmapPanel.value = !showHeatmapPanel.value;
    return;
  }
};

// 热力图相关方法
const closeHeatmapPanel = () => {
  showHeatmapPanel.value = false;
};

const selectHeatmapType = (type) => {
  heatmapType.value = type;
  if (type === 'current') {
    // 切换到当前分布时，隐藏热力图面板，直接显示当前热力图
    showHeatmapPanel.value = false;
    showHeatmap.value = true;
    
    // 通知地图组件切换到当前热力图
    if (mapComponentRef.value) {
      mapComponentRef.value.switchToCurrentHeatmap();
    }
  }
};

const loadPredictionHeatmap = async () => {
  if (!predictionDate.value || predictionHour.value === null) {
    ElMessage.warning('请选择预测日期和时间');
    return;
  }

  isLoadingPrediction.value = true;
  
  try {
    // 调用预测热力图API
    const response = await getHeatMapPredictionData({
      reportDateStr: predictionDate.value,
      predictionTimeHour: predictionHour.value
    });

    if (response.code === '200' || response.code === 200) {
      // 转换预测数据为热力图格式
      const heatmapData = convertPredictionHeatMapData(response.data);
      
      // 通知地图组件更新预测热力图数据
      if (mapComponentRef.value) {
        mapComponentRef.value.updatePredictionHeatmap(heatmapData);
      }
      
      // 显示热力图
      showHeatmap.value = true;
      showHeatmapPanel.value = false;
      
      ElMessage.success('预测热力图加载成功');
    } else {
      ElMessage.error(response.msg || '加载预测数据失败');
    }
  } catch (error) {
    console.error('加载预测热力图失败:', error);
    ElMessage.error('加载预测数据失败，请稍后重试');
  } finally {
    isLoadingPrediction.value = false;
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
  console.log('开始获取用户个人信息')
  console.log('使用的token:', authToken.value)
  
  if (!authToken.value) {
    console.error('Token为空，无法获取个人信息')
    profileError.value = '未找到认证令牌，请重新登录';
    return;
  }

  profileLoading.value = true;
  profileError.value = '';
  
  try {
    console.log('调用getUserProfile API...')
    const response = await getUserProfile(authToken.value);
    console.log('getUserProfile API 响应:', response)
    
    if (response.code === 200 || response.code === '200') {
      console.log('个人信息获取成功, 数据:', response.data)
      profileData.value = response.data;
    } else {
      console.error('个人信息获取失败, 错误码:', response.code, '错误信息:', response.msg)
      profileError.value = response.msg || '获取个人信息失败';
    }
  } catch (error) {
    console.error('获取个人信息时发生异常:', error);
    console.error('错误详情:', {
      message: error.message,
      stack: error.stack,
      response: error.response
    })
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

// 处理用户数据更新事件
const handleUserDataUpdated = (updatedUserData) => {
  console.log('接收到用户数据更新:', updatedUserData);
  
  // 更新本地存储的用户信息
  if (userInfo.value) {
    userInfo.value.totalRides = updatedUserData.totalRides;
    userInfo.value.totalDurationMinutes = updatedUserData.totalDurationMinutes;
    userInfo.value.totalCost = updatedUserData.totalCost;
    
    // 同步更新sessionStorage中的用户信息
    sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value));
    
    // 如果存在完整的用户登录数据，也需要更新
    if (userLoginData.value && userLoginData.value.data && userLoginData.value.data.userInfo) {
      userLoginData.value.data.userInfo.totalRides = updatedUserData.totalRides;
      userLoginData.value.data.userInfo.totalDurationMinutes = updatedUserData.totalDurationMinutes;
      userLoginData.value.data.userInfo.totalCost = updatedUserData.totalCost;
      sessionStorage.setItem('userLoginData', JSON.stringify(userLoginData.value));
    }
    
    console.log('用户信息已更新到本地存储');
  }
};

// 检查用户未完成的骑行记录
const checkUnfinishedRideOrders = async () => {
  if (!userInfo.value || !userInfo.value.userid) {
    console.log('用户信息不完整，跳过未完成骑行检查');
    return;
  }

  try {
    console.log('开始检查用户未完成骑行记录, 用户ID:', userInfo.value.userid);
    
    const response = await getCurrentRideOrders(userInfo.value.userid);
    console.log('未完成骑行记录API响应:', response);

    // 判断是否有未完成记录：检查data是否为null
    if (response.data !== null && Array.isArray(response.data) && response.data.length > 0) {
      console.log('发现未完成骑行记录:', response.data);
      
      // 存储未完成骑行记录
      unfinishedRideOrders.value = response.data;
      
      // 自动开启骑车功能并设置为收起状态
      showRide.value = true;
      
      console.log('已自动开启骑车功能，用户有', response.data.length, '个未完成的骑行记录');
    } else {
      console.log('没有发现未完成的骑行记录');
      unfinishedRideOrders.value = null;
    }
  } catch (error) {
    console.error('检查未完成骑行记录失败:', error);
    // 不显示错误信息给用户，静默处理
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

/* 热力图控制面板样式 */
.heatmap-panel {
  position: absolute;
  top: 80px;
  left: 100px;
  width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  max-height: 80vh;
  overflow-y: auto;
}

.heatmap-panel.hidden {
  display: none;
}

.heatmap-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.heatmap-panel .panel-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.heatmap-panel .panel-content {
  padding: 20px;
}

.heatmap-type-section,
.prediction-time-section,
.heatmap-controls,
.status-section {
  margin-bottom: 24px;
}

.heatmap-type-section h4,
.prediction-time-section h4,
.heatmap-controls h4,
.status-section h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.type-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-option {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.type-option:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
}

.type-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.type-icon {
  font-size: 1.5rem;
  margin-right: 12px;
}

.type-content {
  flex: 1;
}

.type-title {
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 0.85rem;
  opacity: 0.8;
}

.time-inputs {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-group label {
  font-weight: 500;
  color: #555;
  font-size: 0.9rem;
}

.prediction-actions {
  margin-top: 16px;
  text-align: center;
}

.control-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.status-label {
  font-weight: 500;
  color: #666;
  font-size: 0.9rem;
}

.status-value {
  font-weight: 600;
  color: #333;
  font-size: 0.9rem;
}

.status-value.active {
  color: #28a745;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .heatmap-panel {
    width: 95vw;
    left: 2.5vw;
    top: 60px;
  }
  
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