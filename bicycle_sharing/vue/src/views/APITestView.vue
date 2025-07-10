<template>
  <div class="api-test-container">
    <h2>API 测试页面</h2>
    
    <!-- Hello API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>Hello API 测试</span>
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
          <span>单车 API 测试</span>
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
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ areaResult.code }}</p>
        <p><strong>消息：</strong>{{ areaResult.msg }}</p>
        <div v-if="areaResult.data" class="data-table">
          <el-table :data="areaResult.data" style="width: 100%" border stripe>
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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { testHello } from '@/api_test/hello'
import { getAllBicycles, getMapAreaBicycles } from '@/api_test/bicycle'
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

// 区域表单数据
const areaForm = ref({
  minLat: 22.5,
  maxLat: 22.6,
  minLng: 113.9,
  maxLng: 114.1
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

// 测试区域单车 API
const testMapAreaBicycles = async () => {
  try {
    const response = await getMapAreaBicycles(areaForm.value)
    areaResult.value = response
    ElMessage.success('获取区域单车数据成功')
  } catch (error) {
    console.error('获取区域单车数据失败：', error)
    ElMessage.error('获取区域单车数据失败')
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
</style>
