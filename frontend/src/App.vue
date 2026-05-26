<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getProfile } from '@/api/user'

const authStore = useAuthStore()

// 应用初始化时，如果存在token则获取用户信息
onMounted(async () => {
  if (authStore.accessToken && !authStore.user) {
    try {
      const res = await getProfile()
      authStore.setUser(res.data)
    } catch (error) {
      // 获取用户信息失败，清除token
      authStore.forceClear()
    }
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
