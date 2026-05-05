<template>
  <div style="background: #f5f7fa; min-height: 100vh; padding-bottom: 50px">
    <div style="background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%); padding: 30px 0; margin-bottom: 30px">
      <div style="width: 60%; margin: 0 auto">
        <div style="color: white; font-size: 32px; font-weight: bold; margin-bottom: 10px"> 
          <el-icon style="vertical-align: middle; margin-right: 10px"><Warning /></el-icon>
          我的帖子举报
        </div>
        <div style="color: rgba(255,255,255,0.9); font-size: 16px">查看您举报的论坛帖子处理进度</div>
      </div>
    </div>

    <div style="width: 60%; margin: 0 auto">
      <div v-for="item in data.tableData" :key="item.id" style="margin-bottom: 15px; background: white; border-radius: 10px; padding: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: all 0.3s" @mouseenter="$event.currentTarget.style.boxShadow='0 4px 16px rgba(0,0,0,0.12)'" @mouseleave="$event.currentTarget.style.boxShadow='0 2px 8px rgba(0,0,0,0.08)'">
        <div style="display: flex; align-items: flex-start; gap: 15px">
          <div style="width: 50px; height: 50px; background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0">
            <el-icon size="24" color="white">
              <Warning />
            </el-icon>
          </div>
          <div style="flex: 1">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
              <div style="font-size: 16px; font-weight: bold; color: #333">{{ item.articleTitle || '未知帖子' }}</div>
              <el-tag v-if="item.status === '已处理'" type="success" size="large">{{ item.status }}</el-tag>
              <el-tag v-else-if="item.status === '处理中'" type="warning" size="large">{{ item.status }}</el-tag>
              <el-tag v-else type="danger" size="large">{{ item.status }}</el-tag>
            </div>
            <div style="margin-bottom: 8px">
              <span style="color: #999; font-size: 13px">举报类型：</span>
              <el-tag size="small" type="info">{{ item.reportType }}</el-tag>
            </div>
            <div style="color: #666; font-size: 14px; line-height: 1.8; margin-bottom: 10px">
              <span style="color: #999">详细原因：</span>{{ item.detailReason }}
            </div>
            <div style="color: #999; font-size: 12px; margin-bottom: 10px">
              举报时间：{{ formatTime(item.time) }}
            </div>
            <div v-if="item.reason" style="background: #f4f4f5; padding: 10px; border-radius: 5px; margin-bottom: 10px">
              <div style="color: #606266; font-size: 13px">
                <span style="color: #909399">处理说明：</span>{{ item.reason }}
              </div>
            </div>
          </div>
        </div>
        <div style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #f0f0f0; display: flex; gap: 10px">
          <el-button type="primary" size="small" @click="goToArticle(item.articleId)" v-if="item.articleId">查看帖子</el-button>
          <el-button type="info" size="small" @click="viewDetail(item)">查看详情</el-button>
          <el-button type="danger" size="small" @click="del(item.id)">删除</el-button>
        </div>
      </div>

      <div v-if="data.total" style="margin-top: 30px; background: white; padding: 20px; border-radius: 10px; text-align: center">
        <el-pagination @current-change="load" layout="total, prev, pager, next, jumper" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
      <div v-else style="text-align: center; padding: 80px; color: #999; background: white; border-radius: 10px">
        <el-icon :size="48" style="margin-bottom: 20px"><CircleCheck /></el-icon>
        <div style="font-size: 18px">暂无举报记录</div>
        <div style="font-size: 14px; margin-top: 10px; color: #ccc">您举报的帖子处理结果会在这里显示</div>
      </div>
    </div>

    <el-dialog title="举报详情" v-model="data.detailVisible" width="600px" destroy-on-close>
      <div style="padding: 20px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="举报帖子">
            {{ data.currentReport.articleTitle || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="举报类型">
            <el-tag type="info">{{ data.currentReport.reportType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="详细原因">
            {{ data.currentReport.detailReason }}
          </el-descriptions-item>
          <el-descriptions-item label="举报时间">
            {{ formatTime(data.currentReport.time) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag v-if="data.currentReport.status === '已处理'" type="success">{{ data.currentReport.status }}</el-tag>
            <el-tag v-else-if="data.currentReport.status === '处理中'" type="warning">{{ data.currentReport.status }}</el-tag>
            <el-tag v-else type="danger">{{ data.currentReport.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理说明" v-if="data.currentReport.reason">
            {{ data.currentReport.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="附件" v-if="data.currentReport.files">
            <div v-for="(file, index) in data.currentReport.files.split(',')" :key="index" style="margin-bottom: 5px">
              <el-button link type="primary" @click="downloadFile(file)">
                <el-icon><Link /></el-icon>
                查看附件 {{ index + 1 }}
              </el-button>
            </div>
          </el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; text-align: center">
          <el-button type="primary" @click="data.detailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, onMounted} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Warning, CircleCheck, Link} from '@element-plus/icons-vue'

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  tableData: [],
  total: 0,
  detailVisible: false,
  currentReport: {}
})

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

const goToArticle = (articleId) => {
  if (articleId) {
    router.push('/front/articleDetail?id=' + articleId)
  } else {
    ElMessage.warning('帖子不存在或已被删除')
  }
}

const viewDetail = (row) => {
  data.currentReport = JSON.parse(JSON.stringify(row))
  data.detailVisible = true
}

const downloadFile = (fileUrl) => {
  if (fileUrl) {
    window.open(baseUrl + fileUrl)
  }
}

const load = () => {
  console.log('当前用户ID:', data.user.id)
  request.get('/articleReport/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id
    }
  }).then(res => {
    console.log('接口返回数据:', res)
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
      console.log('设置后的total:', data.total)
    } else {
      ElMessage.error(res.msg)
    }
  })
}


const del = (id) => {
  ElMessageBox.confirm('确定要删除这条举报记录吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/articleReport/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

onMounted(() => {
  if (data.user.id) {
    load()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})
</script>
