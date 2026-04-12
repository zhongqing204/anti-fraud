|NEW_FILE_CODE
<template>
  <el-upload
      :action="uploadUrl"
      :headers="headers"
      :on-success="handleFileUpload"
      :show-file-list="showFileList"
      class="avatar-uploader"
  >
    <img v-if="modelValue" :src="modelValue" class="avatar" />
    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
  </el-upload>
</template>

<script setup>
import { ref,computed } from "vue";
import { ElMessage } from "element-plus";

const baseUrl = import.meta.env.VITE_BASE_URL

const props = defineProps({
  modelValue: String,
  showFileList: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const uploadUrl = computed(() => {
  return baseUrl + '/files/upload'
})

const headers = computed(() => {
  const userStr = localStorage.getItem('xm-user')
  const user = userStr ? JSON.parse(userStr) : {}
  return {
    token: user.token || ''
  }
})

const handleFileUpload = (res, file, fileList) => {
  console.log('上传成功:', res)
  if (res.code === '200' && res.data) {
    const fullUrl = baseUrl + res.data
    emit('update:modelValue', fullUrl)
  } else {
    ElMessage.error(res.msg || '文件上传失败')
    console.error('文件上传失败:', res)
  }
}

const handleError = (err, file, fileList) => {
  console.error('上传错误:', err)
  ElMessage.error('上传失败，请检查网络连接')
}
</script>

<style scoped>
.avatar-uploader {
  height: 120px;
}
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>
