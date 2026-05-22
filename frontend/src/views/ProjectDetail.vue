<template>
  <div class="project-detail-page">
    <a-spin :spinning="pageLoading">
      <!-- Top Section: Project Info -->
      <div class="project-info" v-if="project">
        <div class="info-left">
          <a-page-header
            :title="project.name"
            @back="$router.push('/projects')"
            style="padding: 0; margin-bottom: 12px"
          >
            <template #tags>
              <a-tag :color="statusColor(project.status)">{{ statusLabel(project.status) }}</a-tag>
            </template>
            <template #extra>
              <a-button @click="openEditProject">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
            </template>
            <template #footer>
              <div class="project-meta">
                <span v-if="project.description" class="meta-desc">{{ project.description }}</span>
                <span class="meta-item">
                  <CalendarOutlined />
                  {{ project.startDate ? formatDate(project.startDate) : '未设置' }} ~ {{ project.endDate ? formatDate(project.endDate) : '未设置' }}
                </span>
                <span class="meta-item">
                  <UserOutlined />
                  {{ project.createdBy?.username }}
                </span>
              </div>
            </template>
          </a-page-header>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="toolbar-left">
          <a-select
            v-model:value="selectedSprintId"
            placeholder="选择冲刺"
            style="width: 200px"
            allow-clear
            @change="onSprintChange"
          >
            <a-select-option v-for="s in sprints" :key="s.id" :value="s.id">
              {{ s.name }}
            </a-select-option>
          </a-select>
        </div>
        <div class="toolbar-right">
          <a-button type="primary" @click="openNewTask">
            <template #icon><PlusOutlined /></template>
            新建任务
          </a-button>
        </div>
      </div>

      <!-- Tab Bar -->
      <a-tabs v-model:activeKey="activeTab" class="view-tabs">
        <a-tab-pane key="kanban" tab="看板">
          <KanbanBoard
            v-if="activeTab === 'kanban'"
            :project-id="projectId"
            :sprint-id="selectedSprintId"
            @task-click="openTaskDrawer"
          />
        </a-tab-pane>
        <a-tab-pane key="list" tab="列表">
          <TaskList
            v-if="activeTab === 'list'"
            :project-id="projectId"
            :sprint-id="selectedSprintId"
            @task-click="openTaskDrawer"
          />
        </a-tab-pane>
        <a-tab-pane key="gantt" tab="甘特图">
          <GanttChart
            v-if="activeTab === 'gantt'"
            :project-id="projectId"
            :sprint-id="selectedSprintId"
            @task-click="openTaskDrawer"
          />
        </a-tab-pane>
        <a-tab-pane key="calendar" tab="日历">
          <CalendarView
            v-if="activeTab === 'calendar'"
            :project-id="projectId"
            :sprint-id="selectedSprintId"
            @task-click="openTaskDrawer"
          />
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <!-- Task Drawer -->
    <TaskDrawer
      v-if="drawerVisible"
      :visible="drawerVisible"
      :task-id="selectedTaskId"
      :project-id="projectId"
      :project-members="members"
      @close="closeTaskDrawer"
      @updated="onTaskUpdated"
    />

    <!-- Edit Project Modal -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑项目"
      @ok="handleEditProject"
      :confirm-loading="editModalLoading"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="项目名称">
          <a-input v-model:value="editForm.name" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="editForm.description" :rows="3" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="editForm.status">
            <a-select-option value="ACTIVE">进行中</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="PAUSED">暂停</a-select-option>
            <a-select-option value="ARCHIVED">归档</a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期">
              <a-date-picker v-model:value="editForm.startDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期">
              <a-date-picker v-model:value="editForm.endDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- New Task Modal -->
    <a-modal
      v-model:open="newTaskModalVisible"
      title="新建任务"
      @ok="handleCreateTask"
      :confirm-loading="newTaskLoading"
      width="560px"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="任务标题" required>
          <a-input v-model:value="newTaskForm.title" placeholder="请输入任务标题" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="newTaskForm.description" :rows="3" placeholder="请输入任务描述" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="newTaskForm.priority">
                <a-select-option value="LOW">低</a-select-option>
                <a-select-option value="MEDIUM">中</a-select-option>
                <a-select-option value="HIGH">高</a-select-option>
                <a-select-option value="URGENT">紧急</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-select v-model:value="newTaskForm.assigneeId" placeholder="选择负责人" allow-clear>
                <a-select-option v-for="m in members" :key="m.user.id" :value="m.user.id">
                  {{ m.user.username }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期">
              <a-date-picker v-model:value="newTaskForm.startDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止日期">
              <a-date-picker v-model:value="newTaskForm.dueDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  EditOutlined,
  PlusOutlined,
  CalendarOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { getProject, updateProject, getProjectMembers } from '@/api/project'
import { listSprints } from '@/api/sprint'
import { createTask } from '@/api/task'
import type { Project, Sprint, ProjectMember } from '@/types'
import dayjs, { type Dayjs } from 'dayjs'

import KanbanBoard from '@/components/KanbanBoard.vue'
import TaskList from '@/components/TaskList.vue'
import GanttChart from '@/components/GanttChart.vue'
import CalendarView from '@/components/CalendarView.vue'
import TaskDrawer from '@/components/TaskDrawer.vue'

const route = useRoute()
const projectId = computed(() => Number(route.params.id))

const pageLoading = ref(false)
const project = ref<Project | null>(null)
const sprints = ref<Sprint[]>([])
const members = ref<ProjectMember[]>([])
const activeTab = ref('kanban')
const selectedSprintId = ref<number | undefined>(undefined)

// Task Drawer
const drawerVisible = ref(false)
const selectedTaskId = ref<number | null>(null)

// Edit Project
const editModalVisible = ref(false)
const editModalLoading = ref(false)
const editForm = reactive({
  name: '',
  description: '',
  status: '',
  startDate: null as Dayjs | null,
  endDate: null as Dayjs | null
})

// New Task
const newTaskModalVisible = ref(false)
const newTaskLoading = ref(false)
const newTaskForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  assigneeId: undefined as number | undefined,
  startDate: null as Dayjs | null,
  dueDate: null as Dayjs | null
})

