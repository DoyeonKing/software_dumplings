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
import { getAllParkingAreas } from '@/api/map/parking';
import { getAllBicycles } from '@/api/map/bicycle';
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
    // 如果已经加载过数据，直接切换显示状态
    if (bicycleMarkers.value.length > 0) {
      bicycleMarkers.value.forEach(marker => {
        marker.setMap(props.showBicycles ? map.value : null);
      });
      return;
    }

    const response = await getAllBicycles();
    const bicycles = response.data;

    // 创建标记但不立即添加到地图
    bicycles.forEach(bicycle => {
      const marker = new AMap.Marker({
        position: bicycle.location,
        icon: new AMap.Icon({
          image: bicycleIcon,
          size: new AMap.Size(32, 32),
          imageSize: new AMap.Size(32, 32)
        }),
        title: `单车 #${bicycle.id}`,
        map: props.showBicycles ? map.value : null  // 根据当前显示状态决定是否添加到地图
      });

      // 添加点击事件
      marker.on('click', () => {
        const info = new AMap.InfoWindow({
          content: `
            <div class="info-window">
              <h4>单车 #${bicycle.id}</h4>
              <p>状态: ${bicycle.status}</p>
              <p>电量: ${bicycle.battery}%</p>
              <p>最后使用: ${bicycle.lastUsed}</p>
              <p>总里程: ${bicycle.totalMileage}km</p>
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
    // 如果已经加载过数据，直接切换显示状态
    if (parkingMarkers.value.length > 0) {
      parkingMarkers.value.forEach(marker => {
        marker.setMap(props.showParkingAreas ? map.value : null);
      });
      parkingPolygons.value.forEach(polygon => {
        polygon.setMap(props.showParkingAreas ? map.value : null);
      });
      return;
    }

    const response = await getAllParkingAreas();
    const parkingAreas = response.data;

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
              <h4>${area.name}</h4>
              <p>状态: ${area.status}</p>
              <p>可用车位: ${area.available_spots || 0}个</p>
              <p>总车位: ${area.total_spots || 0}个</p>
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
    const AMap = await AMapLoader.load({
      key: '7a9ebfd8db9264a7f90b65369bd2970a',  // Web端 Key
      version: '2.0',
      plugins: [
        'AMap.ControlBar',
        'AMap.Scale',
        'AMap.ToolBar',
      ],
      securityJsCode: '1751964054605',
      security: {
        serviceHost: 'https://restapi.amap.com'
      }
    });

    map.value = new AMap.Map(mapContainer.value, {
      zoom: 13,
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

    // 预加载数据但不显示
    await Promise.all([
      loadBicycles(),
      loadParkingAreas()
    ]);
  } catch (error) {
    console.error('Failed to initialize map:', error);
    ElMessage.error('地图加载失败');
  }
};

onMounted(() => {
  initMap();
});

onUnmounted(() => {
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