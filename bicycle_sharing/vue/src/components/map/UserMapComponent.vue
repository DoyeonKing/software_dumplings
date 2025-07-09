 // 高德地图 API Keys 说明：
// 1. Web端 Key (key-webJS): 7a9ebfd8db9264a7f90b65369bd2970a
//    用于前端地图显示和基础交互功能（JavaScript API、地图组件等）
// 
// 2. Web服务 Key (key-web): 4c4409cdbe818ceb94f8660e2e111563
//    用于后端服务调用（路径规划、搜索、地理编码等），不要在前端直接使用

<template>
  <div class="map-container">
    <div id="map"></div>
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
import { getAllBicycles } from '@/api/map/bicycle';
import { getAllParkingAreas } from '@/api/map/parking';
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

    // 初始化高德地图
    const initMap = async () => {
      try {
        const AMap = await AMapLoader.load({
          key: '7a9ebfd8db9264a7f90b65369bd2970a',  // Web端 Key
          version: '2.0',
          plugins: [
            'AMap.ControlBar',  // 指南针控件
            'AMap.Scale',       // 比例尺控件
            'AMap.Riding',      // 骑行路线规划
            'AMap.LngLat'       // 经纬度转换
          ],
          securityJsCode: '1751964054605',  // 添加安全密钥
          security: {
            serviceHost: 'https://restapi.amap.com'  // 设置安全域名
          }
        });

        // 创建地图实例
        map.value = new AMap.Map('map', {
          zoom: 13,
          center: [114.085947, 22.547],  // 深圳市中心
          viewMode: '2D',
          mapStyle: styleMapping[props.mapStyle]
        });

        // 添加指南针（放在右下角）
        const controlBar = new AMap.ControlBar({
          position: { bottom: '20px', right: '20px' },
          showZoomBar: false,  // 不显示缩放按钮
          showControlButton: true  // 显示指南针
        });
        map.value.addControl(controlBar);

        // 添加比例尺（放在左下角）
        const scale = new AMap.Scale({
          position: { bottom: '20px', left: '20px' }
        });
        map.value.addControl(scale);

        // 初始化骑行规划插件
        riding.value = new AMap.Riding({
          map: map.value,
          panel: false,  // 不使用默认面板
          hideMarkers: false,  // 显示起终点标记
          key: '4c4409cdbe818ceb94f8660e2e111563'  // Web服务 Key [[memory:2538380]]
        });

        // 初始化用户位置为地图中心点
        userPosition.value = [114.085947, 22.547];
        
        // 创建用户位置标记
        userPositionMarker.value = new AMap.Marker({
          map: map.value,
          position: userPosition.value,
          icon: new AMap.Icon({
            size: new AMap.Size(24, 24),
            imageSize: new AMap.Size(24, 24),
            image: 'https://webapi.amap.com/theme/v1.3/markers/n/loc.png'
          }),
          offset: new AMap.Pixel(-12, -12),
          zIndex: 200
        });

        // 添加自定义按钮
        const locationButton = document.createElement('div');
        locationButton.className = 'location-button';
        locationButton.innerHTML = `
          <button class="custom-button" title="设置当前位置">
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm8.94 3A8.994 8.994 0 0 0 13 3.06V1h-2v2.06A8.994 8.994 0 0 0 3.06 11H1v2h2.06A8.994 8.994 0 0 0 11 20.94V23h2v-2.06A8.994 8.994 0 0 0 20.94 13H23v-2h-2.06zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z"/>
            </svg>
          </button>
        `;
        map.value.getContainer().appendChild(locationButton);

        // 添加按钮点击事件
        locationButton.addEventListener('click', () => {
          isSettingUserPosition.value = true;
          isSelectingStart.value = false;
          isSelectingEnd.value = false;
          map.value.setDefaultCursor('crosshair');
          ElMessage.info('点击地图设置当前位置');
        });

        // 添加地图点击事件
        map.value.on('click', handleMapClick);

      } catch (error) {
        console.error('地图加载失败：', error);
        ElMessage.error('地图加载失败');
      }
    };

    // 获取单车数据（直接使用模拟数据）
    const fetchBicyclesData = async () => {
      try {
        const response = await getAllBicycles();
        bicyclesData.value = response.data;
      } catch (error) {
        console.error('获取单车数据失败：', error);
        ElMessage.error('获取单车数据失败');
      }
    };

    // 显示单车位置
    const showBicycleMarkers = async () => {
      if (!map.value) return;
      
      try {
        // 使用模拟数据
        await fetchBicyclesData();

        const AMap = await AMapLoader.load({
          key: '7a9ebfd8db9264a7f90b65369bd2970a',
          version: '2.0'
        });

        // 清除现有的单车标记
        if (bicycleMarkers.value.length) {
          map.value.remove(bicycleMarkers.value);
          bicycleMarkers.value = [];
        }

        // 创建单车图标
        const icon = new AMap.Icon({
          image: '/src/components/icons/bicycle.png',
          size: new AMap.Size(32, 32),
          imageSize: new AMap.Size(32, 32)
        });

        // 为每个单车创建标记
        if (bicyclesData.value && bicyclesData.value.length > 0) {
          bicyclesData.value.forEach(bike => {
            const marker = new AMap.Marker({
              position: bike.location,
              icon: icon,
              title: `单车编号: ${bike.id}`,
              offset: new AMap.Pixel(-16, -16)
            });

            // 添加点击事件
            marker.on('click', () => {
              const content = `
                <div class="bicycle-info">
                  <h4>单车信息</h4>
                  <p><strong>编号：</strong>${bike.id}</p>
                  <p><strong>状态：</strong>${statusText[bike.status]}</p>
                  <p><strong>电量：</strong>${bike.battery}%</p>
                  <p><strong>最后使用：</strong>${bike.lastUsed}</p>
                  <p><strong>总里程：</strong>${bike.totalMileage.toFixed(1)}km</p>
                </div>
              `;
              
              if (!infoWindow.value) {
                infoWindow.value = new AMap.InfoWindow({
                  closeWhenClickMap: true,
                  offset: new AMap.Pixel(0, -30)
                });
              }
              
              infoWindow.value.setContent(content);
              infoWindow.value.open(map.value, marker.getPosition());
            });
            
            bicycleMarkers.value.push(marker);
          });

          // 将所有标记添加到地图上
          map.value.add(bicycleMarkers.value);
        } else {
          console.error('没有可用的单车数据');
          ElMessage.warning('没有可用的单车数据');
        }
      } catch (error) {
        console.error('加载单车标记失败：', error);
        ElMessage.error('加载单车标记失败');
      }
    };

    // 隐藏单车位置
    const hideBicycleMarkers = () => {
      if (!map.value || !bicycleMarkers.value.length) return;
      map.value.remove(bicycleMarkers.value);
      bicycleMarkers.value = [];
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
    const fetchParkingAreas = () => {
      // 直接使用模拟数据
      return getAllParkingAreas().then(response => {
        parkingAreas.value = response.data;
      });
    };

    // 显示停车点区域
    const showParkingAreas = async () => {
      if (!map.value) return;

      try {
        await fetchParkingAreas();

        const AMap = await AMapLoader.load({
          key: '7a9ebfd8db9264a7f90b65369bd2970a',
          version: '2.0'
        });

        // 清除现有的停车区域和标记
        if (parkingPolygons.value.length) {
          map.value.remove(parkingPolygons.value);
          parkingPolygons.value = [];
        }
        if (parkingMarkers.value.length) {
          map.value.remove(parkingMarkers.value);
          parkingMarkers.value = [];
        }

        // 创建停车场图标
        const parkingIcon = new AMap.Icon({
          image: '/src/components/icons/parking_area.png',
          size: new AMap.Size(40, 40),    // 图标大小
          imageSize: new AMap.Size(40, 40) // 图片大小
        });

        // 为每个停车点创建矩形区域和图标
        parkingAreas.value.forEach(area => {
          // 创建矩形的四个顶点
          const path = [
            [area.bounds.southwest[1], area.bounds.southwest[0]],  // 
            [area.bounds.northeast[1], area.bounds.southwest[0]],  // 右下角
            [area.bounds.northeast[1], area.bounds.northeast[0]],  // 
            [area.bounds.southwest[1], area.bounds.northeast[0]]   // 左上角
          ];

          // 创建多边形
          const polygon = new AMap.Polygon({
            path: path,
            strokeColor: '#4CAF50',  // 使用固定的绿色
            strokeWeight: 3,
            strokeOpacity: 1,
            fillColor: '#4CAF50',
            fillOpacity: 0.4,
            cursor: 'pointer'
          });

          // 创建图标标记（放在右上角）
          const marker = new AMap.Marker({
            position: area.bounds.northeast,
            icon: parkingIcon,
            offset: new AMap.Pixel(-20, -20),
            cursor: 'pointer'
          });

          // 添加图标点击事件
          marker.on('click', () => {
            const content = `
              <div class="parking-info">
                <h4>停车区域</h4>
                <p><strong>编号：</strong>${area.id}</p>
                <p><strong>GeoHash：</strong>${area.geohash}</p>
              </div>
            `;
            
            if (!infoWindow.value) {
              infoWindow.value = new AMap.InfoWindow({
                closeWhenClickMap: true,
                offset: new AMap.Pixel(0, -30)
              });
            }
            
            infoWindow.value.setContent(content);
            infoWindow.value.open(map.value, marker.getPosition());
          });

          // 添加多边形鼠标悬停效果
          polygon.on('mouseover', () => {
            polygon.setOptions({
              fillOpacity: 0.6,
              strokeWeight: 4
            });
          });

          polygon.on('mouseout', () => {
            polygon.setOptions({
              fillOpacity: 0.4,
              strokeWeight: 3
            });
          });

          parkingPolygons.value.push(polygon);
          parkingMarkers.value.push(marker);
        });

        // 将所有多边形和标记添加到地图上
        map.value.add(parkingPolygons.value);
        map.value.add(parkingMarkers.value);
      } catch (error) {
        console.error('加载停车区域失败：', error);
        ElMessage.error('加载停车区域失败');
      }
    };

    // 隐藏停车点区域
    const hideParkingAreas = () => {
      if (!map.value) return;
      if (parkingPolygons.value.length) {
        map.value.remove(parkingPolygons.value);
        parkingPolygons.value = [];
      }
      if (parkingMarkers.value.length) {
        map.value.remove(parkingMarkers.value);
        parkingMarkers.value = [];
      }
    };

    // 监听 showParkingAreas 属性变化
    watch(() => props.showParkingAreas, (newValue) => {
      if (newValue) {
        showParkingAreas();
      } else {
        hideParkingAreas();
      }
    });

    // 处理地图点击事件
    const handleMapClick = (e) => {
      const clickPosition = [e.lnglat.getLng(), e.lnglat.getLat()];

      if (isSettingUserPosition.value) {
        // 更新用户位置
        userPosition.value = clickPosition;
        userPositionMarker.value.setPosition(clickPosition);
        isSettingUserPosition.value = false;
        map.value.setDefaultCursor('');
        ElMessage.success('已更新当前位置');
        return;
      }

      if (isSelectingStart.value) {
        startPoint.value = clickPosition;
        addNavigationMarker('start', clickPosition);
        isSelectingStart.value = false;
        map.value.setDefaultCursor('');
        ElMessage.success('已选择起点');
      } else if (isSelectingEnd.value) {
        endPoint.value = clickPosition;
        addNavigationMarker('end', clickPosition);
        isSelectingEnd.value = false;
        map.value.setDefaultCursor('');
        ElMessage.success('已选择终点');
      }
    };

    // 开始选择点位
    const startSelectingPoint = (type) => {
      if (type === 'start') {
        isSelectingStart.value = true;
        isSelectingEnd.value = false;
        isSettingUserPosition.value = false;
        map.value.setDefaultCursor('crosshair');
        ElMessage.info('请在地图上点击选择起点');
      } else {
        isSelectingEnd.value = true;
        isSelectingStart.value = false;
        isSettingUserPosition.value = false;
        map.value.setDefaultCursor('crosshair');
        ElMessage.info('请在地图上点击选择终点');
      }
    };

    // 添加导航标记
    const addNavigationMarker = async (type, position) => {
      const AMap = await AMapLoader.load({
        key: '7a9ebfd8db9264a7f90b65369bd2970a',
        version: '2.0'
      });

      // 移除之前的标记
      const index = type === 'start' ? 0 : 1;
      if (navigationMarkers.value[index]) {
        map.value.remove(navigationMarkers.value[index]);
      }

      // 创建新标记
      const marker = new AMap.Marker({
        position: position,
        icon: new AMap.Icon({
          size: new AMap.Size(25, 34),
          imageSize: new AMap.Size(25, 34),
          image: type === 'start' ? 
            'https://webapi.amap.com/theme/v1.3/markers/n/start.png' : 
            'https://webapi.amap.com/theme/v1.3/markers/n/end.png'
        }),
        offset: new AMap.Pixel(-12, -34)
      });

      navigationMarkers.value[index] = marker;
      map.value.add(marker);
    };

    // 计算路线
    const calculateRoute = async () => {
      if (!startPoint.value || !endPoint.value) {
        ElMessage.warning('请先选择起点和终点');
        return;
      }

      // 保存坐标点
      const start = [...startPoint.value];
      const end = [...endPoint.value];

      try {
        // 清除之前的路线
        clearRoute();

        // 恢复坐标点
        startPoint.value = start;
        endPoint.value = end;

        // 获取路线规划
        const result = await getRidingRoute(start, end);

        if (result.status === 'complete') {
          // 显示路线
          const AMap = await AMapLoader.load({
            key: '7a9ebfd8db9264a7f90b65369bd2970a',
            version: '2.0'
          });

          // 创建路线折线
          routePath.value = new AMap.Polyline({
            path: result.route.polyline,
            strokeColor: '#3366FF',
            strokeWeight: 6,
            strokeOpacity: 0.8,
            showDir: true,  // 显示方向
            dirColor: '#3366FF'  // 方向箭头颜色
          });

          // 添加到地图
          map.value.add(routePath.value);

          // 自动调整视野以显示整条路线
          map.value.setFitView([routePath.value]);

          // 更新路线信息
          routeInfo.value = {
            distance: result.route.distance < 1000 ? 
              `${Math.round(result.route.distance)}米` : 
              `${(result.route.distance / 1000).toFixed(2)}公里`,
            time: Math.ceil(result.route.duration / 60)  // 转换为分钟
          };

          ElMessage.success('路线规划成功');
        } else {
          console.error('路线规划失败:', result.error);
          ElMessage.error(result.error || '路线规划失败，请重试');
        }
      } catch (error) {
        console.error('骑行路线规划失败：', error);
        ElMessage.error(error.message || '路线规划失败，请重试');
      }
    };

    // 清除路线
    const clearRoute = () => {
      // 清除路线
      if (routePath.value) {
        map.value.remove(routePath.value);
        routePath.value = null;
      }
      
      // 清除路线信息
      routeInfo.value = null;
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
      clearNavigation();
      // 触发事件通知父组件关闭导航面板
      emit('update:showNavigation', false);
    };

    // 选择当前位置作为起点
    const selectCurrentPositionAsStart = () => {
      if (!userPosition.value) {
        ElMessage.warning('请先设置当前位置');
        return;
      }
      startPoint.value = userPosition.value;
      addNavigationMarker('start', userPosition.value);
      ElMessage.success('已将当前位置设为起点');
    };

    // 添加样式
    const style = document.createElement('style');
    style.textContent = `
      .location-button {
        position: absolute;
        right: 20px;
        bottom: 140px;  /* 调整位置到指南针上方 */
        z-index: 100;
      }
      .custom-button {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background-color: white;
        border: none;
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0;
        transition: background-color 0.3s;
      }
      .custom-button:hover {
        background-color: #f0f0f0;
      }
      .custom-button:active {
        background-color: #e0e0e0;
      }
    `;
    document.head.appendChild(style);

    onMounted(() => {
      initMap().then(() => {
        setupMapEventListeners();
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
      // 移除样式
      if (style.parentNode) {
        style.parentNode.removeChild(style);
      }
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
      userPosition
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
</style> 