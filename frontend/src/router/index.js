import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/recording'
  },
  {
    path: '/recording',
    name: 'Recording',
    component: () => import('@/views/RecordingView.vue'),
    meta: { title: '录音录像', icon: 'VideoCamera' }
  },
  {
    path: '/devices',
    name: 'Devices',
    component: () => import('@/views/DevicesView.vue'),
    meta: { title: '设备管理', icon: 'Monitor' }
  },
  {
    path: '/files',
    name: 'Files',
    component: () => import('@/views/FilesView.vue'),
    meta: { title: '文件管理', icon: 'Folder' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: { title: '系统设置', icon: 'Setting' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || '免驱录音录像系统'} - 录音录像系统`
})

export default router
