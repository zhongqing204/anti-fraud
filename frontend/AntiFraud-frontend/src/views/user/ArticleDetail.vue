<template>
  <div style="margin: 20px auto; width: 60%">
    <div class="card" style="padding: 30px">
      <div style="display: flex; align-items: center; margin-bottom: 20px">
        <img :src="getAvatarUrl(data.articleData.userAvatar)" alt="" style="width: 50px; height: 50px; border-radius: 50%; object-fit: cover">
        <div style="margin-left: 15px">
          <div style="font-weight: bold; font-size: 16px">{{ data.articleData.userName }}</div>
          <div style="color: #999; font-size: 12px; margin-top: 5px">{{ data.articleData.time }}</div>
        </div>
      </div>
       <div style="font-size: 20px; font-weight: bold; margin-bottom: 20px">{{ data.articleData.title }}</div>
      <div class="article-content" style="line-height: 1.8" v-html="data.articleData.content"></div>
      
      <div style="display: flex; gap: 40px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee">
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

    <div class="card" style="margin-top: 10px; padding: 30px">
      <!-- 输入框和发布按钮可收起 -->
      <div v-show="data.showCommentSection" style="display: flex; align-items: center; gap: 10px">
        <el-input 
          v-model="data.content" 
          ref="commentInputRef"
          placeholder="请输入评论内容"
          style="flex: 1"
        ></el-input>
        <el-button type="primary" @click="submit" :disabled="!data.content || !data.content.trim()">发布</el-button>
      </div>
      
      <!-- 评论列表始终显示 -->
      <div style="font-weight: 400; font-size: 20px; margin-top: 30px">全部评论（{{ data.commentData.length }}）</div>
      <div style="margin-top: 20px; display: flex; grid-gap: 20px; flex-direction: column">
        <div v-for="item in data.commentData" :key="item.id" style="display: flex; grid-gap: 20px; padding: 15px; border-bottom: 1px solid #eee">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover">
          <div style="flex: 1">
            <div style="font-weight: bold; font-size: 14px">{{ item.userName }}</div>
            <div style="margin-top: 8px; color: #333">{{ item.content }}</div>
            <div style="margin-top: 8px; color: #999; font-size: 12px">{{ item.time }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const baseUrl = import.meta.env.VITE_BASE_URL
const commentInputRef = ref(null)

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  articleId: router.currentRoute.value.query.id,
  articleData: {},
  likeFlag: false,
  likeCount: 0,
  collectFlag: false,
  collectCount: 0,
  content: null,
  commentData: [],
  showCommentSection: false,
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

const showCommentInput = () => {
  data.showCommentSection = !data.showCommentSection
  if (data.showCommentSection) {
    setTimeout(() => {
      commentInputRef.value?.focus()
    }, 100)
  }
}

const loadArticle = () => {
  request.get('/article/selectById/' + data.articleId).then(res => {
    if (res.code === '200') {
      data.articleData = res.data
      data.articleData.views = data.articleData.views + 1
      request.put('/article/update', data.articleData)
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadArticle()

const toggleLike = () => {
  request.post('/like/add', {
    userId: data.user.id,
    articleId: data.articleId
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      checkLike()
      loadLikeCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkLike = () => {
  request.get('/like/selectAll', {
    params: {
      userId: data.user.id,
      articleId: data.articleId
    }
  }).then(res => {
    if (res.code === '200') {
      data.likeFlag = !!res.data.length;
    } else {
      ElMessage.error(res.msg)
    }
  })
}
checkLike()

const loadLikeCount = () => {
  request.get('/like/selectAll', {
    params: {
      articleId: data.articleId
    }
  }).then(res => {
    if (res.code === '200') {
      data.likeCount = res.data.length || 0
    }
  })
}
loadLikeCount()

const toggleCollect = () => {
  request.post('/collect/add', {
    userId: data.user.id,
    articleId: data.articleId
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      checkCollect()
      loadCollectCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkCollect = () => {
  request.get('/collect/selectAll', {
    params: {
      userId: data.user.id,
      articleId: data.articleId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectFlag = !!res.data.length;
    } else {
      ElMessage.error(res.msg)
    }
  })
}
checkCollect()

const loadCollectCount = () => {
  request.get('/collect/selectAll', {
    params: {
      articleId: data.articleId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectCount = res.data.length || 0
    }
  })
}
loadCollectCount()

const submit = () => {
  if (!data.content || !data.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  request.post('/comment/add', {
    userId: data.user.id,
    articleId: data.articleId,
    content: data.content
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论成功')
      data.content = null
      loadComment()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadComment = () => {
  request.get('/comment/selectAll', {
    params: {
      articleId: data.articleId
    }
  }).then(res => {
    if (res.code === '200') {
      data.commentData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadComment()
</script>

<style scoped>
.article-content {
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
