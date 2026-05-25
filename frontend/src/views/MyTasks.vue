<template>
  <div class="my-tasks-page">
    <div class="page-header">
      <h2>我的任务</h2>
      <div class="header-actions">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索任务..."
          style="width: 240px"
          @search="loadTasks"
          allow-clear
        />
        <a-select v-model:value="statusFilter" style="width: 140px" @change="loadTasks" allow-clear placeholder="全部状态">
          <a-select-option value="TODO">待办</a-select-option>
          <a-select-option value="IN_PROGRESS">进行中</a-select-option>
          <a-select-option value="IN_REVIEW">待审核</a-select-option>
          <a-select-option value="DONE">已完成</a-select-option>
        </a-select>
      </div>
    </div>

    <a-spin :spinning="loading">
      <div v-if="tasks.length" class="task-list">
        <div
          v-for="task in tasks"
          :key="task.id"
          class="task-item"
          @click="goToTask(task)"
        >
          <div class="task-item-left">
            <a-tag :color="statusColor(task.status)" size="small">
              {{ statusLabel(task.status) }}
            </a-tag>
            <a-tag :color="priorityColor(task.priority)" size="small">
              {{ priorityLabel(task.priority) }}
            </a-tag>
            <span class="task-title">{{ task.title }}</span>
          </div>
          <div class="task-item-right">
            <span v-if="task.dueDate" class="task-date" :class="{ overdue: isOverdue(task.dueDate) }">
              {{ formatDate(task.dueDate) }}
            </span>
            <a-avatar v-if="task.assignee" :size="24" :style="{ backgroundColor: '#5B9BD5', fontSize: '12px' }">
              {{ task.assignee.username.charAt(0).toUpperCase() }}
            </a-avatar>
          </div>
        </div>
      </div>
      <a-empty v-else description="暂无任务" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listMyTasks } from '@/api/task'
import type { Task } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const tasks = ref<Task[]>([])
const keyword = ref('')
const statusFilter = ref<string | undefined>(undefined)

onMounted(() => {
  loadTasks()
})

async function loadTasks() {
  loading.value = true
  try {
    const res = await listMyTasks({ status: statusFilter.value, keyword: keyword.value || undefined })
    tasks.value = res.data
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '加载任务失败'
    message.error(msg)
  } finally {
    loading.value = false
  }
}

function statusColor(status: string): string {
  const map: Record<string, string> = { TODO: 'default', IN_PROGRESS: 'processing', IN_REVIEW: 'warning', DONE: 'success' }
  return map[status] || 'default'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { TODO: '待办', IN_PROGRESS: '进行中', IN_REVIEW: '待审核', DONE: '已完成' }
  return map[status] || status
}

function priorityColor(priority: string): string {
  const map: Record<string, string> = { LOW: 'blue', MEDIUM: 'default', HIGH: 'orange', URGENT: 'red' }
  return map[priority] || 'default'
}

function priorityLabel(priority: string): string {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', URGENT: '紧急' }
  return map[priority] || priority
}

function isOverdue(dateStr: string): boolean {
  return dayjs(dateStr).isBefore(dayjs(), 'day')
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD')
}

function goToTask(task: { id: number; projectId: number }) {
  router.push({ name: 'ProjectDetail', params: { id: task.projectId }, query: { taskId: String(task.id) } })
}
</script>

<style scoped>
.my-tasks-page {
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.task-list {
  display: flex;
  flex-direction: column;
}

.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 6px;
}

.task-item:hover {
  background: #f5f8fc;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.task-title {
  font-size: 14px;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.task-date {
  font-size: 12px;
  color: #8c8c8c;
}

.task-date.overdue {
  color: #ff4d4f;
}
</style>
