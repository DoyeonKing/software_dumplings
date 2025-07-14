<template>
  <div class="location-view-root">
    <div id="mapContainer"></div>

    <MenuComponent @profile-saved="handleProfileSaved" />

    <div class="top-right-btn-group btn-group">
      <button class="yellow-btn" @click="onToggleHeatmap">
        {{ showHeatmap ? '显示普通地图' : '显示热力图' }}
      </button>
      <button class="yellow-btn" @click="goHome">
        返回主页
      </button>
    </div>

    <div class="left-info-panel">
      <div class="info-card">
        <div class="info-title">停车区域位置</div>
        <div class="info-content">
          <div>{{ currentArea.location }} - {{ currentArea.areaCode }}</div>
        </div>
      </div>
      <div class="info-card">
        <div class="info-title">现有停车数量</div>
        <div class="info-content">
          <div>现有车辆：<span class="info-number">{{ currentArea.currentBikes }}</span></div>
          <div>预估可用车位：<span class="info-number">{{ currentArea.availableSpots }}</span></div>
        </div>
      </div>
      <div class="info-card">
        <div class="info-title">车辆数预测</div>
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
                  <div class="location-name">{{ selectedStartArea.location }} - {{ selectedStartArea.areaCode }}</div>
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
                  <div class="location-name">{{ selectedEndArea.location }} - {{ selectedEndArea.areaCode }}</div>
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
                    <div class="worker-id">编号：{{ worker.id }}</div>
                    <div class="worker-name">姓名：{{ worker.name }}</div>
                    <div class="worker-phone">电话：{{ worker.phone }}</div>
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
import { mapMixin } from '@/utils/mapMixin.js';
import { loadAMapV1, unloadAMapV1 } from '@/utils/loadAMapV1.js';

