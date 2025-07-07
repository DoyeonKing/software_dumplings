<template>
  <div>
<!--头部开始-->
    <div style="height: 60px;background-color: #8763f6 ;display: flex;align-items: center">
<!--标题-->
      <div style="width: 200px;display:flex;align-items:center">
        <span style="margin-right: 10px"></span>
        <img src="../assets/volunteew.png" alt="" style="width: 40px;height:40px">
        <span style="margin-left: 10px; font-size:20px;color:white">后台管理系统</span>

      </div>
      <div style="flex: 1"></div>
<!--头像用户名-->
      <div style="width:fit-content;display: flex;align-items: center;padding-right: 10px">
        <img src="../assets/logo.svg" alt="" style="width:40px;height:40px">
        <span style="margin-left: 5px;color: white">{{ data.user.name }}</span>
      </div>


    </div>
<!--头部结束-->

<!--下面部分开始-->
    <div style="display:flex">
      <!--左侧导航菜单开始-->
      <div style="width:200px;border-right:1px solid #e8e0fd;min-height:calc(100vh - 60px)">
          <el-menu router :default-active="router.currentRoute.value.path" :default-openeds="['1']" style="border: 0">          <el-menu-item index="/manager/home"><el-icon><House/></el-icon>系统首页</el-menu-item>
            <el-sub-menu index="1">
            <template #title>
              <el-icon><User/></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item>管理员信息</el-menu-item>
            <el-menu-item index="/manager/employee">员工信息</el-menu-item>
          </el-sub-menu>


          <el-menu-item index="/manager/Data"><el-icon><DataAnalysis/></el-icon>数据统计</el-menu-item>
          <el-menu-item index="/login"><el-icon><SwitchButton/></el-icon>退出登录</el-menu-item>
          <el-menu-item ><el-icon><UserFilled/></el-icon>用户信息</el-menu-item>
        </el-menu>
      </div>
      <!--左侧导航菜单结束-->

      <!--右侧导航菜单开始-->
      <div style="flex:1;width: 0;background: #f3f2fb;padding: 10px">
        <RouterView/>
      </div>
      <!--右侧导航菜单结束-->
    </div>
<!--下面部分结束-->

  </div>
</template>

<script setup>
import router from "@/router/index.js";
import {DataAnalysis, House, InfoFilled, SwitchButton, User, UserFilled} from "@element-plus/icons-vue";
import {reactive} from "vue";



const data=reactive({
  user:JSON.parse(localStorage.getItem('xm-pro-user'))
})
</script>

<style>
.el-menu .is-active {
  background-color: #f3ecfb !important;
}
</style>