<template>
  <div class="article-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-container">
        <div class="search-input-group">
          <el-input 
            prefix-icon="Search" 
            v-model="data.title" 
            @keyup.enter="load" 
            clearable 
            @clear="load" 
            placeholder="请输入帖子名称查询" 
            class="search-input"
          ></el-input>
          <el-popover trigger="manual" :width="300" v-model:visible="data.filterVisible">
            <template #reference>
              <el-button :type="data.hasFilter ? 'primary' : ''" @click="data.filterVisible = !data.filterVisible" class="filter-btn">
                <el-icon><Filter /></el-icon>
                筛选
              </el-button>
            </template>
            <div class="filter-content" @click.stop>
              <div class="filter-item">
                <div class="filter-label">时间</div>
                <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable class="filter-select" :teleported="false">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="最近一周" value="week" />
                  <el-option label="最近一月" value="month" />
                  <el-option label="最近三月" value="threeMonths" />
                </el-select>
              </div>
              <div class="filter-item">
                <div class="filter-label">内容类型</div>
                <el-select v-model="data.contentType" placeholder="请选择内容类型" clearable class="filter-select" :teleported="false">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="图文" value="text" />
                  <el-option label="视频" value="video" />
                </el-select>
              </div>
              <div class="filter-actions">
                <el-button size="small" @click="resetFilter">重置</el-button>
                <el-button type="primary" size="small" @click="applyFilter">确定</el-button>
              </div>
            </div>
          </el-popover>
        </div>
        <el-button type="success" plain @click="handleAdd" class="publish-btn">发布帖子</el-button>
      </div>
    </div>
    
    <!-- 内容区域 -->
    <div class="content-wrapper">
      <div 
        class="article-card" 
        v-for="item in data.articleData" 
        :key="item.id" 
        @click="goToDetail(item.id)"
      >
        <div class="article-header">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" class="user-avatar">
          <div class="user-info">
            <div class="user-name">{{ item.userName }}</div>
            <div class="post-time">{{ item.time }}</div>
          </div>
        </div>
        <div class="article-title">{{ item.title }}</div>
        
        <div v-if="isVideoPost(item.content)" class="video-preview" v-html="renderVideoInList(item.content)"></div>
        
        <div v-else class="article-content-preview" v-html="stripHtml(item.content)"></div>
        
        <div class="article-actions">
          <div class="action-item" @click.stop="toggleLike(item)">
            <img src="@/assets/images/点赞.png" alt="点赞" class="action-icon" :style="{ filter: item.liked ? 'none' : 'grayscale(100%)', opacity: item.liked ? 1 : 0.5 }">
            <span :style="{ color: item.liked ? '#409EFF' : '' }">{{ item.likeCount || 0 }}</span>
          </div>
          <div class="action-item" @click.stop="toggleCollect(item)">
            <img src="@/assets/images/收藏.png" alt="收藏" class="action-icon" :style="{ filter: item.collected ? 'none' : 'grayscale(100%)', opacity: item.collected ? 1 : 0.5 }">
            <span :style="{ color: item.collected ? '#F56C6C' : '' }">{{ item.collectCount || 0 }}</span>
          </div>
          <div class="action-item" @click.stop="goToDetail(item.id)">
            <img src="@/assets/images/评论.png" alt="评论" class="action-icon">
            <span>{{ item.commentCount || 0 }}</span>
          </div>
          <div class="action-item" @click.stop="showArticleReportDialog(item)">
            <img src="@/assets/images/举报.png" alt="举报" class="action-icon">
            <span>举报</span>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination 
          @current-change="load" 
          layout="total, prev, pager, next, jumper" 
          :page-size="data.pageSize" 
          v-model:current-page="data.pageNum" 
          :total="data.total" 
        />
      </div>
    </div>

    <el-dialog title="发布内容" v-model="data.formVisible" width="60%" destroy-on-close draggable>
      <el-tabs v-model="data.activeTab" style="padding: 20px">
        <el-tab-pane label="发贴" name="post">
          <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="100px">
            <el-form-item prop="title" label="帖子标题">
              <el-input 
                v-model="data.form.title" 
                placeholder="请输入帖子标题（5-30个字）" 
                maxlength="30" 
                show-word-limit
                size="large"
              ></el-input>
            </el-form-item>
            
            <el-form-item prop="content" label="帖子内容">
              <el-input 
                v-model="data.form.content" 
                type="textarea"
                :rows="10"
                placeholder="分享你的想法和经验，让更多人受益..."
                maxlength="2000" 
                show-word-limit
                resize="none"
              ></el-input>
            </el-form-item>

            <el-form-item>
              <div style="color: #999; font-size: 12px; margin-top: 10px">
                <el-icon style="vertical-align: middle; margin-right: 5px"><InfoFilled /></el-icon>
                发帖规则：请勿发布违法违规、侵犯他人权益的内容
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" @click="savePost">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="视频" name="video">
          <el-form ref="videoFormRef" :rules="data.videoRules" :model="data.videoForm" label-width="100px">
            <el-form-item prop="title" label="视频标题">
              <el-input 
                v-model="data.videoForm.title" 
                placeholder="请输入视频标题（5-80个字）" 
                maxlength="80" 
                show-word-limit
                size="large"
              ></el-input>
            </el-form-item>

            <el-form-item label="上传视频">
              <div style="border: 2px dashed #dcdfe6; border-radius: 8px; padding: 40px; text-align: center; background: #f8f9fa; cursor: pointer" @click="triggerVideoUpload">
                <el-icon :size="60" color="#409EFF" style="margin-bottom: 15px"><VideoCamera /></el-icon>
                <div style="font-size: 16px; color: #333; margin-bottom: 10px">拖拽视频到此或点击上传</div>
                <el-button type="primary" size="large">上传视频</el-button>
                <input 
                  ref="videoInputRef"
                  type="file" 
                  accept="video/*" 
                  style="display: none"
                  @change="handleVideoFileChange"
                />
              </div>

              <div v-if="data.videoForm.videoUrl" style="margin-top: 15px; padding: 15px; background: #f0f9ff; border-radius: 8px; border: 1px solid #409EFF">
                <div style="display: flex; align-items: center; gap: 10px">
                  <el-icon :size="24" color="#409EFF"><VideoPlay /></el-icon>
                  <div style="flex: 1">
                    <div style="font-size: 14px; color: #333">{{ data.videoForm.title || '视频已上传' }}</div>
                    <div style="font-size: 12px; color: #999; margin-top: 3px">{{ data.videoForm.duration || '解析中...' }}</div>
                  </div>
                  <el-button type="danger" size="small" @click="removeVideo">移除</el-button>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="description" label="视频描述">
              <el-input 
                v-model="data.videoForm.description" 
                type="textarea"
                :rows="4"
                placeholder="添加视频描述，让更多人了解你的视频内容..."
                maxlength="500" 
                show-word-limit
              ></el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" @click="saveVideo">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

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
import {reactive, ref, onBeforeUnmount, inject} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import { Plus, Filter, InfoFilled, VideoCamera, VideoPlay } from "@element-plus/icons-vue";

