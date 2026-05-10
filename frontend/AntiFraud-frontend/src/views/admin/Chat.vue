<template>
  <div class="admin-chat-container">
    <div class="chat-layout">
      <!-- 左侧用户列表 -->
      <div class="user-list-panel">
        <div class="panel-header">
          <h3><el-icon><User /></el-icon> 咨询用户</h3>
          <el-badge :value="unreadTotal" type="primary" />
        </div>
        
        <div class="user-search">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索用户"
            clearable
            prefix-icon="Search"
          />
        </div>
        
        <div class="user-list">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            :class="['user-item', { active: selectedUserId === user.id }]"
            @click="selectUser(user)"
          >
            <el-avatar :size="40" :src="user.avatar">{{ user.name?.charAt(0) }}</el-avatar>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-last-message">{{ user.lastMessage || '暂无消息' }}</div>
            </div>
            <el-badge v-if="user.unreadCount > 0" :value="user.unreadCount" type="danger" />
          </div>
          
          <el-empty v-if="filteredUsers.length === 0" description="暂无用户" :image-size="80" />
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-panel">
        <div v-if="selectedUserId && !chatCollapsed" class="chat-content">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="chat-user-info">
              <el-avatar :size="35" :src="selectedUser?.avatar">{{ selectedUser?.name?.charAt(0) }}</el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ selectedUser?.name }}</div>
                <div class="user-status">
                  <span :class="['status-dot', selectedUser?.online ? 'online' : 'offline']"></span>
                  {{ selectedUser?.online ? '在线' : '离线' }}
                </div>
              </div>
              <el-tooltip content="清空与该用户的聊天记录" placement="bottom">
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
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="messages-container" ref="messagesContainer">
            <div v-for="(msg, index) in messages" :key="index" 
                 :class="['message-item', msg.isSelf ? 'self' : 'other']">
              <div class="message-avatar">
                <el-avatar 
                  :size="35" 
                  :src="msg.isSelf ? admin.avatar : msg.fromUserAvatar"
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
                <div :class="['message-bubble', (msg.messageType === 'image' || msg.messageType === 'video') ? 'media-message' : '']">
                  <img v-if="msg.messageType === 'image'" :src="getFileUrl(msg.content)" alt="图片" class="message-image" @click="previewImage(msg.content)" />
                  <video v-else-if="msg.messageType === 'video'" :src="getFileUrl(msg.content)" controls class="message-video"></video>
                  <span v-else>{{ msg.content }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="messages.length === 0" class="empty-tip">
              <el-empty description="暂无聊天记录" :image-size="100" />
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
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
              placeholder="输入回复内容...（按Enter发送，Ctrl+Enter换行）"
              @keydown="handleKeyDown"
            />
            <div class="input-actions">
              <el-button type="primary" @click="sendMessage" :disabled="!wsConnected || (!inputMessage.trim() && !selectedUserId)">
                <el-icon><Promotion /></el-icon> 发送
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="no-chat-selected">
          <el-empty description="请选择一个用户开始聊天" :image-size="150">
            <template #image>
              <el-icon :size="80" color="#dcdfe6"><ChatDotRound /></el-icon>
            </template>
          </el-empty>
        </div>
      </div>
    </div>
  </div>

  <!-- 图片预览 -->
  <el-image-viewer
    v-if="showImageViewer"
    :url-list="[currentImageUrl]"
    @close="showImageViewer = false"
    class="custom-image-viewer"
  />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onActivated, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ChatDotRound, Promotion, Search, Delete, Close, Picture, VideoCamera } from '@element-plus/icons-vue'
import { getWebSocketUrl, getChatHistory, markAsRead, getChatUsers, clearChatHistory } from '@/api/chat'
import request from '@/utils/request'

const admin = JSON.parse(localStorage.getItem('xm-admin') || '{}')
const route = useRoute()
const wsConnected = ref(false)
let ws = null

