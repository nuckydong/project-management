<template>
  <div class="document-center">
    <!-- Top Bar -->
    <div class="doc-top-bar">
      <div class="top-bar-left">
        <a-select
          v-model:value="selectedProjectId"
          placeholder="选择项目"
          style="width: 240px"
          @change="onProjectChange"
        >
          <a-select-option v-for="p in projects" :key="p.id" :value="p.id">
            {{ p.name }}
          </a-select-option>
        </a-select>
      </div>
      <div class="top-bar-right">
        <a-button type="primary" @click="openCreateModal" :disabled="!selectedProjectId">
          <template #icon><PlusOutlined /></template>
          新建文档
        </a-button>
      </div>
    </div>

    <div class="doc-main">
      <!-- Left Panel: Tree Navigation -->
      <div class="doc-left-panel" :style="{ width: panelCollapsed ? '0px' : '250px' }">
        <div v-if="!panelCollapsed" class="panel-content">
          <div class="panel-header">
            <span>文档目录</span>
          </div>
          <a-spin :spinning="treeLoading">
            <a-tree
              v-if="treeData.length > 0"
              :tree-data="treeData"
              :selected-keys="selectedKeys"
              :expanded-keys="expandedKeys"
              @select="onTreeSelect"
              @expand="onTreeExpand"
              :field-names="{ title: 'title', key: 'key', children: 'children' }"
            >
              <template #icon="{ data }">
                <FolderOutlined v-if="data.children && data.children.length > 0" />
                <FileTextOutlined v-else />
              </template>
            </a-tree>
            <a-empty v-else description="暂无文档" style="margin-top: 40px" />
          </a-spin>
        </div>
      </div>

      <!-- Collapse Toggle -->
      <div class="panel-toggle" @click="panelCollapsed = !panelCollapsed">
        <component :is="panelCollapsed ? MenuUnfoldOutlined : MenuFoldOutlined" />
      </div>

      <!-- Right Panel: Document Detail -->
      <div class="doc-right-panel">
        <div v-if="!selectedDocument" class="empty-state">
          <FileTextOutlined class="empty-icon" />
          <p>请从左侧选择文档</p>
        </div>

        <div v-else class="doc-detail">
          <!-- Document Header -->
          <div class="doc-detail-header">
            <div class="doc-title-section">
              <h2 class="doc-title">{{ selectedDocument.title }}</h2>
              <div class="doc-meta">
                <a-tag :color="typeColor(selectedDocument.type || '')">
                  {{ typeLabel(selectedDocument.type || '') }}
                </a-tag>
                <a-tag v-if="selectedDocument.currentVersion" color="blue">
                  v{{ selectedDocument.currentVersion }}
                </a-tag>
                <span class="meta-info">
                  <UserOutlined />
                  {{ selectedDocument.createdBy?.username || '-' }}
                </span>
                <span class="meta-info">
                  <ClockCircleOutlined />
                  {{ formatDate(selectedDocument.createdAt) }}
                </span>
              </div>
            </div>
            <div class="doc-actions">
              <a-button @click="openUploadModal">
                <template #icon><UploadOutlined /></template>
                上传新版本
              </a-button>
              <a-button @click="openEditDocument">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm title="确定删除此文档？" @confirm="handleDeleteDocument">
                <a-button danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </div>
          </div>

          <!-- Version History -->
          <div class="doc-version-section">
            <h3>版本历史</h3>
            <DocumentVersionPanel :document-id="selectedDocument.id" />
          </div>
        </div>
      </div>
    </div>

    <!-- Create Document Modal -->
    <a-modal
      v-model:open="createModalVisible"
      title="新建文档"
      @ok="handleCreateDocument"
      :confirm-loading="createLoading"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="文档标题" required>
          <a-input v-model:value="createForm.title" placeholder="请输入文档标题" />
        </a-form-item>
        <a-form-item label="文档类型">
          <a-select v-model:value="createForm.type" placeholder="选择文档类型">
            <a-select-option value="DESIGN">设计文档</a-select-option>
            <a-select-option value="CLIENT">客户文档</a-select-option>
            <a-select-option value="MEETING">会议纪要</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Upload Version Modal -->
    <a-modal
      v-model:open="uploadModalVisible"
      title="上传新版本"
      @ok="handleUploadVersion"
      :confirm-loading="uploadLoading"
      width="520px"
    >
      <div style="margin-top: 16px">
        <FileUpload
          :accept="'*'"
          :max-size="50"
          @upload-success="onFileSelected"
        />
        <a-form layout="vertical" style="margin-top: 16px">
          <a-form-item label="变更说明">
            <a-textarea
              v-model:value="uploadChangeSummary"
              :rows="3"
              placeholder="请输入此版本的变更说明"
            />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- Edit Document Modal -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑文档"
      @ok="handleEditDocument"
      :confirm-loading="editLoading"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="文档标题" required>
          <a-input v-model:value="editForm.title" placeholder="请输入文档标题" />
        </a-form-item>
        <a-form-item label="文档类型">
          <a-select v-model:value="editForm.type" placeholder="选择文档类型">
            <a-select-option value="DESIGN">设计文档</a-select-option>
            <a-select-option value="CLIENT">客户文档</a-select-option>
            <a-select-option value="MEETING">会议纪要</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  FileTextOutlined,
  FolderOutlined,
  UploadOutlined,
  EditOutlined,
  DeleteOutlined,
  UserOutlined,
  ClockCircleOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons-vue'
