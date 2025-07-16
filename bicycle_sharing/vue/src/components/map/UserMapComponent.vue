 // 高德地图 API Keys 说明：
// 1. Web端 Key (key-webJS): 7a9ebfd8db9264a7f90b65369bd2970a
//    用于前端地图显示和基础交互功能（JavaScript API、地图组件等）
// 
// 2. Web服务 Key (key-web): 4c4409cdbe818ceb94f8660e2e111563
//    用于后端服务调用（路径规划、搜索、地理编码等），不要在前端直接使用

<template>
  <div class="map-container">
    <div id="map"></div>
    <!-- 定位按钮 -->
    <div class="location-button" @click="setUserPosition" :class="{ hidden: hideUI }">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="3"></circle>
        <path d="M19.94 11A8 8 0 0 0 13 4.06"></path>
        <path d="M12 2v2"></path>
        <path d="M12 20v2"></path>
        <path d="M4.06 11a8 8 0 0 0 0 2"></path>
        <path d="M20 12a8 8 0 0 1-.06 1"></path>
      </svg>
    </div>
    <!-- 导航控制面板 -->
    <div v-if="showNavigation && !isNavigationPanelCollapsed" class="navigation-panel" :class="{ hidden: hideUI }">
      <div class="panel-header">
        <h3>骑行导航</h3>
        <div class="header-buttons">
          <el-button type="text" @click="collapseNavigationPanel" class="collapse-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
            收起
          </el-button>
          <el-button type="text" @click="cancelNavigation">关闭</el-button>
        </div>
      </div>
      <div class="panel-content">
        <div class="location-inputs">
          <div class="start-point">
            <span>起点：</span>
            <el-button 
              size="small" 
              :type="isSelectingStart ? 'primary' : 'default'"
              @click="startSelectingPoint('start')"
            >
              {{ startPoint ? '已选择' : '在地图上选择' }}
            </el-button>
            <el-button 
              size="small"
              type="primary"
              plain
              @click="selectCurrentPositionAsStart"
            >
              选择当前位置
            </el-button>
          </div>
          <div class="end-point">
            <span>终点：</span>
            <el-button 
              size="small" 
              :type="isSelectingEnd ? 'primary' : 'default'"
              @click="startSelectingPoint('end')"
            >
              {{ endPoint ? '已选择' : '在地图上选择' }}
            </el-button>
          </div>
        </div>
        <div class="navigation-actions">
          <el-button 
            type="primary" 
            :disabled="!startPoint || !endPoint"
            @click="calculateRoute" 
          >
            开始导航
          </el-button>
          <el-button @click="clearRoute">清除路线</el-button>
        </div>
        <div v-if="routeInfo" class="route-info">
          <p>距离：{{ routeInfo.distance }}</p>
          <p>预计时间：{{ routeInfo.time }}分钟</p>
        </div>
        
        <!-- 模拟移动控制 -->
        <div class="simulation-controls">
          <h4>🚶‍♂️ 模拟移动</h4>
          <div class="simulation-settings">
            <div class="speed-control">
              <label>移动速度:</label>
              <el-input-number
                v-model="simulationSpeed"
                :min="1"
                :max="50"
                :step="1"
                size="small"
                :disabled="isSimulating"
                style="width: 80px;"
              />
              <span style="margin-left: 5px; font-size: 12px; color: #666;">km/h</span>
            </div>
          </div>
          <div class="simulation-actions">
            <el-button
              v-if="!isSimulating"
              type="primary"
              size="small"
              :disabled="!routeInfo"
              @click="startSimulation()"
            >
              开始模拟
            </el-button>
            <el-button
              v-if="isSimulating"
              type="warning"
              size="small"
              @click="toggleSimulation"
            >
              {{ simulationTimer ? '暂停' : '继续' }}
            </el-button>
            <el-button
              v-if="isSimulating"
              type="danger"
              size="small"
              @click="stopSimulation"
            >
              停止
            </el-button>
          </div>
          <div v-if="isSimulating" class="simulation-status">
            <p><small>🏃‍♂️ 正在模拟移动中... ({{ simulationSpeed }}km/h)</small></p>
            <p><small>进度: {{ simulationIndex + 1 }}/{{ simulationPath.length }} 点</small></p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 骑车面板 -->
    <div v-if="showRide && !isRidePanelCollapsed" class="ride-panel" :class="{ hidden: hideUI }">
      <div class="panel-header">
        <h3>骑车功能</h3>
        <div class="header-buttons">
          <el-button type="text" @click="collapseRidePanel" class="collapse-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
            收起
          </el-button>
          <el-button type="text" @click="cancelRide">关闭</el-button>
        </div>
      </div>
      <div class="panel-content">
        <!-- 功能选项卡 -->
        <div class="ride-tabs">
          <div class="tab-item" :class="{ active: currentTab === 'find' }" @click="currentTab = 'find'">找车</div>
          <div class="tab-item" :class="{ active: currentTab === 'use' }" @click="currentTab = 'use'">用车</div>
          <div class="tab-item" :class="{ active: currentTab === 'return' }" @click="currentTab = 'return'">还车</div>
        </div>
        
        <!-- 找车功能 -->
        <div v-if="currentTab === 'find'" class="find-bike-section">
          <div class="feature-placeholder">
            <p>🚴‍♂️ 找车功能</p>
            <p>导航到最近停车点</p>
            <p>（暂未实现）</p>
          </div>
        </div>
        
        <!-- 用车功能 -->
        <div v-if="currentTab === 'use'" class="use-bike-section">
          <div class="bike-input-section">
            <div class="input-group">
              <label>单车ID:</label>
              <el-input
                v-model="bikeId"
                placeholder="请输入单车ID"
                size="small"
                :disabled="isRiding"
              />
            </div>
            <div class="action-buttons">
              <el-button
                v-if="!isRiding"
                type="primary"
                :disabled="!bikeId"
                @click="startRiding"
              >
                开始使用
              </el-button>
              <div v-else class="riding-message">
                <p>正在骑行中，请前往还车页面结束骑行</p>
              </div>
            </div>
          </div>
          
          <!-- 骑行状态显示 -->
          <div v-if="isRiding" class="riding-status">
            <!-- 订单信息显示 -->
            <div v-if="rideOrderInfo" class="order-info">
              <h4>骑行订单信息</h4>
              <p><strong>订单ID:</strong> {{ rideOrderInfo.orderId }}</p>
              <p><strong>开始时间:</strong> {{ rideOrderInfo.startTime }}</p>
              <p><strong>起始停车区域:</strong> {{ rideOrderInfo.startGeohash }}</p>
            </div>
            
            <!-- 实时骑行状态 -->
            <div class="status-info">
              <p><strong>单车ID:</strong> {{ bikeId }}</p>
              <p><strong>骑行时间:</strong> {{ formatTime(ridingTime) }}</p>
              <p><strong>骑行距离:</strong> {{ formatDistance(ridingDistance) }}</p>
              <p><strong>预计收费:</strong> ¥{{ calculateFee() }}</p>
            </div>
            <div class="current-position">
              <p><strong>当前位置:</strong> {{ formatPosition(currentPosition) }}</p>
            </div>
          </div>
          
          <!-- 未开始骑行状态 -->
          <div v-else class="not-riding">
            <p>请输入单车ID开始骑行</p>
          </div>
        </div>
        
        <!-- 还车功能 -->
        <div v-if="currentTab === 'return'" class="return-bike-section">
          <div v-if="isRiding" class="return-actions">
            <div class="return-info">
              <p><strong>骑行总时间:</strong> {{ formatTime(ridingTime) }}</p>
              <p><strong>骑行总距离:</strong> {{ formatDistance(ridingDistance) }}</p>
              <p><strong>预计费用:</strong> ¥{{ calculateFee() }}</p>
            </div>
            <div class="return-buttons">
              <el-button
                type="primary"
                size="large"
                @click="parkBike"
              >
                停车
              </el-button>
            </div>
          </div>
          <div v-else class="no-riding">
            <p>当前没有正在使用的单车</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 导航圆形收起组件 -->
    <div v-if="showNavigation && isNavigationPanelCollapsed" class="navigation-collapsed-button" :class="{ hidden: hideUI }" @click="expandNavigationPanel">
      <div class="collapsed-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 19l7-7-7-7"/>
          <path d="M15 5l7 7-7 7"/>
        </svg>
      </div>
      <div v-if="routeInfo" class="collapsed-route-indicator">
        <div class="route-pulse"></div>
      </div>
    </div>
    
    <!-- 骑车圆形收起组件 -->
    <div v-if="showRide && isRidePanelCollapsed" class="ride-collapsed-button" :class="{ hidden: hideUI }" @click="expandRidePanel">
      <div class="collapsed-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="7,10 12,15 17,10"/>
        </svg>
      </div>
      <div v-if="isRiding" class="collapsed-riding-indicator">
        <div class="riding-pulse"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted, ref, onUnmounted, watch } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';
// 导入单车数据API
import { getMapAreaBicycles } from '@/api/map/bicycle';
import { getAllParkingAreas, getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking';
import { getHeatMapData, convertHeatMapData } from '@/api/map/heat';
import { updateUserProfile } from '@/api/account/profile';
// 导入骑行API
import { rentBike, returnBike } from '@/api/riding';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getRidingRoute } from '@/utils/amap';

// 状态文本映射
const statusText = {
  available: '可用',
  in_use: '使用中',
  maintenance: '维护中'
};

// 停车点默认颜色
const parkingAreaColor = '#4CAF50';  // 绿色

