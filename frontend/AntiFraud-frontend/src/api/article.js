import request from '@/utils/request'

/**
 * 分页查询帖子列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.title - 帖子标题（可选）
 * @param {number} params.userId - 用户ID（可选）
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
 * @param {Object} params - 查询参数
 * @param {string} params.title - 帖子标题（可选）
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
 * @param {number} id - 帖子 ID
 */
export function getArticleDetail(id) {
    return request({
        url: `/article/selectById/${id}`,
        method: 'get'
    })
}

/**
 * 新增帖子
 * @param {Object} data - 帖子数据
 * @param {string} data.title - 帖子标题
 * @param {string} data.cover - 封面图片
 * @param {string} data.content - 帖子内容
 * @param {number} data.categoryId - 分类ID
 * @param {number} data.userId - 用户ID
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
 * @param {Object} data - 帖子数据
 * @param {number} data.id - 帖子 ID
 * @param {string} data.title - 帖子标题
 * @param {string} data.cover - 封面图片
 * @param {string} data.content - 帖子内容
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
 * @param {number} id - 帖子 ID
 */
export function deleteArticle(id) {
    return request({
        url: `/article/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 批量删除帖子
 * @param {Array<number>} ids - 帖子 ID 数组
 */
export function deleteArticleBatch(ids) {
    return request({
        url: '/article/delete/batch',
        method: 'delete',
        data: ids
    })
}

