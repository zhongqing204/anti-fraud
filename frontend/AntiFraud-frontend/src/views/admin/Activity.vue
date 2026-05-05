D:\anti-fraud\frontend\AntiFraud-frontend\src\views\admin\Activity.vue
<template>
  <div>
    <!-- ===== 搜索和新增区域 ===== -->
    <div class="card" style="margin-bottom: 10px">
      <el-input v-model="data.title" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入活动标题查询"></el-input>
      <!-- 【新增】线上线下筛选 -->
      <el-select v-model="data.activityType" placeholder="活动类型" style="width: 120px; margin-right: 10px" clearable>
        <el-option label="线上" value="线上" />
        <el-option label="线下" value="线下" />
      </el-select>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <!-- ===== 表格区域 ===== -->
    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="活动名称" width="200" show-overflow-tooltip />
        <el-table-column prop="cover" label="活动封面" width="100">
          <template v-slot="scope">
            <el-image style="width: 50px; height: 50px; border-radius: 5px; display: block" v-if="scope.row.cover"
                      :src="getCoverUrl(scope.row.cover)" :preview-src-list="[getCoverUrl(scope.row.cover)]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="反诈分类" width="100" />
        <!-- 【新增】活动类型列 -->
        <el-table-column label="活动类型" width="80">
          <template v-slot="scope">
            <el-tag :type="scope.row.activityType === '线上' ? 'success' : 'primary'">
              {{ scope.row.activityType || '未设置' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 【修改】时间显示精确到分钟 -->
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template v-slot="scope">
            {{ formatTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template v-slot="scope">
            {{ formatTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <!-- 【新增】地点/参与方式列 -->
        <el-table-column label="地点/参与方式" width="180" show-overflow-tooltip>
          <template v-slot="scope">
            <span v-if="scope.row.activityType === '线下'">{{ scope.row.location || '未设置' }}</span>
            <span v-else-if="scope.row.activityType === '线上'">线上活动</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="活动状态" width="100">
          <template v-slot="scope">
            <el-tag :type="scope.row.status === '已结束' ? 'info' : 'success'">{{ scope.row.status || '进行中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template v-slot="scope">
            <el-button type="warning" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ===== 分页区域 ===== -->
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <!-- ===== 新增/编辑活动表单对话框 ===== -->
    <el-dialog :title="data.form.id ? '编辑活动' : '新增活动'" v-model="data.formVisible" width="90%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding: 20px">
        <el-form-item prop="title" label="活动标题">
          <el-input v-model="data.form.title" placeholder="请输入活动标题"></el-input>
        </el-form-item>
        
        <el-form-item prop="cover" label="活动封面">
          <el-upload
              :action="baseUrl + '/file/upload'"
              :on-success="handleCoverUpload"
              list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        
        <el-form-item prop="categoryId" label="反诈分类">
          <el-select v-model="data.form.categoryId" placeholder="请选择反诈分类" style="width: 100%">
            <el-option
                v-for="item in data.categoryData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <!-- 【新增】活动类型选择 -->
        <el-form-item prop="activityType" label="活动类型">
          <el-radio-group v-model="data.form.activityType">
            <el-radio value="线上">线上</el-radio>
            <el-radio value="线下">线下</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 【新增】线下活动地点 -->
        <el-form-item v-if="data.form.activityType === '线下'" prop="location" label="活动地点">
          <el-input v-model="data.form.location" placeholder="请输入活动地点"></el-input>
        </el-form-item>
        
        <!-- 【新增】线上活动参与方式（富文本编辑器） -->
        <el-form-item v-if="data.form.activityType === '线上'" prop="participationMethod" label="参与方式">
          <div style="border: 1px solid #ccc; width: 100%; height: 400px;">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="participationEditorRef"
              :defaultConfig="participationToolbarConfig"
              mode="default"
            />
            <Editor
              style="height: 350px; overflow-y: hidden;"
              v-model="data.form.participationMethod"
              :defaultConfig="participationEditorConfig"
              mode="default"
              @onCreated="handleParticipationCreated"
            />
          </div>
        </el-form-item>
        
        <!-- ===== 活动开始时间（精确到分钟） ===== -->
        <el-form-item prop="startTime" label="开始时间">
          <el-date-picker
              v-model="data.form.startTime"
              type="datetime"
              placeholder="请选择活动开始时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm"
              style="width: 100%"
          />
        </el-form-item>
        
        <!-- ===== 活动结束时间（精确到分钟） ===== -->
        <el-form-item prop="endTime" label="结束时间">
          <el-date-picker
              v-model="data.form.endTime"
              type="datetime"
              placeholder="请选择活动结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              format="YYYY-MM-DD HH:mm"
              style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item prop="status" label="活动状态">
          <el-select v-model="data.form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
        
        <!-- 修改：使用 wangEditor 富文本编辑器 -->
        <el-form-item prop="content" label="活动内容" style="width: 100%;">
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
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef, markRaw} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
// ===== baseUrl用于文件上传 =====
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = shallowRef()
// 【新增】参与方式富文本编辑器
const participationEditorRef = shallowRef()

const toolbarConfig = {}

// 【新增】参与方式编辑器配置
const participationToolbarConfig = {
  toolbarKeys: [
    'bold',
    'italic',
    'underline',
    '|',
    'insertLink',
    'insertImage',
    '|',
    'undo',
    'redo'
  ]
}

const participationEditorConfig = { 
  placeholder: '请输入参与方式或插入二维码图片...',
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

// ===== 编辑器配置：支持图片和视频上传 =====
const editorConfig = { 
  placeholder: '请输入活动内容...',
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

// ===== 组件卸载时销毁编辑器 =====
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
  
  // 【新增】销毁参与方式编辑器
  const participationEditor = participationEditorRef.value
  if (participationEditor == null) return
  participationEditor.destroy()
})

const handleCreated = (editor) => {
  editorRef.value = markRaw(editor)
}

// 【新增】参与方式编辑器创建回调
const handleParticipationCreated = (editor) => {
  participationEditorRef.value = markRaw(editor)
}

// 【新增】格式化时间显示
const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  title: null,
  // 【新增】线上线下筛选字段
  activityType: null,
  categoryData: [],
  ids: [],
  rules: {
    title: [
      { required: true, message: '请输入活动标题', trigger: 'blur' },
    ],
    cover: [
      { required: true, message: '请上传活动封面', trigger: 'blur' },
    ],
    categoryId: [
      { required: true, message: '请选择反诈分类', trigger: 'blur' },
    ],
    // 【新增】活动类型必填
    activityType: [
      { required: true, message: '请选择活动类型', trigger: 'change' },
    ],
    // 【新增】线下活动地点必填
    location: [
      { 
        required: true, 
        validator: (rule, value, callback) => {
          if (data.form.activityType === '线下' && (!value || !value.trim())) {
            callback(new Error('请输入活动地点'))
          } else {
            callback()
          }
        },
        trigger: 'blur' 
      },
    ],
    // 【新增】线上活动参与方式必填
    participationMethod: [
      { 
        required: true, 
        validator: (rule, value, callback) => {
          if (data.form.activityType === '线上' && (!value || !value.trim() || value === '<p><br></p>')) {
            callback(new Error('请输入参与方式或插入二维码'))
          } else {
            callback()
          }
        },
        trigger: 'blur' 
      },
    ],
    // ===== 开始时间和结束时间必填 =====
    startTime: [
      { required: true, message: '请选择活动开始时间', trigger: 'blur' },
    ],
    endTime: [
      { required: true, message: '请选择活动结束时间', trigger: 'blur' },
    ],
    content: [
      { required: true, message: '请输入活动内容', trigger: 'blur' },
    ],
  },
})

// ===== 获取封面完整URL =====
const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

// ===== 封面上传成功回调 =====
const handleCoverUpload = (res) => {
  if (res.code === '200') {
    data.form.cover = res.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res.msg || '封面上传失败')
  }
}

// 加载活动分类数据
const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') {
      data.categoryData = res.data
    }
  })
}
loadCategory()

