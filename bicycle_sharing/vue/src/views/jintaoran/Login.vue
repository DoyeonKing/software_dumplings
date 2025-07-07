<template>
  <div class="login-container">
    <div class="login-box" >
      <div style="background-color: white;border-radius: 10px;padding:30px;box-shadow: 0 0 10px rgb(90,16,6)">
        <div style="margin-bottom: 30px; color: #c32f1b;font-size: 20px;font-weight: bold;text-align: center">志愿者管理系统</div>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" style="margin-right: 30px;margin-top: 10px">
<!--      prop要和data.rules对应-->
      <el-form-item label="用户名" prop="username" label-width="80px">
        <el-input size="large"  v-model="data.form.username" autocomplete="off" placeholder="请输入用户名" prefix-icon="User"/>
      </el-form-item>
      <el-form-item show-password label="密码" prop="password" label-width="80px">
        <el-input size="large"  type="password" v-model="data.form.password" autocomplete="off" placeholder="请输入密码" prefix-icon="Lock" />
      </el-form-item>
    </el-form>
        <div>
          <el-button @click="login" size="large" type="danger" style="width: 100%;color:white">登录</el-button>
        </div>
        <div style="text-align: right;margin-top: 15px">没有账号？请 <a style="color:#c32f1b;text-decoration: none"  href="/register" >注册</a></div>
      </div>

    </div>
  </div>
</template>

<script setup>
import {reactive,ref} from "vue";
import {User,Lock} from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const data=reactive({
  form:{},
  rules:{
    username:[{required:true,message:'请输入用户名',trigger:'blur'}],
    password:[{required: true,message:'请输入密码',trigger:'blur'}]
  }
})

const login=()=>{
  formRef.value.validate((valid)=>{
    if(valid){
      request.post('/login',data.form).then(res=>{
        if(res.code==='200'){
          localStorage.setItem('xm-pro-user',JSON.stringify(res.data))
          ElMessage.success('操作成功')
          setTimeout(()=>{
            location.href='/manager/home'
          },500)
        }else{
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const formRef=ref()
</script>

<style scoped>
.login-container{
  height:100vh;
  overflow:hidden;
  background-image: url("@/assets/volunteerLogin.jpg");
  background-size: 110% 100%;
  background-position:-30px 0px;
}
.login-box{
  width:50%;
  height:100%;
  display:flex;
  right:-200px;
  align-items: center;
  position:absolute;
}
</style>