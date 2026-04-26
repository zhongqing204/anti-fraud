<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto">
        <div style="font-size: 20px; font-weight: bold">我的收藏（{{ data.total }}）</div>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <div class="card" style="margin-bottom: 10px; padding: 20px" v-for="item in data.tableData" :key="item.id">
        <div style="display: flex; align-items: center; margin-bottom: 15px">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 30px; width: 30px; border-radius: 50%; object-fit: cover">
          <div style="margin-left: 10px; color: #666666">{{ item.userName }}</div>
          <div style="margin-left: auto; color: #999; font-size: 12px">{{ formatTime(item.time) }}</div>
        </div>
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.id)">{{ item.title }}</div>
        <div class="article-content-preview">{{ stripHtml(item.content) }}</div>
        <div style="margin-top: 15px; display: flex; gap: 40px; color: #999; font-size: 14px">
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="toggleLike(item)">
            <img src="@/assets/images/点赞.png" alt="点赞" style="width: 16px; height: 16px" :style="{ filter: item.liked ? 'none' : 'grayscale(100%)', opacity: item.liked ? 1 : 0.5 }">
            <span :style="{ color: item.liked ? '#409EFF' : '' }">{{ item.likeCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="toggleCollect(item)">
            <img src="@/assets/images/收藏.png" alt="收藏" style="width: 16px; height: 16px" :style="{ filter: 'none', opacity: 1 }">
            <span style="color: #F56C6C">{{ item.collectCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="router.push('/front/articleDetail?id=' + item.id)">
            <img src="@/assets/images/评论.png" alt="评论" style="width: 16px; height: 16px">
            <span>{{ item.commentCount || 0 }}</span>
          </div>
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
  pageNum: 1,
  pageSize: 10,
  tableData: [],
  total: 0,
  collectArticleIds: []
})

// 获取头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

// 去除HTML标签
const stripHtml = (html) => {
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ''
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 点赞/取消点赞
const toggleLike = (item) => {
  request.post('/likes/add', {
    userId: data.user.id,
    articleId: item.id,
    userName: data.user.name,
    articleTitle: item.title
  }).then(res => {
    if (res.code === '200') {
      load()
    }
  })
}

// 取消收藏
const toggleCollect = (item) => {
  request.post('/collect/add', {
    userId: data.user.id,
    articleId: item.id,
    userName: data.user.name,
    articleTitle: item.title
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已取消收藏')
      load()
    }
  })
}

const load = () => {
  // 先查询用户的所有收藏记录，获取文章ID列表
  request.get('/collect/selectAll', {
    params: {
      userId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      const collects = res.data || []
      data.collectArticleIds = collects.map(c => c.articleId)
      
      if (data.collectArticleIds.length === 0) {
        data.tableData = []
        data.total = 0
        return
      }
      
      // 根据文章ID列表分页查询文章详情
      loadArticlesByPage()
    }
  })
}

// 分页加载文章
const loadArticlesByPage = () => {
  const start = (data.pageNum - 1) * data.pageSize
  const end = start + data.pageSize
  const pageArticleIds = data.collectArticleIds.slice(start, end)
  
  if (pageArticleIds.length === 0) {
    data.tableData = []
    return
  }
  
  // 逐个查询文章详情
  const promises = pageArticleIds.map(id => 
    request.get('/article/selectById/' + id)
  )
  
  Promise.all(promises).then(results => {
    data.tableData = results
      .filter(res => res.code === '200' && res.data)
      .map(res => res.data)
    data.total = data.collectArticleIds.length
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
  line-clamp: 4;
  -webkit-box-orient: vertical;
  word-break: break-all;
}
</style>
