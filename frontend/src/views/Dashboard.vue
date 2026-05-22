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
            <div v-if="dashboard?.recentActivities?.length">
              <a-timeline>
                <a-timeline-item
                  v-for="activity in dashboard.recentActivities"
                  :key="activity.id"
                  :color="activityColor(activity.action)"
                >
                  <div class="activity-item">
                    <a-avatar :size="20" :style="{ backgroundColor: '#5B9BD5', fontSize: '10px', verticalAlign: 'middle' }">
                      {{ activity.user.username.charAt(0).toUpperCase() }}
                    </a-avatar>
                    <span class="activity-user">{{ activity.user.username }}</span>
                    <span class="activity-action">{{ activity.detail || activity.action }}</span>
                    <div class="activity-time">{{ formatDateTime(activity.createdAt) }}</div>
                  </div>
                </a-timeline-item>
              </a-timeline>
            </div>
            <a-empty v-else description="暂无活动" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined,
  SyncOutlined,
  AuditOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import { getDashboard } from '@/api/report'
import type { Dashboard } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const dashboard = ref<Dashboard | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    dashboard.value = res.data
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Failed to load dashboard'
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

function formatDateTime(dateStr: string): string {
  return dayjs(dateStr).format('MM-DD HH:mm')
}

function activityColor(action: string): string {
  if (action.includes('create')) return 'green'
  if (action.includes('update')) return 'blue'
  if (action.includes('delete')) return 'red'
  return 'gray'
}

function goToTask(task: { id: number; projectId: number }) {
  router.push({ name: 'ProjectDetail', params: { id: task.projectId }, query: { taskId: task.id } })
}

function goToProject(projectId: number) {
  router.push({ name: 'ProjectDetail', params: { id: projectId } })
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

.activity-item {
  font-size: 13px;
  line-height: 1.6;
}

.activity-user {
  font-weight: 500;
  color: #2c3e50;
  margin-left: 4px;
}

.activity-action {
  color: #8c8c8c;
  margin-left: 4px;
}

.activity-time {
  font-size: 12px;
  color: #bfbfbf;
  margin-top: 2px;
}
</style>
