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

    <el-dialog title="视频信息" v-model="data.formVisible" width="60%" destroy-on-close draggable>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="100px" style="padding: 20px">
        <el-form-item prop="title" label="视频标题">
          <el-input v-model="data.form.title" placeholder="请输入视频标题"></el-input>
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
        
        <!-- 【新增】封面选择器 - 从视频中截取 -->
        <el-form-item v-if="data.form.videoUrl" label="选择封面">
          <div style="width: 100%">
            <video 
              ref="coverVideoRef"
              :src="getCoverVideoUrl(data.form.videoUrl)"
              crossorigin="anonymous"
              style="width: 100%; max-height: 300px; border-radius: 5px; cursor: pointer"
              @loadedmetadata="handleVideoLoaded"
              @error="handleVideoError"
              @seeked="handleSeeked"
              @click="togglePreviewPlay"
            ></video>
            <div v-if="!data.videoLoaded" style="text-align: center; color: #999; padding: 20px;">
              <el-icon :size="40" class="is-loading"><Loading /></el-icon>
              <div style="margin-top: 10px">视频加载中...</div>
            </div>
            <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center">
              <el-button type="primary" size="small" @click="retryLoadVideo" v-if="!data.videoLoaded">重新加载</el-button>
              <el-slider 
                v-model="data.currentTime" 
                :max="data.videoDuration" 
                :step="0.1"
                style="flex: 1"
                @input="handleSliderChange"
              />
              <el-button type="primary" size="small" @click="captureFrame" :disabled="!data.videoLoaded">截取当前帧</el-button>
            </div>
            <div style="margin-top: 5px; color: #999; font-size: 12px">拖动进度条选择封面帧，点击"截取当前帧"按钮</div>
            <canvas ref="canvasRef" style="display: none"></canvas>
          </div>
        </el-form-item>
        
        <!-- 【保留】手动上传封面选项 -->
        <el-form-item label="或手动上传封面">
          <el-upload
              :action="baseUrl + '/file/upload'"
              :on-success="handleCoverUpload"
              list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
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

    <el-dialog title="视频播放" v-model="data.playVisible" width="70%" destroy-on-close draggable>
      <video 
        :src="getVideoUrl(data.playUrl)" 
        crossorigin="anonymous"
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
import {Loading} from "@element-plus/icons-vue";

const formRef = ref()
const coverVideoRef = ref(null)
const canvasRef = ref(null)
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-admin') || '{}'), 
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
    videoUrl: [
      { required: true, message: '请上传视频文件', trigger: 'blur' },
    ],
    cover: [
      { required: true, message: '请选择或上传视频封面', trigger: 'blur' },
    ],
    categoryId: [
      { required: true, message: '请选择视频分类', trigger: 'blur' },
    ],
  },
  playVisible: false,
  playUrl: '',
  // 【新增】封面选择相关数据
  currentTime: 0,
  videoDuration: 0,
  videoLoaded: false,
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

// 封面选择器使用的视频URL（支持CORS）
const getCoverVideoUrl = (videoUrl) => {
  if (!videoUrl) return ''
  if (videoUrl.startsWith('http://') || videoUrl.startsWith('https://')) {
    return videoUrl
  }
  // 直接使用原始路径，通过后端CORS配置支持跨域
  return baseUrl + videoUrl
}

const playVideo = (videoUrl) => {
  data.playUrl = videoUrl
  data.playVisible = true
}

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    console.log('分类接口响应:', res)
    if (res.code === '200') {
      data.categoryData = res.data
      console.log('分类数据:', data.categoryData)
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
  data.videoLoaded = false  // 重置加载状态
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row)) 
  data.formVisible = true 
  data.videoLoaded = false  // 重置加载状态
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

/**
 * 【修改】封面上传处理
 */
