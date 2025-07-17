<template>
  <div class="suggestion-panel">
    <!-- 新增：预测参数设置区域 -->
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
        @click="predictSuggestions"
        :disabled="!reportDate || predictionHour < 0 || predictionHour > 24 || isLoading"
      >
        {{ isLoading ? '预测中...' : '预测调度建议' }}
      </button>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="search-section">
      <input 
        type="text" 
        v-model="searchQuery" 
        placeholder="搜索建议..." 
        class="search-input"
        @input="filterSuggestions"
      />
    </div>

    <!-- 建议列表 -->
    <div class="suggestions-list">
      <div v-if="suggestions.length === 0 && !isLoading" class="no-suggestions">
        <p>{{ hasSearched ? '当前屏幕范围内暂无调度建议' : '请设置预测参数并点击"预测调度建议"' }}</p>
      </div>
      <div v-else-if="filteredSuggestions.length === 0 && suggestions.length > 0" class="no-suggestions">
        <p>没有符合搜索条件的调度建议</p>
      </div>
      <div 
        v-for="(suggestion, index) in filteredSuggestions" 
        :key="suggestion.id || index"
        class="suggestion-item"
      >
        <div class="suggestion-content">
          <div class="suggestion-header">
            <span class="suggestion-id">建议ID: {{ suggestion.id || `临时ID_${index + 1}` }}</span>
            <div class="suggestion-status" :class="(suggestion.suggestionStatus || 'PENDING')?.toLowerCase()">
              {{ getStatusText(suggestion.suggestionStatus || 'PENDING') }}
            </div>
          </div>
          <div class="suggestion-details">
            <div class="detail-row">
              <span class="label">起点:</span>
              <span class="value">{{ suggestion.sourceGeohash }}</span>
            </div>
            <div class="detail-row">
              <span class="label">终点:</span>
              <span class="value">{{ suggestion.targetGeohash }}</span>
            </div>
            <div class="detail-row">
              <span class="label">调度数量:</span>
              <span class="value">{{ suggestion.suggestedBikeCount }} 辆</span>
            </div>
            <div class="detail-row">
              <span class="label">预测时间:</span>
              <span class="value">{{ formatDateTime(suggestion.predictionTargetTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">建议时间:</span>
              <span class="value">{{ formatDateTime(suggestion.suggestionTime) }}</span>
            </div>
          </div>
        </div>
        <div class="suggestion-actions">
          <button class="action-btn accept" @click="acceptSuggestion(suggestion)">
            接受
          </button>
          <button class="action-btn reject" @click="rejectSuggestion(suggestion)">
            拒绝
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getMapAreaSuggestions, updateSuggestionStatus } from '@/api/prediction/suggestionService';

export default {
  name: 'DispatchSuggestionPanel',
  props: {
    map: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      searchQuery: '',
      suggestions: [],
      reportDate: '',
      predictionHour: 0,
      isLoading: false,
      hasSearched: false
    }
  },
  computed: {
    filteredSuggestions() {
      console.log('计算过滤后的建议，原始建议数量:', this.suggestions.length);
      console.log('原始建议数据:', this.suggestions);
      
      const filtered = this.suggestions
        .filter(suggestion => {
          const matchesSearch = this.searchQuery === '' ||
            suggestion.id?.toString().includes(this.searchQuery) ||
            suggestion.sourceGeohash?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            suggestion.targetGeohash?.toLowerCase().includes(this.searchQuery.toLowerCase());
          
          // 暂时不进行状态过滤，显示所有建议
          // const matchesFilter = this.currentFilter === suggestion.suggestionStatus;
          
          return matchesSearch; // 只进行搜索过滤，不进行状态过滤
        });
      
      console.log('过滤后的建议数量:', filtered.length);
      console.log('过滤后的建议数据:', filtered);
      
      return filtered;
    }
  },
  mounted() {
    // 设置默认日期为今天
    this.reportDate = this.getTodayString();
  },
  methods: {
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'ACCEPTED': '已接受',
        'REJECTED': '已拒绝'
      };
      return statusMap[status] || status;
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    getTodayString() {
      return '2019-12-31';
    },
    formatDateTimeForAPI(dateString) {
      // 直接返回日期字符串，因为现在只选择日期
      if (!dateString) return '';
      return dateString; // 已经是YYYY-MM-DD格式
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
    async predictSuggestions() {
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

        console.log('开始预测调度建议...');
        console.log('当前token:', token ? '已设置' : '未设置');
        console.log('预测参数:', { reportDate: this.reportDate, predictionHour: this.predictionHour });

        // 获取当前地图边界
        const mapBounds = this.getMapBounds();
        if (!mapBounds) {
          alert('获取地图边界失败，请重试');
          this.isLoading = false;
          return;
        }

        console.log('地图边界:', mapBounds);

        // 直接使用日期字符串，已经是YYYY-MM-DD格式
        const reportDateStr = this.reportDate;
        console.log('日期格式:', reportDateStr);
        console.log('预测时段:', this.predictionHour);

        // 验证参数有效性
        if (!reportDateStr || reportDateStr.length !== 10 || !/^\d{4}-\d{2}-\d{2}$/.test(reportDateStr)) {
          alert('日期格式错误，请选择正确的日期（YYYY-MM-DD格式）');
          this.isLoading = false;
          return;
        }

        if (this.predictionHour < 0 || this.predictionHour > 24 || !Number.isInteger(this.predictionHour)) {
          alert('时段必须是0-24之间的整数');
          this.isLoading = false;
          return;
        }

        console.log('参数验证通过，开始调用API...');

        // 显示最终的API调用信息
        const finalUrl = '/api/predict/map_area';
        console.log('=== API调用详情 ===');
        console.log('请求URL:', finalUrl);
        console.log('请求方法: POST');
        console.log('Query参数:', { reportDateStr, predictionTimeHour: this.predictionHour });
        console.log('Request Body:', mapBounds);
        console.log('==================');

        // 调用API，传递所有参数给后端处理
        const response = await getMapAreaSuggestions(
          reportDateStr,
          this.predictionHour,
          mapBounds
        );
        console.log('预测调度建议API响应:', response);

        if (response && (response.code === 200 || response.code === '200')) {
          // 添加详细的数据结构调试信息
          console.log('完整的响应数据结构:', response);
          console.log('response.data:', response.data);
          console.log('response.data.dispatchSuggestions:', response.data?.dispatchSuggestions);
          console.log('response.data.areaStatuses:', response.data?.areaStatuses);
          
          // 修复：正确获取调度建议数据
          const suggestionsData = response.data?.dispatchSuggestions || [];
          this.suggestions = suggestionsData;
          console.log('成功获取调度建议数量:', this.suggestions.length);
          
          if (this.suggestions.length > 0) {
            alert(`成功获取到 ${this.suggestions.length} 条调度建议`);
          } else {
            alert('当前屏幕范围内暂无符合条件的调度建议，请尝试调整预测参数或地图视图');
          }
        } else {
          console.warn('预测调度建议失败 - API响应:', response);
          console.warn('响应code:', response?.code, '响应msg:', response?.msg);
          this.suggestions = [];
          alert(`预测调度建议失败: ${response?.msg || '未知错误'}`);
        }
      } catch (error) {
        console.error('预测调度建议出错 - 完整错误:', error);
        console.error('错误响应:', error.response);
        if (error.response) {
          console.error('HTTP状态码:', error.response.status);
          console.error('错误数据:', error.response.data);
          alert(`预测调度建议失败: HTTP ${error.response.status} - ${error.response.data?.msg || error.message}`);
        } else {
          alert(`预测调度建议失败: ${error.message}`);
        }
        this.suggestions = [];
      } finally {
        this.isLoading = false;
      }
    },
    async acceptSuggestion(suggestion) {
      try {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
          alert('登录已过期，请重新登录');
          return;
        }
        
        console.log('开始接受建议:', suggestion);
        console.log('当前token:', token ? '已设置' : '未设置');
        
        // 如果没有ID，直接处理接受逻辑，不调用API
        if (!suggestion.id) {
          console.log('建议没有ID，直接处理接受逻辑');
          
          // 发送事件给父组件，包含完整的建议数据
          this.$emit('suggestion-accepted', {
            id: `temp_${Date.now()}`,
            startArea: suggestion.sourceGeohash,
            endArea: suggestion.targetGeohash,
            amount: suggestion.suggestedBikeCount,
            originalSuggestion: suggestion
          });
          
          console.log('建议已接受');
          alert('建议已成功接受！');
          return;
        }
        
        const suggestionId = parseInt(suggestion.id);
        console.log('发送API参数:', { suggestionId, newStatus: 'ACCEPTED' });
        const response = await updateSuggestionStatus(suggestionId, 'ACCEPTED');
        console.log('接受建议API响应:', response);
        
        if (response && (response.code === 200 || response.code === '200')) {
          // 更新本地状态
          const suggestionIndex = this.suggestions.findIndex(s => s.id === suggestion.id);
          if (suggestionIndex !== -1) {
            this.suggestions[suggestionIndex].suggestionStatus = 'ACCEPTED';
          }
          
          // 发送事件给父组件，包含完整的建议数据
          this.$emit('suggestion-accepted', {
            id: suggestion.id,
            startArea: suggestion.sourceGeohash,
            endArea: suggestion.targetGeohash,
            amount: suggestion.suggestedBikeCount,
            originalSuggestion: suggestion
          });
          
          console.log('建议已接受，ID:', suggestion.id);
          alert('建议已成功接受！');
        } else {
          console.error('接受建议失败 - API响应:', response);
          console.error('响应code:', response?.code, '响应msg:', response?.msg);
          alert(`接受建议失败: ${response?.msg || '未知错误'}`);
        }
      } catch (error) {
        console.error('接受建议出错 - 完整错误:', error);
        console.error('错误响应:', error.response);
        if (error.response) {
          console.error('HTTP状态码:', error.response.status);
          console.error('错误数据:', error.response.data);
          alert(`接受建议失败: HTTP ${error.response.status} - ${error.response.data?.msg || error.message}`);
        } else {
          alert(`接受建议失败: ${error.message}`);
        }
      }
    },
    async rejectSuggestion(suggestion) {
      try {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
          alert('登录已过期，请重新登录');
          return;
        }
        
        console.log('开始拒绝建议:', suggestion);
        console.log('当前token:', token ? '已设置' : '未设置');
        
        // 如果没有ID，直接处理拒绝逻辑，不调用API
        if (!suggestion.id) {
          console.log('建议没有ID，直接处理拒绝逻辑');
          
          // 发送事件给父组件
          this.$emit('suggestion-rejected', {
            id: `temp_${Date.now()}`,
            originalSuggestion: suggestion
          });
          
          console.log('建议已拒绝');
          alert('建议已成功拒绝！');
          return;
        }
        
        const suggestionId = parseInt(suggestion.id);
        console.log('发送API参数:', { suggestionId, newStatus: 'REJECTED' });
        const response = await updateSuggestionStatus(suggestionId, 'REJECTED');
        console.log('拒绝建议API响应:', response);
        
        if (response && (response.code === 200 || response.code === '200')) {
          // 更新本地状态
          const suggestionIndex = this.suggestions.findIndex(s => s.id === suggestion.id);
          if (suggestionIndex !== -1) {
            this.suggestions[suggestionIndex].suggestionStatus = 'REJECTED';
          }
          
          // 发送事件给父组件
          this.$emit('suggestion-rejected', {
            id: suggestion.id,
            originalSuggestion: suggestion
          });
          
          console.log('建议已拒绝，ID:', suggestion.id);
          alert('建议已成功拒绝！');
        } else {
          console.error('拒绝建议失败 - API响应:', response);
          console.error('响应code:', response?.code, '响应msg:', response?.msg);
          alert(`拒绝建议失败: ${response?.msg || '未知错误'}`);
        }
      } catch (error) {
        console.error('拒绝建议出错 - 完整错误:', error);
        console.error('错误响应:', error.response);
        if (error.response) {
          console.error('HTTP状态码:', error.response.status);
          console.error('错误数据:', error.response.data);
          alert(`拒绝建议失败: HTTP ${error.response.status} - ${error.response.data?.msg || error.message}`);
        } else {
          alert(`拒绝建议失败: ${error.message}`);
        }
      }
    }
  },
  mounted() {
    // 设置默认日期为今天
    this.reportDate = this.getTodayString();
  }
}
</script>

