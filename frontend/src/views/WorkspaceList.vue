<template>
  <div class="workspace-page">
    <div class="page-header">
      <h2>工作空间</h2>
      <a-button type="primary" @click="showCreateModal">
        <template #icon><PlusOutlined /></template>
        新建工作空间
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]">
        <a-col v-for="ws in workspaces" :key="ws.id" :span="8">
          <a-card hoverable class="workspace-card" @click="openDetail(ws)">
            <div class="ws-card-header">
              <div class="ws-icon">
                <TeamOutlined style="font-size: 20px; color: #5b9bd5" />
              </div>
              <a-dropdown :trigger="['click']" @click.stop>
                <MoreOutlined class="ws-more" @click.stop />
                <template #overlay>
                  <a-menu @click.stop>
                    <a-menu-item key="edit" @click.stop="openEdit(ws)">编辑</a-menu-item>
                    <a-menu-item key="delete" @click.stop="confirmDelete(ws)">
                      <span style="color: #ff4d4f">删除</span>
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
            <h3 class="ws-name">{{ ws.name }}</h3>
            <p class="ws-desc">{{ ws.description || '暂无描述' }}</p>
            <div class="ws-meta">
              <span>创建者: {{ ws.owner?.username || '-' }}</span>
            </div>
          </a-card>
        </a-col>
      </a-row>
      <a-empty v-if="!loading && !workspaces.length" description="暂无工作空间" />
    </a-spin>

    <!-- Create / Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingWs ? '编辑工作空间' : '新建工作空间'"
      @ok="handleModalOk"
      :confirm-loading="saving"
    >
      <a-form :label-col="{ span: 5 }" ref="formRef" :model="formState">
        <a-form-item label="名称" name="name" :rules="[{ required: true, message: '请输入名称' }]">
          <a-input v-model:value="formState.name" placeholder="请输入工作空间名称" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formState.description" :rows="3" placeholder="可选描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Drawer -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="currentWs?.name"
      width="520"
    >
      <p class="drawer-desc">{{ currentWs?.description || '暂无描述' }}</p>
      <a-divider />
      <div class="drawer-section-header">
        <h4>成员列表</h4>
        <a-button type="link" size="small" @click="showAddMember">添加成员</a-button>
      </div>
      <a-spin :spinning="membersLoading">
        <div v-if="members.length" class="member-list">
          <div v-for="m in members" :key="m.id" class="member-item">
            <a-avatar :size="32" :style="{ backgroundColor: '#5B9BD5', fontSize: '14px' }">
              {{ m.user.username.charAt(0).toUpperCase() }}
            </a-avatar>
            <div class="member-info">
              <span class="member-name">{{ m.user.username }}</span>
              <a-tag :color="m.role === 'OWNER' ? 'blue' : 'default'" size="small">
                {{ roleLabel(m.role) }}
              </a-tag>
            </div>
            <a-popconfirm
              v-if="m.role !== 'OWNER'"
              title="确定移除该成员？"
              @confirm="handleRemoveMember(m.user.id)"
            >
              <a-button type="link" danger size="small">移除</a-button>
            </a-popconfirm>
          </div>
        </div>
        <a-empty v-else description="暂无成员" />
      </a-spin>

      <!-- Add Member -->
      <a-modal
        v-model:open="addMemberVisible"
        title="添加成员"
        @ok="handleAddMember"
        :confirm-loading="addMemberSaving"
      >
        <a-form :label-col="{ span: 5 }">
          <a-form-item label="搜索用户">
            <a-select
              v-model:value="addMemberForm.userId"
              show-search
              placeholder="输入用户名或邮箱搜索"
              :filter-option="false"
              :not-found-content="wsUserSearchFetching ? undefined : null"
              @search="handleWsUserSearch"
              style="width: 100%"
            >
              <template v-if="wsUserSearchFetching" #notFoundContent>
                <a-spin size="small" />
              </template>
              <a-select-option v-for="u in wsUserSearchResults" :key="u.id" :value="u.id">
                <div class="user-search-item">
                  <a-avatar :size="22" :style="{ backgroundColor: '#5B9BD5', fontSize: '10px' }">
                    {{ u.username.charAt(0).toUpperCase() }}
                  </a-avatar>
                  <span>{{ u.username }}</span>
                  <span class="user-search-email">{{ u.email }}</span>
                </div>
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="角色">
            <a-select v-model:value="addMemberForm.role">
              <a-select-option value="MEMBER">成员</a-select-option>
              <a-select-option value="ADMIN">管理员</a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
      </a-modal>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, type FormInstance } from 'ant-design-vue'
import { PlusOutlined, TeamOutlined, MoreOutlined } from '@ant-design/icons-vue'
import {
  listWorkspaces,
  createWorkspace,
  updateWorkspace,
  deleteWorkspace,
  getWorkspaceMembers,
  addWorkspaceMember,
  removeWorkspaceMember
} from '@/api/workspace'
import { searchUsers } from '@/api/user'
import type { Workspace, WorkspaceMember, User } from '@/types'

