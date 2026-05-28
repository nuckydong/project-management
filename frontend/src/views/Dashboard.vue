<template>
  <div class="dashboard-page">
    <a-spin :spinning="loading">
      <!-- Stats Row -->
      <a-row :gutter="16" class="stats-row">
        <a-col :span="6">
          <a-card class="stat-card stat-todo" :bordered="false">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">待办</div>
                <div class="stat-value">{{ dashboard?.todoCount ?? 0 }}</div>
              </div>
              <div class="stat-icon stat-icon-blue">
                <FileTextOutlined />
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card stat-progress" :bordered="false">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">进行中</div>
                <div class="stat-value">{{ dashboard?.inProgressCount ?? 0 }}</div>
              </div>
              <div class="stat-icon stat-icon-orange">
                <SyncOutlined />
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card stat-review" :bordered="false">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">待审核</div>
                <div class="stat-value">{{ dashboard?.inReviewCount ?? 0 }}</div>
              </div>
              <div class="stat-icon stat-icon-purple">
                <AuditOutlined />
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card stat-done" :bordered="false">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">已完成</div>
                <div class="stat-value">{{ dashboard?.doneCount ?? 0 }}</div>
              </div>
              <div class="stat-icon stat-icon-green">
                <CheckCircleOutlined />
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- Main Content -->
      <a-row :gutter="16" class="content-row">
        <!-- Left Column: My Todo Tasks -->
        <a-col :span="14">
          <a-card title="我的待办" :bordered="false" class="content-card">
            <template #extra>
              <router-link to="/projects">
                <a-button type="link">查看全部</a-button>
              </router-link>
            </template>
            <div v-if="dashboard?.myTodoTasks?.length" class="todo-list">
              <div
                v-for="task in dashboard.myTodoTasks"
                :key="task.id"
                class="todo-item"
                @click="goToTask(task)"
              >
                <div class="todo-item-left">
                  <a-tag :color="priorityColor(task.priority)" size="small">
                    {{ priorityLabel(task.priority) }}
                  </a-tag>
                  <span class="todo-title">{{ task.title }}</span>
                </div>
                <div class="todo-item-right">
                  <span v-if="task.dueDate" class="todo-date" :class="{ overdue: isOverdue(task.dueDate) }">
                    {{ formatDate(task.dueDate) }}
                  </span>
                  <a-avatar v-if="task.assignee" :size="24" :style="{ backgroundColor: '#5B9BD5', fontSize: '12px' }">
                    {{ task.assignee.username.charAt(0).toUpperCase() }}
                  </a-avatar>
                </div>
              </div>
            </div>
            <a-empty v-else description="暂无待办任务" />
          </a-card>
        </a-col>

        <!-- Right Column -->
        <a-col :span="10">
          <!-- Project Progress -->
          <a-card title="项目进度" :bordered="false" class="content-card progress-card">
            <div v-if="dashboard?.projects?.length">
              <div v-for="proj in dashboard.projects" :key="proj.projectId" class="project-progress-item">
                <div class="project-progress-header">
                  <span class="project-name" @click="goToProject(proj.projectId)">{{ proj.projectName }}</span>
                  <span class="project-count">{{ proj.doneTasks }}/{{ proj.totalTasks }}</span>
                </div>
                <a-progress
                  :percent="proj.progress"
                  :stroke-color="'#5B9BD5'"
                  :show-info="true"
                  size="small"
                />
              </div>
            </div>
            <a-empty v-else description="暂无项目" />
          </a-card>

          <!-- Recent Activities -->
          <a-card title="近期活动" :bordered="false" class="content-card activity-card">
            <template #extra>
              <div class="activity-filters">
                <a-select
                  v-model:value="activityProjectFilter"
                  placeholder="全部项目"
                  style="width: 120px"
                  allow-clear
                  size="small"
                >
                  <a-select-option v-for="proj in dashboard?.projects" :key="proj.projectId" :value="proj.projectId">
                    {{ proj.projectName }}
                  </a-select-option>
                </a-select>
                <a-select
                  v-model:value="activityTaskFilter"
                  placeholder="全部任务"
                  style="width: 140px"
                  allow-clear
                  size="small"
                  show-search
                  :filter-option="filterTask"
                >
                  <a-select-option v-for="t in filteredTasks" :key="t.id" :value="t.id">
                    {{ t.title }}
                  </a-select-option>
                </a-select>
              </div>
            </template>
            <div v-if="filteredActivities.length" class="activity-list">
              <div
                v-for="activity in filteredActivities"
                :key="activity.id"
                class="activity-entry"
              >
                <a-avatar :size="24" :style="{ backgroundColor: '#5B9BD5', fontSize: '11px', flexShrink: 0 }">
                  {{ activity.user.username.charAt(0).toUpperCase() }}
                </a-avatar>
                <div class="activity-body">
                  <div class="activity-main">
                    <span class="activity-user">{{ activity.user.username }}</span>
                    <span class="activity-action">{{ activity.detail || activity.action }}</span>
                  </div>
                  <div class="activity-meta">
                    <span v-if="activity.projectName" class="activity-project" @click="goToProject(activity.projectId)">
                      {{ activity.projectName }}
                    </span>
                    <span v-if="activity.taskTitle" class="activity-task" @click="goToActivityTask(activity)">
                      / {{ activity.taskTitle }}
                    </span>
                  </div>
                </div>
                <span class="activity-time">{{ formatRelativeTime(activity.createdAt) }}</span>
              </div>
            </div>
            <a-empty v-else description="暂无活动" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined,
  SyncOutlined,
  AuditOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import { getDashboard } from '@/api/report'
