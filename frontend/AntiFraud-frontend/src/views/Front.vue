<template>
  <div>
    <div class="front-header">
      <div class="front-header-left">
        <img src="../assets/images/logo.png" alt="">
        <div class="title">反诈宣传平台</div>
      </div>
      <div class="front-header-center">
        <el-menu :default-active="router.currentRoute.value.path" router mode="horizontal">
          <el-menu-item index="/front/home">首页</el-menu-item>
          <el-menu-item index="/front/publicity">反诈宣传</el-menu-item>
          <el-menu-item index="/front/activity">反诈活动</el-menu-item>
          <el-menu-item index="/front/notice">系统公告</el-menu-item>
        </el-menu>
      </div>
      <div class="front-header-right">
        <div v-if="!data.user.id">
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </div>
        <div v-else>
          <el-dropdown style="cursor: pointer; height: 60px" :hide-on-click="false">
            <div style="display: flex; align-items: center">
              <img style="width: 40px; height: 40px; border-radius: 50%;" :src="data.user.avatar" alt="">
              <span style="margin-left: 5px; color: #fff; ">{{ data.user.name }}</span><el-icon><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/front/person')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/password')">修改密码</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/activitySignUp')">我的报名</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <div class="main-body">
      <RouterView @updateUser="updateUser" />
    </div>
    <div class="front-footer">
      
    </div>
  </div>
</template>

<script setup>
  import router from "@/router/index.js";
  import { reactive } from "vue";
  import request from "@/utils/request.js";

  const data = reactive({
    user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    top: '',
    noticeData: [],
    noticeTimer: null
  })

  const logout = () => {
    localStorage.removeItem('xm-user')
    router.push('/login')
  }

  const updateUser = () => {
    data.user =  JSON.parse(localStorage.getItem('xm-user') || '{}')
  }

  const loadNotice = () => {
    request.get('/notice/selectAll').then(res => {
      data.noticeData = res.data
      let i = 0
      if (data.noticeData && data.noticeData.length) {
        data.top = data.noticeData[0].content
        if (data.noticeTimer) clearInterval(data.noticeTimer)
        data.noticeTimer = setInterval(() => {
          data.top = data.noticeData[i].content
          i++
          if (i === data.noticeData.length) {
            i = 0
          }
        }, 2500)
      }
    })
  }
  loadNotice()
</script>

<style scoped>
@import "../assets/styles/front.css";
</style>