// ============================================
// useExport — 通用文件导出
// 消除 7 处重复的 blob 下载代码
// ============================================

import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * @param {Function} exportApi - 导出 API 函数，接收 params 返回 Blob response
 * @param {Object} [options]
 * @param {string} [options.filePrefix] - 文件名前缀
 * @param {string} [options.fileType] - 文件 MIME 类型
 */
export function useExport(exportApi, options = {}) {
  const { filePrefix = 'export', fileType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' } = options

  const exporting = ref(false)

  async function handleExport(params = {}) {
    if (exporting.value) return
    exporting.value = true
    try {
      const res = await exportApi(params)
      const blob = new Blob([res.data], { type: fileType })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${filePrefix}_${new Date().toISOString().slice(0, 10)}.xlsx`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    } catch (err) {
      console.error('[useExport] export error:', err)
      ElMessage.error('导出失败')
    } finally {
      exporting.value = false
    }
  }

  return { exporting, handleExport }
}
