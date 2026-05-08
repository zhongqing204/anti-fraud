<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.title" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入宣传标题查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="宣传名称" width="200" show-overflow-tooltip align="center">
          <template v-slot="scope">
            <span class="title-link" @click="viewInit(scope.row.content)">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cover" label="封面" align="center">
          <template v-slot="scope">
            <div style="display: flex; justify-content: center;">
              <el-image style="width: 50px; height: 50px; border-radius: 5px; display: block" v-if="scope.row.cover"
                      :src="getCoverUrl(scope.row.cover)" :preview-src-list="[getCoverUrl(scope.row.cover)]" preview-teleported></el-image>
            </div>          
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="反诈分类" align="center" />
        <el-table-column prop="createTime" label="发布时间" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template v-slot="scope">
            <el-button type="warning" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="宣传信息" v-model="data.formVisible" width="90%" destroy-on-close draggable>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="title" label="宣传标题">
          <el-input v-model="data.form.title" placeholder="请输入宣传标题"></el-input>
        </el-form-item>
        
        <el-form-item prop="cover" label="宣传封面">
          <el-upload
              :action="baseUrl + '/file/upload'"
              :on-success="handleCoverUpload"
              list-type="picture-card"
              :file-list="data.fileList"
              :limit="1"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item prop="categoryId" label="反诈分类">
          <el-select v-model="data.form.categoryId" placeholder="请选择反诈分类">
            <el-option
                v-for="item in data.categoryData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item prop="content" label="宣传内容" style="width: 100%;">
          <div style="border: 1px solid #ccc; width: 100%; height: 600px;">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              style="height: 550px; overflow-y: hidden;"
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
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="宣传内容" v-model="data.viewVisible" width="50%" destroy-on-close draggable>
      <div style="padding: 10px 20px; line-height: 1.8;" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef, markRaw} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Plus} from "@element-plus/icons-vue";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = shallowRef()

const toolbarConfig = {}

const editorConfig = { 
  placeholder: '请输入宣传内容...',
  MENU_CONF: {
    uploadImage: {
      server: baseUrl + '/file/upload',
      fieldName: 'file',
      maxFileSize: 100 * 1024 * 1024,
      allowedFileTypes: ['image/*'],
      customInsert(res, insertFn) {
        if (res.code === '200') {
          const url = res.data.startsWith('http') ? res.data : baseUrl + res.data
          insertFn(url, '', url)
        } else {
          ElMessage.error(res.msg || '上传失败')
        }
      }
    },
    uploadVideo: {
      server: baseUrl + '/file/upload',
      fieldName: 'file',
      maxFileSize: 100 * 1024 * 1024,
      allowedFileTypes: ['video/*'],
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
  formVisible: false, 
  form: {}, 
  tableData: [], 
  pageNum: 1, 
  pageSize: 5, 
  total: 0, 
  title: null, 
  categoryId: null,
  categoryData: [], 
  ids: [], 
  fileList: [],
  rules: { 
    title: [
      { required: true, message: '请输入宣传标题', trigger: 'blur' },
    ],
    cover: [
      { required: true, message: '请上传宣传封面', trigger: 'blur' },
    ],
    categoryId: [
      { required: true, message: '请选择反诈分类', trigger: 'blur' },
    ],
    content: [
      { required: true, message: '请输入宣传内容', trigger: 'blur' },
    ],
  },
  viewContent: null, 
  viewVisible: false, 
})

const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

const viewInit = (content) => {
  data.viewContent = content
  data.viewVisible = true
}

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') {
      data.categoryData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    console.error('加载分类失败:', err)
    ElMessage.error('加载分类失败')
  })
}

const load = () => {
  request.get('/publicity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total  || 0
    }
  })
}

loadCategory()

const handleAdd = () => {
  data.form = {} 
  data.form.content = ''
  data.fileList = []
  data.formVisible = true 
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row)) 
  data.formVisible = true 
  // 如果有封面，显示在上传组件中
  if (row.cover) {
    data.fileList = [{
      name: 'cover',
      url: getCoverUrl(row.cover)
    }]
  } else {
    data.fileList = []
  }
}

const add = () => {
  request.post('/publicity/add', data.form).then(res => {
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
  request.put('/publicity/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      data.form.content = ''
      load() 
    }else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  formRef.value.validateField('content', (valid) => {
    if (!data.form.content || data.form.content === '<p><br></p>') {
      ElMessage.error('请填写宣传内容')
      return
    }
    if (valid) {
      data.form.id ? update() : add()
    } else {
      ElMessage.error('请填写必填项')
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/publicity/delete/' + id).then(res => {
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
    request.delete("/publicity/delete/batch", {data: data.ids}).then(res => {
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
  data.categoryId = null
  load()
}

const handleCoverUpload = (res) => {
  if (res.code === '200') {
    data.form.cover = res.data
    // 更新文件列表显示
    data.fileList = [{
      name: 'cover',
      url: getCoverUrl(res.data)
    }]
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

loadCategory()
load()
</script>

<style scoped>
.title-link {
  display: inline-block;
  cursor: pointer;
}
</style>
