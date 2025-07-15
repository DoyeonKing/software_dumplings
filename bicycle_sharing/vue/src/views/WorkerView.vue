<template>
  <div class="worker-dashboard">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-left">
        <h2>调度管理系统</h2>
        <div class="nav-menu">
          <div
            class="nav-item"
            :class="{ active: showWorkbench }"
            @click="toggleWorkbench"
          >
            调度工作台
          </div>
        </div>
      </div>
      <div class="nav-right">
        <el-dropdown trigger="click" class="map-display-dropdown">
          <div class="nav-item">
            地图显示
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>
                <el-switch
                  v-model="showBicycles"
                  active-text="显示单车"
                />
              </el-dropdown-item>
              <el-dropdown-item>
                <el-switch
                  v-model="showParkingAreas"
                  active-text="显示停车点"
                />
              </el-dropdown-item>
              <el-dropdown-item>
                <el-switch
                  v-model="showHeatmap"
                  active-text="显示热力图"
                />
              </el-dropdown-item>
              <el-dropdown-item divided @click="showMapSettings = true">
                地图设置
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown>
          <span class="user-profile">
            <el-avatar :size="32" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath fill='%23666' d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z'/%3E%3C/svg%3E" />
            <span class="username">工作人员</span>
            <el-icon><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人信息</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 地图设置面板 -->
    <div class="map-settings" v-if="showMapSettings" :class="{ hidden: hideUI }">
      <div class="settings-header">
        <h3>地图设置</h3>
        <div class="close-button" @click="showMapSettings = false">×</div>
      </div>
      <div class="settings-content">
        <!-- 地图样式 -->
        <div class="settings-section">
          <h4>地图样式</h4>
          <div class="style-options">
            <div class="style-option"
                 v-for="style in mapStyles"
                 :key="style.value"
                 :class="{ active: currentMapStyle === style.value }"
                 @click="updateMapStyle(style.value)">
              {{ style.label }}
            </div>
          </div>
        </div>

        <!-- 缩放控制 -->
        <div class="settings-section">
          <h4>缩放控制</h4>
          <div class="zoom-controls">
            <button @click="handleZoom('in')">放大</button>
            <button @click="handleZoom('out')">缩小</button>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧工作台 -->
      <div class="workbench" :class="{ collapsed: !showWorkbench }">
        <div class="collapse-button" @click="toggleWorkbench">
          <el-icon>
            <component :is="showWorkbench ? 'ArrowLeft' : 'ArrowRight'" />
          </el-icon>
        </div>

        <!-- 任务列表 -->
        <div class="task-list">
          <div class="panel-header">
            <h3>调度任务</h3>
            <div class="header-info">
              <span class="current-worker-info">
                当前查看：员工ID {{ selectedWorkerId }}
              </span>
              <el-button type="primary" size="small" @click="refreshTasks" :loading="tasksLoading">
                刷新
              </el-button>
            </div>
          </div>

          <!-- 任务过滤器 -->
          <div class="task-filters">
            <el-radio-group v-model="taskFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="未处理">未处理</el-radio-button>
              <el-radio-button label="处理中">处理中</el-radio-button>
              <el-radio-button label="处理完成">处理完成</el-radio-button>
            </el-radio-group>
          </div>
          
          <!-- 员工ID选择 (测试阶段) -->
          <div class="worker-id-selector">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-input-number 
                  v-model="selectedWorkerId" 
                  placeholder="员工ID" 
                  size="small" 
                  :min="1"
                  controls-position="right"
                  style="width: 100%"
                  @keyup.enter="refreshTasksForWorker"
                />
              </el-col>
              <el-col :span="12">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="refreshTasksForWorker"
                  :loading="tasksLoading"
                  style="width: 100%"
                >
                  查看该员工任务
                </el-button>
              </el-col>
            </el-row>
            
            <!-- 快捷选择常用员工ID -->
            <div class="quick-select-workers" style="margin-top: 8px;">
              <span style="font-size: 12px; color: #666; margin-right: 8px;">快捷选择：</span>
              <el-button 
                v-for="workerId in commonWorkerIds" 
                :key="workerId"
                size="small"
                type="text"
                @click="selectWorkerQuick(workerId)"
                :style="{ 
                  color: selectedWorkerId === workerId ? '#409EFF' : '#666',
                  fontWeight: selectedWorkerId === workerId ? 'bold' : 'normal'
                }"
              >
                员工{{ workerId }}
              </el-button>
            </div>
            
            <el-alert 
              title="测试模式" 
              type="info" 
              size="small" 
              :closable="false"
              style="margin-top: 8px"
            >
              当前为测试阶段，可以查看指定员工ID的调度任务
            </el-alert>
          </div>

          <!-- 高级筛选 -->
          <div class="advanced-filters">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-select v-model="dateFilter" placeholder="时间筛选" size="small" clearable>
                  <el-option label="全部" value="all"></el-option>
                  <el-option label="今天" value="today"></el-option>
                  <el-option label="昨天" value="yesterday"></el-option>
                  <el-option label="最近7天" value="week"></el-option>
                  <el-option label="最近30天" value="month"></el-option>
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-input 
                  v-model="searchKeyword" 
                  placeholder="搜索任务ID或地点" 
                  size="small" 
                  clearable
                  @input="handleSearch"
                />
              </el-col>
              <el-col :span="8">
                <el-select v-model="sortBy" placeholder="排序方式" size="small">
                  <el-option label="创建时间(最新)" value="createdAt-desc"></el-option>
                  <el-option label="创建时间(最旧)" value="createdAt-asc"></el-option>
                  <el-option label="完成时间" value="completedAt-desc"></el-option>
                  <el-option label="单车数量" value="bikeCount-desc"></el-option>
                </el-select>
              </el-col>
            </el-row>
          </div>

          <!-- 任务卡片列表 -->
          <div class="task-cards">
            <div v-if="tasksLoading" class="loading-tasks">
              <el-skeleton 
                v-for="i in 3" 
                :key="i" 
                :loading="true" 
                animated
              >
                <template #default>
                  <el-card class="task-card">
                    <div class="task-card-header">
                      <span class="task-id">任务 #--</span>
                      <el-tag size="small">--</el-tag>
                    </div>
                    <div class="task-card-content">
                      <div class="task-info-row">
                        <span class="info-label">出发地：</span>
                        <span class="info-value">--</span>
                      </div>
                      <div class="task-info-row">
                        <span class="info-label">目的地：</span>
                        <span class="info-value">--</span>
                      </div>
                    </div>
                  </el-card>
                </template>
              </el-skeleton>
            </div>
            
            <div v-else-if="filteredTasks.length === 0" class="empty-tasks">
              <el-empty description="暂无调度任务">
                <template #description>
                  <div style="color: #999; font-size: 14px;">
                    <p>当前员工ID {{ selectedWorkerId }} 暂无调度任务</p>
                    <p>请尝试切换其他员工ID或联系管理员</p>
                  </div>
                </template>
              </el-empty>
            </div>
            
            <el-card
              v-for="task in filteredTasks"
              :key="task.taskId"
              class="task-card"
              :class="{ 'selected': selectedTaskId === task.taskId }"
              @click="selectTask(task)"
            >
              <div class="task-card-header">
                <span class="task-id">任务 #{{ task.taskId }}</span>
                <el-tag
                  :type="getTaskStatusType(task.status)"
                  size="small"
                >
                  {{ task.status }}
                </el-tag>
              </div>
              <div class="task-card-content">
                <div class="task-info-row">
                  <span class="info-label">出发地：</span>
                  <span class="info-value">{{ task.startGeohash }}</span>
                </div>
                <div class="task-info-row">
                  <span class="info-label">目的地：</span>
                  <span class="info-value">{{ task.endGeohash }}</span>
                </div>
                <div class="task-info-row">
                  <span class="info-label">运输数量：</span>
                  <span class="info-value">{{ task.bikeCount }} 辆</span>
                </div>
                <div class="task-info-row">
                  <span class="info-label">创建时间：</span>
                  <span class="info-value">{{ formatDateTime(task.createdAt) }}</span>
                </div>
                <div v-if="task.completedAt" class="task-info-row">
                  <span class="info-label">完成时间：</span>
                  <span class="info-value">{{ formatDateTime(task.completedAt) }}</span>
                </div>
                <div class="task-actions">
                  <el-button
                    type="primary"
                    size="small"
                    :icon="Location"
                    @click.stop="navigateToTask(task)"
                  >
                    导航该路线
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="statistics">
          <div class="panel-header">
            <h3>显示任务统计</h3>
          </div>
          <div class="stat-cards">
            <el-card class="stat-card">
              <template #header>
                <div class="stat-header">待处理任务</div>
              </template>
              <div class="stat-number">{{ filteredStats.pending }}</div>
            </el-card>
            <el-card class="stat-card">
              <template #header>
                <div class="stat-header">进行中任务</div>
              </template>
              <div class="stat-number">{{ filteredStats.inProgress }}</div>
            </el-card>
            <el-card class="stat-card">
              <template #header>
                <div class="stat-header">已完成任务</div>
              </template>
              <div class="stat-number">{{ filteredStats.completed }}</div>
            </el-card>
          </div>
        </div>
      </div>

      <!-- 右侧地图区域 -->
      <div class="right-panel" :class="{ 'expanded': !showWorkbench }">
        <WorkerMapComponent
          :selectedTaskId="selectedTaskId"
          :showBicycles="showBicycles"
          :showParkingAreas="showParkingAreas"
          :showHeatmap="showHeatmap"
          @update:selectedTaskId="selectedTaskId = $event"
          ref="mapComponentRef"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ArrowLeft, ArrowRight, ArrowDown, Location } from '@element-plus/icons-vue';
