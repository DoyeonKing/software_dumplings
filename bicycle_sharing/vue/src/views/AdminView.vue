<template>
  <div class="dashboard">
    <div class="header">
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
      <h1>这是管理员的主界面</h1>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const showDropdown = ref(false);
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background-color: #f5f5fa;
}

.header {
  background: white;
  padding: 16px 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: flex-end;
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
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 72px);
}

h1 {
  font-size: 2rem;
  color: #333;
}
</style> 