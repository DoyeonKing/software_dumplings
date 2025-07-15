<template>
  <div class="menu-container">
    <button class="menu-btn" @click="toggleMenu">
      <span class="menu-icon">☰</span>
    </button>

    <!-- 弹出菜单 -->
    <div class="menu-dropdown" :class="{ 'menu-open': menuOpen }">

      <router-link to="/dashboard" class="menu-item" @click="closeMenu">
        <span class="menu-item-text">指标看板</span>
      </router-link>

      <router-link to="/location" class="menu-item" @click="closeMenu">
        <span class="menu-item-text">车辆投放</span>
      </router-link>

      <router-link to="/tasks" class="menu-item" @click="closeMenu">
        <span class="menu-item-text">任务查询</span>
      </router-link>

      <router-link to="/help" class="menu-item" @click="closeMenu">
        <span class="menu-item-text">帮助指南</span>
      </router-link>
    </div>
  </div>
</template>

<script>
import ProfileModal from './profileModel.vue'

export default {
  name: 'MenuComponent',
  components: {
    ProfileModal
  },
  data() {
    return {
      menuOpen: false,
      showProfile: false,
      profileForm: {
        username: 'admin001',
        realName: '张伟',
        gender: '男',
        education: '硕士研究生',
        organization: '共享单车科技有限公司',
        workArea: '深圳市南山区',
        idNumber: '310101199001011234',
        phone: '138-1234-5678',
        email: 'admin@bikeshare.com',
        birth: '1990-01-01'
      }
    }
  },
  mounted() {
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    toggleMenu() {
      this.menuOpen = !this.menuOpen;
    },
    closeMenu() {
      this.menuOpen = false;
    },
    handleClickOutside(event) {
      const menuContainer = event.target.closest('.menu-container');
      if (!menuContainer) {
        this.menuOpen = false;
      }
    },
    showProfileModal() {
      this.showProfile = true;
      this.closeMenu();
    },
    closeProfileModal() {
      this.showProfile = false;
    },
    saveProfile(formData) {
      // 处理保存逻辑
      this.profileForm = { ...formData };
      this.closeProfileModal();
      this.$emit('profile-saved', formData);
    }
  }
}
</script>

<style scoped>
/* 菜单容器 */
.menu-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
}

/* 菜单按钮 */
.menu-btn {
  width: 50px;
  height: 50px;
  background: white;
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.menu-btn:hover {
  background: #FFD600;
  transform: scale(1.05);
}

.menu-icon {
  font-size: 24px;
  color: #333;
}

.menu-btn:hover .menu-icon {
  color: #333;
}

/* 下拉菜单 */
.menu-dropdown {
  position: absolute;
  top: 60px;
  left: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  min-width: 180px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  overflow: hidden;
}

.menu-dropdown.menu-open {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

/* 菜单项 */
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #FFD600;
  color: #333;
}

.menu-item-text {
  font-size: 14px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-container {
    top: 15px;
    left: 15px;
  }

  .menu-btn {
    width: 45px;
    height: 45px;
  }

  .menu-icon {
    font-size: 20px;
  }

  .menu-dropdown {
    min-width: 160px;
  }
}
</style>