<template>
  <div class="task-query-panel">
    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-row">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索任务..." 
          class="search-input"
          @input="onSearchInput"
        />
      </div>
      <div class="filter-row">
        <div class="filter-label">状态筛选:</div>
        <div class="filter-buttons">
          <button 
            class="filter-btn" 
            :class="{ active: currentFilter === 'all' }"
            @click="setFilter('all')"
          >
            全部
          </button>
          <button 
            class="filter-btn" 
            :class="{ active: currentFilter === 'unprocessed' }"
            @click="setFilter('unprocessed')"
          >
            未处理
          </button>
          <button 
            class="filter-btn" 
            :class="{ active: currentFilter === 'processing' }"
            @click="setFilter('processing')"
          >
            处理中
          </button>
          <button 
            class="filter-btn" 
            :class="{ active: currentFilter === 'completed' }"
            @click="setFilter('completed')"
          >
            处理完成
          </button>
        </div>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="task-list">
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载任务数据中...</p>
      </div>
      <div v-else-if="filteredTasks.length === 0" class="no-tasks">
        <p>{{ hasSearched ? '没有找到符合条件的任务' : '暂无任务数据' }}</p>
      </div>
      <div v-else class="task-scroll-container">
        <div 
          v-for="(task, index) in filteredTasks" 
          :key="task.id || index"
          class="task-item"
        >
          <div class="task-header">
            <span class="task-id">任务ID: {{ task.taskId || task.id || `临时ID_${index + 1}` }}</span>
            <span class="task-status" :class="getStatusClass(task.status)">
              {{ getStatusText(task.status) }}
            </span>
          </div>
          <div class="task-details">
            <div class="detail-row">
              <span class="label">起点区域:</span>
              <span class="value">{{ task.startGeohash || task.sourceGeohash }}</span>
            </div>
            <div class="detail-row">
              <span class="label">终点区域:</span>
              <span class="value">{{ task.endGeohash || task.targetGeohash }}</span>
            </div>
            <div class="detail-row">
              <span class="label">调度数量:</span>
              <span class="value">{{ task.bikeCount || task.dispatchCount || 0 }} 辆</span>
            </div>
            <div class="detail-row">
              <span class="label">执行人员:</span>
              <span class="value">{{ task.assignedTo || task.staffName || task.staffId || '未分配' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">创建时间:</span>
              <span class="value">{{ formatDateTime(task.createdAt || task.createTime || task.createDate) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">完成时间:</span>
              <span class="value">{{ formatDateTime(task.completedAt || task.completeTime || task.completeDate) }}</span>
            </div>
            <div v-if="task.description" class="detail-row">
              <span class="label">任务描述:</span>
              <span class="value description">{{ task.description }}</span>
            </div>
          </div>
          <div class="task-actions">
            <button 
              class="delete-btn" 
              @click="deleteTask(task.taskId || task.id)"
              :disabled="isDeleting"
            >
              {{ isDeleting ? '删除中...' : '删除' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { 
  getAllDispatchTasks, 
  getUnprocessedTasks, 
  getProcessingTasks, 
  getCompletedTasks,
  deleteDispatchTask
} from '@/api/assignment/task';

export default {
  name: 'TaskQueryPanel',
  data() {
    return {
      searchQuery: '',
      currentFilter: 'all',
      tasks: [],
      isLoading: false,
      hasSearched: false,
      isDeleting: false
    }
  },
  computed: {
    filteredTasks() {
      console.log('计算过滤后的任务，原始任务数量:', this.tasks.length);
      
      let filtered = this.tasks;
      
      // 搜索过滤
      if (this.searchQuery) {
        filtered = filtered.filter(task => {
          const searchLower = this.searchQuery.toLowerCase();
          return (task.taskId || task.id)?.toString().includes(searchLower) ||
                 (task.startGeohash || task.sourceGeohash)?.toLowerCase().includes(searchLower) ||
                 (task.endGeohash || task.targetGeohash)?.toLowerCase().includes(searchLower) ||
                 task.assignedTo?.toString().includes(searchLower) ||
                 task.staffName?.toLowerCase().includes(searchLower) ||
                 task.staffId?.toString().includes(searchLower) ||
                 task.description?.toLowerCase().includes(searchLower);
        });
      }
      
      console.log('过滤后的任务数量:', filtered.length);
      return filtered;
    }
  },
  mounted() {
    this.loadTasks();
  },
  methods: {
    getStatusClass(status) {
      const statusMap = {
        '未处理': 'unprocessed',
        '处理中': 'processing',
        '处理完成': 'completed',
        'UNPROCESSED': 'unprocessed',
        'PROCESSING': 'processing',
        'COMPLETED': 'completed'
      };
      return statusMap[status] || 'default';
    },
    getStatusText(status) {
      // API返回的是中文状态，直接返回
      return status || '未知状态';
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    async loadTasks() {
      this.isLoading = true;
      this.hasSearched = true;
      
      try {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
          alert('登录已过期，请重新登录');
          this.isLoading = false;
          return;
        }

        console.log('开始加载任务数据，当前筛选:', this.currentFilter);
        
        let response;
        switch (this.currentFilter) {
          case 'unprocessed':
            response = await getUnprocessedTasks();
            break;
          case 'processing':
            response = await getProcessingTasks();
            break;
          case 'completed':
            response = await getCompletedTasks();
            break;
          default:
            response = await getAllDispatchTasks();
            break;
        }
        
        console.log('任务数据API响应:', response);

        // 处理不同的响应格式
        if (response && Array.isArray(response)) {
          // API直接返回数组
          this.tasks = response;
          console.log('成功获取任务数据数量:', this.tasks.length);
          
          if (this.tasks.length > 0) {
            console.log(`成功获取到 ${this.tasks.length} 条任务数据`);
          } else {
            console.log('当前筛选条件下暂无任务数据');
          }
        } else if (response && (response.code === 200 || response.code === '200')) {
          // API返回包装格式 {code: 200, data: [...]}
          this.tasks = response.data || [];
          console.log('成功获取任务数据数量:', this.tasks.length);
          
          if (this.tasks.length > 0) {
            console.log(`成功获取到 ${this.tasks.length} 条任务数据`);
          } else {
            console.log('当前筛选条件下暂无任务数据');
          }
        } else {
          console.warn('获取任务数据失败 - API响应:', response);
          this.tasks = [];
          alert(`获取任务数据失败: ${response?.msg || '未知错误'}`);
        }
      } catch (error) {
        console.error('获取任务数据出错:', error);
        if (error.response) {
          alert(`获取任务数据失败: HTTP ${error.response.status} - ${error.response.data?.msg || error.message}`);
        } else {
          alert(`获取任务数据失败: ${error.message}`);
        }
        this.tasks = [];
      } finally {
        this.isLoading = false;
      }
    },
    setFilter(filter) {
      this.currentFilter = filter;
      this.searchQuery = ''; // 清空搜索框
      this.loadTasks();
    },
    onSearchInput() {
      // 搜索功能通过computed属性自动处理
      console.log('搜索关键词:', this.searchQuery);
    },
    async deleteTask(taskId) {
      if (!taskId) {
        alert('任务ID无效');
        return;
      }

      if (!confirm(`确定要删除任务ID为 ${taskId} 的任务吗？`)) {
        return;
      }

      this.isDeleting = true;
      try {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
          alert('登录已过期，请重新登录');
          this.isDeleting = false;
          return;
        }

        console.log('开始删除任务:', taskId);
        const response = await deleteDispatchTask(taskId);
        console.log('删除任务API响应:', response);

        if (response && (response.code === 200 || response.code === '200')) {
          // 从本地列表中移除该任务
          this.tasks = this.tasks.filter(task => (task.taskId || task.id) !== taskId);
          alert('任务删除成功！');
        } else {
          console.warn('删除任务失败 - API响应:', response);
          alert(`删除任务失败: ${response?.msg || '未知错误'}`);
        }
      } catch (error) {
        console.error('删除任务出错:', error);
        if (error.response) {
          alert(`删除任务失败: HTTP ${error.response.status} - ${error.response.data?.msg || error.message}`);
        } else {
          alert(`删除任务失败: ${error.message}`);
        }
      } finally {
        this.isDeleting = false;
      }
    }
  }
};
</script>

<style scoped>
.task-query-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  padding: 12px;
}

.search-filter-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.filter-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  min-width: 70px;
}

.filter-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 4px 8px;
  font-size: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  background: #f0f0f0;
  color: #333;
}

.filter-btn.active {
  background: #FFD600;
  color: #333;
  border-color: #FFD600;
  font-weight: 600;
}

.task-list {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: #666;
  font-size: 0.9rem;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #FFD600;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-tasks {
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

.task-scroll-container {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-item {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.task-id {
  font-weight: 600;
  font-size: 0.9rem;
  color: #333;
}

.task-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.task-status.unprocessed {
  background: #fff3cd;
  color: #856404;
}

.task-status.processing {
  background: #d1ecf1;
  color: #0c5460;
}

.task-status.completed {
  background: #d4edda;
  color: #155724;
}

.task-status.default {
  background: #f8f9fa;
  color: #6c757d;
}

.task-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 0.85rem;
}

.detail-row .label {
  color: #666;
  font-weight: 500;
  min-width: 80px;
}

.detail-row .value {
  color: #333;
  font-weight: 600;
  text-align: right;
  flex: 1;
  margin-left: 8px;
}

.detail-row .description {
  font-weight: normal;
  color: #555;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.delete-btn {
  padding: 4px 12px;
  font-size: 0.8rem;
  border: 1px solid #dc3545;
  border-radius: 4px;
  background: white;
  color: #dc3545;
  cursor: pointer;
  transition: all 0.2s;
}

.delete-btn:hover:not(:disabled) {
  background: #dc3545;
  color: white;
}

.delete-btn:disabled {
  background: #f8f9fa;
  color: #ccc;
  cursor: not-allowed;
  border-color: #ccc;
}

/* 滚动条样式 */
.task-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.task-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.task-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.task-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 