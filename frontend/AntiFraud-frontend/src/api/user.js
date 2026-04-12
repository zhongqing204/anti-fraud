NEW_FILE_CODE
import request from '@/utils/request'

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request.get('/user/current')
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return request.put('/user/updateCurrent', data)
}

/**
 * 修改用户密码
 */
export function updatePassword(data) {
  return request.put('/user/updatePassword', data)
}
