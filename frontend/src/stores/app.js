import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const COLLAPSE_KEY = 'library_sidebar_collapse'

function loadCollapseState() {
  try {
    return localStorage.getItem(COLLAPSE_KEY) === 'true'
  } catch {
    return false
  }
}

export const useAppStore = defineStore('app', () => {
  const sidebarCollapse = ref(loadCollapseState())

  function toggleSidebar() {
    sidebarCollapse.value = !sidebarCollapse.value
  }

  // 持久化侧边栏折叠状态
  watch(sidebarCollapse, (val) => {
    try {
      localStorage.setItem(COLLAPSE_KEY, val)
    } catch { /* localStorage 不可用 */ }
  })

  return {
    sidebarCollapse,
    toggleSidebar,
  }
})
