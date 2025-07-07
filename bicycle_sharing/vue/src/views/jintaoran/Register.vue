<template>
  <div class="login-container">
    <div class="login-box" >
      <div style="background-color: white;border-radius: 10px;padding:30px;box-shadow: 0 0 10px rgb(90,16,6)">
        <div style="margin-bottom: 30px; color: #c32f1b;font-size: 20px;font-weight: bold;text-align: center">欢迎注册</div>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" style="margin-right: 30px;margin-top: 10px">
<!--      prop要和data.rules对应-->
      <el-form-item label="用户名" prop="username" label-width="80px">
        <el-input size="large"  v-model="data.form.username" autocomplete="off" placeholder="请输入用户名" prefix-icon="User"/>
      </el-form-item>
      <el-form-item show-password label="密码" prop="password" label-width="80px">
        <el-input size="large"  type="password" v-model="data.form.password" autocomplete="off" placeholder="请输入密码" prefix-icon="Lock" />
      </el-form-item>
        <el-form-item prop="confirmPassword">
            <el-input show-password size="large" v-model="data.form.confirmPassword" placeholder="请确认密码" prefix-icon="Lock"></el-input>
          </el-form-item>
        <el-form-item label="角色" prop="role" label-width="80px">
        <el-input v-model="data.form.role" autocomplete="off" />
      </el-form-item>
      <el-form-item label="姓名" prop="name" label-width="80px">
        <el-input v-model="data.form.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="性别" prop="sex" label-width="80px">
        <el-radio-group v-model="data.form.sex">
          <el-radio label="男" value="男"></el-radio>
          <el-radio label="女" value="女"></el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="工号" prop="no" label-width="80px">
        <el-input v-model="data.form.no" autocomplete="off" />
      </el-form-item>
      <el-form-item label="年龄" prop="age" label-width="80px">
        <el-input-number v-model="data.form.age" autocomplete="off" />
      </el-form-item>
      <el-form-item label="个人简介" label-width="80px">
        <el-input :rows="3" type="textarea" v-model="data.form.description" autocomplete="off" />
      </el-form-item>
      <el-form-item label="部门id" label-width="80px">
        <el-input-number v-model="data.form.departmentId" autocomplete="off" />
      </el-form-item>
    </el-form>
        <div>
          <el-button @click="register" size="large" type="danger" style="width: 100%;color:white">注册</el-button>
        </div>
        <div style="text-align: right;margin-top: 15px">已有账号？请 <a style="color:#c32f1b;text-decoration: none"  href="/login" >登录</a></div>
      </div>

    </div>
  </div>
</template>

<script setup>
import {reactive,ref} from "vue";
import {User,Lock} from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次确认密码'))
  } else if (value !== data.form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const data=reactive({
  form:{},
  rules:{
    username:[{required:true,message:'请输入用户名',trigger:'blur'}],
    password:[{required: true,message:'请输入密码',trigger:'blur'}],
    role:[{required:true,message:'请输入角色',trigger:'blur'},],
    name:[{required:true,message:'请输入姓名',trigger:'blur'},],
    sex:[{required:true,message:'请输入性别',trigger:'blur'},],
    no:[{required:true,message:'请输入工号',trigger:'blur'},],
    age:[{required:true,message:'请输入年龄',trigger:'blur'},],
    confirmPassword: [
      { validator:  validatePass, trigger: 'blur'}
    ]
  }
})

const register=()=>{
  formRef.value.validate((valid)=>{
    if(valid){
      request.post('/register',data.form).then(res=>{
        if(res.code==='200'){
          localStorage.setItem('xm-pro-user',JSON.stringify(res.data))
          ElMessage.success('操作成功')
          setTimeout(()=>{
            location.href='/login'
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