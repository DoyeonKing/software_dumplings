<template>
  <div class="tasks-view-root">
    <div id="mapContainer"></div>

    <MenuComponent @profile-saved="handleProfileSaved" />

    <div class="top-right-btn-group btn-group">
      <button class="yellow-btn" @click="listCollapsed = !listCollapsed">
        {{ listCollapsed ? '展开任务列表' : '收起任务列表' }}
      </button>
      <button class="yellow-btn" @click="goHome">
        返回主页
      </button>
    </div>

    <transition name="fade">
      <div v-if="!listCollapsed" class="center-task-list-card">
        <div class="task-list-toolbar">
          <div class="task-search-group">
            <input
                v-model="searchText"
                class="task-search-input"
                placeholder="搜索任务编号/地点/工作人员"
                @keyup.enter="onSearch"
            />
            <button class="yellow-btn search-btn" @click="onSearch">搜索</button>
            <button class="yellow-btn clear-search-btn" @click="clearSearch">置空</button>
          </div>
          <div class="task-status-filters">
            <button
                v-for="s in statusFilters"
                :key="s.value"
                :class="['status-filter-btn', {active: filterStatus === s.value}]"
                @click="filterStatus = s.value"
            >
              {{ s.label }}
            </button>
          </div>
        </div>
        <div class="task-list-title">投放任务查询列表</div>
        <div class="task-list-scroll">
          <div
              v-for="task in filteredTasks"
              :key="task.id"
              class="task-list-item"
          >
            <div class="task-info">
              <div class="task-row"><span class="label">任务编号：</span>{{ task.id }}</div>
              <div class="task-row"><span class="label">地点：</span>{{ task.location }}</div>
              <div class="task-row"><span class="label">工作人员：</span>{{ task.workerName }}</div>
              <div class="task-row"><span class="label">电话：</span>{{ task.workerPhone }}</div>
              <div class="task-row"><span class="label">投放数量：</span>{{ task.deployAmount }}</div>
              <div class="task-row"><span class="label">取车数量：</span>{{ task.pickupAmount }}</div>
            </div>
            <div class="task-action-col">
              <div class="task-status-tag" :class="statusClass(task.status)">
                {{ statusText(task.status) }}
              </div>

              <div class="task-action-btns" v-if="task.status === 'pending'">
                <button
                    class="yellow-btn task-action-btn"
                    @click="remindTask(task)"
                    :disabled="remindDisabled"
                >
                  催促
                </button>
                <button
                    class="yellow-btn task-action-btn"
                    @click="reselectTask(task)"
                >
                  重新选择投放
                </button>
                <button
                    class="yellow-btn task-action-btn delete-btn"
                    @click="deleteTask(task)"
                >
                  删除
                </button>
              </div>
              <div class="task-action-btns" v-else>
                <button class="yellow-btn task-action-btn" disabled>催促</button>
                <button class="yellow-btn task-action-btn" disabled>重新选择投放</button>
                <button class="yellow-btn task-action-btn delete-btn" @click="deleteTask(task)">删除</button>
              </div>

            </div>
          </div>
          <div v-if="filteredTasks.length === 0" class="no-task-tip">暂无符合条件的任务</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue';
import { mapMixin } from '@/utils/mapMixin.js';
import AMapLoader from '@/utils/loadAMap.js';

