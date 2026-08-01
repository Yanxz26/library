import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import { setToken, getToken, removeToken, setUser, getUser, removeUser, clearAll } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUser() || {})

  // 登录
  async function login(loginData) {
    const res = await loginApi(loginData)
    if (res.code === 200) {
      const { token: jwtToken, userId, userName, roleCode, roleName } = res.data
      token.value = jwtToken
      userInfo.value = { userId, userName, roleCode, roleName }
      setToken(jwtToken)
      setUser({ userId, userName, roleCode, roleName })
    }
    return res
  }

  // 登出
  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      userInfo.value = {}
      clearAll()
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    if (res.code === 200) {
      userInfo.value = res.data
      setUser(res.data)
    }
    return res
  }

  // 重置 token
  function resetToken() {
    token.value = ''
    userInfo.value = {}
    clearAll()
  }

  // 是否有权限
  function hasRole(roles) {
    if (!roles || roles.length === 0) return true
    return roles.includes(userInfo.value?.roleCode)
  }

  return {
    token,
    userInfo,
    login,
    logout,
    fetchUserInfo,
    resetToken,
    hasRole,
  }
})
