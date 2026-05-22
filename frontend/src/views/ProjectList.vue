<template>
  <div class="project-list-page">
    <!-- Header -->
    <div class="page-header">
      <h2 class="page-title">项目列表</h2>
      <a-button type="primary" @click="showCreateModal">
        <template #icon><PlusOutlined /></template>
        新建项目
      </a-button>
    </div>

    <!-- Search / Filter Bar -->
    <div class="filter-bar">
      <a-input-search
        v-model:value="searchKeyword"
        placeholder="搜索项目名称"
        style="width: 300px"
        @search="loadProjects"
        allow-clear
      />
      <a-select
        v-model:value="filterStatus"
        placeholder="状态筛选"
        style="width: 140px"
        allow-clear
        @change="loadProjects"
      >
        <a-select-option value="ACTIVE">进行中</a-select-option>
        <a-select-option value="COMPLETED">已完成</a-select-option>
        <a-select-option value="PAUSED">暂停</a-select-option>
        <a-select-option value="ARCHIVED">归档</a-select-option>
      </a-select>
    </div>

    <!-- Project Cards Grid -->
    <a-spin :spinning="loading">
      <div v-if="projects.length" class="project-grid">
        <a-card
          v-for="project in filteredProjects"
          :key="project.id"
          class="project-card"
          hoverable
          @click="goToDetail(project.id)"
        >
          <template #actions>
            <a-tooltip title="编辑">
              <EditOutlined @click.stop="openEdit(project)" />
            </a-tooltip>
            <a-tooltip title="删除">
              <DeleteOutlined @click.stop="handleDelete(project)" />
            </a-tooltip>
          </template>
          <div class="card-content">
            <div class="card-header">
              <h3 class="card-title">{{ project.name }}</h3>
              <a-tag :color="statusColor(project.status)">{{ statusLabel(project.status) }}</a-tag>
            </div>
            <p class="card-desc" v-if="project.description">{{ project.description }}</p>
            <div class="card-dates">
              <CalendarOutlined style="margin-right: 4px" />
              <span v-if="project.startDate">{{ formatDate(project.startDate) }}</span>
              <span v-if="project.startDate && project.endDate"> ~ </span>
              <span v-if="project.endDate">{{ formatDate(project.endDate) }}</span>
              <span v-if="!project.startDate && !project.endDate">未设置日期</span>
            </div>
            <div class="card-footer">
              <div class="card-members">
                <a-avatar
                  v-if="project.createdBy"
                  :size="28"
                  :style="{ backgroundColor: '#5B9BD5', fontSize: '12px' }"
                >
                  {{ project.createdBy.username.charAt(0).toUpperCase() }}
                </a-avatar>
              </div>
              <span class="card-creator">{{ project.createdBy?.username }}</span>
            </div>
          </div>
        </a-card>
      </div>
      <a-empty v-else description="暂无项目" />
    </a-spin>

    <!-- Create / Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingProject ? '编辑项目' : '新建项目'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="560px"
    >
      <a-form
        :model="formState"
        :rules="formRules"
        ref="formRef"
        layout="vertical"
        style="margin-top: 16px"
      >
        <a-form-item label="项目名称" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入项目名称" />
        </a-form-item>
        <a-form-item label="项目描述" name="description">
          <a-textarea v-model:value="formState.description" placeholder="请输入项目描述" :rows="3" />
        </a-form-item>
        <a-form-item label="工作空间" name="workspaceId" v-if="!editingProject">
          <a-select v-model:value="formState.workspaceId" placeholder="请选择工作空间">
            <a-select-option v-for="ws in workspaces" :key="ws.id" :value="ws.id">
              {{ ws.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期" name="startDate">
              <a-date-picker v-model:value="formState.startDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期" name="endDate">
              <a-date-picker v-model:value="formState.endDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message, type FormInstance } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CalendarOutlined
} from '@ant-design/icons-vue'
import { listProjects, createProject, updateProject, deleteProject } from '@/api/project'
import { listWorkspaces } from '@/api/workspace'
import type { Project, Workspace, ProjectUpdateRequest } from '@/types'
import type { Rule } from 'ant-design-vue/es/form'
import dayjs, { type Dayjs } from 'dayjs'

const router = useRouter()

const loading = ref(false)
const projects = ref<Project[]>([])
const workspaces = ref<Workspace[]>([])
const searchKeyword = ref('')
const filterStatus = ref<string | undefined>(undefined)

const modalVisible = ref(false)
const modalLoading = ref(false)
const editingProject = ref<Project | null>(null)
const formRef = ref<FormInstance>()

interface FormState {
  name: string
  description: string
  workspaceId: number | undefined
  startDate: Dayjs | null
  endDate: Dayjs | null
}

const formState = reactive<FormState>({
  name: '',
  description: '',
  workspaceId: undefined,
  startDate: null,
  endDate: null
})

const formRules: Record<string, Rule[]> = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  workspaceId: [{ required: true, message: '请选择工作空间', trigger: 'change' }]
}

