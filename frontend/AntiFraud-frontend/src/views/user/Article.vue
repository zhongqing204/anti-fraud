<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 70%; margin: 0 auto">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入帖子名称查询" style="width: 400px; height: 40px"></el-input>
      </div>
    </div>
    <div style="width: 70%; margin: 30px auto">
      <div>
        <el-button style="margin-bottom: 20px" @click="changCategory(null)" :type="checkType(null)">全部</el-button>
        <el-button style="margin-bottom: 20px" @click="changCategory(item.id)" :type="checkType(item.id)" v-for="item in data.categoryData">{{ item.name }}</el-button>
      </div>
      <!-- 修改：移除封面图片，改为显示用户头像和帖子信息 -->
      <div class="card" style="margin-bottom: 10px; padding: 20px" v-for="item in data.articleData" :key="item.id">
        <!-- 新增：显示用户头像和用户名 -->
        <div style="display: flex; align-items: center; margin-bottom: 15px">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 30px; width: 30px; border-radius: 50%; object-fit: cover">
          <div style="margin-left: 10px; color: #666666">{{ item.userName }}</div>
          <div style="margin-left: auto; color: #999; font-size: 12px">{{ item.time }}</div>
        </div>
        <!-- 修改：帖子标题可点击跳转 -->
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.id)">{{ item.title }}</div>
        <!-- 修改：显示富文本内容摘要（去除HTML标签） -->
        <div class="article-content-preview">{{ stripHtml(item.content) }}</div>
        <div style="margin-top: 15px; display: flex; gap: 20px; color: #999; font-size: 14px">
          <span style="cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.id)"></span>
        </div>
      </div>
      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>
  </div>

</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  categoryId: null,
  pageNum: 1,
  pageSize: 10,  // 修改：每页显示数量从 5 改为 10
  total: 0,
  articleData: [],
  categoryData: [],
})

// 新增：获取头像URL的辅助函数
const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

// 新增：去除HTML标签获取纯文本的辅助函数
const stripHtml = (html) => {
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ''
}

// 加载分类列表
const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') {
      data.categoryData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCategory()

// 切换分类
const changCategory = (categoryId) => {
  data.categoryId = categoryId
  load()
}

// 检查当前选中的分类
const checkType = (categoryId) => {
  return data.categoryId === categoryId ? 'primary' : ''
}

// 加载帖子列表
const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
      status: '审核通过'  // 只查询审核通过的帖子
    }
  }).then(res => {
    if (res.code === '200') {
      // 修改：MyBatis-Plus 分页返回的数据结构是 records 而不是 list
      data.articleData = res.data?.records || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()

</script>

<style scoped>
.article-content-preview {
  color: #666666;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  word-break: break-all;
}
</style>
