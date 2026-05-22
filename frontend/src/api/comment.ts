import request from './request'
import type { Result, Comment, CommentCreateRequest, CommentUpdateRequest } from '@/types'

export function listComments(taskId: number) {
  return request.get<unknown, Result<Comment[]>>(`/tasks/${taskId}/comments`)
}

export function createComment(taskId: number, data: CommentCreateRequest) {
  return request.post<unknown, Result<Comment>>(`/tasks/${taskId}/comments`, data)
}

export function updateComment(commentId: number, data: CommentUpdateRequest) {
  return request.put<unknown, Result<Comment>>(`/comments/${commentId}`, data)
}

export function deleteComment(commentId: number) {
  return request.delete<unknown, Result<void>>(`/comments/${commentId}`)
}
