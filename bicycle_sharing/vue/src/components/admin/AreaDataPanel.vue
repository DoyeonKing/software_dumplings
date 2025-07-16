<template>
  <div class="area-data-panel">
    <!-- 预测参数设置区域 -->
    <div class="prediction-params">
      <div class="param-row">
        <label>预测时间:</label>
        <input 
          type="date" 
          v-model="reportDate" 
          class="date-input"
          :max="getTodayString()"
        />
      </div>
      <div class="param-row">
        <label>具体时段:</label>
        <input 
          type="number" 
          v-model.number="predictionHour" 
          min="0" 
          max="24" 
          class="hour-input"
          placeholder="0-24"
        />
        <span class="unit">时</span>
      </div>
      <button 
        class="predict-btn yellow-btn" 
        @click="predictAreaData"
        :disabled="!reportDate || predictionHour < 0 || predictionHour > 24 || isLoading"
      >
        {{ isLoading ? '预测中...' : '点击预测' }}
      </button>
    </div>

    <!-- 搜索和区域选择区域 -->
    <div class="search-section">
      <div class="search-row">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索区域..." 
          class="search-input"
          @input="onSearchInput"
        />
      </div>
      <div class="area-selection-row">
        <div class="selection-label">区域选择:</div>
        <div class="selected-area-display">
          {{ selectedAreaGeohash || '点击地图选择区域' }}
        </div>
        <button 
          v-if="selectedAreaGeohash" 
          class="clear-area-btn" 
          @click="clearAreaSelection"
        >
          清空
        </button>
      </div>
    </div>

    <!-- 热力图控制区域 -->
    <div class="heatmap-control">
      <button 
        class="heatmap-btn yellow-btn" 
        @click="toggleHeatmap"
        :disabled="!hasSearched || isLoading"
      >
        {{ showHeatmap ? '关闭预测热力图' : '生成预测热力图' }}
      </button>
    </div>

    <!-- 区域数据列表 -->
    <div class="area-data-list">
      <div v-if="areaData.length === 0 && !isLoading" class="no-data">
        <p>{{ hasSearched ? '当前屏幕范围内暂无区域数据' : '请设置预测参数并点击"点击预测"' }}</p>
      </div>
      <div v-else-if="filteredAreaData.length === 0 && areaData.length > 0" class="no-data">
        <p>没有符合搜索条件的区域数据</p>
      </div>
      <div 
        v-else
        class="area-data-scroll-container"
      >
        <div 
          v-for="(area, index) in filteredAreaData" 
          :key="area.geohash || index"
          class="area-data-item"
        >
          <div class="area-header">
            <span class="area-geohash">{{ area.geohash }}</span>
            <span class="area-status" :class="getStatusClass(area.status)">
              {{ area.status }}
            </span>
          </div>
          <div class="area-details">
            <div class="detail-row">
              <span class="label">位置:</span>
              <span class="value">{{ formatCoordinates(area.latitude, area.longitude) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">使用率:</span>
              <span class="value usage-rate">{{ (area.utilizationRate * 100).toFixed(1) }}%</span>
            </div>
            <div class="detail-row">
              <span class="label">当前车辆:</span>
              <span class="value">{{ area.currentBikes }} 辆</span>
            </div>
            <div class="detail-row">
              <span class="label">停车容量:</span>
              <span class="value">{{ area.parkingCapacity }} 辆</span>
            </div>
            <div class="detail-row">
              <span class="label">预测取车:</span>
              <span class="value">{{ area.predictedPickups }} 辆</span>
            </div>
            <div class="detail-row">
              <span class="label">预测停车:</span>
              <span class="value">{{ area.predictedDropoffs }} 辆</span>
            </div>
            <div class="detail-row">
              <span class="label">未来车辆:</span>
              <span class="value">{{ area.futureBikes }} 辆</span>
            </div>
            <div class="detail-row">
              <span class="label">剩余车位:</span>
              <span class="value">{{ area.futureRemainingSpots }} 个</span>
            </div>
            <div class="detail-row">
              <span class="label">预测时间:</span>
              <span class="value">{{ formatDateTime(area.predictionTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getMapAreaSuggestions } from '@/api/prediction/suggestionService';

export default {
  name: 'AreaDataPanel',
  props: {
    map: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      searchQuery: '',
      areaData: [],
      reportDate: '',
      predictionHour: 0,
      isLoading: false,
      hasSearched: false,
      selectedAreaGeohash: null,
      showHeatmap: false,
      heatmapInstance: null
    }
  },
  computed: {
    filteredAreaData() {
      console.log('计算过滤后的区域数据，原始数据数量:', this.areaData.length);
      
      let filtered = this.areaData;
      
      // 搜索过滤
      if (this.searchQuery) {
        filtered = filtered.filter(area => {
          return area.geohash?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
                 area.status?.toLowerCase().includes(this.searchQuery.toLowerCase());
        });
      }
      
      // 区域选择过滤
      if (this.selectedAreaGeohash) {
        filtered = filtered.filter(area => area.geohash === this.selectedAreaGeohash);
      }
      
      console.log('过滤后的区域数据数量:', filtered.length);
      return filtered;
    }
  },
  mounted() {
    // 设置默认日期为2019-12-31
    this.reportDate = '2019-12-31';
  },
  
  beforeUnmount() {
    // 清理热力图
    this.closeHeatmap();
  },
  methods: {
    getStatusClass(status) {
      const statusMap = {
        '稳定区': 'stable',
        '需求区': 'demand',
        '供应区': 'supply'
      };
      return statusMap[status] || 'default';
    },
    formatCoordinates(lat, lng) {
      if (!lat || !lng) return '';
      return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    getTodayString() {
      return '2019-12-31';
    },
    getMapBounds() {
      if (!this.map) {
        console.error('地图实例不可用');
        return null;
      }
      
      try {
        const bounds = this.map.getBounds();
        const southWest = bounds.getSouthWest();
        const northEast = bounds.getNorthEast();
        
        return {
          minLat: southWest.getLat(),
          maxLat: northEast.getLat(),
          minLon: southWest.getLng(),
          maxLon: northEast.getLng()
        };
      } catch (error) {
        console.error('获取地图边界失败:', error);
        return null;
      }
    },
    async predictAreaData() {
      if (!this.reportDate || this.predictionHour < 0 || this.predictionHour > 24) {
        alert('请先选择预测日期和具体时段（0-24小时）');
        return;
      }

      if (!this.map) {
        alert('地图未加载完成，请稍后再试');
        return;
      }

      this.isLoading = true;
      this.hasSearched = true;
      try {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
          alert('登录已过期，请重新登录');
          this.isLoading = false;
          return;
        }

        console.log('开始预测区域数据...');
        console.log('预测参数:', { reportDate: this.reportDate, predictionHour: this.predictionHour });

        // 获取当前地图边界
        const mapBounds = this.getMapBounds();
        if (!mapBounds) {
          alert('获取地图边界失败，请重试');
          this.isLoading = false;
          return;
        }

        console.log('地图边界:', mapBounds);

        // 调用API
        const response = await getMapAreaSuggestions(
          this.reportDate,
          this.predictionHour,
          mapBounds
        );
        console.log('区域数据API响应:', response);

        if (response && (response.code === 200 || response.code === '200')) {
          // 获取areaStatuses数据
          const areaStatusesData = response.data?.areaStatuses || [];
          this.areaData = areaStatusesData;
          console.log('成功获取区域数据数量:', this.areaData.length);
          
          if (this.areaData.length > 0) {
            alert(`成功获取到 ${this.areaData.length} 条区域数据`);
          } else {
            alert('当前屏幕范围内暂无区域数据，请尝试调整预测参数或地图视图');
          }
        } else {
          console.warn('预测区域数据失败 - API响应:', response);
          this.areaData = [];
          alert(`预测区域数据失败: ${response?.msg || '未知错误'}`);
        }
      } catch (error) {
        console.error('预测区域数据出错:', error);
        if (error.response) {
          alert(`预测区域数据失败: HTTP ${error.response.status} - ${error.response.data?.msg || error.message}`);
        } else {
          alert(`预测区域数据失败: ${error.message}`);
        }
        this.areaData = [];
      } finally {
        this.isLoading = false;
      }
    },
    onSearchInput() {
      // 当搜索框有内容时，清空区域选择
      if (this.searchQuery) {
        this.selectedAreaGeohash = null;
      }
    },
    setSelectedArea(geohash) {
      // 当选择区域时，清空搜索框
      this.searchQuery = '';
      this.selectedAreaGeohash = geohash;
    },
    clearAreaSelection() {
      this.selectedAreaGeohash = null;
    },
    
    // 热力图相关方法
    async toggleHeatmap() {
      if (this.showHeatmap) {
        this.closeHeatmap();
      } else {
        await this.generateHeatmap();
      }
    },
    
    async generateHeatmap() {
      if (!this.map || !this.reportDate || this.predictionHour < 0 || this.predictionHour > 24) {
        alert('请先设置预测参数并获取区域数据');
        return;
      }
      
      try {
        console.log('开始生成预测热力图...');
        
        // 调用热力图API
        const response = await this.fetchHeatmapData();
        
        if (response && response.length > 0) {
          this.createHeatmap(response);
          this.showHeatmap = true;
          console.log('预测热力图生成成功');
        } else {
          alert('未获取到热力图数据，请检查预测参数');
        }
      } catch (error) {
        console.error('生成热力图失败:', error);
        alert(`生成热力图失败: ${error.message}`);
      }
    },
    
    async fetchHeatmapData() {
      const token = sessionStorage.getItem('authToken');
      if (!token) {
        throw new Error('登录已过期，请重新登录');
      }
      
      // 构建查询参数
      const queryParams = new URLSearchParams({
        reportDateStr: this.reportDate,
        predictionTimeHour: this.predictionHour.toString()
      });
      
      console.log('热力图API参数:', queryParams.toString());
      
      // 使用GET方法，参数通过查询字符串传递
      const response = await fetch(`http://localhost:8080/api/predict/heatmap_data?${queryParams}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const result = await response.json();
      console.log('热力图API响应:', result);
      
      if (result.code === '200' && result.data) {
        return result.data;
      } else {
        throw new Error(result.msg || '获取热力图数据失败');
      }
    },
    
    createHeatmap(heatmapData) {
      // 清除现有热力图
      this.closeHeatmap();
      
      // 转换数据格式
      const heatmapPoints = heatmapData.map(item => ({
        lng: item.longitude,
        lat: item.latitude,
        count: item.weight
      }));
      
      console.log('热力图数据点数量:', heatmapPoints.length);
      
      // 创建热力图
      this.heatmapInstance = new window.AMap.HeatMap(this.map, {
        radius: 25,
        opacity: [0.1, 0.9],
        gradient: {
          0.2: 'blue',
          0.4: 'green',
          0.6: 'yellow',
          0.8: 'orange',
          1.0: 'red'
        }
      });
      
      // 设置热力图数据
      this.heatmapInstance.setDataSet({
        data: heatmapPoints,
        max: Math.max(...heatmapPoints.map(p => p.count))
      });
      
      console.log('热力图创建完成');
    },
    
    closeHeatmap() {
      if (this.heatmapInstance) {
        this.heatmapInstance.setMap(null);
        this.heatmapInstance = null;
      }
      this.showHeatmap = false;
      console.log('热力图已关闭');
    }
  }
};
</script>

<style scoped>
.area-data-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.prediction-params {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.param-row label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  min-width: 70px;
}

.date-input, .hour-input {
  flex: 1;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.85rem;
  background: white;
}

.hour-input {
  width: 60px;
  text-align: center;
}

.unit {
  font-size: 0.85rem;
  color: #666;
}

.predict-btn {
  padding: 8px 16px;
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  background: #FFD600;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.predict-btn:hover:not(:disabled) {
  background: #FFC107;
  transform: translateY(-1px);
}

.predict-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.heatmap-control {
  display: flex;
  justify-content: center;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.heatmap-btn {
  padding: 8px 16px;
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  background: #FFD600;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
}

.heatmap-btn:hover:not(:disabled) {
  background: #FFC107;
  transform: translateY(-1px);
}

.heatmap-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.search-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.search-row {
  display: flex;
  align-items: center;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  background: white;
}

.area-selection-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selection-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  min-width: 70px;
}

.selected-area-display {
  flex: 1;
  padding: 6px 8px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #666;
  min-height: 20px;
  display: flex;
  align-items: center;
}

.clear-area-btn {
  padding: 4px 8px;
  font-size: 0.8rem;
  border: 1px solid #dc3545;
  border-radius: 4px;
  background: white;
  color: #dc3545;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-area-btn:hover {
  background: #dc3545;
  color: white;
}

.area-data-list {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: #666;
  font-size: 0.9rem;
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.area-data-scroll-container {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.area-data-item {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.area-geohash {
  font-weight: 600;
  font-size: 0.9rem;
  color: #333;
}

.area-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.area-status.stable {
  background: #d4edda;
  color: #155724;
}

.area-status.demand {
  background: #fff3cd;
  color: #856404;
}

.area-status.supply {
  background: #d1ecf1;
  color: #0c5460;
}

.area-status.default {
  background: #f8f9fa;
  color: #6c757d;
}

.area-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
}

.detail-row .label {
  color: #666;
  font-weight: 500;
}

.detail-row .value {
  color: #333;
  font-weight: 600;
}

.usage-rate {
  color: #28a745;
}

/* 滚动条样式 */
.area-data-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.area-data-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.area-data-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.area-data-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 