export default {
  name: 'MapComponent',
  props: {
    hideUI: {
      type: Boolean,
      default: false
    },
    mapType: {
      type: String,
      default: 'normal'
    },
    mapStyle: {
      type: String,
      default: 'normal'
    },
    showBicycles: {
      type: Boolean,
      default: false
    },
    showParkingAreas: {
      type: Boolean,
      default: false
    },
    showNavigation: {
      type: Boolean,
      default: false
    },
    showRide: {
      type: Boolean,
      default: false
    },
    showHeatmap: {
      type: Boolean,
      default: false
    },
    userInfo: {
      type: Object,
      default: null
    },
    authToken: {
      type: String,
      default: ''
    }
  },
  setup(props, { emit }) {
    const map = ref(null);
    const mapControls = ref([]);  // 存储所有控件引用
    const markers = ref([]);
    const polyline = ref(null);
    const bicycleMarkers = ref([]);
    const bicyclesData = ref([]); // 存储单车数据
    const infoWindow = ref(null);
    const parkingAreas = ref([]);
    const parkingPolygons = ref([]);
    const parkingMarkers = ref([]); // 存储停车场图标标记
    const heatmap = ref(null); // 热力图实例

    // 导航相关的状态
    const startPoint = ref(null);
    const endPoint = ref(null);
    const isSelectingStart = ref(false);
    const isSelectingEnd = ref(false);
    const routeInfo = ref(null);
    const isNavigationPanelCollapsed = ref(false);  // 导航面板是否收起
    const riding = ref(null);  // 存储骑行规划实例
    const navigationMarkers = ref([]);  // 存储导航起终点标记
    const navigationPolyline = ref(null);  // 存储导航路线
    const routePath = ref(null);  // 存储路线路径
    const userPosition = ref(null);  // 用户当前位置
    const userPositionMarker = ref(null);  // 用户位置标记
    const isSettingUserPosition = ref(false);  // 是否正在设置用户位置

    // 骑车相关状态
    const currentTab = ref('use');  // 当前选中的标签页
    const bikeId = ref('');  // 单车ID
    const isRiding = ref(false);  // 是否正在骑行
    const isRidePanelCollapsed = ref(false);  // 骑车面板是否收起
    
    // 模拟移动相关状态
    const isSimulating = ref(false);  // 是否正在模拟移动
    const simulationPath = ref([]);  // 模拟移动路径
    const simulationIndex = ref(0);  // 当前路径点索引
    const simulationProgress = ref(0);  // 在当前路径段的进度（0-1）
    const simulationTimer = ref(null);  // 模拟移动定时器
    const simulationSpeed = ref(15);  // 模拟速度 km/h
    const navigationUpdateTimer = ref(null);  // 导航更新防抖定时器
    const ridingTime = ref(0);  // 骑行时间（秒）
    const ridingDistance = ref(0);  // 骑行距离（米）
    const currentPosition = ref(null);  // 当前位置
    const ridingPath = ref([]);  // 骑行路径
    const ridingTimer = ref(null);  // 骑行计时器
    const ridingPathPolyline = ref(null);  // 骑行路径折线

    // 骑行记录信息（与riding.js返回数据结构一致）
    const rideOrderInfo = ref(null);  // 当前骑行订单信息



    // 地图样式映射
    const styleMapping = {
      normal: 'amap://styles/normal',
      dark: 'amap://styles/dark',
      light: 'amap://styles/light',
      whitesmoke: 'amap://styles/whitesmoke',
      fresh: 'amap://styles/fresh',
      grey: 'amap://styles/grey'
    };

    // 获取单车数据
    const fetchBicyclesData = async () => {
      try {
        if (!props.showBicycles) {
          // 清除现有标记
          bicycleMarkers.value.forEach(marker => {
            if (marker && typeof marker.setMap === 'function') {
              marker.setMap(null);
            }
          });
          bicycleMarkers.value = [];
          return;
        }

        // 获取当前地图边界
        const bounds = map.value.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLng: bounds.getSouthWest().lng,
          maxLng: bounds.getNorthEast().lng
        };

        // 获取区域内的单车
        const response = await getMapAreaBicycles(params);
        bicyclesData.value = response.data;
      } catch (error) {
        console.error('获取单车数据失败：', error);
        ElMessage.error('获取单车数据失败');
      }
    };

    // 更新热力图数据
    const updateHeatmapData = async () => {
      if (!map.value || !heatmap.value) return;

      try {
        // 获取当前地图边界
        const bounds = map.value.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLon: bounds.getSouthWest().lng,
          maxLon: bounds.getNorthEast().lng
        };

        // 获取热力图数据（基于单车数据）
        const response = await getHeatMapData(params);
        
        // 处理单车API的响应格式
        let bicycleData = [];
        if (response && Array.isArray(response.data)) {
          bicycleData = response.data;
        } else if (Array.isArray(response)) {
          bicycleData = response;
        } else {
          console.error('获取单车数据格式错误：', response);
          return;
        }

        // 转换单车数据为热力图格式
        const heatmapData = convertHeatMapData(bicycleData);
        
        // 设置热力图数据
        heatmap.value.setDataSet({
          data: heatmapData,
          max: 10 // 调整最大权重值，因为每个单车权重为1
        });

        console.log('热力图数据已更新，单车数量：', bicycleData.length, '热力图点数：', heatmapData.length);
      } catch (error) {
        console.error('更新热力图数据失败：', error);
        ElMessage.error('获取热力图数据失败');
      }
    };

    // 初始化热力图
    const initHeatmap = async () => {
      if (!map.value) return;

      try {
        if (!heatmap.value) {
          // 先加载插件
          await AMapLoader.load({
            key: '7a9ebfd8db9264a7f90b65369bd2970a',
            version: '2.0',
            plugins: ['AMap.HeatMap']
          });

          // 确保地图实例存在
          if (!map.value) return;

          // 创建热力图实例
          heatmap.value = new window.AMap.HeatMap(map.value, {
            radius: 30, // 增加热力图的半径
            opacity: [0, 0.8],
            gradient: {
              0.4: 'rgb(0, 255, 255)',
              0.65: 'rgb(0, 255, 0)',
              0.85: 'rgb(255, 255, 0)',
              1.0: 'rgb(255, 0, 0)'
            },
            zooms: [1, 20], // 支持的缩放级别范围
            visible: true
          });
        }

        // 获取并设置热力图数据
        await updateHeatmapData();

        // 初始时隐藏热力图
        heatmap.value.hide();
        
        // 只有在props.showHeatmap为true时才显示
        if (props.showHeatmap) {
          heatmap.value.show();
        }
      } catch (error) {
        console.error('初始化热力图失败：', error);
        ElMessage.error('初始化热力图失败');
      }
    };

    // 监听热力图显示状态
    watch(() => props.showHeatmap, async (newVal) => {
      if (!map.value) return;

      if (newVal) {
        // 如果开启热力图，确保已初始化并显示
        await initHeatmap();
        if (heatmap.value) {
          heatmap.value.show();
          // 更新热力图数据
          await updateHeatmapData();
        }
      } else if (heatmap.value) {
        // 如果关闭热力图，隐藏热力图层
        heatmap.value.hide();
      }
    });

    // 显示单车位置
    const showBicycleMarkers = async () => {
      if (!map.value) return;

      try {
        // 确保 bicycleMarkers.value 是数组
        if (!Array.isArray(bicycleMarkers.value)) {
          bicycleMarkers.value = [];
        }

        // 清除现有标记
        bicycleMarkers.value.forEach(marker => {
          if (marker && typeof marker.setMap === 'function') {
            marker.setMap(null);
          }
        });
        bicycleMarkers.value = [];

        if (!props.showBicycles) return;

        // 获取数据
        await fetchBicyclesData();

        const AMap = await AMapLoader.load({
          key: '7a9ebfd8db9264a7f90b65369bd2970a',
          version: '2.0'
        });

        // 创建单车图标
        const icon = new AMap.Icon({
          image: '/src/components/icons/bicycle.png',
          size: new AMap.Size(32, 32),
          imageSize: new AMap.Size(32, 32)
        });

        // 创建标记
        if (Array.isArray(bicyclesData.value)) {
          bicyclesData.value.forEach(bicycle => {
            const marker = new AMap.Marker({
              position: [bicycle.currentLon, bicycle.currentLat],
              icon: icon,
              map: map.value,
              title: `单车 #${bicycle.bikeId}`
            });

            // 添加点击事件
            marker.on('click', () => {
              if (infoWindow.value) {
                infoWindow.value.close();
              }

              infoWindow.value = new AMap.InfoWindow({
                content: `
                  <div class="info-window">
                    <h4>单车 #${bicycle.bikeId}</h4>
                    <p>状态: ${statusText[bicycle.bikeStatus] || bicycle.bikeStatus}</p>
                    <p>最后更新: ${bicycle.lastUpdatedTime}</p>
                  </div>
                `,
                offset: new AMap.Pixel(0, -30)
              });

              infoWindow.value.open(map.value, marker.getPosition());
            });

            bicycleMarkers.value.push(marker);
          });
        }
      } catch (error) {
        console.error('显示单车位置失败：', error);
        ElMessage.error('显示单车位置失败');
      }
    };

    // 隐藏单车位置
    const hideBicycleMarkers = () => {
      if (!map.value || !bicycleMarkers.value.length) return;
      bicycleMarkers.value.forEach(marker => {
        marker.setMap(null);
      });
    };

    // 监听 showBicycles 属性变化
    watch(() => props.showBicycles, (newValue) => {
      if (newValue) {
        showBicycleMarkers();
      } else {
        hideBicycleMarkers();
      }
    });

    // 监听 hideUI 属性变化
    watch(() => props.hideUI, (newValue) => {
      if (!map.value) return;

      mapControls.value.forEach(control => {
        if (newValue) {
          control.hide();
        } else {
          control.show();
        }
      });
    });

    // 监听地图类型变化
    watch(() => props.mapType, (newType) => {
      if (!map.value) return;
      
      if (newType === 'satellite') {
        // 切换到卫星图
        map.value.setLayers([new AMap.TileLayer.Satellite()]);
      } else {
        // 切换到普通地图
        map.value.setLayers([new AMap.TileLayer()]);
      }
    });

    // 监听地图样式变化
    watch(() => props.mapStyle, (newStyle) => {
      if (!map.value) return;
      map.value.setMapStyle(styleMapping[newStyle]);
    });

    // 监听地图移动事件，更新单车、停车点和热力图数据
    const setupMapEventListeners = () => {
      if (!map.value) return;

      // 当地图移动结束时，重新显示单车、停车点和热力图
      map.value.on('moveend', () => {
        if (props.showBicycles) {
          showBicycleMarkers();
        }
        if (props.showParkingAreas) {
          showParkingAreas();
        }
        if (props.showHeatmap) {
          updateHeatmapData();
        }
      });
    };

    // 清除地图上的标记和路线
    const clearMap = () => {
      if (markers.value.length) {
        map.value.remove(markers.value);
        markers.value = [];
      }
      if (polyline.value) {
        map.value.remove(polyline.value);
        polyline.value = null;
      }
    };

    // 缩放方法
    const zoomIn = () => {
      if (map.value) {
        map.value.zoomIn();
      }
    };

    const zoomOut = () => {
      if (map.value) {
        map.value.zoomOut();
      }
    };

    // 获取停车点数据
    const fetchParkingAreas = async () => {
      try {
        if (!props.showParkingAreas) {
          return;
        }

        // 获取当前地图边界
        const bounds = map.value.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLon: bounds.getSouthWest().lng,
          maxLon: bounds.getNorthEast().lng
        };

        // 获取区域内的停车点
        const response = await getParkingAreasInBounds(params);
        console.log('停车点API响应:', response);
        
        // 检查响应数据格式并转换
        let rawData = null;
        if (response && Array.isArray(response)) {
          // 如果响应直接是数组
          rawData = response;
        } else if (response && response.data && Array.isArray(response.data)) {
          // 如果响应包装在data字段中
          rawData = response.data;
        } else if (response && response.code === 200 && Array.isArray(response.data)) {
          // 如果是标准的API响应格式
          rawData = response.data;
        }

        if (rawData && Array.isArray(rawData)) {
          console.log('原始停车点数据:', rawData);
          // 转换数据格式
          const convertedData = convertParkingAreaData(rawData);
          console.log('转换后的停车点数据:', convertedData);
          parkingAreas.value = convertedData;
        } else {
          console.warn('停车点数据格式异常:', response);
          throw new Error('停车点数据格式异常');
        }
      } catch (error) {
        console.error('获取停车点数据失败：', error);
        ElMessage.error('获取停车点数据失败');
        
        // 如果API调用失败，使用备用数据
        try {
          const fallbackResponse = await getAllParkingAreas();
          parkingAreas.value = fallbackResponse.data;
        } catch (fallbackError) {
          console.error('获取备用停车点数据也失败：', fallbackError);
        }
      }
    };

    // 显示停车点区域
    const showParkingAreas = async () => {
      if (!map.value) return;

      try {
        // 清除现有标记和多边形
        parkingPolygons.value.forEach(polygon => {
          if (polygon && typeof polygon.setMap === 'function') {
            polygon.setMap(null);
          }
        });
        parkingMarkers.value.forEach(marker => {
          if (marker && typeof marker.setMap === 'function') {
            marker.setMap(null);
          }
        });
        parkingPolygons.value = [];
        parkingMarkers.value = [];

        // 重新获取停车点数据
        await fetchParkingAreas();

        // 创建停车场图标
        const parkingIcon = new AMap.Icon({
          image: '/src/components/icons/parking_area.png',
          size: new AMap.Size(40, 40),
          imageSize: new AMap.Size(40, 40)
        });

        // 为每个停车点创建区域和图标
        parkingAreas.value.forEach(area => {
          // 使用精确的多边形路径（如果有）或者使用边界框创建矩形
          const path = area.polygonPath || [
            [area.bounds.southwest[1], area.bounds.southwest[0]],
            [area.bounds.northeast[1], area.bounds.southwest[0]],
            [area.bounds.northeast[1], area.bounds.northeast[0]],
            [area.bounds.southwest[1], area.bounds.northeast[0]],
            [area.bounds.southwest[1], area.bounds.southwest[0]]
          ];

          // 创建多边形
          const polygon = new AMap.Polygon({
            path: path,
            strokeColor: parkingAreaColor,
            strokeWeight: 3,
            strokeOpacity: 1,
            fillColor: parkingAreaColor,
            fillOpacity: 0.4,
            cursor: 'pointer',
            map: null  // 初始不添加到地图
          });

          // 创建图标标记（放在区域中心）
          const center = area.center ? [area.center[1], area.center[0]] : [
            (area.bounds.southwest[1] + area.bounds.northeast[1]) / 2,
            (area.bounds.southwest[0] + area.bounds.northeast[0]) / 2
          ];

          const marker = new AMap.Marker({
            position: center,
            icon: parkingIcon,
            offset: new AMap.Pixel(-20, -20),
            cursor: 'pointer',
            map: null  // 初始不添加到地图
          });

          // 添加图标点击事件
          marker.on('click', () => {
            if (!infoWindow.value) {
              infoWindow.value = new AMap.InfoWindow({
                closeWhenClickMap: true,
                offset: new AMap.Pixel(0, -30)
              });
            }

            const content = `
              <div class="parking-info">
                <h4>停车区域 ${area.geohash}</h4>
                <p><strong>区域编号：</strong>${area.geohash}</p>
                <p><strong>区域组ID：</strong>${area.regionGroupId}</p>
                <p><strong>停车容量：</strong>${area.parkingCapacity}个</p>
                <p><strong>中心位置：</strong>${area.centerLat.toFixed(6)}, ${area.centerLon.toFixed(6)}</p>
              </div>
            `;

            infoWindow.value.setContent(content);
            infoWindow.value.open(map.value, marker.getPosition());
          });

          // 移除多边形点击事件 - 只有停车图标才显示详情

          // 仅保留标记鼠标悬停效果
          marker.on('mouseover', () => {
            polygon.setOptions({
              fillOpacity: 0.6,
              strokeWeight: 4
            });
            marker.setzIndex(110);
          });

          marker.on('mouseout', () => {
            polygon.setOptions({
              fillOpacity: 0.4,
              strokeWeight: 3
            });
            marker.setzIndex(100);
          });

          parkingPolygons.value.push(polygon);
          parkingMarkers.value.push(marker);
        });

        // 如果需要显示，一次性添加所有标记到地图
        if (props.showParkingAreas) {
          parkingPolygons.value.forEach(polygon => polygon.setMap(map.value));
          parkingMarkers.value.forEach(marker => marker.setMap(map.value));
        }
      } catch (error) {
        console.error('加载停车区域失败：', error);
        ElMessage.error('加载停车区域失败');
      }
    };

    // 隐藏停车点区域
    const hideParkingAreas = () => {
      if (!map.value) return;
      parkingPolygons.value.forEach(polygon => polygon.setMap(null));
      parkingMarkers.value.forEach(marker => marker.setMap(null));
    };

    // 监听 showParkingAreas 属性变化
    watch(() => props.showParkingAreas, async (show) => {
      if (show) {
        await showParkingAreas();
      } else {
        hideParkingAreas();
      }
    });

    // 计算导航路线
    const calculateRoute = async () => {
      if (!startPoint.value || !endPoint.value) {
        ElMessage.warning('请先选择起点和终点');
        return;
      }

      console.log('开始计算路线');
      console.log('起点:', startPoint.value);
      console.log('终点:', endPoint.value);

      // 验证坐标值是否有效
      if (!isValidCoordinate(startPoint.value)) {
        console.error('起点坐标无效');
        ElMessage.error('起点坐标无效，请重新选择');
        return;
      }

      if (!isValidCoordinate(endPoint.value)) {
        console.error('终点坐标无效');
        ElMessage.error('终点坐标无效，请重新选择');
        return;
      }

      try {
        // 确保先完全清除之前的路线
        clearRoute();

        // 准备起点和终点坐标数组
        const start = [startPoint.value.lng, startPoint.value.lat];
        const end = [endPoint.value.lng, endPoint.value.lat];

        // 调用封装好的路径规划服务
        const result = await getRidingRoute(start, end);
        console.log('路径规划结果:', result);

        if (result.status === 'complete' && result.route) {
          // 更新路线信息
          routeInfo.value = {
            distance: (result.route.distance / 1000).toFixed(1) + 'km',
            time: Math.ceil(result.route.duration / 60)
          };

          // 重新初始化地图API用于显示路线
          const AMap = await AMapLoader.load({
            key: '7a9ebfd8db9264a7f90b65369bd2970a',  // 使用Web端Key显示地图
            version: '2.0',
            plugins: ['AMap.Riding']
          });

          // 创建完整的导航路径，包括连接线段
          const completeNavigationPath = [];
          
          // 1. 从用户当前位置到路径起点的连接线
          if (userPosition.value) {
            completeNavigationPath.push(new AMap.LngLat(userPosition.value[0], userPosition.value[1]));
          }
          
          // 2. 添加规划的路径点
          result.route.polyline.forEach(point => {
            completeNavigationPath.push(new AMap.LngLat(point[0], point[1]));
          });
          
          // 3. 从路径终点到设置终点的连接线
          if (endPoint.value) {
            completeNavigationPath.push(new AMap.LngLat(endPoint.value.lng, endPoint.value.lat));
          }

          // 创建或更新路线折线
          if (navigationPolyline.value) {
            navigationPolyline.value.setPath(completeNavigationPath);
          } else {
            navigationPolyline.value = new AMap.Polyline({
              path: completeNavigationPath,
              strokeColor: '#3366FF',
              strokeWeight: 6,
              strokeOpacity: 0.8,
              strokeStyle: 'solid',
              lineJoin: 'round',
              lineCap: 'round',
              zIndex: 50,
              map: map.value
            });
          }

          // 调整地图视野以显示整个路线
          map.value.setFitView([navigationPolyline.value]);

        } else {
          throw new Error(result.error || '未找到合适的骑行路线');
        }

      } catch (error) {
        console.error('路线规划失败:', error);
        ElMessage.error(error.message || '路线规划失败，请确保起点和终点在合理的距离范围内');
        clearRoute();
      }
    };

    // 验证坐标是否有效
    const isValidCoordinate = (point) => {
      if (!point) {
        console.log('坐标点为空');
        return false;
      }
      
      // 打印调试信息
      console.log('验证坐标:', point);
      console.log('lng类型:', typeof point.lng, '值:', point.lng);
      console.log('lat类型:', typeof point.lat, '值:', point.lat);

      // 转换为数字并验证
      const lng = Number(point.lng);
      const lat = Number(point.lat);

      if (isNaN(lng) || isNaN(lat)) {
        console.log('坐标转换为数字后无效');
        return false;
      }

      // 验证坐标范围
      const isValid = lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90;
      console.log('坐标范围验证结果:', isValid);
      
      return isValid;
    };





    // 修改 clearRoute 函数
    const clearRoute = () => {
      if (riding.value) {
        riding.value.clear();
        riding.value = null;
      }
      routeInfo.value = null;
      if (navigationPolyline.value) {
        navigationPolyline.value.setMap(null);
        navigationPolyline.value = null;
      }
    };

    // 完全清除导航
    const clearNavigation = () => {
      // 清除路线
      if (routePath.value) {
        map.value.remove(routePath.value);
        routePath.value = null;
      }

      // 清除起终点标记
      if (navigationMarkers.value.length) {
        navigationMarkers.value.forEach(marker => {
          if (marker) {
            map.value.remove(marker);
          }
        });
        navigationMarkers.value = [];
      }

      // 重置状态
      startPoint.value = null;
      endPoint.value = null;
      routeInfo.value = null;
      isSelectingStart.value = false;
      isSelectingEnd.value = false;
      isSettingUserPosition.value = false;
      map.value.setDefaultCursor('');
    };

    // 收起导航面板
    const collapseNavigationPanel = () => {
      isNavigationPanelCollapsed.value = true;
    };

    // 展开导航面板
    const expandNavigationPanel = () => {
      isNavigationPanelCollapsed.value = false;
    };

    // 取消导航
    const cancelNavigation = () => {
      // 清除路线和标记
      clearRoute();
      
      // 清除防抖定时器
      if (navigationUpdateTimer.value) {
        clearTimeout(navigationUpdateTimer.value);
        navigationUpdateTimer.value = null;
      }
      
      // 清除起终点标记
      if (navigationMarkers.value.length) {
        navigationMarkers.value.forEach(marker => {
          if (marker) {
            marker.setMap(null);
          }
        });
        navigationMarkers.value = [];
      }
      
      // 重置所有导航相关状态
      startPoint.value = null;
      endPoint.value = null;
      isSelectingStart.value = false;
      isSelectingEnd.value = false;
      routeInfo.value = null;
      isNavigationPanelCollapsed.value = false; // 重置收起状态
      
      // 关闭导航功能
      emit('update:showNavigation', false);
    };

    // 初始化地图
    const initMap = async () => {
      try {
        // 处理可能的加载器冲突 - 清理之前的状态
        const cleanupPreviousState = () => {
          // 如果存在来自其他加载器的script，但AMap状态不完整，进行清理
          const existingScript = document.getElementById('amap-script');
          if (existingScript && window.AMap) {
            // 检查AMap对象是否完整（检查必需的插件）
            const hasRequiredPlugins = window.AMap.HeatMap && window.AMap.ControlBar;
            if (!hasRequiredPlugins) {
              console.log('检测到不完整的AMap状态，清理后重新加载...');
              existingScript.remove();
              delete window.AMap;
              // 清理可能存在的其他相关全局变量
              if (window.AMapUI) delete window.AMapUI;
            }
          }
        };

        // 先清理可能冲突的状态
        cleanupPreviousState();

        const AMap = await AMapLoader.load({
          key: '7a9ebfd8db9264a7f90b65369bd2970a',
          version: '2.0',
          plugins: ['AMap.HeatMap', 'AMap.ControlBar', 'AMap.Riding']
        });

        // 验证关键插件是否正确加载
        if (!AMap.HeatMap || !AMap.ControlBar) {
          throw new Error('必需的地图插件未正确加载');
        }

        map.value = new AMap.Map('map', {
          zoom: 17, // 增加初始缩放级别
          center: [114.04346, 22.51351],
          mapStyle: styleMapping[props.mapStyle] || 'amap://styles/normal',
          zooms: [3, 20] // 设置地图缩放范围
        });

        // 添加指南针和定位按钮
        const controlBar = new AMap.ControlBar({
          position: {
            right: '15px',
            bottom: '15px'
          },
          showZoomBar: false, // 不显示缩放按钮
          showControlButton: true // 显示指南针
        });
        map.value.addControl(controlBar);

        // 调整控制栏的样式
        setTimeout(() => {
          const controlElements = document.querySelectorAll('.amap-control-bar');
          controlElements.forEach(element => {
            element.style.transform = 'scale(1.2)'; // 放大20%
            element.style.transformOrigin = 'bottom right';
          });
        }, 100);

        // 设置默认当前位置为地图中心
        const mapCenter = map.value.getCenter();
        userPosition.value = [mapCenter.getLng(), mapCenter.getLat()];
        
        // 创建用户位置标记
        userPositionMarker.value = new AMap.Marker({
          position: userPosition.value,
          map: map.value,
          content: `
            <div class="user-position-marker">
              <div class="user-position-marker-inner"></div>
            </div>
          `,
          offset: new AMap.Pixel(-10, -10), // 用户位置标记居中偏移
          zIndex: 50
        });

        // 添加用户位置标记样式
        const markerStyle = document.createElement('style');
        markerStyle.textContent = `
          .user-position-marker {
            width: 20px;
            height: 20px;
            position: relative;
          }
          .user-position-marker-inner {
            width: 20px;
            height: 20px;
            background-color: #4A90E2;
            border: 3px solid white;
            border-radius: 50%;
            box-shadow: 0 0 15px rgba(74, 144, 226, 0.4), 0 0 6px rgba(0,0,0,0.3);
            animation: pulse 2s infinite;
          }
          @keyframes pulse {
            0% {
              box-shadow: 0 0 15px rgba(74, 144, 226, 0.4), 0 0 6px rgba(0,0,0,0.3);
            }
            50% {
              box-shadow: 0 0 25px rgba(74, 144, 226, 0.6), 0 0 10px rgba(0,0,0,0.5);
            }
            100% {
              box-shadow: 0 0 15px rgba(74, 144, 226, 0.4), 0 0 6px rgba(0,0,0,0.3);
            }
          }
        `;
        document.head.appendChild(markerStyle);

        // 初始化骑行规划实例
        riding.value = new AMap.Riding({
          map: map.value,
          panel: 'panel'
        });

        // 添加地图点击事件监听
        map.value.on('click', handleMapClick);

      } catch (error) {
        console.error('初始化地图失败：', error);
        
        // 更详细的错误处理和重试机制
        if (error.message && error.message.includes('必需的地图插件未正确加载')) {
          ElMessage.error('地图插件加载失败，请刷新页面重试');
          console.log('建议：可能存在地图加载器冲突，请刷新页面');
        } else if (error.message && error.message.includes('load')) {
          // 可能是加载器冲突，尝试一次重试
          console.log('检测到可能的加载器冲突，尝试重试...');
          
          setTimeout(async () => {
            try {
              // 强制清理所有相关状态
              const scripts = document.querySelectorAll('script[src*="webapi.amap.com"]');
              scripts.forEach(script => script.remove());
              delete window.AMap;
              delete window.AMapUI;
              
              // 重新初始化
              await initMap();
              ElMessage.success('地图重新加载成功');
            } catch (retryError) {
              console.error('重试失败：', retryError);
              ElMessage.error('地图加载失败，请刷新页面重试');
            }
          }, 1000);
        } else {
          ElMessage.error('地图加载失败，请刷新页面重试');
        }
      }
    };

    // 处理地图点击事件
    const handleMapClick = (e) => {
      // 确保从事件对象中正确获取坐标
      if (!e.lnglat) {
        console.error('无法获取点击位置的坐标');
        return;
      }

      // 直接使用高德地图的经纬度对象
      const lnglat = e.lnglat;
      console.log('点击位置坐标:', lnglat.getLng(), lnglat.getLat());

      if (isSettingUserPosition.value) {
        // 处理用户位置设置
        const newPosition = [lnglat.getLng(), lnglat.getLat()];
        userPosition.value = newPosition;
        console.log('设置用户位置:', userPosition.value);

        // 更新用户位置标记
        if (userPositionMarker.value) {
          userPositionMarker.value.setPosition(newPosition);
        } else {
          userPositionMarker.value = new AMap.Marker({
            position: newPosition,
            map: map.value,
            content: `
              <div class="user-position-marker">
                <div class="user-position-marker-inner"></div>
              </div>
            `,
            offset: new AMap.Pixel(-10, -10)
          });
        }

        // 移动地图中心到新位置
        map.value.setCenter(newPosition);
        
        isSettingUserPosition.value = false;
        map.value.setDefaultCursor('');
        ElMessage.success('用户位置已更新');
        
        // 触发位置更新相关的逻辑（包括导航路径更新）
        updateUserPositionMarker();
        
        // 如果正在骑行，更新当前位置和路径
        if (isRiding.value) {
          currentPosition.value = newPosition;
          // 立即记录新位置到路径中
          ridingPath.value.push(newPosition);
          
          // 计算距离
          if (ridingPath.value.length > 1) {
            const lastPosition = ridingPath.value[ridingPath.value.length - 2];
            const distance = calculateDistance(lastPosition, newPosition);
            ridingDistance.value += distance;
          }
          
          // 更新地图上的路径
          updateRidingPath();
        }
      } else if (isSelectingStart.value) {
        startPoint.value = {
          lng: lnglat.getLng(),
          lat: lnglat.getLat()
        };
        console.log('设置起点:', startPoint.value);
        
        // 添加或更新起点标记
        if (navigationMarkers.value[0]) {
          navigationMarkers.value[0].setPosition([startPoint.value.lng, startPoint.value.lat]);
        } else {
          const startMarker = new AMap.Marker({
            position: [startPoint.value.lng, startPoint.value.lat],
            map: map.value,
            offset: new AMap.Pixel(-13, -34), // 确保偏移量一致
            icon: new AMap.Icon({
              size: new AMap.Size(25, 34),
              imageSize: new AMap.Size(25, 34),
              image: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png'
            })
          });
          navigationMarkers.value[0] = startMarker;
        }
        isSelectingStart.value = false;
        map.value.setDefaultCursor('');
      } else if (isSelectingEnd.value) {
        endPoint.value = {
          lng: lnglat.getLng(),
          lat: lnglat.getLat()
        };
        console.log('设置终点:', endPoint.value);

        // 添加或更新终点标记
        if (navigationMarkers.value[1]) {
          navigationMarkers.value[1].setPosition([endPoint.value.lng, endPoint.value.lat]);
        } else {
          const endMarker = new AMap.Marker({
            position: [endPoint.value.lng, endPoint.value.lat],
            map: map.value,
            offset: new AMap.Pixel(-13, -34), // 确保偏移量一致
            icon: new AMap.Icon({
              size: new AMap.Size(25, 34),
              imageSize: new AMap.Size(25, 34),
              image: 'https://webapi.amap.com/theme/v1.3/markers/n/end.png'
            })
          });
          navigationMarkers.value[1] = endMarker;
        }
        isSelectingEnd.value = false;
        map.value.setDefaultCursor('');
      }
    };

    // 修改 selectCurrentPositionAsStart 函数
    const selectCurrentPositionAsStart = () => {
      if (!userPosition.value) {
        ElMessage.warning('请先设置当前位置');
        return;
      }

      // 获取用户位置标记的实际位置，确保准确性
      const actualUserPosition = userPositionMarker.value ? 
        userPositionMarker.value.getPosition() : userPosition.value;

      // 确保位置是数组格式
      let position;
      if (Array.isArray(actualUserPosition)) {
        position = actualUserPosition;
      } else if (actualUserPosition && typeof actualUserPosition.getLng === 'function') {
        position = [actualUserPosition.getLng(), actualUserPosition.getLat()];
      } else if (Array.isArray(userPosition.value)) {
        position = userPosition.value;
      } else {
        position = [userPosition.value.getLng(), userPosition.value.getLat()];
      }

      startPoint.value = {
        lng: position[0],
        lat: position[1]
      };

      console.log('使用当前位置作为起点:', startPoint.value);
      console.log('用户位置标记位置:', actualUserPosition);
      console.log('用户位置变量:', userPosition.value);
      console.log('最终起点位置:', position);

      // 添加或更新起点标记
      if (navigationMarkers.value[0]) {
        navigationMarkers.value[0].setPosition(position);
      } else {
        const startMarker = new AMap.Marker({
          position: position,
          map: map.value,
          offset: new AMap.Pixel(-13, -34), // 起点标记偏移量
          icon: new AMap.Icon({
            size: new AMap.Size(25, 34),
            imageSize: new AMap.Size(25, 34),
            image: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png'
          })
        });
        navigationMarkers.value[0] = startMarker;
      }

      // 取消选点模式
      isSelectingStart.value = false;
      map.value.setDefaultCursor('');
    };

    // 开始选择点位
    const startSelectingPoint = (type) => {
      if (type === 'start') {
        isSelectingStart.value = true;
        isSelectingEnd.value = false;
      } else {
        isSelectingStart.value = false;
        isSelectingEnd.value = true;
      }
      isSettingUserPosition.value = false;
    };

    // 设置用户位置
    const setUserPosition = () => {
      // 如果正在模拟移动，先停止模拟
      if (isSimulating.value) {
        stopSimulation();
        ElMessage.info('已停止模拟移动，请设置新位置');
      }
      
      isSettingUserPosition.value = true;
      isSelectingStart.value = false;
      isSelectingEnd.value = false;
      ElMessage.info('请在地图上点击选择您的当前位置');
      // 改变鼠标样式以提示用户
      if (map.value) {
        map.value.setDefaultCursor('crosshair');
      }
    };

    // 骑车相关方法
    const startRiding = async () => {
      if (!bikeId.value) {
        ElMessage.warning('请输入单车ID');
        return;
      }

      if (!props.userInfo || !props.userInfo.userid) {
        ElMessage.error('用户信息不完整，无法开始骑行');
        return;
      }
      
      try {
        console.log('开始调用用车API...');
        console.log('用户ID:', props.userInfo.userid);
        console.log('单车ID:', bikeId.value);
        
        // 调用用车API
        const response = await rentBike(props.userInfo.userid, bikeId.value);
        console.log('用车API响应:', response);
        
        if (response.code === 200) {
          // 记录骑行订单信息
          rideOrderInfo.value = response.data;
          
          // 设置骑行状态
          isRiding.value = true;
          ridingTime.value = 0;
          ridingDistance.value = 0;
          ridingPath.value = [];
          
          // 获取当前位置作为起始位置
          if (userPosition.value) {
            currentPosition.value = userPosition.value;
            ridingPath.value.push([...userPosition.value]);
          }
          
          // 开始计时器
          ridingTimer.value = setInterval(() => {
            ridingTime.value += 1;
            
            // 每2秒记录一次位置
            if (ridingTime.value % 2 === 0) {
              recordPosition();
            }
          }, 1000);
          
          ElMessage.success(`${response.message || '开始使用单车成功'}`);
          console.log('骑行订单信息已记录:', rideOrderInfo.value);
        } else {
          ElMessage.error(response.message || '用车失败，请重试');
          console.error('用车API失败:', response);
        }
      } catch (error) {
        console.error('调用用车API失败:', error);
        ElMessage.error('用车失败，请检查网络连接或稍后重试');
      }
    };

    const stopRiding = () => {
      if (ridingTimer.value) {
        clearInterval(ridingTimer.value);
        ridingTimer.value = null;
      }
      
      isRiding.value = false;
      
      // 清除路径显示
      if (ridingPathPolyline.value) {
        ridingPathPolyline.value.setMap(null);
        ridingPathPolyline.value = null;
      }
      
      ElMessage.success('骑行结束');
    };

    const parkBike = async () => {
      if (!isRiding.value) {
        ElMessage.warning('当前没有正在使用的单车');
        return;
      }

      if (!rideOrderInfo.value) {
        ElMessage.error('骑行订单信息丢失，无法还车');
        return;
      }

      if (!userPosition.value) {
        ElMessage.error('无法获取当前位置，请设置位置后再还车');
        return;
      }

      try {
        console.log('开始调用还车API...');
        console.log('骑行订单信息:', rideOrderInfo.value);
        console.log('用户信息:', props.userInfo);
        console.log('当前单车ID:', bikeId.value);
        
        // 获取用户ID和单车ID，提供备用方案
        const userId = rideOrderInfo.value?.userId || props.userInfo?.userid;
        const bikeIdForReturn = rideOrderInfo.value?.bikeId || bikeId.value;
        
        console.log('最终使用的用户ID:', userId);
        console.log('最终使用的单车ID:', bikeIdForReturn);
        console.log('当前位置:', userPosition.value);
        
        // 验证必要参数
        if (!userId) {
          ElMessage.error('无法获取用户ID，请重新登录后再试');
          return;
        }
        
        if (!bikeIdForReturn) {
          ElMessage.error('无法获取单车ID，请重新开始骑行');
          return;
        }
        
        // 调用还车API
        const response = await returnBike(
          userId,
          bikeIdForReturn,
          userPosition.value[1], // 纬度
          userPosition.value[0]  // 经度
        );
        
        console.log('还车API响应:', response);
        
        if (response.code === 200) {
          // 更新骑行订单信息
          rideOrderInfo.value = response.data;
          
          // 停止骑行计时器
          stopRiding();
          
          // 构建完整的骑行信息
          const completeRideInfo = {
            ...response.data,
            localRidingTime: ridingTime.value,
            localRidingDistance: ridingDistance.value,
            localFee: calculateFee()
          };
          
          // 显示骑行信息弹窗
          try {
            await ElMessageBox.alert(
              `
              <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px; padding: 20px; color: white; box-shadow: 0 8px 32px rgba(0,0,0,0.1);">
                <div style="text-align: center; margin-bottom: 20px;">
                  <div style="width: 60px; height: 60px; background: rgba(255,255,255,0.2); border-radius: 50%; margin: 0 auto 10px; display: flex; align-items: center; justify-content: center; font-size: 24px;">
                    🚴‍♂️
                  </div>
                  <h3 style="margin: 0; font-size: 20px; font-weight: 600;">骑行完成</h3>
                </div>
                
                <div style="background: rgba(255,255,255,0.1); border-radius: 8px; padding: 15px; margin-bottom: 15px; backdrop-filter: blur(10px);">
                  <h4 style="margin: 0 0 10px 0; font-size: 16px; display: flex; align-items: center; gap: 8px;">
                    ⏰ 时间信息
                  </h4>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 14px;">
                    <div><strong>开始时间:</strong><br/>${completeRideInfo.startTime}</div>
                    <div><strong>结束时间:</strong><br/>${completeRideInfo.endTime}</div>
                  </div>
                  <div style="margin-top: 10px; text-align: center; background: rgba(255,255,255,0.2); padding: 8px; border-radius: 6px;">
                    <strong>骑行时长: ${completeRideInfo.durationMinutes || 0}分钟</strong>
                  </div>
                </div>

                <div style="background: rgba(255,255,255,0.1); border-radius: 8px; padding: 15px; margin-bottom: 15px; backdrop-filter: blur(10px);">
                  <h4 style="margin: 0 0 10px 0; font-size: 16px; display: flex; align-items: center; gap: 8px;">
                    📍 位置信息
                  </h4>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 14px;">
                    <div><strong>起始区域:</strong><br/>${completeRideInfo.startGeohash}</div>
                    <div><strong>结束区域:</strong><br/>${completeRideInfo.endGeohash || '未知'}</div>
                  </div>
                </div>

                <div style="background: rgba(255,255,255,0.1); border-radius: 8px; padding: 15px; margin-bottom: 20px; backdrop-filter: blur(10px);">
                  <h4 style="margin: 0 0 10px 0; font-size: 16px; display: flex; align-items: center; gap: 8px;">
                    📊 骑行数据
                  </h4>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 14px; margin-bottom: 10px;">
                    <div><strong>骑行距离:</strong><br/>${completeRideInfo.distance ? (completeRideInfo.distance / 1000).toFixed(2) + 'km' : formatDistance(ridingDistance.value)}</div>
                    <div style="text-align: center; background: rgba(255,107,107,0.3); padding: 8px; border-radius: 6px; border: 2px solid #ff6b6b;">
                      <strong style="color: #ffeb3b; font-size: 16px;">本次费用<br/>¥${completeRideInfo.cost || completeRideInfo.localFee}</strong>
                    </div>
                  </div>
                </div>

                <div style="text-align: center; padding: 10px; background: rgba(255,255,255,0.1); border-radius: 8px; font-size: 14px;">
                  <span style="color: #e8f5e8;">✨ ${response.message || '感谢您的使用，祝您出行愉快！'} ✨</span>
                </div>
              </div>
              `,
              '',
              {
                dangerouslyUseHTMLString: true,
                confirmButtonText: '确定',
                type: 'success',
                customClass: 'custom-ride-complete-dialog'
              }
            );
          } catch (dialogError) {
            // 用户取消了弹窗，但不影响停车功能
            console.log('用户关闭了骑行信息弹窗');
          }
          
          // 更新用户信息
          await updateUserData(completeRideInfo);
          
          // 重置状态
          bikeId.value = '';
          ridingTime.value = 0;
          ridingDistance.value = 0;
          ridingPath.value = [];
          currentPosition.value = null;
          rideOrderInfo.value = null;
          
          ElMessage.success(response.message || '还车成功');
        } else {
          ElMessage.error(response.message || '还车失败，请重试');
          console.error('还车API失败:', response);
        }
      } catch (error) {
        console.error('调用还车API失败:', error);
        ElMessage.error('还车失败，请检查网络连接或稍后重试');
      }
    };

    const recordPosition = () => {
      if (!isRiding.value || !userPosition.value) return;
      
      // 使用用户的实际位置，确保路径与用户位置匹配
      const newPosition = [...userPosition.value];
      
      // 只有当位置真正发生变化时才添加新的路径点
      if (ridingPath.value.length === 0 || 
          Math.abs(ridingPath.value[ridingPath.value.length - 1][0] - newPosition[0]) > 0.000001 ||
          Math.abs(ridingPath.value[ridingPath.value.length - 1][1] - newPosition[1]) > 0.000001) {
        
        ridingPath.value.push(newPosition);
        currentPosition.value = newPosition;
        
        // 计算距离
        if (ridingPath.value.length > 1) {
          const lastPosition = ridingPath.value[ridingPath.value.length - 2];
          const distance = calculateDistance(lastPosition, newPosition);
          ridingDistance.value += distance;
        }
        
        // 更新地图上的路径
        updateRidingPath();
      }
    };

    const updateRidingPath = async () => {
      if (!map.value || ridingPath.value.length < 2) return;
      
      try {
        // 清除之前的路径
        if (ridingPathPolyline.value) {
          ridingPathPolyline.value.setMap(null);
        }
        
        // 创建新的路径
        const AMap = await AMapLoader.load({
          key: '7a9ebfd8db9264a7f90b65369bd2970a',
          version: '2.0'
        });
        
        ridingPathPolyline.value = new AMap.Polyline({
          path: ridingPath.value,
          strokeColor: '#FF5722',
          strokeWeight: 4,
          strokeOpacity: 0.8,
          strokeStyle: 'solid',
          lineJoin: 'round',
          lineCap: 'round',
          zIndex: 60,
          map: map.value
        });
      } catch (error) {
        console.error('更新骑行路径失败:', error);
      }
    };

    const calculateDistance = (pos1, pos2) => {
      const R = 6371; // 地球半径（公里）
      const dLat = (pos2[1] - pos1[1]) * Math.PI / 180;
      const dLon = (pos2[0] - pos1[0]) * Math.PI / 180;
      const a = 
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(pos1[1] * Math.PI / 180) * Math.cos(pos2[1] * Math.PI / 180) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      return R * c * 1000; // 返回米
    };

    const formatTime = (seconds) => {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const secs = seconds % 60;
      
      if (hours > 0) {
        return `${hours}小时${minutes}分${secs}秒`;
      } else if (minutes > 0) {
        return `${minutes}分${secs}秒`;
      } else {
        return `${secs}秒`;
      }
    };

    const formatDistance = (meters) => {
      if (meters >= 1000) {
        return `${(meters / 1000).toFixed(2)}km`;
      } else {
        return `${meters.toFixed(0)}m`;
      }
    };

    const formatPosition = (position) => {
      if (!position || position.length < 2) return '未知位置';
      return `${position[0].toFixed(6)}, ${position[1].toFixed(6)}`;
    };

    const calculateFee = () => {
      // 计费逻辑：每分钟0.5元，不足1分钟按1分钟计费，最低1元
      const pricePerMinute = 0.5;
      const minutes = Math.ceil(ridingTime.value / 60); // 不足1分钟按1分钟计
      const fee = minutes * pricePerMinute;
      const minFee = 1; // 最低1元
      return Math.max(fee, minFee).toFixed(2);
    };

    // 收起骑车面板
    const collapseRidePanel = () => {
      isRidePanelCollapsed.value = true;
    };

    // 展开骑车面板
    const expandRidePanel = () => {
      isRidePanelCollapsed.value = false;
    };

    const cancelRide = () => {
      // 如果正在骑行，则收起而不是关闭
      if (isRiding.value) {
        collapseRidePanel();
        ElMessage.info('正在骑行中，已收起面板。点击左侧圆形按钮可重新展开。');
        return;
      }
      
      // 如果没有骑行，则正常关闭
      if (ridingTimer.value) {
        stopRiding();
      }
      isRidePanelCollapsed.value = false; // 重置收起状态
      emit('update:showRide', false);
    };

    // 模拟移动相关方法
    
    // 开始模拟移动
    const startSimulation = (path = null) => {
      // 如果没有提供路径，尝试使用导航路径
      const pathToUse = path || (navigationPolyline.value ? 
        navigationPolyline.value.getPath().map(point => [point.getLng(), point.getLat()]) : 
        []);
      
      if (!pathToUse || pathToUse.length < 2) {
        ElMessage.warning('请先规划导航路线或提供有效路径');
        return;
      }
      
      simulationPath.value = pathToUse;
      simulationIndex.value = 0;
      simulationProgress.value = 0;
      isSimulating.value = true;
      
      // 设置起始位置
      if (simulationPath.value.length > 0) {
        userPosition.value = [...simulationPath.value[0]];
        updateUserPositionMarker();
        
        // 如果正在导航，立即更新导航路径
        if (isNavigating()) {
          // 清除防抖定时器，立即执行更新
          if (navigationUpdateTimer.value) {
            clearTimeout(navigationUpdateTimer.value);
            navigationUpdateTimer.value = null;
          }
          // 立即更新导航路径
          setTimeout(() => updateNavigationRoute(), 100);
        }
      }
      
      // 开始定时器（每100ms更新一次）
      simulationTimer.value = setInterval(updateSimulationPosition, 100);
      
      ElMessage.success(`开始模拟移动，路径包含 ${pathToUse.length} 个点`);
    };
    
    // 暂停/恢复模拟移动
    const toggleSimulation = () => {
      if (!isSimulating.value) return;
      
      if (simulationTimer.value) {
        clearInterval(simulationTimer.value);
        simulationTimer.value = null;
        ElMessage.info('模拟移动已暂停');
      } else {
        simulationTimer.value = setInterval(updateSimulationPosition, 100);
        ElMessage.info('模拟移动已恢复');
      }
    };
    
    // 停止模拟移动
    const stopSimulation = () => {
      if (simulationTimer.value) {
        clearInterval(simulationTimer.value);
        simulationTimer.value = null;
      }
      isSimulating.value = false;
      simulationIndex.value = 0;
      simulationProgress.value = 0;
      simulationPath.value = [];
      ElMessage.success('模拟移动已停止');
    };
    
    // 更新模拟位置
    const updateSimulationPosition = () => {
      if (!isSimulating.value || simulationPath.value.length < 2) return;
      
      const currentIndex = simulationIndex.value;
      const nextIndex = currentIndex + 1;
      
      // 检查是否到达路径末尾
      if (nextIndex >= simulationPath.value.length) {
        stopSimulation();
        ElMessage.success('模拟移动完成');
        return;
      }
      
      const currentPoint = simulationPath.value[currentIndex];
      const nextPoint = simulationPath.value[nextIndex];
      
      // 计算这一段的距离（米）
      const segmentDistance = calculateDistance(currentPoint, nextPoint);
      
      // 计算速度（米/秒）
      const speedMS = (simulationSpeed.value * 1000) / 3600; // km/h 转 m/s
      
      // 计算每次更新的进度增量（基于100ms更新间隔）
      const progressIncrement = (speedMS * 0.1) / segmentDistance; // 0.1秒 = 100ms
      
      // 更新进度
      simulationProgress.value += progressIncrement;
      
      // 如果当前段走完，移动到下一段
      if (simulationProgress.value >= 1) {
        simulationIndex.value++;
        simulationProgress.value = 0;
        return; // 下次更新时处理新段
      }
      
      // 计算当前插值位置
      const newPosition = interpolatePosition(currentPoint, nextPoint, simulationProgress.value);
      userPosition.value = newPosition;
      
      // 更新地图上的用户位置标记
      updateUserPositionMarker();
      
      // 如果正在骑行，更新骑行数据
      if (isRiding.value) {
        currentPosition.value = newPosition;
        ridingPath.value.push([...newPosition]);
        
        // 计算累计距离
        if (ridingPath.value.length > 1) {
          const lastPos = ridingPath.value[ridingPath.value.length - 2];
          const distance = calculateDistance(lastPos, newPosition);
          ridingDistance.value += distance;
        }
        
        // 更新骑行路径
        updateRidingPath();
      }
    };
    
    // 两点间插值计算
    const interpolatePosition = (point1, point2, progress) => {
      const lng = point1[0] + (point2[0] - point1[0]) * progress;
      const lat = point1[1] + (point2[1] - point1[1]) * progress;
      return [lng, lat];
    };
    
    // 更新用户位置标记
    const updateUserPositionMarker = () => {
      if (userPositionMarker.value && userPosition.value) {
        userPositionMarker.value.setPosition(userPosition.value);
        
        // 如果正在导航，更新导航路径
        if (isNavigating()) {
          updateNavigationRoute();
        }
        
        // 可选：让地图跟随用户移动（注释掉以避免干扰）
        // if (map.value) {
        //   map.value.setCenter(userPosition.value);
        // }
      }
    };

    // 检查是否正在导航
    const isNavigating = () => {
      return props.showNavigation && 
             !isNavigationPanelCollapsed.value && 
             routeInfo.value && 
             endPoint.value;
    };

    // 更新导航路径（从当前用户位置重新规划到终点）- 带防抖
    const updateNavigationRoute = () => {
      if (!userPosition.value || !endPoint.value) return;
      
      // 清除之前的定时器
      if (navigationUpdateTimer.value) {
        clearTimeout(navigationUpdateTimer.value);
      }
      
      // 设置防抖定时器（1秒后执行，避免过于频繁的API调用）
      navigationUpdateTimer.value = setTimeout(async () => {
        try {
          // 更新起点为当前用户位置
          startPoint.value = {
            lng: userPosition.value[0],
            lat: userPosition.value[1]
          };

          // 更新起点标记位置
          if (navigationMarkers.value[0]) {
            navigationMarkers.value[0].setPosition([startPoint.value.lng, startPoint.value.lat]);
          }

          // 重新计算路线（静默模式，不显示消息）
          await calculateRouteQuietly();
          
        } catch (error) {
          console.warn('更新导航路径失败:', error);
          // 静默失败，不显示错误消息以避免干扰用户体验
        }
      }, 1000); // 1秒防抖
    };

    // 静默计算路线（不显示成功/失败消息）
    const calculateRouteQuietly = async () => {
      if (!startPoint.value || !endPoint.value) return;

      // 验证坐标值是否有效
      if (!isValidCoordinate(startPoint.value) || !isValidCoordinate(endPoint.value)) {
        return;
      }

      try {
        // 准备起点和终点坐标数组
        const start = [startPoint.value.lng, startPoint.value.lat];
        const end = [endPoint.value.lng, endPoint.value.lat];

        // 调用封装好的路径规划服务
        const result = await getRidingRoute(start, end);

        if (result.status === 'complete' && result.route) {
          // 更新路线信息
          routeInfo.value = {
            distance: (result.route.distance / 1000).toFixed(1) + 'km',
            time: Math.ceil(result.route.duration / 60)
          };

          // 重新初始化地图API用于显示路线
          const AMap = await AMapLoader.load({
            key: '7a9ebfd8db9264a7f90b65369bd2970a',
            version: '2.0',
            plugins: ['AMap.Riding']
          });

          // 创建完整的导航路径，包括连接线段
          const completeNavigationPath = [];
          
          // 1. 从用户当前位置到路径起点的连接线
          if (userPosition.value) {
            completeNavigationPath.push(new AMap.LngLat(userPosition.value[0], userPosition.value[1]));
          }
          
          // 2. 添加规划的路径点
          result.route.polyline.forEach(point => {
            completeNavigationPath.push(new AMap.LngLat(point[0], point[1]));
          });
          
          // 3. 从路径终点到设置终点的连接线
          if (endPoint.value) {
            completeNavigationPath.push(new AMap.LngLat(endPoint.value.lng, endPoint.value.lat));
          }

          // 更新路线折线
          if (navigationPolyline.value) {
            navigationPolyline.value.setPath(completeNavigationPath);
          }
        }
      } catch (error) {
        // 静默处理错误
        console.warn('静默路线规划失败:', error);
      }
    };

    // 更新用户数据
    const updateUserData = async (completeRideInfo) => {
      if (!props.userInfo || !props.authToken) {
        console.warn('用户信息或认证令牌缺失，跳过数据更新');
        return;
      }

      try {
        // 计算本次骑行的时长（分钟）和费用
        let rideDurationMinutes = 0;
        let rideCost = 0;

        if (completeRideInfo.durationMinutes !== undefined && completeRideInfo.durationMinutes !== null) {
          // 使用后端返回的骑行时长
          rideDurationMinutes = completeRideInfo.durationMinutes;
        } else if (completeRideInfo.localRidingTime) {
          // 使用本地计时的时长（转换为分钟）
          rideDurationMinutes = Math.ceil(completeRideInfo.localRidingTime / 60);
        }

        if (completeRideInfo.cost !== undefined && completeRideInfo.cost !== null) {
          // 使用后端返回的费用
          rideCost = parseFloat(completeRideInfo.cost);
        } else if (completeRideInfo.localFee) {
          // 使用本地计算的费用
          rideCost = parseFloat(completeRideInfo.localFee);
        }

        // 计算新的用户数据
        const updatedUserData = {
          userid: props.userInfo.userid,
          username: props.userInfo.username,
          phoneNumber: props.userInfo.phoneNumber,
          totalRides: (props.userInfo.totalRides || 0) + 1, // 总骑行数+1
          totalDurationMinutes: (props.userInfo.totalDurationMinutes || 0) + rideDurationMinutes, // 累加骑行时长
          totalCost: parseFloat(((props.userInfo.totalCost || 0) + rideCost).toFixed(2)) // 累加总费用
        };

        console.log('更新用户数据:', updatedUserData);
        console.log('本次骑行时长（分钟）:', rideDurationMinutes);
        console.log('本次骑行费用:', rideCost);

        // 调用API更新用户信息
        const response = await updateUserProfile(props.authToken, updatedUserData);
        
        if (response.code === 200 || response.code === '200') {
          console.log('用户数据更新成功');
          // 发射事件通知父组件更新用户信息
          emit('user-data-updated', updatedUserData);
        } else {
          console.error('用户数据更新失败:', response.msg);
          ElMessage.warning('用户数据更新失败，但还车成功');
        }
      } catch (error) {
        console.error('更新用户数据时出错:', error);
        ElMessage.warning('用户数据更新失败，但还车成功');
      }
    };





    onMounted(async () => {
      initMap().then(() => {
        setupMapEventListeners();
        initHeatmap(); // 在地图初始化完成后初始化热力图
      });
    });

    onUnmounted(() => {
      if (map.value) {
        map.value.destroy();
      }
      if (infoWindow.value) {
        infoWindow.value.close();
      }
      if (userPositionMarker.value) {
        map.value.remove(userPositionMarker.value);
      }
      // 清理骑行计时器
      if (ridingTimer.value) {
        clearInterval(ridingTimer.value);
        ridingTimer.value = null;
      }
      // 清理模拟移动定时器
      if (simulationTimer.value) {
        clearInterval(simulationTimer.value);
        simulationTimer.value = null;
      }
      // 清理导航更新防抖定时器
      if (navigationUpdateTimer.value) {
        clearTimeout(navigationUpdateTimer.value);
        navigationUpdateTimer.value = null;
      }
      // 清理用户位置标记样式
      const markerStyles = document.querySelectorAll('style');
      markerStyles.forEach(style => {
        if (style.textContent && style.textContent.includes('user-position-marker')) {
          style.remove();
        }
      });
    });

    return {
      map,
      zoomIn,
      zoomOut,
      showBicycleMarkers,
      hideBicycleMarkers,
      showParkingAreas,
      hideParkingAreas,
      // 导航相关方法
      startSelectingPoint,
      calculateRoute,
      clearRoute,
      cancelNavigation,
      // 导航相关状态
      startPoint,
      endPoint,
      isSelectingStart,
      isSelectingEnd,
      routeInfo,
      isNavigationPanelCollapsed,
      selectCurrentPositionAsStart,
      userPosition,
      setUserPosition,
      collapseNavigationPanel,
      expandNavigationPanel,
      // 骑车相关状态
      currentTab,
      bikeId,
      isRiding,
      ridingTime,
      ridingDistance,
      currentPosition,
      ridingPath,
      rideOrderInfo,
      isRidePanelCollapsed,
      // 骑车相关方法
      startRiding,
      stopRiding,
      parkBike,
      recordPosition,
      formatTime,
      formatDistance,
      formatPosition,
      calculateFee,
      cancelRide,
      collapseRidePanel,
      expandRidePanel,
      updateUserData,
      // 模拟移动相关状态和方法
      isSimulating,
      simulationSpeed,
      simulationIndex,
      simulationPath,
      simulationTimer,
      navigationUpdateTimer,
      startSimulation,
      toggleSimulation,
      stopSimulation,
      isNavigating,
      updateNavigationRoute
    };
  }
}
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
}

