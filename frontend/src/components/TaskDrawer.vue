<template>
  <a-drawer
    :open="visible"
    :width="640"
    @close="handleClose"
    :title="null"
    :body-style="{ padding: 0 }"
  >
    <a-spin :spinning="loading">
      <div v-if="task" class="drawer-content">
        <!-- Header Section -->
        <div class="drawer-header">
          <div class="header-title-row">
            <a-input
              v-model:value="editTitle"
              class="title-input"
              :bordered="false"
              size="large"
              @blur="saveTitle"
              @pressEnter="saveTitle"
            />
          </div>
          <div class="header-meta">
            <div class="meta-item">
              <span class="meta-label">状态</span>
              <a-select v-model:value="editStatus" size="small" style="width: 110px" @change="saveStatus">
                <a-select-option value="TODO">待办</a-select-option>
                <a-select-option value="IN_PROGRESS">进行中</a-select-option>
                <a-select-option value="IN_REVIEW">待审核</a-select-option>
                <a-select-option value="DONE">已完成</a-select-option>
              </a-select>
            </div>
            <div class="meta-item">
              <span class="meta-label">优先级</span>
              <a-select v-model:value="editPriority" size="small" style="width: 100px" @change="savePriority">
                <a-select-option value="LOW">低</a-select-option>
                <a-select-option value="MEDIUM">中</a-select-option>
                <a-select-option value="HIGH">高</a-select-option>
                <a-select-option value="URGENT">紧急</a-select-option>
              </a-select>
            </div>
            <div class="meta-item">
              <span class="meta-label">负责人</span>
              <a-select v-model:value="editAssigneeId" size="small" style="width: 120px" allow-clear @change="saveAssignee">
                <a-select-option v-for="m in projectMembers" :key="m.user.id" :value="m.user.id">
                  {{ m.user.username }}
                </a-select-option>
              </a-select>
            </div>
          </div>
          <div class="header-dates">
            <div class="meta-item">
              <span class="meta-label">开始日期</span>
              <a-date-picker v-model:value="editStartDate" size="small" @change="saveDates" />
            </div>
            <div class="meta-item">
              <span class="meta-label">截止日期</span>
              <a-date-picker v-model:value="editDueDate" size="small" @change="saveDates" />
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="drawer-section">
          <h4 class="section-title">描述</h4>
          <a-textarea
            v-model:value="editDescription"
            :auto-size="{ minRows: 3, maxRows: 8 }"
            placeholder="添加描述..."
            @blur="saveDescription"
          />
        </div>

        <!-- Tabs: Comments, Attachments, Activity -->
        <a-tabs v-model:activeKey="activeTab" class="drawer-tabs">
          <!-- Comments Tab -->
          <a-tab-pane key="comments" tab="评论">
            <div class="comments-section">
              <div class="comment-input">
                <a-textarea
                  v-model:value="newComment"
                  placeholder="添加评论..."
                  :auto-size="{ minRows: 2, maxRows: 4 }"
                />
                <a-button
                  type="primary"
                  size="small"
                  :disabled="!newComment.trim()"
                  :loading="commentLoading"
                  @click="postComment"
                  style="margin-top: 8px"
                >
                  发送
                </a-button>
              </div>
              <div class="comments-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <a-avatar :size="28" :style="{ backgroundColor: '#5B9BD5', fontSize: '12px' }">
                    {{ comment.user.username.charAt(0).toUpperCase() }}
                  </a-avatar>
                  <div class="comment-body">
                    <div class="comment-header">
                      <span class="comment-user">{{ comment.user.username }}</span>
                      <span class="comment-time">{{ formatDateTime(comment.createdAt) }}</span>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                  </div>
                </div>
                <a-empty v-if="!comments.length" description="暂无评论" :image-style="{ height: '40px' }" />
              </div>
            </div>
          </a-tab-pane>

          <!-- Attachments Tab -->
          <a-tab-pane key="attachments" tab="附件">
            <div class="attachments-section">
              <a-upload
                :file-list="fileList"
                :custom-request="handleUpload"
                @remove="handleRemoveAttachment"
              >
                <a-button size="small">
                  <UploadOutlined /> 上传文件
                </a-button>
              </a-upload>
              <div class="attachment-list">
                <div v-for="att in attachments" :key="att.id" class="attachment-item">
                  <PaperClipOutlined />
                  <span class="att-name">{{ att.fileName }}</span>
                  <span class="att-size">{{ formatSize(att.fileSize) }}</span>
                  <span class="att-user">{{ att.uploadedBy?.username }}</span>
                  <span class="att-actions">
                    <a-button type="link" size="small" @click="handlePreviewAttachment(att.id)">
                      <EyeOutlined /> 预览
                    </a-button>
                    <a-button type="link" size="small" @click="handleDownloadAttachment(att.id, att.fileName)">
                      <DownloadOutlined /> 下载
                    </a-button>
                  </span>
                </div>
                <a-empty v-if="!attachments.length" description="暂无附件" :image-style="{ height: '40px' }" />
              </div>
            </div>
          </a-tab-pane>

          <!-- Activity Tab -->
          <a-tab-pane key="activity" tab="活动">
            <div class="activity-section">
              <a-timeline>
                <a-timeline-item
                  v-for="log in activities"
                  :key="log.id"
                >
                  <div class="activity-entry">
                    <a-avatar :size="20" :style="{ backgroundColor: '#5B9BD5', fontSize: '10px' }">
                      {{ log.user.username.charAt(0).toUpperCase() }}
                    </a-avatar>
                    <span class="activity-user">{{ log.user.username }}</span>
                    <span class="activity-detail">{{ log.detail || log.action }}</span>
                    <div class="activity-ts">{{ formatDateTime(log.createdAt) }}</div>
                  </div>
                </a-timeline-item>
              </a-timeline>
              <a-empty v-if="!activities.length" description="暂无活动记录" :image-style="{ height: '40px' }" />
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined, PaperClipOutlined, EyeOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { getTask, updateTask, updateTaskStatus, assignTask } from '@/api/task'
import { listComments, createComment } from '@/api/comment'
import { listAttachments, uploadAttachment, deleteAttachment, previewAttachment, downloadAttachment } from '@/api/attachment'
import type { Task, Comment, Attachment, ActivityLog, ProjectMember } from '@/types'
import type { UploadFile } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'

