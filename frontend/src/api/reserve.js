import request from '@/utils/request'

// ==================== 图书预约管理 ====================

// 图书预约
export function reserveBook(bookId) {
  return request({ url: `/reserve/${bookId}`, method: 'post' })
}

// 取消预约
export function cancelReserve(reserveId) {
  return request({ url: `/reserve/cancel/${reserveId}`, method: 'post' })
}

// 分页查询预约记录
export function getReservePage(params) {
  return request({ url: '/reserve/page', method: 'get', params })
}

// 获取我的预约记录
export function getMyReserves(params) {
  return request({ url: '/reserve/my-reserves', method: 'get', params })
}

// 管理员替用户预约
export function adminReserveBook(bookId, userId) {
  return request({ url: `/reserve/admin/${bookId}`, method: 'post', params: { userId } })
}

// 标记预约完成
export function completeReserve(reserveId) {
  return request({ url: `/reserve/complete/${reserveId}`, method: 'post' })
}
