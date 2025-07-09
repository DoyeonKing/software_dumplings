<template>
  <div class="dashboard-view-root">
    <div id="mapContainer"></div>

    <!-- 菜单按钮 -->
    <div class="menu-container">
      <button class="menu-btn" @click="toggleMenu">
        <span class="menu-icon">☰</span>
      </button>

      <!-- 弹出菜单 -->
      <div class="menu-dropdown" :class="{ 'menu-open': menuOpen }">
        <div class="menu-item" @click="showProfileModal">
          <span class="menu-item-text">个人资料</span>
        </div>

        <router-link to="/dashboard" class="menu-item" @click="closeMenu">
          <span class="menu-item-text">指标看板</span>
        </router-link>

        <router-link to="/location" class="menu-item" @click="closeMenu">
          <span class="menu-item-text">车辆定位</span>
        </router-link>

        <router-link to="/tasks" class="menu-item" @click="closeMenu">
          <span class="menu-item-text">任务查询</span>
        </router-link>

        <router-link to="/help" class="menu-item" @click="closeMenu">
          <span class="menu-item-text">帮助指南</span>
        </router-link>
      </div>
    </div>

    <!-- 个人资料浮窗 -->
    <div v-if="showProfile" class="profile-modal-overlay" @click="closeProfileModal">
      <div class="profile-modal" @click.stop>
        <div class="profile-card">
          <div class="profile-header">
            <div class="profile-avatar">
              <img src="https://api.dicebear.com/7.x/miniavs/svg?seed=admin" alt="管理员头像" />
            </div>
            <div>
              <div class="profile-name">{{ form.realName }}</div>
              <div class="profile-username">用户名：{{ form.username }}</div>
            </div>
            <button class="close-btn" @click="closeProfileModal">×</button>
          </div>

          <form class="profile-info" @submit.prevent>
            <div class="info-row">
              <div class="info-label">姓名</div>
              <input class="info-input" v-model="form.realName" :disabled="!editMode" />
            </div>
            <div class="info-row">
              <div class="info-label">出生年月</div>
              <input
                  class="info-input"
                  type="date"
                  v-model="form.birth"
                  :disabled="!editMode"
                  style="min-width: 0;"
              />
            </div>
            <div class="info-row">
              <div class="info-label">性别</div>
              <select class="info-input" v-model="form.gender" :disabled="!editMode">
                <option value="男">男</option>
                <option value="女">女</option>
              </select>
            </div>
            <div class="info-row">
              <div class="info-label">学历</div>
              <input class="info-input" v-model="form.education" :disabled="!editMode" />
            </div>
            <div class="info-row">
              <div class="info-label">所在单位</div>
              <input class="info-input" v-model="form.organization" :disabled="!editMode" />
            </div>
            <div class="info-row">
              <div class="info-label">工作地区</div>
              <input class="info-input" v-model="form.workArea" :disabled="!editMode" />
            </div>
            <div class="info-row">
              <div class="info-label">身份证号码</div>
              <input class="info-input" v-model="form.idNumber" :disabled="!editMode" />
            </div>
            <div class="info-row">
              <div class="info-label">联系电话</div>
              <input class="info-input" v-model="form.phone" :disabled="!editMode" />
            </div>
            <div class="info-row">
              <div class="info-label">办公邮箱</div>
              <input class="info-input" v-model="form.email" :disabled="!editMode" />
            </div>
          </form>

          <div class="button-row">
            <button
                class="action-btn"
                v-if="!editMode"
                @click="editMode = true"
                type="button"
            >修改信息</button>
            <button
                class="action-btn"
                v-if="editMode"
                @click="saveInfo"
                type="button"
            >保存信息</button>
            <button
                class="action-btn"
                @click="closeProfileModal"
                type="button"
            >关闭</button>
          </div>
        </div>
      </div>
    </div>


    <button class="yellow-btn toggle-list-btn" @click="listCollapsed = !listCollapsed">
      {{ listCollapsed ? '展开任务列表' : '收起任务列表' }}
    </button>

    <button class="yellow-btn back-home-btn" @click="goHome">
      返回主页
    </button>

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
export default {
  name: "DashboardView",
  data() {
    return {
      // 菜单相关
      menuOpen: false,
      showProfile: false,
      editMode: false,
      form: {
        username: 'admin001',
        realName: '张伟',
        gender: '男',
        education: '硕士研究生',
        organization: '共享单车科技有限公司',
        workArea: '上海市浦东新区',
        idNumber: '310101199001011234',
        phone: '138-1234-5678',
        email: 'admin@bikeshare.com',
        birth: '1990-01-01'
      },
      listCollapsed: false,
      remindDisabled: false,
      searchText: "",
      searchKey: "", // This holds the actual search term used for filtering
      filterStatus: "all",
      statusFilters: [
        { label: "全部", value: "all" },
        { label: "未接收", value: "pending" },
        { label: "正在处理", value: "processing" },
        { label: "已完成", value: "done" }
      ],
      // 任务列表数据（模拟）
      tasks: [
        {
          id: "T20240601001",
          location: "深圳市-福田区-福华三路-区域A",
          workerName: "李明",
          workerPhone: "13800000001",
          deployAmount: 10,
          pickupAmount: 2,
          status: "pending"
        },
        {
          id: "T20240601002",
          location: "深圳市-福田区-金田路-区域B",
          workerName: "王芳",
          workerPhone: "13800000002",
          deployAmount: 8,
          pickupAmount: 1,
          status: "processing"
        },
        {
          id: "T20240601003",
          location: "深圳市-福田区-滨河大道-区域C",
          workerName: "张伟",
          workerPhone: "13800000003",
          deployAmount: 12,
          pickupAmount: 0,
          status: "done"
        },
        {
          id: "T20240601004",
          location: "深圳市-福田区-会展中心-区域D",
          workerName: "赵丽",
          workerPhone: "13800000004",
          deployAmount: 5,
          pickupAmount: 1,
          status: "pending"
        },
        {
          id: "T20240601005",
          location: "深圳市-南山区-科技园-区域E",
          workerName: "钱涛",
          workerPhone: "13800000005",
          deployAmount: 7,
          pickupAmount: 0,
          status: "processing"
        }
      ],
      // **停车区域数据**
      parkingAreas: [
        {
          id: 1,
          location: "深圳市-福田区-福华三路",
          areaCode: "区域A",
          polygon: [
            // Larger polygon coordinates for Area A
            [114.0560, 22.5330],
            [114.0590, 22.5330],
            [114.0590, 22.5360],
            [114.0560, 22.5360]
          ],
          currentBikes: 23,
          availableSpots: 7
        },
        {
          id: 2,
          location: "深圳市-福田区-金田路",
          areaCode: "区域B",
          polygon: [
            // Larger polygon coordinates for Area B (shifted east)
            [114.0595, 22.5330],
            [114.0625, 22.5330],
            [114.0625, 22.5360],
            [114.0595, 22.5360]
          ],
          currentBikes: 15,
          availableSpots: 15
        },
        {
          id: 3,
          location: "深圳市-福田区-滨河大道",
          areaCode: "区域C",
          polygon: [
            // Larger polygon coordinates for Area C (shifted north)
            [114.0560, 22.5365],
            [114.0590, 22.5365],
            [114.0590, 22.5395],
            [114.0560, 22.5395]
          ],
          currentBikes: 10,
          availableSpots: 20
        },
        {
          id: 4,
          location: "深圳市-福田区-会展中心",
          areaCode: "区域D",
          polygon: [
            // Larger polygon coordinates for Area D (shifted east-north)
            [114.0595, 22.5365],
            [114.0625, 22.5365],
            [114.0625, 22.5395],
            [114.0595, 22.5395]
          ],
          currentBikes: 30,
          availableSpots: 0
        }
      ],
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
      map: null,
      infoWindow: null,
      markers: [],
      polygons: []
    };
  },
  computed: {
    filteredTasks() {
      // 状态筛选
      let list = this.filterStatus === "all"
          ? this.tasks
          : this.tasks.filter(t => t.status === this.filterStatus);
      // 搜索过滤
      const kw = this.searchKey.trim().toLowerCase(); // Convert to lowercase for case-insensitive search
      if (kw) {
        list = list.filter(t =>
            t.id.toLowerCase().includes(kw) ||
            t.location.toLowerCase().includes(kw) ||
            t.workerName.toLowerCase().includes(kw) ||
            (t.workerPhone && t.workerPhone.includes(kw)) // Check if workerPhone exists before including
        );
      }
      return list;
    }
  },
  methods: {
    toggleMenu() {
      this.menuOpen = !this.menuOpen;
    },
    closeMenu() {
      this.menuOpen = false;
    },
    handleClickOutside(event) {
      const menuContainer = event.target.closest('.menu-container');
      if (!menuContainer) {
        this.menuOpen = false;
      }
    },
    showProfileModal() {
      this.showProfile = true;
      this.closeMenu();
    },
    closeProfileModal() {
      this.showProfile = false;
      this.editMode = false;
    },
    saveInfo() {
      this.editMode = false;
      window.alert('信息已保存！');
    },

    statusText(status) {
      if (status === "pending") return "未接收";
      if (status === "processing") return "正在处理";
      if (status === "done") return "已完成";
      return "";
    },
    statusClass(status) {
      if (status === "pending") return "status-pending";
      if (status === "processing") return "status-processing";
      if (status === "done") return "status-done";
      return "";
    },
    remindTask(task) {
      this.remindDisabled = true;
      this.$nextTick(() => {
        this.$message
            ? this.$message.success("催促成功")
            : alert("催促成功");
        setTimeout(() => {
          this.remindDisabled = false;
        }, 1000);
      });
    },
    reselectTask(task) {
      this.$router
          ? this.$router.push("/location")
          : (window.location.href = "/location");
    },
    deleteTask(task) {
      if (this.$confirm) {
        this.$confirm("确定要删除该任务吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          this.tasks = this.tasks.filter(t => t.id !== task.id);
          this.$message
              ? this.$message.success("删除成功")
              : alert("删除成功");
        }).catch(() => {
          // User canceled, do nothing
          if (this.$message) {
            this.$message.info('已取消删除');
          }
        });
      } else {
        if (window.confirm("确定要删除该任务吗？")) {
          this.tasks = this.tasks.filter(t => t.id !== task.id);
          alert("删除成功");
        }
      }
    },
    goHome() {
      this.$router
          ? this.$router.push("/admin")
          : (window.location.href = "/admin");
    },
    onSearch() {
      this.searchKey = this.searchText;
    },
    clearSearch() { // New method to clear search
      this.searchText = "";
      this.searchKey = "";
    },
    initMap() {
      this.map = new window.AMap.Map("mapContainer", {
        center: [114.0588, 22.5368], // Adjusted center to better view all 4 areas
        zoom: 15, // Adjusted zoom level to view all 4 areas better
        dragEnable: true
      });

      this.infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });

      // **停车区域代码开始**
      // 清除旧的多边形，以防initMap多次调用
      if (this.polygons && this.polygons.length) {
        this.polygons.forEach(p => p.setMap(null));
      }
      this.polygons = [];

      // 绘制停车区域 (循环 parkingAreas 数组)
      this.parkingAreas.forEach(area => {
        const polygon = new window.AMap.Polygon({
          path: area.polygon.map(([lng, lat]) => [lng, lat]),
          fillColor: "#FFD600", // 浅黄色
          fillOpacity: 0.3,
          strokeColor: "#FFD600",
          strokeWeight: 2,
          zIndex: 50,
          cursor: "pointer"
        });
        polygon.setMap(this.map);

        // 鼠标移入显示信息浮窗
        polygon.on("mouseover", (e) => {
          this.infoWindow.setContent(`
            <div style="min-width:160px;">
              <b>停车区域：</b>${area.location} - ${area.areaCode}<br/>
              <b>现有车辆：</b>${area.currentBikes}<br/>
              <b>可用车位：</b>${area.availableSpots}
            </div>`);
          this.infoWindow.open(this.map, e.lnglat);
        });
        // 鼠标移出关闭信息浮窗
        polygon.on("mouseout", () => this.infoWindow.close());
        this.polygons.push(polygon);
      });
      // **停车区域代码结束**


      // SVG: 黄色圆底+黑色自行车图案 (来自AdminView.vue)
      const bikeSvg = `
        <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="16" cy="16" r="15" fill="#FFD600" stroke="#FFD600" stroke-width="2"/>
          <g stroke="#222" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none">
            <circle cx="10" cy="22" r="4"/>
            <circle cx="22" cy="22" r="4"/>
            <path d="M10 22 L14 14 L18 14 L22 22"/>
            <path d="M14 14 L16 10 L18 14"/>
            <circle cx="16" cy="10" r="1"/>
          </g>
        </svg>
      `;

      // 使用新的自行车图标
      const yellowBikeIcon = new window.AMap.Icon({
        image: 'data:image/svg+xml;base64,' + btoa(bikeSvg),
        size: new window.AMap.Size(32, 32), // 图标尺寸与SVG一致
        imageSize: new window.AMap.Size(32, 32) // 图片尺寸与SVG一致
      });

      // 清除旧的车辆标记
      this.markers.forEach(marker => marker.setMap(null));
      this.markers = [];

      this.bikeList.forEach(bike => {
        const marker = new window.AMap.Marker({
          position: [bike.lng, bike.lat],
          map: this.map,
          icon: yellowBikeIcon, // 使用带有自行车图案的图标
          title: `单车编号: ${bike.id}`
        });

        marker.on('mouseover', () => {
          this.infoWindow.setContent(`
            <div style="min-width:160px;">
              <b>单车编号：</b>${bike.id}<br/>
              <b>状态：</b>${bike.status}<br/>
              <b>位置：</b>${bike.address}
            </div>`);
          this.infoWindow.open(this.map, marker.getPosition());
        });
        marker.on('mouseout', () => this.infoWindow.close());

        this.markers.push(marker);
      });
    }
  },
  mounted() {
    this.initMap();
    this.searchKey = ""; // Ensure search key is empty on mount
  }
};
</script>

