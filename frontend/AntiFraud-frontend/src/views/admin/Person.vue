<template>
  <PersonForm
    :userData="data.user"
    label-width="70px"
    @submit="handleUpdate"
  />
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: {}
})

onMounted(() => {
  const storedUser = localStorage.getItem('xm-user')
  if (storedUser) {
    data.user = JSON.parse(storedUser)
  } else {
    request.get('/admin/current').then(res => {
      if (res.code === '200') {
        data.user = res.data
        localStorage.setItem('xm-user', JSON.stringify(data.user))
      }
    })
  }
})

const handleFileUpload = (res) => {
  if (res.code === '200') {
    data.user.avatar = baseUrl + res.data
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const emit = defineEmits(['updateUser'])
const update = () => {
  request.put('/admin/updateCurrent', data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      localStorage.setItem('xm-user', JSON.stringify(data.user))
      emit('updateUser')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleUpdate = (formData) => {
  data.user = { ...data.user, ...formData }
  update()
}
</script>

<style>
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