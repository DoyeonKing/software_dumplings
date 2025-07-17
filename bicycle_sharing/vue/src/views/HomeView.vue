
<template>
  <div class="home-container">
    <!-- 背景视频 -->
    <video class="background-video" src="/homeVideo.mp4" autoplay muted loop playsinline></video>
    
    <!-- 主要内容区域 -->
    <div class="content-wrapper">
      <!-- 左上角Logo和项目名称 -->
      <div class="header">
        <div class="logo-container">
          <div class="logo">🚲</div>
          <span class="project-name">共享单车管理系统</span>
        </div>
        
        <!-- 右上角菜单按钮 -->
        <div class="menu-button" @click="openMenu">
          <div class="menu-line"></div>
          <div class="menu-line"></div>
          <div class="menu-line"></div>
        </div>
      </div>
      
      <!-- 半透明圆形介绍区域 -->
      <div class="intro-circle">
        <div class="intro-content">
          <h1 class="intro-title">RIDE SMARTER, FLOW BETTER</h1>
          <p class="intro-description">
            算法驱动 智能调度<br>
            Algorithm-driven Intelligent Dispatch
          </p>
        </div>
        
        <!-- 跳转到登录的圆形按钮 -->
        <div class="login-button-container">
          <router-link to="/login" class="login-button">
            <span class="button-text">登录</span>
          </router-link>
        </div>
      </div>
    </div>
    
    <!-- 菜单覆盖层 -->
    <div v-if="showMenu" class="menu-overlay" @click="closeMenu">
      <!-- 扩散动画圆形 -->
      <div class="circle-expand" :class="{expanded: isExpanded}"></div>
      
      <div class="menu-content" @click.stop>
        <!-- 关闭按钮 -->
        <div class="close-button" @click="closeMenu">×</div>
        
        <!-- 菜单项 -->
        <div class="menu-items" :class="{slideIn: isExpanded}">
          <div class="menu-item" @mouseenter="showDetails = 'intro'" @mouseleave="showDetails = null">
            <div class="menu-title">项目简介</div>
            <div class="menu-subtitle">PROJECT INTRODUCTION</div>
            <div class="menu-details" v-show="showDetails === 'intro'">
              本项目是一个创新的共享单车调度方案，通过算法驱动的车辆预测，我们实现了单车的高效调度和使用，同时为用户提供便捷、环保的短途出行选择。
            </div>
          </div>
          <div class="menu-item" @mouseenter="showDetails = 'features'" @mouseleave="showDetails = null">
            <div class="menu-title">核心功能</div>
            <div class="menu-subtitle">CORE FEATURES</div>
            <div class="menu-details" v-show="showDetails === 'features'">
              定位与智能导航、用车还车、预测数据热力图、调度任务派发与管理
            </div>
          </div>
          <div class="menu-item" @mouseenter="showDetails = 'tech'" @mouseleave="showDetails = null">
            <div class="menu-title">技术平台</div>
            <div class="menu-subtitle">TECHNOLOGY PLATFORM</div>
            <div class="menu-details" v-show="showDetails === 'tech'">
              基于Vue.js + 高德地图API + 算法驱动的智能调度系统
            </div>
          </div>
          <div class="menu-item" @mouseenter="showDetails = 'usage'" @mouseleave="showDetails = null">
            <div class="menu-title">使用说明</div>
            <div class="menu-subtitle">USER GUIDE</div>
            <div class="menu-details" v-show="showDetails === 'usage'">
              项目分为3份：用户（用车、还车、导航）、管理员（管理调度任务）、工作人员（实施调度任务），可用不同账号进入相应界面。
            </div>
          </div>
          <div class="menu-item" @mouseenter="showDetails = 'about'" @mouseleave="showDetails = null">
            <div class="menu-title">关于我们</div>
            <div class="menu-subtitle">ABOUT US</div>
            <div class="menu-details" v-show="showDetails === 'about'">
              北京交通大学软件学院software_dumplings开发团队。
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue"

const showMenu = ref(false)
const isExpanded = ref(false)
const showDetails = ref(null)

const openMenu = () => {
  showMenu.value = true
  // 延迟启动扩散动画
  setTimeout(() => {
    isExpanded.value = true
  }, 100)
}

const closeMenu = () => {
  isExpanded.value = false
  // 等待动画完成后隐藏菜单
  setTimeout(() => {
    showMenu.value = false
  }, 600)
}
</script>

<style scoped>
.home-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.background-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  object-fit: cover;
  z-index: 1;
}

.content-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 头部样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 2rem;
  color: white;
}

.project-name {
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
}

/* 菜单按钮样式 */
.menu-button {
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
  padding: 8px;
}

.menu-line {
  width: 25px;
  height: 3px;
  background-color: white;
  border-radius: 2px;
}

