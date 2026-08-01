// ============================================
// usePagination — 分页状态管理
// ============================================

import { reactive, computed } from 'vue'

/**
 * @param {Object} [options]
 * @param {number} [options.defaultSize=10] - 默认每页条数
 * @param {number[]} [options.pageSizes=[10,20,50]] - 可选每页条数
 */
export function usePagination(options = {}) {
  const { defaultSize = 10, pageSizes = [10, 20, 50] } = options

  const pagination = reactive({
    current: 1,
    size: defaultSize,
    total: 0,
  })

  const totalPages = computed(() => Math.ceil(pagination.total / pagination.size))

  function setPage(current, size) {
    pagination.current = current
    if (size !== undefined) pagination.size = size
  }

  function reset() {
    pagination.current = 1
    pagination.size = defaultSize
  }

  function setTotal(total) {
    pagination.total = total
  }

  return {
    pagination,
    totalPages,
    pageSizes,
    setPage,
    reset,
    setTotal,
  }
}
