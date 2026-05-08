<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.title" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入帖子标题查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="success" plain @click="handleAdd">新增帖子</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="帖子名称" width="200" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户名称" />
        <el-table-column prop="time" label="发布时间" />
        <el-table-column label="操作" width="250" align="center">
          <template v-slot="scope">
            <el-button type="primary" plain @click="viewDetail(scope.row)">查看</el-button>
            <el-button type="warning" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="发布内容" v-model="data.formVisible" width="60%" destroy-on-close draggable>
      <el-tabs v-model="data.activeTab" style="padding: 20px">
        <!-- 发帖表单 -->
        <el-tab-pane label="发帖" name="post">
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
              <el-button type="primary" size="large" @click="save">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 视频表单 -->
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

    <el-dialog title="帖子详情" v-model="data.viewVisible" width="70%" destroy-on-close draggable>
      <div style="padding: 20px; line-height: 1.8;" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import { InfoFilled, VideoCamera, VideoPlay } from '@element-plus/icons-vue'

const formRef = ref()
const videoFormRef = ref()
const videoInputRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  formVisible: false, 
  form: {}, 
  videoForm: {
    title: '',
    description: '',
    videoUrl: '',
    cover: '',
    duration: ''
  },
  tableData: [], 
  pageNum: 1, 
  pageSize: 10, 
  total: 0, 
  title: null,
  ids: [], 
  activeTab: 'post',
  rules: { 
    title: [
      { required: true, message: '请输入帖子标题', trigger: 'blur' },
      { min: 5, max: 30, message: '标题长度在 5 到 30 个字符', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '请输入帖子内容', trigger: 'blur' },
      { min: 10, message: '内容至少 10 个字符', trigger: 'blur' }
    ],
  },
  videoRules: {
    title: [
      { required: true, message: '请输入视频标题', trigger: 'blur' },
      { min: 5, max: 80, message: '标题长度在 5 到 80 个字符', trigger: 'blur' }
    ],
    videoUrl: [
      { required: true, message: '请上传视频文件', trigger: 'change' }
    ]
  },
  viewVisible: false,
  viewContent: ''
})

const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
    }
  })
}

load()

const viewDetail = (row) => {
  data.viewContent = row.content || ''
  data.viewVisible = true
}

const handleAdd = () => {
  data.form = {} 
  data.form.content = ''
  data.videoForm = {
    title: '',
    description: '',
    videoUrl: '',
    cover: '',
    duration: ''
  }
  data.activeTab = 'post'
  data.formVisible = true 
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row)) 
  data.videoForm = {
    title: '',
    description: '',
    videoUrl: '',
    cover: '',
    duration: ''
  }
  data.activeTab = 'post'
  data.formVisible = true 
}

const add = () => {
  const admin = JSON.parse(localStorage.getItem('xm-admin') || '{}')
  data.form.userId = admin.id
  data.form.userName = admin.name
  data.form.userAvatar = admin.avatar
  
  request.post('/article/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      data.form.content = ''
      load() 
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/article/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      data.form.content = ''
      load() 
    }
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      if (!data.form.content || !data.form.content.trim()) {
        ElMessage.error('请填写帖子内容')
        return
      }
      data.form.id ? update() : add()
    }
  })
}

// 触发视频上传
const triggerVideoUpload = () => {
  videoInputRef.value?.click()
}

// 处理视频文件选择
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

// 移除已上传的视频
const removeVideo = () => {
  data.videoForm.videoUrl = ''
  data.videoForm.cover = ''
  data.videoForm.duration = ''
  ElMessage.success('已移除视频')
}

// 保存视频帖子
const saveVideo = () => {
  videoFormRef.value.validate(valid => {
    if (valid) {
      if (!data.videoForm.videoUrl) {
        ElMessage.error('请上传视频文件')
        return
      }
      
      const admin = JSON.parse(localStorage.getItem('xm-admin') || '{}')
      const videoContent = `<video src="${data.videoForm.videoUrl}" controls></video>`
      
      const postData = {
        title: data.videoForm.title,
        content: videoContent,
        description: data.videoForm.description,
        userId: admin.id,
        userName: admin.name,
        userAvatar: admin.avatar
      }
      
      request.post('/article/add', postData).then(res => {
        if (res.code === '200') {
          ElMessage.success('视频发布成功')
          data.formVisible = false
          data.videoForm = {
            title: '',
            description: '',
            videoUrl: '',
            cover: '',
            duration: ''
          }
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/article/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/article/delete/batch", {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  data.title = null
  load()
}
</script>
