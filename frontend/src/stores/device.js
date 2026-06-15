import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCameraDevices, getAudioDevices, openDevice, closeDevice } from '@/api'

export const useDeviceStore = defineStore('device', () => {
  // 状态
  const cameras = ref([])
  const audioDevices = ref([])
  const selectedCamera = ref(null)
  const selectedAudio = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const hasCamera = computed(() => cameras.value.length > 0)
  const hasAudio = computed(() => audioDevices.value.length > 0)
  const isReady = computed(() => selectedCamera.value || selectedAudio.value)

  /** 刷新设备列表 */
  async function refreshDevices() {
    loading.value = true
    error.value = null
    try {
      const [camRes, audRes] = await Promise.all([
        getCameraDevices(),
        getAudioDevices()
      ])
      cameras.value = camRes.data || camRes || []
      audioDevices.value = audRes.data || audRes || []

      // 自动选择默认设备
      if (!selectedCamera.value && cameras.value.length > 0) {
        selectedCamera.value = cameras.value[0]
      }
      if (!selectedAudio.value && audioDevices.value.length > 0) {
        selectedAudio.value = audioDevices.value[0]
      }
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  /** 选择设备 */
  function selectCamera(device) {
    selectedCamera.value = device
  }
  function selectAudio(device) {
    selectedAudio.value = device
  }

  /** 打开设备 */
  async function openSelectedDevices(format) {
    const tasks = []
    if (selectedCamera.value) {
      tasks.push(openDevice(selectedCamera.value.id, 'camera', format))
    }
    if (selectedAudio.value) {
      tasks.push(openDevice(selectedAudio.value.id, 'audio', format))
    }
    await Promise.all(tasks)
  }

  /** 关闭所有设备 */
  async function closeAllDevices() {
    const tasks = []
    if (selectedCamera.value) {
      tasks.push(closeDevice(selectedCamera.value.id, 'camera'))
    }
    if (selectedAudio.value) {
      tasks.push(closeDevice(selectedAudio.value.id, 'audio'))
    }
    await Promise.allSettled(tasks)
  }

  return {
    cameras, audioDevices, selectedCamera, selectedAudio,
    loading, error, hasCamera, hasAudio, isReady,
    refreshDevices, selectCamera, selectAudio,
    openSelectedDevices, closeAllDevices
  }
})