const handleCoverUpload = (res) => {
  if (res.code === '200') {
    data.form.cover = res.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

/**
 * 【优化】视频上传成功后自动获取时长、提取文件名作为标题、生成第一帧封面
 */
const handleVideoUpload = (res) => {
  if (res.code === '200') {
    data.form.videoUrl = res.data
    
    // 【新增】从文件路径提取文件名作为默认标题
    const fileName = res.data.split('/').pop().replace(/\.[^/.]+$/, '')
    if (!data.form.title) {
      data.form.title = fileName
    }
    
    // 创建临时 video 元素获取时长和生成封面
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.src = baseUrl + res.data  // 直接使用原始路径获取时长
    
    video.onloadedmetadata = () => {
      const duration = video.duration
      const minutes = Math.floor(duration / 60)
      const seconds = Math.floor(duration % 60)
      data.form.duration = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
      
      ElMessage.success(`视频上传成功，时长：${data.form.duration}`)
    }
    
    video.onerror = () => {
      ElMessage.success('视频上传成功')
    }
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

/**
 * 【新增】从视频中截取指定时间的帧作为封面
 */
const generateCoverFromVideo = (videoElement, time) => {
  return new Promise((resolve) => {
    videoElement.currentTime = time
    videoElement.onseeked = () => {
      const canvas = document.createElement('canvas')
      canvas.width = videoElement.videoWidth
      canvas.height = videoElement.videoHeight
      const ctx = canvas.getContext('2d')
      ctx.drawImage(videoElement, 0, 0, canvas.width, canvas.height)
      
      canvas.toBlob((blob) => {
        if (blob) {
          const formData = new FormData()
          formData.append('file', blob, `cover_${Date.now()}.jpg`)
          
          // 使用 axios post 方法上传
          request.post('/file/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }).then(res => {
            if (res.code === '200') {
              data.form.cover = res.data
              resolve(true)
            } else {
              resolve(false)
            }
          }).catch(() => {
            resolve(false)
          })
        } else {
          resolve(false)
        }
      }, 'image/jpeg', 0.8)
    }
  })
}

/**
 * 【新增】视频元数据加载完成
 */
const handleVideoLoaded = () => {
  if (coverVideoRef.value) {
    data.videoDuration = coverVideoRef.value.duration
    data.currentTime = 0
    data.videoLoaded = true
  }
}

/**
 * 【新增】视频加载错误处理
 */
const handleVideoError = (e) => {
  console.error('视频加载失败:', e)
  console.error('视频源:', coverVideoRef.value?.src)
  data.videoLoaded = false
  
  // 获取更详细的错误信息
  const video = coverVideoRef.value
  if (video && video.error) {
    console.error('视频错误代码:', video.error.code)
    console.error('视频错误信息:', video.error.message)
    
    switch(video.error.code) {
      case 1:
        ElMessage.error('视频加载被中止')
        break
      case 2:
        ElMessage.error('视频网络错误，请检查网络连接')
        break
      case 3:
        ElMessage.error('视频解码失败，格式可能不支持')
        break
      case 4:
        ElMessage.error('视频格式不支持或文件不存在')
        break
      default:
        ElMessage.error('视频加载失败，请检查文件是否有效')
    }
  } else {
    ElMessage.error('视频加载失败，请检查视频文件')
  }
}

/**
 * 【新增】重新加载视频
 */
const retryLoadVideo = () => {
  data.videoLoaded = false
  if (coverVideoRef.value && data.form.videoUrl) {
    // 强制重新加载视频
    const video = coverVideoRef.value
    const currentSrc = video.src
    video.src = ''
    setTimeout(() => {
      video.src = currentSrc
      video.load()
    }, 100)
  }
}

/**
 * 【新增】滑块拖动时预览对应帧
 */
const handleSliderChange = (value) => {
  if (coverVideoRef.value) {
    coverVideoRef.value.currentTime = value
  }
}

/**
 * 【新增】视频跳转完成
 */
const handleSeeked = () => {
  // 可以在这里添加预览逻辑
}

/**
 * 【新增】切换预览播放/暂停
 */
const togglePreviewPlay = () => {
  if (coverVideoRef.value && data.form.videoUrl) {
    const video = coverVideoRef.value
    // 检查视频是否有有效的源
    if (!video.src || video.src === window.location.href) {
      ElMessage.warning('视频加载中，请稍候')
      return
    }
    if (video.paused) {
      video.play().catch(err => {
        console.error('播放失败:', err)
        ElMessage.warning('视频加载失败，请重试')
      })
    } else {
      video.pause()
    }
  }
}

/**
 * 【新增】截取当前帧作为封面
 */
const captureFrame = () => {
  if (!coverVideoRef.value || !canvasRef.value) {
    ElMessage.warning('请先上传视频')
    return
  }
  
  const video = coverVideoRef.value
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  
  try {
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    canvas.toBlob((blob) => {
      if (blob) {
        const formData = new FormData()
        formData.append('file', blob, `cover_${Date.now()}.jpg`)
        
        // 使用 axios post 方法上传
        request.post('/file/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then(res => {
          if (res.code === '200') {
            data.form.cover = res.data
            ElMessage.success('封面截取成功')
          } else {
            ElMessage.error(res.msg || '上传失败')
          }
        }).catch(err => {
          console.error('上传失败:', err)
          ElMessage.error('封面上传失败')
        })
      }
    }, 'image/jpeg', 0.8)
  } catch (error) {
    console.error('Canvas导出失败:', error)
    ElMessage.warning('由于浏览器安全限制，无法自动截取封面。请使用“手动上传封面”功能上传图片。')
  }
}

loadCategory()
load()
</script>
