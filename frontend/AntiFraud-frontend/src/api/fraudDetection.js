import request from '@/utils/request'

// 检测诈骗文本
export function detectFraud(data) {
    return request({
        url: '/fraud-detection/detect',
        method: 'post',
        data
    })
}

// 获取检测历史
export function getDetectionHistory(params) {
    return request({
        url: '/fraud-detection/history',
        method: 'get',
        params
    })
}
