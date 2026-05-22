<template>
  <div class="kanban-board">
    <a-spin :spinning="loading">
      <div class="kanban-columns">
        <div
          v-for="col in columns"
          :key="col.status"
          class="kanban-column"
          @dragover.prevent="onDragOver($event, col.status)"
          @drop="onDrop($event, col.status)"
          :class="{ 'drag-over': dragOverColumn === col.status }"
        >
          <div class="column-header" :style="{ borderTopColor: col.color }">
            <span class="column-title">{{ col.label }}</span>
            <a-badge :count="getTasksByStatus(col.status).length" :number-style="{ backgroundColor: col.color }" />
          </div>
          <div class="column-body">
            <TaskCard
              v-for="task in getTasksByStatus(col.status)"
              :key="task.id"
              :task="task"
              :draggable="true"
              @click="$emit('taskClick', task.id)"
              @dragstart="onDragStart($event, task)"
            />
            <div v-if="!getTasksByStatus(col.status).length" class="column-empty">
              暂无任务
            </div>
            <div class="quick-add" @click="quickAdd(col.status)">
              <PlusOutlined /> 添加任务
            </div>
          </div>
        </div>
      </div>
    </a-spin>

    <!-- Quick Add Modal -->
    <a-modal
      v-model:open="quickAddVisible"
      title="快速添加任务"
      @ok="handleQuickAdd"
      :confirm-loading="quickAddLoading"
    >
      <a-input
        v-model:value="quickAddTitle"
        placeholder="任务标题"
        style="margin-top: 16px"
        @pressEnter="handleQuickAdd"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { listTasks, createTask, updateTaskStatus } from '@/api/task'
import type { Task } from '@/types'
import TaskCard from './TaskCard.vue'

const props = defineProps<{
  projectId: number
  sprintId?: number
}>()

defineEmits<{
  taskClick: [taskId: number]
}>()

const loading = ref(false)
const tasks = ref<Task[]>([])
const draggedTask = ref<Task | null>(null)
const dragOverColumn = ref<string | null>(null)

const quickAddVisible = ref(false)
const quickAddTitle = ref('')
const quickAddStatus = ref('TODO')
const quickAddLoading = ref(false)

const columns = [
  { status: 'TODO', label: '待办', color: '#1677ff' },
  { status: 'IN_PROGRESS', label: '进行中', color: '#fa8c16' },
  { status: 'IN_REVIEW', label: '待审核', color: '#722ed1' },
  { status: 'DONE', label: '已完成', color: '#52c41a' }
]

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
    const res = await listTasks(props.projectId, params)
    tasks.value = res.data.list
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载任务失败')
  } finally {
    loading.value = false
  }
}

function getTasksByStatus(status: string): Task[] {
  return tasks.value.filter(t => t.status === status)
}

function onDragStart(_event: DragEvent, task: Task) {
  draggedTask.value = task
}

function onDragOver(_event: DragEvent, status: string) {
  dragOverColumn.value = status
}

async function onDrop(_event: DragEvent, newStatus: string) {
  dragOverColumn.value = null
  if (!draggedTask.value || draggedTask.value.status === newStatus) {
    draggedTask.value = null
    return
  }
  const task = draggedTask.value
  const oldStatus = task.status
  // Optimistic update
  task.status = newStatus
  try {
    await updateTaskStatus(task.id, { status: newStatus })
  } catch (err: unknown) {
    task.status = oldStatus
    message.error(err instanceof Error ? err.message : '更新状态失败')
  }
  draggedTask.value = null
}

function quickAdd(status: string) {
  quickAddStatus.value = status
  quickAddTitle.value = ''
  quickAddVisible.value = true
}

async function handleQuickAdd() {
  if (!quickAddTitle.value.trim()) {
    message.warning('请输入任务标题')
    return
  }
  quickAddLoading.value = true
  try {
    await createTask(props.projectId, {
      title: quickAddTitle.value,
      sprintId: props.sprintId
    })
    // After creating, move it to the target column status
    quickAddVisible.value = false
    await loadTasks()
    // Find the newly created task and update its status
    const created = tasks.value.find(t => t.title === quickAddTitle.value && t.status === 'TODO')
    if (created && quickAddStatus.value !== 'TODO') {
      await updateTaskStatus(created.id, { status: quickAddStatus.value })
      await loadTasks()
    }
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '创建失败')
  } finally {
    quickAddLoading.value = false
  }
}
</script>

<style scoped>
.kanban-board {
  min-height: 400px;
}

.kanban-columns {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.kanban-column {
  flex: 1;
  min-width: 260px;
  max-width: 320px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  border-top: 3px solid transparent;
  transition: background 0.2s;
}

.kanban-column.drag-over {
  background: #e6f4ff;
}

.column-header {
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 3px solid;
  border-radius: 8px 8px 0 0;
  margin-top: -3px;
}

.column-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.column-body {
  padding: 8px;
  flex: 1;
  min-height: 200px;
}

.column-empty {
  text-align: center;
  color: #bfbfbf;
  padding: 20px 0;
  font-size: 13px;
}

.quick-add {
  padding: 8px;
  text-align: center;
  color: #8c8c8c;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
  margin-top: 4px;
  transition: all 0.2s;
}

.quick-add:hover {
  background: #e8e8e8;
  color: #5b9bd5;
}
</style>
