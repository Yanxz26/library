import request from '@/utils/request'

// ==================== 图书分类管理 ====================

// 获取分类树
export function getCategoryTree() {
  return request({ url: '/sys/category/tree', method: 'get' })
}

// 新增分类
export function addCategory(data) {
  return request({ url: '/sys/category/add', method: 'post', data })
}

// 修改分类
export function updateCategory(data) {
  return request({ url: '/sys/category/update', method: 'post', data })
}

// 删除分类
export function deleteCategory(id) {
  return request({ url: `/sys/category/delete/${id}`, method: 'delete' })
}

// 更新分类状态
export function updateCategoryStatus(id, status) {
  return request({ url: `/sys/category/update-status/${id}/${status}`, method: 'post' })
}
