<template>
  <div class="devices-view">
    <!-- 摄像头 -->
    <el-card class="page-card">
      <template #header>
        <div style="display:flex;align-items:center;gap:8px">
          <el-icon color="#409EFF"><VideoCamera /></el-icon>
          <span>摄像头设备</span>
          <el-tag size="small">{{ deviceStore.cameras.length }} 个</el-tag>
        </div>
      </template>

      <el-empty v-if="deviceStore.cameras.length === 0" description="未发现摄像头设备" :image-size="80">
        <el-button type="primary" @click="refresh">刷新扫描</el-button>
      </el-empty>

      <div v-else class="device-grid">
        <div
          v-for="camera in deviceStore.cameras"
          :key="camera.id || camera.deviceId"
          class="device-card"
          :class="{ active: isSelected(camera, 'camera') }"
          @click="selectDevice(camera, 'camera')"
        >
          <div style="display:flex;align-items:center;justify-content:space-between">
            <div>
              <div class="device-name">
                <el-icon><VideoCamera /></el-icon>
                {{ camera.name || camera.deviceName }}
              </div>
              <div class="device-desc">{{ camera.description || camera.id || camera.deviceId }}</div>
            </div>
            <el-tag :type="isSelected(camera, 'camera') ? 'primary' : 'info'" size="small">
              {{ isSelected(camera, 'camera') ? '已选择' : '可选' }}
            </el-tag>
          </div>

          <el-divider style="margin:12px 0" />

          <div class="device-specs">
            <div class="spec-item" v-if="camera.formats || camera.supportedFormats">
              <span class="spec-label">支持格式：</span>
              <span class="spec-value">
                {{ (camera.formats || camera.supportedFormats || []).join('、') || '自动检测' }}
              </span>
            </div>
            <div class="spec-item">
              <span class="spec-label">状态：</span>
              <el-tag :type="camera.isOpen ? 'success' : 'info'" size="small" effect="plain">
                {{ camera.isOpen ? '已打开' : '未打开' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 音频设备 -->
    <el-card class="page-card" style="margin-top:16px">
      <template #header>
        <div style="display:flex;align-items:center;gap:8px">
          <el-icon color="#67C23A"><Microphone /></el-icon>
          <span>音频设备</span>
          <el-tag size="small">{{ deviceStore.audioDevices.length }} 个</el-tag>
        </div>
      </template>

      <el-empty v-if="deviceStore.audioDevices.length === 0" description="未发现音频设备" :image-size="80">
        <el-button type="primary" @click="refresh">刷新扫描</el-button>
      </el-empty>

      <div v-else class="device-grid">
        <div
          v-for="audio in deviceStore.audioDevices"
          :key="audio.id || audio.deviceId"
          class="device-card"
          :class="{ active: isSelected(audio, 'audio') }"
          @click="selectDevice(audio, 'audio')"
        >
          <div style="display:flex;align-items:center;justify-content:space-between">
            <div>
              <div class="device-name">
                <el-icon><Microphone /></el-icon>
                {{ audio.name || audio.deviceName }}
              </div>
              <div class="device-desc">{{ audio.description || audio.id || audio.deviceId }}</div>
            </div>
            <el-tag :type="isSelected(audio, 'audio') ? 'success' : 'info'" size="small">
              {{ isSelected(audio, 'audio') ? '已选择' : '可选' }}
            </el-tag>
          </div>

          <el-divider style="margin:12px 0" />

          <div class="device-specs">
            <div class="spec-item" v-if="audio.formats || audio.supportedFormats">
              <span class="spec-label">支持格式：</span>
              <span class="spec-value">
                {{ (audio.formats || audio.supportedFormats || []).join('、') || '自动检测' }}
              </span>
            </div>
            <div class="spec-item">
              <span class="spec-label">状态：</span>
              <el-tag :type="audio.isOpen ? 'success' : 'info'" size="small" effect="plain">
                {{ audio.isOpen ? '已打开' : '未打开' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 刷新按钮 -->
    <div style="text-align:center;margin-top:20px">
      <el-button type="primary" :icon="Refresh" @click="refresh" :loading="deviceStore.loading">
        重新扫描设备
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useDeviceStore } from '@/stores/device'
import { ElMessage } from 'element-plus'

const deviceStore = useDeviceStore()

function isSelected(device, type) {
  const selected = type === 'camera' ? deviceStore.selectedCamera : deviceStore.selectedAudio
  if (!selected) return false
  return (device.id || device.deviceId) === (selected.id || selected.deviceId)
}

function selectDevice(device, type) {
  if (type === 'camera') {
    deviceStore.selectCamera(device)
  } else {
    deviceStore.selectAudio(device)
  }
}

async function refresh() {
  await deviceStore.refreshDevices()
  if (deviceStore.cameras.length > 0 || deviceStore.audioDevices.length > 0) {
    ElMessage.success(`发现 ${deviceStore.cameras.length} 个摄像头，${deviceStore.audioDevices.length} 个音频设备`)
  } else {
    ElMessage.warning('未发现任何设备，请检查设备连接')
  }
}

onMounted(() => {
  deviceStore.refreshDevices()
})
</script>

<style scoped>
.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 12px;
}

.device-name {
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.device-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.device-specs {
  font-size: 12px;
}

.spec-item {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.spec-label {
  color: #999;
}

.spec-value {
  color: #333;
}
</style>
