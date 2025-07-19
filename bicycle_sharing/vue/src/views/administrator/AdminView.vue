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
      </div>

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
import MenuComponent from '@/components/admin/MenuComponent.vue'
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
      // 【修改】初始化为空数组，默认隐藏单车和停车区域
      parkingAreas: [],
      parkingPolygons: [],
      bikes: [],
      showBikes: false, // 默认隐藏单车
      showParkingAreas: false, // 默认隐藏停车区域
      // 性能优化相关
      updateTimeout: null,
      isUpdating: false,
      lastUpdateTime:0,
      updateThrottle:1000, //1
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
      // 初始化地图 - 优化配置
      this.map = new window.AMap.Map("mapContainer", {
        center: [114.0610, 22.5395],
        zoom: 17,
        dragEnable: true,
        zoomEnable: true,
        doubleClickZoom: true,
        keyboardEnable: true,
        scrollWheel: true,
        touchZoom: true,
        mapStyle: 'amap://styles/normal',
        // 性能优化配置
        renderMode: '2D', // 使用2D渲染模式
        pitch: 0, // 禁用3D倾斜
        viewMode: '2D', // 强制2D视图
        expandZoomRange: false, // 禁用扩展缩放范围
        jogEnable: false, // 禁用缓动效果
        animateEnable: false, // 禁用动画效果
        resizeEnable: true,
        showIndoorMap: false, // 禁用室内地图
        showBuildingBlock: false, // 禁用建筑物
        showLabel: true, // 保留标签显示
      });

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

      // 【优化】延迟加载初始数据，避免页面加载时卡顿
      setTimeout(() => {
        this.loadBicycles();
        this.loadParkingAreas();
      }, 1000);

      // 【优化】使用节流的地图事件监听
      this.setupMapEventListeners();

    }).catch(err => {
      alert('地图加载失败: ' + err.message);
    });
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
    
    // 清理定时器
    if (this.updateTimeout) {
      clearTimeout(this.updateTimeout);
    }
    
    if (this.map) {
      // 清理地图事件监听器
      this.map.off('moveend', this.loadBicycles);
      this.map.off('zoomend', this.loadBicycles);
      this.map.off('moveend', this.loadParkingAreas);
      this.map.off('zoomend', this.loadParkingAreas);
      
      // 清理标记
      if (this.markers && this.markers.length > 0) {
        this.markers.forEach(marker => marker.setMap(null));
        this.markers = [];
      }
      
      // 清理多边形
      if (this.parkingPolygons && this.parkingPolygons.length > 0) {
        this.parkingPolygons.forEach(polygon => polygon.setMap(null));
        this.parkingPolygons = [];
      }
      
      // 清理热力图
      if (this.heatmap) {
        this.heatmap.setMap(null);
        this.heatmap = null;
      }
      
      // 销毁地图
      this.map.destroy();
      this.map = null;
    }
  },
  methods: {
    // 【新增】获取停车区域数据的方法 (参考 UserMapComponent.vue)
    async fetchParkingAreas() {
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
    async loadParkingAreas() {
      if (!this.map || !this.showParkingAreas) return; // 如果隐藏停车区域则不加载
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
      if (!this.map || !this.showBikes) return; // 如果隐藏单车则不加载
      
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
      if (this.markers && this.markers.length > 0) {
        this.markers.forEach(marker => marker.setMap(null));
        this.markers = [];
      }

      // 批量创建新标记
      const newMarkers = bikeList.map(bike => {
        const marker = new window.AMap.Marker({
          position: [bike.lng, bike.lat],
          map: this.showBikes ? this.map : null, // 根据显示状态决定是否添加到地图
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

        return marker;
      });

      this.markers = newMarkers;
    },

    drawParkingAreas() {
      if (!this.showParkingAreas) return; // 如果隐藏则不绘制
      
      const infoWindow = new window.AMap.InfoWindow({
        offset: new window.AMap.Pixel(0, -20)
      });

      // 批量创建多边形
      const newPolygons = this.parkingAreas.map(area => {
        const polygon = new window.AMap.Polygon({
          path: area.polygonPath,
          fillColor: "#FFD600",
          fillOpacity: 0.2,
          strokeColor: "#FFD600",
          strokeWeight: 2,
          zIndex: 40,
          cursor: "pointer"
        });
        
        // 根据showParkingAreas状态决定是否显示
        if (this.showParkingAreas) {
          this.map.add(polygon);
        }
        
        polygon.on("mouseover", (e) => {
          infoWindow.setContent(`<div style="min-width:160px;"><b>停车区域：</b>${area.geohash}</div>`);
          infoWindow.open(this.map, e.lnglat);
        });
        polygon.on("mouseout", () => infoWindow.close());
        
        return polygon;
      });

      this.parkingPolygons = newPolygons;
    },
    onToggleBikes() {
      this.showBikes = !this.showBikes;
      
      if (this.showBikes) {
        // 显示时立即刷新视野内数据
        this.loadBicycles();
      } else {
        // 隐藏时立即隐藏所有单车标记
        if (this.markers && this.markers.length > 0) {
          this.markers.forEach(marker => marker.setMap(null));
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

    onToggleParkingAreas() {
      this.showParkingAreas = !this.showParkingAreas;
      
      if (this.showParkingAreas) {
        // 显示时立即刷新视野内数据
        this.loadParkingAreas();
      } else {
        // 隐藏时立即隐藏所有停车区域
        if (this.parkingPolygons && this.parkingPolygons.length > 0) {
          this.parkingPolygons.forEach(polygon => {
            polygon.setMap(null);
          });
        }
      }
      
      console.log(`停车区域已${this.showParkingAreas ? '显示' : '隐藏'}`);
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
    // 【新增】设置地图事件监听器 - 使用节流优化
    setupMapEventListeners() {
      const throttledUpdate = this.throttle(() => {
        if (!this.isUpdating) {
          this.isUpdating = true;
          this.loadBicycles();
          this.loadParkingAreas();
          setTimeout(() => {
            this.isUpdating = false;
          }, 500);
        }
      }, this.updateThrottle);

      this.map.on('moveend', throttledUpdate);
      this.map.on('zoomend', throttledUpdate);
    },

    // 【新增】节流函数
    throttle(func, delay) {
      return function(...args) {
        const now = Date.now();
        if (now - this.lastUpdateTime >= delay) {
          this.lastUpdateTime = now;
          func.apply(this, args);
        }
      }.bind(this);
    },

    // 【新增】优化的数据更新方法
    async updateMapData() {
      if (this.updateTimeout) {
        clearTimeout(this.updateTimeout);
      }
      
      this.updateTimeout = setTimeout(async () => {
        if (this.showBikes) {
          await this.loadBicycles();
        }
        if (this.showParkingAreas) {
          await this.loadParkingAreas();
        }
      }, 300);
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
  right: 20px;
  z-index: 1001;
  display: flex;
  align-items: flex-end;
  gap: 16px;
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