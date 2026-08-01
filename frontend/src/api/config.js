import request from '@/utils/request'

// ==================== 系统配置 ====================

// 分页查询配置
export function getConfigPage(params) {
  return request({ url: '/sys/config/page', method: 'get', params })
}

// 根据键获取配置值
export function getConfigValue(configKey) {
  return request({ url: `/sys/config/value/${configKey}`, method: 'get' })
}

// 新增配置
export function addConfig(data) {
  return request({ url: '/sys/config/add', method: 'post', data })
}

// 修改配置
export function updateConfig(data) {
  return request({ url: '/sys/config/update', method: 'post', data })
}

// 删除配置
export function deleteConfig(id) {
  return request({ url: `/sys/config/delete/${id}`, method: 'delete' })
}

// 获取所有配置列表（公开）
export function getPublicConfigs() {
  return request({ url: '/sys/config/public/list', method: 'get' })
}
