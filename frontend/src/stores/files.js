import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFileList, deleteFile, renameFile } from '@/api'

export const useFileStore = defineStore('files', () => {
  const files = ref([])
  const loading = ref(false)
  const searchQuery = ref('')
  const sortBy = ref('createTime')
  const sortOrder = ref('desc')
  const currentDir = ref('output')

  /** 加载文件列表 */
  async function loadFiles() {
    loading.value = true
    try {
      const res = await getFileList({
        dir: currentDir.value,
        query: searchQuery.value,
        sortBy: sortBy.value,
        sortOrder: sortOrder.value
      })
      files.value = res.data || res || []
    } finally {
      loading.value = false
    }
  }

  /** 删除文件 */
  async function remove(fileId) {
    await deleteFile(fileId)
    await loadFiles()
  }

  /** 重命名 */
  async function rename(fileId, newName) {
    await renameFile(fileId, newName)
    await loadFiles()
  }

  /** 搜索 */
  function search(query) {
    searchQuery.value = query
    loadFiles()
  }

  return { files, loading, searchQuery, sortBy, sortOrder, currentDir, loadFiles, remove, rename, search }
})
