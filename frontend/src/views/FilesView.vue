<template>
  <div class="files-view">
    <el-card class="page-card">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:12px">
          <div style="display:flex;align-items:center;gap:8px">
            <el-icon color="#E6A23C"><Folder /></el-icon>
            <span>文件管理</span>
            <el-tag size="small">{{ fileStore.files.length }} 个文件</el-tag>
          </div>

          <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
            <el-input
              v-model="searchQuery"
              placeholder="搜索文件..."
              prefix-icon="Search"
              clearable
              size="small"
              style="width:200px"
              @input="handleSearch"
            />
            <el-select v-model="filterType" placeholder="文件类型" size="small" style="width:120px" @change="loadFiles">
              <el-option label="全部" value="all" />
              <el-option label="视频" value="video" />
              <el-option label="音频" value="audio" />
              <el-option label="图片" value="image" />
            </el-select>
            <el-button size="small" :icon="Refresh" @click="loadFiles" :loading="fileStore.loading">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-if="fileStore.files.length === 0 && !fileStore.loading" description="暂无文件" :image-size="100">
        <p style="color:#999">开始录像后，文件将显示在这里</p>
      </el-empty>

      <!-- 文件列表 -->
      <div v-else class="files-list">
        <div v-for="file in filteredFiles" :key="file.id" class="file-card">
          <div class="file-icon">
            <el-icon><component :is="getFileIcon(file.name)" /></el-icon>
          </div>

          <div class="file-info">
            <div class="file-name" :title="file.name">{{ file.name }}</div>
            <div class="file-meta">
              <el-tag size="small" effect="plain">{{ getFileType(file.name) }}</el-tag>
              <span>{{ formatFileSize(file.size) }}</span>
              <span>{{ file.createTime }}</span>
            </div>
          </div>

          <div class="file-actions">
            <el-button size="small" text type="primary" title="播放/查看" @click="handlePreview(file)">
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-button size="small" text type="primary" title="下载" @click="handleDownload(file)">
              <el-icon><Download /></el-icon>
            </el-button>
            <el-button size="small" text type="primary" title="重命名" @click="handleRename(file)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-popconfirm title="确定要删除此文件吗？" @confirm="handleDelete(file)">
              <template #reference>
                <el-button size="small" text type="danger" title="删除">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useFileStore } from '@/stores/files'
import { formatFileSize, getFileIcon, getFileType } from '@/utils/helpers'
import { downloadFile } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const fileStore = useFileStore()
const searchQuery = ref('')
const filterType = ref('all')

const filteredFiles = computed(() => {
  let list = fileStore.files
  if (filterType.value !== 'all') {
    const typeMap = { video: ['mp4', 'avi', 'mkv', 'mov', 'flv'], audio: ['mp3', 'wav', 'aac', 'ogg', 'flac'], image: ['jpg', 'jpeg', 'png', 'bmp'] }
    const exts = typeMap[filterType.value] || []
    list = list.filter(f => exts.includes((f.name || '').split('.').pop().toLowerCase()))
  }
  return list
})

function loadFiles() {
  fileStore.loadFiles()
}

function handleSearch() {
  fileStore.search(searchQuery.value)
}

async function handlePreview(file) {
  const ext = file.name.split('.').pop().toLowerCase()
  const isMedia = ['mp4', 'avi', 'mkv', 'mov', 'mp3', 'wav', 'aac', 'ogg'].includes(ext)
  if (isMedia) {
    // 打开预览（新窗口或内嵌播放器）
    const url = `/api/files/${file.id}/preview`
    window.open(url, '_blank')
  } else {
    window.open(`/api/files/${file.id}/download`, '_blank')
  }
}

async function handleDownload(file) {
  try {
    const res = await downloadFile(file.id)
    const url = window.URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url
    a.download = file.name
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch {
    ElMessage.error('下载失败')
  }
}

async function handleRename(file) {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新文件名', '重命名', {
      inputValue: file.name,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    if (newName && newName !== file.name) {
      await fileStore.rename(file.id, newName)
      ElMessage.success('重命名成功')
    }
  } catch { /* 取消 */ }
}

async function handleDelete(file) {
  try {
    await fileStore.remove(file.id)
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.files-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    border-color: #409EFF;
    background: #fafbff;
  }
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e6f4ff, #bae0ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #409EFF;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
</style>
