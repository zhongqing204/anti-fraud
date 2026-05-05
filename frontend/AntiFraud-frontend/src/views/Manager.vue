D:\anti-fraud\frontend\AntiFraud-frontend\src\views\Manager.vue
<template>
  <div class="manager-container">
    <div class="manager-header">
      <div class="manager-header-left">
        <img src="../assets/images/logo.png" alt="">
        <div class="title">管理系统</div>
      </div>
      <div class="manager-header-center">
      </div>
      <div class="manager-header-right">
        <el-dropdown style="cursor: pointer">
          <div style="padding-right: 20px; display: flex; align-items: center">
            <img style="width: 40px; height: 40px; border-radius: 50%;" :src="data.user.avatar" alt="">
            <span style="margin-left: 5px; color: white">{{ data.user.name }}</span><el-icon color="#fff"><arrow-down /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/manager/person')">个人资料</el-dropdown-item>
              <el-dropdown-item @click="router.push('/manager/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <!-- 下面部分开始 -->
    <div style="display: flex">
      <div class="manager-main-left">
        <el-menu :default-active="router.currentRoute.value.path"
                 :default-openeds="['1', '2', '3', '4', '5', '6', '7']"
                 router
        >
          <el-menu-item index="/manager/home">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>
          
          <!-- 【修改】分类管理 -->
          <el-sub-menu index="1">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span>分类管理</span>
            </template>
            <el-menu-item index="/manager/category">反诈分类</el-menu-item>
          </el-sub-menu>
          
          <!-- 【修改】宣传管理 -->
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>宣传管理</span>
            </template>
            <el-menu-item index="/manager/publicity">反诈宣传</el-menu-item>
            <el-menu-item index="/manager/publicityLikes">宣传点赞</el-menu-item>
            <el-menu-item index="/manager/publicityCollects">宣传收藏</el-menu-item>
            <el-menu-item index="/manager/publicityComments">宣传评论</el-menu-item>
          </el-sub-menu>
          
          <!-- 【修改】视频管理 -->
          <el-sub-menu index="3">
            <template #title>
              <el-icon><VideoCamera /></el-icon>
              <span>视频管理</span>
            </template>
            <el-menu-item index="/manager/video">反诈视频</el-menu-item>
            <el-menu-item index="/manager/videoLikes">视频点赞</el-menu-item>
            <el-menu-item index="/manager/videoCollects">视频收藏</el-menu-item>
            <el-menu-item index="/manager/videoComments">视频评论</el-menu-item>
          </el-sub-menu>
          
          <!-- 【修改】论坛管理 -->
          <el-sub-menu index="4">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span>论坛管理</span>
            </template>
            <el-menu-item index="/manager/article">帖子管理</el-menu-item>
            <el-menu-item index="/manager/likes">点赞信息</el-menu-item>
            <el-menu-item index="/manager/collect">收藏信息</el-menu-item>
            <el-menu-item index="/manager/comment">评论信息</el-menu-item>
            <el-menu-item index="/manager/articleReport">帖子举报管理</el-menu-item>
          </el-sub-menu>
          
          <!-- 【修改】活动管理 -->
          <el-sub-menu index="5">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>活动管理</span>
            </template>
            <el-menu-item index="/manager/activity">反诈活动</el-menu-item>
            <el-menu-item index="/manager/activitySignUp">报名管理</el-menu-item>
            <el-menu-item index="/manager/activityLikes">活动点赞</el-menu-item>
            <el-menu-item index="/manager/activityCollects">活动收藏</el-menu-item>
            <el-menu-item index="/manager/activityComments">活动评论</el-menu-item>
          </el-sub-menu>
          
          <!-- 【修改】举报管理 -->
          <el-sub-menu index="6">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>举报管理</span>
            </template>
            <el-menu-item index="/manager/report">举报管理</el-menu-item>
          </el-sub-menu>
          
          <!-- 【修改】系统管理 -->
          <el-sub-menu index="7">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/manager/notice">系统公告</el-menu-item>
          </el-sub-menu>
          
          <!-- 【修改】用户管理 -->
          <el-sub-menu index="8">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/manager/admin">管理员信息</el-menu-item>
            <el-menu-item index="/manager/user">用户信息</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      <div class="manager-main-right">
        <RouterView @updateUser="updateUser" />
      </div>
    </div>
    <!-- 下面部分结束 -->


  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-admin') || '{}')
})

const logout = () => {
  localStorage.removeItem('xm-admin')
  router.push('/login')
}

const updateUser = () => {
  data.user =  JSON.parse(localStorage.getItem('xm-admin') || '{}')
}

if (!data.user.id) {
  logout()
  ElMessage.error('请登录！')
}
</script>

<style scoped>
@import "../assets/styles/manager.css";
</style>
