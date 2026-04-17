import request from '@/utils/request'

// 点赞/取消点赞
export function toggleLikes(data) {
    return request({
        url: '/likes/add',
        method: 'post',
        data
    })
}

// 查询所有点赞
export function getLikeLsist(params) {
    return request({
        url: '/likes/selectAll',
        method: 'get',
        params
    })
}

// 分页查询点赞
export function getLikesPage(params) {
    return request({
        url: '/likes/selectPage',
        method: 'get',
        params
    })
}

// 删除点赞
export function deleteLikes(id) {
    return request({
        url: `/likes/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除点赞
export function deleteLikeBatchs(ids) {
    return request({
        url: '/likes/delete/batch',
        method: 'delete',
        data: ids
    })
}