// 【新增】注入全局消息状态
const messageState = inject('messageState')

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref(null)
const videoFormRef = ref(null)
const videoInputRef = ref(null)

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  timeFilter: null,
  contentType: null,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  articleData: [],
  formVisible: false,
  activeTab: 'post',
  form: {
    title: '',
    content: ''
  },
  videoForm: {
    title: '',
    description: '',
    videoUrl: '',
    cover: '',
    duration: ''
  },
  rules: {
    title: [
      { required: true, message: '请输入帖子标题', trigger: 'blur' },
      { min: 5, max: 31, message: '标题长度在 5 到 31 个字符', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '请输入帖子内容', trigger: 'blur' }
    ]
  },
  videoRules: {
    title: [
      { required: true, message: '请输入视频标题', trigger: 'blur' }
    ]
  },
  articleReportVisible: false,
  articleReportSubmitting: false,
  reportTypes: ['色情低俗', '垃圾广告', '辱骂攻击', '违法犯罪', '时政不实信息', '青少年不宜', '侵犯权益', '开盒网暴'],
  articleReportForm: {
    reportType: '',
    detailReason: '',
    files: ''
  },
  articleReportFileList: [],
  articleReportFileUrls: [],
  currentArticleId: null,
  filterVisible: false,
  hasFilter: false
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
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

