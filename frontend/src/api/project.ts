import request from './request'
import type {
  Result,
  Project,
  ProjectMember,
  ProjectCreateRequest,
  ProjectUpdateRequest,
  MemberAddRequest,
  MemberRoleUpdateRequest
} from '@/types'

export function listProjects(workspaceId?: number) {
  const params: Record<string, unknown> = {}
  if (workspaceId !== undefined) {
    params.workspaceId = workspaceId
  }
  return request.get<unknown, Result<Project[]>>('/projects', { params })
}

export function createProject(data: ProjectCreateRequest) {
  return request.post<unknown, Result<Project>>('/projects', data)
}

export function getProject(id: number) {
  return request.get<unknown, Result<Project>>(`/projects/${id}`)
}

export function updateProject(id: number, data: ProjectUpdateRequest) {
  return request.put<unknown, Result<Project>>(`/projects/${id}`, data)
}

export function deleteProject(id: number) {
  return request.delete<unknown, Result<void>>(`/projects/${id}`)
}

export function getProjectMembers(id: number) {
  return request.get<unknown, Result<ProjectMember[]>>(`/projects/${id}/members`)
}

export function addProjectMember(id: number, data: MemberAddRequest) {
  return request.post<unknown, Result<ProjectMember>>(`/projects/${id}/members`, data)
}

export function updateProjectMemberRole(id: number, uid: number, data: MemberRoleUpdateRequest) {
  return request.put<unknown, Result<ProjectMember>>(`/projects/${id}/members/${uid}`, data)
}

export function removeProjectMember(id: number, uid: number) {
  return request.delete<unknown, Result<void>>(`/projects/${id}/members/${uid}`)
}
