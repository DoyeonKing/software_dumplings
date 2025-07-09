<template>
  <div class="dashboard">
    <div class="header">
      <div class="welcome">
        共享单车管理系统
        <span class="admin-welcome">欢迎您，管理员！</span>
      </div>
      <div class="avatar-dropdown">
        <div class="avatar" @click="showDropdown = !showDropdown">
          <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath fill='%23666' d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z'/%3E%3C/svg%3E" alt="Avatar" />
        </div>
        <div v-if="showDropdown" class="dropdown-menu">
          <router-link to="/profile" class="dropdown-item">个人信息</router-link>
          <router-link to="/login" class="dropdown-item">切换账号</router-link>
        </div>
      </div>
    </div>
    <div class="content">
      <div class="profile-card">
        <div class="profile-header">
          <div class="profile-avatar">
            <img src="https://api.dicebear.com/7.x/miniavs/svg?seed=admin" alt="管理员头像" />
          </div>
          <div>
            <div class="profile-name">{{ form.realName }}</div>
            <div class="profile-username">用户名：{{ form.username }}</div>
          </div>
        </div>
        <form class="profile-info" @submit.prevent>
          <div class="info-row">
            <div class="info-label">姓名</div>
            <input class="info-input" v-model="form.realName" :disabled="!editMode" />
          </div>
          <div class="info-row">
            <div class="info-label">出生年月</div>
            <input
                class="info-input"
                type="date"
                v-model="form.birth"
                :disabled="!editMode"
                style="min-width: 0;"
            />
          </div>
          <div class="info-row">
            <div class="info-label">性别</div>
            <select class="info-input" v-model="form.gender" :disabled="!editMode">
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>
          <div class="info-row">
            <div class="info-label">学历</div>
            <input class="info-input" v-model="form.education" :disabled="!editMode" />
          </div>
          <div class="info-row">
            <div class="info-label">所在单位</div>
            <input class="info-input" v-model="form.organization" :disabled="!editMode" />
          </div>
          <div class="info-row">
            <div class="info-label">工作地区</div>
            <input class="info-input" v-model="form.workArea" :disabled="!editMode" />
          </div>
          <div class="info-row">
            <div class="info-label">身份证号码</div>
            <input class="info-input" v-model="form.idNumber" :disabled="!editMode" />
          </div>
          <div class="info-row">
            <div class="info-label">联系电话</div>
            <input class="info-input" v-model="form.phone" :disabled="!editMode" />
          </div>
          <div class="info-row">
            <div class="info-label">办公邮箱</div>
            <input class="info-input" v-model="form.email" :disabled="!editMode" />
          </div>
        </form>
        <div class="button-row">
          <button
              class="action-btn"
              v-if="!editMode"
              @click="editMode = true"
              type="button"
          >修改信息</button>
          <button
              class="action-btn"
              v-if="editMode"
              @click="saveInfo"
              type="button"
          >保存信息</button>
          <button
              class="action-btn"
              @click="goHome"
              type="button"
          >返回主页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const showDropdown = ref(false)
const editMode = ref(false)
const router = useRouter()

const form = ref({
  username: 'admin001',
  realName: '张伟',
  gender: '男',
  education: '硕士研究生',
  organization: '共享单车科技有限公司',
  workArea: '上海市浦东新区',
  idNumber: '310101199001011234',
  phone: '138-1234-5678',
  email: 'admin@bikeshare.com',
  birth: '1990-01-01'
})

function saveInfo() {
  // 这里可以添加保存到后端的逻辑
  editMode.value = false
  // 可选：弹窗提示
  window.alert('信息已保存！')
}

function goHome() {
  router.push('/admin')
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  height: 100vh; /* 保证整个页面高度为视口高度 */
  background-color: #f5f5fa;
  overflow: hidden; /* 防止出现滚动条 */
  display: flex;
  flex-direction: column;
}
.header {
  background: #FFD600;
  padding: 0 40px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
  flex-shrink: 0;
}
.welcome {
  font-size: 1.6rem;
  font-weight: bold;
  color: #333;
  letter-spacing: 2px;
  display: flex;
  align-items: center;
}
.admin-welcome {
  font-size: 1rem;
  margin-left: 18px;
  color: #333;
  font-weight: normal;
  opacity: 0.85;
}
.avatar-dropdown {
  position: relative;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  transition: all 0.3s ease;
}
.avatar:hover {
  background: #e8e8e8;
}
.avatar img {
  width: 24px;
  height: 24px;
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 120px;
  z-index: 10;
}
.dropdown-item {
  display: block;
  padding: 8px 16px;
  color: #333;
  text-decoration: none;
  transition: background-color 0.3s;
}
.dropdown-item:hover {
  background-color: #f5f5fa;
}
.content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 0;
  height: calc(100vh - 80px); /* 80px为header高度 */
  background-image: url('C:/Users/12817/-xxq/software_dumplings/bicycle_sharing/vue/public/bg.adminhome.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  overflow: hidden;
}
.content::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(255, 255, 255, 0.5);
}
.content > * {
  position: relative;
  z-index: 1;
}
.profile-card {
  width: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  padding: 36px 36px 28px 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  backdrop-filter: blur(2px);
}
.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  width: 100%;
  justify-content: center;
}
.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 28px;
  border: 2px solid #FFD600;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.profile-name {
  font-size: 1.6rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 10px;
}
.profile-username {
  font-size: 1.1rem;
  color: #888;
}
.profile-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 0;
  width: 100%;
  justify-content: flex-start;
}
.info-label {
  width: 120px;
  color: #888;
  font-size: 1.08rem;
  font-weight: 600;
  margin-right: 12px;
  text-align: right;
}
.info-input {
  flex: 1;
  color: #222;
  font-size: 1.08rem;
  font-weight: 600;
  text-align: left;
  word-break: break-all;
  border: none;
  background: #f7f7f7;
  border-radius: 6px;
  padding: 8px 12px;
  transition: border 0.2s, background 0.2s;
  outline: none;
}
.info-input:disabled {
  background: #f7f7f7;
  color: #888;
  cursor: not-allowed;
}
.info-input:focus {
  border: 1.5px solid #FFD600;
  background: #fffbe6;
}
.button-row {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 32px;
  width: 100%;
}
.action-btn {
  padding: 10px 28px;
  font-size: 1.08rem;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  background: #222;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.action-btn:hover {
  background: #FFD600;
  color: #222;
}
@media (max-width: 800px) {
  .dashboard {
    height: auto;
    min-height: 100vh;
  }
  .profile-card {
    width: 98vw;
    padding: 18px 2vw 18px 2vw;
  }
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 18px;
  }
  .profile-avatar {
    margin-right: 0;
    margin-bottom: 12px;
    width: 60px;
    height: 60px;
  }
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  .info-label, .info-input {
    width: auto;
    margin-right: 0;
    text-align: left;
    font-size: 1rem;
  }
  .button-row {
    flex-direction: column;
    gap: 12px;
  }
}
</style>