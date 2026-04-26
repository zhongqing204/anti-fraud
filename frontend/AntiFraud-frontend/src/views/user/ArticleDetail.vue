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
      <div class="article-content" style="line-height: 1.8" v-html="renderedContent"></div>
      
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
      <div v-show="data.showCommentSection" style="display: flex; align-items: center; gap: 10px">
        <el-input 
          v-model="data.content" 
          ref="commentInputRef"
          placeholder="请输入评论内容"
          style="flex: 1"
        ></el-input>
        <el-button type="primary" @click="submit" :disabled="!data.content || !data.content.trim()">发布</el-button>
      </div>
      
      <div style="font-weight: 400; font-size: 20px; margin-top: 30px">全部评论（{{ data.commentData.length }}）</div>
      <div style="margin-top: 20px; display: flex; grid-gap: 20px; flex-direction: column">
        <div v-for="item in data.commentData" :key="item.id" style="display: flex; grid-gap: 20px; padding: 15px; border-bottom: 1px solid #eee">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover">
          <div style="flex: 1">
            <div style="font-weight: bold; font-size: 14px">{{ item.userName }}</div>
            <div style="margin-top: 8px; color: #333">{{ item.content }}</div>
            <div style="margin-top: 8px; display: flex; align-items: center; gap: 20px">
              <span style="color: #999; font-size: 12px">{{ item.time }}</span>
              <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666; font-size: 13px" @click="toggleCommentLike(item)">
                <img src="@/assets/images/点赞.png" alt="点赞" style="width: 16px; height: 16px" :style="{ filter: item.liked ? 'none' : 'grayscale(100%)', opacity: item.liked ? 1 : 0.5 }">
                <span :style="{ color: item.liked ? '#409EFF' : '' }">{{ item.likeCount || 0 }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666; font-size: 13px" @click="toggleCommentCollect(item)">
                <img src="@/assets/images/收藏.png" alt="收藏" style="width: 16px; height: 16px" :style="{ filter: item.collected ? 'none' : 'grayscale(100%)', opacity: item.collected ? 1 : 0.5 }">
                <span :style="{ color: item.collected ? '#F56C6C' : '' }">{{ item.collectCount || 0 }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666; font-size: 13px" @click="replyToComment(item)">
                <img src="@/assets/images/评论.png" alt="回复" style="width: 16px; height: 16px">
                <span>回复</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import {reactive, ref, computed} from "vue";
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
  replyToUserId: null,
  replyToUserName: null,
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

const fixVideoUrl = (content) => {
  if (!content) return ''
  return content.replace(
    /<source\s+src="([^"]+)"([^>]*)>/g,
    (match, src, attrs) => {
      let videoUrl = src
      if (!videoUrl.startsWith('http://') && !videoUrl.startsWith('https://')) {
        videoUrl = encodeURI(baseUrl + videoUrl)
      }
      return `<source src="${videoUrl}"${attrs}>`
    }
  )
}

const renderedContent = computed(() => {
  return fixVideoUrl(data.articleData.content)
})

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
  request.post('/likes/add', {
    userId: data.user.id,
    articleId: data.articleId,
    userName: data.user.name,
    articleTitle: data.articleData.title
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
  request.get('/likes/selectAll', {
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
  request.get('/likes/selectAll', {
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
    articleId: data.articleId,
    userName: data.user.name,
    articleTitle: data.articleData.title
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
  
  // 构建评论内容（如果是回复，添加@用户名）
  let finalContent = data.content.trim()
  if (data.replyToUserId && data.replyToUserName) {
    finalContent = `回复 @${data.replyToUserName}：${finalContent}`
  }
  
  request.post('/comment/add', {
    userId: data.user.id,
    articleId: data.articleId,
    content: finalContent,
    replyToUserId: data.replyToUserId,
    replyToUserName: data.replyToUserName
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论成功')
      data.content = null
      data.replyToUserId = null
      data.replyToUserName = null
      data.showCommentSection = true
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
      const comments = res.data || []
      data.commentData = comments
      
      // 批量查询当前用户对评论的点赞和收藏状态
      const commentIds = comments.map(c => c.id)
      if (commentIds.length > 0 && data.user.id) {
        // 查询用户对评论的点赞状态
        request.get('/commentLikes/selectAll', {
          params: { userId: data.user.id }
        }).then(likeRes => {
          if (likeRes.code === '200') {
            const userLikes = likeRes.data || []
            const likedCommentIds = new Set(userLikes.map(l => l.commentId))
            
            // 查询用户对评论的收藏状态
            request.get('/commentCollects/selectAll', {
              params: { userId: data.user.id }
            }).then(collectRes => {
              if (collectRes.code === '200') {
                const userCollects = collectRes.data || []
                const collectedCommentIds = new Set(userCollects.map(c => c.commentId))
                
                // 设置每条评论的点赞和收藏状态及数量
                comments.forEach(comment => {
                  comment.liked = likedCommentIds.has(comment.id)
                  comment.collected = collectedCommentIds.has(comment.id)
                  comment.likeCount = userLikes.filter(l => l.commentId === comment.id).length
                  comment.collectCount = userCollects.filter(c => c.commentId === comment.id).length
                })
                
                data.commentData = comments
              }
            })
          }
        })
      }
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadComment()

// 评论点赞/取消点赞
const toggleCommentLike = (comment) => {
  request.post('/commentLikes/add', {
    userId: data.user.id,
    commentId: comment.id
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      loadComment()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 评论收藏/取消收藏
const toggleCommentCollect = (comment) => {
  request.post('/commentCollects/add', {
    userId: data.user.id,
    commentId: comment.id
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      loadComment()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 回复评论
const replyToComment = (comment) => {
  data.replyToUserId = comment.userId
  data.replyToUserName = comment.userName
  data.showCommentSection = true
  data.content = ''
  setTimeout(() => {
    commentInputRef.value?.focus()
  }, 100)
}
</script>

<style scoped>
.article-content {
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
