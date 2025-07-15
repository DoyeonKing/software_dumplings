<template>
  <div class="location-view-root">
    <div id="mapContainer"></div>

    <MenuComponent @profile-saved="handleProfileSaved" />

    <div class="top-right-btn-group btn-group">
      <button class="yellow-btn" @click="onToggleBikes">
        {{ showBikes ? '隐藏单车' : '显示单车' }}
      </button>
      <button class="yellow-btn" @click="onToggleHeatmap">
        {{ showHeatmap ? '显示普通地图' : '显示热力图' }}
      </button>
      <button class="yellow-btn" @click="goHome">
        返回主页
      </button>
    </div>

    <!-- 修改左侧信息面板为可折叠面板 -->
    <div class="left-panels">
      <div class="collapsible-panel">
        <div class="panel-header" @click="toggleAreaPanel">
          <span class="panel-title">区域数据</span>
          <span class="panel-icon">{{ areaPanelExpanded ? '▼' : '▶' }}</span>
        </div>
        <div class="panel-content" v-show="areaPanelExpanded">
          <div class="info-card">
            <div class="info-section">
              <div class="info-label">停车区域位置</div>
              <div class="info-value">{{ currentArea.geohash || "请在地图上选择区域" }}</div>
            </div>
            <div class="info-section">
              <div class="info-label">现有车辆</div>
              <div class="info-value">{{ currentArea.currentBikes || 0 }}</div>
            </div>
            <div class="info-section">
              <div class="info-label">预估可用车位</div>
              <div class="info-value">{{ currentArea.availableSpots || 0 }}</div>
            </div>
            <div class="info-section">
              <div class="predict-filter">
                <label>预测时间：</label>
                <select v-model="predictHour" class="yellow-select">
                  <option :value="1">未来1小时</option>
                  <option :value="3">未来3小时</option>
                  <option :value="6">未来6小时</option>
                </select>
              </div>
              <div class="predict-stats">
                <div>预计取车量：<span class="info-number">{{ predictData.take }}</span></div>
                <div>预计停车量：<span class="info-number">{{ predictData.park }}</span></div>
                <div>预计总车辆：<span class="info-number">{{ predictData.total }}</span></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="collapsible-panel">
        <div class="panel-header" @click="toggleSuggestionPanel">
          <span class="panel-title">调度建议</span>
          <span class="panel-icon">{{ suggestionPanelExpanded ? '▼' : '▶' }}</span>
        </div>
        <div class="panel-content" v-show="suggestionPanelExpanded">
          <DispatchSuggestionPanel 
            @suggestion-accepted="handleSuggestionAccepted"
            @suggestion-rejected="handleSuggestionRejected"
          />
        </div>
      </div>
    </div>

    <div class="right-task-panel">
      <div class="task-card">
        <div class="task-title-row">
          <div class="task-title">发布调度任务</div>
          <button class="collapse-btn yellow-btn" @click="taskPanelCollapsed = !taskPanelCollapsed">
            {{ taskPanelCollapsed ? '展开' : '收起' }}
          </button>
        </div>
        <transition name="fade">
          <div v-show="!taskPanelCollapsed">

            <div class="task-section">
              <label>调度起点</label>
              <button v-if="!startSelectionActive" class="yellow-btn select-location-btn" @click="activateSelection('start')">
                选择起点
              </button>
              <div v-if="startSelectionActive" class="location-selection-box">
                <div v-if="!selectedStartArea" class="placeholder-text">
                  请在地图上选择起点区域...
                </div>
                <div v-if="selectedStartArea" class="location-details">
                  <div class="location-name">{{ selectedStartArea.geohash }}</div>
                  <div class="predict-filter">
                    <select v-model="startPredictHour" class="yellow-select small-select">
                      <option :value="1">未来1小时</option>
                      <option :value="3">未来3小时</option>
                      <option :value="6">未来6小时</option>
                    </select>
                  </div>
                  <div class="predict-stats-horizontal">
                    <div class="predict-take">取车: {{ startPredictData.take }}</div>
                    <div class="predict-park">停车: {{ startPredictData.park }}</div>
                    <div class="predict-total">总车: {{ startPredictData.total }}</div>
                  </div>
                </div>
                <button class="clear-btn" @click="cancelOrClearSelection('start')">
                  {{ selectedStartArea ? '清空起点' : '取消' }}
                </button>
              </div>
            </div>

            <div class="task-section">
              <label>调度终点</label>
              <button v-if="!endSelectionActive" class="yellow-btn select-location-btn" @click="activateSelection('end')">
                选择终点
              </button>
              <div v-if="endSelectionActive" class="location-selection-box">
                <div v-if="!selectedEndArea" class="placeholder-text">
                  请在地图上选择终点区域...
                </div>
                <div v-if="selectedEndArea" class="location-details">
                  <div class="location-name">{{ selectedEndArea.geohash }}</div>
                  <div class="predict-filter">
                    <select v-model="endPredictHour" class="yellow-select small-select">
                      <option :value="1">未来1小时</option>
                      <option :value="3">未来3小时</option>
                      <option :value="6">未来6小时</option>
                    </select>
                  </div>
                  <div class="predict-stats-horizontal">
                    <div class="predict-take">取车: {{ endPredictData.take }}</div>
                    <div class="predict-park">停车: {{ endPredictData.park }}</div>
                    <div class="predict-total">总车: {{ endPredictData.total }}</div>
                  </div>
                </div>
                <button class="clear-btn" @click="cancelOrClearSelection('end')">
                  {{ selectedEndArea ? '清空终点' : '取消' }}
                </button>
              </div>
            </div>

            <div class="task-section">
              <label>管理员ID：</label>
              <div class="manager-input-group">
                <input 
                  type="text" 
                  v-model="managerId" 
                  placeholder="请输入管理员ID"
                  class="yellow-input"
                />
              </div>
            </div>

            <div class="task-section" v-if="managerId">
              <label>选择工作人员：</label>
              <div class="task-workers-list">
                <div
                  v-for="worker in filteredWorkers"
                  :key="worker.staffId"
                  :class="['worker-item', { selected: selectedWorker && selectedWorker.staffId === worker.staffId }]"
                  @click="selectWorker(worker)"
                >
                  <div class="worker-info">
                    <div class="worker-id">工号：{{ worker.staffId }}</div>
                    <div class="worker-name">用户名：{{ worker.username }}</div>
                  </div>
                </div>
                <div v-if="filteredWorkers.length === 0" class="no-workers-tip">
                  没有找到该管理员负责的工作人员
                </div>
              </div>
            </div>

            <div class="task-section">
              <label>调度数量：</label>
              <div class="amount-input-group">
                <button class="amount-btn yellow-btn" @click="changeDispatchAmount(-1)" :disabled="dispatchAmount<=1">-</button>
                <input class="amount-input" type="number" v-model.number="dispatchAmount" min="1" />
                <button class="amount-btn yellow-btn" @click="changeDispatchAmount(1)">+</button>
              </div>
            </div>

            <button class="yellow-btn deploy-btn"
                    @click="publishTask"
                    :disabled="!selectedStartArea || !selectedEndArea || !selectedWorker || dispatchAmount<1"
            >确定发布</button>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue';
