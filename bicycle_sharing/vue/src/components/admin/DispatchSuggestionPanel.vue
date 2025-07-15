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
        :class="['filter-btn', { active: currentFilter === 'pending' }]"
        @click="setFilter('pending')"
      >
        待处理
      </button>
      <button 
        :class="['filter-btn', { active: currentFilter === 'accepted' }]"
        @click="setFilter('accepted')"
      >
        已接受
      </button>
      <button 
        :class="['filter-btn', { active: currentFilter === 'rejected' }]"
        @click="setFilter('rejected')"
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
            <div class="suggestion-status" :class="suggestion.status">
              {{ getStatusText(suggestion.status) }}
            </div>
          </div>
          <div class="suggestion-details">
            <div class="detail-row">
              <span class="label">起点:</span>
              <span class="value">{{ suggestion.startArea }}</span>
            </div>
            <div class="detail-row">
              <span class="label">终点:</span>
              <span class="value">{{ suggestion.endArea }}</span>
            </div>
            <div class="detail-row">
              <span class="label">调度数量:</span>
              <span class="value">{{ suggestion.amount }} 辆</span>
            </div>
          </div>
        </div>
        <div class="suggestion-actions" v-if="suggestion.status === 'pending'">
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
export default {
  name: 'DispatchSuggestionPanel',
  data() {
    return {
      searchQuery: '',
      currentFilter: 'pending',
      suggestions: [
        {
          id: 'DS001',
          startArea: 'ws105qy',
          endArea: 'ws10547',
          amount: 5,
          status: 'pending'
        },
        {
          id: 'DS002',
          startArea: 'ws105w5',
          endArea: 'ws105r6',
          amount: 3,
          status: 'pending'
        },
        {
          id: 'DS003',
          startArea: 'ws105wc',
          endArea: 'ws105qy',
          amount: 4,
          status: 'pending'
        },
        {
          id: 'DS004',
          startArea: 'ws10547',
          endArea: 'ws105w5',
          amount: 6,
          status: 'pending'
        },
        {
          id: 'DS005',
          startArea: 'ws105r6',
          endArea: 'ws105wc',
          amount: 2,
          status: 'pending'
        },
        {
          id: 'DS006',
          startArea: 'ws105qy',
          endArea: 'ws105w5',
          amount: 7,
          status: 'pending'
        },
        {
          id: 'DS007',
          startArea: 'ws105wc',
          endArea: 'ws10547',
          amount: 3,
          status: 'pending'
        },
        {
          id: 'DS008',
          startArea: 'ws105w5',
          endArea: 'ws105wc',
          amount: 4,
          status: 'pending'
        },
        {
          id: 'DS009',
          startArea: 'ws10547',
          endArea: 'ws105r6',
          amount: 5,
          status: 'pending'
        },
        {
          id: 'DS010',
          startArea: 'ws105r6',
          endArea: 'ws105qy',
          amount: 6,
          status: 'pending'
        }
      ]
    }
  },
  computed: {
    filteredSuggestions() {
      return this.suggestions
        .filter(suggestion => {
          const matchesSearch = this.searchQuery === '' ||
            suggestion.id.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            suggestion.startArea.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            suggestion.endArea.toLowerCase().includes(this.searchQuery.toLowerCase());
          
          const matchesFilter = this.currentFilter === suggestion.status;
          
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
        pending: '待处理',
        accepted: '已接受',
        rejected: '已拒绝'
      };
      return statusMap[status] || status;
    },
    acceptSuggestion(suggestion) {
      suggestion.status = 'accepted';
      this.$emit('suggestion-accepted', suggestion);
    },
    rejectSuggestion(suggestion) {
      suggestion.status = 'rejected';
      this.$emit('suggestion-rejected', suggestion);
    },
    filterSuggestions() {
      // 实时搜索过滤已经通过计算属性实现
    }
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
  width: 320px;
  height: 500px;
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
  height: 380px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 8px;
  margin-right: -8px;
}

/* Customize scrollbar */
.suggestions-list::-webkit-scrollbar {
  width: 4px;
}

.suggestions-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.suggestions-list::-webkit-scrollbar-thumb {
  background: #FFD600;
  border-radius: 2px;
}

.suggestions-list::-webkit-scrollbar-thumb:hover {
  background: #e6c100;
}

.suggestion-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.suggestion-content {
  margin-bottom: 12px;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.suggestion-id {
  font-weight: bold;
  color: #333;
}

.suggestion-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.suggestion-status.pending {
  background: #fff3e0;
  color: #e65100;
}

.suggestion-status.accepted {
  background: #e8f5e9;
  color: #2e7d32;
}

.suggestion-status.rejected {
  background: #ffebee;
  color: #c62828;
}

.suggestion-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-row {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.detail-row .label {
  color: #666;
  min-width: 70px;
}

.detail-row .value {
  color: #333;
  font-weight: 500;
}

.suggestion-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.accept {
  background: #e8f5e9;
  color: #2e7d32;
}

.action-btn.accept:hover {
  background: #c8e6c9;
}

.action-btn.reject {
  background: #ffebee;
  color: #c62828;
}

.action-btn.reject:hover {
  background: #ffcdd2;
}
</style> 