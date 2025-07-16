<template>
  <div class="api-test-container">
    <div class="test-panel">
      <h2>管理员工作人员API测试</h2>
      
      <div class="input-section">
        <label for="token-input">管理员Token:</label>
        <textarea 
          id="token-input"
          v-model="token" 
          placeholder="请输入管理员的Authorization Token"
          rows="3"
          class="token-input"
        ></textarea>
      </div>

      <div class="button-section">
        <button @click="testAPI" :disabled="isLoading" class="test-btn">
          {{ isLoading ? '测试中...' : '测试API' }}
        </button>
        <button @click="clearResults" class="clear-btn">清空结果</button>
        <button @click="goBack" class="back-btn">返回</button>
      </div>

      <div class="info-section">
        <h3>接口信息:</h3>
        <p><strong>URL:</strong> /managers/managed-staff</p>
        <p><strong>方法:</strong> GET</p>
        <p><strong>描述:</strong> 获取管理员手下的工作人员数据</p>
      </div>

      <div class="result-section">
        <h3>API响应结果:</h3>
        <div class="result-container">
          <div v-if="isLoading" class="loading">
            正在请求API...
          </div>
          <div v-else-if="apiResult" class="result-content">
            <div class="result-header">
              <span class="status" :class="getStatusClass(apiResult.code)">
                状态码: {{ apiResult.code }}
              </span>
              <span class="message">{{ apiResult.msg }}</span>
            </div>
            <div class="result-data">
              <h4>返回数据:</h4>
              <pre>{{ formatJSON(apiResult) }}</pre>
            </div>
            <div v-if="apiResult.data && Array.isArray(apiResult.data)" class="data-summary">
              <h4>数据摘要:</h4>
              <p>共获取到 <strong>{{ apiResult.data.length }}</strong> 个工作人员</p>
              <div v-if="apiResult.data.length > 0" class="staff-list">
                <div v-for="(staff, index) in apiResult.data" :key="index" class="staff-item">
                  <span class="staff-id">ID: {{ staff.id || staff.managerId || 'N/A' }}</span>
                  <span class="staff-name" v-if="staff.name">姓名: {{ staff.name }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="errorMessage" class="error-content">
            <h4>错误信息:</h4>
            <p class="error-message">{{ errorMessage }}</p>
          </div>
          <div v-else class="no-result">
            点击"测试API"按钮开始测试
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'APITestManagerStaff',
  data() {
    return {
      token: '',
      apiResult: null,
      errorMessage: '',
      isLoading: false
    }
  },
  methods: {
    async testAPI() {
      if (!this.token.trim()) {
        this.errorMessage = '请输入管理员Token';
        this.apiResult = null;
        return;
      }

      this.isLoading = true;
      this.errorMessage = '';
      this.apiResult = null;

      try {
        console.log('开始测试API，使用token:', this.token);
        
        const response = await request({
          url: '/managers/managed-staff',
          method: 'get',
          headers: {
            'Authorization': this.token.startsWith('Bearer ') ? this.token : 'Bearer ' + this.token
          }
        });

        console.log('API响应:', response);
        this.apiResult = response;
        
      } catch (error) {
        console.error('API测试失败:', error);
        this.errorMessage = `请求失败: ${error.message}`;
        
        if (error.response) {
          this.apiResult = error.response.data;
        }
      } finally {
        this.isLoading = false;
      }
    },

    clearResults() {
      this.apiResult = null;
      this.errorMessage = '';
    },

    goBack() {
      this.$router.go(-1);
    },

    getStatusClass(code) {
      if (code === 200 || code === '200') {
        return 'success';
      } else if (code >= 400 && code < 500) {
        return 'client-error';
      } else if (code >= 500) {
        return 'server-error';
      }
      return 'unknown';
    },

    formatJSON(obj) {
      return JSON.stringify(obj, null, 2);
    }
  },

  mounted() {
    // 自动填入当前存储的token（如果有的话）
    const storedToken = sessionStorage.getItem('authToken');
    if (storedToken) {
      this.token = storedToken;
    }
  }
}
</script>

<style scoped>
.api-test-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.test-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 30px;
  max-width: 800px;
  width: 100%;
  margin-top: 20px;
}

h2 {
  color: #333;
  text-align: center;
  margin-bottom: 30px;
  font-size: 1.8rem;
}

h3 {
  color: #555;
  margin-bottom: 15px;
  border-bottom: 2px solid #FFD600;
  padding-bottom: 5px;
}

h4 {
  color: #666;
  margin-bottom: 10px;
}

.input-section {
  margin-bottom: 25px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
}

.token-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  resize: vertical;
  transition: border-color 0.3s;
}

.token-input:focus {
  outline: none;
  border-color: #FFD600;
}

.button-section {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.test-btn, .clear-btn, .back-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.test-btn {
  background: #FFD600;
  color: #333;
}

.test-btn:hover:not(:disabled) {
  background: #ffcc00;
  transform: translateY(-2px);
}

.test-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.clear-btn {
  background: #f0f0f0;
  color: #666;
}

.clear-btn:hover {
  background: #e0e0e0;
}

.back-btn {
  background: #007bff;
  color: white;
}

.back-btn:hover {
  background: #0056b3;
}

.info-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 25px;
}

.info-section p {
  margin: 8px 0;
  color: #666;
}

.result-section {
  margin-top: 25px;
}

.result-container {
  border: 2px solid #eee;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
  min-height: 100px;
}

.loading {
  text-align: center;
  color: #666;
  font-style: italic;
}

.result-header {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ddd;
}

.status {
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 0.9rem;
}

.status.success {
  background: #d4edda;
  color: #155724;
}

.status.client-error {
  background: #f8d7da;
  color: #721c24;
}

.status.server-error {
  background: #f5c6cb;
  color: #721c24;
}

.status.unknown {
  background: #e2e3e5;
  color: #6c757d;
}

.message {
  color: #555;
  font-weight: 500;
}

.result-data pre {
  background: #2d3748;
  color: #e2e8f0;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.85rem;
  line-height: 1.5;
}

.data-summary {
  margin-top: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.staff-list {
  margin-top: 15px;
  display: grid;
  gap: 10px;
}

.staff-item {
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #FFD600;
  display: flex;
  gap: 20px;
  align-items: center;
}

.staff-id {
  font-weight: bold;
  color: #333;
}

.staff-name {
  color: #666;
}

.error-content {
  text-align: center;
}

.error-message {
  color: #dc3545;
  font-weight: 500;
  background: #f8d7da;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #f5c6cb;
}

.no-result {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .api-test-container {
    padding: 10px;
  }
  
  .test-panel {
    padding: 20px;
  }
  
  .button-section {
    flex-direction: column;
  }
  
  .result-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style> 