const filteredProjects = computed(() => {
  let list = projects.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(p => p.name.toLowerCase().includes(kw))
  }
  if (filterStatus.value) {
    list = list.filter(p => p.status === filterStatus.value)
  }
  return list
})

onMounted(async () => {
  await Promise.all([loadProjects(), loadWorkspaces()])
})

async function loadProjects() {
  loading.value = true
  try {
    const res = await listProjects()
    projects.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载项目失败')
  } finally {
    loading.value = false
  }
}

async function loadWorkspaces() {
  try {
    const res = await listWorkspaces()
    workspaces.value = res.data
  } catch {
    // silently ignore
  }
}

function showCreateModal() {
  editingProject.value = null
  formState.name = ''
  formState.description = ''
  formState.workspaceId = workspaces.value.length ? workspaces.value[0].id : undefined
  formState.startDate = null
  formState.endDate = null
  modalVisible.value = true
}

function openEdit(project: Project) {
  editingProject.value = project
  formState.name = project.name
  formState.description = project.description || ''
  formState.workspaceId = project.workspaceId
  formState.startDate = project.startDate ? dayjs(project.startDate) : null
  formState.endDate = project.endDate ? dayjs(project.endDate) : null
  modalVisible.value = true
}

async function handleModalOk() {
  try {
    await formRef.value?.validateFields()
  } catch {
    return
  }
  modalLoading.value = true
  try {
    if (editingProject.value) {
      const data: ProjectUpdateRequest = {
        name: formState.name,
        description: formState.description || undefined,
        startDate: formState.startDate?.format('YYYY-MM-DD'),
        endDate: formState.endDate?.format('YYYY-MM-DD')
      }
      await updateProject(editingProject.value.id, data)
      message.success('项目已更新')
    } else {
      await createProject({
        workspaceId: formState.workspaceId!,
        name: formState.name,
        description: formState.description || undefined,
        startDate: formState.startDate?.format('YYYY-MM-DD'),
        endDate: formState.endDate?.format('YYYY-MM-DD')
      })
      message.success('项目已创建')
    }
    modalVisible.value = false
    await loadProjects()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '操作失败')
  } finally {
    modalLoading.value = false
  }
}

async function handleDelete(project: Project) {
  try {
    await deleteProject(project.id)
    message.success('项目已删除')
    await loadProjects()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '删除失败')
  }
}

function goToDetail(id: number) {
  router.push({ name: 'ProjectDetail', params: { id } })
}

function statusColor(status: string): string {
  const map: Record<string, string> = {
    ACTIVE: 'blue',
    COMPLETED: 'green',
    PAUSED: 'orange',
    ARCHIVED: 'default'
  }
  return map[status] || 'default'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    ACTIVE: '进行中',
    COMPLETED: '已完成',
    PAUSED: '暂停',
    ARCHIVED: '归档'
  }
  return map[status] || status
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD')
}
</script>

<style scoped>
.project-list-page {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.project-card {
  border-radius: 8px;
  cursor: pointer;
}

.project-card:hover {
  border-color: #5b9bd5;
}

.card-content {
  min-height: 140px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
  margin-right: 8px;
}

.card-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-dates {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-creator {
  font-size: 13px;
  color: #8c8c8c;
}
</style>
