<template>
  <div style="background: #f5f7fa; min-height: 100vh; padding-bottom: 50px">
    <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px 0; margin-bottom: 30px">
      <div style="width: 60%; margin: 0 auto">
        <div style="color: white; font-size: 32px; font-weight: bold; margin-bottom: 10px">
          <el-icon style="vertical-align: middle; margin-right: 10px"><Document /></el-icon>
          我的帖子
        </div>
        <div style="color: rgba(255,255,255,0.9); font-size: 16px">管理您发布的所有帖子</div>
      </div>
    </div>

    <div style="width: 60%; margin: 0 auto">
      <div style="margin-bottom: 20px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); display: flex; justify-content: space-between; align-items: center">
        <div style="font-size: 16px; color: #666">
          共 <span style="color: #409EFF; font-weight: bold">{{ data.total }}</span> 篇帖子
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon style="margin-right: 5px"><Plus /></el-icon>
          发布新帖
        </el-button>
      </div>

      <div v-for="item in data.tableData" :key="item.id" 
           class="article-card"
           @click="goToDetail(item.id)">
        <div style="display: flex; align-items: center; margin-bottom: 15px">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 35px; width: 35px; border-radius: 50%; object-fit: cover">
          <div style="margin-left: 10px">
            <div style="color: #333; font-weight: 500">{{ item.userName }}</div>
            <div style="color: #999; font-size: 12px; margin-top: 2px">{{ formatTime(item.time) }}</div>
          </div>
        </div>
        
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 12px; color: #333; line-height: 1.4">
          {{ item.title }}
        </div>
        
        <div v-if="isVideoPost(item.content)" class="video-preview" v-html="renderVideoInList(item.content)"></div>
        
        <div v-else class="article-content-preview" v-html="stripHtml(item.content)"></div>
        
        <div style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #f0f0f0; display: flex; gap: 30px; color: #999; font-size: 14px">
          <div style="display: flex; align-items: center; gap: 5px">
            <img src="@/assets/images/点赞.png" alt="点赞" style="width: 16px; height: 16px">
            <span>{{ item.likeCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px">
            <img src="@/assets/images/收藏.png" alt="收藏" style="width: 16px; height: 16px">
            <span>{{ item.collectCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px">
            <img src="@/assets/images/评论.png" alt="评论" style="width: 16px; height: 16px">
            <span>{{ item.commentCount || 0 }}</span>
          </div>
          <div style="margin-left: auto">
            <el-button type="danger" size="small" plain @click.stop="handleDelete(item)">
              <el-icon style="margin-right: 3px"><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="data.total" style="margin-top: 30px; background: white; padding: 20px; border-radius: 10px; text-align: center">
        <el-pagination @current-change="load" layout="total, prev, pager, next, jumper" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
      
      <div v-else style="text-align: center; padding: 80px; color: #999; background: white; border-radius: 10px">
        <el-icon :size="48" style="margin-bottom: 20px"><Document /></el-icon>
        <div style="font-size: 18px">暂无发布的帖子</div>
        <div style="font-size: 14px; margin-top: 10px; color: #ccc">点击右上角"发布新帖"开始分享吧</div>
      </div>
    </div>

    <!-- 发布帖子/视频对话框 -->
    <el-dialog 
      v-model="data.formVisible" 
      width="700px" 
      destroy-on-close
      :show-close="true"
      top="5vh"
    >
      <template #header>
        <div style="display: flex; gap: 30px; border-bottom: 2px solid #f0f0f0; padding-bottom: 0">
          <div 
            style="padding: 10px 0; cursor: pointer; font-size: 18px; font-weight: 500; position: relative"
            :style="{ color: data.activeTab === 'article' ? '#409EFF' : '#999' }"
            @click="data.activeTab = 'article'"
          >
            发贴
            <div v-if="data.activeTab === 'article'" style="position: absolute; bottom: -2px; left: 0; right: 0; height: 2px; background: #409EFF"></div>
          </div>
          <div 
            style="padding: 10px 0; cursor: pointer; font-size: 18px; font-weight: 500; position: relative"
            :style="{ color: data.activeTab === 'video' ? '#409EFF' : '#999' }"
            @click="data.activeTab = 'video'"
          >
            发视频
            <div v-if="data.activeTab === 'video'" style="position: absolute; bottom: -2px; left: 0; right: 0; height: 2px; background: #409EFF"></div>
          </div>
        </div>
      </template>

      <!-- 发贴表单 -->
      <div v-if="data.activeTab === 'article'" style="padding: 20px 0">
        <el-form ref="articleFormRef" :rules="data.articleRules" :model="data.articleForm" label-width="100px">
          <el-form-item prop="title" label="帖子标题">
            <el-input 
              v-model="data.articleForm.title" 
              placeholder="请输入帖子标题（5-30个字）" 
              maxlength="30" 
              show-word-limit
              size="large"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="content" label="帖子内容">
            <el-input 
              v-model="data.articleForm.content" 
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
        </el-form>
      </div>

      <!-- 发视频表单 -->
      <div v-if="data.activeTab === 'video'" style="padding: 20px 0">
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
        </el-form>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 15px">
          <el-button size="large" @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" size="large" @click="save" :loading="data.submitting">
            发布
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onMounted} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import { Document, Plus, Delete, InfoFilled, VideoCamera, VideoPlay } from "@element-plus/icons-vue";

const articleFormRef = ref()
const videoFormRef = ref()
const videoInputRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  formVisible: false,
  activeTab: 'article', // article 或 video
  submitting: false,
  tableData: [],
  total: 0,
  // 帖子表单
  articleForm: {
    title: '',
    content: ''
  },
  articleRules: {
    title: [
      { required: true, message: '请输入帖子标题', trigger: 'blur' },
      { min: 5, max: 30, message: '标题长度在 5 到 30 个字符', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '请输入帖子内容', trigger: 'blur' },
      { min: 10, message: '内容至少 10 个字符', trigger: 'blur' }
    ]
  },
  // 视频表单
  videoForm: {
    title: '',
    description: '',
    videoUrl: '',
    cover: '',
    duration: ''
  },
  videoRules: {
    title: [
      { required: true, message: '请输入视频标题', trigger: 'blur' },
      { min: 5, max: 80, message: '标题长度在 5 到 80 个字符', trigger: 'blur' }
    ],
    videoUrl: [
      { required: true, message: '请上传视频文件', trigger: 'change' }
    ]
  }
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

const stripHtml = (html) => {
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

const isVideoPost = (content) => {
  return content && content.includes('<video')
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

const goToDetail = (id) => {
  router.push('/front/articleDetail?id=' + id)
}

const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  load()
})

const handleAdd = () => {
  data.activeTab = 'article'
  data.articleForm = { title: '', content: '' }
  data.videoForm = { title: '', description: '', videoUrl: '', cover: '', duration: '' }
  data.formVisible = true
}

const triggerVideoUpload = () => {
  videoInputRef.value?.click()
}

const handleVideoFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件大小（2G）
  if (file.size > 2 * 1024 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过 2G')
    return
  }

  // 上传视频
  const formData = new FormData()
  formData.append('file', file)
  
  data.submitting = true
  request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    data.submitting = false
    if (res.code === '200') {
      data.videoForm.videoUrl = res.data
      
      // 自动提取文件名作为标题
      if (!data.videoForm.title) {
        const fileName = file.name.replace(/\.[^/.]+$/, '')
        data.videoForm.title = fileName
      }
      
      // 获取视频时长
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
    data.submitting = false
    ElMessage.error('上传失败')
  })
  
  // 清空 input
  event.target.value = ''
}

const removeVideo = () => {
  data.videoForm.videoUrl = ''
  data.videoForm.cover = ''
  data.videoForm.duration = ''
  ElMessage.success('已移除视频')
}

const saveArticle = () => {
  articleFormRef.value.validate(valid => {
    if (valid) {
      data.submitting = true
      const formData = {
        userId: data.user.id,
        userName: data.user.name,
        userAvatar: data.user.avatar,
        title: data.articleForm.title,
        content: data.articleForm.content,
        time: new Date().toISOString()
      }
      
      request.post('/article/add', formData).then(res => {
        data.submitting = false
        if (res.code === '200') {
          ElMessage.success('发布成功')
          data.formVisible = false
          load()
        } else {
          ElMessage.error(res.msg)
        }
      }).catch(() => {
        data.submitting = false
        ElMessage.error('发布失败')
      })
    }
  })
}

const saveVideo = () => {
  videoFormRef.value.validate(valid => {
    if (valid) {
      if (!data.videoForm.videoUrl) {
        ElMessage.error('请上传视频')
        return
      }
      
      data.submitting = true
      const formData = {
        userId: data.user.id,
        userName: data.user.name,
        userAvatar: data.user.avatar,
        title: data.videoForm.title,
        content: `<video src="${data.videoForm.videoUrl}" controls></video>`,
        description: data.videoForm.description,
        time: new Date().toISOString()
      }
      
      request.post('/article/add', formData).then(res => {
        data.submitting = false
        if (res.code === '200') {
          ElMessage.success('发布成功')
          data.formVisible = false
          load()
        } else {
          ElMessage.error(res.msg)
        }
      }).catch(() => {
        data.submitting = false
        ElMessage.error('发布失败')
      })
    }
  })
}

const save = () => {
  if (data.activeTab === 'article') {
    saveArticle()
  } else {
    saveVideo()
  }
}

const handleDelete = (item) => {
  ElMessageBox.confirm('确定要删除该帖子吗？删除后无法恢复', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete('/article/delete/' + item.id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}
</script>

<style scoped>
.article-content-preview {
  color: #666;
  line-height: 1.8;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  -webkit-box-orient: vertical;
  word-break: break-all;
  font-size: 14px;
}

.video-preview {
  margin-bottom: 15px;
}

.video-preview :deep(video) {
  width: 100%;
  max-height: 400px;
  border-radius: 8px;
  background: #000;
}

.article-card {
  background: white;
  border-radius: 10px;
  padding: 25px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.article-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  transform: translateY(-2px);
}
</style>
