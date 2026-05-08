<template>
  <div style="width: 70%; margin: 20px auto">
    <div v-if="data.articleData && data.articleData.title" class="card" style="padding: 30px">
      <div style="font-size: 24px; font-weight: bold; margin-bottom: 20px">{{ data.articleData.title }}</div>
      <div style="display: flex; align-items: flex-start; margin-bottom: 20px;">
      <img :src="getAvatarUrl(data.articleData.userAvatar)" alt="" style="height: 30px; width: 30px; border-radius: 50%; object-fit: cover; flex-shrink: 0;">
      <div style="margin-left: 10px;">
        <div style="color: #666666">{{ data.articleData.userName }}</div>
        <div style="color: #999; font-size: 12px; margin-top: 2px;">{{ data.articleData.time }}</div>
      </div>
    </div>
      <div class="article-content" v-html="data.articleData.content"></div>
      
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
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666" @click="showArticleReportDialog">
          <img src="@/assets/images/举报.png" alt="举报" style="width: 20px; height: 20px">
          <span>举报</span>
        </div>
      </div>
    </div>

    <div v-else style="text-align: center; padding: 100px; color: #999">
      <el-icon :size="48" style="margin-bottom: 20px"><Loading /></el-icon>
      <div>加载中...</div>
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

    <el-dialog title="举报内容" v-model="data.articleReportVisible" width="500px" destroy-on-close draggable>
      <div style="padding: 20px">
        <div style="margin-bottom: 20px">
          <div style="font-weight: bold; margin-bottom: 10px">请选择举报原因</div>
          <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px">
            <div 
              v-for="type in data.reportTypes" 
              :key="type"
              @click="data.articleReportForm.reportType = type"
              :style="{
                padding: '10px',
                textAlign: 'center',
                background: data.articleReportForm.reportType === type ? '#409EFF' : '#f5f7fa',
                color: data.articleReportForm.reportType === type ? '#fff' : '#333',
                borderRadius: '8px',
                cursor: 'pointer',
                transition: 'all 0.3s'
              }"
            >
              {{ type }}
            </div>
          </div>
        </div>

        <div style="margin-bottom: 20px">
          <div style="font-weight: bold; margin-bottom: 10px">
            详细原因 <span style="color: #999; font-weight: normal">（必填）</span>
          </div>
          <el-input
            v-model="data.articleReportForm.detailReason"
            type="textarea"
            :rows="4"
            placeholder="请描述您遇到的问题"
            maxlength="200"
            show-word-limit
          />
        </div>

        <div style="margin-bottom: 20px">
          <div style="font-weight: bold; margin-bottom: 10px">
            图片补充 <span style="color: #999; font-weight: normal">（选填）</span>
          </div>
          <el-upload
            :action="baseUrl + '/file/upload'"
            :on-success="handleArticleReportFileUpload"
            :on-remove="handleArticleReportFileRemove"
            :file-list="data.articleReportFileList"
            list-type="picture-card"
            :limit="3"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </div>

        <div style="text-align: center">
          <el-button type="primary" @click="submitArticleReport" :loading="data.articleReportSubmitting" style="width: 200px">
            确定
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onMounted, inject} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import { Plus, Loading } from "@element-plus/icons-vue";

// 【新增】注入全局消息状态
const messageState = inject('messageState')

const baseUrl = import.meta.env.VITE_BASE_URL
const commentInputRef = ref(null)

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  articleId: null,
  articleData: {},
  likeCount: 0,
  likeFlag: false,
  collectCount: 0,
  collectFlag: false,
  commentData: [],
  showCommentSection: false,
  content: '',
  articleReportVisible: false,
  articleReportSubmitting: false,
  reportTypes: ['色情低俗', '垃圾广告', '辱骂攻击', '违法犯罪', '时政不实信息', '青少年不宜', '侵犯权益', '开盒网暴'],
  articleReportForm: {
    reportType: '',
    detailReason: '',
    files: ''
  },
  articleReportFileList: [],
  articleReportFileUrls: []
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

const loadArticleDetail = () => {
  request.get('/article/selectById/' + data.articleId).then(res => {
    if (res.code === '200') {
      data.articleData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

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
      // 【新增】点赞操作后，刷新未读消息数
      refreshUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkLike = () => {
  if (!data.articleId) return
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

const loadLikeCount = () => {
  if (!data.articleId) return
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
      // 【新增】收藏操作后，刷新未读消息数
      refreshUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkCollect = () => {
  if (!data.articleId) return
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

const loadCollectCount = () => {
  if (!data.articleId) return
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
    articleId: data.articleId,
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
  if (!data.articleId) return
  request.get('/comment/selectAll', {
    params: {
      articleId: data.articleId
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

const showArticleReportDialog = () => {
  if (!data.user.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  data.articleReportForm = {
    reportType: '',
    detailReason: '',
    files: ''
  }
  data.articleReportFileList = []
  data.articleReportFileUrls = []
  data.articleReportVisible = true
}

const handleArticleReportFileUpload = (response, file, fileList) => {
  if (response.code === '200') {
    data.articleReportFileUrls.push(response.data)
    data.articleReportFileList = fileList
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleArticleReportFileRemove = (file, fileList) => {
  const index = data.articleReportFileList.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    data.articleReportFileUrls.splice(index, 1)
  }
  data.articleReportFileList = fileList
}

const submitArticleReport = () => {
  if (!data.articleReportForm.reportType) {
    ElMessage.warning('请选择举报原因')
    return
  }
  if (!data.articleReportForm.detailReason || !data.articleReportForm.detailReason.trim()) {
    ElMessage.warning('请填写详细原因')
    return
  }

  data.articleReportSubmitting = true
  
  request.post('/articleReport/add', {
    userId: data.user.id,
    articleId: data.articleId,
    userName: data.user.name,
    articleTitle: data.articleData.title,
    reportType: data.articleReportForm.reportType,
    detailReason: data.articleReportForm.detailReason,
    files: data.articleReportFileUrls.join(','),
    status: '待处理'
  }).then(res => {
    data.articleReportSubmitting = false
    if (res.code === '200') {
      ElMessage.success('举报成功，我们会尽快处理')
      data.articleReportVisible = false
      // 【新增】举报成功后刷新未读消息数
      refreshUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    data.articleReportSubmitting = false
    ElMessage.error('提交失败，请稍后重试')
    console.error(err)
  })
}

onMounted(() => {
  data.articleId = router.currentRoute.value.query.id
  if (data.articleId) {
    loadArticleDetail()
    checkLike()
    loadLikeCount()
    checkCollect()
    loadCollectCount()
    loadComment()
  } else {
    ElMessage.error('文章ID不存在')
    router.push('/front/article')
  }
})
</script>

<style scoped>
.article-content {
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
