import request from '@/utils/request'

// ============ 设备 API ============

/** 扫描所有设备 */
export function discoverDevices() {
  return request.get('/devices/discover')
}

/** 获取摄像头列表 */
export function getCameraDevices() {
  return request.get('/devices/cameras')
}

/** 获取音频设备列表 */
export function getAudioDevices() {
  return request.get('/devices/audio')
}

/** 打开设备 */
export function openDevice(deviceId, type, format) {
  return request.post('/devices/open', { deviceId, type, format })
}

/** 关闭设备 */
export function closeDevice(deviceId, type) {
  return request.post('/devices/close', { deviceId, type })
}

// ============ 录像 API ============

/** 开始录像 */
export function startRecording(config) {
  return request.post('/recording/start', config)
}

/** 停止录像 */
export function stopRecording() {
  return request.post('/recording/stop')
}

/** 暂停录像 */
export function pauseRecording() {
  return request.post('/recording/pause')
}

/** 恢复录像 */
export function resumeRecording() {
  return request.post('/recording/resume')
}

/** 拍照 */
export function capturePhoto(config) {
  return request.post('/recording/photo', config)
}

/** 获取录像状态 */
export function getRecordingStatus() {
  return request.get('/recording/status')
}

// ============ 文件 API ============

/** 获取文件列表 */
export function getFileList(params) {
  return request.get('/files', { params })
}

/** 下载文件 */
export function downloadFile(fileId) {
  return request.get(`/files/${fileId}/download`, { responseType: 'blob' })
}

/** 删除文件 */
export function deleteFile(fileId) {
  return request.delete(`/files/${fileId}`)
}

/** 重命名文件 */
export function renameFile(fileId, newName) {
  return request.put(`/files/${fileId}/rename`, { name: newName })
}

// ============ 预览流 API ============

/** 获取预览流地址 */
export function getPreviewStreamUrl(deviceId) {
  return `/api/preview/stream?deviceId=${deviceId}`
}

// ============ 系统 API ============

/** 获取系统信息 */
export function getSystemInfo() {
  return request.get('/system/info')
}

/** 获取/保存设置 */
export function getSettings() {
  return request.get('/settings')
}

export function saveSettings(data) {
  return request.put('/settings', data)
}
