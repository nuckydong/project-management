<template>
  <div class="settings-page">
    <a-tabs v-model:activeKey="activeTab">
      <!-- Profile Tab -->
      <a-tab-pane key="profile" tab="个人信息">
        <div class="settings-section">
          <div class="avatar-section">
            <a-upload
              :show-upload-list="false"
              :before-upload="handleAvatarUpload"
              accept="image/*"
            >
              <div class="avatar-upload">
                <a-avatar :size="80" :src="avatarUrl" :style="{ backgroundColor: '#5B9BD5' }">
                  {{ userInitial }}
                </a-avatar>
                <div class="avatar-overlay">
                  <CameraOutlined />
                  <span>更换头像</span>
                </div>
              </div>
            </a-upload>
          </div>

          <a-form layout="vertical" class="settings-form">
            <a-form-item label="用户名">
              <a-input :value="profile.username" disabled />
            </a-form-item>
            <a-form-item label="邮箱">
              <a-input v-model:value="profile.email" placeholder="请输入邮箱地址" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="profileLoading" @click="handleSaveProfile">
                保存
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>

      <!-- Change Password Tab -->
      <a-tab-pane key="password" tab="修改密码">
        <div class="settings-section">
          <a-form layout="vertical" class="settings-form">
            <a-form-item label="当前密码" required>
              <a-input-password
                v-model:value="passwordForm.oldPassword"
                placeholder="请输入当前密码"
              />
            </a-form-item>
            <a-form-item label="新密码" required>
              <a-input-password
                v-model:value="passwordForm.newPassword"
                placeholder="请输入新密码"
              />
            </a-form-item>
            <a-form-item label="确认新密码" required>
              <a-input-password
                v-model:value="passwordForm.confirmPassword"
                placeholder="请再次输入新密码"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>

      <!-- Other Tab -->
      <a-tab-pane key="other" tab="其他">
        <div class="settings-section">
          <div class="settings-block">
            <h4>退出登录</h4>
            <p style="color: #8c8c8c; margin-bottom: 12px">退出当前账号</p>
            <a-button danger @click="handleLogout">退出登录</a-button>
          </div>

          <a-divider />

<!--          <div class="settings-block">-->
<!--            <h4>关于</h4>-->
<!--            <div class="about-info">-->
<!--              <div class="about-row">-->
<!--                <span class="about-label">应用名称</span>-->
<!--                <span class="about-value">ProjectHub</span>-->
<!--              </div>-->
<!--              <div class="about-row">-->
<!--                <span class="about-label">版本号</span>-->
<!--                <span class="about-value">1.0.0</span>-->
<!--              </div>-->
<!--              <div class="about-row">-->
<!--                <span class="about-label">技术栈</span>-->
<!--                <span class="about-value">Vue 3 + TypeScript + Ant Design Vue</span>-->
<!--              </div>-->
<!--            </div>-->
<!--          </div>-->
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CameraOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getProfile, updateProfile, changePassword, updateAvatar } from '@/api/user'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('profile')
const profileLoading = ref(false)
const passwordLoading = ref(false)

const profile = reactive({
  username: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userInitial = computed(() => {
  return profile.username ? profile.username.charAt(0).toUpperCase() : 'U'
})

const avatarUrl = computed(() => {
  return authStore.user?.avatar || ''
})

onMounted(async () => {
  await loadProfile()
})

async function loadProfile() {
  try {
    const res = await getProfile()
    profile.username = res.data.username
    profile.email = res.data.email || ''
    // Update auth store user
    authStore.user = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载个人信息失败')
  }
}

async function handleSaveProfile() {
  if (!profile.email.trim()) {
    message.warning('请输入邮箱地址')
    return
  }
  profileLoading.value = true
  try {
    const res = await updateProfile({
      email: profile.email.trim()
    })
    message.success('个人信息已更新')
    authStore.user = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新失败')
  } finally {
    profileLoading.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword) {
    message.warning('请输入当前密码')
    return
  }
  if (!passwordForm.newPassword) {
    message.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.warning('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    message.warning('密码长度不能少于6位')
    return
  }
  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    message.success('密码已修改，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // Logout and redirect to login
    await authStore.logout()
    router.push('/login')
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}

async function handleAvatarUpload(file: File) {
  try {
    const res = await updateAvatar(file)
    message.success('头像已更新')
    authStore.user = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '上传头像失败')
  }
  return false // Prevent default upload
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.settings-page {
  max-width: 640px;
}

.settings-section {
  padding: 24px 0;
}

.settings-form {
  max-width: 400px;
}

.avatar-section {
  margin-bottom: 24px;
}

.avatar-upload {
  position: relative;
  cursor: pointer;
  display: inline-block;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
  width: 80px;
  height: 80px;
}

.avatar-upload:hover .avatar-overlay {
  opacity: 1;
}

.settings-block h4 {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.about-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.about-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.about-label {
  color: #8c8c8c;
  width: 80px;
  font-size: 13px;
}

.about-value {
  color: #2c3e50;
  font-size: 13px;
}
</style>
