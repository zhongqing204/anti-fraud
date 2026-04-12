<template>
  <PersonForm
    :userData="data.user"
    container-style="width: 40%; margin: 5px auto"
    label-width="60px"
    @submit="handleUpdate"
  />
</template>

<script setup>
import { reactive,onMounted } from "vue";
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
    request.get('/user/current').then(res => {
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

const handleUpdate = (formData) => {
  // 只更新允许的字段，避免覆盖 account 等关键字段
  const allowedFields = ['name', 'phone', 'email', 'avatar']
  const updateData = {}
  
  allowedFields.forEach(field => {
    if (formData[field] !== undefined) {
      updateData[field] = formData[field]
    }
  })
  
  data.user = { ...data.user, ...updateData }
  update()
}

const emit = defineEmits(['updateUser'])
const update = () => {
  request.put('/user/updateCurrent', data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      localStorage.setItem('xm-user', JSON.stringify(data.user))
      emit('updateUser')
    } else {
      ElMessage.error(res.msg)
    }
  })
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
  border-radius: 50%;
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