// Check if we should open a task drawer from route query
watch(() => route.query.taskId, (taskId) => {
  if (taskId) {
    openTaskDrawer(Number(taskId))
  }
}, { immediate: true })

onMounted(async () => {
  await loadPageData()
})

async function loadPageData() {
  pageLoading.value = true
  try {
    const [projRes, sprintsRes, membersRes] = await Promise.all([
      getProject(projectId.value),
      listSprints(projectId.value),
      getProjectMembers(projectId.value)
    ])
    project.value = projRes.data
    sprints.value = sprintsRes.data
    members.value = membersRes.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载失败')
  } finally {
    pageLoading.value = false
  }
}

function onSprintChange() {
  // Tab components will react to prop changes
}

function openTaskDrawer(taskId: number) {
  selectedTaskId.value = taskId
  drawerVisible.value = true
}

function closeTaskDrawer() {
  drawerVisible.value = false
  selectedTaskId.value = null
}

function onTaskUpdated() {
  // Trigger refresh by re-mounting - tab content will reload
}

function openEditProject() {
  if (!project.value) return
  editForm.name = project.value.name
  editForm.description = project.value.description || ''
  editForm.status = project.value.status
  editForm.startDate = project.value.startDate ? dayjs(project.value.startDate) : null
  editForm.endDate = project.value.endDate ? dayjs(project.value.endDate) : null
  editModalVisible.value = true
}

async function handleEditProject() {
  if (!editForm.name) {
    message.warning('请输入项目名称')
    return
  }
  editModalLoading.value = true
  try {
    await updateProject(projectId.value, {
      name: editForm.name,
      description: editForm.description || undefined,
      status: editForm.status as 'ACTIVE' | 'COMPLETED' | 'PAUSED' | 'ARCHIVED',
      startDate: editForm.startDate?.format('YYYY-MM-DD'),
      endDate: editForm.endDate?.format('YYYY-MM-DD')
    })
    message.success('项目已更新')
    editModalVisible.value = false
    await loadPageData()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新失败')
  } finally {
    editModalLoading.value = false
  }
}

function openNewTask() {
  newTaskForm.title = ''
  newTaskForm.description = ''
  newTaskForm.priority = 'MEDIUM'
  newTaskForm.assigneeId = undefined
  newTaskForm.startDate = null
  newTaskForm.dueDate = null
  newTaskModalVisible.value = true
}

async function handleCreateTask() {
  if (!newTaskForm.title) {
    message.warning('请输入任务标题')
    return
  }
  newTaskLoading.value = true
  try {
    await createTask(projectId.value, {
      title: newTaskForm.title,
      description: newTaskForm.description || undefined,
      priority: newTaskForm.priority,
      assigneeId: newTaskForm.assigneeId,
      startDate: newTaskForm.startDate?.format('YYYY-MM-DD'),
      dueDate: newTaskForm.dueDate?.format('YYYY-MM-DD'),
      sprintId: selectedSprintId.value
    })
    message.success('任务已创建')
    newTaskModalVisible.value = false
    activeTab.value = activeTab.value // trigger re-render
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '创建失败')
  } finally {
    newTaskLoading.value = false
  }
}

function statusColor(status: string): string {
  const map: Record<string, string> = { ACTIVE: 'blue', COMPLETED: 'green', PAUSED: 'orange', ARCHIVED: 'default' }
  return map[status] || 'default'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { ACTIVE: '进行中', COMPLETED: '已完成', PAUSED: '暂停', ARCHIVED: '归档' }
  return map[status] || status
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD')
}
</script>

<style scoped>
.project-detail-page {
  padding: 0;
}

.project-info {
  margin-bottom: 16px;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.meta-desc {
  font-size: 14px;
  color: #8c8c8c;
}

.meta-item {
  font-size: 13px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.view-tabs {
  margin-top: 8px;
}
</style>