import WorkerMapComponent from '@/components/map/WorkerMapComponent.vue';
import { getAllTasks, getDispatchTasksByStaff } from '@/api/assignment/task';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

// 用户认证信息
const authToken = ref('')
const userInfo = ref(null)
const userRole = ref('')

// 状态
const showWorkbench = ref(true); // 默认显示工作台
const showBicycles = ref(false);
const showParkingAreas = ref(false);
const showHeatmap = ref(false);
const showMapSettings = ref(false);
const currentMapStyle = ref('normal');
const hideUI = ref(false);
const mapComponentRef = ref(null);
const taskFilter = ref('all');
const selectedTaskId = ref(null);
const tasks = ref([]);
const stats = ref({
  pending: 0,
  inProgress: 0,
  completed: 0
});

// 高级筛选变量
const dateFilter = ref('all'); // 默认显示全部任务
const searchKeyword = ref('');
const sortBy = ref('createdAt-desc');

// 测试阶段变量
const selectedWorkerId = ref(3); // 默认员工ID为3
const tasksLoading = ref(false);
const commonWorkerIds = ref([1, 2, 3, 4, 5]); // 常用员工ID

// 地图样式选项
const mapStyles = [
  { label: '标准', value: 'normal' },
  { label: '幻影黑', value: 'dark' },
  { label: '月光银', value: 'light' },
  { label: '远山黛', value: 'whitesmoke' },
  { label: '草色青', value: 'fresh' },
  { label: '雅士灰', value: 'grey' }
];

