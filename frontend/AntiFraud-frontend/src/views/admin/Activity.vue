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
        <!-- 【新增】限制人数列 -->
        <el-table-column label="限制人数" width="100">
          <template v-slot="scope">
            <span v-if="scope.row.maxParticipants && scope.row.maxParticipants > 0">
              {{ scope.row.currentParticipants || 0 }}/{{ scope.row.maxParticipants }}
            </span>
            <span v-else style="color: #999">不限制</span>
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
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status || '进行中' }}</el-tag>
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
    <el-dialog :title="data.form.id ? '编辑活动' : '新增活动'" v-model="data.formVisible" width="90%" destroy-on-close draggable>
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
        <el-form-item prop="activityType" label="活动形式">
          <el-radio-group v-model="data.form.activityType">
            <el-radio value="线上">线上</el-radio>
            <el-radio value="线下">线下</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 【新增】持续时间类型选择 -->
        <el-form-item prop="activityDurationType" label="活动类型">
          <el-radio-group v-model="data.form.activityDurationType">
            <el-radio value="short">短期活动（一天）</el-radio>
            <el-radio value="long">长期活动（多天）</el-radio>
          </el-radio-group>
          <div style="color: #999; font-size: 12px; margin-top: 5px;">
            短期：只开一天；长期：持续多天，每天固定时间段
          </div>
        </el-form-item>
        
        <!-- 【新增】持续天数（仅长期活动显示） -->
        <el-form-item v-if="data.form.activityDurationType === 'long'" prop="durationDays" label="持续天数">
          <el-input-number v-model="data.form.durationDays" :min="2" :max="30" style="width: 100%" />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">
            根据开始和结束时间自动计算，也可手动调整
          </div>
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
        
        <!-- ===== 短期活动：日期 + 时间段 ===== -->
        <template v-if="data.form.activityDurationType === 'short'">
          <el-form-item prop="activityDate" label="活动日期">
            <el-date-picker
                v-model="data.form.activityDate"
                type="date"
                placeholder="请选择活动日期"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="活动时间段" required>
            <div style="display: flex; gap: 10px; align-items: center;">
              <el-time-picker
                  v-model="data.form.startTimeOnly"
                  placeholder="开始时间"
                  value-format="HH:mm:ss"
                  format="HH:mm"
                  style="flex: 1"
                  :disabled-hours="getDisabledStartHours"
                  :disabled-minutes="getDisabledStartMinutes"
              />
              <span>至</span>
              <el-time-picker
                  v-model="data.form.endTimeOnly"
                  placeholder="结束时间"
                  value-format="HH:mm:ss"
                  format="HH:mm"
                  style="flex: 1"
                  :disabled-hours="getDisabledEndHours"
                  :disabled-minutes="getDisabledEndMinutes"
              />
            </div>
            <div style="color: #999; font-size: 12px; margin-top: 5px;">
              例如：14:00-16:00，表示下午2点到4点
            </div>
          </el-form-item>
        </template>
        
        <!-- ===== 长期活动：活动周期 + 每天时间段 ===== -->
        <template v-else>
          <el-form-item prop="startDate" label="活动开始日期">
            <el-date-picker
                v-model="data.form.startDate"
                type="date"
                placeholder="请选择活动开始日期"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 100%"
                @change="calculateDurationDays"
            />
          </el-form-item>
          
          <el-form-item prop="endDate" label="活动结束日期">
            <el-date-picker
                v-model="data.form.endDate"
                type="date"
                placeholder="请选择活动结束日期"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 100%"
                :disabled-date="disabledEndDate"
                @change="calculateDurationDays"
            />
          </el-form-item>
          
          <el-form-item label="每天活动时间段" required>
            <div style="display: flex; gap: 10px; align-items: center;">
              <el-time-picker
                  v-model="data.form.dailyStartTime"
                  placeholder="每天开始时间"
                  value-format="HH:mm:ss"
                  format="HH:mm"
                  style="flex: 1"
                  :disabled-hours="getDisabledDailyStartHours"
                  :disabled-minutes="getDisabledDailyStartMinutes"
              />
              <span>至</span>
              <el-time-picker
                  v-model="data.form.dailyEndTime"
                  placeholder="每天结束时间"
                  value-format="HH:mm:ss"
                  format="HH:mm"
                  style="flex: 1"
                  :disabled-hours="getDisabledDailyEndHours"
                  :disabled-minutes="getDisabledDailyEndMinutes"
              />
            </div>
            <div style="color: #999; font-size: 12px; margin-top: 5px;">
              例如：12:00-13:00，表示每天中午12点到1点开放
            </div>
          </el-form-item>
        </template>
        
        <!-- 【新增】限制人数 -->
        <el-form-item label="限制人数">
          <el-input-number 
            v-model="data.form.maxParticipants" 
            :min="0" 
            :max="99999"
            placeholder="0表示不限制"
            style="width: 100%"
          />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">
            <span v-if="data.form.activityDurationType === 'long'">
              每天的限制人数（共 {{ data.form.durationDays || 1 }} 天，每天可报 {{ data.form.maxParticipants || '不限' }} 人）
            </span>
            <span v-else>设置为0表示不限制报名人数</span>
          </div>
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

