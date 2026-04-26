<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto; display: flex; align-items: center; justify-content: space-between">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入帖子名称查询" style="width: 400px; height: 40px"></el-input>
        <el-button type="success" plain @click="handleAdd">发布帖子</el-button>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <div class="card article-card" style="margin-bottom: 10px; padding: 20px" v-for="item in data.articleData" :key="item.id">
        <div style="display: flex; align-items: center; margin-bottom: 15px">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 30px; width: 30px; border-radius: 50%; object-fit: cover">
          <div style="margin-left: 10px; color: #666666">{{ item.userName }}</div>
          <div style="margin-left: auto; color: #999; font-size: 12px">{{ item.time }}</div>
        </div>
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.id)">{{ item.title }}</div>
        
        <!-- 视频帖子直接显示视频播放器 -->
        <div v-if="isVideoPost(item.content)" class="video-preview" v-html="renderVideoInList(item.content)"></div>
        <!-- 普通帖子显示文本预览 -->
        <div v-else class="article-content-preview">{{ stripHtml(item.content) }}</div>
        
        <div style="margin-top: 15px; display: flex; gap: 40px; color: #999; font-size: 14px">
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="toggleLike(item)">
            <img src="@/assets/images/点赞.png" alt="点赞" style="width: 16px; height: 16px" :style="{ filter: item.liked ? 'none' : 'grayscale(100%)', opacity: item.liked ? 1 : 0.5 }">
            <span :style="{ color: item.liked ? '#F56C6C' : '' }">{{ item.likeCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="toggleCollect(item)">
            <img src="@/assets/images/收藏.png" alt="收藏" style="width: 16px; height: 16px" :style="{ filter: item.collected ? 'none' : 'grayscale(100%)', opacity: item.collected ? 1 : 0.5 }">
            <span :style="{ color: item.collected ? '#E6A23C' : '' }">{{ item.collectCount || 0 }}</span>
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

    <el-dialog title="发布内容" v-model="data.formVisible" width="60%" destroy-on-close>
      <el-tabs v-model="data.activeTab" style="padding: 20px">
        <!-- 发贴 -->
        <el-tab-pane label="发贴" name="post">
          <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px">
            <el-form-item prop="title" label="标题">
              <el-input v-model="data.form.title" placeholder="请输入完整帖子标题(5-31个字)" maxlength="31" show-word-limit></el-input>
            </el-form-item>
            <el-form-item prop="content" label="内容">
              <div style="border: 1px solid #ccc; width: 100%; height: 400px;">
                <Toolbar
                  style="border-bottom: 1px solid #ccc"
                  :editor="editorRef"
                  :defaultConfig="toolbarConfig"
                  mode="default"
                />
                <Editor
                  style="height: 350px; overflow-y: hidden;"
                  v-model="data.form.content"
                  :defaultConfig="editorConfig"
                  mode="default"
                  @onCreated="handleCreated"
                />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePost" style="width: 100%">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

                <!-- 发视频 -->
        <el-tab-pane label="发视频" name="video">
          <el-form ref="videoFormRef" :rules="data.videoRules" :model="data.videoForm" label-width="100px">
            <el-form-item prop="title" label="视频标题">
              <el-input v-model="data.videoForm.title" placeholder="请输入视频标题（5-80字）" maxlength="80" show-word-limit></el-input>
            </el-form-item>
            <el-form-item label="上传视频">
              <div style="border: 2px dashed #dcdfe6; border-radius: 8px; padding: 40px; text-align: center; background-color: #f5f7fa">
                <el-icon :size="50" color="#909399"><VideoCamera /></el-icon>
                <div style="margin-top: 15px; font-size: 16px; color: #606266">拖拽视频到此或点击上传</div>
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :on-change="handleVideoChange"
                  :limit="1"
                  accept="video/mp4,video/avi,video/mov,video/mkv"                  style="margin-top: 15px; display: inline-block"
                >
                  <el-button type="primary">上传视频</el-button>
                </el-upload>
                <div v-if="data.videoForm.videoUrl" style="margin-top: 15px; color: #67C23A">
                  已选择: {{ data.videoForm.videoName }}
                </div>
              </div>
              <div style="margin-top: 15px; color: #909399; font-size: 12px; line-height: 1.8">
                <div><strong>视频大小</strong></div>
                <div>视频文件大小不超过2G</div>
                <div style="margin-top: 10px"><strong>分辨率&格式</strong></div>
                <div>建议上传清晰视频，支持mp4/avi/mov/mkv格式</div>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveVideo" style="width: 100%">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>

</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef, markRaw} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import {VideoCamera} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const videoFormRef = ref()
const uploadRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = shallowRef()

const toolbarConfig = {}

const editorConfig = { 
  placeholder: '请输入正文（建议200-2000字）',
  MENU_CONF: {
    uploadImage: {
      server: baseUrl + '/file/upload',
      fieldName: 'file',
      maxFileSize: 10 * 1024 * 1024,
      allowedFileTypes: ['image/*'],
      customInsert(res, insertFn) {
        if (res.code === '200') {
          const url = res.data.startsWith('http') ? res.data : baseUrl + res.data
          insertFn(url, '', url)
        } else {
          ElMessage.error(res.msg || '上传失败')
        }
      }
    }
  }
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const handleCreated = (editor) => {
  editorRef.value = markRaw(editor)
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  articleData: [],
  form: {},
  videoForm: {},
  formVisible: false,
  activeTab: 'post',
  rules: {
    title: [
      { required: true, message: '请输入帖子标题', trigger: 'blur' },
    ],
    content: [
      { required: true, message: '请输入帖子内容', trigger: 'blur' },
    ],
  },
  videoRules: {
    title: [
      { required: true, message: '请输入视频标题', trigger: 'blur' },
    ],
  }
})

// 获取头像URL的辅助函数
const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

// 点赞/取消点赞 - 操作后重新加载数据，确保状态与后端同步
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

// 收藏/取消收藏 - 操作后重新加载数据，确保状态与后端同步
const toggleCollect = (item) => {
  request.post('/collect/add', {
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

// 打开发布对话框
const handleAdd = () => {
  data.form = {}
  data.form.userId = data.user.id
  data.form.status = '审核通过'
  data.form.content = ''
  data.videoForm = {}
  data.videoForm.userId = data.user.id
  data.videoForm.status = '审核通过'
  data.activeTab = 'post'
  data.formVisible = true
}

// 发布帖子
const savePost = () => {
  formRef.value.validate(valid => {
    if (valid) {
      if (!data.form.content || data.form.content === '<p><br></p>') {
        ElMessage.error('请填写帖子内容')
        return
      }
      request.post('/article/add', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('发布成功')
          data.formVisible = false
          data.form.content = ''
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 视频文件选择
const handleVideoChange = (file) => {
  data.videoForm.videoFile = file.raw
  data.videoForm.videoName = file.name
  
  // 上传视频文件
  const formData = new FormData()
  formData.append('file', file.raw)
  
  request.post('/file/upload', formData).then(res => {
    if (res.code === '200') {
      data.videoForm.videoUrl = res.data
      ElMessage.success('视频上传成功')
    } else {
      ElMessage.error(res.msg || '视频上传失败')
    }
  })
}

// 发布视频
const saveVideo = () => {
  videoFormRef.value.validate(valid => {
    if (valid) {
      if (!data.videoForm.videoUrl) {
        ElMessage.error('请上传视频')
        return
      }
      
      // 根据文件扩展名获取MIME类型
      const getVideoType = (url) => {
        if (url.endsWith('.mp4')) return 'video/mp4'
        if (url.endsWith('.avi')) return 'video/x-msvideo'
        if (url.endsWith('.mov')) return 'video/quicktime'
        if (url.endsWith('.wmv')) return 'video/x-ms-wmv'
        if (url.endsWith('.flv')) return 'video/x-flv'
        if (url.endsWith('.mkv')) return 'video/x-matroska'
        return 'video/mp4'
      }
      
      // 只对文件名进行URL编码，保留路径结构
      const pathParts = data.videoForm.videoUrl.split('/')
      const fileName = pathParts[pathParts.length - 1]
      pathParts[pathParts.length - 1] = encodeURIComponent(fileName)
      const videoUrl = baseUrl + pathParts.join('/')
      const videoType = getVideoType(data.videoForm.videoUrl)
      
      // 将视频URL嵌入到内容中
      data.videoForm.content = `<video controls style="width: 100%; max-width: 800px" preload="metadata"><source src="${videoUrl}" type="${videoType}">您的浏览器不支持视频播放</video>`
      
      request.post('/article/add', data.videoForm).then(res => {
        if (res.code === '200') {
          ElMessage.success('发布成功')
          data.formVisible = false
          data.videoForm = {}
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 去除HTML标签
const stripHtml = (html) => {
  if (!html) return ''
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  
  // 移除所有video标签及其内容，避免显示降级文本
  const videos = tmp.querySelectorAll('video')
  videos.forEach(video => {
    if (video.parentNode) {
      video.parentNode.removeChild(video)
    }
  })
  
  return tmp.textContent || tmp.innerText || ''
}

// 判断是否为视频帖子
const isVideoPost = (content) => {
  if (!content) return false
  return content.includes('<video') && content.includes('<source')
}

// 在列表中渲染视频
const renderVideoInList = (content) => {
  if (!content) return ''
  return content.replace(
    /<source\s+src="([^"]+)"([^>]*)>/g,
    (match, src, attrs) => {
      let videoUrl = src
      if (!videoUrl.startsWith('http://') && !videoUrl.startsWith('https://')) {
        // 只对文件名进行URL编码，保留路径结构
        const pathParts = videoUrl.split('/')
        const fileName = pathParts[pathParts.length - 1]
        pathParts[pathParts.length - 1] = encodeURIComponent(fileName)
        videoUrl = baseUrl + pathParts.join('/')
      }
      return `<source src="${videoUrl}"${attrs}>`
    }
  ).replace(
    /<video([^>]*)>/,
    '<video$1 style="width: 100%; max-height: 400px; border-radius: 8px; margin-bottom: 15px">'
  )
}

// 加载帖子列表 - 增加查询用户点赞和收藏状态，确保显示正确的交互状态
const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      status: '审核通过'
    }
  }).then(res => {
    if (res.code === '200') {
      const articles = res.data?.records || []
      data.total = res.data?.total || 0
      
      // 批量查询当前用户的点赞和收藏状态
      const articleIds = articles.map(a => a.id)
      if (articleIds.length > 0) {
        // 查询用户的所有点赞记录
        request.get('/likes/selectAll', {
          params: { userId: data.user.id }
        }).then(likeRes => {
          if (likeRes.code === '200') {
            const userLikes = likeRes.data || []
            const likedArticleIds = new Set(userLikes.map(l => l.articleId))
            
            // 查询用户的所有收藏记录
            request.get('/collect/selectAll', {
              params: { userId: data.user.id }
            }).then(collectRes => {
              if (collectRes.code === '200') {
                const userCollects = collectRes.data || []
                const collectedArticleIds = new Set(userCollects.map(c => c.articleId))
                
                // 设置每篇文章的点赞和收藏状态
                articles.forEach(article => {
                  article.liked = likedArticleIds.has(article.id)
                  article.collected = collectedArticleIds.has(article.id)
                })
                
                data.articleData = articles
              }
            })
          }
        })
      } else {
        data.articleData = articles
      }
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
  line-clamp: 4;
  -webkit-box-orient: vertical;
  word-break: break-all;
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
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
</style>

