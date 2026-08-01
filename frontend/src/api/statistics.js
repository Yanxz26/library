import request from '@/utils/request'

// ==================== 数据统计 ====================

// 借阅数据统计
export function getBorrowStatistics(params) {
  return request({ url: '/sys/statistics/borrow', method: 'get', params })
}

// 热门借阅图书TOP10
export function getHotBooksTop10() {
  return request({ url: '/sys/statistics/hot-books', method: 'get' })
}

// 用户数据统计
export function getUserStatistics() {
  return request({ url: '/sys/statistics/user', method: 'get' })
}

// 获取图书统计数据
export function getBookStatistics() {
  return request({ url: '/sys/statistics/book', method: 'get' })
}

// 获取当前用户个人统计数据
export function getMyStatistics() {
  return request({ url: '/sys/statistics/my-stats', method: 'get' })
}

// 导出借阅统计报表
export function exportBorrowReport(params) {
  return request({ url: '/sys/statistics/export-borrow', method: 'get', params, responseType: 'blob' })
}

// 导出逾期记录报表
export function exportOverdueReport() {
  return request({ url: '/sys/statistics/export-overdue', method: 'get', responseType: 'blob' })
}
