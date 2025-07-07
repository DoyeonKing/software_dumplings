<template>
  <div>
    <h2>地图展示</h2>
    <div class="controls">
      <div class="layers">
        <label><input type="checkbox" v-model="showMainRoads"> 主干道</label>
        <label><input type="checkbox" v-model="showSecondaryRoads"> 次干道</label>
      </div>
      <div class="style-switch">
        <button @click="toggleDarkMode">切换深色模式</button>
      </div>
    </div>
    <div id="map" style="height: 600px;"></div>
  </div>
</template>

<script>
import { onMounted, ref } from 'vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

// 设置 Mapbox access token
mapboxgl.accessToken = 'pk.eyJ1IjoibGhtNzA3IiwiYSI6ImNtYm0yZ2o3ZjAzaTgyaW9zZ3p2ZnQ4ZGoifQ.wRLFPVAKZ_v8IeMjZYiCSA';

export default {
  name: 'TestView',
  setup() {
    const showMainRoads = ref(true);
    const showSecondaryRoads = ref(true);
    const isDarkMode = ref(false);
    let map = null;

    // 路网数据示例
    const roadNetworkData = {
      'type': 'FeatureCollection',
      'features': [
        {
          'type': 'Feature',
          'properties': {
            'type': 'main',
            'name': '长安街'
          },
          'geometry': {
            'type': 'LineString',
            'coordinates': [
              [116.4074, 39.9042],
              [116.4080, 39.9045],
              [116.4090, 39.9050]
            ]
          }
        },
        {
          'type': 'Feature',
          'properties': {
            'type': 'secondary',
            'name': '次干道1'
          },
          'geometry': {
            'type': 'LineString',
            'coordinates': [
              [116.4080, 39.9045],
              [116.4085, 39.9055],
              [116.4090, 39.9065]
            ]
          }
        }
      ]
    };

    const toggleDarkMode = () => {
      isDarkMode.value = !isDarkMode.value;
      if (map) {
        const style = isDarkMode.value ? 
          'mapbox://styles/mapbox/dark-v11' : 
          'mapbox://styles/mapbox/light-v11';
        map.setStyle(style);
      }
    };

    onMounted(() => {
      // 初始化地图
      map = new mapboxgl.Map({
        container: 'map',
        style: 'mapbox://styles/mapbox/light-v11',
        center: [116.4074, 39.9042],
        zoom: 13
      });

      map.on('load', () => {
        // 添加路网数据源
        map.addSource('road-network', {
          'type': 'geojson',
          'data': roadNetworkData
        });

        // 添加主干道图层
        map.addLayer({
          'id': 'main-roads',
          'type': 'line',
          'source': 'road-network',
          'filter': ['==', ['get', 'type'], 'main'],
          'layout': {
            'line-join': 'round',
            'line-cap': 'round',
            'visibility': showMainRoads.value ? 'visible' : 'none'
          },
          'paint': {
            'line-color': '#ff4444',
            'line-width': 4,
            'line-opacity': 0.8
          }
        });

        // 添加次干道图层
        map.addLayer({
          'id': 'secondary-roads',
          'type': 'line',
          'source': 'road-network',
          'filter': ['==', ['get', 'type'], 'secondary'],
          'layout': {
            'line-join': 'round',
            'line-cap': 'round',
            'visibility': showSecondaryRoads.value ? 'visible' : 'none'
          },
          'paint': {
            'line-color': '#4477ff',
            'line-width': 3,
            'line-opacity': 0.6
          }
        });

        // 监听图层显示控制
        watch(showMainRoads, (visible) => {
          map.setLayoutProperty(
            'main-roads',
            'visibility',
            visible ? 'visible' : 'none'
          );
        });

        watch(showSecondaryRoads, (visible) => {
          map.setLayoutProperty(
            'secondary-roads',
            'visibility',
            visible ? 'visible' : 'none'
          );
        });

        // 添加弹出框交互
        map.on('click', ['main-roads', 'secondary-roads'], (e) => {
          const coordinates = e.features[0].geometry.coordinates.slice();
          const name = e.features[0].properties.name;
          
          new mapboxgl.Popup()
            .setLngLat(coordinates[0])
            .setHTML(`<h3>${name}</h3>`)
            .addTo(map);
        });

        // 鼠标悬停效果
        map.on('mouseenter', ['main-roads', 'secondary-roads'], () => {
          map.getCanvas().style.cursor = 'pointer';
        });
        
        map.on('mouseleave', ['main-roads', 'secondary-roads'], () => {
          map.getCanvas().style.cursor = '';
        });
      });
    });

    return {
      showMainRoads,
      showSecondaryRoads,
      toggleDarkMode
    };
  }
}
</script>

<style>
.controls {
  margin: 10px 0;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

.layers {
  margin-bottom: 10px;
}

.layers label {
  margin-right: 15px;
}

#map {
  width: 100%;
  height: 600px;
  border: 1px solid #ccc;
}

/* Mapbox GL 自定义样式 */
.mapboxgl-popup {
  max-width: 200px;
}

.mapboxgl-popup-content {
  padding: 10px;
}

.mapboxgl-popup-content h3 {
  margin: 0;
  font-size: 14px;
}
</style>