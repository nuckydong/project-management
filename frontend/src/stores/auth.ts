import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, LoginRequest, RegisterRequest } from '@/types'
import { login as apiLogin, register as apiRegister, refreshToken as apiRefreshToken, logout as apiLogout } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string>(localStorage.getItem('accessToken') || '')
  const refreshTokenVal = ref<string>(localStorage.getItem('refreshToken') || '')
  const user = ref<User | null>(null)

  function forceClear() {
    accessToken.value = ''
    refreshTokenVal.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  function setTokens(access: string, refresh: string) {
    accessToken.value = access
    refreshTokenVal.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  function clearTokens() {
    accessToken.value = ''
    refreshTokenVal.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  function setUser(userData: User) {
    user.value = userData
  }

  async function login(data: LoginRequest) {
    const res = await apiLogin(data)
    setTokens(res.data.accessToken, res.data.refreshToken)
    user.value = res.data.user
    return res.data
  }

  async function register(data: RegisterRequest) {
    const res = await apiRegister(data)
    setTokens(res.data.accessToken, res.data.refreshToken)
    user.value = res.data.user
    return res.data
  }

  async function doRefreshToken() {
    const res = await apiRefreshToken(refreshTokenVal.value)
    setTokens(res.data.accessToken, res.data.refreshToken)
    user.value = res.data.user
    return res.data
  }

  async function logout() {
    try {
      await apiLogout()
    } catch {
      // ignore errors on logout
    }
    clearTokens()
  }

  return {
    accessToken,
    refreshToken: refreshTokenVal,
    user,
    login,
    register,
    doRefreshToken,
    logout,
    forceClear,
    setUser
  }
})