// 用户列表
const onlineUsers = ref([])
const searchKeyword = ref('')
const selectedUserId = ref(null)
const selectedUser = ref(null)
const messages = ref([])
const inputMessage = ref('')
const messagesContainer = ref(null)
const imageInput = ref(null)
const videoInput = ref(null)
const showImageViewer = ref(false)
const currentImageUrl = ref('')
const chatCollapsed = ref(false)

// 过滤用户列表
const filteredUsers = computed(() => {
  if (!searchKeyword.value) return onlineUsers.value
  return onlineUsers.value.filter(user => 
    user.name?.includes(searchKeyword.value)
  )
})

// 未读消息总数
const unreadTotal = computed(() => {
  return onlineUsers.value.reduce((sum, user) => sum + (user.unreadCount || 0), 0)
})

// 初始化WebSocket
const initWebSocket = () => {
  const wsUrl = getWebSocketUrl(admin.id)
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('管理员WebSocket连接成功')
    wsConnected.value = true
    ElMessage.success('已连接到聊天系统')
    loadOnlineUsers()
  }
  
  ws.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data)
      
      // 如果是新消息且不是自己发的
      if (!message.isSelf && message.fromUserId !== admin.id) {
        handleIncomingMessage(message)
      } else {
        // 自己发的消息，添加到当前聊天
        if (selectedUserId.value) {
          messages.value.push(message)
          scrollToBottom()
        }
      }
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

// 处理收到的消息
const handleIncomingMessage = (message) => {
  const fromUserId = message.fromUserId
  
  // 更新用户列表中的最后一条消息
  const userIndex = onlineUsers.value.findIndex(u => u.id === fromUserId)
  if (userIndex !== -1) {
    onlineUsers.value[userIndex].lastMessage = message.content
    onlineUsers.value[userIndex].unreadCount = (onlineUsers.value[userIndex].unreadCount || 0) + 1
    
    // 如果正在和该用户聊天，直接显示消息
    if (selectedUserId.value === fromUserId) {
      messages.value.push(message)
      scrollToBottom()
      // 标记为已读
      markAsRead({ userId: admin.id, fromUserId })
      onlineUsers.value[userIndex].unreadCount = 0
    }
  } else {
    // 新用户，添加到列表
    onlineUsers.value.unshift({
      id: fromUserId,
      name: message.fromUserName,
      lastMessage: message.content,
      unreadCount: 1,
      online: true
    })
  }
}

// 加载在线用户
const loadOnlineUsers = async () => {
  try {
    const res = await getChatUsers(admin.id)
    if (res.code === '200') {
      onlineUsers.value = res.data || []
      
      // 如果之前选中了用户，检查该用户是否还在列表中
      if (selectedUserId.value) {
        const userExists = onlineUsers.value.find(u => u.id === selectedUserId.value)
        if (!userExists) {
          // 用户不在列表中，清空选中状态
          selectedUserId.value = null
          selectedUser.value = null
          messages.value = []
        }
      }
    }
  } catch (error) {
    console.error('加载聊天用户列表失败', error)
  }
}

// 选择用户
const selectUser = async (user) => {
  // 如果点击的是当前已选中的用户，切换折叠状态
  if (selectedUserId.value === user.id) {
    chatCollapsed.value = !chatCollapsed.value
    return
  }
  
  // 选择新用户的时，展开聊天
  chatCollapsed.value = false
  selectedUserId.value = user.id
  selectedUser.value = user
  
  // 加载聊天记录
  await loadChatHistory(user.id)
  
  // 标记为已读
  if (user.unreadCount > 0) {
    await markAsRead({ userId: admin.id, fromUserId: user.id })
    user.unreadCount = 0
  }
  
  scrollToBottom()
}

