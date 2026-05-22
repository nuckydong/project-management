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
        <img src="" alt="" style="display: none" />
        <h1 v-if="!collapsed" class="logo-text">ProjectHub</h1>
        <h1 v-else class="logo-text-mini">PH</h1>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="light"
        mode="inline"
        @click="onMenuClick"
      >
        <a-menu-item key="/">
          <template #icon><DashboardOutlined /></template>
          <span>Dashboard</span>
        </a-menu-item>
        <a-menu-item key="/projects">
          <template #icon><ProjectOutlined /></template>
          <span>Projects</span>
        </a-menu-item>
        <a-menu-item key="/my-tasks">
          <template #icon><CheckSquareOutlined /></template>
          <span>My Tasks</span>
        </a-menu-item>
        <a-menu-item key="/documents">
          <template #icon><FileTextOutlined /></template>
          <span>Documents</span>
        </a-menu-item>
        <a-menu-item key="/reports">
          <template #icon><BarChartOutlined /></template>
          <span>Reports</span>
        </a-menu-item>
        <a-menu-item key="/settings">
          <template #icon><SettingOutlined /></template>
          <span>Settings</span>
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
            placeholder="Search projects, tasks..."
            style="width: 320px; margin-left: 24px"
          />
        </div>

        <div class="header-right">
          <a-badge :count="3" :offset="[-2, 4]">
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
                  <UserOutlined />
                  <span style="margin-left: 8px">Profile</span>
                </a-menu-item>
                <a-menu-item key="settings" @click="$router.push('/settings')">
                  <SettingOutlined />
                  <span style="margin-left: 8px">Settings</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  <span style="margin-left: 8px">Logout</span>
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
import {
  DashboardOutlined,
  ProjectOutlined,
  CheckSquareOutlined,
  FileTextOutlined,
  BarChartOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const collapsed = ref(false)
const searchText = ref('')
const selectedKeys = ref<string[]>(['/'])

const username = computed(() => authStore.user?.username || 'User')
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

const menuPaths = ['/my-tasks', '/projects', '/documents', '/reports', '/settings', '/']

function onMenuClick({ key }: { key: string }) {
  router.push(key)
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
  border-bottom: 1px solid #f0f0f0;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  white-space: nowrap;
}

.logo-text-mini {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
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
</style>
