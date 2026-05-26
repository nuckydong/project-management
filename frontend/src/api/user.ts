import request from './request'
import type { Result, User, UserUpdateRequest, PasswordChangeRequest } from '@/types'

export function searchUsers(keyword?: string) {
  return request.get<unknown, Result<User[]>>('/users/search', { params: { keyword } })
}

export function listAllUsers() {
  return request.get<unknown, Result<User[]>>('/users/list')
}

export function updateUserStatus(id: number, status: number) {
  return request.put<unknown, Result<User>>(`/users/${id}/status`, { status })
}

export function resetUserPassword(id: number, newPassword: string) {
  return request.put<unknown, Result<void>>(`/users/${id}/reset-password`, { newPassword })
}

export function createUser(username: string, email: string, password: string) {
  return request.post<unknown, Result<User>>('/users/create', { username, email, password })
}

export function updateUser(id: number, username: string, email: string) {
  return request.put<unknown, Result<User>>(`/users/${id}/edit`, { username, email })
}

export function deleteUser(id: number) {
  return request.delete<unknown, Result<void>>(`/users/${id}`)
}

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