import { listProjects } from '@/api/project'
import {
  listDocuments,
  createDocument,
  updateDocument,
  deleteDocument,
  uploadVersion
} from '@/api/document'
import type { Project, Document } from '@/types'
import dayjs from 'dayjs'
import DocumentVersionPanel from '@/components/DocumentVersionPanel.vue'
import FileUpload from '@/components/FileUpload.vue'

// Data
const projects = ref<Project[]>([])
const documents = ref<Document[]>([])
const selectedProjectId = ref<number | undefined>(undefined)
const selectedDocument = ref<Document | null>(null)
const panelCollapsed = ref(false)
const treeLoading = ref(false)

// Tree state
const selectedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])

// Create modal
const createModalVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  title: '',
  type: 'OTHER'
})

// Upload modal
const uploadModalVisible = ref(false)
const uploadLoading = ref(false)
const uploadChangeSummary = ref('')
const selectedFile = ref<File | null>(null)

// Edit modal
const editModalVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  title: '',
  type: ''
})

// Document type mappings
const typeGroups: Record<string, string> = {
  DESIGN: '设计文档',
  CLIENT: '客户文档',
  MEETING: '会议纪要',
  OTHER: '其他'
}

const typeColorMap: Record<string, string> = {
  DESIGN: 'blue',
  CLIENT: 'green',
  MEETING: 'orange',
  OTHER: 'default'
}

function typeColor(type: string): string {
  return typeColorMap[type] || 'default'
}

function typeLabel(type: string): string {
  return typeGroups[type] || type || '未知'
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// Tree data computed from documents
const treeData = computed(() => {
  const groups: Record<string, Document[]> = {}
  for (const doc of documents.value) {
    const type = doc.type || 'OTHER'
    if (!groups[type]) groups[type] = []
    groups[type].push(doc)
  }

  const result: Array<{
    title: string
    key: string
    children: Array<{ title: string; key: string; document: Document }>
  }> = []

  for (const [typeKey, typeDocs] of Object.entries(groups)) {
    result.push({
      title: typeGroups[typeKey] || typeKey,
      key: `type-${typeKey}`,
      children: typeDocs.map(doc => ({
        title: doc.title,
        key: `doc-${doc.id}`,
        document: doc
      }))
    })
  }

  return result
})

onMounted(async () => {
  try {
    const res = await listProjects()
    projects.value = res.data
    if (projects.value.length > 0) {
      selectedProjectId.value = projects.value[0].id
      await loadDocuments()
    }
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载项目列表失败')
  }
})

async function loadDocuments() {
  if (!selectedProjectId.value) return
  treeLoading.value = true
  try {
    const res = await listDocuments(selectedProjectId.value)
    documents.value = res.data
    // Auto-expand type groups
    expandedKeys.value = treeData.value.map(n => n.key)
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载文档列表失败')
  } finally {
    treeLoading.value = false
  }
}

async function onProjectChange() {
  selectedDocument.value = null
  selectedKeys.value = []
  await loadDocuments()
}

function onTreeExpand(keys: string[]) {
  expandedKeys.value = keys
}

function onTreeSelect(keys: string[]) {
  if (keys.length === 0) return
  selectedKeys.value = keys
  const key = keys[0]
  if (key.startsWith('doc-')) {
    const docId = Number(key.replace('doc-', ''))
    selectedDocument.value = documents.value.find(d => d.id === docId) || null
  }
}

// Create document
function openCreateModal() {
  createForm.title = ''
  createForm.type = 'OTHER'
  createModalVisible.value = true
}

async function handleCreateDocument() {
  if (!createForm.title.trim()) {
    message.warning('请输入文档标题')
    return
  }
  if (!selectedProjectId.value) return
  createLoading.value = true
  try {
    await createDocument(selectedProjectId.value, {
      title: createForm.title.trim(),
      type: createForm.type
    })
    message.success('文档已创建')
    createModalVisible.value = false
    await loadDocuments()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '创建文档失败')
  } finally {
    createLoading.value = false
  }
}

