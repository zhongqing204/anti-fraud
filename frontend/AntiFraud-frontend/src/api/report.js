import request from '@/utils/request'

// 公告管理相关接口
export function getReportList(params) {
    return request({
        url: '/report/selectPage',
        method: 'get',
        params
    })
}

export function getAllReport(params) {
    return request({
        url: '/report/selectAll',
        method: 'get',
        params
    })
}

export function getReportDetail(id) {
    return request({
        url: `/report/selectById/${id}`,
        method: 'get'
    })
}

export function addReport(data) {
    return request({
        url: '/report/add',
        method: 'post',
        data
    })
}

export function updateReport(data) {
    return request({
        url: '/report/update',
        method: 'put',
        data
    })
}

export function deleteReport(id) {
    return request({
        url: `/report/delete/${id}`,
        method: 'delete'
    })
}

export function deleteReportBatch(ids) {
    return request({
        url: '/report/delete/batch',
        method: 'delete',
        data: ids
    })
}