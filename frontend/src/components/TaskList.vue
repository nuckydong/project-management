<template>
  <div class="task-list-view">
    <!-- Batch Actions Toolbar -->
    <div class="batch-toolbar" v-if="selectedRowKeys.length">
      <span class="batch-info">已选择 {{ selectedRowKeys.length }} 项</span>
      <a-button size="small" @click="batchChangeStatus('TODO')">设为待办</a-button>
      <a-button size="small" @click="batchChangeStatus('IN_PROGRESS')">设为进行中</a-button>
      <a-button size="small" @click="batchChangeStatus('DONE')">设为已完成</a-button>
      <a-button size="small" type="primary" danger @click="batchDelete">删除</a-button>
      <a-button size="small" @click="selectedRowKeys = []">取消选择</a-button>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <a-input-search
        v-model:value="filters.keyword"
        placeholder="搜索任务"
        style="width: 200px"
        @search="loadTasks"
        allow-clear
        size="small"
      />
      <a-select v-model:value="filters.status" placeholder="状态" style="width: 120px" allow-clear size="small" @change="loadTasks">
        <a-select-option value="TODO">待办</a-select-option>
        <a-select-option value="IN_PROGRESS">进行中</a-select-option>
        <a-select-option value="IN_REVIEW">待审核</a-select-option>
        <a-select-option value="DONE">已完成</a-select-option>
      </a-select>
      <a-select v-model:value="filters.priority" placeholder="优先级" style="width: 120px" allow-clear size="small" @change="loadTasks">
        <a-select-option value="LOW">低</a-select-option>
        <a-select-option value="MEDIUM">中</a-select-option>
        <a-select-option value="HIGH">高</a-select-option>
        <a-select-option value="URGENT">紧急</a-select-option>
      </a-select>
    </div>

    <a-table
      :columns="columns"
      :data-source="tasks"
      :loading="loading"
      :row-key="(record: Task) => record.id"
      :row-selection="{ selectedRowKeys, onChange: onSelectionChange }"
      :pagination="pagination"
      @change="onTableChange"
      :scroll="{ x: 900 }"
      size="middle"
      :custom-row="customRow"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'title'">
          <span class="task-title-cell">{{ record.title }}</span>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-if="column.key === 'priority'">
          <a-tag :color="priorityColor(record.priority)">{{ priorityLabel(record.priority) }}</a-tag>
        </template>
        <template v-if="column.key === 'assignee'">
          <div v-if="record.assignee" class="assignee-cell">
            <a-avatar :size="22" :style="{ backgroundColor: '#5B9BD5', fontSize: '11px' }">
              {{ record.assignee.username.charAt(0).toUpperCase() }}
            </a-avatar>
            <span>{{ record.assignee.username }}</span>
          </div>
          <span v-else class="unassigned">未分配</span>
        </template>
        <template v-if="column.key === 'dueDate'">
          <span v-if="record.dueDate" :class="{ overdue: isOverdue(record.dueDate) }">
            {{ formatDate(record.dueDate) }}
          </span>
        </template>
        <template v-if="column.key === 'tags'">
          <a-tag v-for="tag in (record.tags || [])" :key="tag.id" :color="tag.color" size="small" style="margin: 0 2px">
            {{ tag.name }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { listTasks, deleteTask, batchTasks } from '@/api/task'
import type { Task, TaskQueryRequest } from '@/types'
import dayjs from 'dayjs'

const props = defineProps<{
  projectId: number
  sprintId?: number
}>()

const emit = defineEmits<{
  taskClick: [taskId: number]
}>()

const loading = ref(false)
const tasks = ref<Task[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])

const filters = reactive<TaskQueryRequest>({
  status: undefined,
  priority: undefined,
  keyword: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 项`
})

const columns = [
  { title: '任务标题', dataIndex: 'title', key: 'title', width: 280, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 90 },
  { title: '负责人', dataIndex: 'assignee', key: 'assignee', width: 130 },
  { title: '截止日期', dataIndex: 'dueDate', key: 'dueDate', width: 110, sorter: true },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 150 }
]

onMounted(() => {
  loadTasks()
})

watch(() => [props.projectId, props.sprintId], () => {
  pagination.current = 1
  loadTasks()
})

async function loadTasks() {
  loading.value = true
  try {
    const params: TaskQueryRequest = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      ...filters
    }
    if (props.sprintId) {
      params.sprintId = props.sprintId
    }
    const res = await listTasks(props.projectId, params)
    tasks.value = res.data.list
    pagination.total = res.data.total
    total.value = res.data.total
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function onTableChange(pag: { current: number; pageSize: number }) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTasks()
}

function onSelectionChange(keys: number[]) {
  selectedRowKeys.value = keys
}

function customRow(record: Task) {
  return {
    onClick: () => {
      emit('taskClick', record.id)
    },
    style: { cursor: 'pointer' }
  }
}

async function batchChangeStatus(status: string) {
  try {
    await batchTasks({ taskIds: selectedRowKeys.value, action: 'STATUS', value: status })
    message.success('状态已更新')
    selectedRowKeys.value = []
    await loadTasks()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '操作失败')
  }
}

async function batchDelete() {
  try {
    for (const id of selectedRowKeys.value) {
      await deleteTask(id)
    }
    message.success('已删除')
    selectedRowKeys.value = []
    await loadTasks()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '删除失败')
  }
}

function statusColor(status: string): string {
  const map: Record<string, string> = { TODO: 'blue', IN_PROGRESS: 'orange', IN_REVIEW: 'purple', DONE: 'green' }
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
</script>

<style scoped>
.task-list-view {
  padding: 0;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #e6f4ff;
  border-radius: 6px;
  margin-bottom: 12px;
}

.batch-info {
  font-size: 13px;
  color: #1677ff;
  font-weight: 500;
  margin-right: 8px;
}

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.task-title-cell {
  color: #2c3e50;
  font-weight: 500;
}

.assignee-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.unassigned {
  color: #bfbfbf;
  font-size: 13px;
}

.overdue {
  color: #ff4d4f;
  font-weight: 500;
}
</style>
