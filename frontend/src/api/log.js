import request from '@/utils/request'

// ==================== 系统日志 ====================

// 分页查询日志
export function getLogPage(params) {
  return request({ url: '/sys/log/page', method: 'get', params })
}

// 查询操作日志
export function getOperationLogs(params) {
  return request({ url: '/sys/log/operation', method: 'get', params })
}

// 查询业务日志
export function getBusinessLogs(params) {
  return request({ url: '/sys/log/business', method: 'get', params })
}

// 查询异常日志
export function getErrorLogs(params) {
  return request({ url: '/sys/log/error', method: 'get', params })
}

// 导出日志
export function exportLogs(params) {
  return request({ url: '/sys/log/export', method: 'get', params, responseType: 'blob' })
}
