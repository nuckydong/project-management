<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <div class="logo-circle">
          <ProjectOutlined style="font-size: 28px; color: #fff" />
        </div>
        <h1 class="register-title">创建账号</h1>
        <p class="register-subtitle">请填写以下信息完成注册</p>
      </div>

      <a-form
        :model="formState"
        :rules="rules"
        @finish="handleRegister"
        layout="vertical"
        class="register-form"
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

        <a-form-item name="email" :colon="false">
          <a-input
            v-model:value="formState.email"
            size="large"
            placeholder="请输入邮箱"
          >
            <template #prefix>
              <MailOutlined style="color: #bfbfbf" />
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

        <a-form-item name="confirmPassword" :colon="false">
          <a-input-password
            v-model:value="formState.confirmPassword"
            size="large"
            placeholder="请确认密码"
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
            注册
          </a-button>
        </a-form-item>
      </a-form>

      <div class="register-footer">
        已有账号？
        <router-link to="/login" class="register-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { UserOutlined, LockOutlined, MailOutlined, ProjectOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const formState = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (value && value !== formState.password) {
    throw new Error('两次输入的密码不一致')
  }
}

const rules: Record<string, Rule[]> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  loading.value = true
  try {
    await authStore.register({
      username: formState.username,
      email: formState.email,
      password: formState.password
    })
    message.success('注册成功')
    router.push('/login')
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '注册失败'
    message.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f1fb 0%, #5b9bd5 100%);
}

.register-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 40px 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
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

.register-title {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.register-footer {
  text-align: center;
  color: #8c8c8c;
  font-size: 14px;
}

.register-link {
  color: #5b9bd5;
  font-weight: 500;
}

.register-link:hover {
  color: #3a7cc0;
}
</style>
