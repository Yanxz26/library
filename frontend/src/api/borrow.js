import request from '@/utils/request'

// ==================== 图书借阅管理 ====================

// 图书借阅
export function borrowBook(bookId) {
  return request({ url: `/borrow/${bookId}`, method: 'post' })
}

// 图书归还
export function returnBook(borrowId) {
  return request({ url: `/borrow/return/${borrowId}`, method: 'post' })
}

// 图书续借
export function renewBook(borrowId) {
  return request({ url: `/borrow/renew/${borrowId}`, method: 'post' })
}

// 管理员线下登记归还
export function adminReturn(borrowId) {
  return request({ url: `/borrow/admin-return/${borrowId}`, method: 'post' })
}

// 分页查询借阅记录（管理员）
export function getBorrowPage(params) {
  return request({ url: '/borrow/page', method: 'get', params })
}

// 获取当前用户借阅记录
export function getMyBorrows(params) {
  return request({ url: '/borrow/my-borrows', method: 'get', params })
}

// 导出借阅记录
export function exportBorrows(params) {
  return request({ url: '/borrow/export', method: 'get', params, responseType: 'blob' })
}