import { listTasks } from '@/api/task'
import type { Dashboard, Task } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const dashboard = ref<Dashboard | null>(null)
const activityProjectFilter = ref<number | undefined>(undefined)
const activityTaskFilter = ref<number | undefined>(undefined)
const projectTasks = ref<Task[]>([])

const filteredActivities = computed(() => {
  let list = dashboard.value?.recentActivities || []
  if (activityProjectFilter.value) {
    list = list.filter(a => a.projectId === activityProjectFilter.value)
  }
  if (activityTaskFilter.value) {
    list = list.filter(a => a.taskId === activityTaskFilter.value)
  }
  return list
})

const filteredTasks = computed(() => {
  return projectTasks.value
})

function filterTask(input: string, option: any) {
  const task = projectTasks.value.find(t => t.id === option.value)
  if (!task) return false
  return task.title.toLowerCase().includes(input.toLowerCase())
}

watch(activityProjectFilter, async (projectId) => {
  activityTaskFilter.value = undefined
  projectTasks.value = []
  if (projectId) {
    try {
      const res = await listTasks(projectId, { pageSize: 200 })
      projectTasks.value = res.data.list
    } catch { /* ignore */ }
  }
})

// Auto-select first project on load
watch(() => dashboard.value?.projects, (projects) => {
  if (projects?.length && !activityProjectFilter.value) {
    activityProjectFilter.value = projects[0].projectId
  }
}, { immediate: true })

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    dashboard.value = res.data
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '加载仪表盘失败'
    message.error(msg)
  } finally {
    loading.value = false
  }
})

function priorityColor(priority: string): string {
  const map: Record<string, string> = {
    LOW: 'blue',
    MEDIUM: 'default',
    HIGH: 'orange',
    URGENT: 'red'
  }
  return map[priority] || 'default'
}

function priorityLabel(priority: string): string {
  const map: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[priority] || priority
}

function isOverdue(dateStr: string): boolean {
  return dayjs(dateStr).isBefore(dayjs(), 'day')
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('MM-DD')
}

function goToTask(task: { id: number; projectId: number }) {
  router.push({ name: 'ProjectDetail', params: { id: task.projectId }, query: { taskId: task.id } })
}

function goToProject(projectId: number) {
  router.push({ name: 'ProjectDetail', params: { id: projectId } })
}

function goToActivityTask(activity: { projectId: number; taskId: number | null }) {
  if (!activity.taskId) return
  router.push({ name: 'ProjectDetail', params: { id: activity.projectId }, query: { taskId: String(activity.taskId) } })
}

function formatRelativeTime(dateStr: string): string {
  const now = dayjs()
  const target = dayjs(dateStr)
  const diffMin = now.diff(target, 'minute')
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = now.diff(target, 'hour')
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = now.diff(target, 'day')
  if (diffDay < 7) return `${diffDay}天前`
  return target.format('MM-DD HH:mm')
}
</script>

<style scoped>
.dashboard-page {
  background: #f0f5ff;
  min-height: 100%;
  margin: -24px;
  padding: 24px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon-blue {
  background: #e6f4ff;
  color: #1677ff;
}

.stat-icon-orange {
  background: #fff7e6;
  color: #fa8c16;
}

.stat-icon-purple {
  background: #f9f0ff;
  color: #722ed1;
}

.stat-icon-green {
  background: #f6ffed;
  color: #52c41a;
}

.content-row {
  margin-top: 0;
}

.content-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.todo-list {
  display: flex;
  flex-direction: column;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}

.todo-item:hover {
  background: #f0f5ff;
  margin: 0 -24px;
  padding: 10px 24px;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 14px;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.todo-date {
  font-size: 12px;
  color: #8c8c8c;
}

.todo-date.overdue {
  color: #ff4d4f;
}

.progress-card {
  margin-bottom: 16px;
}

.project-progress-item {
  margin-bottom: 12px;
}

.project-progress-item:last-child {
  margin-bottom: 0;
}

.project-progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.project-name {
  font-size: 14px;
  color: #2c3e50;
  cursor: pointer;
}

.project-name:hover {
  color: #5b9bd5;
}

.project-count {
  font-size: 12px;
  color: #8c8c8c;
}

.activity-card {
  margin-bottom: 0;
}

.activity-filters {
  display: flex;
  gap: 8px;
}

.activity-list {
  display: flex;
  flex-direction: column;
}

.activity-entry {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.activity-entry:last-child {
  border-bottom: none;
}

.activity-body {
  flex: 1;
  min-width: 0;
}

.activity-main {
  font-size: 13px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.activity-user {
  font-weight: 500;
  color: #2c3e50;
  margin-right: 4px;
}

.activity-action {
  color: #595959;
}

.activity-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-project {
  color: #1677ff;
  cursor: pointer;
}

.activity-project:hover {
  text-decoration: underline;
}

.activity-task {
  color: #8c8c8c;
  cursor: pointer;
}

.activity-task:hover {
  color: #1677ff;
}

.activity-time {
  font-size: 12px;
  color: #bfbfbf;
  white-space: nowrap;
  flex-shrink: 0;
  margin-top: 2px;
}
</style>
