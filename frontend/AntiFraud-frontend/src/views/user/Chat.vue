<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2><el-icon><ChatDotRound /></el-icon> 在线咨询</h2>
      <div class="header-actions">
        <el-tooltip content="清空所有聊天记录" placement="bottom">
          <el-button 
            size="small" 
            text
            type="danger"
            @click="clearHistory"
            class="clear-btn"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </el-tooltip>
        <div class="online-status">
          <span :class="['status-dot', wsConnected ? 'online' : 'offline']"></span>
          {{ wsConnected ? '在线' : '离线' }}
        </div>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" 
           :class="['message-item', msg.isSelf ? 'self' : 'other']">
        <div class="message-avatar">
          <el-avatar 
            :size="40" 
            :src="msg.isSelf ? user.avatar : msg.fromUserAvatar"
          >
            {{ msg.fromUserName?.charAt(0) }}
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-info">
            <span class="message-name">{{ msg.fromUserName }}</span>
            <span class="message-time">{{ formatTime(msg.sendTime) }}</span>
            <el-tooltip v-if="msg.isSelf" content="删除此消息" placement="top">
              <el-icon 
                class="delete-msg-icon" 
                @click="deleteMessage(msg, index)"
              >
                <Close />
              </el-icon>
            </el-tooltip>
          </div>
          <div :class="['message-bubble', msg.messageType === 'image' || msg.messageType === 'video' ? 'media-message' : getMessageClass(msg.messageType)]">
            <img v-if="msg.messageType === 'image'" :src="getFileUrl(msg.content)" alt="图片" class="message-image" @click="previewImage(msg.content)" />
            <video v-else-if="msg.messageType === 'video'" :src="getFileUrl(msg.content)" controls class="message-video"></video>
            <span v-else>{{ msg.content }}</span>
          </div>
        </div>
      </div>
      
      <div v-if="messages.length === 0" class="empty-tip">
        <el-empty description="暂无聊天记录" />
      </div>
    </div>

    <div class="chat-input">
      <div class="input-toolbar">
        <el-tooltip content="发送图片" placement="top">
          <el-button size="small" @click="triggerImageUpload">
            <el-icon><Picture /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="发送视频" placement="top">
          <el-button size="small" @click="triggerVideoUpload">
            <el-icon><VideoCamera /></el-icon>
          </el-button>
        </el-tooltip>
        <input 
          ref="imageInput" 
          type="file" 
          accept="image/*" 
          style="display: none" 
          @change="handleImageUpload"
        />
        <input 
          ref="videoInput" 
          type="file" 
          accept="video/*" 
          style="display: none" 
          @change="handleVideoUpload"
        />
      </div>
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="输入消息...（按Enter发送，Ctrl+Enter换行）"
        @keydown="handleKeyDown"
      />
      <div class="input-actions">
        <el-button type="primary" @click="sendMessage" :disabled="!wsConnected || !inputMessage.trim()">
          <el-icon><Promotion /></el-icon> 发送
        </el-button>
      </div>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="[currentImageUrl]"
      @close="showImageViewer = false"
      class="custom-image-viewer"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, onActivated, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Promotion, Delete, Close, Picture, VideoCamera } from '@element-plus/icons-vue'
import { getWebSocketUrl, getChatHistory, clearChatHistory } from '@/api/chat'
import request from '@/utils/request'
import { ElImageViewer } from 'element-plus'

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
const route = useRoute()
const messages = ref([])
const inputMessage = ref('')
const wsConnected = ref(false)
let ws = null
const messagesContainer = ref(null)
const imageInput = ref(null)
const videoInput = ref(null)
const showImageViewer = ref(false)
const currentImageUrl = ref('')

// 初始化WebSocket连接
const initWebSocket = () => {
  const wsUrl = getWebSocketUrl(user.id)
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('WebSocket连接成功')
    wsConnected.value = true
    ElMessage.success('已连接到在线客服')
    loadChatHistory()
  }
  
  ws.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data)
      messages.value.push(message)
      scrollToBottom()
    } catch (error) {
      console.error('解析消息失败', error)
    }
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket错误', error)
    ElMessage.error('连接出错，请刷新页面重试')
  }
  
  ws.onclose = () => {
    console.log('WebSocket连接关闭')
    wsConnected.value = false
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || !wsConnected.value) return
  
  const message = {
    fromUserId: user.id,
    fromUserName: user.name,
    fromUserAvatar: user.avatar || '',
    toUserId: 0, // 0表示发送给所有在线管理员（广播）
    toUserName: '管理员',
    content: inputMessage.value.trim(),
    messageType: 'text'
  }
  
  ws.send(JSON.stringify(message))
  inputMessage.value = ''
}

