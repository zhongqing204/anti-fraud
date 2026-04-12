|NEW_FILE_CODE
<template>
  <div :style="containerStyle" class="card">
    <el-form ref="formRef" :model="formData" label-width="labelWidth" style="padding: 20px">
      <div v-if="showAvatar" :style="avatarContainerStyle">
        <AvatarUpload v-model="formData.avatar" :showFileList="false" />
      </div>
      
      <el-form-item prop="account" label="账号">
        <el-input 
          :disabled="disableUsername" 
          v-model="formData.account" 
          placeholder="请输入账号"
        />
      </el-form-item>
      
      <el-form-item prop="name" label="姓名">
        <el-input v-model="formData.name" placeholder="请输入姓名" />
      </el-form-item>
      
      <el-form-item prop="phone" label="电话">
        <el-input v-model="formData.phone" placeholder="请输入电话" />
      </el-form-item>
      
      <el-form-item prop="email" label="邮箱">
        <el-input v-model="formData.email" placeholder="请输入邮箱" />
      </el-form-item>
      
      <div style="text-align: center">
        <el-button type="primary" plain @click="handleSubmit">保 存</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, watch } from "vue";
import AvatarUpload from "../components/AvatarUpload.vue";

const props = defineProps({
  userData: {
    type: Object,
    default: () => ({})
  },
  containerStyle: {
    type: String,
    default: 'width: 50%; margin: 5px auto'
  },
  labelWidth: {
    type: String,
    default: '70px'
  },
  showAvatar: {
    type: Boolean,
    default: true
  },
  avatarContainerStyle: {
    type: String,
    default: 'text-align: center; margin-bottom: 20px'
  },
  disableUsername: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['submit'])

const formData = reactive({ 
  account: '',
  name: '',
  phone: '',
  email: '',
  avatar: ''
})

watch(() => props.userData, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    formData.account = newVal.account || ''
    formData.name = newVal.name || ''
    formData.phone = newVal.phone || ''
    formData.email = newVal.email || ''
    formData.avatar = newVal.avatar || ''
  }
}, { immediate: true, deep: true })

const handleSubmit = () => {
  emit('submit', formData)
}
</script>