// Upload version
function openUploadModal() {
  selectedFile.value = null
  uploadChangeSummary.value = ''
  uploadModalVisible.value = true
}

function onFileSelected(fileInfo: { file: File }) {
  selectedFile.value = fileInfo.file
}

async function handleUploadVersion() {
  if (!selectedFile.value) {
    message.warning('请先选择文件')
    return
  }
  if (!selectedDocument.value) return
  uploadLoading.value = true
  try {
    await uploadVersion(
      selectedDocument.value.id,
      selectedFile.value,
      uploadChangeSummary.value || undefined
    )
    message.success('新版本已上传')
    uploadModalVisible.value = false
    // Reload document to get updated version
    await loadDocuments()
    // Re-select document to refresh version panel
    const docId = selectedDocument.value.id
    selectedDocument.value = documents.value.find(d => d.id === docId) || null
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '上传版本失败')
  } finally {
    uploadLoading.value = false
  }
}

// Edit document
function openEditDocument() {
  if (!selectedDocument.value) return
  editForm.title = selectedDocument.value.title
  editForm.type = selectedDocument.value.type || 'OTHER'
  editModalVisible.value = true
}

async function handleEditDocument() {
  if (!editForm.title.trim()) {
    message.warning('请输入文档标题')
    return
  }
  if (!selectedDocument.value) return
  editLoading.value = true
  try {
    await updateDocument(selectedDocument.value.id, {
      title: editForm.title.trim(),
      type: editForm.type
    })
    message.success('文档已更新')
    editModalVisible.value = false
    await loadDocuments()
    const docId = selectedDocument.value.id
    selectedDocument.value = documents.value.find(d => d.id === docId) || null
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '更新文档失败')
  } finally {
    editLoading.value = false
  }
}

// Delete document
async function handleDeleteDocument() {
  if (!selectedDocument.value) return
  try {
    await deleteDocument(selectedDocument.value.id)
    message.success('文档已删除')
    selectedDocument.value = null
    selectedKeys.value = []
    await loadDocuments()
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '删除文档失败')
  }
}
</script>

<style scoped>
.document-center {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 160px);
}

.doc-top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.doc-main {
  display: flex;
  flex: 1;
  min-height: 0;
  gap: 0;
}

.doc-left-panel {
  flex-shrink: 0;
  border-right: 1px solid #f0f0f0;
  transition: width 0.2s;
  overflow: hidden;
}

.panel-content {
  width: 250px;
  padding-right: 16px;
}

.panel-header {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  cursor: pointer;
  color: #8c8c8c;
  flex-shrink: 0;
  border-right: 1px solid #f0f0f0;
}

.panel-toggle:hover {
  color: #5B9BD5;
}

.doc-right-panel {
  flex: 1;
  padding-left: 16px;
  min-width: 0;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c8c8c;
}

.empty-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 12px;
}

.doc-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.doc-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-info {
  font-size: 13px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.doc-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.doc-version-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}
</style>