// 处理键盘事件
const handleKeyDown = (e) => {
  // Enter键发送消息
  if (e.key === 'Enter' && !e.ctrlKey && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
  // Ctrl+Enter 或 Shift+Enter 换行（默认行为，不需要阻止）
}

// 触发图片上传
const triggerImageUpload = () => {
  imageInput.value.click()
}

// 触发视频上传
const triggerVideoUpload = () => {
  videoInput.value.click()
}

// 处理图片上传
const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (res.code === '200') {
      const imageUrl = res.data
      
      // 直接发送图片消息，不需要输入框内容
      const message = {
        fromUserId: user.id,
        fromUserName: user.name,
        fromUserAvatar: user.avatar || '',
        toUserId: 0,
        toUserName: '管理员',
        content: imageUrl,
        messageType: 'image'
      }
      
      ws.send(JSON.stringify(message))
      ElMessage.success('图片发送成功')
    }
  } catch (error) {
    console.error('图片上传失败', error)
    ElMessage.error('图片上传失败')
  }
  
  // 清空input，允许重复选择同一文件
  event.target.value = ''
}

// 处理视频上传
const handleVideoUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件大小（800MB）
  if (file.size > 800 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过800MB')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (res.code === '200') {
      const videoUrl = res.data
      
      // 直接发送视频消息，不需要输入框内容
      const message = {
        fromUserId: user.id,
        fromUserName: user.name,
        fromUserAvatar: user.avatar || '',
        toUserId: 0,
        toUserName: '管理员',
        content: videoUrl,
        messageType: 'video'
      }
      
      ws.send(JSON.stringify(message))
      ElMessage.success('视频发送成功')
    }
  } catch (error) {
    console.error('视频上传失败', error)
    ElMessage.error('视频上传失败')
  }
  
  // 清空input
  event.target.value = ''
}

// 获取文件完整URL
const getFileUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080'
  return baseUrl + path
}

// 预览图片
const previewImage = (imageUrl) => {
  currentImageUrl.value = getFileUrl(imageUrl)
  showImageViewer.value = true
}

// 清空聊天记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有聊天记录吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await clearChatHistory({ userId: user.id, otherUserId: 0 })
    messages.value = []
    ElMessage.success('聊天记录已清空')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空聊天记录失败', error)
      ElMessage.error('清空失败')
    }
  }
}

// 删除单条消息
const deleteMessage = async (msg, index) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 前端直接删除（后端也需要实现单条删除接口）
    messages.value.splice(index, 1)
    ElMessage.success('消息已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败', error)
    }
  }
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const res = await getChatHistory({ userId: user.id, otherUserId: 0 })
    if (res.code === '200') {
      messages.value = res.data.map(msg => ({
        ...msg,
        isSelf: msg.fromUserId === user.id
      }))
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载聊天历史失败', error)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取消息样式类
const getMessageClass = (type) => {
  switch (type) {
    case 'system':
      return 'system-message'
    case 'image':
      return 'media-message'
    case 'video':
      return 'media-message'
    default:
      return ''
  }
}

onMounted(() => {
  initWebSocket()
})

// 当从其他页面返回时，重新加载聊天记录
onActivated(() => {
  loadChatHistory()
})

// 监听路由变化，每次进入页面都刷新
watch(() => route.path, (newPath) => {
  if (newPath === '/front/chat') {
    loadChatHistory()
  }
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  padding: 15px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.clear-btn {
  padding: 5px;
  transition: all 0.3s;
}

.clear-btn:hover {
  transform: scale(1.1);
}

.online-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background: #67c23a;
}

.status-dot.offline {
  background: #909399;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 10px;
}

.message-content {
  max-width: 60%;
}

.message-item.self .message-content {
  text-align: right;
}

.message-info {
  margin-bottom: 5px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-name {
  margin-right: 0;
}

.delete-msg-icon {
  cursor: pointer;
  font-size: 14px;
  color: #f56c6c;
  opacity: 0;
  transition: all 0.3s;
}

.message-item.self .message-info:hover .delete-msg-icon {
  opacity: 1;
}

.delete-msg-icon:hover {
  transform: scale(1.2);
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 8px;
  background: white;
  word-wrap: break-word;
  line-height: 1.6;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.message-item.self .message-bubble {
  background: #409eff;
  color: white;
}

/* 图片和视频消息不显示背景框 */
.media-message {
  padding: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
}

.media-message img,
.media-message video {
  display: block;
  border-radius: 8px;
}

.system-message {
  background: #fef0f0 !important;
  color: #f56c6c !important;
  text-align: center !important;
  font-size: 13px;
}

.empty-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.chat-input {
  padding: 15px 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.message-image {
  max-width: 300px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.message-image:hover {
  transform: scale(1.05);
}

.message-video {
  max-width: 400px;
  max-height: 300px;
  border-radius: 8px;
}

.input-actions {
  margin-top: 10px;
  text-align: right;
}

/* 图片预览样式 */
.custom-image-viewer :deep(.el-image-viewer__canvas) img {
  max-width: 50vw !important;
  max-height: 50vh !important;
  object-fit: contain;
}
</style>