import DispatchSuggestionPanel from '@/components/admin/DispatchSuggestionPanel.vue';
import AMapLoader from '@/utils/loadAMap.js';
import bicycleIcon from '@/components/icons/bicycle.png';
import { getMapAreaBicycles } from '@/api/map/bicycle';
import { getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking.js';

// 颜色定义
const HIGHLIGHT_COLORS = {
  DEFAULT: { fillColor: "#FFD600", fillOpacity: 0.2, strokeColor: "#FFD600" },
  START:   { fillColor: "#ef5350", fillOpacity: 0.35, strokeColor: "#ef5350" },
  END:     { fillColor: "#66bb6a", fillOpacity: 0.35, strokeColor: "#66bb6a" },
};

export default {
  name: "LocationView",
  components: {
    MenuComponent,
    DispatchSuggestionPanel
  },
  data() {
    return {
      taskPanelCollapsed: false,
      polygons: [],
      polygonMap: {},
      currentArea: { geohash: "请在地图上选择区域", currentBikes: 0, availableSpots: 0 },
      predictHour: 1,
      predictData: { take: 0, park: 0, total: 0 },
      selectingFor: null,
      startSelectionActive: false,
      endSelectionActive: false,
      selectedStartArea: null,
      selectedEndArea: null,
      startPredictHour: 1,
      endPredictHour: 1,
      startPredictData: { take: 0, park: 0, total: 0 },
      endPredictData: { take: 0, park: 0, total: 0 },
      selectedWorker: null,
      dispatchAmount: 1,
      managerId: '',
      allWorkers: [],
      // 地图相关
      map: null,
      infoWindow: null,
      markers: [],
      heatmap: null,
      heatmapReady: false,
      showHeatmap: false,
      parkingAreas: [],
      bikes: [],
      showBikes: true,
      // 新增面板状态
      areaPanelExpanded: true,
      suggestionPanelExpanded: true,
      // 添加高亮区域的颜色配置
      areaColors: {
        start: { fillColor: "#ffcdd2", fillOpacity: 0.5, strokeColor: "#ef5350" },
        end: { fillColor: "#c8e6c9", fillOpacity: 0.5, strokeColor: "#66bb6a" }
      }
    };
  },
  computed: {
    filteredWorkers() {
      if (!this.managerId) return [];
      return this.allWorkers.filter(worker => worker.managerId === this.managerId);
    }
  },
  watch: {
    predictHour() { this.updatePrediction(this.currentArea, this.predictHour, 'predictData'); },
    startPredictHour() { this.updatePrediction(this.selectedStartArea, this.startPredictHour, 'startPredictData'); },
    endPredictHour() { this.updatePrediction(this.selectedEndArea, this.endPredictHour, 'endPredictData'); },
    currentArea() { this.updatePrediction(this.currentArea, this.predictHour, 'predictData'); }
  },
  methods: {
    // 【已修改】实现了从API加载和转换停车区域数据的完整逻辑
    async loadParkingAreas() {
      if (!this.map) return;
      try {
        const bounds = this.map.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLon: bounds.getSouthWest().lng,
          maxLon: bounds.getNorthEast().lng
        };
        const response = await getParkingAreasInBounds(params);

        // 健壮地处理多种可能的API响应格式
        let rawData = null;
        if (response && Array.isArray(response)) {
          rawData = response;
        } else if (response && response.data && Array.isArray(response.data)) {
          rawData = response.data;
        } else if (response && response.code === 200 && Array.isArray(response.data)) {
          rawData = response.data;
        }

        if (rawData) {
          // 使用 parking.js 中的转换函数处理数据
          this.parkingAreas = convertParkingAreaData(rawData);
        } else {
          console.warn('停车区域数据格式异常或为空:', response);
          this.parkingAreas = [];
        }
      } catch (error) {
        console.error("加载停车区域数据失败:", error);
        this.parkingAreas = [];
      } finally {
        // 无论成功或失败，都重新绘制图层（失败时会清空）
        this.drawParkingAreas();
      }
    },
    async loadBicycles() {
      try {
        const bounds = this.map.getBounds();
        const params = { minLat: bounds.getSouthWest().lat, maxLat: bounds.getNorthEast().lat, minLng: bounds.getSouthWest().lng, maxLng: bounds.getNorthEast().lng };
        const response = await getMapAreaBicycles(params);
        const bikesForMixin = response.data.map(bike => ({ ...bike, lng: bike.currentLon, lat: bike.currentLat, id: bike.bikeId }));
        this.bikes = bikesForMixin;
        const bikeMarkerIcon = new window.AMap.Icon({ image: bicycleIcon, size: new window.AMap.Size(32, 32), imageSize: new window.AMap.Size(32, 32) });
        this.addBikeMarkers(this.bikes, bikeMarkerIcon);
        if (!this.showBikes) { this.markers.forEach(marker => marker.hide()); }
      } catch (error) { console.error("加载单车数据失败:", error); }
    },

    addBikeMarkers(bikeList, bikeIcon) {
      this.markers.forEach(marker => marker.setMap(null));
      this.markers = [];
      bikeList.forEach(bike => {
        const marker = new window.AMap.Marker({ position: [bike.lng, bike.lat], map: this.map, icon: bikeIcon, title: `单车编号: ${bike.id}` });
        marker.on('mouseover', () => {
          this.infoWindow.setContent(`<div style="padding: 8px 12px; font-size: 14px;"><b>单车编号：</b>${bike.id}</div>`);
          this.infoWindow.open(this.map, marker.getPosition());
        });
        marker.on('mouseout', () => this.infoWindow.close());
        this.markers.push(marker);
      });
    },

    onToggleBikes() {
      this.showBikes = !this.showBikes;
      if (this.markers && this.markers.length > 0) {
        if (this.showBikes && this.showHeatmap) { this.toggleHeatmap(this.bikes); }
        else { this.markers.forEach(marker => { this.showBikes ? marker.show() : marker.hide(); }); }
      }
    },

    updatePolygonStyles() {
      this.parkingAreas.forEach(area => {
        const polygon = this.polygonMap[area.id];
        if (!polygon) return;

        let colors = HIGHLIGHT_COLORS.DEFAULT;
        if (this.selectedStartArea && this.selectedStartArea.id === area.id) {
          colors = HIGHLIGHT_COLORS.START;
        } else if (this.selectedEndArea && this.selectedEndArea.id === area.id) {
          colors = HIGHLIGHT_COLORS.END;
        }

        polygon.setOptions({
          fillColor: colors.fillColor,
          fillOpacity: colors.fillOpacity,
          strokeColor: colors.strokeColor
        });
      });
    },

    // 【已修改】确保此方法能正确处理动态获取并转换后的数据
    drawParkingAreas() {
      if (this.polygons && this.polygons.length) this.map.remove(this.polygons);
      this.polygons = [];
      this.polygonMap = {};
      this.parkingAreas.forEach(area => {
        const polygon = new window.AMap.Polygon({
          // 使用从 convertParkingAreaData 函数获得的精确多边形路径
          path: area.polygonPath,
          fillColor: HIGHLIGHT_COLORS.DEFAULT.fillColor,
          fillOpacity: HIGHLIGHT_COLORS.DEFAULT.fillOpacity,
          strokeColor: HIGHLIGHT_COLORS.DEFAULT.strokeColor,
          strokeWeight: 2,
          zIndex: 50,
          cursor: "pointer"
        });
        polygon.setMap(this.map);

        polygon.on("mouseover", () => {
          this.infoWindow.setContent(`
            <div style="min-width:160px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
              <b>停车区域：</b>${area.geohash}
            </div>`);
          // 使用转换后数据中的中心点坐标，更精确
          const centerPosition = area.center ? [area.center[1], area.center[0]] : polygon.getBounds().getCenter();
          this.infoWindow.open(this.map, centerPosition);
        });

        polygon.on("mouseout", () => {
          this.infoWindow.close();
        });

        polygon.on("click", () => this.handlePolygonClick(area));
        this.polygons.push(polygon);
        this.polygonMap[area.id] = polygon; // area.id 即 area.geohash
      });
      // 绘制完成后，立即根据当前选择状态更新样式
      this.updatePolygonStyles();
    },

    handlePolygonClick(area) {
      if (this.selectingFor === 'start') {
        if (this.selectedEndArea && this.selectedEndArea.id === area.id) {
          alert('起点和终点不能是同一个区域！');
          return;
        }
        this.selectedStartArea = area;
        this.updatePrediction(this.selectedStartArea, this.startPredictHour, 'startPredictData');
        this.selectingFor = null;
      } else if (this.selectingFor === 'end') {
        if (this.selectedStartArea && this.selectedStartArea.id === area.id) {
          alert('起点和终点不能是同一个区域！');
          return;
        }
        this.selectedEndArea = area;
        this.updatePrediction(this.selectedEndArea, this.endPredictHour, 'endPredictData');
        this.selectingFor = null;
      } else {
        this.currentArea = area;
      }
      this.updatePolygonStyles();
    },

    cancelOrClearSelection(type) {
      if (type === 'start') {
        this.startSelectionActive = false;
        this.selectedStartArea = null;
        this.selectingFor = null;
      } else if (type === 'end') {
        this.endSelectionActive = false;
        this.selectedEndArea = null;
        this.selectingFor = null;
      }
      this.updatePolygonStyles();
    },

    publishTask() {
      if (!this.selectedStartArea || !this.selectedEndArea || !this.selectedWorker || this.dispatchAmount < 1) return;
      alert(`调度任务已发布！\n\n` + `起点：${this.selectedStartArea.geohash}\n` + `终点：${this.selectedEndArea.geohash}\n` + `调度数量：${this.dispatchAmount}\n` + `工作人员：${this.selectedWorker.name} (${this.selectedWorker.phone})`);
      this.cancelOrClearSelection('start');
      this.cancelOrClearSelection('end');
      this.selectedWorker = null;
      this.dispatchAmount = 1;
    },

    onToggleHeatmap() {
      if (!this.showHeatmap) {
        this.showBikes = false;
      }
      this.toggleHeatmap();
      if (!this.showHeatmap && !this.showBikes) {
        this.markers.forEach(marker => marker.hide());
      }
    },

    toggleHeatmap() {
      this.showHeatmap = !this.showHeatmap;

      if (this.showHeatmap) {
        this.markers.forEach(m => m.hide());
        const heatData = this.bikes.map(bike => ({
          lng: bike.lng,
          lat: bike.lat,
          count: 80  // 与AdminView保持一致的权重值
        }));
        if (this.heatmapReady && this.heatmap) {
          try {
            if (typeof this.heatmap.setDataSet === 'function') {
              this.heatmap.setDataSet({
                data: heatData,
                max: 100  // 与AdminView保持一致的最大值
              });
            } else if (typeof this.heatmap.setData === 'function') {
              this.heatmap.setData({
                data: heatData,
                max: 100
              });
            } else if (typeof this.heatmap.setPoints === 'function') {
              this.heatmap.setPoints(heatData);
            }
            this.heatmap.show();
          } catch (error) {
            console.error('设置热力图数据失败：', error);
          }
        }
      } else {
        this.markers.forEach(m => m.show());
        if (this.heatmap) this.heatmap.hide();
      }
    },

    handleProfileSaved(formData) { console.log('个人资料已保存:', formData); window.alert('个人信息已在控制台输出。'); },
    goHome() { this.$router.push('/admin'); },
    updatePrediction(area, hour, dataProperty) {
      if (!area || !area.id) { this[dataProperty] = { take: 0, park: 0, total: 0 }; return; }
      const take = Math.round((area.baseTakeRate || 3) * hour * (Math.random() * 0.4 + 0.8));
      const park = Math.round((area.baseParkRate || 5) * hour * (Math.random() * 0.4 + 0.8));
      const total = (area.currentBikes || 20) + park - take;
      this[dataProperty] = { take, park, total };
    },
    activateSelection(type) {
      if (type === 'start') { this.startSelectionActive = true; this.selectingFor = 'start'; }
      else if (type === 'end') { this.endSelectionActive = true; this.selectingFor = 'end'; }
    },
    selectWorker(worker) {
      this.selectedWorker = worker;
    },
    changeDispatchAmount(delta) {
      let next = this.dispatchAmount + delta;
      if (next < 1) next = 1;
      this.dispatchAmount = next;
    },
    async fetchWorkers() {
      try {
        const response = await fetch('http://localhost:8080/staff/workers');
        const data = await response.json();
        this.allWorkers = data;
      } catch (error) {
        console.error('获取工作人员列表失败:', error);
        alert('获取工作人员列表失败');
      }
    },

    // 添加面板切换方法
    toggleAreaPanel() {
      this.areaPanelExpanded = !this.areaPanelExpanded;
    },
    toggleSuggestionPanel() {
      this.suggestionPanelExpanded = !this.suggestionPanelExpanded;
    },

    // 处理调度建议的接受和拒绝
    handleSuggestionAccepted(suggestion) {
      // 自动填充起点和终点
      this.selectedStartArea = {
        geohash: suggestion.startArea,
        id: suggestion.startArea,
        currentBikes: 20, // 使用默认值，实际应该从API获取
      };
      this.selectedEndArea = {
        geohash: suggestion.endArea,
        id: suggestion.endArea,
        currentBikes: 20, // 使用默认值，实际应该从API获取
      };
      
      // 设置调度数量
      this.dispatchAmount = suggestion.amount;
      
      // 展开任务面板
      this.taskPanelCollapsed = false;
      
      // 更新预测数据
      this.updatePrediction(this.selectedStartArea, this.startPredictHour, 'startPredictData');
      this.updatePrediction(this.selectedEndArea, this.endPredictHour, 'endPredictData');
      
      // 更新地图上的多边形样式
      this.updatePolygonStyles();
    },

    handleSuggestionRejected(suggestion) {
      // 可以添加一些视觉反馈或其他处理逻辑
      console.log('建议已拒绝:', suggestion);
    },

    // 更新区域颜色
    updateAreaColors(startAreaId, endAreaId) {
      this.parkingAreas.forEach(area => {
        const polygon = this.polygonMap[area.id];
        if (!polygon) return;

        if (area.geohash === startAreaId) {
          polygon.setOptions(this.areaColors.start);
        } else if (area.geohash === endAreaId) {
          polygon.setOptions(this.areaColors.end);
        }
      });
    }
  },
  mounted() {
    this.fetchWorkers(); // 页面加载时获取所有工作人员信息
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      // 初始化地图
      this.map = new window.AMap.Map("mapContainer", {
        center: [114.0580, 22.5390],
        zoom: 18, // 更高的缩放级别
        dragEnable: true,
        zoomEnable: true,
        doubleClickZoom: true,
        keyboardEnable: true,
        scrollWheel: true,
        touchZoom: true,
        mapStyle: 'amap://styles/normal'
      });

      // 初始化信息窗口
      this.infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });

      // 加载热力图插件
      window.AMap.plugin(['AMap.HeatMap'], () => {
        this.heatmap = new window.AMap.HeatMap(this.map, {
          radius: 25,
          opacity: [0.1, 0.9],
          gradient: {
            0.2: 'blue',
            0.4: 'green',
            0.6: 'yellow',
            0.8: 'orange',
            1.0: 'red'
          }
        });
        this.heatmapReady = true;
      });

      // 【修改】将数据加载逻辑统一管理，并添加防抖
      let timeout;
      const loadAllData = () => {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
          this.loadBicycles();
          this.loadParkingAreas();
        }, 500); // 500ms的防抖延迟
      };

      // 初始加载所有数据
      loadAllData();

      // 监听地图移动和缩放事件，使用防抖函数
      this.map.on('moveend', loadAllData);
      this.map.on('zoomend', loadAllData);

    }).catch(err => { alert('地图加载失败: ' + err.message); });
  },

  beforeUnmount() {
    if (this.map) {
      this.map.destroy();
    }
  },
};
</script>

