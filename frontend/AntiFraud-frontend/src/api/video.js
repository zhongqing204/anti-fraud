import request from '@/utils/request'

// 新增视频
export function addVideo(data) {
    return request({
        url: '/video/add',
        method: 'post',
        data
    })
}

// 修改视频
export function updateVideo(data) {
    return request({
        url: '/video/update',
        method: 'put',
        data
    })
}

// 删除单个视频
export function deleteVideo(id) {
    return request({
        url: `/video/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除视频
export function deleteVideoBatch(ids) {
    return request({
        url: '/video/delete/batch',
        method: 'delete',
        data: ids
    })
}

// 查询所有视频
export function getVideoList(params) {
    return request({
        url: '/video/selectAll',
        method: 'get',
        params
    })
}

// 分页查询视频
export function getVideoPage(params) {
    return request({
        url: '/video/selectPage',
        method: 'get',
        params
    })
}

// 根据ID查询视频
export function getVideoById(id) {
    return request({
        url: `/video/selectById/${id}`,
        method: 'get'
    })
}

// 查询热门视频（Top4）
export function getTopVideos() {
    return request({
        url: '/video/selectTop4',
        method: 'get'
    })
}
