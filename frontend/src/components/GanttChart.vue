<template>
  <div class="gantt-chart">
    <a-spin :spinning="loading">
      <div v-if="tasks.length" class="gantt-container">
        <!-- Left panel: task list -->
        <div class="gantt-left">
          <div class="gantt-header-cell">任务名称</div>
          <div
            v-for="task in tasks"
            :key="task.id"
            class="gantt-task-name"
            :style="{ paddingLeft: (task.parentId ? 24 : 8) + 'px' }"
            @click="$emit('taskClick', task.id)"
          >
            <span class="task-name-text">{{ task.title }}</span>
          </div>
        </div>

        <!-- Right panel: timeline -->
        <div class="gantt-right" ref="ganttRightRef">
          <!-- Date headers -->
          <div class="gantt-timeline-header">
            <div
              v-for="date in dateColumns"
              :key="date.key"
              class="gantt-date-cell"
              :class="{ 'is-today': date.isToday, 'is-weekend': date.isWeekend }"
            >
              <div class="date-label">{{ date.label }}</div>
            </div>
          </div>

          <!-- Task rows -->
          <div class="gantt-body">
            <div
              v-for="task in tasks"
              :key="task.id"
              class="gantt-row"
            >
              <div class="gantt-row-bg">
                <div
                  v-for="date in dateColumns"
                  :key="date.key"
                  class="gantt-cell"
                  :class="{ 'is-today': date.isToday, 'is-weekend': date.isWeekend }"
                ></div>
              </div>
              <div
                v-if="task.startDate && task.dueDate"
                class="gantt-bar"
                :class="'bar-' + barColor(task.status)"
                :style="barStyle(task)"
                @click="$emit('taskClick', task.id)"
                :title="task.title"
              >
                <span class="bar-label">{{ task.title }}</span>
              </div>
            </div>
          </div>

          <!-- Today line -->
          <div v-if="todayOffset >= 0" class="today-line" :style="{ left: todayOffset + 'px' }"></div>
        </div>
      </div>
      <a-empty v-else description="暂无任务" />
    </a-spin>
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

const CELL_WIDTH = 36

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

interface DateColumn {
  key: string
  date: Dayjs
  label: string
  isToday: boolean
  isWeekend: boolean
}

const dateRange = computed(() => {
  const allDates: Dayjs[] = []
  tasks.value.forEach(t => {
    if (t.startDate) allDates.push(dayjs(t.startDate))
    if (t.dueDate) allDates.push(dayjs(t.dueDate))
  })
  if (allDates.length === 0) {
    const today = dayjs()
    return { start: today.subtract(7, 'day'), end: today.add(14, 'day') }
  }
  allDates.sort((a, b) => a.valueOf() - b.valueOf())
  return {
    start: allDates[0].subtract(3, 'day'),
    end: allDates[allDates.length - 1].add(3, 'day')
  }
})

const dateColumns = computed((): DateColumn[] => {
  const { start, end } = dateRange.value
  const cols: DateColumn[] = []
  let current = start
  while (current.isBefore(end) || current.isSame(end, 'day')) {
    cols.push({
      key: current.format('YYYY-MM-DD'),
      date: current,
      label: current.format('MM/DD'),
      isToday: current.isSame(dayjs(), 'day'),
      isWeekend: current.day() === 0 || current.day() === 6
    })
    current = current.add(1, 'day')
  }
  return cols
})

const todayOffset = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  const idx = dateColumns.value.findIndex(d => d.key === today)
  return idx >= 0 ? idx * CELL_WIDTH + Math.floor(CELL_WIDTH / 2) : -1
})

function barStyle(task: Task): Record<string, string> {
  if (!task.startDate || !task.dueDate) return {}
  const start = dayjs(task.startDate)
  const end = dayjs(task.dueDate)
  const rangeStart = dateRange.value.start
  const startOffset = start.diff(rangeStart, 'day')
  const duration = end.diff(start, 'day') + 1
  const left = Math.max(0, startOffset) * CELL_WIDTH
  const width = Math.max(CELL_WIDTH, duration * CELL_WIDTH)
  return {
    left: left + 'px',
    width: width + 'px'
  }
}

function barColor(status: string): string {
  const map: Record<string, string> = {
    TODO: 'todo',
    IN_PROGRESS: 'progress',
    IN_REVIEW: 'review',
    DONE: 'done'
  }
  return map[status] || 'todo'
}
</script>

<style scoped>
.gantt-chart {
  width: 100%;
  overflow: hidden;
}

.gantt-container {
  display: flex;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  overflow: hidden;
}

.gantt-left {
  width: 220px;
  flex-shrink: 0;
  border-right: 2px solid #e8e8e8;
  z-index: 2;
  background: #fff;
}

.gantt-header-cell {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  font-weight: 600;
  font-size: 13px;
  color: #2c3e50;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.gantt-task-name {
  height: 36px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}

.gantt-task-name:hover {
  background: #f0f5ff;
}

.task-name-text {
  font-size: 13px;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gantt-right {
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
  position: relative;
}

.gantt-timeline-header {
  display: flex;
  height: 40px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 1;
}

.gantt-date-cell {
  width: 36px;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #f0f0f0;
  font-size: 11px;
  color: #8c8c8c;
}

.gantt-date-cell.is-weekend {
  background: #fafafa;
}

.gantt-date-cell.is-today {
  background: #e6f4ff;
  color: #1677ff;
  font-weight: 600;
}

.date-label {
  writing-mode: horizontal-tb;
  font-size: 10px;
}

.gantt-body {
  position: relative;
}

.gantt-row {
  position: relative;
  height: 36px;
  border-bottom: 1px solid #f0f0f0;
}

.gantt-row-bg {
  display: flex;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.gantt-cell {
  width: 36px;
  min-width: 36px;
  border-right: 1px solid #f5f5f5;
}

.gantt-cell.is-weekend {
  background: #fafafa;
}

.gantt-cell.is-today {
  background: rgba(230, 244, 255, 0.5);
}

.gantt-bar {
  position: absolute;
  top: 4px;
  height: 28px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  padding: 0 8px;
  cursor: pointer;
  z-index: 1;
  transition: opacity 0.15s;
  overflow: hidden;
}

.gantt-bar:hover {
  opacity: 0.85;
}

.bar-todo {
  background: #e6f4ff;
  border: 1px solid #91caff;
  color: #1677ff;
}

.bar-progress {
  background: #fff7e6;
  border: 1px solid #ffd591;
  color: #d46b08;
}

.bar-review {
  background: #f9f0ff;
  border: 1px solid #d3adf7;
  color: #722ed1;
}

.bar-done {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #389e0d;
}

.bar-label {
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.today-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #ff4d4f;
  z-index: 3;
  pointer-events: none;
}
</style>
