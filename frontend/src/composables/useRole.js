// ============================================
// useRole — 角色检查工具
// 统一所有页面的 isAdmin / isLibrary / isStaff 判断
// ============================================

import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export function useRole() {
  const authStore = useAuthStore()

  const isAdmin = computed(() => authStore.userInfo?.roleCode === 'admin')
  const isLibrary = computed(() => authStore.userInfo?.roleCode === 'library')
  const isStaff = computed(() => ['admin', 'library'].includes(authStore.userInfo?.roleCode))
  const isUser = computed(() => authStore.userInfo?.roleCode === 'user')
  const roleCode = computed(() => authStore.userInfo?.roleCode || '')

  return { isAdmin, isLibrary, isStaff, isUser, roleCode }
}