// 切换工作台显示
const toggleWorkbench = () => {
  showWorkbench.value = !showWorkbench.value;
};

// 根据过滤器筛选任务
const filteredTasks = computed(() => {
  let filtered = tasks.value;
  
  // 状态筛选
  if (taskFilter.value !== 'all') {
    filtered = filtered.filter(task => task.status === taskFilter.value);
  }
  
  // 时间筛选
  if (dateFilter.value && dateFilter.value !== 'all') {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    
    filtered = filtered.filter(task => {
      const taskDate = new Date(task.createdAt);
      switch (dateFilter.value) {
        case 'today':
          return taskDate >= today;
        case 'yesterday':
          const yesterday = new Date(today);
          yesterday.setDate(yesterday.getDate() - 1);
          return taskDate >= yesterday && taskDate < today;
        case 'week':
          const weekAgo = new Date(today);
          weekAgo.setDate(weekAgo.getDate() - 7);
          return taskDate >= weekAgo;
        case 'month':
          const monthAgo = new Date(today);
          monthAgo.setDate(monthAgo.getDate() - 30);
          return taskDate >= monthAgo;
        default:
          return true;
      }
    });
  }
  
  // 关键字搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(task => 
      task.taskId.toString().includes(keyword) ||
      task.startGeohash.toLowerCase().includes(keyword) ||
      task.endGeohash.toLowerCase().includes(keyword)
    );
  }
  
  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'createdAt-desc':
        return new Date(b.createdAt) - new Date(a.createdAt);
      case 'createdAt-asc':
        return new Date(a.createdAt) - new Date(b.createdAt);
      case 'completedAt-desc':
        const aCompleted = a.completedAt ? new Date(a.completedAt) : new Date(0);
        const bCompleted = b.completedAt ? new Date(b.completedAt) : new Date(0);
        return bCompleted - aCompleted;
      case 'bikeCount-desc':
        return b.bikeCount - a.bikeCount;
      default:
        return 0;
    }
  });
  
  return filtered;
});