// 【新增】监听开始和结束时间变化，自动计算持续天数
const calculateDurationDays = () => {
  if (data.form.activityDurationType !== 'long') return
  if (!data.form.startDate || !data.form.endDate) return
  
  const start = new Date(data.form.startDate)
  const end = new Date(data.form.endDate)
  
  // 计算相差的天数
  const diffTime = end.getTime() - start.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1 // +1因为包含当天
  
  if (diffDays > 0 && diffDays !== data.form.durationDays) {
    data.form.durationDays = diffDays
  }
}

// 【新增】验证时间合理性
const validateTimeRange = () => {
  if (data.form.activityDurationType === 'short') {
    // 短期活动：验证时间段
    if (data.form.startTimeOnly && data.form.endTimeOnly) {
      const start = new Date(`2000-01-01 ${data.form.startTimeOnly}`)
      const end = new Date(`2000-01-01 ${data.form.endTimeOnly}`)
      if (end <= start) {
        ElMessage.warning('结束时间必须晚于开始时间')
        data.form.endTimeOnly = ''
        return false
      }
    }
  } else {
    // 长期活动：验证日期和时间段
    if (data.form.startDate && data.form.endDate) {
      const start = new Date(data.form.startDate)
      const end = new Date(data.form.endDate)
      if (end < start) {
        ElMessage.warning('结束日期不能早于开始日期')
        data.form.endDate = ''
        return false
      }
    }
    if (data.form.dailyStartTime && data.form.dailyEndTime) {
      const start = new Date(`2000-01-01 ${data.form.dailyStartTime}`)
      const end = new Date(`2000-01-01 ${data.form.dailyEndTime}`)
      if (end <= start) {
        ElMessage.warning('每天结束时间必须晚于开始时间')
        data.form.dailyEndTime = ''
        return false
      }
    }
  }
  return true
}

// ===== 短期活动时间禁用逻辑 =====
// 禁用开始时间的小时（不能晚于结束时间的小时）
const getDisabledStartHours = () => {
  if (!data.form.endTimeOnly) return []
  const endHour = parseInt(data.form.endTimeOnly.split(':')[0])
  const disabled = []
  for (let i = endHour; i < 24; i++) {
    disabled.push(i)
  }
  return disabled
}

// 禁用开始时间的分钟（如果小时相同，分钟不能晚于结束时间的分钟）
const getDisabledStartMinutes = (hour) => {
  if (!data.form.endTimeOnly) return []
  const [endHour, endMinute] = data.form.endTimeOnly.split(':').map(Number)
  if (hour === endHour) {
    const disabled = []
    for (let i = endMinute; i < 60; i++) {
      disabled.push(i)
    }
    return disabled
  }
  return []
}

// 禁用结束时间的小时（不能早于开始时间的小时）
const getDisabledEndHours = () => {
  if (!data.form.startTimeOnly) return []
  const startHour = parseInt(data.form.startTimeOnly.split(':')[0])
  const disabled = []
  for (let i = 0; i <= startHour; i++) {
    disabled.push(i)
  }
  return disabled
}

// 禁用结束时间的分钟（如果小时相同，分钟不能早于开始时间的分钟）
const getDisabledEndMinutes = (hour) => {
  if (!data.form.startTimeOnly) return []
  const [startHour, startMinute] = data.form.startTimeOnly.split(':').map(Number)
  if (hour === startHour) {
    const disabled = []
    for (let i = 0; i <= startMinute; i++) {
      disabled.push(i)
    }
    return disabled
  }
  return []
}

// ===== 长期活动时间禁用逻辑 =====
const getDisabledDailyStartHours = () => {
  if (!data.form.dailyEndTime) return []
  const endHour = parseInt(data.form.dailyEndTime.split(':')[0])
  const disabled = []
  for (let i = endHour; i < 24; i++) {
    disabled.push(i)
  }
  return disabled
}

const getDisabledDailyStartMinutes = (hour) => {
  if (!data.form.dailyEndTime) return []
  const [endHour, endMinute] = data.form.dailyEndTime.split(':').map(Number)
  if (hour === endHour) {
    const disabled = []
    for (let i = endMinute; i < 60; i++) {
      disabled.push(i)
    }
    return disabled
  }
  return []
}

const getDisabledDailyEndHours = () => {
  if (!data.form.dailyStartTime) return []
  const startHour = parseInt(data.form.dailyStartTime.split(':')[0])
  const disabled = []
  for (let i = 0; i <= startHour; i++) {
    disabled.push(i)
  }
  return disabled
}

const getDisabledDailyEndMinutes = (hour) => {
  if (!data.form.dailyStartTime) return []
  const [startHour, startMinute] = data.form.dailyStartTime.split(':').map(Number)
  if (hour === startHour) {
    const disabled = []
    for (let i = 0; i <= startMinute; i++) {
      disabled.push(i)
    }
    return disabled
  }
  return []
}

