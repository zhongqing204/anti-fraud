<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.title" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入视频标题查询"></el-input>
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
        <el-table-column prop="title" label="视频标题" width="200" show-overflow-tooltip align="center" />
        <el-table-column prop="cover" label="封面" align="center">
          <template v-slot="scope">
            <div style="display: flex; justify-content: center;">
              <el-image style="width: 80px; height: 50px; border-radius: 5px; display: block" v-if="scope.row.cover"
                      :src="getCoverUrl(scope.row.cover)" :preview-src-list="[getCoverUrl(scope.row.cover)]" preview-teleported></el-image>
            </div>          
          </template>
        </el-table-column>
        <el-table-column prop="videoUrl" label="视频" align="center">
          <template v-slot="scope">
            <el-button type="primary" link @click="playVideo(scope.row.videoUrl)">播放</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="100" align="center" />
        <el-table-column prop="categoryName" label="视频分类" width="120" align="center" />
        <el-table-column prop="viewCount" label="播放量" width="100" align="center" />
        <el-table-column prop="createTime" label="上传时间" width="180" align="center" />
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

    <el-dialog title="视频信息" v-model="data.formVisible" width="60%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="100px" style="padding: 20px">
        <el-form-item prop="title" label="视频标题">
          <el-input v-model="data.form.title" placeholder="请输入视频标题"></el-input>
        </el-form-item>
        
        <el-form-item prop="cover" label="视频封面">
          <el-upload
              :action="baseUrl + '/file/upload'"
              :on-success="handleCoverUpload"
              list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        
        <el-form-item prop="videoUrl" label="视频文件">
          <el-upload
              :action="baseUrl + '/file/upload'"
              :on-success="handleVideoUpload"
              accept="video/*"
          >
            <el-button type="primary">点击上传视频</el-button>
          </el-upload>
        </el-form-item>
        
        <el-form-item prop="duration" label="视频时长">
          <el-input v-model="data.form.duration" placeholder="上传视频后自动获取" readonly></el-input>
        </el-form-item>
        
        <el-form-item prop="categoryId" label="视频分类">
          <el-select v-model="data.form.categoryId" placeholder="请选择视频分类">
            <el-option
                v-for="item in data.categoryData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item prop="description" label="视频描述">
          <el-input v-model="data.form.description" type="textarea" :rows="3" placeholder="请输入视频描述"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="视频播放" v-model="data.playVisible" width="70%" destroy-on-close>
      <video 
        :src="getVideoUrl(data.playUrl)" 
        controls 
        style="width: 100%; max-height: 500px"
      ></video>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'), 
  formVisible: false, 
  form: {}, 
  tableData: [], 
  pageNum: 1, 
  pageSize: 10, 
  total: 0, 
  title: null, 
  categoryId: null,
  categoryData: [], 
  ids: [], 
  rules: { 
    title: [
      { required: true, message: '请输入视频标题', trigger: 'blur' },
    ],
    cover: [
      { required: true, message: '请上传视频封面', trigger: 'blur' },
    ],
    videoUrl: [
      { required: true, message: '请上传视频文件', trigger: 'blur' },
    ],
    categoryId: [
      { required: true, message: '请选择视频分类', trigger: 'blur' },
    ],
  },
  playVisible: false,
  playUrl: '',
})

const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

const getVideoUrl = (videoUrl) => {
  if (!videoUrl) return ''
  if (videoUrl.startsWith('http://') || videoUrl.startsWith('https://')) {
    return videoUrl
  }
  return baseUrl + videoUrl
}

const playVideo = (videoUrl) => {
  data.playUrl = videoUrl
  data.playVisible = true
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
  request.get('/video/selectPage', {
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
  data.formVisible = true 
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row)) 
  data.formVisible = true 
}

const add = () => {
  request.post('/video/add', data.form).then(res => {
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
  request.put('/video/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load() 
    }else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      data.form.id ? update() : add()
    } else {
      ElMessage.error('请填写必填项')
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/video/delete/' + id).then(res => {
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
    request.delete("/video/delete/batch", {data: data.ids}).then(res => {
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
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

/**
 * 修改：视频上传成功后自动获取时长
 */
const handleVideoUpload = (res) => {
  if (res.code === '200') {
    data.form.videoUrl = res.data
    
    // 创建临时 video 元素获取时长
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.src = getVideoUrl(res.data)
    
    video.onloadedmetadata = () => {
      const duration = video.duration
      const minutes = Math.floor(duration / 60)
      const seconds = Math.floor(duration % 60)
      data.form.duration = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
      ElMessage.success(`视频上传成功，时长：${data.form.duration}`)
      
      // 清理临时元素
      URL.revokeObjectURL(video.src)
    }
    
    video.onerror = () => {
      ElMessage.success('视频上传成功')
    }
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

loadCategory()
load()
</script>
