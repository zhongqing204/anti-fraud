import request from '@/utils/request'

// 公告管理相关接口
export function getNoticeList(params) {
    return request({
        url: '/notice/selectPage',
        method: 'get',
        params
    })
}

export function getAllNotices(params) {
    return request({
        url: '/notice/selectAll',
        method: 'get',
        params
    })
}

export function getNoticeDetail(id) {
    return request({
        url: `/notice/selectById/${id}`,
        method: 'get'
    })
}

export function addNotice(data) {
    return request({
        url: '/notice/add',
        method: 'post',
        data
    })
}

export function updateNotice(data) {
    return request({
        url: '/notice/update',
        method: 'put',
        data
    })
}

export function deleteNotice(id) {
    return request({
        url: `/notice/delete/${id}`,
        method: 'delete'
    })
}

export function deleteNoticeBatch(ids) {
    return request({
        url: '/notice/delete/batch',
        method: 'delete',
        data: ids
    })
}