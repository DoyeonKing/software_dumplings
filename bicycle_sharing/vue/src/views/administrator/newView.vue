<template>
  <div class="heatmap-view-root">
    <div id="heatmapContainer"></div>
    <button class="yellow-btn toggle-btn" @click="toggleHeatmap">
      {{ showHeatmap ? '显示普通地图' : '显示热力图' }}
    </button>
  </div>
</template>

<script>
export default {
  name: "NewView",
  data() {
    return {
      map: null,
      heatmap: null,
      bikeMarkers: [],
      showHeatmap: false,
      bikePoints: []
    };
  },
  mounted() {
    if (window.AMap && window.AMap.Heatmap) {
      this.initMap();
    } else {
      const check = setInterval(() => {
        if (window.AMap && window.AMap.Heatmap) {
          clearInterval(check);
          this.initMap();
        }
      }, 100);
    }
  },
  methods: {
    initMap() {
      this.map = new window.AMap.Map('heatmapContainer', {
        zoom: 15,
        center: [114.0598, 22.5350]
      });
      this.generateBikePoints();
      this.addBikeMarkers();
      this.initHeatmap();
    },
    generateBikePoints() {
      const points = [];
      for (let i = 0; i < 120; i++) {
        points.push({
          lng: 114.05 + Math.random() * 0.02,
          lat: 22.53 + Math.random() * 0.02,
          value: Math.floor(Math.random() * 10) + 1 // 2.0用value
        });
      }
      this.bikePoints = points;
    },
    addBikeMarkers() {
      if (this.bikeMarkers.length) {
        this.bikeMarkers.forEach(marker => marker.setMap(null));
        this.bikeMarkers = [];
      }
      const svgIcon = `
        <svg width="36" height="36" viewBox="0 0 36 36">
          <circle cx="18" cy="18" r="16" fill="#FFD600" stroke="#FFC107" stroke-width="2"/>
          <text x="18" y="23" text-anchor="middle" font-size="18" font-family="Arial" fill="#222">🚲</text>
        </svg>
      `;
      this.bikePoints.forEach(point => {
        const marker = new window.AMap.Marker({
          position: [point.lng, point.lat],
          content: svgIcon,
          offset: new window.AMap.Pixel(-18, -18),
          zIndex: 120
        });
        marker.setMap(this.map);
        this.bikeMarkers.push(marker);
      });
    },
    initHeatmap() {
      if (this.heatmap) {
        this.heatmap.hide();
        this.heatmap = null;
      }
      this.heatmap = new window.AMap.Heatmap(this.map, {
        radius: 40,
        opacity: [0.2, 0.95],
        gradient: {
          0.2: 'blue',
          0.5: 'lime',
          0.7: 'yellow',
          1.0: 'red'
        }
      });
      // 2.0用 setData，点数据用 value 字段
      this.heatmap.setData(this.bikePoints);
      this.heatmap.hide();
    },
    toggleHeatmap() {
      this.showHeatmap = !this.showHeatmap;
      if (this.showHeatmap) {
        this.bikeMarkers.forEach(marker => marker.hide());
        this.heatmap.show();
      } else {
        this.bikeMarkers.forEach(marker => marker.show());
        this.heatmap.hide();
      }
    }
  }
};
</script>

<style scoped>
.heatmap-view-root {
  width: 100vw;
  height: 100vh;
  position: relative;
}
#heatmapContainer {
  width: 100vw;
  height: 100vh;
}
.toggle-btn {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 10;
  font-size: 1rem;
  padding: 10px 24px;
  border-radius: 20px;
  background: #FFD600;
  color: #222;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
  cursor: pointer;
  font-weight: bold;
  transition: background 0.2s;
}
.toggle-btn:hover {
  background: #ffe066;
}
</style>