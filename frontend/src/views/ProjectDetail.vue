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
          <a-button @click="openSprintModal">
            <template #icon><ThunderboltOutlined /></template>
            管理冲刺
          </a-button>
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
        <a-tab-pane key="members" tab="成员">
          <div v-if="activeTab === 'members'" class="member-section">
            <div class="member-toolbar">
              <a-button type="primary" @click="openAddMemberModal">
                <template #icon><UserAddOutlined /></template>
                添加成员
              </a-button>
            </div>
            <a-table :dataSource="members" :columns="memberColumns" row-key="id" :pagination="false" size="middle">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'user'">
                  <div class="member-user-cell">
                    <a-avatar :size="28" :style="{ backgroundColor: '#5B9BD5', fontSize: '12px' }">
                      {{ record.user.username.charAt(0).toUpperCase() }}
                    </a-avatar>
                    <span>{{ record.user.username }}</span>
                  </div>
                </template>
                <template v-if="column.key === 'role'">
                  <a-tag :color="record.role === 'OWNER' ? 'blue' : record.role === 'ADMIN' ? 'orange' : 'default'">
                    {{ memberRoleLabel(record.role) }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-select
                      v-if="record.role !== 'OWNER'"
                      :value="record.role"
                      size="small"
                      style="width: 100px"
                      @change="(val: string) => handleChangeRole(record.user.id, val)"
                    >
                      <a-select-option value="ADMIN">管理员</a-select-option>
                      <a-select-option value="MEMBER">成员</a-select-option>
                      <a-select-option value="VIEWER">查看者</a-select-option>
                    </a-select>
                    <a-popconfirm
                      v-if="record.role !== 'OWNER'"
                      title="确定移除该成员？"
                      @confirm="handleRemoveMember(record.user.id)"
                    >
                      <a-button type="link" danger size="small">移除</a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>
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

    <!-- Add Member Modal -->
    <a-modal
      v-model:open="addMemberModalVisible"
      title="添加项目成员（从工作空间成员中选择）"
      @ok="handleAddMember"
      :confirm-loading="addMemberLoading"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="选择成员">
          <a-select
            v-model:value="addMemberUserId"
            placeholder="请选择工作空间成员"
            show-search
            :filter-option="filterWsMember"
            :not-found-content="wsMembersLoading ? undefined : (candidateMembers.length === 0 ? '工作空间中无可添加的成员' : '无匹配结果')"
          >
            <template v-if="wsMembersLoading" #notFoundContent>
              <a-spin size="small" />
            </template>
            <a-select-option v-for="m in candidateMembers" :key="m.user.id" :value="m.user.id">
              <div class="user-search-item">
                <a-avatar :size="22" :style="{ backgroundColor: '#5B9BD5', fontSize: '10px' }">
                  {{ m.user.username.charAt(0).toUpperCase() }}
                </a-avatar>
                <span>{{ m.user.username }}</span>
                <span class="user-search-email">{{ m.user.email }}</span>
              </div>
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="项目角色">
          <a-select v-model:value="addMemberRole">
            <a-select-option value="ADMIN">管理员</a-select-option>
            <a-select-option value="MEMBER">成员</a-select-option>
            <a-select-option value="VIEWER">查看者</a-select-option>
          </a-select>
        </a-form-item>
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
            <a-form-item label="冲刺">
              <a-select v-model:value="newTaskForm.sprintId" placeholder="不选择则为非冲刺任务" allow-clear>
                <a-select-option v-for="s in sprints" :key="s.id" :value="s.id">
                  {{ s.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
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
        </a-row>
        <a-row :gutter="16">
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

    <!-- Sprint Management Modal -->
    <a-modal
      v-model:open="sprintModalVisible"
      title="管理冲刺"
      :footer="null"
      width="700px"
    >
      <div class="sprint-modal-content">
        <div class="sprint-modal-toolbar">
          <a-button type="primary" size="small" @click="openNewSprintForm">
            <template #icon><PlusOutlined /></template>
            新建冲刺
          </a-button>
        </div>

        <!-- New Sprint Form (inline) -->
        <div v-if="showSprintForm" class="sprint-form">
          <a-form layout="inline" style="gap: 8px; flex-wrap: wrap">
            <a-form-item label="名称">
              <a-input v-model:value="sprintForm.name" style="width: 140px" size="small" />
            </a-form-item>
            <a-form-item label="目标">
              <a-input v-model:value="sprintForm.goal" style="width: 160px" size="small" placeholder="可选" />
            </a-form-item>
            <a-form-item label="开始">
              <a-date-picker v-model:value="sprintForm.startDate" size="small" />
            </a-form-item>
            <a-form-item label="结束">
              <a-date-picker v-model:value="sprintForm.endDate" size="small" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" size="small" :loading="sprintFormLoading" @click="handleSaveSprint">
                {{ editingSprintId ? '保存' : '创建' }}
              </a-button>
              <a-button size="small" style="margin-left: 4px" @click="cancelSprintForm">取消</a-button>
            </a-form-item>
          </a-form>
        </div>

        <!-- Sprint List -->
        <a-table
          :data-source="sprints"
          :columns="sprintColumns"
          row-key="id"
          :pagination="false"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-select :value="record.status" size="small" style="width: 90px" @change="(val: string) => handleSprintStatusChange(record.id, val)">
                <a-select-option value="PLANNING">规划中</a-select-option>
                <a-select-option value="ACTIVE">进行中</a-select-option>
                <a-select-option value="COMPLETED">已完成</a-select-option>
              </a-select>
            </template>
            <template v-if="column.key === 'dates'">
              <span>{{ record.startDate || '未设置' }} ~ {{ record.endDate || '未设置' }}</span>
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="editSprint(record)">编辑</a-button>
                <a-popconfirm title="确定删除该冲刺？" @confirm="handleDeleteSprint(record.id)">
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
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
  UserOutlined,
  UserAddOutlined,
  ThunderboltOutlined
} from '@ant-design/icons-vue'
import { getProject, updateProject, getProjectMembers, addProjectMember, updateProjectMemberRole, removeProjectMember } from '@/api/project'
import { listSprints, createSprint, updateSprint, deleteSprint } from '@/api/sprint'
import { createTask } from '@/api/task'
import { getWorkspaceMembers } from '@/api/workspace'
import type { Project, Sprint, ProjectMember, WorkspaceMember } from '@/types'
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
  sprintId: undefined as number | undefined,
  startDate: null as Dayjs | null,
  dueDate: null as Dayjs | null
})

