import request from './request'
import type {
  Result,
  Workspace,
  WorkspaceMember,
  WorkspaceCreateRequest,
  WorkspaceUpdateRequest,
  MemberAddRequest,
  MemberRoleUpdateRequest
} from '@/types'

export function listWorkspaces() {
  return request.get<unknown, Result<Workspace[]>>('/workspaces')
}

export function createWorkspace(data: WorkspaceCreateRequest) {
  return request.post<unknown, Result<Workspace>>('/workspaces', data)
}

export function getWorkspace(id: number) {
  return request.get<unknown, Result<Workspace>>(`/workspaces/${id}`)
}

export function updateWorkspace(id: number, data: WorkspaceUpdateRequest) {
  return request.put<unknown, Result<Workspace>>(`/workspaces/${id}`, data)
}

export function deleteWorkspace(id: number) {
  return request.delete<unknown, Result<void>>(`/workspaces/${id}`)
}

export function getWorkspaceMembers(id: number) {
  return request.get<unknown, Result<WorkspaceMember[]>>(`/workspaces/${id}/members`)
}

export function addWorkspaceMember(id: number, data: MemberAddRequest) {
  return request.post<unknown, Result<WorkspaceMember>>(`/workspaces/${id}/members`, data)
}

export function updateWorkspaceMemberRole(id: number, uid: number, data: MemberRoleUpdateRequest) {
  return request.put<unknown, Result<WorkspaceMember>>(`/workspaces/${id}/members/${uid}`, data)
}

export function removeWorkspaceMember(id: number, uid: number) {
  return request.delete<unknown, Result<void>>(`/workspaces/${id}/members/${uid}`)
}
