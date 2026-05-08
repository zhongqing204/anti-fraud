<template>
  <div style="background: #f5f7fa; min-height: 100vh; padding-bottom: 50px">
    <div style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); padding: 30px 0; margin-bottom: 30px">
      <div style="width: 60%; margin: 0 auto">
        <div style="color: white; font-size: 32px; font-weight: bold; margin-bottom: 10px">
          <el-icon style="vertical-align: middle; margin-right: 10px"><Bell /></el-icon>
          我的消息
        </div>
        <div style="color: rgba(255,255,255,0.9); font-size: 16px">查看系统通知和互动消息</div>
      </div>
    </div>

    <div style="width: 60%; margin: 0 auto">
      <!-- 【新增】消息分类标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" style="background: white; border-radius: 10px; padding: 20px; margin-bottom: 20px">
        <el-tab-pane label="全部消息" name="all">
          <template #label>
            <span>全部消息 <el-badge v-if="unreadCountMap.all > 0" :value="unreadCountMap.all" :max="99" style="margin-left: 5px" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="评论消息" name="comment">
          <template #label>
            <span style="display: flex; align-items: center"><ChatDotRound style="margin-right: 5px; font-size: 14px" />评论 <el-badge v-if="unreadCountMap.comment > 0" :value="unreadCountMap.comment" :max="99" style="margin-left: 5px" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="点赞消息" name="like">
          <template #label>
            <span style="display: flex; align-items: center"><Star style="margin-right: 5px; font-size: 14px" />点赞 <el-badge v-if="unreadCountMap.like > 0" :value="unreadCountMap.like" :max="99" style="margin-left: 5px" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="收藏消息" name="collect">
          <template #label>
            <span style="display: flex; align-items: center"><Star style="margin-right: 5px; font-size: 14px" />收藏 <el-badge v-if="unreadCountMap.collect > 0" :value="unreadCountMap.collect" :max="99" style="margin-left: 5px" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="举报消息" name="report">
          <template #label>
            <span style="display: flex; align-items: center"><Warning style="margin-right: 5px; font-size: 14px" />反诈举报 <el-badge v-if="unreadCountMap.report > 0" :value="unreadCountMap.report" :max="99" style="margin-left: 5px" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="帖子举报" name="article_report">
          <template #label>
            <span style="display: flex; align-items: center"><Warning style="margin-right: 5px; font-size: 14px" />帖子举报 <el-badge v-if="unreadCountMap.article_report > 0" :value="unreadCountMap.article_report" :max="99" style="margin-left: 5px" /></span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 【新增】清除消息按钮 -->
      <div v-if="data.tableData.length > 0" style="background: white; border-radius: 10px; padding: 15px 20px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center">
        <div style="color: #666; font-size: 14px">
          共 {{ data.total }} 条消息
          <span v-if="selectedIds.length > 0" style="margin-left: 10px; color: #409EFF">
            已选择 {{ selectedIds.length }} 条
          </span>
        </div>
        <div style="display: flex; gap: 10px">
          <el-button 
            v-if="selectedIds.length > 0" 
            type="danger" 
            plain 
            size="small"
            @click="clearSelectedMessages"
          >
            <el-icon style="margin-right: 5px"><Delete /></el-icon>
            删除选中
          </el-button>
          <el-popconfirm
            title="确定要清空所有消息吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="clearAllMessages"
          >
            <template #reference>
              <el-button type="danger" plain size="small">
                <el-icon style="margin-right: 5px"><Delete /></el-icon>
                清空全部
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <div v-for="item in data.tableData" :key="item.id" 
           class="message-card"
           :class="{ 'selected': selectedIds.includes(item.id) }"
           @click="handleMessageClick(item)">
        <!-- 【新增】选择框 -->
        <div class="message-checkbox" @click.stop>
          <el-checkbox 
            v-model="item.selected" 
            @change="handleSelectChange(item)"
          />
        </div>
        <div v-if="item.isRead === 0" class="unread-dot"></div>
        
        <div style="display: flex; align-items: flex-start; gap: 15px">
          <div class="message-icon">
            <el-icon size="24" color="white">
              <component :is="getMessageIcon(item.type)" />
            </el-icon>
          </div>
          
          <div style="flex: 1">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px">
              <div style="font-size: 16px; font-weight: bold; color: #333">{{ getMessageTitle(item) }}</div>
              <div style="color: #999; font-size: 12px; flex-shrink: 0">{{ formatTime(item.createdTime) }}</div>
            </div>
            
            <div style="color: #666; font-size: 14px; line-height: 1.8; margin-bottom: 10px; white-space: pre-wrap">{{ item.content }}</div>
            
            <div v-if="getActionText(item)" class="action-hint">
              <el-icon style="margin-right: 5px"><ArrowRight /></el-icon>
              {{ getActionText(item) }}
            </div>
          </div>
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
import {reactive, onBeforeUnmount, onMounted, inject, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {ChatDotRound, Star, Warning, Bell, ArrowRight, Delete} from '@element-plus/icons-vue'
import { getUnreadCountByType } from '@/api/message'

// 【新增】注入全局消息状态
const messageState = inject('messageState')

// 【新增】当前激活的标签页
const activeTab = ref('all')

// 【新增】已选择的消息ID列表
const selectedIds = ref([])

// 【新增】各类型未读数量映射
const unreadCountMap = reactive({
  all: 0,
  comment: 0,
  like: 0,
  collect: 0,
  report: 0,
  article_report: 0
})

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

// 【新增】获取操作提示文字
const getActionText = (item) => {
  const actionMap = {
    'like': item.articleId ? '点击查看帖子详情' : item.videoId ? '点击查看视频详情' : item.publicityId ? '点击查看宣传详情' : item.activityId ? '点击查看活动详情' : '',
    'collect': item.articleId ? '点击查看帖子详情' : item.videoId ? '点击查看视频详情' : item.publicityId ? '点击查看宣传详情' : item.activityId ? '点击查看活动详情' : '',
    'comment': item.publicityId ? '点击查看宣传详情' : item.videoId ? '点击查看视频详情' : item.activityId ? '点击查看活动详情' : item.articleId ? '点击查看帖子详情' : '',
    'report': '查看我的举报',
    'article_report': '查看我的帖子举报',
    'activity_signup': '点击查看活动详情'
  }
  return actionMap[item.type] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 【优化】点击消息卡片跳转到对应页面（增加内容存在性校验）
const handleMessageClick = async (item) => {
  // 根据消息类型跳转到不同页面
  if (item.type === 'like' || item.type === 'collect') {
    // 点赞和收藏消息：跳转到帖子详情页
    if (item.articleId) {
      // 【修复】先检查帖子是否存在
      const exists = await checkContentExists('article', item.articleId)
      if (exists) {
        router.push('/front/articleDetail?id=' + item.articleId)
      } else {
        ElMessage.warning('该帖子已被删除，无法查看')
      }
    } else if (item.videoId) {
      const exists = await checkContentExists('video', item.videoId)
      if (exists) {
        router.push('/front/videoDetail?id=' + item.videoId)
      } else {
        ElMessage.warning('该视频已被删除，无法查看')
      }
    } else if (item.publicityId) {
      const exists = await checkContentExists('publicity', item.publicityId)
      if (exists) {
        router.push('/front/publicityDetail?id=' + item.publicityId)
      } else {
        ElMessage.warning('该宣传文章已被删除，无法查看')
      }
    } else if (item.activityId) {
      const exists = await checkContentExists('activity', item.activityId)
      if (exists) {
        router.push('/front/activityDetail?id=' + item.activityId)
      } else {
        ElMessage.warning('该活动已被删除，无法查看')
      }
    }
  } else if (item.type === 'article_report') {
    // 帖子举报消息：跳转到我的帖子举报页面
    router.push('/front/myArticleReport')
  } else if (item.type === 'report') {
    // 举报消息：跳转到我的举报页面
    router.push('/front/myReport')
  } else if (item.type === 'comment') {
    // 评论消息：根据关联ID判断是哪种内容的评论
    if (item.publicityId) {
      const exists = await checkContentExists('publicity', item.publicityId)
      if (exists) {
        router.push('/front/publicityDetail?id=' + item.publicityId)
      } else {
        ElMessage.warning('该宣传文章已被删除，无法查看')
      }
    } else if (item.videoId) {
      const exists = await checkContentExists('video', item.videoId)
      if (exists) {
        router.push('/front/videoDetail?id=' + item.videoId)
      } else {
        ElMessage.warning('该视频已被删除，无法查看')
      }
    } else if (item.activityId) {
      const exists = await checkContentExists('activity', item.activityId)
      if (exists) {
        router.push('/front/activityDetail?id=' + item.activityId)
      } else {
        ElMessage.warning('该活动已被删除，无法查看')
      }
    } else if (item.articleId) {
      const exists = await checkContentExists('article', item.articleId)
      if (exists) {
        router.push('/front/articleDetail?id=' + item.articleId)
      } else {
        ElMessage.warning('该帖子已被删除，无法查看')
      }
    }
  } else if (item.type === 'activity_signup') {
    // 活动报名通知：跳转到活动详情页
    if (item.activityId) {
      const exists = await checkContentExists('activity', item.activityId)
      if (exists) {
        router.push('/front/activityDetail?id=' + item.activityId)
      } else {
        ElMessage.warning('该活动已被删除，无法查看')
      }
    }
  }
}

// 【新增】检查内容是否存在
const checkContentExists = async (type, id) => {
  try {
    let url = ''
    switch(type) {
      case 'article':
        url = '/article/selectById/' + id
        break
      case 'video':
        url = '/video/selectById/' + id
        break
      case 'publicity':
        url = '/publicity/selectById/' + id
        break
      case 'activity':
        url = '/activity/selectById/' + id
        break
      default:
        return false
    }
    
    const res = await request.get(url)
    return res.code === '200' && res.data !== null
  } catch (error) {
    console.error('检查内容存在性失败:', error)
    return false
  }
}

const load = () => {
  // 【优化】根据当前标签页决定查询方式
  if (activeTab.value === 'all') {
    // 查询全部消息
    request.get('/message/selectPage', {
      params: { pageNum: data.pageNum, pageSize: data.pageSize, userId: data.user.id }
    }).then(res => {
      if (res.code === '200') {
        data.tableData = res.data?.records || []
        data.total = res.data?.total || 0
        // 【新增】初始化选择状态
        data.tableData.forEach(item => {
          item.selected = false
        })
        selectedIds.value = []
        loadUnreadCount()
      } else {
        ElMessage.error(res.msg)
      }
    })
  } else {
    // 按类型查询消息
    request.get('/message/selectByType', {
      params: { 
        pageNum: data.pageNum, 
        pageSize: data.pageSize, 
        userId: data.user.id,
        type: activeTab.value
      }
    }).then(res => {
      if (res.code === '200') {
        data.tableData = res.data?.records || []
        data.total = res.data?.total || 0
        // 【新增】初始化选择状态
        data.tableData.forEach(item => {
          item.selected = false
        })
        selectedIds.value = []
        loadUnreadCount()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }
}

const loadUnreadCount = () => {
  request.get('/message/unreadCount', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      const count = res.data || 0
      data.unreadCount = count
      unreadCountMap.all = count
      // 【新增】同步更新全局消息状态，实现红点实时更新
      messageState.updateUnreadCount(count)
    }
  })
  
  // 【新增】加载各类型的未读数量
  loadUnreadCountByType()
}

// 【新增】加载各类型未读数量
const loadUnreadCountByType = () => {
  const types = ['comment', 'like', 'collect', 'report', 'article_report']
  types.forEach(type => {
    getUnreadCountByType(data.user.id, type).then(res => {
      if (res.code === '200') {
        unreadCountMap[type] = res.data || 0
      }
    })
  })
}

// 【新增】标签页切换事件
const handleTabChange = (tabName) => {
  data.pageNum = 1 // 重置页码
  load() // 重新加载数据
}

onMounted(() => {
  if (data.user.id) {
    load()
    loadUnreadCount()
    // 【新增】进入消息页面时，自动标记所有消息为已读
    markAllAsReadOnEnter()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})

// 【新增】进入页面时自动标记所有消息为已读
const markAllAsReadOnEnter = () => {
  request.post('/message/markAllAsRead', { userId: data.user.id }).then(res => {
    if (res.code === '200') {
      // 标记成功后，立即更新未读数为0
      data.unreadCount = 0
      messageState.updateUnreadCount(0)
      // 重新加载消息列表，显示已读状态
      setTimeout(() => {
        load()
      }, 300)
    }
  }).catch(err => {
    console.error('标记已读失败:', err)
  })
}

// 【新增】清空所有消息
const clearAllMessages = () => {
  // 获取当前用户的所有消息ID
  request.get('/message/selectAll', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      const messages = res.data || []
      if (messages.length === 0) {
        ElMessage.info('暂无消息可清空')
        return
      }
      
      const ids = messages.map(m => m.id)
      
      // 批量删除
      request.delete('/message/delete/batch', {
        data: ids
      }).then(delRes => {
        if (delRes.code === '200') {
          ElMessage.success('清空成功')
          // 重置页码并重新加载
          data.pageNum = 1
          load()
          loadUnreadCount()
        } else {
          ElMessage.error(delRes.msg || '清空失败')
        }
      }).catch(err => {
        console.error('清空消息失败:', err)
        ElMessage.error('清空失败，请稍后重试')
      })
    }
  }).catch(err => {
    console.error('获取消息列表失败:', err)
    ElMessage.error('操作失败，请稍后重试')
  })
}

// 【新增】处理选择变化
const handleSelectChange = (item) => {
  if (item.selected) {
    selectedIds.value.push(item.id)
  } else {
    const index = selectedIds.value.indexOf(item.id)
    if (index > -1) {
      selectedIds.value.splice(index, 1)
    }
  }
}

// 【新增】删除选中的消息
const clearSelectedMessages = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的消息')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 条消息吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    request.delete('/message/delete/batch', {
      data: selectedIds.value
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        // 重置页码并重新加载
        data.pageNum = 1
        load()
        loadUnreadCount()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    }).catch(err => {
      console.error('删除消息失败:', err)
      ElMessage.error('删除失败，请稍后重试')
    })
  }).catch(() => {
    // 用户取消操作
  })
}

const timer = setInterval(() => {
  loadUnreadCount()
}, 30000)

onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.message-card {
  background: white;
  border-radius: 10px;
  padding: 25px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.message-card.selected {
  background: #f0f9ff;
  border: 2px solid #409EFF;
}

.message-checkbox {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
}

.message-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  transform: translateY(-2px);
}

.unread-dot {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 10px;
  height: 10px;
  background: #F56C6C;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.message-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-hint {
  color: #409EFF;
  font-size: 13px;
  display: flex;
  align-items: center;
  margin-top: 8px;
}
</style>
