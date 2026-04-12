import request from '@/utils/request'

/**
 * 分页查询反诈宣传列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.title - 宣传标题（可选）
 */
export function getActivityList(params) {
    return request({
        url: '/activity/selectPage',
        method: 'get',
        params
    })
}

/**
 * 查询所有反诈宣传
 * @param {Object} params - 查询参数
 * @param {string} params.title - 宣传标题（可选）
 */
export function getAllActivity(params) {
    return request({
        url: '/activity/selectAll',
        method: 'get',
        params
    })
}

/**
 * 根据 ID 查询宣传详情
 * @param {number} id - 宣传 ID
 */
export function getActivityDetail(id) {
    return request({
        url: `/activity/selectById/${id}`,
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
export function addActivity(data) {
    return request({
        url: '/activity/add',
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
export function updateActivity(data) {
    return request({
        url: '/activity/update',
        method: 'put',
        data
    })
}

/**
 * 删除单个宣传
 * @param {number} id - 宣传 ID
 */
export function deleteActivity(id) {
    return request({
        url: `/activity/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 批量删除宣传
 * @param {Array<number>} ids - 宣传 ID 数组
 */
export function deleteActivityBatch(ids) {
    return request({
        url: '/activity/delete/batch',
        method: 'delete',
        data: ids
    })
}

export function signUpAdd(data) {
  return request({
    url: '/activitySignUp/add',
    method: 'post',
    data
  })
}

export function signUpSelectPage(params) {
  return request({
    url: '/activitySignUp/selectPage',
    method: 'get',
    params
  })
}

export function signUpDelete(id) {
  return request({
    url: `/activitySignUp/delete/${id}`,
    method: 'delete'
  })
}

export function signUpDeleteBatch(ids) {
  return request({
    url: '/activitySignUp/delete/batch',
    method: 'delete',
    data: ids
  })
}