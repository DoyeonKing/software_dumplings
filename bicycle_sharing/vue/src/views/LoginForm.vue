<template>
  <div class="login-container">
    <h1 style="color: #5a1006;font-size: 80px;">请选择角色</h1>
    <div class="login-box">
      <div style="font-size: 80px;" class="login-block" @click="handleLogin('volunteer')">志愿者</div>
      <div style="font-size: 80px;text-align: center;" class="login-block" @click="handleLogin('organization')">志愿组织机构</div>
      <div style="font-size: 80px;" class="login-block" @click="handleLogin('admin')">管理员</div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  name: 'LoginForm',
  setup() {
    const router = useRouter();

    const handleLogin = (userType) => {
      switch (userType) {
        case 'volunteer':
          router.push('/volunteerlogin');
          break;
        case 'organization':
          router.push('/organizationlogin');
          break;
        case 'admin':
          router.push('/administratorlogin');
          break;
        default:
          console.error('未知用户类型');
      }
    };

    return {
      handleLogin
    };
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  /* 假设背景图在 public 目录下，替换 'your-background-image.jpg' 为实际图片名 */
  background-image: url("@/assets/volunteerLogin.jpg");
  background-size: cover;
  background-position: center;
}

.login-box {
  display: flex;
  justify-content: space-between;
  width: 80%; /* 调整容器宽度 */
  margin-top: 2rem;
}

.login-block {
  width: 300px; /* 设置宽度 */
  height:300px; /* 设置高度，与宽度相同实现正方形 */
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: white;
  color: #c32f1b;
  font-size: 1.2rem;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 1); /* 添加阴影 */
  cursor: pointer;
  transition: 0.3s;
  border-radius: 15px;
}
.login-block:hover {
  box-shadow: 0 8px 16px 0 rgba(0, 0, 0, 0.2); /* 鼠标悬停时加深阴影 */
  transform: scale(1.1); /* 鼠标悬停时放大 10%，可根据需求调整比例 */
}
</style>