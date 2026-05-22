<template>
  <div class="document-version-panel">
    <a-spin :spinning="loading">
      <div v-if="versions.length === 0 && !loading" class="empty-versions">
        <a-empty description="暂无版本记录" />
      </div>
      <a-table
        v-else
        :columns="columns"
        :data-source="versions"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'versionNo'">
            <a-tag color="blue">v{{ record.versionNo }}</a-tag>
          </template>
          <template v-if="column.key === 'fileName'">
            <div class="file-name-cell">
              <FileOutlined />
              <span>{{ record.fileName }}</span>
            </div>
          </template>
          <template v-if="column.key === 'fileSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-if="column.key === 'uploadedBy'">
            {{ record.uploadedBy?.username || '-' }}
          </template>
          <template v-if="column.key === 'createdAt'">
            {{ formatDate(record.createdAt) }}
          </template>
          <template v-if="column.key === 'changeSummary'">
            {{ record.changeSummary || '-' }}
          </template>
          <template v-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="handlePreview(record)">
                <EyeOutlined /> 预览
              </a-button>
              <a-button type="link" size="small" @click="handleDownload(record)">
                <DownloadOutlined /> 下载
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { FileOutlined, DownloadOutlined, EyeOutlined } from '@ant-design/icons-vue'
import { getVersions, downloadVersion, previewVersion } from '@/api/document'
import type { DocumentVersion } from '@/types'
import dayjs from 'dayjs'

const props = defineProps<{
  documentId: number | null
}>()

const loading = ref(false)
const versions = ref<DocumentVersion[]>([])

const columns = [
  { title: '版本', key: 'versionNo', dataIndex: 'versionNo', width: 80 },
  { title: '文件名', key: 'fileName', dataIndex: 'fileName' },
  { title: '大小', key: 'fileSize', dataIndex: 'fileSize', width: 100 },
  { title: '上传者', key: 'uploadedBy', width: 100 },
  { title: '上传时间', key: 'createdAt', width: 160 },
  { title: '变更说明', key: 'changeSummary', dataIndex: 'changeSummary' },
  { title: '操作', key: 'actions', width: 160 }
]

watch(() => props.documentId, (newId) => {
  if (newId) {
    loadVersions()
  } else {
    versions.value = []
  }
}, { immediate: true })

async function loadVersions() {
  if (!props.documentId) return
  loading.value = true
  try {
    const res = await getVersions(props.documentId)
    versions.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载版本列表失败')
  } finally {
    loading.value = false
  }
}

async function handleDownload(record: DocumentVersion) {
  try {
    const res = await downloadVersion(record.id)
    const url = res.data
    window.open(url, '_blank')
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '获取下载链接失败')
  }
}

async function handlePreview(record: DocumentVersion) {
  try {
    const res = await previewVersion(record.id)
    const url = res.data
    window.open(url, '_blank')
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '获取预览链接失败')
  }
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.document-version-panel {
  margin-top: 16px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.empty-versions {
  padding: 24px 0;
}
</style>
