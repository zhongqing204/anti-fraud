<template>
  <div style="margin: 20px auto; width: 60%">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.activityData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.activityData.createTime }}</span>
        <el-button type="primary" size="small" :disabled="data.activityData.status === '已结束'"  @click="submit" style="margin-left: 30px;">报名</el-button>
      </div>
      <div 
        class="vditor-reset" style="margin-top: 50px; line-height: 1.8;"
        v-html="renderedContent"
      ></div>
    </div>
  </div>

</template>

<script setup>
import {reactive,onMounted,computed} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {signUpAdd, getActivityDetail} from "@/api/activity.js";
import MarkdownIt from "markdown-it";

const md = new MarkdownIt()
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  activityId: router.currentRoute.value.query.id,
  activityData: {},
  content: null,
})

const renderedContent = computed(() => {
  if (!data.activityData.content) return ''
  return md.render(data.activityData.content)
})

const loadActivityDetail = () => {
  if (!data.activityId || data.activityId === 'undefined') {
    ElMessage.error('参数错误，无法加载内容')
    return
  }
  getActivityDetail(data.activityId).then(res => {
    if (res.code === '200') {
      data.activityData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const submit = () => {
  signUpAdd({
    userId: data.user.id,
    activityId: data.activityId,
    status: '待审核'
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('报名成功，等待管理员审核')
    } else {
      ElMessage.error(res.msg || '你已经报名，请等待审核')
    }
  }).catch(err => {
    console.error('报名失败:', err)
    ElMessage.error(err.response?.data?.msg || '报名失败，请稍后重试')
  })
}

onMounted(() => {
  loadActivityDetail()
})

</script>