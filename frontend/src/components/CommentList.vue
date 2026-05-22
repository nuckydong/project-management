<template>
  <div class="comment-list">
    <div class="comment-header">
      <h4>评论 ({{ comments.length }})</h4>
    </div>

    <a-spin :spinning="loading">
      <div v-if="comments.length === 0 && !loading" class="empty-comments">
        <a-empty description="暂无评论" :image-style="{ height: '40px' }" />
      </div>

      <div class="comments-body" v-else>
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
        >
          <a-avatar :size="32" :style="{ backgroundColor: '#5B9BD5', flexShrink: 0 }">
            {{ comment.user?.username?.charAt(0).toUpperCase() || 'U' }}
          </a-avatar>
          <div class="comment-content">
            <div class="comment-meta">
              <span class="comment-user">{{ comment.user?.username || '未知用户' }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
              <span v-if="isOwnComment(comment)" class="comment-actions">
                <a-button
                  v-if="editingId !== comment.id"
                  type="link"
                  size="small"
                  @click="startEdit(comment)"
                >
                  编辑
                </a-button>
                <a-popconfirm title="确定删除此评论？" @confirm="handleDelete(comment.id)">
                  <a-button type="link" size="small" danger>删除</a-button>
                </a-popconfirm>
              </span>
            </div>
            <div v-if="editingId === comment.id" class="comment-edit">
              <a-textarea
                v-model:value="editContent"
                :rows="2"
                placeholder="编辑评论内容"
              />
              <div class="edit-actions">
                <a-button size="small" @click="cancelEdit">取消</a-button>
                <a-button
                  type="primary"
                  size="small"
                  :loading="submitting"
                  @click="handleUpdateComment"
                >
                  保存
                </a-button>
              </div>
            </div>
            <div v-else class="comment-text">{{ comment.content }}</div>
          </div>
        </div>
      </div>
    </a-spin>

    <div class="comment-input">
      <a-textarea
        v-model:value="newContent"
        :rows="3"
        placeholder="输入评论内容..."
        :maxlength="500"
        show-count
      />
      <a-button
        type="primary"
        :loading="submitting"
        :disabled="!newContent.trim()"
        @click="handleCreateComment"
        style="margin-top: 8px"
      >
        发表评论
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { listComments, createComment, updateComment, deleteComment } from '@/api/comment'
import { useAuthStore } from '@/stores/auth'
import type { Comment } from '@/types'
import dayjs from 'dayjs'

const props = defineProps<{
  taskId: number | null
}>()

const authStore = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const comments = ref<Comment[]>([])
const newContent = ref('')
const editingId = ref<number | null>(null)
const editContent = ref('')

watch(() => props.taskId, (newId) => {
  if (newId) {
    loadComments()
  } else {
    comments.value = []
  }
}, { immediate: true })

async function loadComments() {
  if (!props.taskId) return
  loading.value = true
  try {
    const res = await listComments(props.taskId)
    comments.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载评论失败')
  } finally {
    loading.value = false
  }
}

function isOwnComment(comment: Comment): boolean {
  return comment.user?.id === authStore.user?.id
}

function startEdit(comment: Comment) {
  editingId.value = comment.id
  editContent.value = comment.content
}

function cancelEdit() {
  editingId.value = null
  editContent.value = ''
}

async function handleCreateComment() {
  if (!props.taskId || !newContent.value.trim()) return
  submitting.value = true
  try {
    await createComment(props.taskId, { content: newContent.value.trim() })
    message.success('评论已发表')
    newContent.value = ''
    await loadComments()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '发表评论失败')
  } finally {
    submitting.value = false
  }
}

async function handleUpdateComment() {
  if (!editingId.value || !editContent.value.trim()) return
  submitting.value = true
  try {
    await updateComment(editingId.value, { content: editContent.value.trim() })
    message.success('评论已更新')
    editingId.value = null
    editContent.value = ''
    await loadComments()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新评论失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(commentId: number) {
  try {
    await deleteComment(commentId)
    message.success('评论已删除')
    await loadComments()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '删除评论失败')
  }
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-header h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #2c3e50;
}

.comments-body {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-meta {
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
  color: #8c8c8c;
}

.comment-actions {
  margin-left: auto;
}

.comment-text {
  font-size: 13px;
  color: #595959;
  line-height: 1.6;
  word-break: break-word;
}

.comment-edit {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.comment-input {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.empty-comments {
  padding: 16px 0;
}
</style>
