<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #1450aa">欢 迎 登 录</div>
      <el-form ref="formRef" :model="data.form" :rules="data.rules">
        <el-form-item prop="account">
          <el-input :prefix-icon="User" size="large" v-model="data.form.account" placeholder="请输入账号"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input show-password :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select size="large" v-model="data.form.role" placeholder="请选择角色">
            <el-option value="ADMIN" label="管理员"></el-option>
            <el-option value="USER" label="用户"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="login" :loading="data.loading">登 录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="warning" style="width: 100%" @click="router.push('/register')">注 册</el-button>
        </el-form-item>
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

const data = reactive({
  loading: false,
  form: { role: '' },
  rules: {
    account: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    role: [
      { required: true, message:'请选择身份', trigger:'blur' }
    ]
  }
})

const formRef = ref()

const login = () => {
  formRef.value.validate(valid => {
    if (valid) { // 表示表单校验通过
      data.loading = true;
      request.post('/auth/login', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('登录成功')
          // 存储用户信息到浏览器的缓存
          const userData = {
            ...res.data.userInfo,
            token: res.data.token,
            password: data.form.password
          };
          localStorage.setItem('xm-user', JSON.stringify(userData))
          setTimeout(() => {
            const role = userData.role?.trim()?.toUpperCase();
            if(role === 'ADMIN'){
              location.href = '/manager/home'
            }else{
              location.href = '/front/home'
            }
          },500)
        } else {
          console.error('登录失败 - 返回码:', res.code, '消息:', res.message);
          ElMessage.error(res.message || '登录失败，请检查账号和密码')
        }
      }).catch(err => {
        console.error('登录请求异常:', err);
        console.error('错误详情:', err.response?.data);
        ElMessage.error(err.response?.data?.message || '网络错误，请稍后重试')
      }).finally(() => {
        data.loading = false;
      })
    } else {
      ElMessage.warning('请填写完整的登录信息')
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
  background-image: url('../assets/images/LoginBackground.png');
  background-size: 100% 100%;
  padding-left: 60%;
}
.login-box {
  width: 400px;
  padding: 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  background-color: rgb(173, 173, 173);
}
</style>