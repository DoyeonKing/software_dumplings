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
          <div class="task-title">发布投放任务</div>
          <button class="collapse-btn yellow-btn" @click="taskPanelCollapsed = !taskPanelCollapsed">
            {{ taskPanelCollapsed ? '展开' : '收起' }}
          </button>
        </div>
        <transition name="fade">
          <div v-show="!taskPanelCollapsed">
            <div class="task-section">
              <label>投放/取车地点：</label>
              <span class="deploy-location-value">
                {{ selectedDeployArea ? selectedDeployArea.location + ' - ' + selectedDeployArea.areaCode : '请在地图上选择区域' }}
              </span>
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
              <label>投放数目：</label>
              <div class="amount-input-group">
                <button class="amount-btn yellow-btn" @click="changeDeployAmount(-1)" :disabled="deployAmount<=1">-</button>
                <input class="amount-input" type="number" v-model.number="deployAmount" min="1" />
                <button class="amount-btn yellow-btn" @click="changeDeployAmount(1)">+</button>
              </div>
            </div>
            <div class="task-section">
              <label>取车数目：</label>
              <div class="amount-input-group">
                <button class="amount-btn yellow-btn" @click="changePickupAmount(-1)" :disabled="pickupAmount<=0">-</button>
                <input class="amount-input" type="number" v-model.number="pickupAmount" min="0" />
                <button class="amount-btn yellow-btn" @click="changePickupAmount(1)">+</button>
              </div>
            </div>
            <button class="yellow-btn deploy-btn"
                    @click="publishTask"
                    :disabled="!selectedDeployArea || !selectedWorker || deployAmount<1"
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
import AMapLoader from '@/utils/loadAMap.js';

