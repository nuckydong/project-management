import request from './request'
import type {
  Result,
  Dashboard,
  ProjectProgress,
  TaskDistribution,
  Burndown,
  MemberWorkload
} from '@/types'

export function getDashboard() {
  return request.get<unknown, Result<Dashboard>>('/reports/dashboard')
}

export function getProjectProgress(projectId: number) {
  return request.get<unknown, Result<ProjectProgress>>(`/reports/project/${projectId}/progress`)
}

export function getTaskDistribution(projectId: number) {
  return request.get<unknown, Result<TaskDistribution>>(`/reports/project/${projectId}/task-distribution`)
}

export function getBurndown(sprintId: number) {
  return request.get<unknown, Result<Burndown>>(`/reports/sprint/${sprintId}/burndown`)
}

export function getMemberWorkload(projectId: number) {
  return request.get<unknown, Result<MemberWorkload[]>>(`/reports/project/${projectId}/member-workload`)
}