export default {
  name: "LocationView",
  components: {
    MenuComponent
  },
  mixins: [mapMixin],
  data() {
    return {
      // General State
      taskPanelCollapsed: false,
      polygons: [],
      infoWindow: null, // For hover tooltips on map

      // Map & Left Panel Data
      currentArea: {
        location: "请在地图上选择区域",
        areaCode: "",
        currentBikes: 0,
        availableSpots: 0
      },
      predictHour: 1,
      predictData: { take: 0, park: 0, total: 0 },

      // Task Panel Data
      selectingFor: null, // 'start', 'end', or null
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

      // Static Data
      parkingAreas: [
        { id: 1, location: "深圳市-福田区-福华三路", areaCode: "P1001", polygon: [[114.0575, 22.5342], [114.0582, 22.5342], [114.0582, 22.5348], [114.0575, 22.5348]], currentBikes: 23, availableSpots: 7, baseTakeRate: 3, baseParkRate: 5 },
        { id: 2, location: "深圳市-福田区-金田路", areaCode: "P1002", polygon: [[114.0605, 22.5347], [114.0612, 22.5347], [114.0612, 22.5353], [114.0605, 22.5353]], currentBikes: 15, availableSpots: 15, baseTakeRate: 2, baseParkRate: 4 },
        { id: 3, location: "深圳市-福田区-滨河大道", areaCode: "P1003", polygon: [[114.0585, 22.5360], [114.0592, 22.5360], [114.0592, 22.5366], [114.0585, 22.5366]], currentBikes: 30, availableSpots: 5, baseTakeRate: 6, baseParkRate: 3 }
      ],
      workers: [
        { id: "W001", name: "李明", phone: "13800000001", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=1" },
        { id: "W002", name: "王芳", phone: "13800000002", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=2" },
        { id: "W003", name: "张伟", phone: "13800000003", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=3" },
        { id: "W004", name: "赵强", phone: "13800000004", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=4" }
      ],
      bikeList: [
        { id: "SZ1001", lng: 114.057868, lat: 22.53445, status: "正常", address: "深圳市-福田区-福华三路" },
        { id: "SZ1002", lng: 114.060868, lat: 22.53495, status: "故障", address: "深圳市-福田区-金田路" },
      ],
    };
  },
  watch: {
    predictHour() { this.updatePrediction(this.currentArea, this.predictHour, 'predictData'); },
    startPredictHour() { this.updatePrediction(this.selectedStartArea, this.startPredictHour, 'startPredictData'); },
    endPredictHour() { this.updatePrediction(this.selectedEndArea, this.endPredictHour, 'endPredictData'); },
    currentArea() { this.updatePrediction(this.currentArea, this.predictHour, 'predictData'); }
  },
  methods: {
    handleProfileSaved(formData) {
      console.log('个人资料已保存:', formData);
      window.alert('个人信息已在控制台输出。');
    },
    goHome() { this.$router.push('/admin'); },
    onToggleHeatmap() { this.toggleHeatmap(this.bikeList); },
    drawParkingAreas() {
      if (this.polygons && this.polygons.length) this.map.remove(this.polygons);
      this.polygons = [];
      this.parkingAreas.forEach(area => {
        const polygon = new window.AMap.Polygon({
          path: area.polygon.map(([lng, lat]) => [lng, lat]),
          fillColor: "#FFD600", fillOpacity: 0.3, strokeColor: "#FFD600",
          strokeWeight: 2, zIndex: 50, cursor: "pointer"
        });

        // --- NEW: Add mouse hover events for tooltip ---
        const infoWindowContent = `
          <div style="background-color: white; padding: 10px 15px; border-radius: 8px; box-shadow: 0 3px 8px rgba(0,0,0,0.2); border: 1px solid #ddd;">
              <div style="font-weight: bold; margin-bottom: 5px; font-size: 14px;">${area.location}</div>
              <div style="font-size: 13px; color: #333;">编号: ${area.areaCode}</div>
          </div>
        `;

        polygon.on("mouseover", (e) => {
          this.infoWindow = new window.AMap.InfoWindow({
            isCustom: true,
            content: infoWindowContent,
            offset: new window.AMap.Pixel(0, -15),
            anchor: 'bottom-center'
          });
          this.infoWindow.open(this.map, e.lnglat);
        });

        polygon.on("mouseout", () => {
          if (this.infoWindow) {
            this.infoWindow.close();
            this.infoWindow = null;
          }
        });
        // --- End of new hover events ---

        polygon.on("click", () => this.handlePolygonClick(area));
        polygon.setMap(this.map);
        this.polygons.push(polygon);
      });
    },
    handlePolygonClick(area) {
      if (this.selectingFor === 'start') {
        this.selectedStartArea = area;
        this.updatePrediction(this.selectedStartArea, this.startPredictHour, 'startPredictData');
        this.selectingFor = null;
      } else if (this.selectingFor === 'end') {
        this.selectedEndArea = area;
        this.updatePrediction(this.selectedEndArea, this.endPredictHour, 'endPredictData');
        this.selectingFor = null;
      } else {
        this.currentArea = area;
      }
    },
    updatePrediction(area, hour, dataProperty) {
      if (!area || !area.id) {
        this[dataProperty] = { take: 0, park: 0, total: 0 };
        return;
      }
      const take = Math.round(area.baseTakeRate * hour * (Math.random() * 0.4 + 0.8));
      const park = Math.round(area.baseParkRate * hour * (Math.random() * 0.4 + 0.8));
      const total = area.currentBikes + park - take;
      this[dataProperty] = { take, park, total };
    },
    activateSelection(type) {
      if (type === 'start') {
        this.startSelectionActive = true;
        this.selectingFor = 'start';
      } else if (type === 'end') {
        this.endSelectionActive = true;
        this.selectingFor = 'end';
      }
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
    },
    selectWorker(worker) { this.selectedWorker = worker; },
    changeDispatchAmount(delta) {
      let next = this.dispatchAmount + delta;
      if (next < 1) next = 1;
      this.dispatchAmount = next;
    },
    publishTask() {
      if (!this.selectedStartArea || !this.selectedEndArea || !this.selectedWorker || this.dispatchAmount < 1) return;
      // --- MODIFIED: Updated alert message for clarity ---
      alert(
          `调度任务已发布！\n\n` +
          `起点：${this.selectedStartArea.location} (编号: ${this.selectedStartArea.areaCode})\n` +
          `终点：${this.selectedEndArea.location} (编号: ${this.selectedEndArea.areaCode})\n` +
          `调度数量：${this.dispatchAmount}\n` +
          `工作人员：${this.selectedWorker.name} (${this.selectedWorker.phone})`
      );
      this.cancelOrClearSelection('start');
      this.cancelOrClearSelection('end');
      this.selectedWorker = null;
      this.dispatchAmount = 1;
    },
  },
  mounted() {
    loadAMapV1('dea7cc14dad7340b0c4e541dfa3d27b7').then(() => {
      const { yellowBikeIcon } = this.initMap();
      this.map.setZoomAndCenter(17, [114.0598, 22.5350]);
      this.addBikeMarkers(this.bikeList, yellowBikeIcon);
      this.drawParkingAreas();
      if (this.parkingAreas.length > 0) { this.currentArea = this.parkingAreas[0]; }
    }).catch(err => {
      alert('地图加载失败: ' + err.message);
    });
  },
  beforeDestroy() {
    if (this.map) {
      this.map.destroy();
      this.map = null;
    }
    // Close info window if it's open
    if (this.infoWindow) {
      this.infoWindow.close();
      this.infoWindow = null;
    }
    unloadAMapV1();
  }
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
.predict-take { color: #2e7d32; } /* Green */
.predict-park { color: #d32f2f; } /* Red */
.predict-total { color: #1976d2; } /* Blue */

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

@media (max-width: 900px) {
  .left-info-panel, .right-task-panel { left: 10px; right: 10px; top: 80px; min-width: unset; max-width: calc(100vw - 20px); gap: 8px; }
  .right-task-panel { top: auto; bottom: 10px; }
  .info-card, .task-card { padding: 12px; }
  .top-right-btn-group { right: 10px; gap: 10px; }
}
</style>