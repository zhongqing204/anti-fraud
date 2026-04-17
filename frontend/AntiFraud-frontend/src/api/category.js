import request from '@/utils/request'

/**
 * 分页查询分类列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.name - 分类名称（可选）
 */
export function getCategoryList(params) {
    return request({
        url: '/category/selectPage',
        method: 'get',
        params
    })
}

/**
 * 查询所有分类
 * @param {Object} params - 查询参数
 * @param {string} params.name - 分类名称（可选）
 */
export function getAllCategories(params) {
    return request({
        url: '/category/selectAll',
        method: 'get',
        params
    })
}

/**
 * 根据 ID 查询分类详情
 * @param {number} id - 分类 ID
 */
export function getCategoryDetail(id) {
    return request({
        url: `/category/selectById/${id}`,
        method: 'get'
    })
}

/**
 * 新增分类
 * @param {Object} data - 分类数据
 * @param {string} data.name - 分类名称
 */
export function addCategory(data) {
    return request({
        url: '/category/add',
        method: 'post',
        data
    })
}

/**
 * 修改分类
 * @param {Object} data - 分类数据
 * @param {number} data.id - 分类 ID
 * @param {string} data.name - 分类名称
 */
export function updateCategory(data) {
    return request({
        url: '/category/update',
        method: 'put',
        data
    })
}

/**
 * 删除单个分类
 * @param {number} id - 分类 ID
 */
export function deleteCategory(id) {
    return request({
        url: `/category/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 批量删除分类
 * @param {Array<number>} ids - 分类 ID 数组
 */
export function deleteCategoryBatch(ids) {
    return request({
        url: '/category/delete/batch',
        method: 'delete',
        data: ids
    })
}
