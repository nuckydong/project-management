import request from './request'
import type { Result, Attachment } from '@/types'

export function listAttachments(taskId: number) {
  return request.get<unknown, Result<Attachment[]>>(`/tasks/${taskId}/attachments`)
}

export function uploadAttachment(taskId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, Result<Attachment>>(`/tasks/${taskId}/attachments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteAttachment(attachmentId: number) {
  return request.delete<unknown, Result<void>>(`/attachments/${attachmentId}`)
}

export function previewAttachment(attachmentId: number) {
  return request.get<unknown, Result<string>>(`/attachments/${attachmentId}/preview`)
}

export function downloadAttachment(attachmentId: number) {
  return request.get<unknown, Result<string>>(`/attachments/${attachmentId}/download`)
}