/* 主要内容区域 */
.intro-circle {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 700px;
  height: 700px;
  background: rgba(0, 0, 0, 0.6); /* 偏黑且更高透明度 */
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(0, 0, 0, 0.3); /* 黑色边框 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.intro-content {
  text-align: left;
  color: white;
  padding: 60px 60px 60px 80px;
}

.intro-title {
  font-size: 4rem;
  font-weight: bold;
  margin-bottom: 30px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  letter-spacing: 2px;
  text-align: left;
}

.intro-description {
  font-size: 1.7rem;
  line-height: 2.2;
  margin-bottom: 30px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  text-align: left;
}

.login-button-container {
  position: absolute;
  bottom: 0;
  right: 0;
  transform: translate(50x, 50px); /* 移动到圆的右下角边缘 */
}

.login-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background: #4CAF50; /* 绿色背景 */
  border-radius: 50%;
  text-decoration: none;
  color: white;
  font-weight: bold;
  font-size: 1rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.login-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
  background: #45a049;
}

.button-text {
  font-size: 0.9rem;
}

/* 菜单覆盖层样式 */
.menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 扩散动画圆形 */
.circle-expand {
  position: absolute;
  top:20px;
  right: 20px;
  width:0;
  height: 0;
  background-color: rgba(0, 0, 0);
  border-radius:50%;
  transform: translate(50%, -50%);
  transition: all 0.6s cubic-bezier(0.4, 0.2, 0.2, 1);
}

.circle-expand.expanded {
  width: 3000px;
  height: 3000px;
  top: 50%;
  right: 50%;
  transform: translate(50%, -50%);
}

.menu-content {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  transition-delay: 0.3s;
}

.circle-expand.expanded ~ .menu-content {
  opacity: 1;
}

.close-button {
  position: absolute;
  top:40px;
  right: 40px;
  color: white;
  font-size: 3rem;
  cursor: pointer;
  font-weight:300;
  transition: opacity 0.3s ease;
  transition-delay: 0.6s;
}

.circle-expand.expanded ~ .menu-content .close-button {
  opacity:1;
}

.menu-items {
  display: flex;
  flex-direction: row;
  gap: 60px;
  flex-wrap: nowrap;
  justify-content: center;
  max-width: 1200px;
  opacity: 0;
  transform: translateX(100px);
  transition: all 0.5s ease;
  transition-delay: 0.6s;
}

.menu-items.slideIn {
  opacity: 1;
  transform: translateX(0);
}

.menu-item {
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease;
  position: relative;
}

.menu-item:hover {
  transform: scale(1.05);
}

.menu-title {
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 8px;
}

.menu-subtitle {
  color: #ccc;
  font-size: 0.9rem;
  letter-spacing: 1px;
}

.menu-details {
  color: #eee;
  font-size: 0.8rem;
  line-height: 1.5;
  margin-top: 10px;
  padding: 10px;
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 5px;
  max-width: 220px;
  min-width: 180px;
  margin-left: auto;
  margin-right: auto;
  text-align: left;
  transition: opacity 0.3s, transform 0.3s, max-height 0.3s;
  opacity: 0;
  transform: translateY(-10px) scaleY(0.95);
  max-height: 0;
  overflow: hidden;
  pointer-events: none;
  position: absolute;
  left: 50%;
  top: 100%;
  transform: translate(-50%, 10px) scaleY(0.95);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  z-index: 10;
}

.menu-item:hover .menu-details {
  opacity: 1;
  transform: translate(-50%, 20px) scaleY(1);
  max-height: 500px;
  pointer-events: auto;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .menu-items {
    gap: 30px;
  }
}
@media (max-width: 900px) {
  .menu-items {
    gap: 15px;
  }
  .menu-title {
    font-size: 1.1rem;
  }
  .menu-details {
    max-width: 160px;
    font-size: 0.75rem;
  }
}
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
  }
  
  .logo {
    font-size: 1.5rem;
  }
  
  .project-name {
    font-size: 1.2rem;
  }
  
  .intro-circle {
    width: 350px;
    height: 350px;
  }
  
  .intro-title {
    font-size: 2rem;
  }
  
  .intro-description {
    font-size: 1rem;
  }
  
  .login-button {
    width: 70px;
    height: 70px;
    font-size: 1rem;
  }
  
  .login-button-container {
    bottom: -25px;
    right: -25px;
  }
  
  .menu-items {
    flex-direction: column;
    gap: 30px;
  }
  
  .menu-details {
    position: static;
    left: auto;
    top: auto;
    transform: none;
    max-width: 90vw;
    font-size: 0.9rem;
    box-shadow: none;
  }
  
  .menu-title {
    font-size: 1.2em;
  }
  
  .menu-subtitle {
    font-size: 0.8rem;
  }

  .menu-details {
    max-width: 90vw;
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .intro-circle {
    width: 280px;
    height: 280px;
  }
  
  .intro-title {
    font-size: 1.5rem;
  }
  
  .intro-description {
    font-size: 0.9rem;
  }
}
</style>