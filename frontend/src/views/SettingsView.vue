<template>
  <div class="settings-view">
    <!-- 视频设置 -->
    <el-card class="page-card">
      <template #header>
        <div style="display:flex;align-items:center;gap:8px">
          <el-icon color="#409EFF"><VideoCamera /></el-icon>
          <span>视频设置</span>
        </div>
      </template>
      <el-form label-width="120px" label-position="right">
        <el-form-item label="默认分辨率">
          <el-select v-model="settings.video.resolution" style="width:200px">
            <el-option label="640×480 (VGA)" value="640x480" />
            <el-option label="1280×720 (HD)" value="1280x720" />
            <el-option label="1920×1080 (FHD)" value="1920x1080" />
            <el-option label="3840×2160 (4K)" value="3840x2160" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认帧率">
          <el-select v-model="settings.video.frameRate" style="width:200px">
            <el-option label="15 FPS" :value="15" />
            <el-option label="24 FPS" :value="24" />
            <el-option label="30 FPS" :value="30" />
            <el-option label="60 FPS" :value="60" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频编码">
          <el-select v-model="settings.video.codec" style="width:200px">
            <el-option label="H.264 (推荐)" value="h264" />
            <el-option label="H.265 (高压缩)" value="h265" />
            <el-option label="MJPEG" value="mjpeg" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频比特率">
          <el-slider
            v-model="settings.video.bitrate"
            :min="500"
            :max="20000"
            :step="500"
            :format-tooltip="(v) => (v / 1000).toFixed(1) + ' Mbps'"
            style="width:400px"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 音频设置 -->
    <el-card class="page-card" style="margin-top:16px">
      <template #header>
        <div style="display:flex;align-items:center;gap:8px">
          <el-icon color="#67C23A"><Microphone /></el-icon>
          <span>音频设置</span>
        </div>
      </template>
      <el-form label-width="120px" label-position="right">
        <el-form-item label="采样率">
          <el-select v-model="settings.audio.sampleRate" style="width:200px">
            <el-option label="22050 Hz" :value="22050" />
            <el-option label="44100 Hz (CD)" :value="44100" />
            <el-option label="48000 Hz (DVD)" :value="48000" />
            <el-option label="96000 Hz" :value="96000" />
          </el-select>
        </el-form-item>
        <el-form-item label="位深度">
          <el-select v-model="settings.audio.bitDepth" style="width:200px">
            <el-option label="16 bit" :value="16" />
            <el-option label="24 bit" :value="24" />
          </el-select>
        </el-form-item>
        <el-form-item label="声道">
          <el-radio-group v-model="settings.audio.channels">
            <el-radio :value="1">单声道</el-radio>
            <el-radio :value="2">立体声</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="音频编码">
          <el-select v-model="settings.audio.codec" style="width:200px">
            <el-option label="AAC (推荐)" value="aac" />
            <el-option label="MP3" value="mp3" />
            <el-option label="PCM (无损)" value="pcm" />
          </el-select>
        </el-form-item>
        <el-form-item label="音频比特率">
          <el-slider
            v-model="settings.audio.bitrate"
            :min="64"
            :max="320"
            :step="32"
            :format-tooltip="(v) => v + ' kbps'"
            style="width:400px"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 存储设置 -->
    <el-card class="page-card" style="margin-top:16px">
      <template #header>
        <div style="display:flex;align-items:center;gap:8px">
          <el-icon color="#E6A23C"><Folder /></el-icon>
          <span>存储设置</span>
        </div>
      </template>
      <el-form label-width="120px" label-position="right">
        <el-form-item label="保存路径">
          <el-input v-model="settings.storage.outputPath" style="width:360px" placeholder="例如：C:\Recordings">
            <template #append>
              <el-button>浏览</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="文件格式">
          <el-select v-model="settings.storage.format" style="width:200px">
            <el-option label="MP4 (推荐)" value="mp4" />
            <el-option label="AVI" value="avi" />
            <el-option label="MKV" value="mkv" />
            <el-option label="MOV" value="mov" />
          </el-select>
        </el-form-item>
        <el-form-item label="自动分割">
          <el-switch v-model="settings.storage.autoSplit" />
        </el-form-item>
        <el-form-item v-if="settings.storage.autoSplit" label="分割大小">
          <el-select v-model="settings.storage.splitSize" style="width:200px">
            <el-option label="500 MB" :value="524288000" />
            <el-option label="1 GB" :value="1073741824" />
            <el-option label="2 GB" :value="2147483648" />
            <el-option label="4 GB" :value="4294967296" />
          </el-select>
        </el-form-item>
        <el-form-item label="最长录制">
          <el-select v-model="settings.storage.maxDuration" style="width:200px">
            <el-option label="不限" :value="0" />
            <el-option label="30 分钟" :value="1800" />
            <el-option label="1 小时" :value="3600" />
            <el-option label="2 小时" :value="7200" />
            <el-option label="4 小时" :value="14400" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 保存按钮 -->
    <div style="text-align:center;margin-top:24px">
      <el-button type="primary" size="large" @click="saveSettings" :loading="saving" style="width:200px">
        保存设置
      </el-button>
      <el-button size="large" @click="resetSettings" style="width:200px">
        恢复默认
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSettings, saveSettings as apiSaveSettings } from '@/api'

const saving = ref(false)

const settings = reactive({
  video: {
    resolution: '1280x720',
    frameRate: 30,
    codec: 'h264',
    bitrate: 2500
  },
  audio: {
    sampleRate: 44100,
    bitDepth: 16,
    channels: 2,
    codec: 'aac',
    bitrate: 128
  },
  storage: {
    outputPath: 'output',
    format: 'mp4',
    autoSplit: false,
    splitSize: 1073741824,
    maxDuration: 0
  }
})

const defaultSettings = JSON.parse(JSON.stringify(settings))

async function loadSettings() {
  try {
    const res = await getSettings()
    const data = res.data || res
    if (data) {
      Object.assign(settings.video, data.video || {})
      Object.assign(settings.audio, data.audio || {})
      Object.assign(settings.storage, data.storage || {})
    }
  } catch {
    // 使用默认值
  }
}

async function saveSettings() {
  saving.value = true
  try {
    await apiSaveSettings(settings)
    ElMessage.success('设置已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function resetSettings() {
  Object.assign(settings.video, defaultSettings.video)
  Object.assign(settings.audio, defaultSettings.audio)
  Object.assign(settings.storage, defaultSettings.storage)
  ElMessage.info('已恢复默认设置')
}

onMounted(() => {
  loadSettings()
})
</script>
