<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-circle">
          <ProjectOutlined style="font-size: 28px; color: #fff" />
        </div>
        <h1 class="login-title">项目管理系统</h1>
        <p class="login-subtitle">欢迎回来，请登录您的账号</p>
      </div>

      <a-form
        :model="formState"
        :rules="rules"
        @finish="handleLogin"
        layout="vertical"
        class="login-form"
      >
        <a-form-item name="username" :colon="false">
          <a-input
            v-model:value="formState.username"
            size="large"
            placeholder="请输入用户名"
          >
            <template #prefix>
              <UserOutlined style="color: #bfbfbf" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password" :colon="false">
          <a-input-password
            v-model:value="formState.password"
            size="large"
            placeholder="请输入密码"
          >
            <template #prefix>
              <LockOutlined style="color: #bfbfbf" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        还没有账号？
        <router-link to="/register" class="login-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, ProjectOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)

const formState = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(formState)
    message.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    await router.push(redirect)
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '登录失败'
    message.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f1fb 0%, #5b9bd5 100%);
}

.login-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5b9bd5, #3a7cc0);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.login-form {
  margin-top: 8px;
}

.login-footer {
  text-align: center;
  color: #8c8c8c;
  font-size: 14px;
}

.login-link {
  color: #5b9bd5;
  font-weight: 500;
}

.login-link:hover {
  color: #3a7cc0;
}
</style>
