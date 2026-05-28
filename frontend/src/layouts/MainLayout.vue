<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider
      v-model:collapsed="collapsed"
      collapsible
      :trigger="null"
      width="240"
      collapsed-width="80"
      :style="{
        background: '#fff',
        boxShadow: '2px 0 8px rgba(0, 0, 0, 0.06)',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        zIndex: 10
      }"
    >
      <div class="logo">
        <img src="/favicon.svg" alt="logo" class="logo-icon" />
        <h1 v-if="!collapsed" class="logo-text">项目管理系统</h1>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="light"
        mode="inline"
        @click="onMenuClick"
      >
        <a-menu-item key="/">
          <template #icon><DashboardOutlined /></template>
          <span>工作台</span>
        </a-menu-item>
        <a-menu-item key="/projects">
          <template #icon><ProjectOutlined /></template>
          <span>项目</span>
        </a-menu-item>
        <a-menu-item key="/documents">
          <template #icon><FileTextOutlined /></template>
          <span>文档中心</span>
        </a-menu-item>
        <a-menu-item key="/reports">
          <template #icon><BarChartOutlined /></template>
          <span>报表</span>
        </a-menu-item>
        <a-menu-item key="/settings">
          <template #icon><SettingOutlined /></template>
          <span>个人设置</span>
        </a-menu-item>
        <a-menu-item v-if="isAdmin" key="/workspaces">
          <template #icon><TeamOutlined /></template>
          <span>工作空间管理</span>
        </a-menu-item>
        <a-menu-item v-if="isAdmin" key="/admin/users">
          <template #icon><UserSwitchOutlined /></template>
          <span>用户管理</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout :style="{ marginLeft: collapsed ? '80px' : '240px', transition: 'margin-left 0.2s' }">
      <a-layout-header
        :style="{
          background: '#fff',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 1px 4px rgba(0, 0, 0, 0.06)',
          position: 'sticky',
          top: 0,
          zIndex: 9
        }"
      >
        <div class="header-left">
          <component
            :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
            class="trigger"
            @click="collapsed = !collapsed"
          />
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索项目、任务..."
            style="width: 320px; margin-left: 24px"
            @search="handleGlobalSearch"
            @pressEnter="handleGlobalSearch"
            allow-clear
          />
        </div>

        <div class="header-right">
          <a-badge :count="0" :offset="[-2, 4]">
            <BellOutlined class="header-icon" />
          </a-badge>
          <a-dropdown>
            <div class="user-info">
              <a-avatar
                :size="32"
                :style="{ backgroundColor: '#5B9BD5', cursor: 'pointer' }"
              >
                {{ userInitial }}
              </a-avatar>
              <span class="username">{{ username }}</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="$router.push('/settings')">
                  <span class="dropdown-item"><UserOutlined /> 个人资料</span>
                </a-menu-item>
                <a-menu-item key="settings" @click="$router.push('/settings')">
                  <span class="dropdown-item"><SettingOutlined /> 个人设置</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <span class="dropdown-item"><LogoutOutlined /> 退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content
        :style="{
          margin: '24px',
          padding: '24px',
          background: '#fff',
          borderRadius: '8px',
          minHeight: 'auto'
        }"
      >
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  ProjectOutlined,
  FileTextOutlined,
  BarChartOutlined,
  SettingOutlined,
  TeamOutlined,
  UserSwitchOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { listProjects } from '@/api/project'
import { listMyTasks } from '@/api/task'
import type { Project } from '@/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const collapsed = ref(false)
const searchText = ref('')
const selectedKeys = ref<string[]>(['/'])

const menuPaths = ['/admin/users', '/workspaces', '/projects', '/documents', '/reports', '/settings', '/']

const isAdmin = computed(() => authStore.user?.username === 'admin')
const username = computed(() => authStore.user?.username || '用户')
const userInitial = computed(() => username.value.charAt(0).toUpperCase())

watch(
  () => route.path,
  (path) => {
    const match = menuPaths.find((p) => path.startsWith(p))
    if (match) {
      selectedKeys.value = [match]
    }
  },
  { immediate: true }
)

function onMenuClick({ key }: { key: string }) {
  router.push(key)
}

async function handleGlobalSearch(value: string) {
  const kw = (value || '').trim()
  if (!kw) return

  try {
    const [projRes, taskRes] = await Promise.all([
      listProjects(),
      listMyTasks({ keyword: kw })
    ])

    const matchedProjects = (projRes.data as Project[]).filter((p: Project) =>
      p.name.toLowerCase().includes(kw.toLowerCase()) ||
      (p.description && p.description.toLowerCase().includes(kw.toLowerCase()))
    )
    const matchedTasks = taskRes.data || []

    if (matchedProjects.length === 1 && matchedTasks.length === 0) {
      router.push({ name: 'ProjectDetail', params: { id: matchedProjects[0].id } })
    } else if (matchedTasks.length === 1 && matchedProjects.length === 0) {
      router.push({ name: 'ProjectDetail', params: { id: matchedTasks[0].projectId }, query: { taskId: String(matchedTasks[0].id) } })
    } else if (matchedProjects.length === 0 && matchedTasks.length === 0) {
      message.info('未找到匹配的项目或任务')
    } else {
      // 多个结果：跳到项目列表，带上搜索关键词
      router.push({ path: '/projects', query: { search: kw } })
    }
  } catch {
    message.error('搜索失败')
  }
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.logo-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  white-space: nowrap;
}

.trigger {
  font-size: 18px;
  cursor: pointer;
  padding: 0 12px;
  transition: color 0.3s;
}

.trigger:hover {
  color: #5b9bd5;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-icon {
  font-size: 18px;
  cursor: pointer;
  color: #5a6c7d;
}

.header-icon:hover {
  color: #5b9bd5;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #2c3e50;
}

.dropdown-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
