<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto; display: flex; align-items: center; justify-content: space-between">
        <div style="font-size: 20px; font-weight: bold">
          我的帖子
          <span v-if="data.total > 0" style="color: #409EFF; margin-left: 8px">（{{ data.total }}）</span>
        </div>
        <el-button type="success" plain @click="handleAdd">发布帖子</el-button>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <div class="card article-card" style="margin-bottom: 10px; padding: 20px" v-for="item in data.tableData" :key="item.id">
        <div style="display: flex; align-items: center; margin-bottom: 15px">
          <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 30px; width: 30px; border-radius: 50%; object-fit: cover">
          <div style="margin-left: 10px; color: #666666">{{ item.userName }}</div>
          <div style="margin-left: auto; color: #999; font-size: 12px">{{ formatTime(item.time) }}</div>
        </div>
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px; cursor: pointer" @click="goToDetail(item.id)">
          {{ item.title }}
        </div>
        
        <div v-if="isVideoPost(item.content)" class="video-preview" v-html="renderVideoInList(item.content)" @click="goToDetail(item.id)"></div>
        
        <div v-else class="article-content-preview" v-html="stripHtml(item.content)" @click="goToDetail(item.id)"></div>
        
        <div style="margin-top: 15px; display: flex; gap: 40px; color: #999; font-size: 14px">
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
          <div style="margin-left: auto; display: flex; gap: 10px">
            <el-button type="danger" size="small" @click.stop="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
      <div v-else style="text-align: center; padding: 50px; color: #999">
        暂无发布的帖子
      </div>
    </div>

    <el-dialog title="发布帖子" v-model="data.formVisible" width="60%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">
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
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, markRaw} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = ref(null)

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
  pageNum: 1,
  pageSize: 10,
  form: {},
  formVisible: false,
  tableData: [],
  total: 0,
  rules: {
    title: [
      { required: true, message: '请输入帖子标题', trigger: 'blur' },
    ],
    content: [
      { required: true, message: '请输入帖子内容', trigger: 'blur' },
    ],
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
load()

const handleAdd = () => {
  data.form = {}
  data.form.userId = data.user.id
  data.form.content = ''
  data.formVisible = true
}

const save = () => {
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

// 删除帖子
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
  color: #666666;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  -webkit-box-orient: vertical;
  word-break: break-all;
  cursor: pointer;
}

.video-preview {
  margin-bottom: 15px;
  cursor: pointer;
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
