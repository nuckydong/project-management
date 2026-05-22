import axios from 'axios'
import type { Result } from '@/types'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data as Result<unknown>
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || 'Request failed'))
    }
    return response.data
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const authStore = useAuthStore()
      if (authStore.refreshToken) {
        try {
          await authStore.doRefreshToken()
          originalRequest.headers.Authorization = `Bearer ${authStore.accessToken}`
          return request(originalRequest)
        } catch {
          authStore.logout()
          router.push('/login')
          return Promise.reject(error)
        }
      } else {
        authStore.logout()
        router.push('/login')
      }
    }

    const message = error.response?.data?.message || error.message || 'Network error'
    return Promise.reject(new Error(message))
  }
)

export default request
