import request from '@/utils/request'

// 获取聊天记录
export function getChatHistory(params) {
    return request({
        url: '/chat/history',
        method: 'get',
        params
    })
}

// 分页获取聊天记录
export function getChatHistoryPage(params) {
    return request({
        url: '/chat/history/page',
        method: 'get',
        params
    })
}

// 标记消息为已读
export function markAsRead(data) {
    return request({
        url: '/chat/markAsRead',
        method: 'post',
        params: data
    })
}

// 获取未读消息数
export function getChatUnreadCount(userId) {
    return request({
        url: '/chat/unreadCount',
        method: 'get',
        params: { userId }
    })
}

// 获取有聊天记录的用户列表（管理员使用）
export function getChatUsers(adminId) {
    return request({
        url: '/chat/chatUsers',
        method: 'get',
        params: { adminId }
    })
}

// 清空聊天记录
export function clearChatHistory(data) {
    return request({
        url: '/chat/clearHistory',
        method: 'delete',
        params: data
    })
}

// WebSocket连接地址
export function getWebSocketUrl(userId) {
    const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080'
    const wsProtocol = baseUrl.startsWith('https') ? 'wss' : 'ws'
    const wsBaseUrl = baseUrl.replace(/^https?:\/\//, '')
    return `${wsProtocol}://${wsBaseUrl}/chat/${userId}`
}