const props = defineProps<{
  visible: boolean
  taskId: number | null
  projectId: number
  projectMembers: ProjectMember[]
}>()

const emit = defineEmits<{
  close: []
  updated: []
}>()

const loading = ref(false)
const task = ref<Task | null>(null)
const activeTab = ref('comments')

// Edit fields
const editTitle = ref('')
const editStatus = ref('')
const editPriority = ref('')
const editAssigneeId = ref<number | null>(null)
const editStartDate = ref<Dayjs | null>(null)
const editDueDate = ref<Dayjs | null>(null)
const editDescription = ref('')

// Comments
const comments = ref<Comment[]>([])
const newComment = ref('')
const commentLoading = ref(false)

// Attachments
const attachments = ref<Attachment[]>([])
const fileList = ref<UploadFile[]>([])

// Activities (from task history - we use comments as proxy since there's no task-specific activity endpoint)
const activities = ref<ActivityLog[]>([])

watch(() => props.visible, async (vis) => {
  if (vis && props.taskId) {
    await loadTaskData()
  }
}, { immediate: true })

async function loadTaskData() {
  if (!props.taskId) return
  loading.value = true
  try {
    const [taskRes, commentsRes, attachRes] = await Promise.all([
      getTask(props.taskId),
      listComments(props.taskId),
      listAttachments(props.taskId)
    ])
    task.value = taskRes.data
    comments.value = commentsRes.data
    attachments.value = attachRes.data

    editTitle.value = task.value.title
    editStatus.value = task.value.status
    editPriority.value = task.value.priority
    editAssigneeId.value = task.value.assignee?.id ?? null
    editStartDate.value = task.value.startDate ? dayjs(task.value.startDate) : null
    editDueDate.value = task.value.dueDate ? dayjs(task.value.dueDate) : null
    editDescription.value = task.value.description || ''
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载任务失败')
  } finally {
    loading.value = false
  }
}

async function saveTitle() {
  if (!task.value || editTitle.value === task.value.title) return
  try {
    await updateTask(task.value.id, { title: editTitle.value })
    task.value.title = editTitle.value
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新标题失败')
    editTitle.value = task.value.title
  }
}