<style scoped>
html, body, #app, .dashboard-view-root {
  height: 100%;
  margin: 0;
  padding: 0;
}
.dashboard-view-root {
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


/* 菜单容器 */
.menu-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
}

/* 菜单按钮 */
.menu-btn {
  width: 50px;
  height: 50px;
  background: white;
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.menu-btn:hover {
  background: #FFD600;
  transform: scale(1.05);
}

.menu-icon {
  font-size: 24px;
  color: #333;
}

.menu-btn:hover .menu-icon {
  color: #333;
}

/* 下拉菜单 */
.menu-dropdown {
  position: absolute;
  top: 60px;
  left: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  min-width: 180px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  overflow: hidden;
}

.menu-dropdown.menu-open {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

/* 菜单项 */
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #FFD600;
  color: #333;
}

.menu-item-text {
  font-size: 14px;
  font-weight: 500;
}

/* 个人资料浮窗 */
.profile-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(2px);
}

.profile-modal {
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.profile-card {
  width: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  padding: 36px 36px 28px 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  backdrop-filter: blur(2px);
  position: relative;
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 30px;
  height: 30px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;
}

.close-btn:hover {
  background: #ff3742;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  width: 100%;
  justify-content: center;
  position: relative;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 28px;
  border: 2px solid #FFD600;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-name {
  font-size: 1.6rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 10px;
}

.profile-username {
  font-size: 1.1rem;
  color: #888;
}

.profile-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 0;
  width: 100%;
  justify-content: flex-start;
}

