import request from '@/utils/request'

// ==================== 用户管理 ====================

// 分页查询用户列表
export function getUserPage(params) {
  return request({ url: '/sys/user/page', method: 'get', params })
}

// 新增用户
export function addUser(data) {
  return request({ url: '/sys/user/add', method: 'post', data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/sys/user/update', method: 'post', data })
}

// 删除用户
export function deleteUser(userId) {
  return request({ url: `/sys/user/delete/${userId}`, method: 'delete' })
}

// 修改用户状态
export function updateUserStatus(userId, status) {
  return request({ url: `/sys/user/update-status/${userId}/${status}`, method: 'post' })
}

// 批量导入用户
export function batchImportUsers(formData) {
  return request({ url: '/sys/user/batch-import', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

// 导出用户数据
export function exportUsers(params) {
  return request({ url: '/sys/user/export', method: 'get', params, responseType: 'blob' })
}

// 获取用户详情
export function getUserDetail(userId) {
  return request({ url: `/sys/user/detail/${userId}`, method: 'get' })
}

// 修改个人信息
export function updateProfile(data) {
  return request({ url: '/sys/user/profile', method: 'post', data })
}
