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

    <!-- 用户注册 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>用户注册 API 测试</span>
          <div>
            <el-button type="info" @click="clearRegisterForm" style="margin-right: 10px">
              清空表单
            </el-button>
            <el-button type="primary" @click="testRegisterAPI">
              注册用户
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 功能说明 -->
      <el-alert 
        title="📋 注册功能说明" 
        type="info" 
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <p>• 统一调用 <code>/register</code> 接口，通过role字段区分身份</p>
          <p>• 支持三种角色：user(普通用户)、admin(管理员)、worker(工作人员)</p>
        </template>
      </el-alert>
      
      <div class="test-form">
        <el-form ref="registerFormRef" :model="registerForm" label-width="120px" :rules="registerRules">
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名 (2-30个字符)"
              clearable
            />
          </el-form-item>
          <el-form-item label="手机号码" prop="phoneNumber">
            <el-input 
              v-model="registerForm.phoneNumber" 
              placeholder="请输入11位手机号码"
              clearable
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password"
              placeholder="请输入密码 (至少6位)"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password"
              placeholder="请再次输入密码"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item label="角色" prop="role">
            <el-select v-model="registerForm.role" style="width: 100%">
              <el-option label="普通用户" value="user" />
              <el-option label="管理员" value="admin" />
              <el-option label="工作人员" value="worker" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="test-result">
        <p><strong>状态码：</strong>{{ registerResult.code }}</p>
        <p><strong>消息：</strong>{{ registerResult.msg }}</p>
        <div v-if="registerResult.data" class="data-detail">
          <el-descriptions title="注册结果" :column="1" border>
            <el-descriptions-item label="完整Data内容">
              <pre style="background: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; word-wrap: break-word;">{{ JSON.stringify(registerResult.data, null, 2) }}</pre>
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 如果data是字符串消息，直接显示 -->
          <div v-if="typeof registerResult.data === 'string'" class="mt-4">
            <el-alert :title="registerResult.data" type="success" :closable="false" />
          </div>
          
          <!-- 如果data是对象，显示用户信息 -->
          <div v-else-if="typeof registerResult.data === 'object' && registerResult.data.userid" class="mt-4">
            <el-descriptions title="新注册用户信息" :column="2" border>
              <el-descriptions-item label="用户ID">
                <el-tag type="primary">{{ registerResult.data.userid }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="用户名">
                <el-tag type="success">{{ registerResult.data.username }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="手机号码">
                <el-tag type="info">{{ registerResult.data.phoneNumber }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="角色">
                <el-tag 
                  :type="registerResult.data.role === 'admin' ? 'danger' : registerResult.data.role === 'worker' ? 'warning' : 'success'"
                >
                  {{ registerResult.data.role === 'admin' ? '管理员' : registerResult.data.role === 'worker' ? '工作人员' : '普通用户' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        <div v-else-if="registerResult.code === 200 || registerResult.code === '200'" class="empty-data">
          <el-empty description="注册成功，暂无额外数据返回" />
        </div>
      </div>
    </el-card>

    <!-- 个人信息 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>个人信息 API 测试</span>
          <div>
            <el-button type="info" @click="copyTokenFromLogin" style="margin-right: 10px">
              复制登录Token
            </el-button>
            <el-button type="primary" @click="testProfileAPI">获取个人信息</el-button>
          </div>
        </div>
      </template>
             <div class="test-form">
         <el-form :model="profileForm" label-width="120px">
           <el-form-item label="JWT Token">
             <el-input 
               v-model="profileForm.token" 
               type="textarea"
               :rows="3"
               placeholder="请输入JWT Token (不需要Bearer前缀，系统会自动添加) 或点击上方按钮复制登录Token"
             />
           </el-form-item>
           <el-form-item label="发送的头部" v-if="profileForm.token">
             <el-input 
               :value="'Bearer ' + profileForm.token" 
               type="textarea"
               :rows="2"
               readonly
               style="background-color: #f5f5f5;"
             />
           </el-form-item>
         </el-form>
       </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ profileResult.code }}</p>
        <p><strong>消息：</strong>{{ profileResult.msg }}</p>
                 <div v-if="profileResult.data" class="data-detail">
           <!-- 用户基本信息 -->
           <el-descriptions title="用户基本信息" :column="2" border>
             <el-descriptions-item label="用户ID">
               <el-tag type="primary">{{ profileResult.data.userid }}</el-tag>
             </el-descriptions-item>
             <el-descriptions-item label="用户名">
               <el-tag type="success">{{ profileResult.data.username }}</el-tag>
             </el-descriptions-item>
             <el-descriptions-item label="手机号码" :span="2">
               <el-tag type="info">{{ profileResult.data.phoneNumber }}</el-tag>
             </el-descriptions-item>
           </el-descriptions>

           <!-- 骑行统计信息 -->
           <el-descriptions title="骑行统计信息" :column="2" border class="mt-4">
             <el-descriptions-item label="总骑行次数">
               <el-statistic :value="profileResult.data.totalRides" suffix="次">
                 <template #prefix>
                   <span style="color: #409EFF;">🚴</span>
                 </template>
               </el-statistic>
             </el-descriptions-item>
             <el-descriptions-item label="总骑行时长">
               <el-statistic :value="profileResult.data.totalDurationMinutes" suffix="分钟">
                 <template #prefix>
                   <span style="color: #67C23A;">⏱️</span>
                 </template>
               </el-statistic>
             </el-descriptions-item>
             <el-descriptions-item label="总消费金额" :span="2">
               <el-statistic :value="profileResult.data.totalCost" suffix="元" :precision="2">
                 <template #prefix>
                   <span style="color: #E6A23C;">💰</span>
                 </template>
               </el-statistic>
             </el-descriptions-item>
           </el-descriptions>

           <!-- 数据统计卡片 -->
           <div class="statistics-cards mt-4">
             <el-row :gutter="16">
               <el-col :span="8">
                 <el-card class="stat-card">
                   <el-statistic title="平均每次骑行时长" :value="getAverageRideDuration()" suffix="分钟" />
                 </el-card>
               </el-col>
               <el-col :span="8">
                 <el-card class="stat-card">
                   <el-statistic title="平均每次消费" :value="getAverageCost()" suffix="元" :precision="2" />
                 </el-card>
               </el-col>
               <el-col :span="8">
                 <el-card class="stat-card">
                   <el-statistic title="平均每分钟费用" :value="getCostPerMinute()" suffix="元" :precision="3" />
                 </el-card>
               </el-col>
             </el-row>
           </div>

           <!-- 完整数据结构（折叠显示） -->
           <el-collapse class="mt-4">
             <el-collapse-item title="查看完整数据结构（调试用）" name="debug">
               <pre style="background: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; word-wrap: break-word;">{{ JSON.stringify(profileResult.data, null, 2) }}</pre>
             </el-collapse-item>
                      </el-collapse>
         </div>
         <div v-else-if="profileResult.code === 200 || profileResult.code === '200'" class="empty-data">
           <el-empty description="暂无个人信息数据" />
         </div>
       </div>
     </el-card>

    <!-- 修改密码 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>修改密码 API 测试</span>
          <div>
            <el-button type="info" @click="copyTokenToChangePassword" style="margin-right: 10px">
              复制登录Token
            </el-button>
            <el-button type="primary" @click="testChangePasswordAPI">修改密码</el-button>
          </div>
        </div>
      </template>
             <!-- 安全提示 -->
       <el-alert 
         title="🔒 安全提示" 
         type="info" 
         :closable="false"
         style="margin-bottom: 20px;"
       >
         <template #default>
           <p>• 请确保在安全的网络环境下修改密码</p>
           <p>• 新密码长度不能少于6位</p>
           <p>• 修改成功后请妥善保管新密码</p>
         </template>
       </el-alert>
       
       <div class="test-form">
         <el-form ref="changePasswordFormRef" :model="changePasswordForm" label-width="120px" :rules="passwordRules">
           <el-form-item label="JWT Token">
            <el-input 
              v-model="changePasswordForm.token" 
              type="textarea"
              :rows="2"
              placeholder="请输入JWT Token (不需要Bearer前缀) 或点击上方按钮复制登录Token"
            />
          </el-form-item>
          <el-form-item label="发送的头部" v-if="changePasswordForm.token">
            <el-input 
              :value="'Bearer ' + changePasswordForm.token" 
              type="textarea"
              :rows="2"
              readonly
              style="background-color: #f5f5f5;"
            />
          </el-form-item>
          <el-form-item label="原密码" prop="oldPassword">
            <el-input 
              v-model="changePasswordForm.oldPassword" 
              type="password"
              placeholder="请输入原密码"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="changePasswordForm.newPassword" 
              type="password"
              placeholder="请输入新密码"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmNewPassword">
            <el-input 
              v-model="changePasswordForm.confirmNewPassword" 
              type="password"
              placeholder="请再次输入新密码"
              show-password
              clearable
            />
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ changePasswordResult.code }}</p>
        <p><strong>消息：</strong>{{ changePasswordResult.msg }}</p>
        <div v-if="changePasswordResult.data" class="data-detail">
          <el-descriptions title="返回数据" :column="1" border>
            <el-descriptions-item label="完整Data内容">
              <pre style="background: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; word-wrap: break-word;">{{ JSON.stringify(changePasswordResult.data, null, 2) }}</pre>
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 如果data是字符串消息，直接显示 -->
          <div v-if="typeof changePasswordResult.data === 'string'" class="mt-4">
            <el-alert :title="changePasswordResult.data" type="success" :closable="false" />
          </div>
        </div>
        <div v-else-if="changePasswordResult.code === 200 || changePasswordResult.code === '200'" class="empty-data">
          <el-empty description="密码修改成功，暂无额外数据返回" />
        </div>
      </div>
    </el-card>

    <!-- 天气数据 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>天气数据 API 测试</span>
          <el-button type="primary" @click="testWeatherAPI">获取天气数据</el-button>
        </div>
      </template>
      <div class="test-form">
        <el-form :model="weatherForm" label-width="120px">
          <el-form-item label="地理哈希区域">
            <el-input v-model="weatherForm.geohashArea" placeholder="请输入地理哈希区域" />
          </el-form-item>
          <el-form-item label="记录日期">
            <el-date-picker 
              v-model="weatherForm.recordDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ weatherResult.code }}</p>
        <p><strong>消息：</strong>{{ weatherResult.msg }}</p>
        <div v-if="weatherResult.data" class="data-detail">
          <el-descriptions title="天气详细信息" :column="2" border>
            <el-descriptions-item label="天气记录ID">{{ weatherResult.data.weatherRecordId }}</el-descriptions-item>
            <el-descriptions-item label="记录日期">{{ weatherResult.data.recordDate }}</el-descriptions-item>
            <el-descriptions-item label="地理哈希区域">{{ weatherResult.data.geohashArea }}</el-descriptions-item>
            <el-descriptions-item label="最高温度">
              <el-tag type="danger">{{ weatherResult.data.tempMaxC }}°C</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最低温度">
              <el-tag type="primary">{{ weatherResult.data.tempMinC }}°C</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风向/天气">{{ weatherResult.data.windDirection }}</el-descriptions-item>
            <el-descriptions-item label="风力等级">{{ weatherResult.data.windLevel }} 级</el-descriptions-item>
            <el-descriptions-item label="降水指标" :span="2">
              <el-tag :type="getPrecipitationType(weatherResult.data.hasPrecipitationTextIndicator)">
                {{ weatherResult.data.hasPrecipitationTextIndicator === 0 ? '无降水' : '有降水' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else-if="weatherResult.code === 200" class="empty-data">
          <el-empty description="该区域暂无天气数据" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { testHello } from '@/api_test/hello'
import { getAllBicycles, getMapAreaBicycles, getBikeDetails } from '@/api_test/bicycle'
import { login, changePassword } from '@/api_test/login'
import { getWeatherRecord } from '@/api_test/weather'
import { getUserProfile } from '@/api_test/profile'
import { register } from '@/api_test/register'
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

// 天气表单数据
const weatherForm = ref({
  geohashArea: 'uxbpbz',
  recordDate: '2018-01-01'
})

// 天气结果
const weatherResult = ref({
  code: null,
  msg: '',
  data: null
})

// 个人信息表单数据
const profileForm = ref({
  token: ''
})

// 个人信息结果
const profileResult = ref({
  code: null,
  msg: '',
  data: null
})

// 修改密码表单数据
const changePasswordForm = ref({
  token: '',
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: ''
})

// 修改密码结果
const changePasswordResult = ref({
  code: null,
  msg: '',
  data: null
})

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== changePasswordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 修改密码表单引用
const changePasswordFormRef = ref(null)

// 注册表单数据
const registerForm = ref({
  username: '',
  phoneNumber: '',
  password: '',
  confirmPassword: '',
  role: 'user'
})

// 注册结果
const registerResult = ref({
  code: null,
  msg: '',
  data: null
})

// 注册表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度在2到30个字符', trigger: 'blur' }
  ],
  phoneNumber: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 注册表单引用
const registerFormRef = ref(null)

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

// 获取降水指标的标签类型
const getPrecipitationType = (indicator) => {
  if (indicator === 0) {
    return 'success' // 无降水
  } else {
    return 'warning' // 有降水
  }
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

// 测试天气API
const testWeatherAPI = async () => {
  if (!weatherForm.value.geohashArea || !weatherForm.value.recordDate) {
    ElMessage.warning('请输入地理哈希区域和记录日期')
    return
  }
  try {
    const response = await getWeatherRecord(weatherForm.value)
    console.log('天气数据响应:', response)
    weatherResult.value = response
    if (response.code === 200) {
      ElMessage.success('获取天气数据成功')
    } else {
      ElMessage.warning(`${response.msg}`)
    }
  } catch (error) {
    console.error('获取天气数据失败：', error)
    weatherResult.value = {
      code: 500,
      msg: '网络请求失败',
      data: null
    }
    ElMessage.error('获取天气数据失败')
  }
}

// 测试个人信息API
const testProfileAPI = async () => {
  if (!profileForm.value.token) {
    ElMessage.warning('请输入Authorization Token')
    return
  }
  try {
    console.log('发送的Authorization头部:', 'Bearer ' + profileForm.value.token)
    const response = await getUserProfile(profileForm.value.token)
    console.log('个人信息响应:', response)
    profileResult.value = response
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('获取个人信息成功')
    } else {
      ElMessage.warning(`获取个人信息失败: ${response.msg}`)
    }
  } catch (error) {
    console.error('获取个人信息失败：', error)
    profileResult.value = {
      code: 500,
      msg: '网络请求失败',
      data: null
    }
    ElMessage.error('获取个人信息失败')
  }
}

// 从登录结果复制Token
const copyTokenFromLogin = () => {
  if (loginResult.value.data && loginResult.value.data.token) {
    profileForm.value.token = loginResult.value.data.token
    ElMessage.success('已复制登录Token')
  } else {
    ElMessage.warning('请先成功登录获取Token')
  }
}

// 计算平均每次骑行时长
const getAverageRideDuration = () => {
  const data = profileResult.value.data
  if (!data || data.totalRides === 0) return 0
  return (data.totalDurationMinutes / data.totalRides).toFixed(1)
}

// 计算平均每次消费
const getAverageCost = () => {
  const data = profileResult.value.data
  if (!data || data.totalRides === 0) return 0
  return (data.totalCost / data.totalRides).toFixed(2)
}

// 计算平均每分钟费用
const getCostPerMinute = () => {
  const data = profileResult.value.data
  if (!data || data.totalDurationMinutes === 0) return 0
  return (data.totalCost / data.totalDurationMinutes).toFixed(3)
}

// 测试修改密码API
const testChangePasswordAPI = async () => {
  if (!changePasswordForm.value.token) {
    ElMessage.warning('请输入Authorization Token')
    return
  }
  
  // 使用Element Plus表单验证
  if (!changePasswordFormRef.value) {
    ElMessage.warning('表单未初始化')
    return
  }
  
  try {
    await changePasswordFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请修正表单错误后重试')
    return
  }
  
  if (changePasswordForm.value.newPassword === changePasswordForm.value.oldPassword) {
    ElMessage.warning('新密码不能与旧密码相同')
    return
  }
  
  try {
    console.log('发送的Authorization头部:', 'Bearer ' + changePasswordForm.value.token)
    console.log('修改密码请求数据:', {
      oldPassword: '***',
      newPassword: '***',
      confirmNewPassword: '***'
    })
    
    const response = await changePassword(changePasswordForm.value.token, {
      oldPassword: changePasswordForm.value.oldPassword,
      newPassword: changePasswordForm.value.newPassword,
      confirmNewPassword: changePasswordForm.value.confirmNewPassword
    })
    
    console.log('修改密码响应:', response)
    changePasswordResult.value = response
    
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('密码修改成功')
      // 清空密码字段并重置表单验证状态
      changePasswordForm.value.oldPassword = ''
      changePasswordForm.value.newPassword = ''
      changePasswordForm.value.confirmNewPassword = ''
      if (changePasswordFormRef.value) {
        changePasswordFormRef.value.resetFields()
      }
    } else {
      ElMessage.warning(`修改密码失败: ${response.msg}`)
    }
  } catch (error) {
    console.error('修改密码失败：', error)
    changePasswordResult.value = {
      code: 500,
      msg: '网络请求失败',
      data: null
    }
    ElMessage.error('修改密码失败')
  }
}

// 从登录结果复制Token到修改密码
const copyTokenToChangePassword = () => {
  if (loginResult.value.data && loginResult.value.data.token) {
    changePasswordForm.value.token = loginResult.value.data.token
    ElMessage.success('已复制登录Token到修改密码')
  } else {
    ElMessage.warning('请先成功登录获取Token')
  }
}

// 测试注册API
const testRegisterAPI = async () => {
  // 表单验证
  if (!registerFormRef.value) {
    ElMessage.warning('表单未初始化')
    return
  }
  
  try {
    await registerFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请修正表单错误后重试')
    return
  }
  
  try {
    console.log('注册请求数据:', {
      username: registerForm.value.username,
      phoneNumber: registerForm.value.phoneNumber,
      password: '***',
      confirmPassword: '***',
      role: registerForm.value.role
    })
    
    const response = await register(registerForm.value)
    console.log('注册响应:', response)
    registerResult.value = response
    
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('注册成功')
      // 清空表单
      registerForm.value = {
        username: '',
        phoneNumber: '',
        password: '',
        confirmPassword: '',
        role: 'user'
      }
      if (registerFormRef.value) {
        registerFormRef.value.resetFields()
      }
    } else {
      ElMessage.warning(`注册失败: ${response.msg}`)
    }
  } catch (error) {
    console.error('注册失败：', error)
    registerResult.value = {
      code: 500,
      msg: '网络请求失败',
      data: null
    }
    ElMessage.error('注册失败')
  }
}

// 清空注册表单
const clearRegisterForm = () => {
  registerForm.value = {
    username: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
    role: 'user'
  }
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
  registerResult.value = {
    code: null,
    msg: '',
    data: null
  }
  ElMessage.success('表单已清空')
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

.card-header div {
  display: flex;
  align-items: center;
}

pre {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  max-height: 300px;
  overflow-y: auto;
}

.statistics-cards {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.empty-data {
  text-align: center;
  padding: 40px;
}

code {
  background-color: #f5f7fa;
  color: #e6a23c;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}
</style>
