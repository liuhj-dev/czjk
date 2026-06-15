import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  startRecording, stopRecording, pauseRecording, resumeRecording,
  getRecordingStatus
} from '@/api'

export const useRecordingStore = defineStore('recording', () => {
  // 状态
  const isRecording = ref(false)
  const isPaused = ref(false)
  const startTime = ref(null)
  const duration = ref(0)
  const fileSize = ref(0)
  const outputPath = ref('')
  const videoFormat = ref({ width: 1280, height: 720, frameRate: 30 })
  const audioFormat = ref({ sampleRate: 44100, bitDepth: 16, channels: 2 })
  const mode = ref('video') // video | audio | both
  const error = ref(null)
  const loading = ref(false)

  // 计时器
  let timer = null

  // 计算属性
  const isIdle = computed(() => !isRecording.value && !isPaused.value)
  const canStart = computed(() => !isRecording.value)
  const canPause = computed(() => isRecording.value && !isPaused.value)
  const canStop = computed(() => isRecording.value || isPaused.value)

  /** 开始录像 */
  async function start(config = {}) {
    loading.value = true
    error.value = null
    try {
      const payload = {
        outputPath: config.outputPath || generateOutputPath(),
        videoFormat: { ...videoFormat.value, ...config.videoFormat },
        audioFormat: { ...audioFormat.value, ...config.audioFormat },
        mode: mode.value,
        ...config
      }
      await startRecording(payload)
      isRecording.value = true
      isPaused.value = false
      startTime.value = Date.now()
      duration.value = 0
      fileSize.value = 0

      // 开始计时
      timer = setInterval(() => {
        duration.value = Date.now() - startTime.value
        pollStatus()
      }, 1000)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  /** 停止录像 */
  async function stop() {
    loading.value = true
    try {
      await stopRecording()
      clearInterval(timer)
      timer = null
      isRecording.value = false
      isPaused.value = false
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  /** 暂停 */
  async function pause() {
    loading.value = true
    try {
      await pauseRecording()
      isPaused.value = true
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  /** 恢复 */
  async function resume() {
    loading.value = true
    try {
      await resumeRecording()
      isPaused.value = false
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  /** 轮询状态 */
  async function pollStatus() {
    try {
      const res = await getRecordingStatus()
      const data = res.data || res
      fileSize.value = data.fileSize || fileSize.value
    } catch {
      // 静默处理
    }
  }

  /** 生成输出路径 */
  function generateOutputPath() {
    const ts = Date.now()
    const ext = mode.value === 'audio' ? 'wav' : 'mp4'
    return `output/recording_${ts}.${ext}`
  }

  /** 重置状态 */
  function reset() {
    clearInterval(timer)
    timer = null
    isRecording.value = false
    isPaused.value = false
    startTime.value = null
    duration.value = 0
    fileSize.value = 0
    outputPath.value = ''
    error.value = null
  }

  return {
    isRecording, isPaused, startTime, duration, fileSize,
    outputPath, videoFormat, audioFormat, mode, error, loading,
    isIdle, canStart, canPause, canStop,
    start, stop, pause, resume, reset
  }
})
