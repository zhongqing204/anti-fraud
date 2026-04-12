import request from '@/utils/request'

/**
 * 统一登录接口
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 用户登录接口（旧接口，保留兼容）
 */
export function userLogin(data) {
  return request.post('/user/login', data)
}

/**
 * 管理员登录接口（旧接口，保留兼容）
 */
export function adminLogin(data) {
  return request.post('/admin/login', data)
}
