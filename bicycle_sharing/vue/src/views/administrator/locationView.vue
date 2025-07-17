<template>
  <div class="location-view-root">
    <div id="mapContainer"></div>

    <MenuComponent @profile-saved="handleProfileSaved" />

    <!-- API测试链接 - 左上角 -->
    <div class="top-left-link-group">
      <span class="api-test-link" @click="goToAPITest">
        API测试
      </span>
    </div>

    <div class="top-right-controls">
      <div class="control-group">
        <button class="control-btn" @click="onToggleBikes" :class="{ active: showBikes }">
          <span class="btn-icon">🚲</span>
          <span class="btn-text">{{ showBikes ? '隐藏单车' : '显示单车' }}</span>
        </button>
        <button class="control-btn" @click="onToggleHeatmap" :class="{ active: showHeatmap }">
          <span class="btn-icon">🔥</span>
          <span class="btn-text">{{ showHeatmap ? '普通地图' : '热力图' }}</span>
        </button>
        <button class="control-btn" @click="onToggleParkingAreas" :class="{ active: showParkingAreas }">
          <span class="btn-icon">🅿️</span>
          <span class="btn-text">{{ showParkingAreas ? '隐藏区域' : '显示区域' }}</span>
        </button>
        <button class="control-btn" @click="onToggleStaff" :class="{ active: showStaff }">
          <span class="btn-icon">👥</span>
          <span class="btn-text">{{ showStaff ? '隐藏人员' : '显示人员' }}</span>
        </button>
        <button class="control-btn" @click="goHome">
          <span class="btn-icon">🏠</span>
          <span class="btn-text">主页</span>
        </button>
      </div>
    </div>

    <!-- 左侧信息面板 - 使用标签页设计 -->
    <div class="left-panel-container">
      <div class="panel-tabs">
        <div 
          class="tab-button" 
          :class="{ active: activeTab === 'area' }"
          @click="switchTab('area')"
        >
          <span class="tab-icon">📊</span>
          <span class="tab-text">区域数据</span>
        </div>
        <div 
          class="tab-button" 
          :class="{ active: activeTab === 'suggestion' }"
          @click="switchTab('suggestion')"
        >
          <span class="tab-icon">💡</span>
          <span class="tab-text">调度建议</span>
        </div>
        <div 
          class="tab-button" 
          :class="{ active: activeTab === 'task' }"
          @click="switchTab('task')"
        >
          <span class="tab-icon">📋</span>
          <span class="tab-text">任务查询</span>
        </div>
      </div>
      
      <div class="panel-content-container">
        <div class="panel-content" v-show="activeTab === 'area'">
          <AreaDataPanel 
            :map="map"
            ref="areaDataPanel"
          />
        </div>
        
        <div class="panel-content" v-show="activeTab === 'suggestion'">
          <DispatchSuggestionPanel
              :map="map"
              @suggestion-accepted="handleSuggestionAccepted"
              @suggestion-rejected="handleSuggestionRejected"
          />
        </div>
        
        <div class="panel-content" v-show="activeTab === 'task'">
          <TaskQueryPanel />
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
              <div class="location-selection-container">
                <!-- 手动输入区域 -->
                <div class="input-section">
                  <input 
                    v-model="startInputValue"
                    @input="onStartInput"
                    placeholder="输入停车区域编号..."
                    class="area-input"
                    :disabled="startSelectionActive"
                  />
                </div>
                
                <!-- 地图选择按钮 -->
                <button v-if="!startSelectionActive" class="yellow-btn select-location-btn" @click="activateSelection('start')">
                  地图选择
                </button>
                <div v-if="startSelectionActive" class="location-selection-box">
                  <div v-if="!selectedStartArea" class="placeholder-text">
                    请在地图上选择起点区域...
                  </div>
                  <div v-if="selectedStartArea" class="location-details">
                    <div class="location-name">{{ selectedStartArea.geohash }}</div>
                  </div>
                  <button class="clear-btn" @click="cancelOrClearSelection('start')">
                    {{ selectedStartArea ? '清空起点' : '取消' }}
                  </button>
                </div>
              </div>
            </div>

            <div class="task-section">
              <label>调度终点</label>
              <div class="location-selection-container">
                <!-- 手动输入区域 -->
                <div class="input-section">
                  <input 
                    v-model="endInputValue"
                    @input="onEndInput"
                    placeholder="输入停车区域编号..."
                    class="area-input"
                    :disabled="endSelectionActive"
                  />
                </div>
                
                <!-- 地图选择按钮 -->
                <button v-if="!endSelectionActive" class="yellow-btn select-location-btn" @click="activateSelection('end')">
                  地图选择
                </button>
                <div v-if="endSelectionActive" class="location-selection-box">
                  <div v-if="!selectedEndArea" class="placeholder-text">
                    请在地图上选择终点区域...
                  </div>
                  <div v-if="selectedEndArea" class="location-details">
                    <div class="location-name">{{ selectedEndArea.geohash }}</div>
                  </div>
                  <button class="clear-btn" @click="cancelOrClearSelection('end')">
                    {{ selectedEndArea ? '清空终点' : '取消' }}
                  </button>
                </div>
              </div>
            </div>

            <div class="task-section">
              <label>选择工作人员：</label>
              <div class="task-workers-list">
                <div
                    v-for="worker in workers"
                    :key="worker.id"
                    :class="['worker-card', { selected: selectedWorker && selectedWorker.id === worker.id }]"
                    @click="selectWorker(worker)"
                >
                  <img :src="worker.avatar" class="worker-avatar" />
                  <div class="worker-info">
                    <div class="worker-staff-id">工作人员ID：{{ worker.staffId }}</div>
                    <div class="worker-username">用户名：{{ worker.username }}</div>
                    <div class="worker-manager-id">上级管理员ID：{{ worker.managerId }}</div>
                    <div class="worker-geohash">负责区域：{{ worker.geohash }}</div>
                  </div>
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
                    :disabled="!startAreaValid || !endAreaValid || !selectedWorker || dispatchAmount<1"
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
import AreaDataPanel from '@/components/admin/AreaDataPanel.vue';
import TaskQueryPanel from '@/components/admin/TaskQueryPanel.vue';
import AMapLoader from '@/utils/loadAMap.js';
import bicycleIcon from '@/components/icons/bicycle.png';
import staffIcon from '@/components/icons/staff_position.png';
import { getMapAreaBicycles } from '@/api/map/bicycle';
import { getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking.js';
import { getManagedStaff, getStaffWorkers } from '@/api/account/staffService.js';
import { createDispatchTask } from '@/api/assignment/dispatchService.js';

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
    DispatchSuggestionPanel,
    AreaDataPanel,
    TaskQueryPanel
  },
  data() {
    return {
      taskPanelCollapsed: false,
      polygons: [],
      polygonMap: {},
      currentArea: { geohash: "请在地图上选择区域", currentBikes: 0, availableSpots: 0 },
      selectingFor: null,
      startSelectionActive: false,
      endSelectionActive: false,
      selectedStartArea: null,
      selectedEndArea: null,

      selectedWorker: null,
      dispatchAmount: 1,
      workers: [],
      // 地图相关
      map: null,
      infoWindow: null,
      markers: [],
      heatmap: null,
      heatmapReady: false,
      showHeatmap: false,
      parkingAreas: [],
      bikes: [],
      showBikes: false, // 默认隐藏单车
      showParkingAreas: false, // 默认隐藏停车区域
      showStaff: false, // 默认不显示工作人员
      staffMarkers: [], // 工作人员标记
      staffData: [], // 工作人员数据
      // 标签页状态
      activeTab: 'area', // 默认显示区域数据标签页
      // 添加高亮区域的颜色配置
      areaColors: {
        start: { fillColor: "#ffcdd2", fillOpacity: 0.5, strokeColor: "#ef5350" },
        end: { fillColor: "#c8e6c9", fillOpacity: 0.5, strokeColor: "#66bb6a" }
      },
      // 停车区域选择相关
      startInputValue: '',
      endInputValue: '',
      startAreaValid: false,
      endAreaValid: false,

    };
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
        } else if (response && (response.code === 200 || response.code === '200') && Array.isArray(response.data)) {
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

    onToggleParkingAreas() {
      this.showParkingAreas = !this.showParkingAreas;
      
      if (this.polygons && this.polygons.length > 0) {
        this.polygons.forEach(polygon => {
          if (this.showParkingAreas) {
            polygon.setMap(this.map);
          } else {
            polygon.setMap(null);
          }
        });
      }
      
      console.log(`停车区域已${this.showParkingAreas ? '显示' : '隐藏'}`);
    },

    onToggleStaff() {
      this.showStaff = !this.showStaff;
      if (this.showStaff) {
        if (this.staffData.length > 0) {
          // 如果已有数据，直接显示标记
          this.addStaffMarkers(this.staffData);
          this.showStaffMarkers();
        } else {
          // 如果没有数据，重新加载
          this.loadStaffData();
        }
      } else {
        this.hideStaffMarkers();
      }
      console.log(`工作人员已${this.showStaff ? '显示' : '隐藏'}`);
    },

    async loadStaffData() {
      try {
        console.log('开始加载工作人员数据...');
        const response = await getStaffWorkers();
        console.log('工作人员API响应:', response);
        
        let staffData = [];
        
        // 处理响应数据
        if (response && Array.isArray(response)) {
          staffData = response;
        } else if (response && response.data && Array.isArray(response.data)) {
          staffData = response.data;
        } else if (response && (response.code === 200 || response.code === '200') && Array.isArray(response.data)) {
          staffData = response.data;
        }
        
        console.log('处理后的工作人员数据:', staffData);
        this.staffData = staffData;
        
        if (staffData.length > 0) {
          this.addStaffMarkers(staffData);
        } else {
          console.warn('没有获取到工作人员数据');
        }
      } catch (error) {
        console.error("加载工作人员数据失败:", error);
        this.staffData = [];
      }
    },

    addStaffMarkers(staffList) {
      console.log('开始添加工作人员标记，数据:', staffList);
      // 清除现有标记
      this.hideStaffMarkers();
      
      if (!this.map || !staffList || staffList.length === 0) {
        console.warn('无法添加工作人员标记：地图未初始化或数据为空');
        return;
      }
      
      const staffMarkerIcon = new window.AMap.Icon({ 
        image: staffIcon, 
        size: new window.AMap.Size(32, 32),
        imageSize: new window.AMap.Size(32, 32)
      });
      
      let markerCount = 0;
      staffList.forEach(staff => {
        console.log('处理工作人员:', staff);
        if (staff.latitude && staff.longitude) {
          console.log(`添加工作人员标记: ${staff.username} at [${staff.longitude}, ${staff.latitude}]`);
          const marker = new window.AMap.Marker({ 
            position: [staff.longitude, staff.latitude], 
            map: null, // 不立即添加到地图，通过showStaffMarkers控制
            icon: staffMarkerIcon, 
            title: `工作人员: ${staff.username}` 
          });
          
          marker.on('mouseover', () => {
            this.infoWindow.setContent(`
              <div style="padding: 8px 12px; font-size: 14px;">
                <b>工作人员信息</b><br>
                <b>ID：</b>${staff.staffId}<br>
                <b>用户名：</b>${staff.username}<br>
                <b>上级管理员：</b>${staff.managerId}<br>
                <b>负责区域：</b>${staff.geohash}<br>
                <b>位置：</b>${staff.latitude}, ${staff.longitude}
              </div>
            `);
            this.infoWindow.open(this.map, marker.getPosition());
          });
          
          marker.on('mouseout', () => this.infoWindow.close());
          
          this.staffMarkers.push(marker);
          markerCount++;
        } else {
          console.warn(`工作人员 ${staff.username} 缺少经纬度信息`);
        }
      });
      
      console.log(`成功添加了 ${markerCount} 个工作人员标记`);
    },

    hideStaffMarkers() {
      this.staffMarkers.forEach(marker => {
        marker.setMap(null);
      });
      this.staffMarkers = [];
    },

    showStaffMarkers() {
      this.staffMarkers.forEach(marker => {
        marker.setMap(this.map);
      });
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
        
        // 根据showParkingAreas状态决定是否显示
        if (this.showParkingAreas) {
          polygon.setMap(this.map);
        }

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
        this.startAreaValid = true;
        this.selectingFor = null;
        // 清空输入框
        this.startInputValue = '';
      } else if (this.selectingFor === 'end') {
        if (this.selectedStartArea && this.selectedStartArea.id === area.id) {
          alert('起点和终点不能是同一个区域！');
          return;
        }
        this.selectedEndArea = area;
        this.endAreaValid = true;
        this.selectingFor = null;
        // 清空输入框
        this.endInputValue = '';
      } else {
        this.currentArea = area;
        // 通知AreaDataPanel组件区域选择变化
        if (this.$refs.areaDataPanel) {
          this.$refs.areaDataPanel.setSelectedArea(area.geohash);
        }
      }
      this.updatePolygonStyles();
    },

    cancelOrClearSelection(type) {
      if (type === 'start') {
        this.startSelectionActive = false;
        this.selectedStartArea = null;
        this.selectingFor = null;
        // 清空输入框
        this.startInputValue = '';
      } else if (type === 'end') {
        this.endSelectionActive = false;
        this.selectedEndArea = null;
        this.selectingFor = null;
        // 清空输入框
        this.endInputValue = '';
        this.endSuggestions = [];
        this.showEndSuggestions = false;
      }
      this.updatePolygonStyles();
    },

    async publishTask() {
      // 优先用地图选择，否则用输入框
      let startArea = this.selectedStartArea;
      let endArea = this.selectedEndArea;
      if (!startArea && this.startInputValue.trim()) {
        startArea = this.parkingAreas.find(a => a.geohash === this.startInputValue.trim());
      }
      if (!endArea && this.endInputValue.trim()) {
        endArea = this.parkingAreas.find(a => a.geohash === this.endInputValue.trim());
      }
      if (!startArea || !endArea || !this.selectedWorker || this.dispatchAmount < 1) {
        alert('请填写完整的起点、终点、工作人员和调度数量！');
        return;
      }
      try {
        await createDispatchTask({
          startGeohash: startArea.geohash,
          endGeohash: endArea.geohash,
          assignedTo: this.selectedWorker.id,
          bikeCount: this.dispatchAmount
        });
        alert(`调度任务已发布！\n\n起点：${startArea.geohash}\n终点：${endArea.geohash}\n调度数量：${this.dispatchAmount}\n执行工作人员：${this.selectedWorker.username} (ID: ${this.selectedWorker.staffId})\n负责区域：${this.selectedWorker.geohash}`);
        // 清空
        this.cancelOrClearSelection('start');
        this.cancelOrClearSelection('end');
        this.selectedWorker = null;
        this.dispatchAmount = 1;
        this.startInputValue = '';
        this.endInputValue = '';
        this.startAreaValid = false;
        this.endAreaValid = false;
      } catch (e) {
        alert('调度任务发布失败，请重试！');
      }
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
    goToAPITest() { this.$router.push('/api-test-manager-staff'); },



    // 起点输入处理
    onStartInput() {
      const val = this.startInputValue.trim();
      if (this.startSelectionActive) return;
      const area = this.parkingAreas.find(a => a.geohash === val);
      if (area) {
        this.selectedStartArea = area;
        this.startAreaValid = true;
      } else {
        this.selectedStartArea = null;
        this.startAreaValid = false;
      }
    },

    // 终点输入处理
    onEndInput() {
      const val = this.endInputValue.trim();
      if (this.endSelectionActive) return;
      const area = this.parkingAreas.find(a => a.geohash === val);
      if (area) {
        this.selectedEndArea = area;
        this.endAreaValid = true;
      } else {
        this.selectedEndArea = null;
        this.endAreaValid = false;
      }
    },



    activateSelection(type) {
      if (type === 'start') { 
        this.startSelectionActive = true; 
        this.selectingFor = 'start'; 
        // 清空输入框
        this.startInputValue = '';
      }
      else if (type === 'end') { 
        this.endSelectionActive = true; 
        this.selectingFor = 'end'; 
        // 清空输入框
        this.endInputValue = '';
      }
    },
    selectWorker(worker) { this.selectedWorker = worker; },
    changeDispatchAmount(delta) {
      let next = this.dispatchAmount + delta;
      if (next < 1) next = 1;
      this.dispatchAmount = next;
    },

    // 标签页切换方法
    switchTab(tabName) {
      this.activeTab = tabName;
    },

    // 处理调度建议的接受和拒绝
    handleSuggestionAccepted(suggestion) {
      console.log('接受调度建议:', suggestion);
      
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



      // 更新地图上的多边形样式
      this.updatePolygonStyles();
      
      // 显示成功消息
      alert(`已接受调度建议！\n起点：${suggestion.startArea}\n终点：${suggestion.endArea}\n调度数量：${suggestion.amount}辆`);
    },

    handleSuggestionRejected(suggestion) {
      console.log('拒绝调度建议:', suggestion);
      alert(`已拒绝调度建议 ID: ${suggestion.id}`);
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
    },





    // 获取管理员手下的工作人员
    async loadManagedStaff() {
      try {
        // 检查是否有token
        const token = sessionStorage.getItem('authToken'); // 修正键名为authToken
        console.log('当前token:', token); // 调试信息
        console.log('Authorization头部格式:', token ? 'Bearer ' + token : ''); // 调试信息
        
        if (!token) {
          console.warn('未找到登录token，无法获取工作人员数据');
          this.workers = [];
          return;
        }

        console.log('开始调用getManagedStaff API...'); // 调试信息
        const response = await getManagedStaff();
        console.log('API完整响应:', response); // 调试信息
        
        if (response && (response.code === 200 || response.code === '200') && response.data) {
          // 处理API返回的数据，显示完整的工作人员信息
          this.workers = response.data.map(staff => ({
            id: staff.staffId, // 工作人员ID
            staffId: staff.staffId, // 工作人员ID
            username: staff.username, // 用户名
            managerId: staff.managerId, // 上级管理员ID
            geohash: staff.geohash, // 负责区域
            avatar: `https://api.dicebear.com/7.x/miniavs/svg?seed=${staff.staffId}` // 生成头像
          }));
          console.log('成功加载工作人员数据:', this.workers);
        } else {
          console.warn('获取工作人员数据失败:', response);
          // 如果是500错误，可能是后端问题
          if (response && response.code === '500') {
            console.error('后端服务器错误(500)，可能原因：');
            console.error('1. 数据库连接问题');
            console.error('2. 后端业务逻辑错误');
            console.error('3. Token格式或权限问题');
            console.error('错误信息:', response.msg);
          }
          this.workers = [];
        }
      } catch (error) {
        console.error('加载工作人员数据失败:', error);
        console.error('错误详情:', error.response); // 更详细的错误信息
        
        // 根据不同的错误状态码给出提示
        if (error.response) {
          switch (error.response.status) {
            case 401:
              console.warn('登录已过期或无权限，请重新登录');
              break;
            case 403:
              console.warn('权限不足，无法访问工作人员数据');
              break;
            case 500:
              console.warn('服务器内部错误，请联系管理员');
              break;
            default:
              console.warn(`HTTP错误: ${error.response.status}`);
          }
        }
        this.workers = [];
      }
    }
  },
  mounted() {
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      console.log('开始初始化地图...');
      
      // 初始化地图
      this.map = new window.AMap.Map("mapContainer", {
        center: [114.0610, 22.5395],
        zoom: 17, // 更高的缩放级别
        dragEnable: true,
        zoomEnable: true,
        doubleClickZoom: true,
        keyboardEnable: true,
        scrollWheel: true,
        touchZoom: true,
        mapStyle: 'amap://styles/normal'
      });

      console.log('地图初始化完成，地图实例:', this.map);

      // 初始化信息窗口
      this.infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });

      // 加载热力图插件
      window.AMap.plugin(['AMap.HeatMap'], () => {
        this.heatmap = new window.AMap.HeatMap(this.map, {
          radius: 20,
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
        console.log('热力图插件加载完成');
      });

      // 【修改】将数据加载逻辑统一管理，并添加防抖
      let timeout;
      const loadAllData = () => {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
          console.log('开始加载地图数据...');
          this.loadBicycles();
          this.loadParkingAreas();
        }, 500); // 500ms的防抖延迟
      };

      // 初始加载所有数据
      loadAllData();

      // 监听地图移动和缩放事件，使用防抖函数
      this.map.on('moveend', loadAllData);
      this.map.on('zoomend', loadAllData);
      
      console.log('地图事件监听器设置完成');

    }).catch(err => { 
      console.error('地图加载失败:', err);
      alert('地图加载失败: ' + err.message); 
    });

    // 加载工作人员数据
    console.log('开始加载工作人员数据...');
    this.loadManagedStaff();
    
    // 预加载工作人员位置数据（用于地图显示）
    console.log('开始预加载工作人员位置数据...');
    this.loadStaffData();
    
    // 确保调度建议面板默认展开
    this.suggestionPanelExpanded = true;
    console.log('locationView 组件初始化完成');
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
.top-left-link-group { position: fixed; top: 0px; left: 15px; z-index: 30; }
.top-right-controls {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 30;
  display: flex;
  align-items: flex-end;
}