// 计算当前筛选显示的任务统计
const filteredStats = computed(() => {
  return {
    pending: filteredTasks.value.filter(t => t.status === '未处理').length,
    inProgress: filteredTasks.value.filter(t => t.status === '处理中').length,
    completed: filteredTasks.value.filter(t => t.status === '处理完成').length
  };
});

// 任务状态样式映射
const getTaskStatusType = (status) => {
  const statusMap = {
    '未处理': 'warning',
    '处理中': 'primary',
    '处理完成': 'success'
  };
  return statusMap[status] || 'info';
};

// 选择任务
const selectTask = (task) => {
  selectedTaskId.value = task.taskId;
  // 如果工作台是收起状态，则展开
  if (!showWorkbench.value) {
    showWorkbench.value = true;
  }
};

// 刷新任务列表
const refreshTasks = async () => {
  await refreshTasksForWorker();
};

// 刷新指定员工的任务列表
const refreshTasksForWorker = async () => {
  if (!selectedWorkerId.value) {
    ElMessage.error('请输入员工ID');
    return;
  }
  
  tasksLoading.value = true;
  try {
    const response = await getDispatchTasksByStaff(selectedWorkerId.value);
    if (response.code === 200 || response.code === '200') {
      tasks.value = response.data || [];
      updateStats();
      ElMessage.success(`已获取员工ID ${selectedWorkerId.value} 的调度任务`);
    } else {
      ElMessage.error(response.msg || '获取调度任务失败');
    }
  } catch (error) {
    console.error('刷新任务列表失败：', error);
    ElMessage.error('刷新任务列表失败');
  } finally {
    tasksLoading.value = false;
  }
};

// 更新统计信息
const updateStats = () => {
  stats.value = {
    pending: tasks.value.filter(t => t.status === '未处理').length,
    inProgress: tasks.value.filter(t => t.status === '处理中').length,
    completed: tasks.value.filter(t => t.status === '处理完成').length
  };
};

// 更新地图样式
const updateMapStyle = (style) => {
  currentMapStyle.value = style;
  if (mapComponentRef.value) {
    mapComponentRef.value.updateMapStyle(style);
  }
};

// 处理缩放
const handleZoom = (type) => {
  if (!mapComponentRef.value) return;
  if (type === 'in') {
    mapComponentRef.value.zoomIn();
  } else {
    mapComponentRef.value.zoomOut();
  }
};

// 退出登录
const handleLogout = () => {
  // 清除登录状态
  localStorage.removeItem('worker_token');
  localStorage.removeItem('worker_info');
  // 跳转到登录页
  router.push('/login');
};

// 导航到任务路线
const navigateToTask = (task) => {
  // TODO: 实现导航功能
  ElMessage.info('导航功能开发中...');
};

// 格式化时间显示
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  try {
    const date = new Date(dateTime);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (error) {
    return dateTime;
  }
};

// 处理搜索
const handleSearch = () => {
  // 搜索是实时的，由computed属性处理
};

// 快捷选择员工ID
const selectWorkerQuick = (workerId) => {
  selectedWorkerId.value = workerId;
  refreshTasksForWorker();
};

// 初始化
onMounted(() => {
  // 获取存储的认证信息
  authToken.value = sessionStorage.getItem('authToken') || ''
  const storedUserInfo = sessionStorage.getItem('userInfo')
  
  // 修复JSON解析错误 - 检查是否为有效的JSON字符串
  if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
    try {
      userInfo.value = JSON.parse(storedUserInfo)
    } catch (e) {
      console.error('解析用户信息失败:', e)
      userInfo.value = null
      // 清除无效的sessionStorage数据
      sessionStorage.removeItem('userInfo')
    }
  }
  
  userRole.value = sessionStorage.getItem('userRole') || ''
  
  // 如果没有token，重定向到登录页
  if (!authToken.value) {
    router.push('/login')
    return
  }
  
  // 检查用户角色是否为worker
  if (userRole.value !== 'worker') {
    ElMessage.error('权限不足，请使用工作人员账号登录')
    router.push('/login')
    return
  }
  
  refreshTasks();
});
</script>

