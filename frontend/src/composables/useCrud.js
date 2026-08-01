// ============================================
// useCrud — 通用 CRUD 操作
// 将所有管理页面的增删改查逻辑抽取为统一模式
// ============================================

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * @param {Object} options
 * @param {Function} options.fetchPage - 分页查询 API 函数，接收 params 返回 Result<PageResult>
 * @param {Function} [options.deleteItem] - 删除 API 函数，接收 id 返回 Result
 * @param {Object} [options.defaultQuery] - 默认查询参数
 * @param {Function} [options.onFetchError] - 自定义错误处理
 */
export function useCrud(options) {
  const { fetchPage, deleteItem, defaultQuery = {}, onFetchError } = options

  const tableData = ref([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref(null)
  const queryForm = reactive({
    current: 1,
    size: 10,
    ...defaultQuery,
  })

  // 清理空参数
  function cleanParams(params) {
    const cleaned = { ...params }
    Object.keys(cleaned).forEach(k => {
      if (cleaned[k] === '' || cleaned[k] === null || cleaned[k] === undefined) {
        delete cleaned[k]
      }
    })
    return cleaned
  }

  // 获取数据
  async function fetchData() {
    loading.value = true
    error.value = null
    try {
      const params = cleanParams({ ...queryForm })
      const res = await fetchPage(params)
      tableData.value = res?.data?.records ?? res?.data ?? []
      total.value = res?.data?.total ?? 0
    } catch (err) {
      console.error('[useCrud] fetchData error:', err)
      error.value = err
      tableData.value = []
      total.value = 0
      if (onFetchError) {
        onFetchError(err)
      } else {
        ElMessage.error('加载数据失败，请刷新重试')
      }
    } finally {
      loading.value = false
    }
  }

  // 搜索（重置到第一页）
  function handleSearch() {
    queryForm.current = 1
    fetchData()
  }

  // 重置搜索条件
  function handleReset() {
    Object.keys(queryForm).forEach(k => {
      if (k === 'current') queryForm.current = 1
      else if (k === 'size') queryForm.size = 10
      else if (defaultQuery[k] !== undefined) queryForm[k] = defaultQuery[k]
      else queryForm[k] = ''
    })
    fetchData()
  }

  // 分页变化
  function handlePageChange(current, size) {
    queryForm.current = current
    queryForm.size = size
    fetchData()
  }

  // 删除
  async function handleDelete(id) {
    if (!deleteItem) {
      console.warn('[useCrud] deleteItem not provided')
      return false
    }
    try {
      await deleteItem(id)
      ElMessage.success('删除成功')
      // 如果当前页只有一条且不是第一页，回到上一页
      if (tableData.value.length === 1 && queryForm.current > 1) {
        queryForm.current--
      }
      await fetchData()
      return true
    } catch (err) {
      console.error('[useCrud] delete error:', err)
      // 错误已在拦截器中处理
      return false
    }
  }

  // 刷新
  function refresh() {
    fetchData()
  }

  return {
    // 状态
    tableData,
    total,
    loading,
    error,
    queryForm,
    // 方法
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleDelete,
    refresh,
    cleanParams,
  }
}