export default {
  name: "TasksView",
  components: {
    MenuComponent
  },
  mixins: [mapMixin],
  data() {
    return {
      listCollapsed: false,
      remindDisabled: false,
      searchText: "",
      searchKey: "",
      filterStatus: "all",
      statusFilters: [
        { label: "全部", value: "all" },
        { label: "未接收", value: "pending" },
        { label: "正在处理", value: "processing" },
        { label: "已完成", value: "done" }
      ],
      tasks: [
        { id: "T20240601001", location: "深圳市-福田区-福华三路-区域A", workerName: "李明", workerPhone: "13800000001", deployAmount: 10, pickupAmount: 2, status: "pending" },
        { id: "T20240601002", location: "深圳市-福田区-金田路-区域B", workerName: "王芳", workerPhone: "13800000002", deployAmount: 8, pickupAmount: 1, status: "processing" },
        { id: "T20240601003", location: "深圳市-福田区-滨河大道-区域C", workerName: "张伟", workerPhone: "13800000003", deployAmount: 12, pickupAmount: 0, status: "done" },
        { id: "T20240601004", location: "深圳市-福田区-会展中心-区域D", workerName: "赵丽", workerPhone: "13800000004", deployAmount: 5, pickupAmount: 1, status: "pending" },
        { id: "T20240601005", location: "深圳市-南山区-科技园-区域E", workerName: "钱涛", workerPhone: "13800000005", deployAmount: 7, pickupAmount: 0, status: "processing" }
      ],
      parkingAreas: [
        { id: 1, location: "深圳市-福田区-福华三路", areaCode: "区域A", polygon: [ [114.0560, 22.5330], [114.0590, 22.5330], [114.0590, 22.5360], [114.0560, 22.5360] ], currentBikes: 23, availableSpots: 7 },
        { id: 2, location: "深圳市-福田区-金田路", areaCode: "区域B", polygon: [ [114.0595, 22.5330], [114.0625, 22.5330], [114.0625, 22.5360], [114.0595, 22.5360] ], currentBikes: 15, availableSpots: 15 },
        { id: 3, location: "深圳市-福田区-滨河大道", areaCode: "区域C", polygon: [ [114.0560, 22.5365], [114.0590, 22.5365], [114.0590, 22.5395], [114.0560, 22.5395] ], currentBikes: 10, availableSpots: 20 },
        { id: 4, location: "深圳市-福田区-会展中心", areaCode: "区域D", polygon: [ [114.0595, 22.5365], [114.0625, 22.5365], [114.0625, 22.5395], [114.0595, 22.5395] ], currentBikes: 30, availableSpots: 0 }
      ],
      bikeList: [
        { id: "SZ1001", lng: 114.057868, lat: 22.53445, status: "正常", address: "深圳市-福田区-福华三路" },
        { id: "SZ1002", lng: 114.060868, lat: 22.53495, status: "故障", address: "深圳市-福田区-金田路" },
        { id: "SZ1003", lng: 114.058868, lat: 22.53645, status: "待维修", address: "深圳市-福田区-滨河大道" },
        { id: "SZ1004", lng: 114.061868, lat: 22.53445, status: "正常", address: "深圳市-福田区-会展中心" },
        { id: "SZ1005", lng: 114.061867, lat: 22.53545, status: "正常", address: "深圳市-福田区-福华一路" }
      ],
      polygons: []
    };
  },
  computed: {
    filteredTasks() {
      let list = this.filterStatus === "all"
          ? this.tasks
          : this.tasks.filter(t => t.status === this.filterStatus);
      const kw = this.searchKey.trim().toLowerCase();
      if (kw) {
        list = list.filter(t =>
            t.id.toLowerCase().includes(kw) ||
            t.location.toLowerCase().includes(kw) ||
            t.workerName.toLowerCase().includes(kw) ||
            (t.workerPhone && t.workerPhone.includes(kw))
        );
      }
      return list;
    }
  },
  methods: {
    handleProfileSaved(formData) {
      console.log('个人资料已保存:', formData);
      window.alert('个人信息已在控制台输出。');
    },
    goHome() {
      this.$router.push("/admin");
    },
    drawParkingAreas() {
      const infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });
      this.map.remove(this.polygons);
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

        polygon.on("mouseover", (e) => {
          infoWindow.setContent(`
            <div style="min-width:160px;">
              <b>停车区域：</b>${area.location} - ${area.areaCode}<br/>
              <b>现有车辆：</b>${area.currentBikes}<br/>
              <b>可用车位：</b>${area.availableSpots}
            </div>`);
          infoWindow.open(this.map, e.lnglat);
        });
        polygon.on("mouseout", () => infoWindow.close());
        this.polygons.push(polygon);
      });
    },
    statusText(status) {
      const map = { pending: "未接收", processing: "正在处理", done: "已完成" };
      return map[status] || "";
    },
    statusClass(status) {
      return `status-${status}`;
    },
    remindTask() {
      this.remindDisabled = true;
      alert("催促成功");
      setTimeout(() => {
        this.remindDisabled = false;
      }, 1000);
    },
    reselectTask() {
      this.$router.push("/location");
    },
    deleteTask(task) {
      if (window.confirm("确定要删除该任务吗？")) {
        this.tasks = this.tasks.filter(t => t.id !== task.id);
        alert("删除成功");
      }
    },
    onSearch() {
      this.searchKey = this.searchText;
    },
    clearSearch() {
      this.searchText = "";
      this.searchKey = "";
    },
  },
  mounted() {
    // 动态加载高德地图SDK，加载完成后再初始化地图
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      const { yellowBikeIcon } = this.initMap();
      this.map.setZoomAndCenter(15, [114.0588, 22.5368]);
      this.drawParkingAreas();
      this.addBikeMarkers(this.bikeList, yellowBikeIcon);
      this.searchKey = "";
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

/* ...原样保留你的样式... */
.tasks-view-root {
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
  z-index: 200;
  display: flex;
  gap: 14px;
}

/* ...后续样式同你原文件... */
.center-task-list-card {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 1200px;
  height: 700px;
  max-width: 95vw;
  max-height: 90vh;
  background: rgba(255, 251, 236, 0.98);
  border-radius: 22px;
  box-shadow: 0 4px 32px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  align-items: stretch;
  z-index: 150;
  padding: 24px 32px;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 214, 0, 0.5);
}
/* ...其余样式不变... */
.task-list-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 16px;
  text-align: center;
}
.task-list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.task-search-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-grow: 1;
}
.task-search-input {
  flex-grow: 1;
  min-width: 180px;
  height: 38px;
  border: 1.5px solid #FFD600;
  border-radius: 18px;
  padding: 0 18px;
  font-size: 1rem;
  outline: none;
  background: #fff;
}
.search-btn, .clear-search-btn {
  height: 38px;
  padding: 0 20px;
  font-size: 1rem;
  border-radius: 18px;
}
.task-status-filters {
  display: flex;
  gap: 10px;
}
.status-filter-btn {
  background: #fff;
  color: #555;
  border: 1.5px solid #ddd;
  border-radius: 16px;
  padding: 7px 20px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.status-filter-btn.active,
.status-filter-btn:hover {
  background: #ffebee;
  color: #c62828;
  border-color: #FFD600;
}
.task-list-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-right: 4px;
}
.task-list-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  padding: 18px 20px;
  gap: 18px;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.task-list-item:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border-color: #FFD600;
}
.task-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 1rem;
  color: #444;
  flex-grow: 1;
}
.task-row .label {
  color: #888;
  font-weight: 500;
}
.task-action-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  min-width: 130px;
  text-align: right;
}
.task-status-tag {
  width: 100px;
  text-align: center;
  font-size: 0.95rem;
  font-weight: 600;
  border-radius: 16px;
  padding: 6px 0;
}
.status-pending { background: #ffebee; color: #c62828; }
.status-processing { background: #e3f2fd; color: #1565c0; }
.status-done { background: #e8f5e9; color: #2e7d32; }
.task-action-btns {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  width: 100%;
}
.task-action-btn {
  width: 100%;
  padding: 6px 0;
  font-size: 0.95rem;
}
.task-action-btn[disabled] {
  background: #f5f5f5 !important;
  color: #bdbdbd !important;
  cursor: not-allowed !important;
  border-color: #eee !important;
}
.delete-btn {
  background: #ff4d4f !important;
  color: #fff !important;
  border: none !important;
}
.delete-btn:hover:not([disabled]) {
  background: #d32f2f !important;
}
.no-task-tip {
  text-align: center;
  color: #aaa;
  font-size: 1.1rem;
  padding: 40px 0;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -48%) scale(0.95);
}
.fade-enter-to, .fade-leave-from {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
}

@media (max-width: 1200px) {
  .center-task-list-card {
    padding: 20px;
  }
}

@media (max-width: 900px) {
  .center-task-list-card {
    width: 100%;
    height: 100%;
    max-width: 100%;
    max-height: 100%;
    top: 0;
    left: 0;
    transform: none;
    border-radius: 0;
    padding: 16px;
    padding-top: 80px;
  }
  .task-list-item {
    flex-direction: column;
    align-items: stretch;
  }
  .task-action-col {
    align-items: flex-start;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: space-between;
  }
  .task-action-btns {
    flex-direction: row;
    justify-content: flex-end;
    flex-grow: 1;
  }
  .task-action-btn {
    width: auto;
    padding: 6px 12px;
  }
  .task-list-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>