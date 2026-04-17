import request from '@/utils/request'

/**
 * 分页查询帖子列表
 */
export function getArticleList(params) {
    return request({
        url: '/article/selectPage',
        method: 'get',
        params
    })
}

/**
 * 查询所有帖子
 */
export function getAllArticle(params) {
    return request({
        url: '/article/selectAll',
        method: 'get',
        params
    })
}

/**
 * 根据 ID 查询帖子详情
 */
export function getArticleDetail(id) {
    return request({
        url: `/article/selectById/${id}`,
        method: 'get'
    })
}

/**
 * 新增帖子
 */
export function addArticle(data) {
    return request({
        url: '/article/add',
        method: 'post',
        data
    })
}

/**
 * 修改帖子
 */
export function updateArticle(data) {
    return request({
        url: '/article/update',
        method: 'put',
        data
    })
}

/**
 * 删除单个帖子
 */
export function deleteArticle(id) {
    return request({
        url: `/article/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 批量删除帖子
 */
export function deleteArticleBatch(ids) {
    return request({
        url: '/article/delete/batch',
        method: 'delete',
        data: ids
    })
}
