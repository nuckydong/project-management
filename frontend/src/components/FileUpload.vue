<template>
  <a-upload-dragger
    :accept="accept"
    :before-upload="handleBeforeUpload"
    :custom-request="handleUpload"
    :show-upload-list="false"
    :multiple="false"
  >
    <p class="ant-upload-drag-icon">
      <inbox-outlined />
    </p>
    <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
    <p class="ant-upload-hint">
      支持单个文件上传，最大 {{ maxSize }}MB
    </p>
    <div v-if="uploading" style="margin-top: 12px">
      <a-progress :percent="uploadPercent" :size="'small'" />
    </div>
  </a-upload-dragger>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import type { UploadRequestOption } from 'ant-design-vue/es/vc-upload/interface'

const props = withDefaults(defineProps<{
  accept?: string
  maxSize?: number
  bucket?: string
}>(), {
  accept: '*',
  maxSize: 50,
  bucket: 'default'
})

const emit = defineEmits<{
  (e: 'upload-success', fileInfo: { file: File; url: string }): void
}>()

const uploading = ref(false)
const uploadPercent = ref(0)

function handleBeforeUpload(file: File) {
  const sizeMB = file.size / 1024 / 1024
  if (sizeMB > props.maxSize) {
    message.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

function handleUpload(options: UploadRequestOption) {
  const file = options.file as File
  uploading.value = true
  uploadPercent.value = 0

  // Simulate progress for FormData upload
  const progressInterval = setInterval(() => {
    if (uploadPercent.value < 90) {
      uploadPercent.value += 10
    }
  }, 200)

  // Emit the file back to the parent component to handle actual upload
  // The parent component will call the appropriate API
  setTimeout(() => {
    clearInterval(progressInterval)
    uploadPercent.value = 100
    uploading.value = false
    emit('upload-success', { file, url: '' })
  }, 300)
}
</script>

<style scoped>
.ant-upload-drag-icon {
  color: #5B9BD5;
  font-size: 48px;
}
</style>
