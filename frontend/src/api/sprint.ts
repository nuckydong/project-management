import request from './request'
import type {
  Result,
  Sprint,
  SprintCreateRequest,
  SprintUpdateRequest
} from '@/types'

export function listSprints(projectId: number) {
  return request.get<unknown, Result<Sprint[]>>(`/projects/${projectId}/sprints`)
}

export function createSprint(projectId: number, data: SprintCreateRequest) {
  return request.post<unknown, Result<Sprint>>(`/projects/${projectId}/sprints`, data)
}

export function getSprint(id: number) {
  return request.get<unknown, Result<Sprint>>(`/sprints/${id}`)
}

export function updateSprint(id: number, data: SprintUpdateRequest) {
  return request.put<unknown, Result<Sprint>>(`/sprints/${id}`, data)
}

export function deleteSprint(id: number) {
  return request.delete<unknown, Result<void>>(`/sprints/${id}`)
}
