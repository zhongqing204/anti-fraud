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

    <el-dialog title="发布内容" v-model="data.formVisible" width="60%" destroy-on-close>
      <el-tabs v-model="data.activeTab" style="padding: 20px">
        <el-tab-pane label="发帖" name="post">
          <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px">
            <el-form-item prop="title" label="帖子标题">
              <el-input v-model="data.form.title" placeholder="请输入帖子标题" maxlength="31" show-word-limit></el-input>
            </el-form-item>
            <el-form-item prop="content" label="帖子内容">
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
              <el-button type="primary" @click="save">发布</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="视频" name="video">
          <el-form ref="videoFormRef" :rules="data.videoRules" :model="data.videoForm" label-width="80px">
            <el-form-item prop="title" label="视频标题">
              <el-input v-model="data.videoForm.title" placeholder="请输入视频标题"></el-input>
            </el-form-item>
            <el-form-item label="视频文件">
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

    <el-dialog title="帖子详情" v-model="data.viewVisible" width="70%" destroy-on-close>
      <div style="padding: 20px; line-height: 1.8;" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef, markRaw} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const videoFormRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = shallowRef()

const toolbarConfig = {}
const editorConfig = { 
  placeholder: '请输入帖子内容...',
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
  formVisible: false, 
  form: {}, 
  videoForm: {},
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
    ],
    content: [
      { required: true, message: '请输入帖子内容', trigger: 'blur' },
    ],
  },
  videoRules: {
    title: [
      { required: true, message: '请输入视频标题', trigger: 'blur' },
    ],
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
  data.videoForm = {}
  data.activeTab = 'post'
  data.formVisible = true 
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row)) 
  data.videoForm = {}
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
      if (!data.form.content || data.form.content === '<p><br></p>') {
        ElMessage.error('请填写帖子内容')
        return
      }
      data.form.id ? update() : add()
    }
  })
}

// 处理视频上传成功
const handleVideoUpload = (response, file, fileList) => {
  if (response.code === '200') {
    data.videoForm.videoUrl = response.data
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error('视频上传失败')
  }
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
      const videoContent = `<p><video src="${baseUrl + data.videoForm.videoUrl}" controls width="100%"></video></p>`
      
      const postData = {
        title: data.videoForm.title,
        content: videoContent,
        userId: admin.id,
        userName: admin.name,
        userAvatar: admin.avatar
      }
      
      request.post('/article/add', postData).then(res => {
        if (res.code === '200') {
          ElMessage.success('视频发布成功')
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