// 【新增】禁用结束日期（不能早于开始日期）
const disabledEndDate = (time) => {
  if (!data.form.startDate) return false
  const startDate = new Date(data.form.startDate)
  // 禁用所有早于开始日期的日期
  return time.getTime() < startDate.getTime()
}

// 【新增】获取状态标签类型
const getStatusType = (status) => {
  switch(status) {
    case '未开始': return ''
    case '进行中': return 'success'
    case '已结束': return 'info'
    default: return 'success'
  }
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
    activityDurationType: 'short', // 【新增】默认短期活动
    durationDays: 1, // 【新增】默认1天
    maxParticipants: 0, // 【新增】默认不限制
    // 短期活动字段
    activityDate: '',
    startTimeOnly: '14:00:00',
    endTimeOnly: '16:00:00',
    // 长期活动字段
    startDate: '',
    endDate: '',
    dailyStartTime: '09:00:00',
    dailyEndTime: '17:00:00'
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
  if (!data.form.activityDurationType) {
    data.form.activityDurationType = 'short' // 【新增】兼容旧数据
  }
  if (!data.form.durationDays) {
    data.form.durationDays = 1 // 【新增】兼容旧数据
  }
  if (!data.form.participationMethod) {
    data.form.participationMethod = '' // 【新增】兼容旧数据
  }
  
  // 【新增】短期活动字段初始化
  if (data.form.activityDurationType === 'short' && data.form.startTime) {
    data.form.activityDate = data.form.startTime.substring(0, 10)
    data.form.startTimeOnly = data.form.startTime.substring(11, 19)
    data.form.endTimeOnly = data.form.endTime ? data.form.endTime.substring(11, 19) : ''
  }
  
  // 【新增】长期活动字段初始化
  if (data.form.activityDurationType === 'long') {
    if (!data.form.startDate && data.form.startTime) {
      data.form.startDate = data.form.startTime.substring(0, 10)
    }
    if (!data.form.endDate && data.form.endTime) {
      data.form.endDate = data.form.endTime.substring(0, 10)
    }
    if (!data.form.dailyStartTime) {
      data.form.dailyStartTime = data.form.startTime ? data.form.startTime.substring(11, 19) : '09:00:00'
    }
    if (!data.form.dailyEndTime) {
      data.form.dailyEndTime = data.form.endTime ? data.form.endTime.substring(11, 19) : '17:00:00'
    }
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
      
      // 【新增】验证时间合理性
      if (!validateTimeRange()) {
        return
      }
      
      // 【新增】短期活动：合并日期和时间段为startTime和endTime
      if (data.form.activityDurationType === 'short') {
        if (!data.form.activityDate) {
          ElMessage.error('请选择活动日期')
          return
        }
        if (!data.form.startTimeOnly || !data.form.endTimeOnly) {
          ElMessage.error('请设置活动时间段')
          return
        }
        
        // 构造startTime和endTime
        const submitData = { ...data.form }
        submitData.startTime = `${submitData.activityDate} ${submitData.startTimeOnly}`
        submitData.endTime = `${submitData.activityDate} ${submitData.endTimeOnly}`
        
        // 删除临时字段
        delete submitData.activityDate
        delete submitData.startTimeOnly
        delete submitData.endTimeOnly
        
        // 提交
        const url = submitData.id ? '/activity/update' : '/activity/add'
        const api = submitData.id ? request.put : request.post
        api(url, submitData).then(res => {
          if (res.code === '200') {
            ElMessage.success('操作成功')
            data.formVisible = false
            data.form.content = ''
            data.form.participationMethod = ''
            load()
          } else {
            ElMessage.error(res.msg)
          }
        })
        return
      }
      
      // 【新增】长期活动：合并日期和时间段为startTime和endTime
      if (data.form.activityDurationType === 'long') {
        if (!data.form.startDate || !data.form.endDate) {
          ElMessage.error('请选择活动开始和结束日期')
          return
        }
        if (!data.form.dailyStartTime || !data.form.dailyEndTime) {
          ElMessage.error('请设置每天的活动时间段')
          return
        }
        
        // 构造startTime和endTime（后端需要）
        const submitData = { ...data.form }
        submitData.startTime = `${submitData.startDate} ${submitData.dailyStartTime}`
        submitData.endTime = `${submitData.endDate} ${submitData.dailyEndTime}`
        
        // 删除临时字段
        delete submitData.startDate
        delete submitData.endDate
        delete submitData.dailyStartTime
        delete submitData.dailyEndTime
        
        // 提交
        const url = submitData.id ? '/activity/update' : '/activity/add'
        const api = submitData.id ? request.put : request.post
        api(url, submitData).then(res => {
          if (res.code === '200') {
            ElMessage.success('操作成功')
            data.formVisible = false
            data.form.content = ''
            data.form.participationMethod = ''
            load()
          } else {
            ElMessage.error(res.msg)
          }
        })
        return
      }
      
      // 兼容旧数据：直接提交
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