.control-group {
  display: flex;
  gap: 4px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 10px;
  padding: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 214, 0, 0.15);
}

.control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 8px 10px;
  border: none;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.8);
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.7rem;
  font-weight: 500;
  min-width: 60px;
  backdrop-filter: blur(5px);
}

.control-btn:hover {
  background: rgba(255, 214, 0, 0.15);
  color: #333;
  transform: translateY(-1px);
}

.control-btn.active {
  background: #FFD600;
  color: #333;
  box-shadow: 0 2px 8px rgba(255, 214, 0, 0.3);
}

.btn-icon {
  font-size: 1rem;
  line-height: 1;
}

.btn-text {
  font-size: 0.65rem;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
  font-weight: 500;
}


.left-info-panel { position: fixed; top: 90px; left: 30px; z-index: 20; display: flex; flex-direction: column; gap: 14px; min-width: 260px; max-width: 320px; }
.info-title { font-size: 1.1rem; font-weight: 700; color: #222; margin-bottom: 6px; }
.info-content { font-size: 1rem; color: #444; }
.info-number { color: #FFD600; font-weight: bold; font-size: 1.1em; }

.api-test-link { 
  color: #17a2b8; 
  font-size: 0.75rem; 
  font-weight: 500; 
  cursor: pointer; 
  transition: all 0.3s; 
  text-decoration: underline;
  user-select: none;
}
.api-test-link:hover { 
  color: #138496; 
  text-decoration: none;
}


.right-task-panel { position: fixed; top: 80px; right: 23px; z-index: 20; display: flex; flex-direction: column; min-width: 300px; max-width: 360px; align-items: flex-end; }
.task-card { background: #fff; border-radius: 12px; box-shadow: 0 2px 16px rgba(0,0,0,0.08); padding: 14px 18px; display: flex; flex-direction: column; gap: 12px; width: 100%; }
.task-title-row { display: flex; align-items: center; justify-content: space-between; }
.task-title { font-size: 1.1rem; font-weight: 700; color: #222; }
.collapse-btn { padding: 3px 15px; font-size: 14px; border-radius: 14px; min-width: 65px; margin-left: 8px; font-weight: 500; }
.task-section { display: flex; flex-direction: column; gap: 8px; border-bottom: 1px solid #f0f0f0; padding-bottom: 12px; }
.task-section:last-child { border-bottom: none; padding-bottom: 0; }
.task-section > label { font-weight: 600; font-size: 1rem; color: #333; }
.select-location-btn { font-size: 0.9rem; padding: 7px 14px; width: 100%; }

.location-selection-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-section {
  position: relative;
}

.area-input {
  width: 90%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
  outline: none;
  transition: border-color 0.2s;
}

.area-input:focus {
  border-color: #FFD600;
  background: #fffbe6;
}

.area-input:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

.suggestion-item {
  padding: 8px 12px;
  cursor: pointer;
  font-size: 0.9rem;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background-color: #f8f9fa;
}

.suggestion-item:last-child {
  border-bottom: none;
}



.location-selection-box { border: 1.5px solid #FFD600; background-color: #fffbef; border-radius: 8px; padding: 10px; position: relative; }
.clear-btn { background: none; border: none; color: #999; cursor: pointer; font-size: 0.85rem; position: absolute; top: 6px; right: 6px; }
.clear-btn:hover { color: #e65100; text-decoration: underline; }
.placeholder-text { color: #777; font-size: 0.9rem; text-align: center; padding: 16px 0; }
.location-details { display: flex; flex-direction: column; gap: 8px; }
.location-name { font-weight: bold; color: #e65100; font-size: 0.95rem; }


.task-workers-list { display: flex; flex-direction: column; gap: 10px; max-height: 200px; overflow-y: auto; padding-right: 5px; }
.worker-card { border: 2px solid #eee; border-radius: 8px; padding: 10px 12px; display: flex; align-items: flex-start; cursor: pointer; transition: border 0.2s, box-shadow 0.2s, background 0.2s; min-height: 80px; }
.worker-card:hover { border-color: #FFD600; }
.worker-card.selected { border: 2.5px solid #FFD600; background: #fffbe6; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.worker-avatar { width: 40px; height: 40px; border-radius: 50%; margin-right: 12px; background: #fff; border: 1.5px solid #FFD600; flex-shrink: 0; margin-top: 2px; }
.worker-info { text-align: left; font-size: 0.8rem; color: #444; line-height: 1.4; flex: 1; }
.worker-staff-id { font-weight: bold; color: #333; margin-bottom: 4px; }
.worker-username { color: #2c5aa0; font-weight: 600; margin-bottom: 4px; }
.worker-manager-id { color: #666; margin-bottom: 4px; }
.worker-geohash { color: #28a745; font-weight: 500; background: #f8f9fa; padding: 2px 6px; border-radius: 4px; display: inline-block; }

.amount-input-group { display: flex; align-items: center; gap: 6px; }
.amount-btn { width: 28px; height: 28px; font-size: 1.1rem; border-radius: 50%; }
.amount-btn:disabled { background: #f1f1f1; color: #aaa; cursor: not-allowed; }
.amount-input { width: 44px; text-align: center; font-size: 1rem; border: 1px solid #ddd; border-radius: 6px; padding: 3px 0; outline: none; transition: border-color 0.2s; }
.amount-input:focus { border-color: #FFD600; }
.deploy-btn { width: 100%; margin-top: 8px; padding: 10px; }

.fade-enter-active, .fade-leave-active { transition: all 0.3s ease-in-out; transform-origin: top; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: scaleY(0.9); max-height: 0; }
.fade-enter-to, .fade-leave-from { opacity: 1; transform: scaleY(1); max-height: 1000px; }

.left-panel-container {
  position: fixed;
  top: 90px;
  left: 30px;
  z-index: 20;
  min-width: 320px;
  max-width: 380px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  transition: all 0.3s ease;
}

.panel-tabs {
  display: flex;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  border-radius: 16px 16px 0 0;
}

.tab-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 8px;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s ease;
  position: relative;
  font-weight: 600;
  color: #666;
  border-radius: 16px 16px 0 0;
  font-size: 0.9rem;
}

.tab-button:hover {
  background: #e9ecef;
  color: #333;
}

.tab-button.active {
  background: white;
  color: #333;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #FFD600;
}

.tab-icon {
  font-size: 1rem;
}

.tab-text {
  font-size: 0.85rem;
}

.panel-content-container {
  background: white;
  border-radius: 0 0 16px 16px;
  max-height: 75vh;
  overflow: hidden;
}

.panel-content {
  padding: 0;
  height: 100%;
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
  .left-panel-container {
    left: 10px;
    right: 10px;
    min-width: unset;
    max-width: calc(100vw - 20px);
  }
  
  .tab-button {
    padding: 12px 8px;
  }
  
  .tab-text {
    font-size: 0.9rem;
  }
}
</style>