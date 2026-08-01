import request from '@/utils/request'

// ==================== 认证管理 ====================

// 用户登录
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

// 用户登出
export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}

// 修改密码
export function changePassword(data) {
  return request({ url: '/auth/change-password', method: 'post', data })
}

// 管理员重置用户密码
export function resetPassword(userId) {
  return request({ url: `/auth/reset-password/${userId}`, method: 'post' })
}

// 管理员设置用户密码
export function setPassword(userId, password) {
  return request({ url: `/auth/set-password/${userId}`, method: 'post', data: { password } })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({ url: '/auth/user-info', method: 'get' })
}

// 忘记密码 - 验证身份
export function forgotPasswordVerify(data) {
  return request({ url: '/auth/forgot-password/verify', method: 'post', data })
}

// 忘记密码 - 设置新密码
export function forgotPasswordReset(data) {
  return request({ url: '/auth/forgot-password/reset', method: 'post', data })
}