<style scoped>
.suggestion-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  height: 500px;
  box-sizing: border-box;
  overflow: hidden;
}

.prediction-params {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.param-row label {
  font-size: 14px;
  color: #495057;
  font-weight: 600;
  min-width: 80px;
}

.date-input, .hour-input {
  padding: 8px 12px;
  border: 1.5px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
  box-sizing: border-box;
  flex: 1;
  background: white;
}

.date-input {
  max-width: 150px;
}

.date-input:focus, .hour-input:focus {
  border-color: #FFD600;
  box-shadow: 0 0 0 2px rgba(255, 214, 0, 0.1);
}

.hour-input {
  max-width: 100px;
}

.unit {
  font-size: 14px;
  color: #6c757d;
  font-weight: 500;
}

.predict-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  background: #FFD600;
  color: #000;
  margin-top: 8px;
}

.predict-btn:hover:not(:disabled) {
  background: #e6c100;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 214, 0, 0.3);
}

.predict-btn:disabled {
  background: #e9ecef;
  color: #6c757d;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.search-section {
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1.5px solid #FFD600;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: #FFD600;
  box-shadow: 0 0 0 2px rgba(255, 214, 0, 0.2);
}

.suggestions-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 4px;
  margin-right: -4px;
}

.no-suggestions {
  text-align: center;
  padding: 30px 20px;
  color: #6c757d;
  font-size: 14px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px dashed #dee2e6;
}

.no-suggestions p {
  margin: 0;
  line-height: 1.5;
}

/* 自定义滚动条样式 */
.suggestions-list::-webkit-scrollbar {
  width: 6px;
}

.suggestions-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.suggestions-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.suggestions-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.suggestion-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-sizing: border-box;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.suggestion-id {
  font-weight: 500;
  color: #333;
}

.suggestion-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.suggestion-status.pending {
  background: #fff3cd;
  color: #856404;
}

.suggestion-status.accepted {
  background: #d4edda;
  color: #155724;
}

.suggestion-status.rejected {
  background: #f8d7da;
  color: #721c24;
}

.suggestion-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.label {
  color: #666;
}

.value {
  color: #333;
  font-weight: 500;
}

.suggestion-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.action-btn {
  flex: 1;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.action-btn.accept {
  background: #28a745;
  color: white;
}

.action-btn.accept:hover {
  background: #218838;
}

.action-btn.reject {
  background: #dc3545;
  color: white;
}

.action-btn.reject:hover {
  background: #c82333;
}
</style> 