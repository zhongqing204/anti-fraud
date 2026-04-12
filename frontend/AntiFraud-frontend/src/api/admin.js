NEW_FILE_CODE
import request from '@/utils/request'

/**
 * 获取当前管理员信息
 */
export function getAdminInfo() {
  return request.get('/admin/current')
}

/**
 * 更新管理员信息
 */
export function updateAdminInfo(data) {
  return request.put('/admin/updateCurrent', data)
}

/**
 * 修改管理员密码
 */
export function updateAdminPassword(data) {
  return request.put('/admin/updatePassword', data)
}
