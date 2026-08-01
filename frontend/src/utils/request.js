import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, removeToken } from '@/utils/auth'

const service = axios.create({
  baseURL: '/api',
  timeout: 8000,
})

// 请求拦截器 - 添加 JWT Token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理错误
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 文件下载等特殊请求直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务成功
    if (res.code === 200) {
      return res
    }

    // 业务失败
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // HTTP 错误
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        removeToken()
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('没有权限执行此操作')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status >= 500) {
        ElMessage.error('服务器错误，请稍后重试')
      }
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service
