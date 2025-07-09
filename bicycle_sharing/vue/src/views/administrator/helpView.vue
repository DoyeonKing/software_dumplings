<template>
  <div class="help-view-root">
    <div id="mapContainer"></div>
    <!-- 复用菜单组件，个人资料弹窗也由其内部控制 -->
    <MenuComponent @profile-saved="handleProfileSaved" />
    <!-- 你可以在这里继续添加其他业务面板、按钮等 -->
  </div>
</template>

<script>
import MenuComponent from '@/components/admin/menuComponent.vue'
import { mapMixin } from '@/utils/mapMixin.js'
import AMapLoader from '@/utils/loadAMap.js' // 新增

export default {
  name: "HelpView",
  components: {MenuComponent},
  mixins: [mapMixin],
  data() {
    return {
      // 你可以在这里添加页面需要的业务数据
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
      ]
    };
  },
  mounted() {
    // 先动态加载高德地图SDK
    AMapLoader.load('dea7cc14dad7340b0c4e541dfa3d27b7', 'AMap.Heatmap').then(() => {
      // SDK加载完成后再初始化地图和标记
      const {yellowBikeIcon} = this.initMap();
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
    }
    // 你可以继续添加其他业务方法
  }
};
</script>

<style scoped>
@import '@/assets/globalStyles.css';

html, body, #app, .help-view-root {
  height: 100%;
  margin: 0;
  padding: 0;
}

.help-view-root {
  position: relative;
  height: 100vh;
  overflow: hidden;
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