<style scoped>
@import '@/assets/globalStyles.css';

.location-view-root { position: relative; height: 100vh; overflow: hidden; }
#mapContainer { width: 100vw; height: 100vh; position: fixed; top: 0; left: 0; z-index: 1; }
.top-right-btn-group { position: fixed; top: 20px; right: 30px; z-index: 30; }
.left-info-panel { position: fixed; top: 90px; left: 30px; z-index: 20; display: flex; flex-direction: column; gap: 14px; min-width: 260px; max-width: 320px; }
.info-title { font-size: 1.1rem; font-weight: 700; color: #222; margin-bottom: 6px; }
.info-content { font-size: 1rem; color: #444; }
.info-number { color: #FFD600; font-weight: bold; font-size: 1.1em; }
.predict-filter { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.yellow-select { background: #f7f7f7; border: 1.5px solid #FFD600; border-radius: 8px; padding: 4px 12px; font-size: 1rem; color: #222; font-weight: bold; outline: none; transition: background 0.2s; }
.yellow-select.small-select { padding: 2px 8px; font-size: 0.9rem; border-radius: 6px; }
.yellow-select:focus { background: #fffbe6; }
.predict-stats { display: flex; flex-direction: column; gap: 4px; font-size: 1rem; color: #444; }

.right-task-panel { position: fixed; top: 50px; right: 23px; z-index: 20; display: flex; flex-direction: column; min-width: 360px; max-width: 420px; align-items: flex-end; }
.task-card { background: #fff; border-radius: 14px; box-shadow: 0 2px 16px rgba(0,0,0,0.08); padding: 18px 24px; display: flex; flex-direction: column; gap: 14px; width: 100%; }
.task-title-row { display: flex; align-items: center; justify-content: space-between; }
.task-title { font-size: 1.2rem; font-weight: 700; color: #222; }
.collapse-btn { padding: 4px 18px; font-size: 15px; border-radius: 16px; min-width: 70px; margin-left: 10px; font-weight: 500; }
.task-section { display: flex; flex-direction: column; gap: 10px; border-bottom: 1px solid #f0f0f0; padding-bottom: 14px; }
.task-section:last-child { border-bottom: none; padding-bottom: 0; }
.task-section > label { font-weight: 600; font-size: 1.05rem; color: #333; }
.select-location-btn { font-size: 0.95rem; padding: 8px 16px; width: 100%; }

.location-selection-box { border: 1.5px solid #FFD600; background-color: #fffbef; border-radius: 8px; padding: 12px; position: relative; }
.clear-btn { background: none; border: none; color: #999; cursor: pointer; font-size: 0.9rem; position: absolute; top: 8px; right: 8px; }
.clear-btn:hover { color: #e65100; text-decoration: underline; }
.placeholder-text { color: #777; font-size: 1rem; text-align: center; padding: 20px 0; }
.location-details { display: flex; flex-direction: column; gap: 10px; }
.location-name { font-weight: bold; color: #e65100; font-size: 1rem; }
.predict-stats-horizontal { display: flex; justify-content: space-around; align-items: center; font-size: 0.95rem; font-weight: 600; }
.predict-take { color: #2e7d32; }
.predict-park { color: #d32f2f; }
.predict-total { color: #1976d2; }

.task-workers-list { display: flex; flex-direction: column; gap: 10px; max-height: 180px; overflow-y: auto; padding-right: 5px; }
.worker-card { border: 2px solid #eee; border-radius: 10px; padding: 10px 12px; display: flex; align-items: center; cursor: pointer; transition: border 0.2s, box-shadow 0.2s, background 0.2s; }
.worker-card:hover { border-color: #FFD600; }
.worker-card.selected { border: 2.5px solid #FFD600; background: #fffbe6; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.worker-avatar { width: 48px; height: 48px; border-radius: 50%; margin-right: 12px; background: #fff; border: 1.5px solid #FFD600; flex-shrink: 0; }
.worker-info { text-align: left; font-size: 0.9rem; color: #444; line-height: 1.4; }
.worker-id { font-weight: bold; }

.amount-input-group { display: flex; align-items: center; gap: 8px; }
.amount-btn { width: 32px; height: 32px; font-size: 1.2rem; border-radius: 50%; }
.amount-btn:disabled { background: #f1f1f1; color: #aaa; cursor: not-allowed; }
.amount-input { width: 48px; text-align: center; font-size: 1.1rem; border: 1px solid #ddd; border-radius: 6px; padding: 4px 0; outline: none; transition: border-color 0.2s; }
.amount-input:focus { border-color: #FFD600; }
.deploy-btn { width: 100%; margin-top: 10px; padding: 12px; }

.fade-enter-active, .fade-leave-active { transition: all 0.3s ease-in-out; transform-origin: top; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: scaleY(0.9); max-height: 0; }
.fade-enter-to, .fade-leave-from { opacity: 1; transform: scaleY(1); max-height: 1000px; }

.left-panels {
  position: fixed;
  top: 90px;
  left: 30px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 300px;
  max-width: 360px;
}

.collapsible-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  overflow: hidden;
}

.panel-header {
  padding: 12px 16px;
  background: #f8f8f8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.panel-header:hover {
  background: #f0f0f0;
}

.panel-title {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
}

.panel-icon {
  color: #666;
  font-size: 0.9rem;
}

.panel-content {
  background: white;
  transition: all 0.3s ease;
}

.info-card {
  padding: 16px;
}

.info-section {
  margin-bottom: 16px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.info-value {
  color: #333;
  font-weight: 500;
  font-size: 1.1rem;
}

/* 适配移动设备 */
@media (max-width: 768px) {
  .left-panels {
    left: 10px;
    right: 10px;
    min-width: unset;
    max-width: calc(100vw - 20px);
  }
}

.manager-input-group {
  margin-top: 8px;
}

.yellow-input {
  width: 100%;
  padding: 8px 12px;
  border: 1.5px solid #FFD600;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.yellow-input:focus {
  box-shadow: 0 0 0 2px rgba(255, 214, 0, 0.2);
}

.task-workers-list {
  margin-top: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 4px;
}

.task-workers-list::-webkit-scrollbar {
  width: 4px;
}

.task-workers-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.task-workers-list::-webkit-scrollbar-thumb {
  background: #FFD600;
  border-radius: 2px;
}

.worker-item {
  padding: 12px;
  border-radius: 8px;
  background: white;
  border: 1px solid #eee;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.worker-item:hover {
  border-color: #FFD600;
}

.worker-item.selected {
  background: #fff8e1;
  border-color: #FFD600;
  box-shadow: 0 0 0 1px #FFD600;
}

.worker-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.worker-id, .worker-name {
  font-size: 14px;
  color: #333;
}

.no-workers-tip {
  text-align: center;
  color: #666;
  padding: 16px;
  font-size: 14px;
}
</style>