.info-label {
  width: 120px;
  color: #888;
  font-size: 1.08rem;
  font-weight: 600;
  margin-right: 12px;
  text-align: right;
}

.info-input {
  flex: 1;
  color: #222;
  font-size: 1.08rem;
  font-weight: 600;
  text-align: left;
  word-break: break-all;
  border: none;
  background: #f7f7f7;
  border-radius: 6px;
  padding: 8px 12px;
  transition: border 0.2s, background 0.2s;
  outline: none;
}

.info-input:disabled {
  background: #f7f7f7;
  color: #888;
  cursor: not-allowed;
}

.info-input:focus {
  border: 1.5px solid #FFD600;
  background: #fffbe6;
}

.button-row {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 32px;
  width: 100%;
}

.action-btn {
  padding: 10px 28px;
  font-size: 1.08rem;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  background: #222;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.action-btn:hover {
  background: #FFD600;
  color: #222;
}

/* 顶部工具栏 */
.task-list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  width: 100%;
  flex-wrap: wrap; /* Allow wrapping on smaller screens */
  gap: 10px; /* Space between groups */
}
.task-search-group {
  display: flex;
  align-items: center;
  gap: 8px; /* Space between input and buttons */
  flex-grow: 1; /* Allows search group to take available space */
}
.task-search-input {
  flex-grow: 1; /* Input takes up remaining space in search-group */
  min-width: 180px; /* Ensure input doesn't get too small */
  height: 38px;
  border: 1.5px solid #FFD600;
  border-radius: 18px;
  padding: 0 18px;
  font-size: 1rem;
  outline: none;
  transition: border 0.2s;
  background: #fffbe6;
}
.task-search-input:focus {
  border-color: #ffb800;
  background: #fffde7;
}
.search-btn, .clear-search-btn { /* Apply styles to both search and clear buttons */
  height: 38px;
  padding: 0 22px;
  font-size: 1rem;
  border-radius: 18px;
  font-weight: 600;
  white-space: nowrap; /* Prevent text wrap */
}
.task-status-filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap; /* Allow filters to wrap */
}
.status-filter-btn {
  background: #fffbe6;
  color: #bfa100;
  border: 1.5px solid #FFD600;
  border-radius: 16px;
  padding: 7px 22px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border 0.2s;
  outline: none;
  white-space: nowrap; /* Prevent text wrapping inside buttons */
}
.status-filter-btn.active,
.status-filter-btn:hover {
  background: #FFCEDD;
  color: #FF5C4D;
  border-color: #FFD600;
}