export default {
  name: "LocationView",
  components: {
    MenuComponent
  },
  mixins: [mapMixin],
  data() {
    return {
      parkingAreas: [
        {
          id: 1,
          location: "深圳市-福田区-福华三路",
          areaCode: "区域A",
          polygon: [
            [114.0575, 22.5342],
            [114.0582, 22.5342],
            [114.0582, 22.5348],
            [114.0575, 22.5348]
          ],
          currentBikes: 23,
          availableSpots: 7
        },
        {
          id: 2,
          location: "深圳市-福田区-金田路",
          areaCode: "区域B",
          polygon: [
            [114.0605, 22.5347],
            [114.0612, 22.5347],
            [114.0612, 22.5353],
            [114.0605, 22.5353]
          ],
          currentBikes: 15,
          availableSpots: 15
        }
      ],
      currentArea: {
        location: "深圳市-福田区-福华三路",
        areaCode: "区域A",
        currentBikes: 23,
        availableSpots: 7
      },
      selectedDeployArea: null,
      polygons: [],
      predictHour: 1,
      predictData: {
        take: 3,
        park: 5,
        total: 25
      },
      workers: [
        { id: "W001", name: "李明", phone: "13800000001", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=1" },
        { id: "W002", name: "王芳", phone: "13800000002", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=2" },
        { id: "W003", name: "张伟", phone: "13800000003", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=3" },
        { id: "W004", name: "赵强", phone: "13800000004", avatar: "https://api.dicebear.com/7.x/miniavs/svg?seed=4" }
      ],
      selectedWorker: null,
      deployAmount: 1,
      pickupAmount: 0,
      taskPanelCollapsed: false,
      bikeList: [
        { id: "SZ1001", lng: 114.057868, lat: 22.53445, status: "正常", address: "深圳市-福田区-福华三路" },
        { id: "SZ1002", lng: 114.060868, lat: 22.53495, status: "故障", address: "深圳市-福田区-金田路" },
        { id: "SZ1003", lng: 114.058868, lat: 22.53645, status: "待维修", address: "深圳市-福田区-滨河大道" },
        { id: "SZ1004", lng: 114.061868, lat: 22.53445, status: "正常", address: "深圳市-福田区-会展中心" },
        { id: "SZ1005", lng: 114.061867, lat: 22.53545, status: "正常", address: "深圳市-福田区-福华一路" },
        { id: "SZ1006", lng: 114.057000, lat: 22.53400, status: "正常", address: "深圳市-福田区-福华三路附近" },
        { id: "SZ1007", lng: 114.058500, lat: 22.53500, status: "正常", address: "深圳市-福田区-金田路附近" },
        { id: "SZ1008", lng: 114.059500, lat: 22.53600, status: "故障", address: "深圳市-福田区-滨河大道附近" },
        { id: "SZ1009", lng: 114.060500, lat: 22.53400, status: "正常", address: "深圳市-福田区-会展中心附近" },
        { id: "SZ1010", lng: 114.062500, lat: 22.53500, status: "待维修", address: "深圳市-福田区-福华一路附近" },
        { id: "SZ1011", lng: 114.057200, lat: 22.53460, status: "正常", address: "深圳市-福田区-中心区" },
        { id: "SZ1012", lng: 114.060200, lat: 22.53520, status: "正常", address: "深圳市-福田区-益田路" },
        { id: "SZ1013", lng: 114.058000, lat: 22.53680, status: "正常", address: "深圳市-福田区-彩田路" },
        { id: "SZ1014", lng: 114.061000, lat: 22.53480, status: "故障", address: "深圳市-福田区-民田路" },
        { id: "SZ1015", lng: 114.061500, lat: 22.53580, status: "正常", address: "深圳市-福田区-福中路" },
        { id: "SZ1016", lng: 114.059000, lat: 22.53420, status: "正常", address: "深圳市-福田区-新闻路" },
        { id: "SZ1017", lng: 114.058200, lat: 22.53540, status: "正常", address: "深圳市-福田区-景田路" },
        { id: "SZ1018", lng: 114.060000, lat: 22.53620, status: "待维修", address: "深圳市-福田区-华强北" },
        { id: "SZ1019", lng: 114.062000, lat: 22.53460, status: "正常", address: "深圳市-福田区-八卦岭" },
        { id: "SZ1020", lng: 114.062200, lat: 22.53560, status: "正常", address: "深圳市-福田区-上步路" }
      ],
    };
  },
  watch: {
    predictHour() {
      this.predictData = {
        take: 3 * this.predictHour,
        park: 5 * this.predictHour,
        total: this.currentArea.currentBikes + (5 * this.predictHour) - (3 * this.predictHour)
      };
    }
  },
  methods: {
    handleProfileSaved(formData) {
      console.log('个人资料已保存:', formData);
      window.alert('个人信息已在控制台输出。');
    },
    goHome() {
      this.$router.push('/admin');
    },
    onToggleHeatmap() {
      this.toggleHeatmap(this.bikeList);
    },
    drawParkingAreas() {
      if (this.polygons && this.polygons.length) {
        this.map.remove(this.polygons);
      }
      this.polygons = [];
      this.parkingAreas.forEach(area => {
        const polygon = new window.AMap.Polygon({
          path: area.polygon.map(([lng, lat]) => [lng, lat]),
          fillColor: "#FFD600",
          fillOpacity: 0.3,
          strokeColor: "#FFD600",
          strokeWeight: 2,
          zIndex: 50,
          cursor: "pointer"
        });
        polygon.setMap(this.map);
        polygon.on("click", () => this.handlePolygonClick(area));
        this.polygons.push(polygon);
      });
    },
    handlePolygonClick(area) {
      this.currentArea = area;
      this.selectedDeployArea = area;
    },
    selectWorker(worker) {
      this.selectedWorker = worker;
    },
    changeDeployAmount(delta) {
      let next = this.deployAmount + delta;
      if (next < 1) next = 1;
      this.deployAmount = next;
    },
    changePickupAmount(delta) {
      let next = this.pickupAmount + delta;
      if (next < 0) next = 0;
      this.pickupAmount = next;
    },
    publishTask() {
      if (!this.selectedDeployArea || !this.selectedWorker || this.deployAmount < 1) return;
      alert(
          `投放任务已发布！\n\n地点：${this.selectedDeployArea.location} - ${this.selectedDeployArea.areaCode}\n` +
          `工作人员：${this.selectedWorker.name}（${this.selectedWorker.phone}）\n` +
          `投放数目：${this.deployAmount}\n取车数目：${this.pickupAmount}`
      );
      this.selectedDeployArea = null;
      this.selectedWorker = null;
      this.deployAmount = 1;
      this.pickupAmount = 0;
    },
  },
  mounted() {
    // 动态加载高德地图SDK，加载完成后再初始化地图
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      const { yellowBikeIcon } = this.initMap();
      this.map.setZoomAndCenter(17, [114.0598, 22.5350]);
      this.addBikeMarkers(this.bikeList, yellowBikeIcon);
      this.drawParkingAreas();
    }).catch(err => {
      this.$message && this.$message.error
          ? this.$message.error('地图加载失败: ' + err.message)
          : alert('地图加载失败: ' + err.message);
    });
  },
  beforeDestroy() {
    if (this.map) {
      this.map.destroy();
    }
  }
};
</script>

<style scoped>
@import '@/assets/globalStyles.css';

.location-view-root {
  position: relative;
  height: 100vh;
  overflow: hidden;
}

#mapContainer {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1;
}

