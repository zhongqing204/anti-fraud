<template>
  <div style="margin: 20px auto; width: 60%">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.newsData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.newsData.createTime }}</span>
      </div>
      <div 
        class="vditor-reset"
        style="margin-top: 50px; line-height: 1.8;"
        v-html="renderedContent"
      ></div>
      
      <!-- 【新增】点赞、收藏、评论功能区域 -->
      <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; display: flex; gap: 40px">
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666" @click="toggleLike">
          <img src="@/assets/images/点赞.png" alt="点赞" style="width: 20px; height: 20px" :style="{ filter: data.likeFlag ? 'none' : 'grayscale(100%)', opacity: data.likeFlag ? 1 : 0.5 }">
          <span :style="{ color: data.likeFlag ? '#409EFF' : '' }">点赞 {{ data.likeCount }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666" @click="toggleCollect">
          <img src="@/assets/images/收藏.png" alt="收藏" style="width: 20px; height: 20px" :style="{ filter: data.collectFlag ? 'none' : 'grayscale(100%)', opacity: data.collectFlag ? 1 : 0.5 }">
          <span :style="{ color: data.collectFlag ? '#F56C6C' : '' }">收藏 {{ data.collectCount }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666" @click="showCommentInput">
          <img src="@/assets/images/评论.png" alt="评论" style="width: 20px; height: 20px">
          <span>评论 {{ data.commentData.length }}</span>
        </div>
      </div>
    </div>

    <!-- 【新增】评论区 -->
    <div class="card" style="margin-top: 10px; padding: 30px">
      <div v-show="data.showCommentSection" style="display: flex; align-items: center; gap: 10px">
        <el-input 
          v-model="data.content" 
          ref="commentInputRef"
          placeholder="请输入评论内容"
          style="flex: 1"
        ></el-input>
        <el-button type="primary" @click="submit" :disabled="!data.content || !data.content.trim()">发布</el-button>
      </div>
      
      <div style="margin-top: 20px">
        <div v-for="item in data.commentData" :key="item.id" style="padding: 15px 0; border-bottom: 1px solid #eee">
          <div style="display: flex; align-items: center; margin-bottom: 10px">
            <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 25px; width: 25px; border-radius: 50%; object-fit: cover">
            <div style="margin-left: 10px">
              <div style="color: #666; font-size: 14px">{{ item.userName }}</div>
              <div style="color: #999; font-size: 12px; margin-top: 2px">{{ item.time }}</div>
            </div>
          </div>
          <div style="margin-bottom: 10px; color: #333; line-height: 1.6">{{ item.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref, computed, onMounted, inject} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

// 【新增】注入全局消息状态
const messageState = inject('messageState')

const baseUrl = import.meta.env.VITE_BASE_URL
// 【新增】评论输入框引用
const commentInputRef = ref(null)

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  newsId: router.currentRoute.value.query.id,
  newsData: {},
  content: null,
  // 【新增】点赞相关数据
  likeCount: 0,
  likeFlag: false,
  // 【新增】收藏相关数据
  collectCount: 0,
  collectFlag: false,
  // 【新增】评论相关数据
  commentData: [],
  showCommentSection: false,
})

// 【修改】直接返回HTML内容，不再使用markdown-it渲染
const renderedContent = computed(() => {
  if (!data.newsData.content) return ''
  return data.newsData.content
})

// 【新增】获取头像URL方法
const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

const loadActivity = () => {
  if (!data.newsId || data.newsId === 'undefined') {
    ElMessage.error('参数错误，无法加载内容')
    return
  }
  request.get('/publicity/selectById/' + data.newsId).then(res => {
    if (res.code === '200') {
      data.newsData = res.data
      request.put('/publicity/update', data.newsData)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 【新增】点赞功能
const toggleLike = () => {
  request.post('/likes/add', {
    userId: data.user.id,
    publicityId: data.newsId,
    userName: data.user.name,
    publicityTitle: data.newsData.title
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      checkLike()
      loadLikeCount()
      // 【新增】点赞操作后，刷新未读消息数
      refreshUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkLike = () => {
  if (!data.newsId) return
  request.get('/likes/selectAll', {
    params: {
      userId: data.user.id,
      publicityId: data.newsId
    }
  }).then(res => {
    if (res.code === '200') {
      data.likeFlag = !!res.data.length;
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadLikeCount = () => {
  if (!data.newsId) return
  request.get('/likes/selectAll', {
    params: {
      publicityId: data.newsId
    }
  }).then(res => {
    if (res.code === '200') {
      data.likeCount = res.data.length || 0
    }
  })
}

// 【新增】收藏功能
const toggleCollect = () => {
  request.post('/collect/add', {
    userId: data.user.id,
    publicityId: data.newsId,
    userName: data.user.name,
    publicityTitle: data.newsData.title
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      checkCollect()
      loadCollectCount()
      // 【新增】收藏操作后，刷新未读消息数
      refreshUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkCollect = () => {
  if (!data.newsId) return
  request.get('/collect/selectAll', {
    params: {
      userId: data.user.id,
      publicityId: data.newsId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectFlag = !!res.data.length;
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadCollectCount = () => {
  if (!data.newsId) return
  request.get('/collect/selectAll', {
    params: {
      publicityId: data.newsId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectCount = res.data.length || 0
    }
  })
}

// 【新增】评论功能
const showCommentInput = () => {
  data.showCommentSection = !data.showCommentSection
  if (data.showCommentSection) {
    setTimeout(() => {
      commentInputRef.value?.focus()
    }, 100)
  }
}

const submit = () => {
  if (!data.content || !data.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  request.post('/comment/add', {
    userId: data.user.id,
    publicityId: data.newsId,
    userName: data.user.name,
    content: data.content,
    time: new Date().toLocaleString()
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论成功')
      data.content = ''
      loadComment()
      // 【新增】评论操作后，刷新未读消息数
      refreshUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadComment = () => {
  if (!data.newsId) return
  request.get('/comment/selectAll', {
    params: {
      publicityId: data.newsId
    }
  }).then(res => {
    if (res.code === '200') {
      data.commentData = res.data || []
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 【新增】刷新未读消息数
const refreshUnreadCount = () => {
  if (!data.user.id) return
  request.get('/message/unreadCount', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      const count = res.data || 0
      messageState.updateUnreadCount(count)
    }
  })
}

onMounted(() => {
  loadActivity()
  // 【新增】初始化点赞、收藏、评论数据
  checkLike()
  loadLikeCount()
  checkCollect()
  loadCollectCount()
  loadComment()
})
</script>

<style scoped>
/* 富文本内容样式 */
:deep(.vditor-reset) {
  p {
    margin: 1em 0;
    line-height: 1.8;
  }
  
  br {
    display: block;
    margin: 0.5em 0;
  }
  
  ul, ol {
    padding-left: 20px;
    margin: 1em 0;
  }
  
  li {
    margin: 0.5em 0;
  }
  
  blockquote {
    margin: 1em 0;
    padding: 10px 20px;
    border-left: 4px solid #ccc;
    background-color: #f8f8f8;
  }
  
  pre {
    margin: 1em 0;
    padding: 10px;
    background-color: #f6f8fa;
    border-radius: 3px;
    overflow-x: auto;
  }
  
  table {
    border-collapse: collapse;
    margin: 1em 0;
    width: 100%;
  }
  
  th, td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
  }
  
  th {
    background-color: #f6f8fa;
    font-weight: bold;
  }
  
  h1, h2, h3, h4, h5, h6 {
    margin: 1em 0;
    font-weight: bold;
  }
  
  h1 { font-size: 2em; }
  h2 { font-size: 1.5em; }
  h3 { font-size: 1.3em; }
  h4 { font-size: 1.1em; }
  
  strong {
    font-weight: bold;
  }
  
  em {
    font-style: italic;
  }
  
  a {
    color: #409EFF;
    text-decoration: none;
  }
  
  a:hover {
    text-decoration: underline;
  }
  
  img {
    max-width: 100%;
    height: auto;
  }
  
  hr {
    border: none;
    border-top: 1px solid #eee;
    margin: 2em 0;
  }
}
</style>
