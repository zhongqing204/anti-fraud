<template>
  <div style="width: 70%; margin: 20px auto">
    <div class="card" style="padding: 20px">
      <div style="position: relative; width: 100%; background: #000; border-radius: 8px; overflow: hidden">
        <video 
          ref="videoRef"
          :src="getVideoUrl(data.videoData.videoUrl)" 
          :poster="getCoverUrl(data.videoData.cover)"
          style="width: 100%; max-height: 600px; display: block"
          controls
          @play="isPlaying = true"
          @pause="isPlaying = false"
        ></video>
      </div>
      
      <div style="margin-top: 20px">
        <div style="font-size: 20px; font-weight: bold">{{ data.videoData.title }}</div>
        <div style="display: flex; align-items: center; margin-top: 15px; color: #999; font-size: 14px">
          <div style="margin-left: auto; display: flex; gap: 20px">
            <div style="display: flex; align-items: center; gap: 5px">
              <el-icon><VideoPlay /></el-icon>
              <span>{{ data.videoData.viewCount || 0 }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 5px">
              <el-icon><Clock /></el-icon>
              <span>{{ data.videoData.createTime }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; display: flex; gap: 40px">
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click="toggleLike">
          <img :src="data.likeFlag ? likeActiveIcon : likeIcon" alt="点赞" style="width: 24px; height: 24px">
          <span :style="{ color: data.likeFlag ? '#409EFF' : '#666' }">点赞 {{ data.likeCount }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click="toggleCollect">
          <img :src="data.collectFlag ? collectActiveIcon : collectIcon" alt="收藏" style="width: 24px; height: 24px">
          <span :style="{ color: data.collectFlag ? '#F56C6C' : '#666' }">收藏 {{ data.collectCount }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click="showCommentInput">
          <img :src="commentIcon" alt="评论" style="width: 24px; height: 24px">
          <span style="color: #666">评论 {{ data.commentData.length }}</span>
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
import {reactive, ref, onMounted, onUnmounted, inject} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import { VideoPlay, FullScreen, Clock } from "@element-plus/icons-vue";
import likeIcon from '@/assets/images/点赞.png';
import likeActiveIcon from '@/assets/images/点赞.png';
import collectIcon from '@/assets/images/收藏.png';
import collectActiveIcon from '@/assets/images/收藏.png';
import commentIcon from '@/assets/images/评论.png';

// 【新增】注入全局消息状态
const messageState = inject('messageState')

const baseUrl = import.meta.env.VITE_BASE_URL
const commentInputRef = ref(null)
const videoRef = ref(null)
const isPlaying = ref(false)

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  videoId: router.currentRoute.value.query.id,
  videoData: {},
  likeCount: 0,
  likeFlag: false,
  collectCount: 0,
  collectFlag: false,
  commentData: [],
  showCommentSection: false,
  content: '',
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

const getVideoUrl = (videoUrl) => {
  if (!videoUrl) return ''
  if (videoUrl.startsWith('http://') || videoUrl.startsWith('https://')) {
    return videoUrl
  }
  return baseUrl + videoUrl
}

/**
 * 【修改】全屏功能
 */
const toggleFullscreen = () => {
  if (!videoRef.value) return
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    videoRef.value.requestFullscreen()
  }
}

const toggleLike = () => {
  request.post('/likes/add', {
    userId: data.user.id,
    videoId: data.videoId,
    userName: data.user.name,
    videoTitle: data.videoData.title
  }).then(res => {
    if (res.code === '200') {
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
  request.get('/likes/selectAll', {
    params: {
      userId: data.user.id,
      videoId: data.videoId
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
  request.get('/likes/selectAll', {
    params: {
      videoId: data.videoId
    }
  }).then(res => {
    if (res.code === '200') {
      data.likeCount = res.data.length || 0
    }
  })
}

const toggleCollect = () => {
  request.post('/collect/add', {
    userId: data.user.id,
    videoId: data.videoId,
    userName: data.user.name,
    videoTitle: data.videoData.title
  }).then(res => {
    if (res.code === '200') {
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
  request.get('/collect/selectAll', {
    params: {
      userId: data.user.id,
      videoId: data.videoId
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
  request.get('/collect/selectAll', {
    params: {
      videoId: data.videoId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectCount = res.data.length || 0
    }
  })
}

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
    videoId: data.videoId,
    userName: data.user.name,
    userAvatar: data.user.avatar,
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
  request.get('/comment/selectAll', {
    params: {
      videoId: data.videoId
    }
  }).then(res => {
    if (res.code === '200') {
      data.commentData = res.data || []
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const incrementViewCount = () => {
  request.put('/video/update', {
    id: data.videoId,
    viewCount: (data.videoData.viewCount || 0) + 1
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

/**
 * 【新增】尝试自动播放视频
 */
const tryAutoPlay = () => {
  if (videoRef.value) {
    videoRef.value.play().then(() => {
      isPlaying.value = true
      ElMessage.success('视频开始播放')
    }).catch(() => {
      // 浏览器阻止自动播放，显示播放按钮
      isPlaying.value = false
      ElMessage.info('点击视频开始播放')
    })
  }
}

onMounted(() => {
  if (data.videoId && data.videoId !== 'undefined') {
    request.get('/video/selectById/' + data.videoId).then(res => {
      if (res.code === '200') {
        data.videoData = res.data
        incrementViewCount()
        checkLike()
        loadLikeCount()
        checkCollect()
        loadCollectCount()
        loadComment()
        
        // 【新增】数据加载完成后尝试自动播放
        setTimeout(() => {
          tryAutoPlay()
        }, 500)
      } else {
        ElMessage.error(res.msg)
      }
    })
  }
})

onUnmounted(() => {
  if (videoRef.value) {
    videoRef.value.pause()
  }
})
</script>
