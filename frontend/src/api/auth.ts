import request from './request'
import type { Result, AuthResponse, LoginRequest, RegisterRequest } from '@/types'

export function login(data: LoginRequest) {
  return request.post<unknown, Result<AuthResponse>>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<unknown, Result<AuthResponse>>('/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return request.post<unknown, Result<AuthResponse>>('/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post<unknown, Result<void>>('/auth/logout')
}
