// ========== Common ==========

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// ========== User ==========

export interface User {
  id: number
  username: string
  email: string
  avatar: string | null
  status: number
  createdAt: string
}

// ========== Auth ==========

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

// ========== User Update ==========

export interface UserUpdateRequest {
  username?: string
  email?: string
}

export interface PasswordChangeRequest {
  oldPassword: string
  newPassword: string
}

// ========== Workspace ==========

export interface Workspace {
  id: number
  name: string
  description: string | null
  owner: User
  createdAt: string
}

export interface WorkspaceMember {
  id: number
  user: User
  role: string
}

export interface WorkspaceCreateRequest {
  name: string
  description?: string
}

export interface WorkspaceUpdateRequest {
  name?: string
  description?: string
}

// ========== Project ==========

export interface Project {
  id: number
  workspaceId: number
  name: string
  description: string | null
  status: string
  startDate: string | null
  endDate: string | null
  createdBy: User
  createdAt: string
}

export interface ProjectMember {
  id: number
  user: User
  role: string
}

export interface ProjectCreateRequest {
  workspaceId: number
  name: string
  description?: string
  startDate?: string
  endDate?: string
}

export interface ProjectUpdateRequest {
  name?: string
  description?: string
  status?: string
  startDate?: string
  endDate?: string
}

// ========== Task ==========

export interface Tag {
  id: number
  name: string
  color: string
}

export interface Task {
  id: number
  projectId: number
  sprintId: number | null
  parentId: number | null
  title: string
  description: string | null
  status: string
  priority: string
  assignee: User | null
  creator: User
  startDate: string | null
  dueDate: string | null
  sortOrder: number | null
  progress: number
  tags: Tag[]
  createdAt: string
  updatedAt: string
}

export interface TaskCreateRequest {
  sprintId?: number
  parentId?: number
  title: string
  description?: string
  priority?: string
  assigneeId?: number
  startDate?: string
  dueDate?: string
  sortOrder?: number
  tagIds?: number[]
}

export interface TaskUpdateRequest {
  title?: string
  description?: string
  priority?: string
  startDate?: string
  dueDate?: string
  sortOrder?: number
  progress?: number
  sprintId?: number
  tagIds?: number[]
}

export interface TaskStatusUpdateRequest {
  status: string
}

export interface TaskAssignRequest {
  assigneeId: number | null
}

export interface TaskBatchRequest {
  taskIds: number[]
  action?: string
  value?: string
  status?: string
  priority?: string
  assigneeId?: number
  sprintId?: number
}

export interface TaskQueryRequest {
  status?: string
  assigneeId?: number
  sprintId?: number
  priority?: string
  keyword?: string
  page?: number
  pageSize?: number
}

// ========== Comment ==========

export interface Comment {
  id: number
  taskId: number
  user: User
  content: string
  createdAt: string
  updatedAt: string
}

export interface CommentCreateRequest {
  content: string
}

export interface CommentUpdateRequest {
  content: string
}

// ========== Sprint ==========

export interface Sprint {
  id: number
  projectId: number
  name: string
  goal: string | null
  status: string
  startDate: string | null
  endDate: string | null
  createdAt: string
}

export interface SprintCreateRequest {
  name: string
  goal?: string
  startDate?: string
  endDate?: string
}

export interface SprintUpdateRequest {
  name?: string
  goal?: string
  status?: string
  startDate?: string
  endDate?: string
}

// ========== Document ==========

export interface DocumentVersion {
  id: number
  documentId: number
  versionNo: number
  fileName: string
  fileSize: number
  uploadedBy: User
  changeSummary: string | null
  downloadUrl: string | null
  createdAt: string
}

export interface Document {
  id: number
  projectId: number
  title: string
  type: string | null
  currentVersion: number | null
  createdBy: User
  currentVersionInfo: DocumentVersion | null
  createdAt: string
  updatedAt: string
}

export interface DocumentCreateRequest {
  title: string
  type?: string
}

export interface DocumentUpdateRequest {
  title?: string
  type?: string
}

// ========== Attachment ==========

export interface Attachment {
  id: number
  taskId: number
  fileName: string
  fileSize: number
  uploadedBy: User
  downloadUrl: string | null
  createdAt: string
}

// ========== Activity Log ==========

export interface ActivityLog {
  id: number
  projectId: number
  taskId: number | null
  projectName: string
  taskTitle: string | null
  user: User
  action: string
  detail: string | null
  createdAt: string
}

// ========== Member ==========

export interface MemberAddRequest {
  userId: number
  role: string
}

export interface MemberRoleUpdateRequest {
  role: string
}

// ========== Reports ==========

export interface Dashboard {
  todoCount: number
  inProgressCount: number
  inReviewCount: number
  doneCount: number
  projects: ProjectProgress[]
  myTodoTasks: Task[]
  recentActivities: ActivityLog[]
}

export interface ProjectProgress {
  projectId: number
  projectName: string
  totalTasks: number
  doneTasks: number
  progress: number
}

export interface TaskDistribution {
  todoCount: number
  inProgressCount: number
  inReviewCount: number
  doneCount: number
}

export interface Burndown {
  dates: string[]
  idealRemaining: number[]
  actualRemaining: number[]
}

export interface MemberWorkload {
  userId: number
  username: string
  totalTasks: number
  todoCount: number
  inProgressCount: number
  doneCount: number
}
