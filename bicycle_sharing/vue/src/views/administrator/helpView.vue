<template>
  <div class="help-view-root">
    <div id="mapContainer"></div>

    <div class="help-backdrop">
      <div class="help-container">
        <div class="help-tip">温馨提示：所有页面点击跳转！</div>

        <h2 class="help-title">
          <span class="icon-help">💡</span> 帮助指南
        </h2>
        
        <div class="help-content">
          <div class="help-item">
            <h3 class="help-question">
              <span class="icon-q">❓</span> 如何快速开始使用系统？
            </h3>
            <p class="help-answer">
              系统核心功能都集中在几个主要页面。您可以首先访问
              <router-link to="/admin" class="help-link">主页面</router-link>
              ，这里是系统的入口和概览，可以快速了解系统整体状态。
            </p>
          </div>

          <div class="help-item">
            <h3 class="help-question">
              <span class="icon-q">❓</span> 我想查看车辆运营的核心数据，应该去哪里？
            </h3>
            <p class="help-answer">
              所有关键的运营数据，如车辆使用率、收入、区域天气等，都在
              <router-link to="/dashboard" class="help-link">指标看板页面</router-link>
              中以图表形式展示，方便您进行数据分析和决策。这里提供了完整的业务洞察。
            </p>
          </div>

          <div class="help-item">
            <h3 class="help-question">
              <span class="icon-q">❓</span> 如何管理车辆调度和定位？
            </h3>
            <p class="help-answer">
              请前往
              <router-link to="/location" class="help-link">车辆调度页面</router-link>
              。您不仅可以在地图上看到所有车辆的实时位置，还可以查看特定停车区域的车辆数量，发布车辆调度任务，并管理区域数据和调度建议。
            </p>
          </div>
        </div>
      </div>
    </div>

    <MenuComponent @profile-saved="handleProfileSaved" />
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue'
import { mapMixin } from '@/utils/mapMixin.js'
import AMapLoader from '@/utils/loadAMap.js'

export default {
  name: "HelpView",
  components: {MenuComponent},
  mixins: [mapMixin],
  data() {
    return {
      bikeList: [
        { id: "SZ1001", lng: 114.057868, lat: 22.53445, status: "正常", address: "深圳市-福田区-福华三路" },
        { id: "SZ1002", lng: 114.060868, lat: 22.53495, status: "故障", address: "深圳市-福田区-金田路" },
        { id: "SZ1003", lng: 114.058868, lat: 22.53645, status: "待维修", address: "深圳市-福田区-滨河大道" },
        { id: "SZ1004", lng: 114.061868, lat: 22.53445, status: "正常", address: "深圳市-福田区-会展中心" },
        { id: "SZ1005", lng: 114.061867, lat: 22.53545, status: "正常", address: "深圳市-福田区-福华一路" },
        { id: "SZ1006", lng: 114.057000, lat: 22.53400, status: "正常", address: "深圳市-福田区-福华三路附近" },
        { id: "SZ1007", lng: 114.058500, lat: 22.53500, status: "正常", address: "深圳市-福田区-金田路附近" },
        { id: "SZ1008", lng: 114.059500, lat: 22.53600, status: "故障", address: "深圳市-福田区-滨河大道附近" },
      ]
    };
  },
  mounted() {
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      const {yellowBikeIcon} = this.initMap();
      this.map.setZoomAndCenter(15, [114.0598, 22.5350]);
      this.addBikeMarkers(this.bikeList, yellowBikeIcon);
    }).catch(err => {
      this.$message && this.$message.error
          ? this.$message.error('地图加载失败: ' + err.message)
          : alert('地图加载失败: ' + err.message);
    });
  },
  methods: {
    handleProfileSaved(formData) {
      console.log('个人资料已保存:', formData);
      window.alert('个人资料已在控制台正确捕获，详情请查看浏览器开发者工具的控制台。');
    }
  }
};
</script>

<style scoped>
@import '@/assets/globalStyles.css';

.help-view-root {
  position: relative;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

#mapContainer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 毛玻璃背景遮罩 */
.help-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  background-color: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 帮助信息浮窗 */
.help-container {
  /* 新增 position: relative; 以便子元素绝对定位 */
  position: relative;
  width: 70vw;
  max-width: 1200px;
  min-width: 800px;
  aspect-ratio: 16 / 9;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 30px 40px;
  display: flex;
  flex-direction: column;
  color: #333;
}

/* 新增的提示信息样式 */
.help-tip {
  position: absolute;
  top: 83px;
  right: 40px;
  font-size: 0.9rem;
  color: #999;
  font-weight: 500;
}

.help-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  color: #111;
  border-bottom: 2px solid #FFD600;
  padding-bottom: 15px;
}

.icon-help {
  font-size: 2.2rem;
  margin-right: 15px;
}

.help-content {
  flex-grow: 1;
  overflow-y: auto;
  padding-right: 15px; /* For scrollbar */
}

.help-item {
  margin-bottom: 25px;
}

.help-question {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.icon-q {
  margin-right: 10px;
  font-size: 1.3rem;
  color: #FFD600;
}

.help-answer {
  font-size: 1.1rem;
  line-height: 1.8;
  padding-left: 35px;
  color: #444;
}

/* 可点击链接的样式 */
.help-link {
  font-weight: bold;
  color: #e65100; /* 橙色，用于突出显示 */
  text-decoration: none;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease-in-out;
}

.help-link:hover {
  color: #d84315;
  border-bottom-color: #ffd180;
  background-color: #fff9c4;
  padding: 2px 4px;
  border-radius: 4px;
}


/* 响应式设计 */
@media (max-width: 900px) {
  .help-container {
    width: 90vw;
    min-width: unset;
    height: 80vh;
    aspect-ratio: unset;
    padding: 20px;
  }
  .help-tip {
    top: 25px;
    right: 20px;
    font-size: 0.8rem;
  }
  .help-title {
    font-size: 1.5rem;
  }
  .help-question {
    font-size: 1.1rem;
  }
  .help-answer {
    font-size: 1rem;
  }
}
</style>