// 加载活动数据
const load = () => {
  request.get('/activity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      // 【新增】线上线下筛选参数
      activityType: data.activityType
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total
    }
  })
}
load()

// 新增活动
const handleAdd = () => {
  data.form = {
    activityType: '线下', // 【新增】默认线下
    status: '未开始'
  }
  data.form.content = '' // 修改：初始化空内容
  data.form.participationMethod = '' // 【新增】初始化参与方式
  data.formVisible = true
}

// 编辑活动
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  if (!data.form.activityType) {
    data.form.activityType = '线下' // 【新增】兼容旧数据
  }
  if (!data.form.participationMethod) {
    data.form.participationMethod = '' // 【新增】兼容旧数据
  }
  data.formVisible = true
}

// 保存活动
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      // 修改：验证内容不为空或默认占位符
      if (!data.form.content || data.form.content === '<p><br></p>') {
        ElMessage.error('请填写活动内容')
        return
      }
      
      // 【新增】验证参与方式不为空或默认占位符
      if (data.form.activityType === '线上') {
        if (!data.form.participationMethod || data.form.participationMethod === '<p><br></p>') {
          ElMessage.error('请填写参与方式或插入二维码')
          return
        }
      }
      
      // 【修改】编辑时使用PUT请求，新增使用POST
      const url = data.form.id ? '/activity/update' : '/activity/add'
      const api = data.form.id ? request.put : request.post
      api(url, data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('操作成功')
          data.formVisible = false
          data.form.content = '' // 修改：清空内容
          data.form.participationMethod = '' // 【新增】清空参与方式
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 删除单个活动
const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/activity/delete/' + id).then(res => {
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

// 批量删除活动
const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/activity/delete/batch", {data: data.ids}).then(res => {
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

// 选择框变化
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

// 重置搜索
const reset = () => {
  data.title = null
  data.activityType = null // 【新增】重置线上线下筛选
  load()
}
</script>