// Sprint Management
const sprintModalVisible = ref(false)
const showSprintForm = ref(false)
const sprintFormLoading = ref(false)
const editingSprintId = ref<number | null>(null)
const sprintForm = reactive({
  name: '',
  goal: '',
  startDate: null as Dayjs | null,
  endDate: null as Dayjs | null
})

const sprintColumns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '目标', dataIndex: 'goal', key: 'goal', ellipsis: true },
  { title: '状态', key: 'status', width: 90 },
  { title: '日期', key: 'dates', width: 200 },
  { title: '操作', key: 'action', width: 130 }
]

function openSprintModal() {
  sprintModalVisible.value = true
  showSprintForm.value = false
  editingSprintId.value = null
}

function openNewSprintForm() {
  editingSprintId.value = null
  sprintForm.name = ''
  sprintForm.goal = ''
  sprintForm.startDate = null
  sprintForm.endDate = null
  showSprintForm.value = true
}

function cancelSprintForm() {
  showSprintForm.value = false
  editingSprintId.value = null
}

function editSprint(record: Sprint) {
  editingSprintId.value = record.id
  sprintForm.name = record.name
  sprintForm.goal = record.goal || ''
  sprintForm.startDate = record.startDate ? dayjs(record.startDate) : null
  sprintForm.endDate = record.endDate ? dayjs(record.endDate) : null
  showSprintForm.value = true
}