const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const drawerVisible = ref(false)
const editingWs = ref<Workspace | null>(null)
const currentWs = ref<Workspace | null>(null)
const workspaces = ref<Workspace[]>([])
const members = ref<WorkspaceMember[]>([])
const membersLoading = ref(false)
const addMemberVisible = ref(false)
const addMemberSaving = ref(false)
const formRef = ref<FormInstance>()

const formState = reactive({
  name: '',
  description: ''
})

const addMemberForm = reactive({
  userId: undefined as number | undefined,
  role: 'MEMBER'
})
const wsUserSearchResults = ref<User[]>([])
const wsUserSearchFetching = ref(false)
let wsUserSearchTimer: ReturnType<typeof setTimeout> | null = null

onMounted(() => {
  loadWorkspaces()
})

async function loadWorkspaces() {
  loading.value = true
  try {
    const res = await listWorkspaces()
    workspaces.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function showCreateModal() {
  editingWs.value = null
  formState.name = ''
  formState.description = ''
  modalVisible.value = true
}

function openEdit(ws: Workspace) {
  editingWs.value = ws
  formState.name = ws.name
  formState.description = ws.description || ''
  modalVisible.value = true
}

async function handleModalOk() {
  try {
    await formRef.value?.validateFields()
  } catch {
    return
  }
  saving.value = true
  try {
    if (editingWs.value) {
      await updateWorkspace(editingWs.value.id, formState)
      message.success('更新成功')
    } else {
      await createWorkspace(formState)
      message.success('创建成功')
    }
    modalVisible.value = false
    loadWorkspaces()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '操作失败')
  } finally {
    saving.value = false
  }
}

function confirmDelete(ws: Workspace) {
  Modal.confirm({
    title: '确定删除该工作空间？',
    content: '删除后不可恢复',
    okType: 'danger',
    async onOk() {
      try {
        await deleteWorkspace(ws.id)
        message.success('删除成功')
        loadWorkspaces()
      } catch (err: unknown) {
        message.error(err instanceof Error ? err.message : '删除失败')
      }
    }
  })
}

async function openDetail(ws: Workspace) {
  currentWs.value = ws
  drawerVisible.value = true
  loadMembers(ws.id)
}

async function loadMembers(wsId: number) {
  membersLoading.value = true
  try {
    const res = await getWorkspaceMembers(wsId)
    members.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载成员失败')
  } finally {
    membersLoading.value = false
  }
}

function showAddMember() {
  addMemberForm.userId = undefined
  addMemberForm.role = 'MEMBER'
  wsUserSearchResults.value = []
  addMemberVisible.value = true
}

function handleWsUserSearch(value: string) {
  if (wsUserSearchTimer) clearTimeout(wsUserSearchTimer)
  if (!value) {
    wsUserSearchResults.value = []
    return
  }
  wsUserSearchFetching.value = true
  wsUserSearchTimer = setTimeout(async () => {
    try {
      const res = await searchUsers(value)
      wsUserSearchResults.value = res.data
    } catch {
      wsUserSearchResults.value = []
    } finally {
      wsUserSearchFetching.value = false
    }
  }, 300)
}

async function handleAddMember() {
  if (!addMemberForm.userId || !currentWs.value) return
  addMemberSaving.value = true
  try {
    await addWorkspaceMember(currentWs.value.id, {
      userId: addMemberForm.userId,
      role: addMemberForm.role
    })
    message.success('添加成功')
    addMemberVisible.value = false
    loadMembers(currentWs.value.id)
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '添加失败')
  } finally {
    addMemberSaving.value = false
  }
}

async function handleRemoveMember(uid: number) {
  if (!currentWs.value) return
  try {
    await removeWorkspaceMember(currentWs.value.id, uid)
    message.success('已移除')
    loadMembers(currentWs.value.id)
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '移除失败')
  }
}

function roleLabel(role: string): string {
  const map: Record<string, string> = { OWNER: '拥有者', ADMIN: '管理员', MEMBER: '成员' }
  return map[role] || role
}
</script>

<script lang="ts">
import { Modal } from 'ant-design-vue'
</script>

<style scoped>
.workspace-page {
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
}

.workspace-card {
  border-radius: 8px;
}

.ws-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.ws-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ws-more {
  font-size: 16px;
  color: #8c8c8c;
  cursor: pointer;
  padding: 4px;
}

.ws-more:hover {
  color: #5b9bd5;
}

.ws-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px;
}

.ws-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0 0 12px;
}

.ws-meta {
  font-size: 12px;
  color: #bfbfbf;
}

.drawer-desc {
  color: #5a6c7d;
  font-size: 14px;
}

.drawer-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.drawer-section-header h4 {
  margin: 0;
  font-size: 15px;
  color: #2c3e50;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.member-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name {
  font-size: 14px;
  color: #2c3e50;
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
</style>
