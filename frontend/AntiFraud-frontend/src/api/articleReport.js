NEW_FILE_CODE
import request from '@/utils/request'

// 提交帖子举报
export function addArticleReport(data) {
    return request({
        url: '/articleReport/add',
        method: 'post',
        data
    })
}

// 查询所有帖子举报
export function getArticleReportList(params) {
    return request({
        url: '/articleReport/selectAll',
        method: 'get',
        params
    })
}

// 分页查询帖子举报
export function getArticleReportPage(params) {
    return request({
        url: '/articleReport/selectPage',
        method: 'get',
        params
    })
}

// 删除单个帖子举报
export function deleteArticleReport(id) {
    return request({
        url: `/articleReport/delete/${id}`,
        method: 'delete'
    })
}

// 批量删除帖子举报
export function deleteArticleReportBatch(ids) {
    return request({
        url: '/articleReport/delete/batch',
        method: 'delete',
        data: ids
    })
}

// 批量处理帖子举报
export function batchUpdateArticleReport(data) {
    return request({
        url: '/articleReport/batchUpdate',
        method: 'post',
        data
    })
}
