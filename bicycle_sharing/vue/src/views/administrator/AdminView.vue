<template>
  <div class="dashboard-view-root">
    <div id="mapContainer"></div>

    <MenuComponent @profile-saved="handleProfileSaved" />

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
            <div class="info-row"><div class="info-label">姓名</div><input class="info-input" v-model="form.realName" :disabled="!editMode" /></div>
            <div class="info-row"><div class="info-label">出生年月</div><input class="info-input" type="date" v-model="form.birth" :disabled="!editMode" style="min-width: 0;" /></div>
            <div class="info-row"><div class="info-label">性别</div><select class="info-input" v-model="form.gender" :disabled="!editMode"><option value="男">男</option><option value="女">女</option></select></div>
            <div class="info-row"><div class="info-label">学历</div><input class="info-input" v-model="form.education" :disabled="!editMode" /></div>
            <div class="info-row"><div class="info-label">所在单位</div><input class="info-input" v-model="form.organization" :disabled="!editMode" /></div>
            <div class="info-row"><div class="info-label">工作地区</div><input class="info-input" v-model="form.workArea" :disabled="!editMode" /></div>
            <div class="info-row"><div class="info-label">身份证号码</div><input class="info-input" v-model="form.idNumber" :disabled="!editMode" /></div>
            <div class="info-row"><div class="info-label">联系电话</div><input class="info-input" v-model="form.phone" :disabled="!editMode" /></div>
            <div class="info-row"><div class="info-label">办公邮箱</div><input class="info-input" v-model="form.email" :disabled="!editMode" /></div>
          </form>
          <div class="button-row">
            <button class="action-btn" v-if="!editMode" @click="editMode = true" type="button">修改信息</button>
            <button class="action-btn" v-if="editMode" @click="saveInfo" type="button">保存信息</button>
            <button class="action-btn" @click="closeProfileModal" type="button">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <div class="top-right-controls">
      <button class="toggle-btn" @click="onToggleHeatmap">
        {{ showHeatmap ? '显示普通地图' : '显示热力图' }}
      </button>

      <div class="user-menu-container">
        <img
            src="https://api.dicebear.com/7.x/miniavs/svg?seed=3"
            alt="用户头像"
            class="user-avatar-btn"
            @click="toggleUserMenu"
        />
        <div class="user-dropdown" :class="{ 'menu-open': userMenuOpen }">
          <router-link to="/login" class="user-menu-item">切换账号</router-link>
          <router-link to="/new" class="user-menu-item">原有切换new功能</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue'
import { mapMixin } from '@/utils/mapMixin.js'
import AMapLoader from '@/utils/loadAMap.js'

export default {
  name: "DashboardView",
  components: { MenuComponent },
  mixins: [mapMixin],
  data() {
    return {
      menuOpen: false,
      userMenuOpen: false, // 新增：控制用户菜单的开关
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
      parkingAreas: [
        { id: 1, location: "深圳市-福田区-福华三路", areaCode: "区域A", polygon: [ [114.0560, 22.5330], [114.0590, 22.5330], [114.0590, 22.5360], [114.0560, 22.5360] ] },
        { id: 2, location: "深圳市-福田区-金田路", areaCode: "区域B", polygon: [ [114.0595, 22.5330], [114.0625, 22.5330], [114.0625, 22.5360], [114.0595, 22.5360] ] },
      ],
      bikeList: [
        { id: "SZ1001", lng: 114.057868, lat: 22.53445, status: "正常", address: "深圳市-福田区-福华三路" },
        { id: "SZ1002", lng: 114.060868, lat: 22.53495, status: "故障", address: "深圳市-福田区-金田路" },
      ]
    };
  },
  mounted() {
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      const { yellowBikeIcon } = this.initMap();
      this.map.setZoomAndCenter(15, [114.0588, 22.5368]);
      this.addBikeMarkers(this.bikeList, yellowBikeIcon);
      this.drawParkingAreas();
    }).catch(err => {
      alert('地图加载失败: ' + err.message);
    });
    // 增加对两个菜单的外部点击监听
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeUnmount() {
    // 移除监听
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    drawParkingAreas() {
      const infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });
      this.parkingAreas.forEach(area => {
        const polygon = new window.AMap.Polygon({
          path: area.polygon,
          fillColor: "#FFD600",
          fillOpacity: 0.2,
          strokeColor: "#FFD600",
          strokeWeight: 2,
          zIndex: 40,
          cursor: "pointer"
        });
        this.map.add(polygon);
        polygon.on("mouseover", (e) => {
          infoWindow.setContent(`<div style="min-width:160px;"><b>停车区域：</b>${area.location}-${area.areaCode}</div>`);
          infoWindow.open(this.map, e.lnglat);
        });
        polygon.on("mouseout", () => infoWindow.close());
      });
    },
    handleProfileSaved(formData) {
      this.form = { ...this.form, ...formData };
    },
    // 新增：切换用户菜单
    toggleUserMenu() {
      this.userMenuOpen = !this.userMenuOpen;
    },
    // 更新：处理外部点击，同时关闭两个菜单
    handleClickOutside(event) {
      const menuContainer = event.target.closest('.menu-container');
      const userMenuContainer = event.target.closest('.user-menu-container');
      if (!menuContainer) {
        this.menuOpen = false;
      }
      if (!userMenuContainer) {
        this.userMenuOpen = false;
      }
    },
    showProfileModal() {
      this.showProfile = true;
      this.menuOpen = false;
    },
    closeProfileModal() {
      this.showProfile = false;
      this.editMode = false;
    },
    saveInfo() {
      this.editMode = false;
      window.alert('信息已保存！');
    },
    onToggleHeatmap() {
      this.toggleHeatmap(this.bikeList);
    }
  }
};
</script>

