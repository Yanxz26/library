import request from '@/utils/request'

// ==================== 逾期管理 ====================

// 分页查询逾期记录（管理员）
export function getOverduePage(params) {
  return request({ url: '/overdue/page', method: 'get', params })
}

// 获取我的逾期记录
export function getMyOverdues(params) {
  return request({ url: '/overdue/my-overdue', method: 'get', params })
}

// 缴纳逾期罚款
export function payFine(overdueId) {
  return request({ url: `/overdue/pay/${overdueId}`, method: 'post' })
}

// 管理员减免罚款
export function waiveFine(overdueId, reason) {
  return request({ url: `/overdue/waive/${overdueId}`, method: 'post', params: { reason } })
}

// 获取逾期汇总信息
export function getOverdueSummary() {
  return request({ url: '/overdue/summary', method: 'get' })
}

// 导出逾期记录
export function exportOverdue() {
  return request({ url: '/overdue/export', method: 'get', responseType: 'blob' })
}
