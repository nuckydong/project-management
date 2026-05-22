<template>
  <div class="calendar-view">
    <a-spin :spinning="loading">
      <a-calendar v-model:value="currentMonth" @select="onDateSelect">
        <template #dateCellRender="{ current }">
          <div class="date-cell-content">
            <div
              v-for="task in getTasksForDate(current)"
              :key="task.id"
              class="calendar-task-dot"
              :class="'dot-' + statusDotColor(task.status)"
              @click.stop="$emit('taskClick', task.id)"
              :title="task.title"
            >
              <span class="dot-label">{{ task.title }}</span>
            </div>
          </div>
        </template>
      </a-calendar>
    </a-spin>

    <!-- Day Popover -->
    <a-modal
      v-model:open="dayModalVisible"
      :title="selectedDateLabel"
      :footer="null"
      width="480px"
    >
      <div v-if="selectedDayTasks.length">
        <div
          v-for="task in selectedDayTasks"
          :key="task.id"
          class="day-task-item"
          @click="$emit('taskClick', task.id); dayModalVisible = false"
        >
          <a-tag :color="statusColor(task.status)" size="small">{{ statusLabel(task.status) }}</a-tag>
          <span class="day-task-title">{{ task.title }}</span>
          <a-tag :color="priorityColor(task.priority)" size="small">{{ priorityLabel(task.priority) }}</a-tag>
        </div>
      </div>
      <a-empty v-else description="当日无任务" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { listTasks } from '@/api/task'
import type { Task } from '@/types'
import dayjs, { type Dayjs } from 'dayjs'

const props = defineProps<{
  projectId: number
  sprintId?: number
}>()

defineEmits<{
  taskClick: [taskId: number]
}>()

const loading = ref(false)
const tasks = ref<Task[]>([])
const currentMonth = ref<Dayjs>(dayjs())
const dayModalVisible = ref(false)
const selectedDate = ref<Dayjs>(dayjs())

// Build a map of due_date => Task[]
const taskDateMap = computed(() => {
  const map = new Map<string, Task[]>()
  tasks.value.forEach(task => {
    if (task.dueDate) {
      const key = dayjs(task.dueDate).format('YYYY-MM-DD')
      const list = map.get(key) || []
      list.push(task)
      map.set(key, list)
    }
  })
  return map
})

const selectedDateLabel = computed(() => {
  return selectedDate.value.format('YYYY年MM月DD日')
})

const selectedDayTasks = computed(() => {
  const key = selectedDate.value.format('YYYY-MM-DD')
  return taskDateMap.value.get(key) || []
})

onMounted(() => {
  loadTasks()
})

watch(() => [props.projectId, props.sprintId], () => {
  loadTasks()
})

async function loadTasks() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {}
    if (props.sprintId) {
      params.sprintId = props.sprintId
    }
    const res = await listTasks(props.projectId, { ...params, pageSize: 500 })
    tasks.value = res.data.list
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载任务失败')
  } finally {
    loading.value = false
  }
}

function getTasksForDate(date: Dayjs): Task[] {
  const key = date.format('YYYY-MM-DD')
  return taskDateMap.value.get(key) || []
}

function onDateSelect(date: Dayjs) {
  selectedDate.value = date
  dayModalVisible.value = true
}

function statusDotColor(status: string): string {
  const map: Record<string, string> = { TODO: 'blue', IN_PROGRESS: 'orange', IN_REVIEW: 'purple', DONE: 'green' }
  return map[status] || 'gray'
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
</script>

<style scoped>
.calendar-view {
  padding: 0;
}

.calendar-view :deep(.ant-picker-calendar) {
  background: #fff;
}

.date-cell-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.calendar-task-dot {
  display: flex;
  align-items: center;
  padding: 1px 4px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 11px;
  overflow: hidden;
  max-height: 18px;
}

.calendar-task-dot:hover {
  opacity: 0.8;
}

.dot-blue {
  background: #e6f4ff;
  color: #1677ff;
}

.dot-orange {
  background: #fff7e6;
  color: #fa8c16;
}

.dot-purple {
  background: #f9f0ff;
  color: #722ed1;
}

.dot-green {
  background: #f6ffed;
  color: #52c41a;
}

.dot-gray {
  background: #f5f5f5;
  color: #8c8c8c;
}

.dot-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.day-task-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}

.day-task-item:hover {
  background: #f0f5ff;
}

.day-task-item:last-child {
  border-bottom: none;
}

.day-task-title {
  flex: 1;
  font-size: 14px;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
