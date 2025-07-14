// 高德地图 API Keys 说明：
// 1. Web端 Key (key-webJS): 7a9ebfd8db9264a7f90b65369bd2970a
//    用于前端地图显示和基础交互功能（JavaScript API、地图组件等）
//
// 2. Web服务 Key (key-web): 4c4409cdbe818ceb94f8660e2e111563
//    用于后端服务调用（路径规划、搜索、地理编码等），不要在前端直接使用

<template>
  <div class="map-container">
    <div ref="mapContainer" class="map"></div>

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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';
import parkingIcon from '@/components/icons/parking_area.png';
import bicycleIcon from '@/components/icons/bicycle.png';
import { getAllTasks, acceptTask, completeTask } from '@/api/assignment/task';
import { getAllParkingAreas, getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking';
import { getMapAreaBicycles } from '@/api/map/bicycle';
import { getHeatMapData, convertHeatMapData } from '@/api/map/heat';
import { ElMessage } from 'element-plus';
import { Close } from '@element-plus/icons-vue';


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

// 地图实例
const map = ref(null);
const mapContainer = ref(null);
const bicycleMarkers = ref([]);
const parkingMarkers = ref([]);
const parkingPolygons = ref([]); // 新增：存储停车区域多边形
const taskLines = ref([]);
const selectedTaskLine = ref(null);
const currentTask = ref(null);
const heatmap = ref(null); // 热力图实例

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

    console.log('工作人员热力图数据已更新，单车数量：', bicycleData.length, '热力图点数：', heatmapData.length);
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
      // 创建热力图实例
      heatmap.value = new window.AMap.HeatMap(map.value, {
        radius: 30, // 热力图的半径
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
  zoomOut
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