// 加载聊天历史
const loadChatHistory = async (userId) => {
  try {
    const res = await getChatHistory({ userId: admin.id, otherUserId: userId })
    if (res.code === '200') {
      messages.value = res.data.map(msg => ({
        ...msg,
        isSelf: msg.fromUserId === admin.id
      }))
    }
  } catch (error) {
    console.error('加载聊天历史失败', error)
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || !wsConnected.value || !selectedUserId.value) return
  
  const message = {
    fromUserId: admin.id,
    fromUserName: admin.name || '管理员',
    fromUserAvatar: admin.avatar || '',
    toUserId: selectedUserId.value,
    toUserName: selectedUser.value?.name,
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
        fromUserId: admin.id,
        fromUserName: admin.name || '管理员',
        fromUserAvatar: admin.avatar || '',
        toUserId: selectedUserId.value,
        toUserName: selectedUser.value?.name,
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
  
  event.target.value = ''
}

// 处理视频上传
const handleVideoUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
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
        fromUserId: admin.id,
        fromUserName: admin.name || '管理员',
        fromUserAvatar: admin.avatar || '',
        toUserId: selectedUserId.value,
        toUserName: selectedUser.value?.name,
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
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 清空聊天记录
const clearHistory = async () => {
  if (!selectedUserId.value) return
  
  try {
    await ElMessageBox.confirm(`确定要清空与 ${selectedUser.value?.name} 的聊天记录吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await clearChatHistory({ userId: admin.id, otherUserId: selectedUserId.value })
    messages.value = []
    
    // 更新用户列表中的最后消息
    const userIndex = onlineUsers.value.findIndex(u => u.id === selectedUserId.value)
    if (userIndex !== -1) {
      onlineUsers.value[userIndex].lastMessage = '暂无消息'
    }
    
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
    
    // 前端直接删除
    messages.value.splice(index, 1)
    ElMessage.success('消息已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败', error)
    }
  }
}

onMounted(() => {
  // 先加载用户列表，再建立WebSocket连接
  loadOnlineUsers()
  initWebSocket()
})

// 当从其他页面返回时，刷新用户列表和聊天记录
onActivated(() => {
  loadOnlineUsers()
  // 如果之前选中了用户，重新加载聊天记录
  if (selectedUserId.value) {
    loadChatHistory(selectedUserId.value)
  }
})

// 监听路由变化，每次进入页面都刷新
watch(() => route.path, (newPath) => {
  if (newPath === '/manager/chat') {
    loadOnlineUsers()
    // 如果之前选中了用户，重新加载聊天记录
    if (selectedUserId.value) {
      loadChatHistory(selectedUserId.value)
    }
  }
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<style scoped>
.admin-chat-container {
  height: calc(100vh - 100px);
  padding: 20px;
}

.chat-layout {
  display: flex;
  height: 100%;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 左侧用户列表 */
.user-list-panel {
  width: 300px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-search {
  padding: 10px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.user-list {
  flex: 1;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  transition: background 0.3s;
  border-bottom: 1px solid #f5f5f5;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.user-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.user-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.user-last-message {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧聊天面板 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  flex: 1;
}

.user-name {
  font-weight: 500;
  font-size: 16px;
}

.user-status {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 6px;
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

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
  gap: 10px;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 60%;
}

.message-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
  font-size: 12px;
  color: #909399;
}

.message-item.self .message-info {
  flex-direction: row-reverse;
}

.message-name {
  font-weight: 500;
  color: #606266;
}

.delete-msg-icon {
  cursor: pointer;
  color: #f56c6c;
  transition: all 0.3s;
  opacity: 0;
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
  word-break: break-word;
  line-height: 1.5;
}

.message-item.other .message-bubble {
  background: white;
  border: 1px solid #e4e7ed;
}

.message-item.self .message-bubble {
  background: #409eff;
  color: white;
}

.media-message {
  padding: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
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
  border-radius: 8px;
}

.chat-input-area {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
  background: white;
}

.input-toolbar {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
}

.input-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.no-chat-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.empty-tip {
  padding: 40px 0;
}

.clear-btn {
  padding: 5px;
  transition: all 0.3s;
}

.clear-btn:hover {
  transform: scale(1.1);
}

/* 图片预览样式 */
.custom-image-viewer :deep(.el-image-viewer__canvas) img {
  max-width: 50vw !important;
  max-height: 50vh !important;
  object-fit: contain;
}
</style>
