import request from './request'
import type {
  Result,
  Document,
  DocumentVersion,
  DocumentCreateRequest,
  DocumentUpdateRequest
} from '@/types'

export function listDocuments(projectId: number) {
  return request.get<unknown, Result<Document[]>>(`/projects/${projectId}/documents`)
}

export function createDocument(projectId: number, data: DocumentCreateRequest) {
  return request.post<unknown, Result<Document>>(`/projects/${projectId}/documents`, data)
}

export function getDocument(id: number) {
  return request.get<unknown, Result<Document>>(`/documents/${id}`)
}

export function updateDocument(id: number, data: DocumentUpdateRequest) {
  return request.put<unknown, Result<Document>>(`/documents/${id}`, data)
}

export function deleteDocument(id: number) {
  return request.delete<unknown, Result<void>>(`/documents/${id}`)
}

export function uploadVersion(id: number, file: File, changeSummary?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (changeSummary) {
    formData.append('changeSummary', changeSummary)
  }
  return request.post<unknown, Result<DocumentVersion>>(`/documents/${id}/versions`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getVersions(id: number) {
  return request.get<unknown, Result<DocumentVersion[]>>(`/documents/${id}/versions`)
}

export function downloadVersion(versionId: number) {
  return request.get<unknown, Result<string>>(`/document-versions/${versionId}/download`)
}

export function previewVersion(versionId: number) {
  return request.get<unknown, Result<string>>(`/document-versions/${versionId}/preview`)
}
