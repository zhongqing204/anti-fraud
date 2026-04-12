<template>
  <div style="margin: 20px auto; width: 60%">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.newsData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.newsData.createTime }}</span>
      </div>
      <div 
        class="vditor-reset"
        style="margin-top: 50px; line-height: 1.8;"
        v-html="renderedContent"
      ></div>
    </div>
  </div>

</template>

<script setup>
import {reactive,computed} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt()

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  newsId: router.currentRoute.value.query.id,
  newsData: {},
  content: null,
})

const renderedContent = computed(() => {
  if (!data.newsData.content) return ''
  return md.render(data.newsData.content)
})

const loadActivity = () => {
  if (!data.newsId || data.newsId === 'undefined') {
    ElMessage.error('参数错误，无法加载内容')
    return
  }
  request.get('/publicity/selectById/' + data.newsId).then(res => {
    if (res.code === '200') {
      data.newsData = res.data
      request.put('/publicity/update', data.newsData)
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadActivity()

</script>
<style scoped>
/* Vditor 内容重置样式，确保富文本格式正确显示 */
.vditor-reset {
  /* 段落样式 */
  p {
    margin: 1em 0;
    line-height: 1.8;
  }
  
  /* 换行样式 */
  br {
    display: block;
    margin: 0.5em 0;
  }
  
  /* 列表样式 */
  ul, ol {
    padding-left: 20px;
    margin: 1em 0;
  }
  
  li {
    margin: 0.5em 0;
  }
  
  /* 引用块样式 */
  blockquote {
    margin: 1em 0;
    padding: 10px 20px;
    border-left: 4px solid #ccc;
    background-color: #f8f8f8;
  }
  
  /* 代码块样式 */
  pre {
    margin: 1em 0;
    padding: 10px;
    background-color: #f6f8fa;
    border-radius: 3px;
    overflow-x: auto;
  }
  
  /* 表格样式 */
  table {
    border-collapse: collapse;
    margin: 1em 0;
    width: 100%;
  }
  
  th, td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
  }
  
  th {
    background-color: #f6f8fa;
    font-weight: bold;
  }
}
</style>
