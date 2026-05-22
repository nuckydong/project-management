import request from './request'
import type {
  Result,
  PageResult,
  Task,
  TaskCreateRequest,
  TaskUpdateRequest,
  TaskStatusUpdateRequest,
  TaskAssignRequest,
  TaskBatchRequest,
  TaskQueryRequest
} from '@/types'

export function listTasks(projectId: number, params?: TaskQueryRequest) {
  return request.get<unknown, Result<PageResult<Task>>>(`/projects/${projectId}/tasks`, { params })
}

export function createTask(projectId: number, data: TaskCreateRequest) {
  return request.post<unknown, Result<Task>>(`/projects/${projectId}/tasks`, data)
}

export function getTask(id: number) {
  return request.get<unknown, Result<Task>>(`/tasks/${id}`)
}

export function updateTask(id: number, data: TaskUpdateRequest) {
  return request.put<unknown, Result<Task>>(`/tasks/${id}`, data)
}

export function deleteTask(id: number) {
  return request.delete<unknown, Result<void>>(`/tasks/${id}`)
}

export function updateTaskStatus(id: number, data: TaskStatusUpdateRequest) {
  return request.put<unknown, Result<Task>>(`/tasks/${id}/status`, data)
}

export function assignTask(id: number, data: TaskAssignRequest) {
  return request.put<unknown, Result<Task>>(`/tasks/${id}/assignee`, data)
}

export function batchTasks(data: TaskBatchRequest) {
  return request.put<unknown, Result<void>>('/tasks/batch', data)
}
