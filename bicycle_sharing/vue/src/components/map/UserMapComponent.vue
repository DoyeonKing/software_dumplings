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
    <div v-if="showNavigation" class="navigation-panel" :class="{ hidden: hideUI }">
      <div class="panel-header">
        <h3>骑行导航</h3>
        <el-button type="text" @click="cancelNavigation">关闭</el-button>
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
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted, ref, onUnmounted, watch } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';
// 导入单车数据API
import { getMapAreaBicycles } from '@/api/map/bicycle';
import { getAllParkingAreas } from '@/api/map/parking';
import { getHeatMapData } from '@/api/map/heat';
import { ElMessage } from 'element-plus';
import { getRidingRoute } from '@/utils/amap';

// 状态文本映射
const statusText = {
  available: '可用',
  in_use: '使用中',
  maintenance: '维护中'
};

// 停车点状态文本映射
const parkingStatusText = {
  normal: '正常',
  full: '已满',
  maintenance: '维护中'
};

// 停车点状态对应的颜色
const parkingStatusColors = {
  normal: '#4CAF50',  // 绿色
  full: '#FF9800',    // 橙色
  maintenance: '#F44336'  // 红色
};

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
    showHeatmap: {
      type: Boolean,
      default: false
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
    const riding = ref(null);  // 存储骑行规划实例
    const navigationMarkers = ref([]);  // 存储导航起终点标记
    const navigationPolyline = ref(null);  // 存储导航路线
    const routePath = ref(null);  // 存储路线路径
    const userPosition = ref(null);  // 用户当前位置
    const userPositionMarker = ref(null);  // 用户位置标记
    const isSettingUserPosition = ref(false);  // 是否正在设置用户位置



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
        try {
          const response = await getHeatMapData();
          if (response.code === 200 && Array.isArray(response.data)) {
            heatmap.value.setDataSet({
              data: response.data,
              max: 100 // 最大权重值
            });

            // 初始时隐藏热力图
            heatmap.value.hide();
            
            // 只有在props.showHeatmap为true时才显示
            if (props.showHeatmap) {
              heatmap.value.show();
            }
            console.log('热力图数据已更新，数据点数：', response.data.length);
          } else {
            console.error('获取热力图数据格式错误：', response);
            ElMessage.error('获取热力图数据格式错误');
          }
        } catch (error) {
          console.error('获取热力图数据失败：', error);
          ElMessage.error('获取热力图数据失败');
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

    // 监听地图移动事件，更新单车数据
    const setupMapEventListeners = () => {
      if (!map.value) return;

      // 当地图移动结束时，重新显示单车
      map.value.on('moveend', () => {
        if (props.showBicycles) {
          showBicycleMarkers();
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
        // 如果已经加载过数据，直接返回
        if (parkingAreas.value.length > 0) {
          return;
        }
        const response = await getAllParkingAreas();
        parkingAreas.value = response.data;
      } catch (error) {
        console.error('获取停车点数据失败：', error);
        ElMessage.error('获取停车点数据失败');
      }
    };

    // 显示停车点区域
    const showParkingAreas = async () => {
      if (!map.value) return;

      try {
        // 如果已经创建了标记和多边形，只需要切换显示状态
        if (parkingPolygons.value.length > 0 && parkingMarkers.value.length > 0) {
          parkingPolygons.value.forEach(polygon => {
            polygon.setMap(map.value);
          });
          parkingMarkers.value.forEach(marker => {
            marker.setMap(map.value);
          });
          return;
        }

        await fetchParkingAreas();

        // 创建停车场图标
        const parkingIcon = new AMap.Icon({
          image: '/src/components/icons/parking_area.png',
          size: new AMap.Size(40, 40),
          imageSize: new AMap.Size(40, 40)
        });

        // 为每个停车点创建矩形区域和图标
        parkingAreas.value.forEach(area => {
          // 创建矩形的四个顶点
          const path = [
            [area.bounds.southwest[1], area.bounds.southwest[0]],
            [area.bounds.northeast[1], area.bounds.southwest[0]],
            [area.bounds.northeast[1], area.bounds.northeast[0]],
            [area.bounds.southwest[1], area.bounds.northeast[0]],
            [area.bounds.southwest[1], area.bounds.southwest[0]]
          ];

          // 创建多边形
          const polygon = new AMap.Polygon({
            path: path,
            strokeColor: parkingStatusColors[area.status] || '#4CAF50',
            strokeWeight: 3,
            strokeOpacity: 1,
            fillColor: parkingStatusColors[area.status] || '#4CAF50',
            fillOpacity: 0.4,
            cursor: 'pointer',
            map: null  // 初始不添加到地图
          });

          // 创建图标标记（放在区域中心）
          const center = [
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
                <h4>${area.name}</h4>
                <p><strong>编号：</strong>${area.id}</p>
                <p><strong>状态：</strong>${parkingStatusText[area.status]}</p>
                <p><strong>可用车位：</strong>${area.available_spots}个</p>
                <p><strong>总车位：</strong>${area.total_spots}个</p>
              </div>
            `;

            infoWindow.value.setContent(content);
            infoWindow.value.open(map.value, marker.getPosition());
          });

          // 添加多边形点击事件
          polygon.on('click', () => {
            if (!infoWindow.value) {
              infoWindow.value = new AMap.InfoWindow({
                closeWhenClickMap: true,
                offset: new AMap.Pixel(0, -30)
              });
            }

            const content = `
              <div class="parking-info">
                <h4>${area.name}</h4>
                <p><strong>编号：</strong>${area.id}</p>
                <p><strong>状态：</strong>${parkingStatusText[area.status]}</p>
                <p><strong>可用车位：</strong>${area.available_spots}个</p>
                <p><strong>总车位：</strong>${area.total_spots}个</p>
              </div>
            `;

            infoWindow.value.setContent(content);
            infoWindow.value.open(map.value, center);
          });

          // 添加多边形鼠标悬停效果
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

          // 添加标记鼠标悬停效果
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

          // 创建路线点数组
          const pathPoints = result.route.polyline.map(point => 
            new AMap.LngLat(point[0], point[1])
          );

          // 创建或更新路线折线
          if (navigationPolyline.value) {
            navigationPolyline.value.setPath(pathPoints);
          } else {
            navigationPolyline.value = new AMap.Polyline({
              path: pathPoints,
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

    // 取消导航
    const cancelNavigation = () => {
      clearRoute();
      startPoint.value = null;
      endPoint.value = null;
      isSelectingStart.value = false;
      isSelectingEnd.value = false;
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
          center: [114.00, 22.55],
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
      isSettingUserPosition.value = true;
      isSelectingStart.value = false;
      isSelectingEnd.value = false;
      ElMessage.info('请在地图上点击选择您的当前位置');
      // 改变鼠标样式以提示用户
      if (map.value) {
        map.value.setDefaultCursor('crosshair');
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
      selectCurrentPositionAsStart,
      userPosition,
      setUserPosition
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
</style> 