/* 左上角展开/收起按钮 */
.toggle-list-btn {
  position: fixed;
  top: 16px;
  right: 170px;
  z-index: 200;
}

/* 右上角返回主页按钮 */
.back-home-btn {
  position: fixed;
  top: 16px;
  right: 36px;
  z-index: 200;
  background: #FFD600;
  color: #222;
  border: none;
  border-radius: 20px;
  padding: 10px 22px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transition: background 0.2s, color 0.2s;
  outline: none;
  display: flex;
  align-items: center;
  justify-content: center;
}
.back-home-btn:hover {
  background: #ffe066;
  color: #111;
}

/* 居中任务列表信息框 16:9 比例 */
.center-task-list-card {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 1200px;
  height: 700px;
  max-width: 98vw;
  max-height: 92vh;
  background: #FFFBEC; /* 浅黄色 */
  border-radius: 22px;
  box-shadow: 0 4px 32px rgba(0,0,0,0.13);
  display: flex;
  flex-direction: column;
  align-items: stretch;
  z-index: 150;
  padding: 38px 44px 32px 44px;
}
.task-list-title {
  font-size: 1.45rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 18px;
  text-align: center;
  letter-spacing: 2px;
}
.task-list-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-right: 4px;
}
.task-list-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  background: #FFFFFF; /* 纯白色 */
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 18px 20px;
  gap: 18px;
  border: 1.5px solid #b6eab6;
  transition: box-shadow 0.2s, border 0.2s;
}
.task-list-item:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border-color: #7be07b;
}
.task-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 1.08rem;
  color: #444;
  min-width: 320px;
}
.task-row .label {
  color: #888;
  font-weight: 500;
  margin-right: 2px;
}
.task-action-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  min-width: 120px;
}
.task-status-tag {
  width: 102px;
  text-align: center;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 16px;
  padding: 6px 0;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  letter-spacing: 1px;
}
.status-pending {
  background: #FFD0D6;
  color: #F0315E;
}
.status-processing {
  background: #95E1FF;
  color: #3968F4;
}
.status-done {
  background: #d2f8d2;
  color: #1ca01c;
}
.task-action-btns {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.task-action-btn {
  width: 102px;
  padding: 6px 0;
  font-size: 1rem;
  margin: 0;
  border-radius: 16px;
}
.task-action-btn[disabled] {
  background: #f3f3f3 !important;
  color: #bbb !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
}
.delete-btn {
  background: #ff4d4f !important;
  color: #fff !important;
  font-weight: 600;
  margin-top: 2px;
  border: none;
  transition: background 0.2s;
}
.delete-btn:hover:not([disabled]) {
  background: #ff7875 !important;
  color: #fff !important;
}

/* 无任务提示 */
.no-task-tip {
  text-align: center;
  color: #aaa;
  font-size: 1.1rem;
  margin-top: 40px;
}

/* 黄色按钮统一样式 */
.yellow-btn {
  background: #FFD600;
  color: #222;
  border: none;
  border-radius: 20px;
  padding: 10px 22px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transition: background 0.2s, color 0.2s;
  outline: none;
  display: flex;
  align-items: center;
  justify-content: center;
}
.yellow-btn:hover {
  background: #ffe066;
  color: #111;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .center-task-list-card {
    width: 98vw;
    height: 92vh;
    padding: 18px 2vw 18px 2vw;
  }
  .task-info {
    min-width: 0;
  }
}
@media (max-width: 900px) {
  .center-task-list-card {
    width: 99vw;
    height: auto;
    min-width: 0;
    padding: 10px 1vw 10px 1vw;
  }
  .task-list-title {
    font-size: 1.1rem;
    margin-bottom: 10px;
  }
  .task-list-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 10px 8px;
  }
  .toggle-list-btn {
    left: 10px;
    top: 10px;
    padding: 8px 12px;
    font-size: 14px;
  }
  .back-home-btn {
    right: 10px;
    top: 10px;
    padding: 8px 12px;
    font-size: 14px;
  }
  .task-action-col {
    align-items: stretch;
    min-width: 0;
    width: 100%;
  }
  .task-action-btns {
    width: 100%;
  }
  .task-action-btn {
    width: 100%;
    font-size: 14px;
    padding: 8px 0;
  }
  .yellow-btn {
    font-size: 14px;
    padding: 8px 12px;
  }
  .task-search-input {
    width: 100%;
    min-width: 0;
    margin-right: 0;
    margin-bottom: 8px;
  }
  .task-list-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
}
</style>