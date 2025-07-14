<template>
  <div class="login-container">
    <!-- API测试按钮 -->
    <div class="api-test-link">
      <el-button type="primary" link @click="router.push('/api-test')">
        API Test Page
      </el-button>
    </div>

    <div class="login-content">
      <div class="left-section">
        <div class="title-container">
          <h1>Shared-Bicycles<br>Management<br>System</h1>
          <p class="register-hint">
            If you don't have an account,<br>
            <router-link to="/register" class="link">Register here</router-link>.
          </p>
        </div>
      </div>

      <div class="right-section">
        <div class="login-box">
          <form @submit.prevent="handleLogin" class="login-form">
            <!-- 错误信息显示 -->
            <div v-if="errorMessage" class="error-message">
              {{ errorMessage }}
            </div>

            <div class="form-group">
              <input 
                type="text" 
                v-model="formData.username" 
                placeholder="Username"
                required
              />
            </div>
            
            <div class="form-group">
              <input 
                type="password" 
                v-model="formData.password" 
                placeholder="Password"
                required
              />
              <a href="#" class="recovery-link">Forgot password?</a>
            </div>

            <div class="form-group role-select">
              <label>Login as:</label>
              <div class="role-options">
                <label class="role-option">
                  <input type="radio" v-model="selectedRole" value="user" name="role">
                  <span class="role-text">User</span>
                </label>
                <label class="role-option">
                  <input type="radio" v-model="selectedRole" value="admin" name="role">
                  <span class="role-text">Admin</span>
                </label>
                <label class="role-option">
                  <input type="radio" v-model="selectedRole" value="worker" name="role">
                  <span class="role-text">Worker</span>
                </label>
              </div>
            </div>

            <button 
              type="submit" 
              class="sign-in-btn"
              :disabled="isLoading"
            >
              {{ isLoading ? 'Logging in...' : 'Login' }}
            </button>

            <div class="divider">
              <span>Or login with</span>
            </div>

            <div class="social-login">
              <button type="button" class="social-btn" aria-label="Sign in with Google">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
              </button>
              <button type="button" class="social-btn" aria-label="Sign in with Apple">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <path d="M17.05 20.28c-.98 1.15-2.01 1.15-3.07.52-1.12-.66-2.15-.69-3.33 0-1.48.85-2.36.61-3.28-.52C3.71 15.65 4.76 8.37 9.14 8.06c1.15.08 1.94.77 2.89.77 1.11-.03 1.77-.77 3.02-.77 3.56.23 5.13 6.19 2 12.22zM15.84 6.5c-1.8-2.17-4.29-1.81-5.19-1.04-.43-.9-.77-1.84-.77-2.88 0-2.58 2.13-3.24 3.41-3.45.22-.04.44-.07.66-.09 2.82.31 4.11 3.17 1.89 7.46z"/>
                </svg>
              </button>
              <button type="button" class="social-btn" aria-label="Sign in with Facebook">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <path fill="#1877F2" d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                </svg>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/account/login.js'

const router = useRouter()
const selectedRole = ref('user')
const errorMessage = ref('')
const isLoading = ref(false)

const formData = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  // 清空之前的错误信息
  errorMessage.value = ''
  
  // 验证表单
  if (!formData.value.username || !formData.value.password) {
    errorMessage.value = 'Please enter username and password'
    return
  }
  
  isLoading.value = true
  
  try {
    const response = await login({
      username: formData.value.username,
      password: formData.value.password,
      role: selectedRole.value
    })
    
    // 检查响应状态
    if (response.code === '200' || response.code === 200) {
      // 登录成功，获取token和用户信息
      const { token, user } = response.data
      
      // 将token存储到sessionStorage，方便各个页面使用
      sessionStorage.setItem('authToken', token)
      sessionStorage.setItem('userInfo', JSON.stringify(user))
      sessionStorage.setItem('userRole', selectedRole.value)
      
      // 根据用户角色跳转到相应页面
      const roleRoutes = {
        user: '/user',
        admin: '/admin', 
        worker: '/worker'
      }
      
      router.push(roleRoutes[selectedRole.value])
    } else {
      errorMessage.value = response.msg || 'Login failed, please check username and password'
    }
  } catch (error) {
    console.error('登录错误:', error)
    errorMessage.value = 'Network error, please try again later'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;  /* 添加相对定位 */
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  background-color: #ecedf6;
}

.login-content {
  display: flex;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  align-items: center;
  justify-content: space-between;
  padding: 60px 120px;
  gap: 160px;
}

.left-section {
  flex: 1.5;
  display: flex;
  justify-content: flex-start;
}

.title-container {
  max-width: 800px;
}

.right-section {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  max-width: 440px;
}

.login-box {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  padding: 60px;
  width: 100%;
  max-width: 380px;
  margin: 0 auto;
}

.login-form {
  width: 100%;
}

h1 {
  font-size: 5.5rem;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 2rem;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.register-hint {
  color: #666;
  line-height: 1.5;
  font-size: 1.2rem;
  margin-top: 2rem;
}

.link {
  color: #4F6EF7;
  text-decoration: none;
  font-weight: 500;
}

.form-group {
  margin-bottom: 1.5rem;
  width: 100%;
}

input {
  width: 100%;
  padding: 15px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  background: #f8f9fd;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

input:focus {
  outline: none;
  border-color: #4F6EF7;
}

.recovery-link {
  display: block;
  text-align: right;
  color: #666;
  text-decoration: none;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.role-select {
  margin-bottom: 2rem;
}

.role-select label {
  display: block;
  color: #666;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.role-options {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.role-option {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.role-option input[type="radio"] {
  width: auto;
  margin-right: 0.5rem;
  cursor: pointer;
}

.role-text {
  color: #1a1a1a;
  font-size: 1rem;
}

.sign-in-btn {
  width: 100%;
  padding: 15px;
  background: #4F6EF7;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
  margin: 1rem 0;
}

.sign-in-btn:hover:not(:disabled) {
  background: #3d5af5;
}

.sign-in-btn:disabled {
  background-color: #b7b7b7;
  cursor: not-allowed;
}

.divider {
  text-align: center;
  margin: 2rem 0;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: calc(50% - 80px);
  height: 1px;
  background: #e0e0e0;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  background: white;
  padding: 0 20px;
  color: #666;
  font-size: 0.9rem;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 2rem;
}

.social-btn {
  width: 50px;
  height: 50px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  padding: 12px;
}

.social-btn:hover {
  transform: translateY(-2px);
  border-color: #4F6EF7;
  background-color: #f8f9fd;
}

.social-btn img, .social-btn svg {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

/* 错误信息样式 */
.error-message {
  background: #fee;
  color: #d33;
  padding: 10px 15px;
  border-radius: 8px;
  margin-bottom: 1rem;
  border: 1px solid #fcc;
  font-size: 0.9rem;
  text-align: center;
}

.api-test-link {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
}

@media (max-width: 1200px) {
  .login-content {
    padding: 40px 60px;
    gap: 60px;
  }

  h1 {
    font-size: 4rem;
  }
}

@media (max-width: 768px) {
  .login-content {
    flex-direction: column;
    padding: 20px;
    text-align: center;
  }

  .right-section {
    max-width: 100%;
  }

  .login-box {
    max-width: 380px;
  }

  h1 {
    font-size: 3rem;
  }

  .register-hint {
    font-size: 1rem;
  }
}
</style> 