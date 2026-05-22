import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Task, TaskCreateRequest, TaskUpdateRequest, TaskQueryRequest, PageResult } from '@/types'
import { listTasks as apiListTasks, createTask as apiCreateTask, updateTask as apiUpdateTask } from '@/api/task'

export const useTaskStore = defineStore('task', () => {
  const currentTask = ref<Task | null>(null)
  const taskFilters = ref<TaskQueryRequest>({})
  const tasks = ref<PageResult<Task> | null>(null)

  async function loadTasks(projectId: number, params?: TaskQueryRequest) {
    const res = await apiListTasks(projectId, params || taskFilters.value)
    tasks.value = res.data
    return res.data
  }

  async function createTask(projectId: number, data: TaskCreateRequest) {
    const res = await apiCreateTask(projectId, data)
    return res.data
  }

  async function updateTask(id: number, data: TaskUpdateRequest) {
    const res = await apiUpdateTask(id, data)
    return res.data
  }

  function setCurrentTask(task: Task | null) {
    currentTask.value = task
  }

  function setFilters(filters: TaskQueryRequest) {
    taskFilters.value = filters
  }

  return {
    currentTask,
    taskFilters,
    tasks,
    loadTasks,
    createTask,
    updateTask,
    setCurrentTask,
    setFilters
  }
})
