<template>
  <div class="suggestion-panel">
    <div class="search-section">
      <input 
        type="text" 
        v-model="searchQuery" 
        placeholder="搜索建议..." 
        class="search-input"
        @input="filterSuggestions"
      />
    </div>

    <div class="filter-buttons">
      <button 
        :class="['filter-btn', { active: currentFilter === 'PENDING' }]"
        @click="setFilter('PENDING')"
      >
        待处理
      </button>
      <button 
        :class="['filter-btn', { active: currentFilter === 'ACCEPTED' }]"
        @click="setFilter('ACCEPTED')"
      >
        已接受
      </button>
      <button 
        :class="['filter-btn', { active: currentFilter === 'REJECTED' }]"
        @click="setFilter('REJECTED')"
      >
        已拒绝
      </button>
    </div>

    <div class="suggestions-list">
      <div 
        v-for="suggestion in filteredSuggestions" 
        :key="suggestion.id"
        class="suggestion-item"
      >
        <div class="suggestion-content">
          <div class="suggestion-header">
            <span class="suggestion-id">建议ID: {{ suggestion.id }}</span>
            <div class="suggestion-status" :class="suggestion.suggestionStatus?.toLowerCase()">
              {{ getStatusText(suggestion.suggestionStatus) }}
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
        <div class="suggestion-actions" v-if="suggestion.suggestionStatus === 'PENDING'">
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
import { getAllSuggestions, updateSuggestionStatus } from '@/api/prediction/suggestionService';

export default {
  name: 'DispatchSuggestionPanel',
  data() {
    return {
      searchQuery: '',
      currentFilter: 'PENDING',
      suggestions: []
    }
  },
  computed: {
    filteredSuggestions() {
      return this.suggestions
        .filter(suggestion => {
          const matchesSearch = this.searchQuery === '' ||
            suggestion.id.toString().includes(this.searchQuery) ||
            suggestion.sourceGeohash.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            suggestion.targetGeohash.toLowerCase().includes(this.searchQuery.toLowerCase());
          
          const matchesFilter = this.currentFilter === suggestion.suggestionStatus;
          
          return matchesSearch && matchesFilter;
        });
    }
  },
  methods: {
    setFilter(filter) {
      this.currentFilter = filter;
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'ACCEPTED': '已接受',
        'REJECTED': '已拒绝'
      };
      return statusMap[status] || status;
    },
    async acceptSuggestion(suggestion) {
      try {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
          alert('登录已过期，请重新登录');
          return;
        }
        
        console.log('开始接受建议，ID:', suggestion.id, '状态: ACCEPTED');
        console.log('当前token:', token ? '已设置' : '未设置');
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
        
        console.log('开始拒绝建议，ID:', suggestion.id, '状态: REJECTED');
        console.log('当前token:', token ? '已设置' : '未设置');
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
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      const date = new Date(dateTimeStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    async loadSuggestions() {
      try {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
          console.warn('未找到登录token，无法加载调度建议');
          this.suggestions = [];
          return;
        }
        
        console.log('开始加载调度建议...');
        console.log('当前token:', token ? '已设置' : '未设置');
        const response = await getAllSuggestions();
        console.log('获取建议API响应:', response);
        
        if (response && (response.code === 200 || response.code === '200') && response.data) {
          this.suggestions = response.data;
          console.log('成功加载建议数量:', this.suggestions.length);
        } else {
          console.warn('获取调度建议失败 - API响应:', response);
          console.warn('响应code:', response?.code, '响应msg:', response?.msg);
          this.suggestions = [];
        }
      } catch (error) {
        console.error('获取调度建议出错 - 完整错误:', error);
        console.error('错误响应:', error.response);
        if (error.response) {
          console.error('HTTP状态码:', error.response.status);
          console.error('错误数据:', error.response.data);
        }
        this.suggestions = [];
      }
    }
  },
  async mounted() {
    await this.loadSuggestions();
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

.filter-buttons {
  display: flex;
  gap: 8px;
}

.filter-btn {
  flex: 1;
  padding: 8px;
  border: none;
  border-radius: 6px;
  background: #f5f5f5;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn.active {
  background: #FFD600;
  color: #000;
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