// 重置筛选条件
const resetFilter = () => {
  data.timeFilter = null
  data.contentType = null
  applyFilter()
}

// 应用筛选条件
const applyFilter = () => {
  data.filterVisible = false
  data.pageNum = 1
  updateFilterStatus()
  load()
}

// 更新筛选状态
const updateFilterStatus = () => {
  data.hasFilter = !!(data.timeFilter || data.contentType)
}

const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      userId: data.user.id
    }
  }).then(res => {
    console.log('Article response:', res)
    if (res.code === '200') {
      console.log('Records:', res.data?.records)
      console.log('Total:', res.data?.total)
      data.articleData = res.data?.records || []
      data.total = res.data?.total || 0
      console.log('data.total set to:', data.total)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const toggleLike = (article) => {
  if (!data.user.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  request.post('/likes/add', {
    userId: data.user.id,
    articleId: article.id,
    userName: data.user.name,
    articleTitle: article.title
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const toggleCollect = (article) => {
  if (!data.user.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  request.post('/collect/add', {
    userId: data.user.id,
    articleId: article.id,
    userName: data.user.name,
    articleTitle: article.title
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const goToDetail = (id) => {
  router.push('/front/articleDetail?id=' + id)
}

const handleAdd = () => {
  data.formVisible = true
  data.activeTab = 'post'
  data.form = {
    title: '',
    content: ''
  }
  data.videoForm = {
    title: '',
    description: '',
    videoUrl: '',
    cover: '',
    duration: ''
  }
}

const savePost = () => {
  formRef.value.validate(valid => {
    if (valid) {
      request.post('/article/add', {
        ...data.form,
        userId: data.user.id,
        userName: data.user.name,
        userAvatar: data.user.avatar
      }).then(res => {
        if (res.code === '200') {
          ElMessage.success('发布成功')
          data.formVisible = false
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const saveVideo = () => {
  videoFormRef.value.validate(valid => {
    if (valid) {
      if (!data.videoForm.videoUrl) {
        ElMessage.warning('请上传视频')
        return
      }
      
      const videoContent = `<video src="${data.videoForm.videoUrl}" controls></video>`
      
      request.post('/article/add', {
        title: data.videoForm.title,
        content: videoContent,
        description: data.videoForm.description,
        userId: data.user.id,
        userName: data.user.name,
        userAvatar: data.user.avatar
      }).then(res => {
        if (res.code === '200') {
          ElMessage.success('发布成功')
          data.formVisible = false
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const isVideoPost = (content) => {
  if (!content) return false
  return content.includes('<video') || content.includes('<iframe')
}

const renderVideoInList = (content) => {
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = content
  const video = tempDiv.querySelector('video')
  if (video) {
    const src = video.getAttribute('src')
    return `<video src="${encodeURI(baseUrl + src)}" controls style="width: 100%; max-height: 400px; border-radius: 8px;"></video>`
  }
  return content
}

const stripHtml = (html) => {
  if (!html) return ''
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html
  return tempDiv.textContent || tempDiv.innerText || ''
}

const showArticleReportDialog = (article) => {
  data.currentArticleId = article.id
  data.articleReportVisible = true
  data.articleReportForm = {
    reportType: '',
    detailReason: '',
    files: ''
  }
  data.articleReportFileList = []
  data.articleReportFileUrls = []
}

const handleArticleReportFileUpload = (res) => {
  if (res.code === '200') {
    const url = baseUrl + res.data
    data.articleReportFileUrls.push(url)
    data.articleReportForm.files = data.articleReportFileUrls.join(',')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const handleArticleReportFileRemove = (file, fileList) => {
  const index = data.articleReportFileList.indexOf(file)
  if (index > -1) {
    data.articleReportFileUrls.splice(index, 1)
    data.articleReportForm.files = data.articleReportFileUrls.join(',')
  }
}

const submitArticleReport = () => {
  if (!data.articleReportForm.reportType) {
    ElMessage.warning('请选择举报原因')
    return
  }
  if (!data.articleReportForm.detailReason) {
    ElMessage.warning('请填写详细原因')
    return
  }
  
  data.articleReportSubmitting = true
  request.post('/articleReport/add', {
    articleId: data.currentArticleId,
    userId: data.user.id,
    userName: data.user.name,
    reportType: data.articleReportForm.reportType,
    detailReason: data.articleReportForm.detailReason,
    files: data.articleReportForm.files
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('举报成功')
      data.articleReportVisible = false
      // 【新增】举报成功后刷新未读消息数
      refreshUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  }).finally(() => {
    data.articleReportSubmitting = false
  })
}

const triggerVideoUpload = () => {
  videoInputRef.value?.click()
}

const handleVideoFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过 2G')
    return
  }

  const formData = new FormData()
  formData.append('file', file)
  
  request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    if (res.code === '200') {
      data.videoForm.videoUrl = res.data
      
      if (!data.videoForm.title) {
        const fileName = file.name.replace(/\.[^/.]+$/, '')
        data.videoForm.title = fileName
      }
      
      const video = document.createElement('video')
      video.preload = 'metadata'
      video.src = baseUrl + res.data
      video.onloadedmetadata = () => {
        const duration = video.duration
        const minutes = Math.floor(duration / 60)
        const seconds = Math.floor(duration % 60)
        data.videoForm.duration = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
      }
      
      ElMessage.success('视频上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  }).catch(() => {
    ElMessage.error('上传失败')
  })
  
  event.target.value = ''
}

const removeVideo = () => {
  data.videoForm.videoUrl = ''
  data.videoForm.cover = ''
  data.videoForm.duration = ''
  ElMessage.success('已移除视频')
}

load()
</script>

<style scoped>
/* 全局样式 */
.article-page {
  background: 
    url('https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=1920&q=80') center/cover,
    linear-gradient(135deg, rgba(255, 195, 18, 0.15) 0%, rgba(255, 159, 67, 0.15) 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 1px;
}

.article-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 25% 35%, rgba(255, 195, 18, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 75% 65%, rgba(255, 159, 67, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(255, 107, 107, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

/* 搜索头部 */
.search-header {
  background: transparent;
  padding: 30px 20px;
}

.search-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
}

.search-input-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  width: 350px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-btn {
  border-radius: 20px;
  padding: 10px 20px;
}

.publish-btn {
  border-radius: 20px;
  padding: 10px 25px;
  font-weight: bold;
}

.filter-content {
  padding: 15px;
}

.filter-item {
  margin-bottom: 15px;
}

.filter-label {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.filter-select {
  width: 100%;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 15px;
}

/* 内容区域 */
.content-wrapper {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

/* 文章卡片 */
.article-card {
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 250, 240, 0.98) 100%);
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  animation: fadeIn 0.6s ease-out;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.article-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px);
}

.article-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
}

.user-avatar {
  height: 40px;
  width: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid #e8ecf1;
}

.user-info {
  margin-left: 12px;
}

.user-name {
  color: #333;
  font-weight: 500;
}

.post-time {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}

.article-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  transition: color 0.3s ease;
}

.article-card:hover .article-title {
  color: #667eea;
}

.article-content-preview {
  color: #666;
  font-size: 15px;
  line-height: 1.8;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}

.video-preview {
  margin: 15px 0;
  width: 100%;
}

.video-preview video {
  width: 100%;
  max-height: 400px;
  border-radius: 12px;
}

.article-actions {
  margin-top: 20px;
  display: flex;
  gap: 40px;
  color: #999;
  font-size: 14px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  color: #667eea;
}

.action-icon {
  width: 18px;
  height: 18px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 文本截断 */
.line1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
  
  .article-actions {
    gap: 20px;
    flex-wrap: wrap;
  }
}
</style>
