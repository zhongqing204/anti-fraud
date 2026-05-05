<template>
  <div style="background: #f5f7fa; min-height: 100vh; padding-bottom: 50px">
    <div style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); padding: 30px 0; margin-bottom: 30px">
      <div style="width: 60%; margin: 0 auto">
        <div style="color: white; font-size: 32px; font-weight: bold; margin-bottom: 10px"> 我的消息</div>
        <div style="color: rgba(255,255,255,0.9); font-size: 16px">查看系统通知和互动消息</div>
      </div>
    </div>

    <div style="width: 60%; margin: 0 auto">
      <div v-for="item in data.tableData" :key="item.id" style="margin-bottom: 15px; background: white; border-radius: 10px; padding: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: all 0.3s; position: relative" @mouseenter="$event.currentTarget.style.boxShadow='0 4px 16px rgba(0,0,0,0.12)'" @mouseleave="$event.currentTarget.style.boxShadow='0 2px 8px rgba(0,0,0,0.08)'">
        <div v-if="item.isRead === 0" style="position: absolute; top: 15px; right: 15px; width: 10px; height: 10px; background: #F56C6C; border-radius: 50%"></div>
        <div style="display: flex; align-items: flex-start; gap: 15px">
          <div style="width: 50px; height: 50px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0">
            <el-icon size="24" color="white">
              <component :is="getMessageIcon(item.type)" />
            </el-icon>
          </div>
          <div style="flex: 1">
            <div style="font-size: 16px; font-weight: bold; margin-bottom: 8px; color: #333">{{ getMessageTitle(item) }}</div>
            <div style="color: #666; font-size: 14px; line-height: 1.8; margin-bottom: 10px; white-space: pre-wrap">{{ item.content }}</div>
            <div style="color: #999; font-size: 12px">{{ formatTime(item.createdTime) }}</div>
          </div>
        </div>
        <div v-if="item.articleId" style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #f0f0f0; display: flex; gap: 10px">
          <el-button type="primary" size="small" @click="goToArticle(item.articleId)">查看帖子</el-button>
          <el-button v-if="item.type === 'article_report'" type="info" size="small" @click="goToMyArticleReport">查看我的举报</el-button>
        </div>
      </div>

      <div v-if="data.total" style="margin-top: 30px; background: white; padding: 20px; border-radius: 10px; text-align: center">
        <el-pagination @current-change="load" layout="total, prev, pager, next, jumper" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
      <div v-else style="text-align: center; padding: 80px; color: #999; background: white; border-radius: 10px">
        <el-icon :size="48" style="margin-bottom: 20px"><Bell /></el-icon>
        <div style="font-size: 18px">暂无消息</div>
        <div style="font-size: 14px; margin-top: 10px; color: #ccc">当您有互动或举报处理结果时，会在这里显示</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, onBeforeUnmount, onMounted} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {ChatDotRound, Star, Warning, Bell} from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  tableData: [],
  total: 0,
  unreadCount: 0
})

const getMessageIcon = (type) => {
  const iconMap = {
    'like': Star,
    'collect': Star,
    'comment': ChatDotRound,
    'report': Warning,
    'article_report': Warning,
    'activity_signup': Bell
  }
  return iconMap[type] || Bell
}

const getMessageTitle = (item) => {
  const titleMap = {
    'like': '收到点赞',
    'collect': '收到收藏',
    'comment': '评论消息',
    'report': '举报通知',
    'article_report': '帖子举报通知',
    'activity_signup': '活动报名通知'
  }
  return titleMap[item.type] || '系统通知'
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

const goToArticle = (articleId) => {
  if (articleId) router.push('/front/articleDetail?id=' + articleId)
}

const goToMyArticleReport = () => {
  router.push('/front/myArticleReport')
}

const load = () => {
  request.get('/message/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
      loadUnreadCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadUnreadCount = () => {
  request.get('/message/unreadCount', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      data.unreadCount = res.data || 0
    }
  })
}

const handleMarkAllRead = () => {
  request.post('/message/markAllAsRead', { userId: data.user.id }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已全部标记为已读')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  if (data.user.id) {
    load()
    loadUnreadCount()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})


const timer = setInterval(() => {
  loadUnreadCount()
}, 30000)

onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>
