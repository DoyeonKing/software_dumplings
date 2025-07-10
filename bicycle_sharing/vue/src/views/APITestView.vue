<template>
  <div class="api-test-container">
    <h2>API 测试页面</h2>
    
    <!-- Hello API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>Hello API 测试（已废弃）</span>
          <el-button type="primary" @click="testHelloAPI">测试连接</el-button>
        </div>
      </template>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ helloResult.code }}</p>
        <p><strong>消息：</strong>{{ helloResult.msg }}</p>
        <p><strong>数据：</strong>{{ helloResult.data }}</p>
      </div>
    </el-card>

    <!-- 单车 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>单车 API 测试（已废弃）</span>
          <el-button type="primary" @click="testBicycleAPI">获取所有单车</el-button>
        </div>
      </template>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ bicycleResult.code }}</p>
        <p><strong>消息：</strong>{{ bicycleResult.msg }}</p>
        <div v-if="bicycleResult.data" class="data-table">
          <el-table :data="bicycleResult.data" style="width: 100%" border stripe>
            <el-table-column prop="bike_id" label="单车ID" width="100" />
            <el-table-column prop="current_lat" label="纬度" width="120" />
            <el-table-column prop="current_lon" label="经度" width="120" />
            <el-table-column prop="current_geohash" label="地理哈希" width="120" />
            <el-table-column prop="bike_status" label="状态" width="100" />
            <el-table-column prop="last_updated_time" label="最后更新时间" />
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 区域单车 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>区域单车 API 测试</span>
          <el-button type="primary" @click="testMapAreaBicycles">获取区域单车</el-button>
        </div>
      </template>
      <div class="test-form">
        <el-form :model="areaForm" label-width="100px">
          <el-form-item label="最小纬度">
            <el-input v-model.number="areaForm.minLat" type="number" step="0.000001" />
          </el-form-item>
          <el-form-item label="最大纬度">
            <el-input v-model.number="areaForm.maxLat" type="number" step="0.000001" />
          </el-form-item>
          <el-form-item label="最小经度">
            <el-input v-model.number="areaForm.minLng" type="number" step="0.000001" />
          </el-form-item>
          <el-form-item label="最大经度">
            <el-input v-model.number="areaForm.maxLng" type="number" step="0.000001" />
          </el-form-item>
          <el-form-item label="单车状态">
            <el-select v-model="areaForm.bikeStatus">
              <el-option label="使用中" value="使用中" />
              <el-option label="空闲中" value="空闲中" />
              <el-option label="维修中" value="维修中" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ areaResult.code }}</p>
        <p><strong>消息：</strong>{{ areaResult.msg }}</p>
        <div v-if="areaResult.data" class="data-table">
          <el-table :data="areaResult.data" style="width: 100%" border stripe>
            <el-table-column prop="bikeId" label="单车ID" width="100" />
            <el-table-column prop="currentLat" label="纬度" width="120" />
            <el-table-column prop="currentLon" label="经度" width="120" />
            <el-table-column prop="currentGeohash" label="地理哈希" width="120" />
            <el-table-column prop="bikeStatus" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getBikeStatusType(scope.row.bikeStatus)">
                  {{ scope.row.bikeStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastUpdatedTime" label="最后更新时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.lastUpdatedTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else-if="areaResult.code === 200" class="empty-data">
          <el-empty description="该区域暂无单车数据" />
        </div>
      </div>
    </el-card>

    <!-- 单车详情 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>单车详情 API 测试</span>
          <el-button type="primary" @click="testBikeDetails">获取单车详情</el-button>
        </div>
      </template>
      <div class="test-form">
        <el-form label-width="100px">
          <el-form-item label="单车ID">
            <el-input v-model="bikeIdInput" placeholder="请输入单车ID" />
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ bikeDetailResult.code }}</p>
        <p><strong>消息：</strong>{{ bikeDetailResult.msg }}</p>
        <div v-if="bikeDetailResult.data" class="data-detail">
          <el-descriptions title="单车详细信息" :column="2" border>
            <el-descriptions-item label="单车ID">{{ bikeDetailResult.data.bikeId }}</el-descriptions-item>
            <el-descriptions-item label="纬度">{{ bikeDetailResult.data.currentLat }}</el-descriptions-item>
            <el-descriptions-item label="经度">{{ bikeDetailResult.data.currentLon }}</el-descriptions-item>
            <el-descriptions-item label="地理哈希">{{ bikeDetailResult.data.currentGeohash }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getBikeStatusType(bikeDetailResult.data.bikeStatus)">
                {{ bikeDetailResult.data.bikeStatus }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最后更新时间">
              {{ formatDateTime(bikeDetailResult.data.lastUpdatedTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <!-- 登录 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>登录 API 测试</span>
          <el-button type="primary" @click="testLogin">测试登录</el-button>
        </div>
      </template>
      <div class="test-form">
        <el-form :model="loginForm" label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="loginForm.role" style="width: 100%">
              <el-option label="用户" value="user" />
              <el-option label="管理员" value="admin" />
              <el-option label="工作人员" value="worker" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ loginResult.code }}</p>
        <p><strong>消息：</strong>{{ loginResult.msg }}</p>
        <div v-if="loginResult.data" class="data-detail">
          <el-descriptions title="用户信息" :column="2" border>
            <el-descriptions-item label="用户ID">{{ loginResult.data.user.userid }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ loginResult.data.user.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号码">{{ loginResult.data.user.phoneNumber }}</el-descriptions-item>
            <el-descriptions-item label="总骑行次数">{{ loginResult.data.user.totalRides }}</el-descriptions-item>
            <el-descriptions-item label="总骑行时长(分钟)">{{ loginResult.data.user.totalDurationMinutes }}</el-descriptions-item>
            <el-descriptions-item label="总消费(元)">{{ loginResult.data.user.totalCost }}</el-descriptions-item>
          </el-descriptions>

          <el-descriptions title="认证信息" :column="1" border class="mt-4">
            <el-descriptions-item label="Token">
              <el-input
                type="textarea"
                :rows="3"
                v-model="loginResult.data.token"
                readonly
              />
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { testHello } from '@/api_test/hello'
import { getAllBicycles, getMapAreaBicycles, getBikeDetails } from '@/api_test/bicycle'
import { login } from '@/api_test/login'
import { ElMessage } from 'element-plus'

// 测试结果
const helloResult = ref({
  code: null,
  msg: '',
  data: null
})

const bicycleResult = ref({
  code: null,
  msg: '',
  data: null
})

const areaResult = ref({
  code: null,
  msg: '',
  data: null
})

// 单车详情结果
const bikeDetailResult = ref({
  code: null,
  msg: '',
  data: null
})

// 单车ID输入
const bikeIdInput = ref('')

// 区域表单数据
const areaForm = ref({
  minLat: 22.5,
  maxLat: 22.6,
  minLng: 113.9,
  maxLng: 114.1,
  bikeStatus: '使用中'  // 添加单车状态字段
})

// 登录表单数据
const loginForm = ref({
  username: '00076f606aab98e4b47e0c',
  password: '',
  role: 'user'
})

// 登录结果
const loginResult = ref({
  code: null,
  msg: '',
  data: null
})

// 测试Hello API
const testHelloAPI = async () => {
  try {
    const response = await testHello()
    helloResult.value = response
    ElMessage.success('API调用成功')
  } catch (error) {
    console.error('API调用失败：', error)
    ElMessage.error('API调用失败')
  }
}

// 测试单车 API
const testBicycleAPI = async () => {
  try {
    const response = await getAllBicycles()
    bicycleResult.value = response
    ElMessage.success('获取单车数据成功')
  } catch (error) {
    console.error('获取单车数据失败：', error)
    ElMessage.error('获取单车数据失败')
  }
}

// 在script setup部分添加以下函数
const getBikeStatusType = (status) => {
  switch (status) {
    case '使用中':
      return 'warning'
    case '空闲中':
      return 'success'
    case '维修中':
      return 'danger'
    default:
      return 'info'
  }
}

const formatDateTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 修改测试区域单车API函数
const testMapAreaBicycles = async () => {
  try {
    const response = await getMapAreaBicycles(areaForm.value)
    console.log('API Response:', response) // 添加调试日志
    areaResult.value = response
    if (response.code === 200) {
      ElMessage.success('获取区域单车数据成功')
    } else {
      ElMessage.warning(`获取数据返回状态码: ${response.code}`)
    }
  } catch (error) {
    console.error('获取区域单车数据失败：', error)
    ElMessage.error('获取区域单车数据失败')
  }
}

// 测试获取单车详情
const testBikeDetails = async () => {
  if (!bikeIdInput.value) {
    ElMessage.warning('请输入单车ID')
    return
  }
  try {
    const response = await getBikeDetails(bikeIdInput.value)
    console.log('单车详情响应:', response)
    bikeDetailResult.value = response
    if (response.code === 200) {
      ElMessage.success('获取单车详情成功')
    } else {
      ElMessage.warning(`获取数据返回状态码: ${response.code}`)
    }
  } catch (error) {
    console.error('获取单车详情失败：', error)
    ElMessage.error('获取单车详情失败')
  }
}

// 测试登录API
const testLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  try {
    const response = await login(loginForm.value)
    console.log('登录响应:', response)
    loginResult.value = response
    if (response.code === '200') {
      ElMessage.success('登录成功')
    } else {
      ElMessage.warning(`登录失败: ${response.msg}`)
    }
  } catch (error) {
    console.error('登录请求失败：', error)
    ElMessage.error('登录请求失败')
  }
}
</script>

<style scoped>
.api-test-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-result {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.test-result p {
  margin: 5px 0;
}

.data-table {
  margin-top: 15px;
}

.test-form {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.data-detail {
  margin-top: 15px;
  padding: 15px;
  background-color: #fff;
  border-radius: 4px;
}
.mt-4 {
  margin-top: 1rem;
}
</style>
