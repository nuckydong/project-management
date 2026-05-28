import request from './request'
import type { Result, ActivityLog } from '@/types'

export function listActivityLogs(taskId: number) {
  return request.get<unknown, Result<ActivityLog[]>>(`/tasks/${taskId}/activities`)
}
