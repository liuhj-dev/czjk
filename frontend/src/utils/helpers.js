import { useRecordingStore } from '@/stores/recording'

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(i === 0 ? 0 : 1)} ${units[i]}`
}

/**
 * 格式化时长（毫秒 → mm:ss）
 */
export function formatDuration(ms) {
  if (!ms || ms <= 0) return '00:00'
  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

/**
 * 格式化时长（秒 → hh:mm:ss）
 */
export function formatDurationLong(seconds) {
  if (!seconds || seconds <= 0) return '00:00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/**
 * 根据 MIME 类型返回文件图标
 */
export function getFileIcon(filename) {
  if (!filename) return 'Document'
  const ext = filename.split('.').pop().toLowerCase()
  const map = {
    mp4: 'VideoPlay', avi: 'VideoPlay', mkv: 'VideoPlay', mov: 'VideoPlay', flv: 'VideoPlay',
    mp3: 'Headset', wav: 'Headset', aac: 'Headset', ogg: 'Headset', flac: 'Headset',
    jpg: 'Picture', jpeg: 'Picture', png: 'Picture', bmp: 'Picture', gif: 'Picture'
  }
  return map[ext] || 'Document'
}

/**
 * 根据 MIME 类型返回文件类型标签
 */
export function getFileType(filename) {
  if (!filename) return '未知'
  const ext = filename.split('.').pop().toLowerCase()
  if (['mp4', 'avi', 'mkv', 'mov', 'flv'].includes(ext)) return '视频'
  if (['mp3', 'wav', 'aac', 'ogg', 'flac'].includes(ext)) return '音频'
  if (['jpg', 'jpeg', 'png', 'bmp', 'gif'].includes(ext)) return '图片'
  return '其他'
}

/**
 * 生成带时间戳的文件名
 */
export function generateFileName(prefix, ext) {
  const now = new Date()
  const ts = [
    now.getFullYear(),
    String(now.getMonth() + 1).padStart(2, '0'),
    String(now.getDate()).padStart(2, '0'),
    '_',
    String(now.getHours()).padStart(2, '0'),
    String(now.getMinutes()).padStart(2, '0'),
    String(now.getSeconds()).padStart(2, '0')
  ].join('')
  return `${prefix}_${ts}.${ext}`
}
