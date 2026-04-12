<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #1450aa">欢 迎 注 册</div>
      <el-form ref="formRef" :model="data.form" :rules="data.rules">
        <el-form-item prop="account">
          <el-input :prefix-icon="User" size="large" v-model="data.form.account" placeholder="请输入账号"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input show-password :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input show-password :prefix-icon="Lock" size="large" v-model="data.form.confirmPassword" placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="register" :loading="data.loading">注 册</el-button>
        </el-form-item>
        <div style="text-align: right">
          已有账号？请 <a href="/login">登录</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else {
    if (value !== data.form.password) {
      callback(new Error("确认密码跟原密码不一致!"))
    }
    callback()
  }
}

const validateAccount = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入账号'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('账号长度必须在 3-20 个字符之间'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度必须在 6-20 个字符之间'))
  } else {
    callback()
  }
}

const data = reactive({
  loading: false,
  form: { },
  rules: {
    account: [
      { validator: validateAccount, trigger: 'blur' }
    ],
    password: [
      { validator: validatePassword, trigger: 'blur' }
    ],
    confirmPassword: [
        { validator: validatePass, trigger: 'blur' }
    ]
  }
})

const formRef = ref()

const register = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.loading = true;
      request.post('/auth/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('注册成功')
          setTimeout(()=> {
            router.push('/login')
          },1500)
        } else {
          ElMessage.error(res.msg || '注册失败，请稍后重试')
        }
      }).catch(err => {
        console.error('注册失败:', err)
      }).finally(() => {
        data.loading = false;
      })
    } else {
      ElMessage.warning('请填写完整信息')
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(to top, #00467f, #a5cc82);
}
.login-box {
  width: 350px;
  padding: 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.5);
}
</style>