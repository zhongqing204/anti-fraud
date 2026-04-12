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
        <el-table-column prop="title" label="反诈标题" width="200" show-overflow-tooltip align="center" />
        <el-table-column prop="cover" label="封面" align="center">
          <template v-slot="scope">
            <div style="display: flex; justify-content: center;">
              <el-image style="width: 50px; height: 50px; border-radius: 5px; display: block" v-if="scope.row.cover"
                      :src="getCoverUrl(scope.row.cover)" :preview-src-list="[scope.row.cover]" preview-teleported></el-image>
            </div>          
          </template>
        </el-table-column>
        <el-table-column prop="content" label="查看内容" align="center">
          <template v-slot="scope">
            <el-button type="primary" @click="viewInit(scope.row.content)">查看内容</el-button>
          </template>
        </el-table-column>
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

    <el-dialog title="宣传信息" v-model="data.formVisible" width="60%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="100px" style="padding: 20px">
        <el-form-item prop="title" label="宣传标题">
          <el-input v-model="data.form.title" placeholder="请输入宣传标题"></el-input>
        </el-form-item>
        
        <el-form-item prop="cover" label="宣传封面">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :on-success="handleCoverUpload"
              list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        
        <!-- 富文本编辑器区域（使用 Vditor） -->
        <el-form-item prop="content" label="宣传内容">
          <!-- Vditor 编辑器容器 -->
          <div id="vditor-container"></div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="宣传内容" v-model="data.viewVisible" width="50%" destroy-on-close>
      <div class="vditor-reset" style="padding: 10px 20px; line-height: 1.8;" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef, nextTick} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL
const vditorRef = shallowRef(null)

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'), 
  formVisible: false, 
  form: {}, 
  tableData: [], 
  pageNum: 1, 
  pageSize: 5, 
  total: 0, 
  title: null, 
  categoryData: [], 
  ids: [], 
  rules: { 
    title: [
      { required: true, message: '请输入宣传标题', trigger: 'blur' },
    ],
    cover: [
      { required: true, message: '请上传宣传封面', trigger: 'blur' },
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

const initVditor = (initialValue = '') => {
  if (vditorRef.value) {
    vditorRef.value.destroy()
  }
  
  const vditor = new Vditor('vditor-container', {
    height: 500, 
    cache: {
      enable: false, 
    },
    mode: 'ir',
    upload: {
      url: baseUrl + '/files/upload',
      token: data.user.token,
      fieldName: 'file', 
      max: 10 * 1024 * 1024, 
      accept: 'image/*,.png,.jpg,.jpeg,.gif,.webp,.mp4,.avi,.mov',
      success: (editor, msg) => {
        console.log('Vditor 上传成功回调:', msg)
        try {
          const res = JSON.parse(msg)
          if (res.code === '200') {
            const fileUrl = res.data.startsWith('http') ? res.data : baseUrl + res.data
            vditor.insertValue(`![image](${fileUrl})`)
          } else {
            ElMessage.error(res.msg || '上传失败')
          }
        } catch (e) {
          console.error('解析响应失败:', e)
          ElMessage.error('上传失败')
        }
      },
      error: (msg) => {
        console.error('Vditor 上传错误:', msg)
        ElMessage.error('图片上传失败')
      }
    },
    toolbarConfig: {
      pin: true, 
    },
    icon: 'material',
    value: initialValue,
    after: () => {
      console.log('Vditor 编辑器加载完成')
    }
  })
  
  vditorRef.value = vditor
}

onBeforeUnmount(() => {
  if (vditorRef.value) {
    vditorRef.value.destroy() 
  }
})


const viewInit = (content) => {
  data.viewContent = content
  data.viewVisible = true
}

const load = () => {
  request.get('/publicity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total  || 0
    }
  })
}


const handleAdd = () => {
  data.form = {} 
  data.formVisible = true 
  
  nextTick(() => {
    initVditor('') 
  })
}


const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row)) 
  data.formVisible = true 
  
  nextTick(() => {
    initVditor(row.content || '') 
  })
}


const add = () => {

  if (vditorRef.value) {
    data.form.content = vditorRef.value.getValue()
  }
  
  request.post('/publicity/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load() 
    } else {
      ElMessage.error(res.msg)
    }
  })
}


const update = () => {
  if (vditorRef.value) {
    data.form.content = vditorRef.value.getValue()
  }
  
  request.put('/publicity/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load() 
    }
  })
}

const save = () => {
  if (vditorRef.value) {
    data.form.content = vditorRef.value.getValue()
  }
  
  formRef.value.validateField('content', (valid) => {
    if (valid) {
      data.form.id ? update() : add()
    } else {
      ElMessage.error('请填写宣传内容')
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
  load()
}


const handleCoverUpload = (res) => {
  if (res.code === '200') {
    data.form.cover = res.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

load()
</script>

<style scoped>
.vditor-reset {
  /* 段落样式 */
  p {
    margin: 1em 0;
    line-height: 1.8;
  }
  
  /* 换行样式 */
  br {
    display: block;
    margin: 0.5em 0;
  }
  
  /* 列表样式 */
  ul, ol {
    padding-left: 20px;
    margin: 1em 0;
  }
  
  li {
    margin: 0.5em 0;
  }
  
  /* 引用块样式 */
  blockquote {
    margin: 1em 0;
    padding: 10px 20px;
    border-left: 4px solid #ccc;
    background-color: #f8f8f8;
  }
  
  /* 代码块样式 */
  pre {
    margin: 1em 0;
    padding: 10px;
    background-color: #f6f8fa;
    border-radius: 3px;
    overflow-x: auto;
  }
  
  /* 表格样式 */
  table {
    border-collapse: collapse;
    margin: 1em 0;
    width: 100%;
  }
  
  th, td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
  }
  
  th {
    background-color: #f6f8fa;
    font-weight: bold;
  }
  
  /* 【新增】图片样式 - 支持图片居中显示和自适应大小 */
  img {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 1em auto;
  }
  
  /* 【新增】视频样式 - 支持视频居中显示和自适应大小 */
  video {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 1em auto;
  }
}
</style>