<style scoped>
.worker-dashboard {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-nav {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.nav-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
  white-space: nowrap;
}

.nav-menu {
  display: flex;
  gap: 20px;
}

.nav-item {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #666;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.workbench {
  position: relative; /* 确保相对定位 */
  width: 480px; /* 增大宽度从360px到480px */
  height: 100%;
  background: #fff;
  border-right: 1px solid #eee;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workbench.collapsed {
  width: 0;
  padding: 0;
  border: none;
}

.collapse-button {
  position: absolute;
  right: -24px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 48px;
  background: #fff;
  border: 1px solid #eee;
  border-left: none; /* 移除左边框 */
  border-radius: 0 24px 24px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100; /* 确保按钮在最上层 */
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.1);
}

.collapse-button:hover {
  background: #f5f7fa;
}

.right-panel {
  flex: 1;
  overflow: hidden;
  transition: margin-left 0.3s ease;
}

.right-panel.expanded {
  margin-left: -480px; /* 调整以匹配新的工作台宽度 */
}

/* 滑动动画 */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(-100%);
}

/* 其他样式保持不变 */
.panel-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-worker-info {
  font-size: 12px;
  color: #666;
  background-color: #f0f2f5;
  padding: 4px 8px;
  border-radius: 12px;
  white-space: nowrap;
}

.task-filters {
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.worker-id-selector {
  padding: 20px; /* 增加内边距 */
  background-color: #fff8e1;
  border-bottom: 1px solid #eee;
}

.worker-id-selector .el-row {
  align-items: center;
}

.quick-select-workers {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.quick-select-workers .el-button {
  padding: 2px 8px;
  min-height: 24px;
  font-size: 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.quick-select-workers .el-button:hover {
  background-color: #ecf5ff;
}

.empty-tasks {
  padding: 60px 20px; /* 增加空状态的内边距 */
  text-align: center;
}

.loading-tasks {
  padding: 16px;
}

.advanced-filters {
  padding: 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.advanced-filters .el-row {
  align-items: center;
}

.task-list {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.task-cards {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0; /* 确保在flex容器中正确滚动 */
}

.task-card {
  margin-bottom: 16px; /* 增加卡片间距 */
  cursor: pointer;
  transition: all 0.3s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.task-card.selected {
  border: 2px solid var(--el-color-primary);
}

.task-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-id {
  font-size: 14px;
  color: #999;
}

.task-description {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
}

.task-time {
  font-size: 12px;
  color: #999;
}

.task-card-content {
  padding: 12px 0; /* 增加内容区域的内边距 */
}

.task-info-row {
  display: flex;
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.4;
}

.info-label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  flex: 1;
}

.task-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.statistics {
  padding-bottom: 16px;
  border-top: 1px solid #eee;
}

.stat-cards {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px; /* 增大间距 */
}

.stat-card {
  text-align: center;
}

.stat-header {
  font-size: 14px;
  color: #666;
}

.stat-number {
  font-size: 24px;
  font-weight: 500;
  color: var(--el-color-primary);
}

/* 地图设置面板样式 */
.map-settings {
  position: absolute;
  top: 80px;
  right: 20px; /* 改为右侧显示 */
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 300px;
  z-index: 1000;
  transition: all 0.3s ease;
}

.map-settings.hidden {
  display: none;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.settings-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-button {
  cursor: pointer;
  font-size: 20px;
  color: #666;
}

.settings-content {
  padding: 16px;
}

.settings-section {
  margin-bottom: 20px;
}

.settings-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.style-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.style-option {
  padding: 8px;
  text-align: center;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.style-option:hover {
  background-color: #f5f7fa;
}

.style-option.active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.zoom-controls {
  display: flex;
  gap: 8px;
}

.zoom-controls button {
  flex: 1;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.zoom-controls button:hover {
  background-color: #f5f7fa;
}
</style> 