async function handleSaveSprint() {
  if (!sprintForm.name) {
    message.warning('请输入冲刺名称')
    return
  }
  sprintFormLoading.value = true
  try {
    if (editingSprintId.value) {
      await updateSprint(editingSprintId.value, {
        name: sprintForm.name,
        goal: sprintForm.goal || undefined,
        startDate: sprintForm.startDate?.format('YYYY-MM-DD'),
        endDate: sprintForm.endDate?.format('YYYY-MM-DD')
      })
      message.success('冲刺已更新')
    } else {
      await createSprint(projectId.value, {
        name: sprintForm.name,
        goal: sprintForm.goal || undefined,
        startDate: sprintForm.startDate?.format('YYYY-MM-DD'),
        endDate: sprintForm.endDate?.format('YYYY-MM-DD')
      })
      message.success('冲刺已创建')
    }
    showSprintForm.value = false
    editingSprintId.value = null
    const res = await listSprints(projectId.value)
    sprints.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '操作失败')
  } finally {
    sprintFormLoading.value = false
  }
}

async function handleDeleteSprint(id: number) {
  try {
    await deleteSprint(id)
    message.success('冲刺已删除')
    if (selectedSprintId.value === id) {
      selectedSprintId.value = undefined
    }
    const res = await listSprints(projectId.value)
    sprints.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '删除失败')
  }
}

async function handleSprintStatusChange(id: number, status: string) {
  try {
    await updateSprint(id, { status })
    message.success('状态已更新')
    const res = await listSprints(projectId.value)
    sprints.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新失败')
  }
}

// Add Member
const addMemberModalVisible = ref(false)
const addMemberLoading = ref(false)
const addMemberUserId = ref<number | undefined>(undefined)
const addMemberRole = ref('MEMBER')
const wsMembers = ref<WorkspaceMember[]>([])
const wsMembersLoading = ref(false)

const candidateMembers = computed(() => {
  const existingIds = new Set(members.value.map(m => m.user.id))
  return wsMembers.value.filter(m => !existingIds.has(m.user.id))
})

const memberColumns = [
  { title: '用户', key: 'user', dataIndex: 'user' },
  { title: '角色', key: 'role', dataIndex: 'role', width: 120 },
  { title: '操作', key: 'action', width: 200 }
]

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
  newTaskForm.sprintId = selectedSprintId.value
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
      sprintId: newTaskForm.sprintId
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

function memberRoleLabel(role: string): string {
  const map: Record<string, string> = { OWNER: '拥有者', ADMIN: '管理员', MEMBER: '成员', VIEWER: '查看者' }
  return map[role] || role
}

function openAddMemberModal() {
  addMemberUserId.value = undefined
  addMemberRole.value = 'MEMBER'
  loadWorkspaceMembers()
  addMemberModalVisible.value = true
}

function filterWsMember(input: string, option: any) {
  const member = wsMembers.value.find(m => m.user.id === option.value)
  if (!member) return false
  const kw = input.toLowerCase()
  return member.user.username.toLowerCase().includes(kw) || member.user.email.toLowerCase().includes(kw)
}

async function loadWorkspaceMembers() {
  if (!project.value?.workspaceId) return
  wsMembersLoading.value = true
  try {
    const res = await getWorkspaceMembers(project.value.workspaceId)
    wsMembers.value = res.data
  } catch {
    wsMembers.value = []
  } finally {
    wsMembersLoading.value = false
  }
}

async function handleAddMember() {
  if (!addMemberUserId.value) {
    message.warning('请选择用户')
    return
  }
  addMemberLoading.value = true
  try {
    await addProjectMember(projectId.value, { userId: addMemberUserId.value, role: addMemberRole.value })
    message.success('成员已添加')
    addMemberModalVisible.value = false
    const res = await getProjectMembers(projectId.value)
    members.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '添加失败')
  } finally {
    addMemberLoading.value = false
  }
}

async function handleChangeRole(uid: number, role: string) {
  try {
    await updateProjectMemberRole(projectId.value, uid, { role })
    message.success('角色已更新')
    const res = await getProjectMembers(projectId.value)
    members.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新失败')
  }
}

async function handleRemoveMember(uid: number) {
  try {
    await removeProjectMember(projectId.value, uid)
    message.success('成员已移除')
    const res = await getProjectMembers(projectId.value)
    members.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '移除失败')
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

.member-section {
  padding: 8px 0;
}

.member-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.member-user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-search-email {
  color: #8c8c8c;
  font-size: 12px;
}

.sprint-modal-toolbar {
  margin-bottom: 12px;
}

.sprint-form {
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  margin-bottom: 12px;
}
</style>
