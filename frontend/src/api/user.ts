import request from './request'
import type { Result, User, UserUpdateRequest, PasswordChangeRequest } from '@/types'

export function getProfile() {
  return request.get<unknown, Result<User>>('/users/profile')
}

export function updateProfile(data: UserUpdateRequest) {
  return request.put<unknown, Result<User>>('/users/profile', data)
}

export function changePassword(data: PasswordChangeRequest) {
  return request.put<unknown, Result<void>>('/users/password', data)
}

export function updateAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, Result<User>>('/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