async function saveStatus() {
  if (!task.value) return
  try {
    await updateTaskStatus(task.value.id, { status: editStatus.value })
    task.value.status = editStatus.value
    emit('updated')
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新状态失败')
    editStatus.value = task.value.status
  }
}

async function savePriority() {
  if (!task.value || editPriority.value === task.value.priority) return
  try {
    await updateTask(task.value.id, { priority: editPriority.value })
    task.value.priority = editPriority.value
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新优先级失败')
    editPriority.value = task.value.priority
  }
}

async function saveAssignee() {
  if (!task.value) return
  try {
    await assignTask(task.value.id, { assigneeId: editAssigneeId.value })
    emit('updated')
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新负责人失败')
  }
}

async function saveDates() {
  if (!task.value) return
  try {
    await updateTask(task.value.id, {
      startDate: editStartDate.value?.format('YYYY-MM-DD'),
      dueDate: editDueDate.value?.format('YYYY-MM-DD')
    })
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新日期失败')
  }
}

async function saveDescription() {
  if (!task.value) return
  try {
    await updateTask(task.value.id, { description: editDescription.value || undefined })
    task.value.description = editDescription.value
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新描述失败')
  }
}

async function postComment() {
  if (!props.taskId || !newComment.value.trim()) return
  commentLoading.value = true
  try {
    const res = await createComment(props.taskId, { content: newComment.value })
    comments.value.push(res.data)
    newComment.value = ''
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '评论失败')
  } finally {
    commentLoading.value = false
  }
}

async function handleUpload(options: { file: File; onSuccess?: () => void; onError?: (err: Error) => void }) {
  if (!props.taskId) return
  try {
    const res = await uploadAttachment(props.taskId, options.file)
    attachments.value.push(res.data)
    options.onSuccess?.()
  } catch (err: unknown) {
    options.onError?.(err instanceof Error ? err : new Error('Upload failed'))
    message.error('上传失败')
  }
}

async function handleRemoveAttachment(file: UploadFile) {
  // Find attachment by file name match
  const att = attachments.value.find(a => a.fileName === file.name)
  if (att) {
    try {
      await deleteAttachment(att.id)
      attachments.value = attachments.value.filter(a => a.id !== att.id)
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : '删除失败')
    }
  }
}

async function handlePreviewAttachment(id: number) {
  try {
    const res = await previewAttachment(id)
    window.open(res.data, '_blank')
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '获取预览链接失败')
  }
}

async function handleDownloadAttachment(id: number, fileName: string) {
  try {
    const res = await downloadAttachment(id)
    const link = document.createElement('a')
    link.href = res.data
    link.download = fileName
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '获取下载链接失败')
  }
}

function handleClose() {
  emit('close')
}

function formatDateTime(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
</script>

<style scoped>
.drawer-content {
  padding: 16px 20px;
}

.drawer-header {
  margin-bottom: 20px;
}

.header-title-row {
  margin-bottom: 12px;
}

.title-input {
  font-size: 20px !important;
  font-weight: 700 !important;
  color: #2c3e50;
  padding: 0 !important;
}

.title-input :deep(.ant-input) {
  font-size: 20px;
  font-weight: 700;
}

.header-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.header-dates {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-label {
  font-size: 13px;
  color: #8c8c8c;
  white-space: nowrap;
}

.drawer-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.drawer-tabs {
  margin-top: 8px;
}

.comments-section {
  padding: 0;
}

.comment-input {
  margin-bottom: 16px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 10px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-user {
  font-weight: 500;
  font-size: 13px;
  color: #2c3e50;
}

.comment-time {
  font-size: 12px;
  color: #bfbfbf;
}

.comment-content {
  font-size: 13px;
  color: #595959;
  line-height: 1.6;
}

.attachments-section {
  padding: 0;
}

.attachment-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
  color: #595959;
}

.att-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.att-size {
  color: #bfbfbf;
  font-size: 12px;
}

.att-user {
  color: #8c8c8c;
  font-size: 12px;
}

.activity-section {
  padding: 0;
}

.activity-entry {
  font-size: 13px;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  flex-wrap: wrap;
}

.activity-user {
  font-weight: 500;
  color: #2c3e50;
}

.activity-detail {
  color: #8c8c8c;
}

.activity-ts {
  width: 100%;
  font-size: 11px;
  color: #bfbfbf;
  margin-top: 2px;
}
</style>
