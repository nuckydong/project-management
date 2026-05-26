<template>
  <div class="user-management-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <a-space>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索用户名或邮箱..."
          style="width: 240px"
          allow-clear
        />
        <a-button type="primary" @click="openCreateModal">
          <template #icon><PlusOutlined /></template>
          新建用户
        </a-button>
      </a-space>
    </div>

    <a-table
      :dataSource="filteredUsers"
      :columns="columns"
      row-key="id"
      :pagination="{ pageSize: 15, showSizeChanger: false }"
      :loading="loading"
      size="middle"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'user'">
          <div class="user-cell">
            <a-avatar :size="32" :style="{ backgroundColor: avatarColor(record.username), fontSize: '14px' }">
              {{ record.username.charAt(0).toUpperCase() }}
            </a-avatar>
            <div>
              <div class="user-cell-name">{{ record.username }}</div>
              <div class="user-cell-email">{{ record.email }}</div>
            </div>
          </div>
        </template>
        <template v-if="column.key === 'status'">
          <a-badge :status="statusBadge(record.status)" :text="statusLabel(record.status)" />
        </template>
        <template v-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space v-if="record.username === 'admin'">
            <a-tag color="blue">超级管理员</a-tag>
          </a-space>
          <a-space v-else>
            <a-select
              :value="record.status"
              size="small"
              style="width: 100px"
              @change="(val: number) => handleStatusChange(record.id, val)"
            >
              <a-select-option :value="1">启用</a-select-option>
              <a-select-option :value="2">禁用</a-select-option>
              <a-select-option :value="0">未激活</a-select-option>
            </a-select>
            <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
            <a-button type="link" size="small" @click="openResetModal(record)">重置密码</a-button>
            <a-popconfirm title="确定删除该用户？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Create / Edit User Modal -->
    <a-modal
      v-model:open="formModalVisible"
      :title="editingUser ? '编辑用户' : '新建用户'"
      @ok="handleFormSubmit"
      :confirm-loading="formLoading"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="用户名" required>
          <a-input v-model:value="formData.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="邮箱" required>
          <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item v-if="!editingUser" label="密码" required>
          <a-input-password v-model:value="formData.password" placeholder="请输入密码（至少6位）" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Reset Password Modal -->
    <a-modal
      v-model:open="resetModalVisible"
      title="重置密码"
      @ok="handleResetPassword"
      :confirm-loading="resetLoading"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="用户">
          <a-input :value="resetTarget?.username" disabled />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password v-model:value="newPassword" placeholder="请输入新密码（至少6位）" />
        </a-form-item>
        <a-form-item label="确认密码" required>
          <a-input-password v-model:value="confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { listAllUsers, updateUserStatus, resetUserPassword, createUser, updateUser, deleteUser } from '@/api/user'
import type { User } from '@/types'
import dayjs from 'dayjs'

const loading = ref(false)
const users = ref<User[]>([])
const searchKeyword = ref('')

const formModalVisible = ref(false)
const formLoading = ref(false)
const editingUser = ref<User | null>(null)
const formData = reactive({ username: '', email: '', password: '' })

const resetModalVisible = ref(false)
const resetLoading = ref(false)
const resetTarget = ref<User | null>(null)
const newPassword = ref('')
const confirmPassword = ref('')

const columns = [
  { title: '用户', key: 'user', width: 260 },
  { title: '状态', key: 'status', width: 120 },
  { title: '注册时间', key: 'createdAt', dataIndex: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 300 }
]

const filteredUsers = computed(() => {
  if (!searchKeyword.value) return users.value
  const kw = searchKeyword.value.toLowerCase()
  return users.value.filter(u =>
    u.username.toLowerCase().includes(kw) || u.email.toLowerCase().includes(kw)
  )
})

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const res = await listAllUsers()
    users.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  editingUser.value = null
  formData.username = ''
  formData.email = ''
  formData.password = ''
  formModalVisible.value = true
}

function openEditModal(user: User) {
  editingUser.value = user
  formData.username = user.username
  formData.email = user.email
  formData.password = ''
  formModalVisible.value = true
}

async function handleFormSubmit() {
  if (!formData.username || !formData.email) {
    message.warning('请填写用户名和邮箱')
    return
  }
  if (!editingUser.value && (!formData.password || formData.password.length < 6)) {
    message.warning('密码至少6位')
    return
  }
  formLoading.value = true
  try {
    if (editingUser.value) {
      await updateUser(editingUser.value.id, formData.username, formData.email)
      message.success('用户已更新')
    } else {
      await createUser(formData.username, formData.email, formData.password)
      message.success('用户已创建')
    }
    formModalVisible.value = false
    loadUsers()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '操作失败')
  } finally {
    formLoading.value = false
  }
}

async function handleStatusChange(userId: number, status: number) {
  try {
    await updateUserStatus(userId, status)
    message.success('状态已更新')
    loadUsers()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新失败')
  }
}

function openResetModal(user: User) {
  resetTarget.value = user
  newPassword.value = ''
  confirmPassword.value = ''
  resetModalVisible.value = true
}

async function handleResetPassword() {
  if (!newPassword.value || newPassword.value.length < 6) {
    message.warning('密码至少6位')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    message.warning('两次密码不一致')
    return
  }
  if (!resetTarget.value) return
  resetLoading.value = true
  try {
    await resetUserPassword(resetTarget.value.id, newPassword.value)
    message.success('密码已重置')
    resetModalVisible.value = false
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '重置失败')
  } finally {
    resetLoading.value = false
  }
}

async function handleDelete(userId: number) {
  try {
    await deleteUser(userId)
    message.success('用户已删除')
    loadUsers()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '删除失败')
  }
}

function statusLabel(status: number): string {
  const map: Record<number, string> = { 0: '未激活', 1: '启用', 2: '禁用' }
  return map[status] || '未知'
}

function statusBadge(status: number): string {
  const map: Record<number, string> = { 0: 'default', 1: 'success', 2: 'error' }
  return map[status] || 'default'
}

function avatarColor(name: string): string {
  const colors = ['#5B9BD5', '#52c41a', '#722ed1', '#fa8c16', '#eb2f96', '#13c2c2']
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.user-management-page {
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-cell-name {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

.user-cell-email {
  font-size: 12px;
  color: #8c8c8c;
}
</style>
