<template>
  <div class="task-card" @click="$emit('click', task.id)">
    <!-- Overdue alert -->
    <div v-if="isOverdue" class="overdue-alert">
      <ExclamationCircleFilled />
    </div>
    <div class="task-card-header">
      <span class="task-title">{{ task.title }}</span>
    </div>
    <div class="task-card-tags" v-if="task.tags?.length">
      <a-tag v-for="tag in task.tags" :key="tag.id" :color="tag.color" size="small" style="margin: 0 2px">
        {{ tag.name }}
      </a-tag>
    </div>
    <div class="task-card-footer">
      <div class="task-card-left">
        <span class="priority-dot" :class="'priority-' + task.priority.toLowerCase()"></span>
        <span v-if="task.dueDate" class="task-due" :class="{ overdue: isOverdue }">
          {{ formattedDueDate }}
        </span>
      </div>
      <a-avatar
        v-if="task.assignee"
        :size="24"
        :style="{ backgroundColor: '#5B9BD5', fontSize: '11px' }"
      >
        {{ task.assignee.username.charAt(0).toUpperCase() }}
      </a-avatar>
    </div>
    <div class="task-card-progress">
      <ProgressSlider :progress="task.progress ?? 0" size="mini" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ExclamationCircleFilled } from '@ant-design/icons-vue'
import ProgressSlider from './ProgressSlider.vue'
import type { Task } from '@/types'
import dayjs from 'dayjs'

const props = defineProps<{
  task: Task
}>()

defineEmits<{
  click: [taskId: number]
}>()

const isOverdue = computed(() => {
  if (!props.task.dueDate) return false
  if (props.task.status === 'DONE') return false
  return dayjs(props.task.dueDate).isBefore(dayjs(), 'day') && (props.task.progress ?? 0) < 100
})

const formattedDueDate = computed(() => {
  if (!props.task.dueDate) return ''
  return dayjs(props.task.dueDate).format('MM-DD')
})
</script>

<style scoped>
.task-card {
  position: relative;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 10px 12px;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
  margin-bottom: 8px;
}

.task-card:hover {
  box-shadow: 0 2px 8px rgba(91, 155, 213, 0.15);
  border-color: #5b9bd5;
}

.task-card-header {
  margin-bottom: 6px;
}

.task-title {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-card-tags {
  margin-bottom: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.task-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.task-card-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-card-progress {
  margin-top: 6px;
}

.priority-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.priority-low {
  background: #1677ff;
}

.priority-medium {
  background: #8c8c8c;
}

.priority-high {
  background: #fa8c16;
}

.priority-urgent {
  background: #ff4d4f;
}

.task-due {
  font-size: 12px;
  color: #8c8c8c;
}

.task-due.overdue {
  color: #ff4d4f;
  font-weight: 500;
}

/* Overdue alert */
.overdue-alert {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  background: #ff4d4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  animation: pulse-alert 2s infinite;
  z-index: 1;
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.4);
}

@keyframes pulse-alert {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
