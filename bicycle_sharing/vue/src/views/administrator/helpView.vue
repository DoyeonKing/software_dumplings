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
    };
  },
  mounted() {
    this.initMap();
    // 点击其他地方关闭菜单
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    // 菜单相关方法
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


    initMap() {
      // 1. 初始化地图
      this.map = new window.AMap.Map("mapContainer", {
        center: [114.057868, 22.53445],
        zoom: 17,
        dragEnable: true
      });

      this.infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });
    }
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

/* 地图容器样式 */
#mapContainer {
  width: 100vw;
  height: 100vh;
  position: absolute;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-container {
    top: 15px;
    left: 15px;
  }

  .menu-btn {
    width: 45px;
    height: 45px;
  }

  .menu-icon {
    font-size: 20px;
  }

  .menu-dropdown {
    min-width: 160px;
  }

  .toggle-btn {
    top: 15px;
    right: 15px;
    padding: 8px 18px;
    font-size: 14px;
  }
}
</style>