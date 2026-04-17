import request from '@/utils/request'

// 收藏/取消收藏
export function toggleCollect(data) {
    return request({
        url: '/collect/add',
        method: 'post',
        data
    })
}

// 查询所有收藏
export function getCollectList(params) {
    return request({
        url: '/collect/selectAll',
        method: 'get',
        params
    })
}

// 分页查询收藏
export function getCollectPage(params) {
    return request({
        url: '/collect/selectPage',
        method: 'get',
        params
    })
}

// 删除收藏
export function deleteCollect(id) {
    return request({
        url: `/collect/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除收藏
export function deleteCollectBatch(ids) {
    return request({
        url: '/collect/delete/batch',
        method: 'delete',
        data: ids
    })
}
