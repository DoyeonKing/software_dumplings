// 高德地图 API Keys 说明：
// 1. Web端 Key (key-webJS): 7a9ebfd8db9264a7f90b65369bd2970a
//    用于前端地图显示和基础交互功能（JavaScript API、地图组件等）
//
// 2. Web服务 Key (key-web): 4c4409cdbe818ceb94f8660e2e111563
//    用于后端服务调用（路径规划、搜索、地理编码等），不要在前端直接使用

<template>
  <div class="map-container">
    <div ref="mapContainer" class="map"></div>

    <!-- 设置当前位置按钮 -->
    <div class="location-controls">
      <el-tooltip content="设置当前位置" placement="left">
        <el-button
          :type="isSettingLocation ? 'primary' : 'default'"
          :icon="LocationInformation"
          circle
          size="large"
          @click="toggleLocationSetting"
          class="location-button"
        />
      </el-tooltip>
      <el-tooltip v-if="workerLocation.lat && workerLocation.lng" content="定位到当前位置" placement="left">
        <el-button
          type="success"
          :icon="Aim"
          circle
          size="large"
          @click="centerToWorkerLocation"
          class="center-button"
        />
      </el-tooltip>
    </div>

    <!-- 任务详情面板 -->
    <div v-if="selectedTaskId" class="task-panel">
      <div class="panel-header">
        <h3>任务详情</h3>
        <el-button type="text" @click="closeTaskPanel">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="panel-content">
        <div class="task-info">
          <p>任务编号: {{ selectedTaskId }}</p>
          <p>状态: {{ getTaskStatus }}</p>
          <p>车辆数量: {{ getTaskBikeCount }}</p>
        </div>
        <div class="task-actions">
          <el-button
              type="primary"
              size="small"
              @click="handleAcceptTask"
              v-if="canAcceptTask"
          >
            接受任务
          </el-button>
          <el-button
              type="success"
              size="small"
              @click="handleCompleteTask"
              v-if="canCompleteTask"
          >
            完成任务
          </el-button>
        </div>
      </div>
    </div>

    <!-- 位置设置提示 -->
    <div v-if="isSettingLocation" class="location-hint">
      <el-alert
        title="点击地图设置当前位置"
        description="点击地图上的任意位置来设置您的当前位置，再次点击右下角按钮可取消"
        type="info"
        :closable="false"
        show-icon
      />
    </div>

    <!-- 导航信息面板 -->
    <div v-if="isNavigating && navigationRoutes.length > 0" class="navigation-panel" :class="{ 'collapsed': isNavigationPanelCollapsed, 'minimized': isNavigationPanelMinimized }">
      <!-- 最小化状态的小组件 -->
      <div v-if="isNavigationPanelMinimized" class="minimized-widget" @click="restoreNavigationPanel">
        <div class="widget-icon">🧭</div>
        <div class="widget-text">导航中</div>
      </div>
      
      <!-- 完整面板 -->
      <div v-else>
        <div class="panel-header">
          <h3>🧭 导航路径</h3>
          <div class="panel-controls">
            <el-button type="text" @click="minimizeNavigationPanel" class="control-btn">
              <el-icon><Minus /></el-icon>
            </el-button>
            <el-button type="text" @click="toggleNavigationPanel" class="control-btn">
              <el-icon>
                <component :is="isNavigationPanelCollapsed ? 'ArrowDown' : 'ArrowUp'" />
              </el-icon>
            </el-button>
            <el-button type="text" @click="endNavigation" class="control-btn end-btn">
              结束导航
            </el-button>
          </div>
        </div>
        <div v-if="!isNavigationPanelCollapsed" class="panel-content">
          <div class="navigation-info">
            <div class="task-info">
              <p><strong>任务 #{{ currentNavigationTask?.taskId }}</strong></p>
              <p>起点：{{ currentNavigationTask?.startGeohash }}</p>
              <p>终点：{{ currentNavigationTask?.endGeohash }}</p>
            </div>
            
            <div class="route-details">
              <div 
                v-for="(route, index) in navigationRoutes" 
                :key="index"
                class="route-item"
                :class="{ 'route-1': index === 0, 'route-2': index === 1 }"
              >
                <div class="route-header">
                  <span class="route-color" :style="{ backgroundColor: index === 0 ? '#FF6B6B' : '#4ECDC4' }"></span>
                  <span class="route-name">{{ route.name }}</span>
                </div>
                <div class="route-stats">
                  <span>{{ formatDistance(route.distance) }}</span>
                  <span>{{ formatDuration(route.duration) }}</span>
                </div>
              </div>
            </div>
            
            <div class="navigation-summary" v-if="navigationRoutes.length === 2">
              <div class="summary-item">
                <span class="summary-label">总距离：</span>
                <span class="summary-value">{{ formatDistance(navigationRoutes[0].distance + navigationRoutes[1].distance) }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">总时间：</span>
                <span class="summary-value">{{ formatDuration(navigationRoutes[0].duration + navigationRoutes[1].duration) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';
import parkingIcon from '@/components/icons/parking_area.png';
import bicycleIcon from '@/components/icons/bicycle.png';
import { getAllTasks, acceptTask, completeTask } from '@/api/assignment/task';
import { getAllParkingAreas, getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking';
import { getMapAreaBicycles } from '@/api/map/bicycle';
import { getHeatMapData, convertHeatMapData } from '@/api/map/heat';
import { getTaskNavigationRoutes, formatDistance, formatDuration } from '@/api/navigation';
import { ElMessage } from 'element-plus';
import { Close, LocationInformation, Aim, ArrowDown, ArrowUp, Minus } from '@element-plus/icons-vue';


const props = defineProps({
  showBicycles: {
    type: Boolean,
    default: false
  },
  showParkingAreas: {
    type: Boolean,
    default: false
  },
  showHeatmap: {
    type: Boolean,
    default: false
  },
  selectedTaskId: {
    type: String,
    default: null
  }
});

const emit = defineEmits(['update:selectedTaskId']);

// 地图实例和标记
const map = ref(null);
const mapContainer = ref(null);
const bicycleMarkers = ref([]);
const parkingMarkers = ref([]);
const parkingPolygons = ref([]); // 新增：存储停车区域多边形
const taskLines = ref([]);
const selectedTaskLine = ref(null);
const currentTask = ref(null);
const heatmap = ref(null); // 热力图实例

// 工作人员位置相关
const workerLocation = ref({
  lat: null,
  lng: null
});
const workerLocationMarker = ref(null);
const isSettingLocation = ref(false);
const mapClickListener = ref(null);

// 导航路径相关
const navigationRoutes = ref([]);
const navigationPolylines = ref([]);
const isNavigating = ref(false);
const currentNavigationTask = ref(null);
const isNavigationPanelCollapsed = ref(false);
const isNavigationPanelMinimized = ref(false);
const isUpdatingMapView = ref(false);

// 计算属性
const getTaskStatus = computed(() => {
  if (!currentTask.value) return '';
  return currentTask.value.status;
});

const getTaskBikeCount = computed(() => {
  if (!currentTask.value) return 0;
  return currentTask.value.bike_count;
});

const canAcceptTask = computed(() => {
  if (!currentTask.value) return false;
  return currentTask.value.status === 'pending';
});

const canCompleteTask = computed(() => {
  if (!currentTask.value) return false;
  return currentTask.value.status === 'in_progress';
});

// 工作人员位置相关方法
const createWorkerLocationMarker = () => {
  if (!map.value || !workerLocation.value.lat || !workerLocation.value.lng) return;

  // 如果已存在标记，先移除
  if (workerLocationMarker.value) {
    workerLocationMarker.value.setMap(null);
  }

  // 创建工作人员位置标记
  workerLocationMarker.value = new AMap.Marker({
    position: [workerLocation.value.lng, workerLocation.value.lat],
    icon: new AMap.Icon({
      image: 'data:image/svg+xml;base64,' + btoa(`
        <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40">
          <circle cx="20" cy="20" r="18" fill="#1890ff" stroke="#fff" stroke-width="3"/>
          <circle cx="20" cy="20" r="8" fill="#fff"/>
          <circle cx="20" cy="20" r="4" fill="#1890ff"/>
        </svg>
      `),
      size: new AMap.Size(40, 40),
      imageSize: new AMap.Size(40, 40)
    }),
    offset: new AMap.Pixel(-20, -20),
    title: '当前位置',
    map: map.value,
    zIndex: 200 // 确保在其他标记之上
  });

  // 添加点击事件显示位置信息
  workerLocationMarker.value.on('click', () => {
    const info = new AMap.InfoWindow({
      content: `
        <div class="info-window">
          <h4>🚴 工作人员当前位置</h4>
          <p>纬度: ${workerLocation.value.lat.toFixed(6)}</p>
          <p>经度: ${workerLocation.value.lng.toFixed(6)}</p>
          <p>点击右下角按钮可重新设置位置</p>
        </div>
      `,
      offset: new AMap.Pixel(0, -30)
    });

    info.open(map.value, workerLocationMarker.value.getPosition());
  });
};

const toggleLocationSetting = () => {
  isSettingLocation.value = !isSettingLocation.value;

  if (isSettingLocation.value) {
    // 开始设置位置模式
    ElMessage.info('请点击地图上的位置来设置您的当前位置');
    
    // 添加地图点击监听器
    mapClickListener.value = map.value.on('click', (e) => {
      const { lng, lat } = e.lnglat;
      
      // 更新工作人员位置
      workerLocation.value = { lat, lng };
      
      // 创建或更新位置标记
      createWorkerLocationMarker();
      
      // 退出设置模式
      isSettingLocation.value = false;
      
      // 移除点击监听器
      if (mapClickListener.value) {
        map.value.off('click', mapClickListener.value);
        mapClickListener.value = null;
      }
      
      // ElMessage.success(`位置已设置: ${lat.toFixed(6)}, ${lng.toFixed(6)}`);
    });
  } else {
    // 取消设置位置模式
    if (mapClickListener.value) {
      map.value.off('click', mapClickListener.value);
      mapClickListener.value = null;
    }
    ElMessage.info('已取消位置设置');
  }
};

const centerToWorkerLocation = () => {
  if (!map.value || !workerLocation.value.lat || !workerLocation.value.lng) {
    ElMessage.warning('尚未设置工作人员位置');
    return;
  }

  // 将地图中心移动到工作人员位置
  map.value.setCenter([workerLocation.value.lng, workerLocation.value.lat]);
  map.value.setZoom(18); // 放大到合适的级别
  
  ElMessage.success('已定位到当前位置');
};

// 初始化默认位置
const initDefaultWorkerLocation = () => {
  // 设置一个默认位置（深圳市南山区）
  workerLocation.value = {
    lat: 22.547,
    lng: 114.085947
  };
  
  // 创建位置标记
  setTimeout(() => {
    createWorkerLocationMarker();
  }, 1000); // 等待地图完全加载
};

// 导航路径相关方法
const showNavigationRoutes = async (task) => {
  try {
    // 检查工作人员位置是否已设置
    if (!workerLocation.value.lat || !workerLocation.value.lng) {
      ElMessage.warning('请先设置工作人员当前位置');
      return;
    }

    // 检查任务数据
    if (!task || !task.startGeohash || !task.endGeohash) {
      ElMessage.error('任务数据不完整，无法规划路径');
      return;
    }

    isNavigating.value = true;
    currentNavigationTask.value = task;
    
    ElMessage.info('正在规划导航路径...');

    // 获取导航路径
    const navigationResult = await getTaskNavigationRoutes(
      workerLocation.value,
      task.startGeohash,
      task.endGeohash
    );

    if (!navigationResult.success) {
      throw new Error(navigationResult.error || '路径规划失败');
    }

    navigationRoutes.value = navigationResult.routes;

    // 清除现有的导航路径线条（保留状态）
    navigationPolylines.value.forEach(item => {
      if (item && typeof item.setMap === 'function') {
        item.setMap(null);
      }
    });
    navigationPolylines.value = [];

    // 在地图上绘制路径
    const colors = ['#FF6B6B', '#4ECDC4']; // 红色和青色区分两条路径
    const routeNames = ['工作人员到起点', '起点到终点'];

    navigationResult.routes.forEach((route, index) => {
      console.log(`绘制路径 ${index + 1}:`, route.name, '坐标点数量:', route.coordinates?.length);
      
      if (route.coordinates && route.coordinates.length > 0) {
        // 确保坐标格式正确
        const validCoordinates = route.coordinates.filter(coord => 
          Array.isArray(coord) && coord.length === 2 && 
          !isNaN(coord[0]) && !isNaN(coord[1])
        );
        
        console.log(`有效坐标点数量:`, validCoordinates.length);
        console.log(`前3个坐标点:`, validCoordinates.slice(0, 3));
        
        if (validCoordinates.length < 2) {
          console.error(`路径 ${index + 1} 坐标点不足，无法绘制`);
          return;
        }

        const polyline = new AMap.Polyline({
          path: validCoordinates,
          strokeColor: colors[index],
          strokeWeight: 6,
          strokeOpacity: 0.8,
          strokeStyle: 'solid',
          lineJoin: 'round',
          lineCap: 'round',
          zIndex: 100
        });

        polyline.setMap(map.value);
        navigationPolylines.value.push(polyline);
        
        console.log(`路径 ${index + 1} 已添加到地图`);

                 // 添加起点和终点标记
         if (index === 0) {
           // 第一条路径：工作人员到起点
           const startMarker = new AMap.Marker({
             position: route.coordinates[route.coordinates.length - 1],
             icon: new AMap.Icon({
               image: 'data:image/svg+xml;base64,' + btoa(`
                 <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
                   <circle cx="16" cy="16" r="14" fill="#FF6B6B" stroke="#fff" stroke-width="2"/>
                   <text x="16" y="20" text-anchor="middle" fill="white" font-size="12" font-weight="bold">S</text>
                 </svg>
               `),
               size: new AMap.Size(32, 32),
               imageSize: new AMap.Size(32, 32)
             }),
             offset: new AMap.Pixel(-16, -16),
             title: `任务起点 (${task.startGeohash})`,
             map: map.value,
             zIndex: 150
           });
           navigationPolylines.value.push(startMarker);
         } else {
           // 第二条路径：起点到终点
           const endMarker = new AMap.Marker({
             position: route.coordinates[route.coordinates.length - 1],
             icon: new AMap.Icon({
               image: 'data:image/svg+xml;base64,' + btoa(`
                 <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
                   <circle cx="16" cy="16" r="14" fill="#4ECDC4" stroke="#fff" stroke-width="2"/>
                   <text x="16" y="20" text-anchor="middle" fill="white" font-size="12" font-weight="bold">E</text>
                 </svg>
               `),
               size: new AMap.Size(32, 32),
               imageSize: new AMap.Size(32, 32)
             }),
             offset: new AMap.Pixel(-16, -16),
             title: `任务终点 (${task.endGeohash})`,
             map: map.value,
             zIndex: 150
           });
           navigationPolylines.value.push(endMarker);
         }
      }
    });

    // 调整地图视野以显示完整路径
    if (navigationPolylines.value.length > 0) {
      try {
        const bounds = new AMap.Bounds();
        
        // 添加工作人员位置
        bounds.extend([workerLocation.value.lng, workerLocation.value.lat]);
        
        // 添加所有路径点（采样方式，避免处理过多点）
        navigationResult.routes.forEach(route => {
          if (route.coordinates && route.coordinates.length > 0) {
            // 每隔10个点取一个，减少计算量
            for (let i = 0; i < route.coordinates.length; i += 10) {
              const coord = route.coordinates[i];
              if (Array.isArray(coord) && coord.length === 2 && 
                  !isNaN(coord[0]) && !isNaN(coord[1])) {
                bounds.extend(coord);
              }
            }
            // 确保包含起点和终点
            const firstCoord = route.coordinates[0];
            const lastCoord = route.coordinates[route.coordinates.length - 1];
            if (firstCoord && Array.isArray(firstCoord) && !isNaN(firstCoord[0]) && !isNaN(firstCoord[1])) {
              bounds.extend(firstCoord);
            }
            if (lastCoord && Array.isArray(lastCoord) && !isNaN(lastCoord[0]) && !isNaN(lastCoord[1])) {
              bounds.extend(lastCoord);
            }
          }
        });
        
        // 使用更温和的视野调整方式
        setTimeout(() => {
          if (map.value && bounds.isValid() && !isUpdatingMapView.value) {
            isUpdatingMapView.value = true;
            try {
              map.value.setBounds(bounds, false, [80, 80, 80, 80]);
            } catch (error) {
              console.error('设置地图边界失败:', error);
            } finally {
              // 重置状态，确保地图交互恢复
              setTimeout(() => {
                isUpdatingMapView.value = false;
              }, 500);
            }
          }
        }, 300);
      } catch (error) {
        console.error('调整地图视野失败:', error);
      }
    }

    // 显示导航信息
    const summary = navigationResult.summary;
    const message = `导航路径规划完成！\n总距离：${formatDistance(summary.totalDistance)}\n预计时间：${formatDuration(summary.totalDuration)}`;
    ElMessage.success(message);

    console.log('导航路径显示成功:', navigationResult);

  } catch (error) {
    console.error('导航路径规划失败:', error);
    ElMessage.error(`导航路径规划失败: ${error.message}`);
    isNavigating.value = false;
    currentNavigationTask.value = null;
  }
};

const clearNavigationRoutes = () => {
  // 清除地图上的导航路径和标记
  navigationPolylines.value.forEach(item => {
    if (item && typeof item.setMap === 'function') {
      try {
        item.setMap(null);
      } catch (error) {
        console.warn('清除地图元素失败:', error);
      }
    }
  });
  
  navigationPolylines.value = [];
  navigationRoutes.value = [];
  isNavigating.value = false;
  currentNavigationTask.value = null;
  isNavigationPanelCollapsed.value = false;
  isNavigationPanelMinimized.value = false;
  isUpdatingMapView.value = false;
  
  console.log('导航路径已清除');
};

// 切换导航面板收起状态
const toggleNavigationPanel = () => {
  isNavigationPanelCollapsed.value = !isNavigationPanelCollapsed.value;
};

// 最小化导航面板
const minimizeNavigationPanel = () => {
  isNavigationPanelMinimized.value = true;
  isNavigationPanelCollapsed.value = false;
};

// 恢复导航面板
const restoreNavigationPanel = () => {
  isNavigationPanelMinimized.value = false;
  isNavigationPanelCollapsed.value = false;
};

// 结束导航
const endNavigation = () => {
  clearNavigationRoutes();
  ElMessage.success('导航已结束');
};

// 加载单车
const loadBicycles = async () => {
  try {
    // 如果不需要显示单车，清除现有标记
    if (!props.showBicycles) {
      bicycleMarkers.value.forEach(marker => {
        marker.setMap(null);
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
    const bicycles = response.data;

    // 清除现有标记
    bicycleMarkers.value.forEach(marker => {
      marker.setMap(null);
    });
    bicycleMarkers.value = [];

    // 创建新标记
    bicycles.forEach(bicycle => {
      const marker = new AMap.Marker({
        position: [bicycle.currentLon, bicycle.currentLat], // 修改为正确的经纬度字段
        icon: new AMap.Icon({
          image: bicycleIcon,
          size: new AMap.Size(32, 32),
          imageSize: new AMap.Size(32, 32)
        }),
        title: `单车 #${bicycle.bikeId}`,
        map: map.value
      });

      // 添加点击事件
      marker.on('click', () => {
        const info = new AMap.InfoWindow({
          content: `
            <div class="info-window">
              <h4>单车 #${bicycle.bikeId}</h4>
              <p>状态: ${bicycle.bikeStatus}</p>
              <p>最后更新: ${bicycle.lastUpdatedTime}</p>
            </div>
          `,
          offset: new AMap.Pixel(0, -30)
        });

        info.open(map.value, marker.getPosition());
      });

      bicycleMarkers.value.push(marker);
    });
  } catch (error) {
    console.error('Failed to load bicycles:', error);
    ElMessage.error('加载单车失败');
  }
};

// 加载停车点
const loadParkingAreas = async () => {
  try {
    // 如果不需要显示停车点，清除现有标记
    if (!props.showParkingAreas) {
      parkingMarkers.value.forEach(marker => {
        marker.setMap(null);
      });
      parkingPolygons.value.forEach(polygon => {
        polygon.setMap(null);
      });
      return;
    }

    // 清除现有标记和多边形
    parkingMarkers.value.forEach(marker => {
      if (marker && typeof marker.setMap === 'function') {
        marker.setMap(null);
      }
    });
    parkingPolygons.value.forEach(polygon => {
      if (polygon && typeof polygon.setMap === 'function') {
        polygon.setMap(null);
      }
    });
    parkingMarkers.value = [];
    parkingPolygons.value = [];

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
    console.log('工作人员地图-停车点API响应:', response);
    
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

    if (!rawData || !Array.isArray(rawData)) {
      console.warn('工作人员地图-停车点数据格式异常:', response);
      throw new Error('停车点数据格式异常');
    }
    
    // 转换数据格式
    const parkingAreas = convertParkingAreaData(rawData);

    parkingAreas.forEach(area => {
      // 使用中心点坐标（如果有）或者计算停车区域的中心点
      const centerLat = area.center ? area.center[0] : (area.bounds.northeast[0] + area.bounds.southwest[0]) / 2;
      const centerLng = area.center ? area.center[1] : (area.bounds.northeast[1] + area.bounds.southwest[1]) / 2;

      // 使用精确的多边形路径（如果有）或者使用边界框创建矩形
      const path = area.polygonPath || [
        [area.bounds.southwest[1], area.bounds.southwest[0]],  // 左下角
        [area.bounds.northeast[1], area.bounds.southwest[0]],  // 右下角
        [area.bounds.northeast[1], area.bounds.northeast[0]],  // 右上角
        [area.bounds.southwest[1], area.bounds.northeast[0]],  // 左上角
        [area.bounds.southwest[1], area.bounds.southwest[0]]   // 闭合多边形
      ];

      // 根据状态设置颜色
      const statusColors = {
        normal: '#4CAF50',  // 绿色
        full: '#FF9800',    // 橙色
        maintenance: '#F44336'  // 红色
      };

      // 创建多边形
      const polygon = new AMap.Polygon({
        path: path,
        strokeColor: statusColors[area.status] || '#4CAF50',
        strokeWeight: 3,
        strokeOpacity: 1,
        fillColor: statusColors[area.status] || '#4CAF50',
        fillOpacity: 0.4,
        cursor: 'pointer',
        map: props.showParkingAreas ? map.value : null
      });

      // 创建标记
      const marker = new AMap.Marker({
        position: [centerLng, centerLat],
        icon: new AMap.Icon({
          image: parkingIcon,
          size: new AMap.Size(48, 48),     // 增大图标尺寸
          imageSize: new AMap.Size(48, 48)  // 增大图片尺寸
        }),
        offset: new AMap.Pixel(-24, -24),  // 调整偏移以保持居中
        title: area.name,
        map: props.showParkingAreas ? map.value : null
      });

      // 添加多边形悬停效果
      let originalOptions = null;

      polygon.on('mouseover', () => {
        originalOptions = {
          fillOpacity: polygon.getOptions().fillOpacity,
          strokeWeight: polygon.getOptions().strokeWeight
        };

        polygon.setOptions({
          fillOpacity: 0.6,
          strokeWeight: 4
        });

        marker.setzIndex(110);
      });

      polygon.on('mouseout', () => {
        if (originalOptions) {
          polygon.setOptions(originalOptions);
        }
        marker.setzIndex(100);
      });

      // 添加标记悬停效果
      marker.on('mouseover', () => {
        if (!originalOptions) {
          originalOptions = {
            fillOpacity: polygon.getOptions().fillOpacity,
            strokeWeight: polygon.getOptions().strokeWeight
          };
        }

        polygon.setOptions({
          fillOpacity: 0.6,
          strokeWeight: 4
        });

        marker.setzIndex(110);
      });

      marker.on('mouseout', () => {
        if (originalOptions) {
          polygon.setOptions(originalOptions);
        }
        marker.setzIndex(100);
      });

      // 添加点击事件
      const showInfo = () => {
        const info = new AMap.InfoWindow({
          content: `
            <div class="info-window">
              <h4>停车区域 ${area.geohash}</h4>
              <p>区域编号: ${area.geohash}</p>
              <p>区域组ID: ${area.regionGroupId}</p>
              <p>停车容量: ${area.parkingCapacity}个</p>
              <p>中心位置: ${area.centerLat.toFixed(6)}, ${area.centerLon.toFixed(6)}</p>
            </div>
          `,
          offset: new AMap.Pixel(0, -30)
        });

        info.open(map.value, marker.getPosition());
      };

      // 为多边形和标记添加点击事件
      polygon.on('click', showInfo);
      marker.on('click', showInfo);

      parkingMarkers.value.push(marker);
      parkingPolygons.value.push(polygon);
    });
  } catch (error) {
    console.error('Failed to load parking areas:', error);
    ElMessage.error('加载停车点失败');
    
    // 如果API调用失败，使用备用数据
    try {
      const fallbackResponse = await getAllParkingAreas();
      const parkingAreas = fallbackResponse.data;
      
      // 重新创建标记和多边形（使用备用数据）
      parkingAreas.forEach(area => {
        // 计算停车区域的中心点
        const centerLat = (area.bounds.northeast[0] + area.bounds.southwest[0]) / 2;
        const centerLng = (area.bounds.northeast[1] + area.bounds.southwest[1]) / 2;

        // 创建多边形路径
        const path = [
          [area.bounds.southwest[1], area.bounds.southwest[0]],  // 左下角
          [area.bounds.northeast[1], area.bounds.southwest[0]],  // 右下角
          [area.bounds.northeast[1], area.bounds.northeast[0]],  // 右上角
          [area.bounds.southwest[1], area.bounds.northeast[0]],  // 左上角
          [area.bounds.southwest[1], area.bounds.southwest[0]]   // 闭合多边形
        ];

        // 根据状态设置颜色
        const statusColors = {
          normal: '#4CAF50',  // 绿色
          full: '#FF9800',    // 橙色
          maintenance: '#F44336'  // 红色
        };

        // 创建多边形
        const polygon = new AMap.Polygon({
          path: path,
          strokeColor: statusColors[area.status] || '#4CAF50',
          strokeWeight: 3,
          strokeOpacity: 1,
          fillColor: statusColors[area.status] || '#4CAF50',
          fillOpacity: 0.4,
          cursor: 'pointer',
          map: map.value
        });

        // 创建标记
        const marker = new AMap.Marker({
          position: [centerLng, centerLat],
          icon: new AMap.Icon({
            image: parkingIcon,
            size: new AMap.Size(40, 40),
            imageSize: new AMap.Size(40, 40)
          }),
          map: map.value
        });

        parkingMarkers.value.push(marker);
        parkingPolygons.value.push(polygon);
      });
    } catch (fallbackError) {
      console.error('获取备用停车点数据也失败：', fallbackError);
    }
  }
};

// 加载任务线
const loadTasks = async () => {
  try {
    const response = await getAllTasks();
    const tasks = response.data;

    // 清除现有的任务线
    if (taskLines.value.length > 0) {
      map.value.remove(taskLines.value);
      taskLines.value = [];
    }

    tasks.forEach(task => {
      const line = new AMap.Polyline({
        path: [
          [task.start_location.longitude, task.start_location.latitude],
          [task.end_location.longitude, task.end_location.latitude]
        ],
        strokeColor: getTaskColor(task.status),
        strokeWeight: 4,
        strokeOpacity: 0.8,
        extData: task
      });

      // 添加点击事件
      line.on('click', () => {
        currentTask.value = task;
        emit('update:selectedTaskId', task.task_id);
        highlightTask(task.task_id);
      });

      taskLines.value.push(line);
    });

    // 一次性添加所有任务线
    if (taskLines.value.length > 0) {
      map.value.add(taskLines.value);
    }
  } catch (error) {
    console.error('Failed to load tasks:', error);
    ElMessage.error('加载任务失败');
  }
};

// 更新热力图数据
const updateHeatmapData = async () => {
  if (!map.value || !heatmap.value) return;

  try {
    // 获取当前地图边界
    const bounds = map.value.getBounds();
    
    // 验证边界数据的有效性
    const southWest = bounds.getSouthWest();
    const northEast = bounds.getNorthEast();
    
    if (!southWest || !northEast || 
        isNaN(southWest.lat) || isNaN(southWest.lng) || 
        isNaN(northEast.lat) || isNaN(northEast.lng)) {
      console.warn('地图边界数据无效，跳过热力图更新');
      return;
    }
    
    const params = {
      minLat: southWest.lat,
      maxLat: northEast.lat,
      minLon: southWest.lng,
      maxLon: northEast.lng
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
    
    // 验证热力图数据的有效性 - 修复：convertHeatMapData返回的是对象格式 {lng, lat, count}
    const validHeatmapData = heatmapData.filter(point => {
      if (!point || typeof point !== 'object') return false;
      const { lng, lat, count } = point;
      return !isNaN(lng) && !isNaN(lat) && !isNaN(count) && 
             lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90 && count > 0;
    });
    
    if (validHeatmapData.length === 0) {
      console.log('没有有效的热力图数据点');
      return;
    }
    
    // 设置热力图数据 - 高德地图2.0版本的热力图
    try {
      // 尝试使用 setDataSet 方法
      if (typeof heatmap.value.setDataSet === 'function') {
        heatmap.value.setDataSet({
          data: validHeatmapData,
          max: 10 // 调整最大权重值，因为每个单车权重为1
        });
      } else if (typeof heatmap.value.setData === 'function') {
        // 如果没有 setDataSet，尝试使用 setData
        heatmap.value.setData(validHeatmapData);
      } else {
        console.error('热力图API方法未找到，可用方法：', Object.getOwnPropertyNames(heatmap.value));
      }
    } catch (error) {
      console.error('设置热力图数据失败：', error);
    }

    console.log('工作人员热力图数据已更新，单车数量：', bicycleData.length, '有效热力图点数：', validHeatmapData.length);
  } catch (error) {
    console.error('更新热力图数据失败：', error);
    // 不显示错误消息，避免在导航过程中频繁提示
  }
};

// 初始化热力图
const initHeatmap = async () => {
  if (!map.value) return;

  try {
    if (!heatmap.value) {
      // 确保 HeatMap 插件已加载
      if (!window.AMap.HeatMap) {
        console.error('HeatMap 插件未加载');
        ElMessage.error('热力图插件未加载，请刷新页面重试');
        return;
      }

      // 创建热力图实例 - 使用更兼容的配置
      heatmap.value = new window.AMap.HeatMap(map.value, {
        radius: 35, // 增加热力图的半径
        opacity: [0.1, 0.8], // 调整透明度范围
        gradient: {
          0.2: 'blue',
          0.4: 'cyan', 
          0.6: 'lime',
          0.8: 'yellow',
          1.0: 'red'
        },
        zooms: [1, 20], // 支持的缩放级别范围
        visible: true
      });

      console.log('热力图实例创建成功，可用方法：', Object.getOwnPropertyNames(heatmap.value));
    }

    // 获取并设置热力图数据
    await updateHeatmapData();

    // 初始时隐藏热力图
    if (heatmap.value && typeof heatmap.value.hide === 'function') {
      heatmap.value.hide();
    }
    
    // 只有在props.showHeatmap为true时才显示
    if (props.showHeatmap && heatmap.value && typeof heatmap.value.show === 'function') {
      heatmap.value.show();
    }
  } catch (error) {
    console.error('初始化热力图失败：', error);
    ElMessage.error('初始化热力图失败: ' + error.message);
  }
};

// 更新地图样式
const updateMapStyle = (style) => {
  if (!map.value) return;
  const styleMapping = {
    normal: 'amap://styles/normal',
    dark: 'amap://styles/dark',
    light: 'amap://styles/light',
    whitesmoke: 'amap://styles/whitesmoke',
    fresh: 'amap://styles/fresh',
    grey: 'amap://styles/grey'
  };
  map.value.setMapStyle(styleMapping[style] || styleMapping.normal);
};

// 缩放控制
const zoomIn = () => {
  if (!map.value) return;
  map.value.zoomIn();
};

const zoomOut = () => {
  if (!map.value) return;
  map.value.zoomOut();
};

// 监听显示状态变化
watch(() => props.showBicycles, async (show) => {
  if (bicycleMarkers.value.length === 0) {
    await loadBicycles();
  } else {
    bicycleMarkers.value.forEach(marker => {
      marker.setMap(show ? map.value : null);
    });
  }
});

watch(() => props.showParkingAreas, async (show) => {
  if (show) {
    await loadParkingAreas();
  } else {
    parkingMarkers.value.forEach(marker => marker.setMap(null));
    parkingPolygons.value.forEach(polygon => polygon.setMap(null));
  }
});

// 监听热力图显示状态
watch(() => props.showHeatmap, async (newVal) => {
  if (!map.value) return;

  console.log('热力图显示状态变化:', newVal);

  if (newVal) {
    // 如果开启热力图，确保已初始化并显示
    await initHeatmap();
    if (heatmap.value && typeof heatmap.value.show === 'function') {
      heatmap.value.show();
      console.log('热力图已显示');
      // 更新热力图数据
      await updateHeatmapData();
    } else {
      console.error('热力图实例不可用或缺少 show 方法');
    }
  } else if (heatmap.value && typeof heatmap.value.hide === 'function') {
    // 如果关闭热力图，隐藏热力图层
    heatmap.value.hide();
    console.log('热力图已隐藏');
  }
});

// 高亮选中的任务
const highlightTask = (taskId) => {
  // 重置所有任务线的样式
  taskLines.value.forEach(line => {
    const task = line.getExtData();
    line.setOptions({
      strokeColor: getTaskColor(task.status),
      strokeWeight: 4,
      strokeOpacity: 0.8
    });
  });

  // 高亮选中的任务线
  const selectedLine = taskLines.value.find(line => line.getExtData().task_id === taskId);
  if (selectedLine) {
    selectedLine.setOptions({
      strokeWeight: 6,
      strokeOpacity: 1
    });
  }
};

// 获取任务状态对应的颜色
const getTaskColor = (status) => {
  switch (status) {
    case 'pending':
      return '#ff9800';
    case 'in_progress':
      return '#2196f3';
    case 'completed':
      return '#4caf50';
    default:
      return '#999';
  }
};

// 处理任务操作
const handleAcceptTask = async () => {
  if (!currentTask.value) return;
  try {
    await acceptTask(currentTask.value.task_id);
    ElMessage.success('任务已接受');
    await loadTasks();
  } catch (error) {
    console.error('Failed to accept task:', error);
    ElMessage.error('接受任务失败');
  }
};

const handleCompleteTask = async () => {
  if (!currentTask.value) return;
  try {
    await completeTask(currentTask.value.task_id);
    ElMessage.success('任务已完成');
    await loadTasks();
  } catch (error) {
    console.error('Failed to complete task:', error);
    ElMessage.error('完成任务失败');
  }
};

const closeTaskPanel = () => {
  currentTask.value = null;
  emit('update:selectedTaskId', null);
};

// 暴露方法给父组件
defineExpose({
  updateMapStyle,
  zoomIn,
  zoomOut,
  showNavigationRoutes,
  clearNavigationRoutes
});

// 初始化地图
const initMap = async () => {
  try {
    // 处理可能的加载器冲突 - 清理之前的状态
    const cleanupPreviousState = () => {
      // 如果存在来自其他加载器的script，但AMap状态不完整，进行清理
      const existingScript = document.getElementById('amap-script');
      if (existingScript && window.AMap) {
        // 检查AMap对象是否完整（检查必需的插件）
        const hasRequiredPlugins = window.AMap.HeatMap && window.AMap.ControlBar && window.AMap.Scale && window.AMap.ToolBar;
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
      key: '7a9ebfd8db9264a7f90b65369bd2970a',  // Web端 Key
      version: '2.0',
      plugins: [
        'AMap.ControlBar',
        'AMap.Scale',
        'AMap.ToolBar',
        'AMap.HeatMap'
      ],
      securityJsCode: '1751964054605',
      security: {
        serviceHost: 'https://restapi.amap.com'
      }
    });

    // 验证关键插件是否正确加载
    if (!AMap.HeatMap || !AMap.ControlBar || !AMap.Scale || !AMap.ToolBar) {
      throw new Error('必需的地图插件未正确加载');
    }

    map.value = new AMap.Map(mapContainer.value, {
      zoom: 16, // 放大地图比例尺
      center: [114.085947, 22.547],  // 深圳市中心
      viewMode: '2D'
    });

    // 添加地图控件
    map.value.addControl(new AMap.Scale({
      position: { bottom: '20px', left: '20px' }
    }));

    map.value.addControl(new AMap.ToolBar({
      position: { top: '20px', right: '20px' }
    }));

    // 添加地图移动和缩放事件监听
    map.value.on('moveend', () => {
      // 在导航状态或地图更新过程中减少事件处理频率
      if (isNavigating.value || isUpdatingMapView.value) return;
      
      if (props.showBicycles) {
        loadBicycles();
      }
      if (props.showParkingAreas) {
        loadParkingAreas();
      }
      if (props.showHeatmap) {
        updateHeatmapData();
      }
    });

    map.value.on('zoomend', () => {
      // 在导航状态或地图更新过程中减少事件处理频率
      if (isNavigating.value || isUpdatingMapView.value) return;
      
      if (props.showBicycles) {
        loadBicycles();
      }
      if (props.showParkingAreas) {
        loadParkingAreas();
      }
      if (props.showHeatmap) {
        updateHeatmapData();
      }
    });

    // 预加载停车区域数据
    await loadParkingAreas();
    
    // 初始化热力图
    await initHeatmap();

    // 初始化工作人员默认位置
    initDefaultWorkerLocation();
  } catch (error) {
    console.error('Failed to initialize map:', error);
    
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

onMounted(() => {
  initMap();
});

onUnmounted(() => {
  // 清理地图点击监听器
  if (mapClickListener.value && map.value) {
    map.value.off('click', mapClickListener.value);
  }

  // 清理工作人员位置标记
  if (workerLocationMarker.value) {
    workerLocationMarker.value.setMap(null);
  }

  // 清理导航路径
  clearNavigationRoutes();
  
  // 重置所有状态
  isUpdatingMapView.value = false;

  // 清理热力图
  if (heatmap.value) {
    heatmap.value.setMap(null);
    heatmap.value = null;
  }
  
  if (map.value) {
    map.value.destroy();
  }
});
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
}

.location-controls {
  position: absolute;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1000;
}

.location-button {
  width: 50px !important;
  height: 50px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border: 2px solid #fff;
  transition: all 0.3s ease;
}

.location-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.center-button {
  width: 50px !important;
  height: 50px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border: 2px solid #fff;
  transition: all 0.3s ease;
}

.center-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.location-hint {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  max-width: 400px;
}

.navigation-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 320px;
  z-index: 1000;
  max-height: 500px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.navigation-panel.collapsed {
  max-height: 60px;
}

.navigation-panel.minimized {
  width: 80px;
  height: 50px;
  max-height: 50px;
  border-radius: 25px;
  top: 50%;
  transform: translateY(-50%);
}

.minimized-widget {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 50px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 25px;
}

.minimized-widget:hover {
  background-color: #f5f7fa;
  transform: scale(1.05);
}

.widget-icon {
  font-size: 16px;
}

.widget-text {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.navigation-info {
  max-height: 400px;
  overflow-y: auto;
}

.task-info {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.task-info p {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

.route-details {
  margin: 16px 0;
}

.route-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.route-item:last-child {
  border-bottom: none;
}

.route-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.route-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.route-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.route-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  font-size: 12px;
  color: #666;
}

.navigation-summary {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0;
}

.summary-label {
  font-size: 14px;
  color: #666;
}

.summary-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.task-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 300px;
  z-index: 1000;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

.panel-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-btn {
  color: #666;
  font-size: 16px;
  padding: 4px;
  transition: all 0.3s ease;
}

.control-btn:hover {
  color: #409EFF;
  background-color: #f0f9ff;
}

.end-btn {
  color: #f56565;
  font-size: 14px;
  padding: 4px 8px;
}

.end-btn:hover {
  color: #fff;
  background-color: #f56565;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.panel-content {
  padding: 16px;
}

.task-info {
  margin-bottom: 16px;
}

.task-info p {
  margin: 8px 0;
  color: #666;
}

.task-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.info-window {
  padding: 8px;
  max-width: 200px;
}

.info-window h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.info-window p {
  margin: 4px 0;
  color: #666;
}
</style> 