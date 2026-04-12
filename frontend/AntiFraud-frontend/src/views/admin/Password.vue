<template>
  <div style="width: 40%; margin: 20px auto" class="card">
    <div style="font-size: 20px; padding: 20px">修改密码</div>
    <el-form ref="formRef" :rules="data.rules" :model="data.user" label-width="80px" style="padding: 20px">
      <el-form-item label="原密码" prop="password">
        <el-input v-model="data.user.password" placeholder="请输入原密码" show-password></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="data.user.newPassword" placeholder="请输入新密码" show-password></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="data.user.confirmPassword" placeholder="请确认新密码" show-password></el-input>
      </el-form-item>
      <div style="text-align: center">
        <el-button type="primary" plain @click="updatePassword">保 存</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const formRef = ref()

const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else {
    if (value !== data.user.newPassword) {
      callback(new Error("两次输入的密码不一致!"))
    }
    callback()
  }
}

const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度必须在 6-20 个字符之间'))
  } else if (value === data.user.password) {
    callback(new Error('新密码不能与原密码相同'))
  } else {
    callback()
  }
}

const userData = localStorage.getItem('xm-user')
let userObj = {}
if (userData) {
  try {
    userObj = JSON.parse(userData)
  } catch (e) {
    console.error('解析用户数据失败:', e)
  }
}

const data = reactive({
  user: {
    id: userObj.id || '',
    password: userObj.password || '',
    newPassword: '',
    confirmPassword: ''
  },
  rules: {
    password: [
      { required: true, message: '请输入原密码', trigger: 'blur' },
    ],
    newPassword: [
      { validator: validateNewPassword, trigger: 'blur' },
    ],
    confirmPassword: [
      { validator: validatePass, trigger: 'blur' }
    ]
  }
})

const updatePassword = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const updateData = {
        id: String(data.user.id),
        password: data.user.password,
        newPassword: data.user.newPassword
      }
      request.put('/admin/updatePassword', updateData).then(res => {
        if (res.code === '200') {
          ElMessage.success('密码修改成功')
          localStorage.setItem('xm-user', JSON.stringify({
            ...userObj,
            password: data.user.newPassword
          }))
          data.user.password = ''
          data.user.newPassword = ''
          data.user.confirmPassword = ''
        } else {
          ElMessage.error(res.msg || '密码修改失败')
        }
      }).catch(err => {
        console.error('修改密码失败:', err)
      }).finally(() => {
        data.loading = false;
      })
    }else{
      ElMessage.warning('请填写完整的密码信息')
    }
  })
}
</script>