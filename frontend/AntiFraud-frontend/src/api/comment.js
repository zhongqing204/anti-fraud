import request from '@/utils/request'

// 新增评论
export function addComment(data) {
    return request({
        url: '/comment/add',
        method: 'post',
        data
    })
}

// 修改评论
export function updateComment(data) {
    return request({
        url: '/comment/update',
        method: 'put',
        data
    })
}

// 删除单个评论
export function deleteComment(id) {
    return request({
        url: `/comment/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除评论
export function deleteCommentBatch(ids) {
    return request({
        url: '/comment/delete/batch',
        method: 'delete',
        data: ids
    })
}

// 查询所有评论
export function getCommentList(params) {
    return request({
        url: '/comment/selectAll',
        method: 'get',
        params
    })
}

// 分页查询评论
export function getCommentPage(params) {
    return request({
        url: '/comment/selectPage',
        method: 'get',
        params
    })
}
