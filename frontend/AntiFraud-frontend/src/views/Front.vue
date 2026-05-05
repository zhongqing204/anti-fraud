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
          <el-menu-item index="/front/video">反诈视频</el-menu-item>
          <el-menu-item index="/front/activity">反诈活动</el-menu-item>
          <el-menu-item index="/front/article">反诈论坛</el-menu-item>
          <el-menu-item index="/front/report">反诈举报</el-menu-item>
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
              <div style="position: relative; margin-right: 15px; cursor: pointer" @click="router.push('/front/myMessage')">
                <el-icon :size="24"><Bell /></el-icon>
                <div v-if="data.unreadCount > 0" style="position: absolute; top: -5px; right: -5px; background: #f56c6c; color: #fff; border-radius: 50%; width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; font-size: 12px">
                  {{ data.unreadCount > 99 ? '99+' : data.unreadCount }}
                </div>
              </div>
              <img style="width: 40px; height: 40px; border-radius: 50%;" :src="data.user.avatar" alt="">
              <span style="margin-left: 5px; color: #fff; ">{{ data.user.name }}</span><el-icon><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/front/person')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/password')">修改密码</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myActivitySignUp')">我的报名</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myArticle')">我的帖子</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myLikes')">我的点赞</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myCollect')">我的收藏</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myComment')">我的评论</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myArticleReport')">我的帖子举报</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myReport')">我的举报</el-dropdown-item>
                <el-dropdown-item @click="router.push('/front/myMessage')">
                  我的消息
                  <el-badge v-if="data.unreadCount > 0" :value="data.unreadCount" :max="99" style="margin-left: 10px" />
                </el-dropdown-item>
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
  import { reactive, onMounted, onUnmounted } from "vue";
  import request from "@/utils/request.js";
  import { Bell } from "@element-plus/icons-vue";

  const data = reactive({
    user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    top: '',
    noticeData: [],
    noticeTimer: null,
    unreadCount: 0
  });

  const logout = () => {
    localStorage.removeItem('xm-user');
    router.push('/login');
  };

  const updateUser = () => {
    data.user = JSON.parse(localStorage.getItem('xm-user') || '{}');
    if (data.user.id) {
      loadUnreadCount();
    }
  };

  const loadNotice = () => {
    request.get('/notice/selectAll').then(res => {
      data.noticeData = res.data;
      let i = 0;
      if (data.noticeData && data.noticeData.length) {
        data.top = data.noticeData[0].content;
        if (data.noticeTimer) clearInterval(data.noticeTimer);
        data.noticeTimer = setInterval(() => {
          data.top = data.noticeData[i].content;
          i++;
          if (i === data.noticeData.length) {
            i = 0;
          }
        }, 2500);
      }
    });
  };

  const loadUnreadCount = () => {
    if (!data.user.id) return;
    request.get('/message/unreadCount', {
      params: { userId: data.user.id }
    }).then(res => {
      if (res.code === '200') {
        data.unreadCount = res.data || 0;
      }
    });
  };

  onMounted(() => {
    loadNotice();
    if (data.user.id) {
      loadUnreadCount();
      data.unreadTimer = setInterval(loadUnreadCount, 30000);
    }
  });

  onUnmounted(() => {
    if (data.noticeTimer) clearInterval(data.noticeTimer);
    if (data.unreadTimer) clearInterval(data.unreadTimer);
  });

  loadNotice();
  if (data.user.id) {
    loadUnreadCount();
    data.unreadTimer = setInterval(loadUnreadCount, 30000);
  }
</script>

<style scoped>
@import "../assets/styles/front.css";
</style>
