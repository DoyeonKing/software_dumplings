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
      <button class="toggle-btn" @click="onToggleBikes">
        {{ showBikes ? '隐藏单车' : '显示单车' }}
      </button>

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

        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue'
import { mapMixin } from '@/utils/mapMixin.js'
import AMapLoader from '@/utils/loadAMap.js'
import bicycleIcon from '@/components/icons/bicycle.png';
import { getMapAreaBicycles } from '@/api/map/bicycle';
// 【修改】导入停车区域相关的API函数
import { getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking.js';


export default {
  name: "DashboardView",
  components: { MenuComponent },
  mixins: [mapMixin],
  data() {
    return {
      authToken: '',
      userInfo: null,
      userRole: '',
      menuOpen: false,
      userMenuOpen: false,
      showProfile: false,
      editMode: false,
      form: {
        username: 'admin001',
        realName: '张伟',
        gender: '男',
        education: '硕士研究生',
        organization: '共享单车科技有限公司',
        workArea: '深圳市南山区',
        idNumber: '310101199001011234',
        phone: '138-1234-5678',
        email: 'admin@bikeshare.com',
        birth: '1990-01-01'
      },
      // 【修改】初始化为空数组
      parkingAreas: [],
      parkingPolygons: [],
      bikes: [],
      showBikes: true,
    };
  },
  mounted() {
    this.authToken = sessionStorage.getItem('authToken') || ''
    const storedUserInfo = sessionStorage.getItem('userInfo')

    if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
      try {
        this.userInfo = JSON.parse(storedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败:', e)
        this.userInfo = null
        sessionStorage.removeItem('userInfo')
      }
    }

    this.userRole = sessionStorage.getItem('userRole') || ''

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
             0.4: '#4575b4',   // 深蓝色 - 最低密度
            0.5: '#74add1',   // 浅蓝色
            0.6: '#abd9e9',   // 更浅的蓝色
            0.7: '#ffffbf',   // 淡黄色
            0.8: '#fdae61',   // 橙色
            0.9: '#f46d43',   // 深橙色
            1.0: '#d73027'    // 红色 - 最高密度
          }
        });
        this.heatmapReady = true;
      });

      // 加载初始数据
      this.loadBicycles();
      this.showParkingAreas();

      // 监听地图移动和缩放事件，但使用防抖来减少API调用频率
      let timeout;
      const updateData = () => {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
          this.loadBicycles();
          this.showParkingAreas();
        }, 500); // 500ms的防抖延迟
      };

      this.map.on('moveend', updateData);
      this.map.on('zoomend', updateData);

    }).catch(err => {
      alert('地图加载失败: ' + err.message);
    });
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
    if (this.map) {
      // 【修改】移除所有监听器
      this.map.off('moveend', this.loadBicycles);
      this.map.off('zoomend', this.loadBicycles);
      this.map.off('moveend', this.showParkingAreas);
      this.map.off('zoomend', this.showParkingAreas);
    }
  },
  methods: {
    // 【新增】获取停车区域数据的方法 (参考 UserMapComponent.vue)
    async fetchParkingAreas() {
      try {
        const bounds = this.map.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLon: bounds.getSouthWest().lng,
          maxLon: bounds.getNorthEast().lng
        };
        const response = await getParkingAreasInBounds(params);

        let rawData = null;
        if (response && response.data && Array.isArray(response.data)) {
          rawData = response.data;
        } else if (response && Array.isArray(response)) {
          rawData = response;
        }

        if (rawData) {
          this.parkingAreas = convertParkingAreaData(rawData);
        } else {
          console.warn('停车点数据格式异常:', response);
          this.parkingAreas = [];
        }
      } catch (error) {
        console.error('获取停车点数据失败:', error);
        this.parkingAreas = [];
      }
    },
    // 【新增】显示停车区域的主方法 (参考 UserMapComponent.vue)
    async showParkingAreas() {
      if (!this.map) return;
      try {
        // 清除旧的图层
        if (this.parkingPolygons && this.parkingPolygons.length > 0) {
          this.map.remove(this.parkingPolygons);
          this.parkingPolygons = [];
        }

        // 获取新数据
        await this.fetchParkingAreas();

        // 绘制新图层
        this.drawParkingAreas();

      } catch (error) {
        console.error("显示停车区域失败:", error);
      }
    },
    async loadBicycles() {
      try {
        const bounds = this.map.getBounds();
        const params = {
          minLat: bounds.getSouthWest().lat,
          maxLat: bounds.getNorthEast().lat,
          minLng: bounds.getSouthWest().lng,
          maxLng: bounds.getNorthEast().lng
        };
        const response = await getMapAreaBicycles(params);

        const bikesForMixin = response.data.map(bike => ({
          ...bike,
          lng: bike.currentLon,
          lat: bike.currentLat,
          id: bike.bikeId,
        }));

        this.bikes = bikesForMixin;

        const bikeMarkerIcon = new window.AMap.Icon({
          image: bicycleIcon,
          size: new window.AMap.Size(32, 32),
          imageSize: new window.AMap.Size(32, 32)
        });

        this.addBikeMarkers(this.bikes, bikeMarkerIcon);

        if (!this.showBikes) {
          this.markers.forEach(marker => marker.hide());
        }

      } catch (error) {
        console.error("加载单车数据失败:", error);
      }
    },

    addBikeMarkers(bikeList, bikeIcon) {
      this.markers.forEach(marker => marker.setMap(null));
      this.markers = [];

      bikeList.forEach(bike => {
        const marker = new window.AMap.Marker({
          position: [bike.lng, bike.lat],
          map: this.map,
          icon: bikeIcon,
          title: `单车编号: ${bike.id}`
        });

        marker.on('mouseover', () => {
          this.infoWindow.setContent(`
                    <div style="padding: 8px 12px; font-size: 14px;">
                        <b>单车编号：</b>${bike.id}
                    </div>
                `);
          this.infoWindow.open(this.map, marker.getPosition());
        });
        marker.on('mouseout', () => this.infoWindow.close());

        this.markers.push(marker);
      });
    },

    drawParkingAreas() {
      const infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });

      // 遍历从API获取的新数据进行绘制
      this.parkingAreas.forEach(area => {
        const polygon = new window.AMap.Polygon({
          path: area.polygonPath,
          fillColor: "#FFD600",
          fillOpacity: 0.2,
          strokeColor: "#FFD600",
          strokeWeight: 2,
          zIndex: 40,
          cursor: "pointer"
        });
        this.map.add(polygon);
        this.parkingPolygons.push(polygon);

        polygon.on("mouseover", (e) => {
          infoWindow.setContent(`<div style="min-width:160px;"><b>停车区域：</b>${area.geohash}</div>`);
          infoWindow.open(this.map, e.lnglat);
        });
        polygon.on("mouseout", () => infoWindow.close());
      });
    },
    onToggleBikes() {
      this.showBikes = !this.showBikes;
      if (this.markers && this.markers.length > 0) {
        if (this.showBikes && this.showHeatmap) {
          this.toggleHeatmap(this.bikes);
        } else {
          this.markers.forEach(marker => {
            this.showBikes ? marker.show() : marker.hide();
          });
        }
      }
    },
    onToggleHeatmap() {
      if (!this.showHeatmap) {
        this.showBikes = false;
      }
      this.toggleHeatmap(this.bikes);

      if (!this.showHeatmap && !this.showBikes) {
        this.markers.forEach(marker => marker.hide());
      }
    },
    handleProfileSaved(formData) {
      this.form = { ...this.form, ...formData };
    },
    toggleUserMenu() {
      this.userMenuOpen = !this.userMenuOpen;
    },
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
  }
};
</script>

<style scoped>
/* 样式部分保持不变 */
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

.top-right-controls {
  position: fixed;
  top: 20px;
  right: 30px;
  z-index: 1001;
  display: flex;
  align-items: center;
  gap: 16px;
}

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

.user-dropdown {
  position: absolute;
  top: 54px;
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