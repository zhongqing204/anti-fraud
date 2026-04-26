import request from '@/utils/request'

// 点赞/取消点赞
export function toggleLikes(data) {
    return request({
        url: '/like/add',
        method: 'post',
        data
    })
}

// 查询所有点赞
export function getLikeLsist(params) {
    return request({
        url: '/like/selectAll',
        method: 'get',
        params
    })
}

// 分页查询点赞
export function getLikesPage(params) {
    return request({
        url: '/like/selectPage',
        method: 'get',
        params
    })
}

// 删除点赞
export function deleteLikes(id) {
    return request({
        url: `/like/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除点赞
export function deleteLikeBatchs(ids) {
    return request({
        url: '/like/delete/batch',
        method: 'delete',
        data: ids
    })
}
