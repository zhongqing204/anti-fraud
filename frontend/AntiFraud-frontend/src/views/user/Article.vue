<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto; display: flex; align-items: center; justify-content: space-between">
        <div style="display: flex; align-items: center; gap: 15px">
          <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入帖子名称查询" style="width: 350px; height: 40px"></el-input>
          <el-popover trigger="manual" :width="300" v-model:visible="data.filterVisible">
            <template #reference>
              <el-button :type="data.hasFilter ? 'primary' : ''" @click="data.filterVisible = !data.filterVisible">
                <el-icon><Filter /></el-icon>
                筛选
              </el-button>
            </template>
            <div style="padding: 10px" @click.stop>
              <div style="margin-bottom: 15px">
                <div style="font-weight: bold; margin-bottom: 8px">时间</div>
                <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable style="width: 100%" :teleported="false">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="最近一周" value="week" />
                  <el-option label="最近一月" value="month" />
                  <el-option label="最近三月" value="threeMonths" />
                </el-select>
              </div>
              <div style="margin-bottom: 15px">
                <div style="font-weight: bold; margin-bottom: 8px">内容类型</div>
                <el-select v-model="data.contentType" placeholder="请选择内容类型" clearable style="width: 100%" :teleported="false">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="图文" value="text" />
                  <el-option label="视频" value="video" />
                </el-select>
              </div>
              <div style="display: flex; gap: 10px; justify-content: flex-end">
                <el-button size="small" @click="resetFilter">重置</el-button>
                <el-button type="primary" size="small" @click="applyFilter">确定</el-button>
              </div>
            </div>
          </el-popover>
        </div>
        <el-button type="success" plain @click="handleAdd">发布帖子</el-button>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <div class="card article-card" style="margin-bottom: 10px; padding: 20px" v-for="item in data.articleData" :key="item.id" @click="goToDetail(item.id)">
        <div style="display: flex; align-items: flex-start; margin-bottom: 15px">
        <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 30px; width: 30px; border-radius: 50%; object-fit: cover; flex-shrink: 0;">
        <div style="margin-left: 10px;">
          <div style="color: #666666">{{ item.userName }}</div>
          <div style="color: #999; font-size: 12px; margin-top: 2px;">{{ item.time }}</div>
        </div>
      </div>
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px; cursor: pointer">{{ item.title }}</div>
        
        <div v-if="isVideoPost(item.content)" class="video-preview" v-html="renderVideoInList(item.content)"></div>
        
        <div v-else class="article-content-preview" v-html="stripHtml(item.content)"></div>
        
        <div style="margin-top: 15px; display: flex; gap: 40px; color: #999; font-size: 14px">
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="toggleLike(item)">
            <img src="@/assets/images/点赞.png" alt="点赞" style="width: 16px; height: 16px" :style="{ filter: item.liked ? 'none' : 'grayscale(100%)', opacity: item.liked ? 1 : 0.5 }">
            <span :style="{ color: item.liked ? '#409EFF' : '' }">{{ item.likeCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="toggleCollect(item)">
            <img src="@/assets/images/收藏.png" alt="收藏" style="width: 16px; height: 16px" :style="{ filter: item.collected ? 'none' : 'grayscale(100%)', opacity: item.collected ? 1 : 0.5 }">
            <span :style="{ color: item.collected ? '#F56C6C' : '' }">{{ item.collectCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="goToDetail(item.id)">
            <img src="@/assets/images/评论.png" alt="评论" style="width: 16px; height: 16px">
            <span>{{ item.commentCount || 0 }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 5px; cursor: pointer" @click.stop="showArticleReportDialog(item)">
            <img src="@/assets/images/举报.png" alt="举报" style="width: 16px; height: 16px">
            <span>举报</span>
          </div>
        </div>
      </div>
      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>

    <el-dialog title="发布内容" v-model="data.formVisible" width="60%" destroy-on-close>
      <el-tabs v-model="data.activeTab" style="padding: 20px">
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
                  style="height: 360px; overflow-y: hidden;"
                  v-model="data.form.content"
                  :defaultConfig="editorConfig"
                  mode="default"
                  @onCreated="handleCreated"
                />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePost">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="视频" name="video">
          <el-form ref="videoFormRef" :rules="data.videoRules" :model="data.videoForm" label-width="80px">
            <el-form-item prop="title" label="标题">
              <el-input v-model="data.videoForm.title" placeholder="请输入视频标题"></el-input>
            </el-form-item>
            <el-form-item label="视频">
              <el-upload
                :action="baseUrl + '/file/upload'"
                :on-success="handleVideoUpload"
                :limit="1"
                accept=".mp4,.avi,.mov,.wmv,.flv"
              >
                <el-button type="primary">上传视频</el-button>
              </el-upload>
              <div v-if="data.videoForm.videoUrl" style="margin-top: 10px">
                <video :src="encodeURI(baseUrl + data.videoForm.videoUrl)" controls style="width: 100%; max-height: 300px"></video>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveVideo">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog title="举报内容" v-model="data.articleReportVisible" width="500px" destroy-on-close>
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
import {reactive, ref, markRaw, onBeforeUnmount} from "vue";
import {Editor, Toolbar} from "@wangeditor/editor-for-vue";
import "@wangeditor/editor/dist/css/style.css";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import { Plus, Filter } from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = ref(null)
const formRef = ref(null)
const videoFormRef = ref(null)

const toolbarConfig = {}
const editorConfig = { placeholder: '请输入内容...' }

const handleCreated = (editor) => {
  editorRef.value = markRaw(editor)
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

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
    videoUrl: ''
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
      title: data.title
    }
  }).then(res => {
    if (res.code === '200') {
      let articleData = res.data?.records || []
      data.total = res.data?.total || 0
      
      // 时间筛选
      if (data.timeFilter) {
        const now = new Date()
        articleData = articleData.filter(item => {
          const createTime = new Date(item.time)
          const diffDays = (now - createTime) / (1000 * 60 * 60 * 24)
          
          switch(data.timeFilter) {
            case 'week':
              return diffDays <= 7
            case 'month':
              return diffDays <= 30
            case 'threeMonths':
              return diffDays <= 90
            default:
              return true
          }
        })
      }
      
      // 内容类型筛选
      if (data.contentType) {
        articleData = articleData.filter(item => {
          if (data.contentType === 'video') {
            return isVideoPost(item.content)
          } else if (data.contentType === 'text') {
            return !isVideoPost(item.content)
          }
          return true
        })
      }
      
      data.articleData = articleData
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
    videoUrl: ''
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

const handleVideoUpload = (res) => {
  if (res.code === '200') {
    data.videoForm.videoUrl = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
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
    } else {
      ElMessage.error(res.msg)
    }
  }).finally(() => {
    data.articleReportSubmitting = false
  })
}

load()
</script>

<style scoped>
.line1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-card {
  transition: all 0.3s;
}

.article-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.article-content-preview {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}

.video-preview {
  margin: 10px 0;
  width: 100%;
}

.video-preview video {
  width: 100%;
  max-height: 400px;
  border-radius: 8px;
}
</style>