<style scoped>
/* 你的样式有所调整，以适应新控件 */
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
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

/* 右上角控件容器 */
.top-right-controls {
  position: fixed;
  top: 20px;
  right: 30px;
  z-index: 1001;
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 热力图切换按钮 */
.toggle-btn {
  background: #ffd600;
  color: #222;
  border: none;
  border-radius: 20px;
  padding: 10px 22px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transition: background 0.2s;
}

.toggle-btn:hover {
  background: #ffe066;
}

/* 新增：用户菜单容器 */
.user-menu-container {
  position: relative;
}

.user-avatar-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  cursor: pointer;
  transition: transform 0.2s;
}
.user-avatar-btn:hover {
  transform: scale(1.1);
}

/* 新增：用户下拉菜单 */
.user-dropdown {
  position: absolute;
  top: 54px; /* 在头像下方 */
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  min-width: 120px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  overflow: hidden;
}

.user-dropdown.menu-open {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.user-menu-item {
  display: block;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  transition: background 0.2s;
  font-size: 14px;
  text-align: center;
}

.user-menu-item:hover {
  background: #f5f5f5;
}


/* 个人资料浮窗 (样式保持不变) */
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
  line-height: 30px;
  text-align: center;
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
}
.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 28px;
  border: 2px solid #FFD600;
}
.profile-avatar img {
  width: 100%;
  height: 100%;
}
.profile-name {
  font-size: 1.6rem;
  font-weight: 700;
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
  width: 100%;
}
.info-label {
  width: 120px;
  color: #888;
  font-size: 1.08rem;
  font-weight: 600;
  text-align: right;
  flex-shrink: 0;
  margin-right: 12px;
}
.info-input {
  flex: 1;
  font-size: 1.08rem;
  border: none;
  background: #f7f7f7;
  border-radius: 6px;
  padding: 8px 12px;
  transition: all 0.2s;
}
.info-input:disabled {
  background: #f7f7f7;
  color: #888;
  cursor: not-allowed;
}
.info-input:not(:disabled) {
  border: 1.5px solid #ddd;
}
.info-input:focus {
  border: 1.5px solid #FFD600;
  background: #fffbe6;
  outline: none;
}
.button-row {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 32px;
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
  transition: all 0.2s;
}
.action-btn:hover {
  background: #FFD600;
  color: #222;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .top-right-controls {
    top: 15px;
    right: 15px;
  }
  .toggle-btn {
    padding: 8px 18px;
    font-size: 14px;
  }
  .user-avatar-btn {
    width: 40px;
    height: 40px;
  }
  .profile-card {
    width: 95vw;
    padding: 20px;
  }
}
</style>