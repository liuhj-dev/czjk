<template>
  <div class="recording-view">
    <!-- 预览 + 控制区 -->
    <el-card class="page-card">
      <!-- 设备选择栏 -->
      <div class="device-selector-bar">
        <div class="selector-item">
          <span class="selector-label">摄像头：</span>
          <el-select
            v-model="cameraId"
            placeholder="选择摄像头"
            size="small"
            style="width: 220px"
            @change="onCameraChange"
          >
            <el-option
              v-for="c in deviceStore.cameras"
              :key="c.id || c.deviceId"
              :label="c.name || c.deviceName"
              :value="c.id || c.deviceId"
            >
              <div style="display:flex;align-items:center;gap:6px">
                <el-icon><VideoCamera /></el-icon>
                <span>{{ c.name || c.deviceName }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
        <div class="selector-item">
          <span class="selector-label">麦克风：</span>
          <el-select
            v-model="audioId"
            placeholder="选择麦克风"
            size="small"
            style="width: 220px"
            @change="onAudioChange"
          >
            <el-option
              v-for="a in deviceStore.audioDevices"
              :key="a.id || a.deviceId"
              :label="a.name || a.deviceName"
              :value="a.id || a.deviceId"
            >
              <div style="display:flex;align-items:center;gap:6px">
                <el-icon><Microphone /></el-icon>
                <span>{{ a.name || a.deviceName }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
        <div class="selector-item">
          <el-button size="small" :icon="Refresh" @click="refreshDevices" :loading="deviceStore.loading">
            刷新设备
          </el-button>
        </div>
      </div>

      <!-- 视频预览 -->
      <div class="preview-container">
        <img
          v-if="previewUrl"
          :src="previewUrl"
          alt="预览"
          style="width:100%;height:100%;object-fit:contain"
        />
        <div v-else class="preview-placeholder">
          <el-icon><VideoCamera /></el-icon>
          <span>{{ cameraId ? '正在连接...' : '请先选择摄像头' }}</span>
        </div>

        <!-- 录制中指示器 -->
        <div v-if="recordingStore.isRecording" class="recording-indicator">
          <div class="recording-dot"></div>
          <span>{{ formatDuration(recordingStore.duration) }}</span>
        </div>

        <!-- 暂停指示器 -->
        <div v-if="recordingStore.isPaused" class="recording-indicator" style="color:#faad14">
          <el-icon><VideoPause /></el-icon>
          <span>已暂停</span>
        </div>
      </div>

      <!-- 控制按钮 -->
      <div class="control-bar">
        <button
          class="control-btn photo-btn"
          :disabled="!cameraId || recordingStore.isRecording"
          title="拍照"
          @click="takePhoto"
        >
          <el-icon><Camera /></el-icon>
        </button>

        <button
          v-if="recordingStore.isIdle"
          class="control-btn record-btn"
          :disabled="!cameraId && !audioId"
          title="开始录制"
          @click="handleStart"
        >
          <el-icon><VideoCamera /></el-icon>
        </button>

        <button
          v-if="recordingStore.canPause"
          class="control-btn pause-btn"
          title="暂停"
          @click="handlePause"
        >
          <el-icon><VideoPause /></el-icon>
        </button>

        <button
          v-if="recordingStore.canStop"
          class="control-btn stop-btn"
          title="停止"
          @click="handleStop"
        >
          <el-icon><VideoCameraFilled /></el-icon>
        </button>
      </div>

      <!-- 状态栏 -->
      <div class="status-bar">
        <div class="status-item">
          <el-icon><Timer /></el-icon>
          <span>时长：</span>
          <span class="status-value">{{ formatDuration(recordingStore.duration) }}</span>
        </div>
        <div class="status-item">
          <el-icon><Coin /></el-icon>
          <span>大小：</span>
          <span class="status-value">{{ formatFileSize(recordingStore.fileSize) }}</span>
        </div>
        <div class="status-item">
          <el-icon><Monitor /></el-icon>
          <span>分辨率：</span>
          <span class="status-value">{{ currentResolution }}</span>
        </div>
        <div class="status-item">
          <el-icon><Microphone /></el-icon>
          <span>模式：</span>
          <el-radio-group v-model="recordingStore.mode" size="small" :disabled="!recordingStore.isIdle">
            <el-radio-button value="both">音视频</el-radio-button>
            <el-radio-button value="video">仅视频</el-radio-button>
            <el-radio-button value="audio">仅音频</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 最近录像列表 -->
    <el-card class="page-card" style="margin-top:16px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>最近录像</span>
          <el-button text type="primary" @click="$router.push('/files')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <div v-if="recentFiles.length === 0" style="text-align:center;padding:40px;color:#999">
        <el-icon :size="40"><FolderOpened /></el-icon>
        <p style="margin-top:8px">暂无录像文件</p>
      </div>
      <div v-else class="recent-files-grid">
        <div v-for="file in recentFiles" :key="file.id" class="file-card">
          <div class="file-icon">
            <el-icon><component :is="getFileIcon(file.name)" /></el-icon>
          </div>
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-meta">
              {{ formatFileSize(file.size) }} · {{ file.createTime }}
            </div>
          </div>
          <el-button-group>
            <el-button size="small" text type="primary" title="播放">
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-button size="small" text type="primary" title="下载">
              <el-icon><Download /></el-icon>
            </el-button>
          </el-button-group>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useDeviceStore } from '@/stores/device'
import { useRecordingStore } from '@/stores/recording'
import { formatFileSize, formatDuration, getFileIcon } from '@/utils/helpers'
import { ElMessage } from 'element-plus'
import { capturePhoto } from '@/api'

const deviceStore = useDeviceStore()
const recordingStore = useRecordingStore()

const cameraId = ref(null)
const audioId = ref(null)
const previewUrl = ref('')
const recentFiles = ref([])

let previewTimer = null

const currentResolution = computed(() => {
  const vf = recordingStore.videoFormat
  if (vf.width && vf.height) return `${vf.width}×${vf.height}`
  return '自动'
})

function onCameraChange(id) {
  deviceStore.selectCamera(deviceStore.cameras.find(c => (c.id || c.deviceId) === id))
  startPreview()
}

function onAudioChange(id) {
  deviceStore.selectAudio(deviceStore.audioDevices.find(a => (a.id || a.deviceId) === id))
}

async function refreshDevices() {
  await deviceStore.refreshDevices()
  if (deviceStore.cameras.length > 0 && !cameraId.value) {
    cameraId.value = deviceStore.cameras[0].id || deviceStore.cameras[0].deviceId
    startPreview()
  }
  if (deviceStore.audioDevices.length > 0 && !audioId.value) {
    audioId.value = deviceStore.audioDevices[0].id || deviceStore.audioDevices[0].deviceId
  }
}

function startPreview() {
  if (!cameraId.value) return
  // MJPEG 预览流（后端提供）
  previewUrl.value = `/api/preview/stream?deviceId=${cameraId.value}&t=${Date.now()}`
  // 定时刷新预览帧（避免缓存）
  previewTimer = setInterval(() => {
    if (cameraId.value && recordingStore.isIdle) {
      previewUrl.value = `/api/preview/stream?deviceId=${cameraId.value}&t=${Date.now()}`
    }
  }, 100)
}

async function handleStart() {
  try {
    await recordingStore.start({ outputPath: generateOutputPath() })
    ElMessage.success('开始录制')
  } catch (e) {
    ElMessage.error(e.message || '开始录制失败')
  }
}

async function handleStop() {
  try {
    await recordingStore.stop()
    ElMessage.success('录制完成')
    startPreview() // 恢复预览
  } catch (e) {
    ElMessage.error(e.message || '停止录制失败')
  }
}

async function handlePause() {
  try {
    await recordingStore.pause()
    ElMessage.info('已暂停录制')
  } catch (e) {
    ElMessage.error(e.message || '暂停失败')
  }
}

async function takePhoto() {
  try {
    await capturePhoto({ deviceId: cameraId.value })
    ElMessage.success('拍照成功')
  } catch (e) {
    ElMessage.error(e.message || '拍照失败')
  }
}

function generateOutputPath() {
  const ts = Date.now()
  const ext = recordingStore.mode === 'audio' ? 'wav' : 'mp4'
  return `output/recording_${ts}.${ext}`
}

onMounted(() => {
  refreshDevices()
})

onUnmounted(() => {
  if (previewTimer) clearInterval(previewTimer)
})
</script>

<style scoped>
.device-selector-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 16px;
  flex-wrap: wrap;
}

.selector-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selector-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.recent-files-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
