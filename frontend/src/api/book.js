import request from '@/utils/request'

// ==================== 图书管理 ====================

// 分页查询图书列表
export function getBookPage(params) {
  return request({ url: '/sys/book/page', method: 'get', params })
}

// 新增图书
export function addBook(data) {
  return request({ url: '/sys/book/add', method: 'post', data })
}

// 修改图书
export function updateBook(data) {
  return request({ url: '/sys/book/update', method: 'post', data })
}

// 图书下架
export function offShelf(bookId) {
  return request({ url: `/sys/book/off-shelf/${bookId}`, method: 'post' })
}

// 图书上架
export function onShelf(bookId) {
  return request({ url: `/sys/book/on-shelf/${bookId}`, method: 'post' })
}

// 删除图书
export function deleteBook(bookId) {
  return request({ url: `/sys/book/delete/${bookId}`, method: 'delete' })
}

// 库存增补
export function addStock(bookId, addNum) {
  return request({ url: `/sys/book/add-stock/${bookId}/${addNum}`, method: 'post' })
}

// 批量导入图书
export function batchImportBooks(formData) {
  return request({ url: '/sys/book/batch-import', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

// 导出图书数据
export function exportBooks(params) {
  return request({ url: '/sys/book/export', method: 'get', params, responseType: 'blob' })
}

// 获取图书详情
export function getBookDetail(bookId) {
  return request({ url: `/sys/book/detail/${bookId}`, method: 'get' })
}

// 获取热门图书TOP10
export function getHotBooks() {
  return request({ url: '/sys/book/hot', method: 'get' })
}

// 获取新书上架
export function getNewBooks() {
  return request({ url: '/sys/book/new', method: 'get' })
}
