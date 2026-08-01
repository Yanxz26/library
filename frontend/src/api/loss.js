import request from '@/utils/request'

// ==================== 图书损耗管理 ====================

// 分页查询损耗记录
export function getLossPage(params) {
  return request({ url: '/sys/loss/page', method: 'get', params })
}

// 登记损耗
export function registerLoss(data) {
  return request({ url: '/sys/loss/register', method: 'post', data })
}

// 导出损耗记录
export function exportLoss() {
  return request({ url: '/sys/loss/export', method: 'get', responseType: 'blob' })
}
