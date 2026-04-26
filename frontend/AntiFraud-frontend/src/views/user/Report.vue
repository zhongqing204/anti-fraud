<template>
  <div>
    <div style="width: 50%; margin: 20px auto">
      <div class="card" style="padding: 20px">
        <div>
          <el-input 
            type="textarea" 
            :rows="5" 
            v-model="data.content" 
            placeholder="请输入你要举报的内容"
            style="margin-bottom: 10px;"
          />
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 8px; border: 1px solid #dcdfe6; border-top: none; border-radius: 0 0 4px 4px; background: #f5f7fa;">
            <div style="display: flex; align-items: center; gap: 15px; flex: 1;">
              <el-upload
                  :action="baseUrl + '/file/upload'"
                  :on-success="handleFileUpload"
                  :on-remove="handleFileRemove"
                  :file-list="data.fileList"
                  multiple
                  accept=".jpg,.jpeg,.png,.gif,.webp,.bmp"
                  :show-file-list="false"
              >
                <el-icon style="font-size: 20px; cursor: pointer; color: #606266;" title="上传图片">
                  <Picture />
                </el-icon>
              </el-upload>
              <div v-if="data.fileList.length > 0" style="display: flex; flex-wrap: wrap; gap: 8px;">
                <el-tag 
                  v-for="(file, index) in data.fileList" 
                  :key="index"
                  closable
                  @close="removeFile(index)"
                  style="max-width: 150px;"
                >
                  {{ file.name }}
                </el-tag>
              </div>
            </div>
            <el-button type="primary" @click="submit">提交</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Picture, Close} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  content: null,
  fileList: [],
  fileUrls: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  reportData: [],
})

const loadReports = () => {
  request.get('/report/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
    }
  }).then(res => {
    if (res.code === '200') {
      data.reportData = res.data?.records || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadReports()

const submit = () => {
  if (!data.content) {
    ElMessage.warning('请输入举报内容')
    return
  }
  request.post('/report/add', {
    userId: data.user.id,
    content: data.content,
    files: data.fileUrls.join(','),
    status: '待处理'
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('举报成功，我们会尽快处理')
      data.content = null
      data.fileList = []
      data.fileUrls = []
      loadReports()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleFileUpload = (response, file, fileList) => {
  if (response.code === '200') {
    data.fileUrls.push(response.data)
    data.fileList = fileList
    // 只有当上传完成的文件数量等于当前文件列表数量时才提示
    if (data.fileUrls.length === fileList.length) {
      ElMessage.success('图片上传成功')
    }
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleFileRemove = (file, fileList) => {
  data.fileList = fileList
}

const removeFile = (index) => {
  data.fileUrls.splice(index, 1)
  data.fileList.splice(index, 1)
}
</script>
