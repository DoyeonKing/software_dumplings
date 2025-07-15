<template>
  <div class="api-test-container">
    <h2>API 测试页面</h2>
    
    <!-- 调度任务 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>调度任务 API 测试</span>
          <el-button type="primary" @click="testGetAllDispatchTasks">获取所有调度任务</el-button>
        </div>
      </template>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ dispatchTasksResult.code || '未请求' }}</p>
        <p><strong>消息：</strong>{{ dispatchTasksResult.msg || '暂无消息' }}</p>
        
        <!-- 显示调试信息 -->
        <div v-if="dispatchTasksResult.code" class="debug-section" style="margin: 15px 0; padding: 15px; background: #f0f9ff; border: 1px solid #0ea5e9; border-radius: 8px;">
          <h4 style="margin: 0 0 10px 0; color: #0369a1;">🔍 完整响应数据（调试用）</h4>
          <pre style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 11px; max-height: 300px; overflow-y: auto;">{{ JSON.stringify(dispatchTasksResult, null, 2) }}</pre>
        </div>
        
        <div v-if="dispatchTasksResult.data && Array.isArray(dispatchTasksResult.data) && dispatchTasksResult.data.length > 0" class="raw-data">
          <h4>调度任务数据（共 {{ dispatchTasksResult.data.length }} 条）：</h4>
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto; max-height: 400px;">{{ JSON.stringify(dispatchTasksResult.data, null, 2) }}</pre>
        </div>
        <div v-else-if="dispatchTasksResult.data" class="raw-data">
          <h4>调度任务数据：</h4>
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto; max-height: 400px;">{{ JSON.stringify(dispatchTasksResult.data, null, 2) }}</pre>
        </div>
        <div v-else-if="dispatchTasksResult.code === 200" class="empty-data">
          <el-empty description="暂无调度任务数据" />
        </div>
        <div v-else-if="dispatchTasksResult.code && dispatchTasksResult.code !== 200" class="error-data">
          <el-alert title="请求失败" type="error" :description="dispatchTasksResult.msg" show-icon />
        </div>
      </div>
    </el-card>

    <!-- 按日期范围和工作人员查询调度任务 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>按日期范围和工作人员查询调度任务 API 测试</span>
          <el-button type="primary" @click="testGetTasksByDateRangeAndStaff">查询调度任务</el-button>
        </div>
      </template>
      <div class="test-form">
        <el-form :model="dateRangeStaffForm" label-width="120px">
          <el-form-item label="开始日期">
            <el-date-picker 
              v-model="dateRangeStaffForm.startDate"
              type="date"
              placeholder="选择开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker 
              v-model="dateRangeStaffForm.endDate"
              type="date"
              placeholder="选择结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="分配给工作人员ID">
            <el-input-number 
              v-model="dateRangeStaffForm.assignedTo" 
              :min="1"
              placeholder="请输入工作人员ID"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ dateRangeStaffResult.code || '未请求' }}</p>
        <p><strong>消息：</strong>{{ dateRangeStaffResult.msg || '暂无消息' }}</p>
        
        <!-- 显示调试信息 -->
        <div v-if="dateRangeStaffResult.code" class="debug-section" style="margin: 15px 0; padding: 15px; background: #f0f9ff; border: 1px solid #0ea5e9; border-radius: 8px;">
          <h4 style="margin: 0 0 10px 0; color: #0369a1;">🔍 完整响应数据（调试用）</h4>
          <pre style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 11px; max-height: 300px; overflow-y: auto;">{{ JSON.stringify(dateRangeStaffResult, null, 2) }}</pre>
        </div>
        
        <div v-if="dateRangeStaffResult.data && Array.isArray(dateRangeStaffResult.data) && dateRangeStaffResult.data.length > 0" class="raw-data">
          <h4>查询到的调度任务（共 {{ dateRangeStaffResult.data.length }} 条）：</h4>
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto; max-height: 400px;">{{ JSON.stringify(dateRangeStaffResult.data, null, 2) }}</pre>
        </div>
        <div v-else-if="dateRangeStaffResult.data" class="raw-data">
          <h4>查询到的调度任务：</h4>
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto; max-height: 400px;">{{ JSON.stringify(dateRangeStaffResult.data, null, 2) }}</pre>
        </div>
        <div v-else-if="dateRangeStaffResult.code === 200" class="empty-data">
          <el-empty description="该时间范围内该工作人员暂无调度任务" />
        </div>
        <div v-else-if="dateRangeStaffResult.code && dateRangeStaffResult.code !== 200" class="error-data">
          <el-alert title="查询失败" type="error" :description="dateRangeStaffResult.msg" show-icon />
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

    <!-- 停车点 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>停车点 API 测试</span>
          <el-button type="primary" @click="testGetAllParkingAreas">获取所有停车点（备用数据）</el-button>
        </div>
      </template>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ parkingResult.code }}</p>
        <p><strong>消息：</strong>{{ parkingResult.msg }}</p>
        <div v-if="parkingResult.data" class="raw-data">
          <h4>原始数据：</h4>
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto; max-height: 400px;">{{ JSON.stringify(parkingResult.data, null, 2) }}</pre>
        </div>
      </div>
    </el-card>

    <!-- 区域停车点 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>区域停车点 API 测试（后端接口）</span>
          <el-button type="primary" @click="testGetParkingAreasInBounds">获取区域停车点</el-button>
        </div>
      </template>
      <div class="test-form">
        <el-form :model="parkingAreaForm" label-width="100px">
          <el-form-item label="最小纬度">
            <el-input v-model.number="parkingAreaForm.minLat" type="number" step="0.000001" />
          </el-form-item>
          <el-form-item label="最大纬度">
            <el-input v-model.number="parkingAreaForm.maxLat" type="number" step="0.000001" />
          </el-form-item>
          <el-form-item label="最小经度">
            <el-input v-model.number="parkingAreaForm.minLon" type="number" step="0.000001" />
          </el-form-item>
          <el-form-item label="最大经度">
            <el-input v-model.number="parkingAreaForm.maxLon" type="number" step="0.000001" />
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ parkingAreaResult.code }}</p>
        <p><strong>消息：</strong>{{ parkingAreaResult.msg }}</p>
        <div v-if="parkingAreaResult.data" class="raw-data">
          <h4>原始数据：</h4>
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto; max-height: 400px;">{{ JSON.stringify(parkingAreaResult.data, null, 2) }}</pre>
          
          <div style="margin-top: 15px;">
            <el-button type="success" @click="testConvertParkingData">测试数据转换</el-button>
          </div>
          
          <div v-if="convertedParkingData" style="margin-top: 15px;">
            <h4>转换后的数据：</h4>
            <pre style="background: #e8f5e8; padding: 10px; border-radius: 4px; overflow-x: auto; max-height: 400px;">{{ JSON.stringify(convertedParkingData, null, 2) }}</pre>
          </div>
        </div>
        <div v-else-if="parkingAreaResult.code === 200" class="empty-data">
          <el-empty description="该区域暂无停车点数据" />
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
        
        <!-- 调试：显示完整的响应数据 -->
        <div v-if="loginResult.code" class="debug-section" style="margin: 15px 0; padding: 15px; background: #f0f9ff; border: 1px solid #0ea5e9; border-radius: 8px;">
          <h4 style="margin: 0 0 10px 0; color: #0369a1;">🔍 完整响应数据（调试用）</h4>
          <pre style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 11px; max-height: 300px; overflow-y: auto;">{{ JSON.stringify(loginResult, null, 2) }}</pre>
        </div>
        
        <div v-if="loginResult.data" class="data-detail">
          <!-- 用户基本信息 -->
          <div v-if="loginResult.data.user">
            <el-descriptions title="用户基本信息" :column="2" border>
              <el-descriptions-item label="用户ID">
                <el-tag type="primary">{{ loginResult.data.user.userid }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="用户名">
                <el-tag type="success">{{ loginResult.data.user.username }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="手机号码">
                <el-tag type="info">{{ loginResult.data.user.phoneNumber }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="总骑行次数">
                <el-statistic :value="loginResult.data.user.totalRides" suffix="次">
                  <template #prefix>
                    <span style="color: #409EFF;">🚴</span>
                  </template>
                </el-statistic>
              </el-descriptions-item>
              <el-descriptions-item label="总骑行时长">
                <el-statistic :value="loginResult.data.user.totalDurationMinutes" suffix="分钟">
                  <template #prefix>
                    <span style="color: #67C23A;">⏱️</span>
                  </template>
                </el-statistic>
              </el-descriptions-item>
              <el-descriptions-item label="总消费">
                <el-statistic :value="loginResult.data.user.totalCost" suffix="元" :precision="2">
                  <template #prefix>
                    <span style="color: #E6A23C;">💰</span>
                  </template>
                </el-statistic>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- Token信息 - 重点突出显示 -->
          <div v-if="loginResult.data.token">
            <el-card class="token-card mt-4" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border: none;">
              <template #header>
                <div style="display: flex; align-items: center; justify-content: space-between; color: white;">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="font-size: 20px;">🔑</span>
                    <span style="font-weight: bold; font-size: 16px;">JWT认证Token</span>
                  </div>
                  <el-tag type="success" effect="plain" style="background: rgba(255,255,255,0.2); color: white; border: 1px solid rgba(255,255,255,0.3);">
                    长度: {{ loginResult.data.token.length }} 字符
                  </el-tag>
                </div>
              </template>
              
              <!-- Token显示区域 -->
              <div style="background: rgba(255,255,255,0.95); padding: 15px; border-radius: 8px; margin-bottom: 15px;">
                <el-input
                  type="textarea"
                  :rows="5"
                  :value="loginResult.data.token"
                  readonly
                  style="font-family: 'Courier New', monospace; font-size: 11px;"
                  :autosize="{ minRows: 5, maxRows: 8 }"
                />
              </div>
              
              <!-- 操作按钮区域 -->
              <div style="display: flex; gap: 10px; justify-content: center;">
                <el-button 
                  type="primary" 
                  @click="copyTokenToClipboard(loginResult.data.token)"
                  style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white;"
                >
                  <span style="margin-right: 5px;">📋</span>
                  复制Token
                </el-button>
                <el-button 
                  type="info" 
                  @click="copyTokenFromLogin"
                  style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white;"
                >
                  <span style="margin-right: 5px;">📤</span>
                  复制到个人信息测试
                </el-button>
                <el-button 
                  type="warning" 
                  @click="copyTokenToStaffProfile"
                  style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white;"
                >
                  <span style="margin-right: 5px;">👷</span>
                  复制到工作人员测试
                </el-button>
              </div>
              
              <!-- Token信息说明 -->
              <el-alert 
                title="💡 Token使用说明" 
                type="info" 
                :closable="false"
                style="margin-top: 15px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2);"
              >
                <template #default>
                  <div style="color: rgba(255,255,255,0.9); font-size: 13px;">
                    <p style="margin: 5px 0;">• 此Token用于API身份验证，请妥善保管</p>
                    <p style="margin: 5px 0;">• 发送请求时会自动添加 "Bearer " 前缀</p>
                    <p style="margin: 5px 0;">• Token有过期时间，请及时重新登录获取新Token</p>
                  </div>
                </template>
              </el-alert>
            </el-card>
          </div>
          
          <!-- 当有data但没有token时的提示 -->
          <div v-else-if="!loginResult.data.token" class="no-token-warning">
            <el-alert 
              title="⚠️ 未找到Token" 
              type="warning" 
              :closable="false"
              style="margin: 15px 0;"
            >
              <template #default>
                <p>登录响应中包含用户数据，但未找到Token字段。</p>
                <p>请检查后端返回的数据结构中是否包含token字段。</p>
              </template>
            </el-alert>
          </div>

          <!-- 完整数据结构（折叠显示） -->
          <el-collapse class="mt-4">
            <el-collapse-item title="查看完整登录数据结构（调试用）" name="debug">
              <pre style="background: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; word-wrap: break-word;">{{ JSON.stringify(loginResult.data, null, 2) }}</pre>
            </el-collapse-item>
          </el-collapse>
        </div>
        
        <!-- 当登录成功但没有data时的提示 -->
        <div v-else-if="(loginResult.code === 200 || loginResult.code === '200') && !loginResult.data" class="empty-data">
          <el-alert 
            title="⚠️ 登录响应异常" 
            type="warning" 
            :closable="false"
            style="margin: 15px 0;"
          >
            <template #default>
              <p>登录状态码显示成功，但未返回用户数据和Token。</p>
              <p>请检查上方的完整响应数据，确认后端返回的数据结构。</p>
            </template>
          </el-alert>
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

    <!-- 工作人员个人信息 API 测试 -->
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>工作人员个人信息 API 测试</span>
          <div>
            <el-button type="info" @click="copyTokenToStaffProfile" style="margin-right: 10px">
              复制登录Token
            </el-button>
            <el-button type="primary" @click="testStaffProfileAPI">获取工作人员个人信息</el-button>
          </div>
        </div>
      </template>
      <div class="test-form">
        <el-form :model="staffProfileForm" label-width="120px">
          <el-form-item label="JWT Token">
            <el-input 
              v-model="staffProfileForm.token" 
              type="textarea"
              :rows="3"
              placeholder="请输入JWT Token (不需要Bearer前缀，系统会自动添加) 或点击上方按钮复制登录Token"
            />
          </el-form-item>
          <el-form-item label="发送的头部" v-if="staffProfileForm.token">
            <el-input 
              :value="'Bearer ' + staffProfileForm.token" 
              type="textarea"
              :rows="2"
              readonly
              style="background-color: #f5f5f5;"
            />
          </el-form-item>
        </el-form>
      </div>
      <div class="test-result">
        <p><strong>状态码：</strong>{{ staffProfileResult.code }}</p>
        <p><strong>消息：</strong>{{ staffProfileResult.msg }}</p>
        <div v-if="staffProfileResult.data" class="data-detail">
          <!-- 工作人员基本信息 -->
          <el-descriptions title="工作人员基本信息" :column="2" border>
            <el-descriptions-item label="工作人员ID">
              <el-tag type="primary">{{ staffProfileResult.data.staffId }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              <el-tag type="success">{{ staffProfileResult.data.username }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手机号码">
              <el-tag type="info">{{ staffProfileResult.data.phoneNumber }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="工作区域">
              <el-tag type="warning">{{ staffProfileResult.data.workArea }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 工作统计信息 -->
          <el-descriptions title="工作统计信息" :column="2" border class="mt-4">
            <el-descriptions-item label="已完成任务数">
              <el-statistic :value="staffProfileResult.data.completedTasks" suffix="项">
                <template #prefix>
                  <span style="color: #409EFF;">✅</span>
                </template>
              </el-statistic>
            </el-descriptions-item>
            <el-descriptions-item label="总工作时长">
              <el-statistic :value="staffProfileResult.data.workingHours" suffix="小时" :precision="1">
                <template #prefix>
                  <span style="color: #67C23A;">⏰</span>
                </template>
              </el-statistic>
            </el-descriptions-item>
            <el-descriptions-item label="绩效评级" :span="2">
              <el-statistic :value="staffProfileResult.data.performanceRating" suffix="分" :precision="1">
                <template #prefix>
                  <span style="color: #E6A23C;">⭐</span>
                </template>
              </el-statistic>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 工作效率数据统计卡片 -->
          <div class="statistics-cards mt-4" v-if="staffProfileResult.data.completedTasks > 0">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-card class="stat-card">
                  <el-statistic 
                    title="平均每小时任务数" 
                    :value="staffProfileResult.data.workingHours > 0 ? (staffProfileResult.data.completedTasks / staffProfileResult.data.workingHours).toFixed(2) : 0" 
                    suffix="项/小时" 
                  />
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <el-statistic 
                    title="平均任务效率" 
                    :value="staffProfileResult.data.completedTasks > 0 ? (staffProfileResult.data.workingHours / staffProfileResult.data.completedTasks).toFixed(2) : 0" 
                    suffix="小时/项" 
                  />
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <el-statistic 
                    title="绩效等级" 
                    :value="getPerformanceLevel(staffProfileResult.data.performanceRating)"
                    class="performance-level"
                  />
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 完整数据结构（折叠显示） -->
          <el-collapse class="mt-4">
            <el-collapse-item title="查看完整工作人员数据结构（调试用）" name="debug">
              <pre style="background: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; word-wrap: break-word;">{{ JSON.stringify(staffProfileResult.data, null, 2) }}</pre>
            </el-collapse-item>
          </el-collapse>
        </div>
        <div v-else-if="staffProfileResult.code === 200 || staffProfileResult.code === '200'" class="empty-data">
          <el-empty description="暂无工作人员个人信息数据" />
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

import { getAllBicycles, getMapAreaBicycles, getBikeDetails } from '@/api/map/bicycle'
import { getAllParkingAreas, getParkingAreasInBounds, convertParkingAreaData } from '@/api/map/parking'
import { login, changePassword } from '@/api/account/login'
import { getWeatherRecord } from '@/api/weather'
import { getUserProfile, getStaffProfile } from '@/api/account/profile'
import { register } from '@/api/account/register'
import { getAllDispatchTasks, getDispatchTasksByDateRangeAndStaff } from '@/api/assignment/task'
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

// 调度任务结果
const dispatchTasksResult = ref({
  code: null,
  msg: '',
  data: null
})

// 按日期和工作人员查询调度任务的表单数据
const dateRangeStaffForm = ref({
  startDate: '2017-01-01',
  endDate: '2020-01-01',
  assignedTo: 3
})

// 按日期和工作人员查询调度任务的结果
const dateRangeStaffResult = ref({
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

// 停车点测试结果
const parkingResult = ref({
  code: null,
  msg: '',
  data: null
})

// 停车点区域测试结果
const parkingAreaResult = ref({
  code: null,
  msg: '',
  data: null
})

// 停车点区域表单数据
const parkingAreaForm = ref({
  minLat: 22.5,
  maxLat: 22.6,
  minLon: 114.0,
  maxLon: 114.1
})

// 转换后的停车点数据
const convertedParkingData = ref(null)

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

// 工作人员个人信息表单数据
const staffProfileForm = ref({
  token: ''
})

// 工作人员个人信息结果
const staffProfileResult = ref({
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



// 测试调度任务 API
const testGetAllDispatchTasks = async () => {
  try {
    const response = await getAllDispatchTasks()
    console.log('调度任务API响应:', response)
    
    // 确保设置正确的数据结构
    dispatchTasksResult.value = {
      code: response.code || response.status || 200,
      msg: response.msg || response.message || '请求成功',
      data: response.data || response
    }
    
    if (dispatchTasksResult.value.code === 200) {
      ElMessage.success('获取调度任务数据成功')
    } else {
      ElMessage.warning(`获取数据返回状态码: ${dispatchTasksResult.value.code}`)
    }
  } catch (error) {
    console.error('获取调度任务数据失败：', error)
    ElMessage.error('获取调度任务数据失败')
    dispatchTasksResult.value = {
      code: 'ERROR',
      msg: error.message || '请求失败',
      data: null
    }
  }
}

// 测试按日期范围和工作人员查询调度任务 API
const testGetTasksByDateRangeAndStaff = async () => {
  if (!dateRangeStaffForm.value.startDate || !dateRangeStaffForm.value.endDate || !dateRangeStaffForm.value.assignedTo) {
    ElMessage.warning('请填写所有必需的参数')
    return
  }

  try {
    const response = await getDispatchTasksByDateRangeAndStaff(dateRangeStaffForm.value)
    console.log('按日期和工作人员查询调度任务API响应:', response)
    
    // 确保设置正确的数据结构
    dateRangeStaffResult.value = {
      code: response.code || response.status || 200,
      msg: response.msg || response.message || '请求成功',
      data: response.data || response
    }
    
    if (dateRangeStaffResult.value.code === 200) {
      ElMessage.success('查询调度任务数据成功')
    } else {
      ElMessage.warning(`获取数据返回状态码: ${dateRangeStaffResult.value.code}`)
    }
  } catch (error) {
    console.error('查询调度任务数据失败：', error)
    ElMessage.error('查询调度任务数据失败')
    dateRangeStaffResult.value = {
      code: 'ERROR',
      msg: error.message || '请求失败',
      data: null
    }
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

// 测试获取所有停车点
const testGetAllParkingAreas = async () => {
  try {
    const response = await getAllParkingAreas()
    console.log('获取所有停车点响应:', response)
    parkingResult.value = response
    if (response.code === 200) {
      ElMessage.success('获取所有停车点成功')
    } else {
      ElMessage.warning(`获取数据返回状态码: ${response.code}`)
    }
  } catch (error) {
    console.error('获取所有停车点失败：', error)
    ElMessage.error('获取所有停车点失败')
    parkingResult.value = {
      code: 'ERROR',
      msg: error.message || '请求失败',
      data: null
    }
  }
}

// 测试获取区域停车点
const testGetParkingAreasInBounds = async () => {
  try {
    const response = await getParkingAreasInBounds(parkingAreaForm.value)
    console.log('获取区域停车点响应:', response)
    parkingAreaResult.value = response
    // 清空之前的转换数据
    convertedParkingData.value = null
    
    // 检查响应数据格式
    let hasValidData = false
    if (response && Array.isArray(response)) {
      hasValidData = true
      ElMessage.success(`获取区域停车点成功，返回 ${response.length} 个停车点`)
    } else if (response && response.data && Array.isArray(response.data)) {
      hasValidData = true
      ElMessage.success(`获取区域停车点成功，返回 ${response.data.length} 个停车点`)
    } else if (response && response.code === 200) {
      hasValidData = true
      ElMessage.success('获取区域停车点成功')
    } else {
      ElMessage.warning(`获取数据返回状态码: ${response?.code || '未知'}`)
    }
  } catch (error) {
    console.error('获取区域停车点失败：', error)
    ElMessage.error('获取区域停车点失败')
    parkingAreaResult.value = {
      code: 'ERROR',
      msg: error.message || '请求失败',
      data: null
    }
  }
}

// 测试数据转换
const testConvertParkingData = () => {
  // 获取原始数据
  let rawData = null
  const response = parkingAreaResult.value
  
  if (response && Array.isArray(response)) {
    rawData = response
  } else if (response && response.data && Array.isArray(response.data)) {
    rawData = response.data
  } else if (response && Array.isArray(response.data)) {
    rawData = response.data
  }
  
  if (!rawData || !Array.isArray(rawData)) {
    ElMessage.warning('没有可转换的数据或数据格式错误')
    return
  }
  
  try {
    const converted = convertParkingAreaData(rawData)
    console.log('转换后的数据:', converted)
    convertedParkingData.value = converted
    ElMessage.success(`数据转换成功，共转换 ${converted.length} 个停车点`)
  } catch (error) {
    console.error('数据转换失败：', error)
    ElMessage.error('数据转换失败: ' + error.message)
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
    // 修正状态码判断，兼容数字和字符串
    if (response.code === '200' || response.code === 200) {
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

// 测试工作人员个人信息API
const testStaffProfileAPI = async () => {
  if (!staffProfileForm.value.token) {
    ElMessage.warning('请输入Authorization Token')
    return
  }
  try {
    console.log('发送的Authorization头部:', 'Bearer ' + staffProfileForm.value.token)
    const response = await getStaffProfile(staffProfileForm.value.token)
    console.log('工作人员个人信息响应:', response)
    staffProfileResult.value = response
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('获取工作人员个人信息成功')
    } else {
      ElMessage.warning(`获取工作人员个人信息失败: ${response.msg}`)
    }
  } catch (error) {
    console.error('获取工作人员个人信息失败：', error)
    staffProfileResult.value = {
      code: 500,
      msg: '网络请求失败',
      data: null
    }
    ElMessage.error('获取工作人员个人信息失败')
  }
}

// 从登录结果复制Token到工作人员个人信息
const copyTokenToStaffProfile = () => {
  if (loginResult.value.data && loginResult.value.data.token) {
    staffProfileForm.value.token = loginResult.value.data.token
    ElMessage.success('已复制登录Token到工作人员个人信息')
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

// 计算绩效等级
const getPerformanceLevel = (rating) => {
  if (rating >= 90) return '优秀'
  if (rating >= 80) return '良好'
  if (rating >= 70) return '合格'
  if (rating >= 60) return '待改进'
  return '不合格'
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

// 复制Token到剪贴板
const copyTokenToClipboard = async (token) => {
  try {
    await navigator.clipboard.writeText(token)
    ElMessage.success('Token已复制到剪贴板')
  } catch (error) {
    // 如果 Clipboard API 不可用，使用传统方法
    const textArea = document.createElement('textarea')
    textArea.value = token
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('Token已复制到剪贴板')
    } catch (err) {
      ElMessage.error('复制失败，请手动复制')
    }
    document.body.removeChild(textArea)
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

.raw-data {
  margin-top: 15px;
}

.raw-data h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
}

.raw-data pre {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Token卡片样式 */
.token-card {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.token-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
}

.token-card .el-card__header {
  padding: 15px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.token-card .el-card__body {
  padding: 20px;
}

/* Token输入框样式 */
.token-card .el-textarea__inner {
  background: rgba(248, 250, 252, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #2d3748;
  font-weight: 500;
  line-height: 1.6;
  border-radius: 6px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
}

/* 按钮hover效果 */
.token-card .el-button:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