.top-right-btn-group {
  position: fixed;
  top: 20px;
  right: 30px;
  z-index: 30;
}

.left-info-panel {
  position: fixed;
  top: 90px;
  left: 30px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 260px;
  max-width: 320px;
}
.info-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 6px;
}
.info-content {
  font-size: 1rem;
  color: #444;
}
.info-number {
  color: #FFD600;
  font-weight: bold;
  font-size: 1.1em;
}
.predict-filter {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.yellow-select {
  background: #f7f7f7;
  border: 1.5px solid #FFD600;
  border-radius: 8px;
  padding: 4px 12px;
  font-size: 1rem;
  color: #222;
  font-weight: bold;
  outline: none;
  transition: background 0.2s;
}
.yellow-select:focus {
  background: #fffbe6;
}
.predict-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 1rem;
  color: #444;
}

.right-task-panel {
  position: fixed;
  top: 90px;
  right: 30px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  min-width: 320px;
  max-width: 380px;
  align-items: flex-end;
}
.task-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.08);
  padding: 18px 24px 18px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
  position: relative;
}
.task-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.task-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #222;
}
.collapse-btn {
  padding: 4px 18px;
  font-size: 15px;
  border-radius: 16px;
  min-width: 70px;
  margin-left: 10px;
  font-weight: 500;
}
.task-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.deploy-location-value {
  color: #e65100;
  font-weight: bold;
}
.task-workers-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 180px;
  overflow-y: auto;
  margin-top: 4px;
  padding-right: 5px;
}
.worker-card {
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 8px 10px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: border 0.2s, box-shadow 0.2s, background 0.2s;
}
.worker-card:hover {
  border-color: #FFD600;
}
.worker-card.selected {
  border: 2.5px solid #FFD600;
  background: #fffbe6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.worker-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 10px;
  background: #fff;
  border: 1.5px solid #FFD600;
}
.worker-info {
  text-align: left;
  font-size: 0.95rem;
  color: #444;
}
.amount-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}
.amount-btn {
  width: 32px;
  height: 32px;
  font-size: 1.2rem;
  border-radius: 50%;
}
.amount-btn:disabled {
  background: #f1f1f1;
  color: #aaa;
  cursor: not-allowed;
}
.amount-input {
  width: 48px;
  text-align: center;
  font-size: 1.1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 4px 0;
  outline: none;
  transition: border-color 0.2s;
}
.amount-input:focus {
  border-color: #FFD600;
}
.deploy-btn {
  width: 100%;
  margin-top: 10px;
  padding: 12px;
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease-in-out;
  transform-origin: top;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: scaleY(0.9);
  max-height: 0;
}
.fade-enter-to, .fade-leave-from {
  opacity: 1;
  transform: scaleY(1);
  max-height: 1000px;
}

@media (max-width: 900px) {
  .left-info-panel, .right-task-panel {
    left: 10px;
    right: 10px;
    top: 80px;
    min-width: unset;
    max-width: calc(100vw - 20px);
    gap: 8px;
  }
  .right-task-panel {
    top: auto;
    bottom: 10px;
  }
  .info-card, .task-card {
    padding: 12px;
  }
  .top-right-btn-group {
    right: 10px;
    gap: 10px;
  }
}
</style>