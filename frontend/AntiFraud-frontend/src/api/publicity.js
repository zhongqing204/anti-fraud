import request from '@/utils/request'

/**
 * 分页查询反诈宣传列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.title - 宣传标题（可选）
 */
export function getPublicityList(params) {
    return request({
        url: '/publicity/selectPage',
        method: 'get',
        params
    })
}

/**
 * 查询所有反诈宣传
 * @param {Object} params - 查询参数
 * @param {string} params.title - 宣传标题（可选）
 */
export function getAllPublicity(params) {
    return request({
        url: '/publicity/selectAll',
        method: 'get',
        params
    })
}

/**
 * 根据 ID 查询宣传详情
 * @param {number} id - 宣传 ID
 */
export function getPublicityDetail(id) {
    return request({
        url: `/publicity/selectById/${id}`,
        method: 'get'
    })
}

/**
 * 新增反诈宣传
 * @param {Object} data - 宣传数据
 * @param {string} data.title - 宣传标题
 * @param {string} data.cover - 封面图片
 * @param {string} data.content - 宣传内容
 */
export function addPublicity(data) {
    return request({
        url: '/publicity/add',
        method: 'post',
        data
    })
}

/**
 * 修改反诈宣传
 * @param {Object} data - 宣传数据
 * @param {number} data.id - 宣传 ID
 * @param {string} data.title - 宣传标题
 * @param {string} data.cover - 封面图片
 * @param {string} data.content - 宣传内容
 */
export function updatePublicity(data) {
    return request({
        url: '/publicity/update',
        method: 'put',
        data
    })
}

/**
 * 删除单个宣传
 * @param {number} id - 宣传 ID
 */
export function deletePublicity(id) {
    return request({
        url: `/publicity/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 批量删除宣传
 * @param {Array<number>} ids - 宣传 ID 数组
 */
export function deletePublicityBatch(ids) {
    return request({
        url: '/publicity/delete/batch',
        method: 'delete',
        data: ids
    })
}
