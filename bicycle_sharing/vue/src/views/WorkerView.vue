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
              <el-dropdown-item>
                <el-switch
                  v-model="showStaff"
                  active-text="显示工作人员"
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
              <el-dropdown-item @click="showProfileModal">个人信息</el-dropdown-item>
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
            <h3>我的调度任务</h3>
            <div class="header-info">
              <span class="current-worker-info">
                员工ID：{{ currentWorkerId }}
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
                    <p>您暂无调度任务</p>
                    <p>请等待管理员分配任务或联系管理员</p>
                  </div>
                </template>
              </el-empty>
            </div>
            
            <el-card
              v-for="task in filteredTasks"
              :key="task.taskId"
              class="task-card"
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
                  <!-- 任务状态操作按钮组 -->
                  <div class="status-actions">
                    <el-button
                      v-if="task.status === '未处理'"
                      type="success"
                      size="small"
                      @click.stop="handleStartTask(task.taskId)"
                      :loading="taskOperationLoading === task.taskId"
                    >
                      开始任务
                    </el-button>
                    <el-button
                      v-else-if="task.status === '处理中'"
                      type="warning"
                      size="small"
                      @click.stop="handleCompleteTask(task.taskId)"
                      :loading="taskOperationLoading === task.taskId"
                    >
                      完成任务
                    </el-button>
                  </div>
                  
                  <!-- 导航功能按钮组 -->
                  <div class="navigation-actions">
                    <el-button
                      type="info"
                      size="small"
                      @click.stop="showTaskDetailDialog(task)"
                    >
                      查看详情
                    </el-button>
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
          :showBicycles="showBicycles"
          :showParkingAreas="showParkingAreas"
          :showHeatmap="showHeatmap"
          :showStaff="showStaff"
          :workerInitialLocation="workerInitialLocation"
          ref="mapComponentRef"
        />
      </div>
    </div>

    <!-- 个人信息弹窗 -->
    <div v-if="showProfile" class="profile-modal-overlay" @click="closeProfileModal">
      <div class="profile-modal" @click.stop>
        <div class="profile-card">
          <div class="profile-header">
            <div class="profile-avatar">
              <img src="@/components/icons/staff.png" alt="工作人员头像" />
            </div>
            <div>
              <div class="profile-name">{{ profileData?.username || '工作人员' }}</div>
              <div class="profile-username">员工ID：{{ profileData?.staffId || '-' }}</div>
            </div>
            <button class="close-btn" @click="closeProfileModal">×</button>
          </div>

          <div v-if="profileLoading" class="loading-section">
            <div class="loading-spinner"></div>
            <p>正在加载个人信息...</p>
          </div>

          <div v-else-if="profileData" class="profile-content">
            <!-- 工作人员基本信息 -->
            <div class="info-section">
              <h4>工作人员基本信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">员工ID</span>
                  <span class="info-value primary">{{ profileData.staffId }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">用户名</span>
                  <span class="info-value success">{{ profileData.username }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">初始纬度</span>
                  <span class="info-value info">{{ profileData.latitude }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">初始经度</span>
                  <span class="info-value info">{{ profileData.longitude }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Geohash</span>
                  <span class="info-value info">{{ profileData.geohash || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">管理员ID</span>
                  <span class="info-value info">{{ profileData.managerId || '未分配' }}</span>
                </div>
              </div>
            </div>

            <!-- 位置信息卡片 -->
            <div class="info-section">
              <h4>📍 初始位置信息</h4>
              <div class="location-cards">
                <div class="location-card">
                  <div class="location-icon">🌍</div>
                  <div class="location-content">
                    <div class="location-value">{{ profileData.latitude }}</div>
                    <div class="location-label">初始纬度</div>
                  </div>
                </div>
                <div class="location-card">
                  <div class="location-icon">🌐</div>
                  <div class="location-content">
                    <div class="location-value">{{ profileData.longitude }}</div>
                    <div class="location-label">初始经度</div>
                  </div>
                </div>
                <div class="location-card">
                  <div class="location-icon">📍</div>
                  <div class="location-content">
                    <div class="location-value">{{ profileData.geohash || '未设置' }}</div>
                    <div class="location-label">Geohash</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="error-section">
            <p>{{ profileError || '获取个人信息失败' }}</p>
            <button class="retry-btn" @click="fetchWorkerProfile">重试</button>
          </div>

          <div class="button-row">
            <button class="action-btn" @click="closeProfileModal">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 任务详情弹窗 -->
    <el-dialog
      v-model="showTaskDetail"
      title="调度任务详情"
      width="600px"
      :before-close="closeTaskDetailDialog"
    >
      <div v-if="currentTaskDetail" class="task-detail-content">
        <!-- 任务基本信息 -->
        <div class="detail-section">
          <h3>📋 任务信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">任务编号:</span>
              <span class="detail-value">#{{ currentTaskDetail.taskId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">任务状态:</span>
              <el-tag :type="getTaskStatusType(currentTaskDetail.status)" size="small">
                {{ currentTaskDetail.status }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="detail-label">分配给:</span>
              <span class="detail-value">员工ID {{ currentTaskDetail.assignedTo || '未分配' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">运输数量:</span>
              <span class="detail-value">{{ currentTaskDetail.bikeCount }} 辆</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">起点区域:</span>
              <span class="detail-value">{{ currentTaskDetail.startGeohash }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">终点区域:</span>
              <span class="detail-value">{{ currentTaskDetail.endGeohash }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间:</span>
              <span class="detail-value">{{ formatDateTime(currentTaskDetail.createdAt) }}</span>
            </div>
            <div class="detail-item" v-if="currentTaskDetail.completedAt">
              <span class="detail-label">完成时间:</span>
              <span class="detail-value">{{ formatDateTime(currentTaskDetail.completedAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 单车列表 -->
        <div class="detail-section">
          <h3>🚴 调度单车列表</h3>
          <div v-loading="taskDetailLoading" class="bikes-container">
            <div v-if="taskBikes.length === 0 && !taskDetailLoading" class="no-bikes">
              <el-empty 
                description="暂无单车信息" 
                :image-size="80"
              >
                <template #description>
                  <p v-if="currentTaskDetail.status === '未处理'">
                    任务尚未开始，暂无分配的单车
                  </p>
                  <p v-else>
                    未获取到单车信息
                  </p>
                </template>
              </el-empty>
            </div>
            <div v-else class="bikes-grid">
              <div 
                v-for="(bikeId, index) in taskBikes" 
                :key="bikeId"
                class="bike-item"
              >
                <div class="bike-icon">🚲</div>
                <div class="bike-info">
                  <div class="bike-number">单车 #{{ index + 1 }}</div>
                  <div class="bike-id">{{ bikeId }}</div>
                </div>
              </div>
            </div>
            <div v-if="taskBikes.length > 0" class="bikes-summary">
              <el-alert
                :title="`共 ${taskBikes.length} 辆单车`"
                type="info"
                :closable="false"
                show-icon
              >
                <template #default>
                  <p v-if="currentTaskDetail.status === '处理中'">
                    这些单车已被选中用于调度，请前往起点区域取车
                  </p>
                  <p v-else-if="currentTaskDetail.status === '处理完成'">
                    这些单车已成功调度到目标区域
                  </p>
                </template>
              </el-alert>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeTaskDetailDialog">关闭</el-button>
          <el-button 
            v-if="currentTaskDetail?.status === '未处理'"
            type="success" 
            @click="handleStartTask(currentTaskDetail.taskId)"
            :loading="taskOperationLoading === currentTaskDetail?.taskId"
          >
            开始任务
          </el-button>
          <el-button 
            v-else-if="currentTaskDetail?.status === '处理中'"
            type="warning" 
            @click="handleCompleteTask(currentTaskDetail.taskId)"
            :loading="taskOperationLoading === currentTaskDetail?.taskId"
          >
            完成任务
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ArrowLeft, ArrowRight, ArrowDown, Location } from '@element-plus/icons-vue';
import WorkerMapComponent from '@/components/map/WorkerMapComponent.vue';
import { getAllTasks, getDispatchTasksByStaff } from '@/api/assignment/task';
import { startDispatchTask, completeDispatchTask, getDispatchTaskBikes } from '@/api/assignment/wzm_task';
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
const showStaff = ref(false);
const showMapSettings = ref(false);
const currentMapStyle = ref('normal');
const hideUI = ref(false);
const mapComponentRef = ref(null);
const taskFilter = ref('all');
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

// 任务相关变量
const tasksLoading = ref(false);

// 任务操作状态
const taskOperationLoading = ref(null); // 当前正在操作的任务ID

// 任务详情状态
const showTaskDetail = ref(false); // 控制详情弹窗显示
const currentTaskDetail = ref(null); // 当前查看详情的任务
const taskBikes = ref([]); // 任务关联的单车列表
const taskDetailLoading = ref(false); // 加载任务详情状态

// 个人信息弹窗相关
const showProfile = ref(false)
const profileData = ref(null)
const profileLoading = ref(false)
const profileError = ref('')

// 当前工人ID计算属性
const currentWorkerId = computed(() => {
  return userInfo.value?.staffId || '未知'
})

// 工作人员初始位置计算属性
const workerInitialLocation = computed(() => {
  if (userInfo.value?.latitude && userInfo.value?.longitude) {
    return {
      latitude: userInfo.value.latitude,
      longitude: userInfo.value.longitude
    }
  }
  // 默认位置（深圳市中心）
  return {
    latitude: 22.547,
    longitude: 114.085947
  }
})

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



// 刷新任务列表
const refreshTasks = async () => {
  if (!currentWorkerId.value || currentWorkerId.value === '未知') {
    ElMessage.error('无法获取当前工人信息');
    return;
  }
  
  tasksLoading.value = true;
  try {
    const response = await getDispatchTasksByStaff(currentWorkerId.value);
    if (response.code === 200 || response.code === '200') {
      tasks.value = response.data || [];
      updateStats();
      ElMessage.success('已刷新调度任务');
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
  sessionStorage.removeItem('authToken');
  sessionStorage.removeItem('userInfo');
  sessionStorage.removeItem('userRole');
  localStorage.removeItem('worker_token');
  localStorage.removeItem('worker_info');
  // 跳转到登录页
  router.push('/login');
};

// 导航到任务路线
const navigateToTask = (task) => {
  if (!mapComponentRef.value) {
    ElMessage.error('地图组件未准备就绪');
    return;
  }

  // 调用地图组件的导航功能
  mapComponentRef.value.showNavigationRoutes(task);
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



// 开始任务
const handleStartTask = async (taskId) => {
  if (!taskId) {
    ElMessage.error('任务ID无效');
    return;
  }

  try {
    taskOperationLoading.value = taskId;
    
    const result = await startDispatchTask(taskId);
    
    if (result.code === '200' || result.code === 200) {
      ElMessage.success('任务已开始！已为您选择调度单车');
      
      // 显示选中的单车信息
      if (result.data && Array.isArray(result.data) && result.data.length > 0) {
        ElMessage.info({
          message: `已选择 ${result.data.length} 辆单车进行调度`,
          duration: 3000
        });
        console.log('选中的单车ID列表:', result.data);
      }
      
      // 刷新任务列表
      await refreshTasks();
      
      // 如果当前正在查看任务详情，刷新详情内容
      if (showTaskDetail.value && currentTaskDetail.value?.taskId === taskId) {
        const updatedTask = tasks.value.find(t => t.taskId === taskId);
        if (updatedTask) {
          currentTaskDetail.value = updatedTask;
          // 重新获取单车列表
          const bikeResult = await getDispatchTaskBikes(taskId);
          if (bikeResult.code === '200' || bikeResult.code === 200) {
            taskBikes.value = bikeResult.data || [];
          }
        }
      }
    } else {
      ElMessage.error(result.msg || '开始任务失败');
    }
  } catch (error) {
    console.error('开始任务失败:', error);
    ElMessage.error('开始任务失败，请重试');
  } finally {
    taskOperationLoading.value = null;
  }
};

// 完成任务
const handleCompleteTask = async (taskId) => {
  if (!taskId) {
    ElMessage.error('任务ID无效');
    return;
  }

  try {
    taskOperationLoading.value = taskId;
    
    const result = await completeDispatchTask(taskId);
    
    if (result.code === '200' || result.code === 200) {
      ElMessage.success('任务已完成！单车已重新分配到目标区域');
      
      // 刷新任务列表
      await refreshTasks();
      
      // 如果当前正在查看任务详情，刷新详情内容
      if (showTaskDetail.value && currentTaskDetail.value?.taskId === taskId) {
        const updatedTask = tasks.value.find(t => t.taskId === taskId);
        if (updatedTask) {
          currentTaskDetail.value = updatedTask;
        }
      }
    } else {
      ElMessage.error(result.msg || '完成任务失败');
    }
  } catch (error) {
    console.error('完成任务失败:', error);
    ElMessage.error('完成任务失败，请重试');
  } finally {
    taskOperationLoading.value = null;
  }
};

// 显示任务详情
const showTaskDetailDialog = async (task) => {
  if (!task) {
    ElMessage.error('任务信息无效');
    return;
  }

  try {
    taskDetailLoading.value = true;
    currentTaskDetail.value = task;
    
    // 获取任务关联的单车列表
    const result = await getDispatchTaskBikes(task.taskId);
    
    if (result.code === '200' || result.code === 200) {
      taskBikes.value = result.data || [];
    } else {
      console.warn('获取任务单车列表失败:', result.msg);
      taskBikes.value = [];
    }
    
    showTaskDetail.value = true;
  } catch (error) {
    console.error('获取任务详情失败:', error);
    ElMessage.error('获取任务详情失败');
    taskBikes.value = [];
  } finally {
    taskDetailLoading.value = false;
  }
};

// 关闭任务详情
const closeTaskDetailDialog = () => {
  showTaskDetail.value = false;
  currentTaskDetail.value = null;
  taskBikes.value = [];
};

// 个人信息弹窗相关方法
const showProfileModal = () => {
  showProfile.value = true;
  fetchWorkerProfile();
};

const closeProfileModal = () => {
  showProfile.value = false;
  profileData.value = null;
  profileError.value = '';
};

const fetchWorkerProfile = async () => {
  console.log('开始获取工作人员个人信息')
  console.log('使用的token:', authToken.value)
  
  if (!authToken.value) {
    console.error('Token为空，无法获取个人信息')
    profileError.value = '未找到认证令牌，请重新登录';
    return;
  }

  profileLoading.value = true;
  profileError.value = '';
  
  try {
    // 从sessionStorage获取工作人员信息
    const storedUserInfo = sessionStorage.getItem('userInfo')
    if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
      try {
        const userInfo = JSON.parse(storedUserInfo)
        console.log('从sessionStorage获取的工作人员信息:', userInfo)
        
        // 设置role为staff（根据要求）
        userInfo.role = 'staff'
        
        profileData.value = userInfo;
        console.log('个人信息获取成功, 数据:', profileData.value)
      } catch (e) {
        console.error('解析工作人员信息失败:', e)
        profileError.value = '解析工作人员信息失败';
      }
    } else {
      console.error('sessionStorage中没有工作人员信息')
      profileError.value = '未找到工作人员信息，请重新登录';
    }
  } catch (error) {
    console.error('获取个人信息时发生异常:', error);
    console.error('错误详情:', {
      message: error.message,
      stack: error.stack,
      response: error.response
    })
    profileError.value = '网络错误，请稍后重试';
  } finally {
    profileLoading.value = false;
  }
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
  
  // 获取当前工人的调度任务
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
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.status-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.navigation-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.task-actions .el-button {
  margin: 0; /* 移除默认边距，使用gap控制间距 */
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

/* 任务详情弹窗样式 */
.task-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.detail-label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
  margin-right: 12px;
}

.detail-value {
  color: #333;
  font-weight: 400;
}

.bikes-container {
  min-height: 100px;
}

.no-bikes {
  padding: 20px;
  text-align: center;
  color: #999;
}

.bikes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.bike-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.bike-item:hover {
  background: #e3f2fd;
  border-color: #2196f3;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.1);
}

.bike-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #2196f3;
}

.bike-info {
  flex: 1;
}

.bike-number {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  margin-bottom: 4px;
}

.bike-id {
  font-size: 12px;
  color: #666;
  font-family: 'Courier New', monospace;
  background: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #ddd;
  word-break: break-all;
}

.bikes-summary {
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .bikes-grid {
    grid-template-columns: 1fr;
  }
}

/* 个人信息弹窗样式 */
.profile-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(2px);
}

.profile-modal {
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.profile-card {
  width: 600px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  padding: 24px;
  position: relative;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.profile-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  border: 2px solid #4F6EF7;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-name {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.profile-username {
  font-size: 0.9rem;
  color: #666;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #ff3742;
}

.loading-section {
  text-align: center;
  padding: 40px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4F6EF7;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.profile-content {
  max-height: 60vh;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 24px;
}

.info-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-label {
  font-weight: 500;
  color: #666;
  font-size: 0.9rem;
}

.info-value {
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.info-value.primary {
  background: #e3f2fd;
  color: #1976d2;
}

.info-value.success {
  background: #e8f5e8;
  color: #2e7d32;
}

.info-value.info {
  background: #e0f7fa;
  color: #0097a7;
}

.location-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.location-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid #dee2e6;
}

.location-icon {
  font-size: 2rem;
  margin-right: 12px;
}

.location-content {
  flex: 1;
}

.location-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.location-label {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
}

.error-section {
  text-align: center;
  padding: 40px 20px;
  color: #d33;
}

.retry-btn {
  margin-top: 16px;
  padding: 8px 16px;
  background: #4F6EF7;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
}

.retry-btn:hover {
  background: #3d5af5;
}

.button-row {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.action-btn {
  padding: 10px 24px;
  background: #4F6EF7;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
}

.action-btn:hover {
  background: #3d5af5;
}
</style> 