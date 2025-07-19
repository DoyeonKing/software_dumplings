<template>
  <div class="profile-modal-overlay" @click="$emit('close')">
    <div class="profile-modal" @click.stop>
      <div class="profile-card">
        <div class="profile-header">
          <div class="profile-avatar">
            <img src="https://api.dicebear.com/7.x/miniavs/svg?seed=admin" alt="管理员头像" />
          </div>
          <div>
            <div class="profile-name">{{ form.realName }}</div>
            <div class="profile-username">用户名：{{ form.username }}</div>
          </div>
          <button class="close-btn" @click="$emit('close')">×</button>
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
              @click="$emit('close')"
              type="button"
          >关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileModal',
  props: {
    form: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      editMode: false
    }
  },
  methods: {
    saveInfo() {
      this.editMode = false;
      this.$emit('save', this.form);
      window.alert('信息已保存！');
    }
  }
}
</script>

<style scoped>
/* 个人资料浮窗 */
.profile-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(2px);
}

.profile-modal {
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.profile-card {
  width: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  padding: 36px 36px 28px 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  backdrop-filter: blur(2px);
  position: relative;
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 30px;
  height: 30px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;
}

.close-btn:hover {
  background: #ff3742;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  width: 100%;
  justify-content: center;
  position: relative;
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

@media (max-width: 768px) {
  .profile-card {
    width: 95vw;
    padding: 20px;
  }

  .profile-header {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 20px;
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