#map {
  width: 100%;
  height: 100%;
}

/* 导航面板样式 */
.navigation-panel {
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

.navigation-panel.hidden {
  display: none;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.panel-content {
  padding: 16px;
}

.location-inputs {
  margin-bottom: 15px;
}

.start-point, .end-point {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 8px;
}

.start-point span, .end-point span {
  width: 60px;
  color: #666;
}

.navigation-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.route-info {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.route-info p {
  margin: 5px 0;
  color: #666;
}

/* 模拟移动控制样式 */
.simulation-controls {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.simulation-controls h4 {
  margin: 0 0 12px 0;
  color: #495057;
  font-size: 14px;
  font-weight: 600;
}

.simulation-settings {
  margin-bottom: 12px;
}

.speed-control {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.speed-control label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.simulation-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.simulation-status {
  margin-top: 10px;
  padding: 8px;
  background: rgba(24, 144, 255, 0.1);
  border-radius: 4px;
  border-left: 3px solid #1890ff;
}

.simulation-status p {
  margin: 2px 0;
  color: #1890ff;
  font-size: 12px;
}

/* 继承现有的信息窗体样式 */
:deep(.bicycle-info) {
  padding: 10px;
  min-width: 200px;
}

:deep(.bicycle-info h4) {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

:deep(.bicycle-info p) {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

:deep(.bicycle-info strong) {
  color: #333;
}

:deep(.parking-info) {
  padding: 10px;
  min-width: 200px;
}

:deep(.parking-info h4) {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
  font-weight: bold;
}

:deep(.parking-info p) {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

:deep(.parking-info strong) {
  color: #333;
}

.location-button {
  position: absolute;
  right: 15px;
  bottom: 130px;  /* 位于指南针上方，增加间距 */
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
  transition: all 0.3s ease;
}

.location-button:hover {
  background: #f5f5f5;
}

.location-button.hidden {
  display: none;
}

.location-button svg {
  color: #666;
  width: 24px;
  height: 24px;
}

/* 骑车面板样式 */
.ride-panel {
  position: absolute;
  top: 80px;
  left: 100px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 400px;
  min-height: 500px;
  z-index: 1000;
  transition: all 0.3s ease;
}

.ride-panel.hidden {
  display: none;
}

.ride-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.ride-panel .panel-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.header-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.collapse-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  padding: 4px 8px;
}

.collapse-btn svg {
  width: 14px;
  height: 14px;
}

.ride-panel .panel-content {
  padding: 16px;
}

/* 选项卡样式 */
.ride-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
  border-bottom: 2px solid transparent;
}

.tab-item:hover {
  color: #409eff;
  background-color: #f5f7fa;
}

.tab-item.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

/* 找车功能样式 */
.find-bike-section {
  text-align: center;
  padding: 40px 20px;
}

.feature-placeholder {
  color: #666;
  font-size: 14px;
}

.feature-placeholder p {
  margin: 8px 0;
}

/* 用车功能样式 */
.use-bike-section {
  padding: 10px 0;
}

.bike-input-section {
  margin-bottom: 20px;
}

.input-group {
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.riding-message {
  text-align: center;
  padding: 15px;
  background-color: #f0f9ff;
  border: 1px solid #409eff;
  border-radius: 6px;
  margin-top: 10px;
}

.riding-message p {
  margin: 0;
  color: #409eff;
  font-size: 14px;
}

.riding-status {
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.order-info {
  background: #fff7f3;
  border: 1px solid #fed7aa;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
}

.order-info h4 {
  margin: 0 0 10px 0;
  color: #ea580c;
  font-size: 14px;
  font-weight: 600;
}

.order-info p {
  margin: 4px 0;
  color: #333;
  font-size: 13px;
}

.status-info {
  margin-bottom: 10px;
}

.status-info p {
  margin: 5px 0;
  color: #333;
  font-size: 14px;
}

.current-position {
  font-size: 12px;
  color: #666;
  word-break: break-all;
}

.not-riding {
  text-align: center;
  color: #666;
  padding: 20px;
  font-size: 14px;
}

/* 还车功能样式 */
.return-bike-section {
  padding: 10px 0;
}

.return-actions {
  text-align: center;
}

.return-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.return-info p {
  margin: 5px 0;
  color: #333;
  font-size: 14px;
}

.return-buttons {
  display: flex;
  justify-content: center;
}

.no-riding {
  text-align: center;
  color: #666;
  padding: 20px;
  font-size: 14px;
}

/* 导航圆形收起组件样式 */
.navigation-collapsed-button {
  position: absolute;
  left: 20px;
  top: 40%;
  transform: translateY(-50%);
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(79, 172, 254, 0.4), 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: visible;
}

.navigation-collapsed-button:hover {
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 6px 25px rgba(79, 172, 254, 0.5), 0 4px 12px rgba(0, 0, 0, 0.2);
}

.navigation-collapsed-button.hidden {
  display: none;
}

.navigation-collapsed-button .collapsed-icon {
  color: white;
  transition: transform 0.3s ease;
}

.navigation-collapsed-button:hover .collapsed-icon {
  transform: scale(1.1);
}

.collapsed-route-indicator {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  background: #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.route-pulse {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: pulse-route 2s infinite;
}

@keyframes pulse-route {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(0.8);
    opacity: 1;
  }
}

/* 骑车圆形收起组件样式 */
.ride-collapsed-button {
  position: absolute;
  left: 20px;
  top: 60%;
  transform: translateY(-50%);
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4), 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: visible;
}

.ride-collapsed-button:hover {
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5), 0 4px 12px rgba(0, 0, 0, 0.2);
}

.ride-collapsed-button.hidden {
  display: none;
}

.collapsed-icon {
  color: white;
  transition: transform 0.3s ease;
}

.ride-collapsed-button:hover .collapsed-icon {
  transform: scale(1.1);
}

.collapsed-riding-indicator {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  background: #ff4757;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.riding-pulse {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: pulse-riding 1.5s infinite;
}

@keyframes pulse-riding {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(0.8);
    opacity: 1;
  }
}

/* 自定义骑行完成弹窗样式 */
:deep(.custom-ride-complete-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: none;
}

:deep(.custom-ride-complete-dialog .el-message-box__header) {
  display: none;
}

:deep(.custom-ride-complete-dialog .el-message-box__content) {
  padding: 0;
  text-align: left;
}

:deep(.custom-ride-complete-dialog .el-message-box__message) {
  margin: 0;
}

:deep(.custom-ride-complete-dialog .el-message-box__btns) {
  padding: 15px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.custom-ride-complete-dialog .el-button--primary) {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
  padding: 8px 24px;
  border-radius: 20px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

:deep(.custom-ride-complete-dialog .el-button--primary:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(255, 255, 255, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ride-panel {
    width: 95vw;
    left: 2.5vw;
    top: 60px;
  }
  
  .tab-item {
    font-size: 13px;
    padding: 8px 4px;
  }
  
  .status-info p {
    font-size: 13px;
  }
  
  .current-position {
    font-size: 11px;
  }
  
  .navigation-collapsed-button {
    width: 50px;
    height: 50px;
    left: 15px;
    top: 35%;
  }
  
  .ride-collapsed-button {
    width: 50px;
    height: 50px;
    left: 15px;
    top: 55%;
  }
  
  .collapsed-route-indicator,
  .collapsed-riding-indicator {
    width: 16px;
    height: 16px;
    top: -3px;
    right: -3px;
    border-width: 2px;
  }
  
  .route-pulse,
  .riding-pulse {
    width: 6px;
    height: 6px;
  }
  
  /* 移动端骑行完成弹窗样式 */
  :deep(.custom-ride-complete-dialog) {
    margin: 20px;
    max-width: calc(100vw - 40px);
  }
  
  :deep(.custom-ride-complete-dialog .el-message-box__btns) {
    padding: 12px 15px;
  }
  
  :deep(.custom-ride-complete-dialog .el-button--primary) {
    padding: 6px 20px;
    font-size: 14px;
  }
}
</style> 