import request from '@/utils/request'

// 查询所有消息
export function getMessageList(params) {
    return request({
        url: '/message/selectAll',
        method: 'get',
        params
    })
}

// 分页查询消息
export function getMessagePage(params) {
    return request({
        url: '/message/selectPage',
        method: 'get',
        params
    })
}

// 查询未读消息数量
export function getUnreadCount(userId) {
    return request({
        url: '/message/unreadCount',
        method: 'get',
        params: { userId }
    })
}

// 标记消息为已读
export function markAsRead(ids) {
    return request({
        url: '/message/markAsRead',
        method: 'post',
        data: { ids }
    })
}

// 标记所有消息为已读
export function markAllAsRead(userId) {
    return request({
        url: '/message/markAllAsRead',
        method: 'post',
        data: { userId }
    })
}

// 删除单个消息
export function deleteMessage(id) {
    return request({
        url: `/message/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除消息
export function deleteMessageBatch(ids) {
    return request({
        url: '/message/delete/batch',
        method: 'delete',
        data: ids
    })
}

// 按类型分页查询消息
export function getMessageByType(params) {
    return request({
        url: '/message/selectByType',
        method: 'get',
        params
    })
}

// 查询某类型的未读消息数量
export function getUnreadCountByType(userId, type) {
    return request({
        url: '/message/unreadCountByType',
        method: 'get',
        params: { userId, type }
    })
}
