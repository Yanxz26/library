import request from '@/utils/request'

// ==================== 图书盘点管理 ====================

// 分页查询盘点记录
export function getInventoryPage(params) {
  return request({ url: '/sys/inventory/page', method: 'get', params })
}

// 执行盘点
export function doInventory(data) {
  return request({ url: '/sys/inventory/do', method: 'post', data })
}

// 导出盘点记录
export function exportInventory() {
  return request({ url: '/sys/inventory/export', method: 'get', responseType: 'blob' })
}
