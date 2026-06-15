<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="app-aside">
      <div class="logo-area">
        <el-icon :size="28" color="#409EFF"><VideoCamera /></el-icon>
        <span v-show="!isCollapsed" class="logo-text">录音录像系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        router
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#409EFF"
        class="side-menu"
      >
        <el-menu-item index="/recording">
          <el-icon><VideoCamera /></el-icon>
          <template #title>录音录像</template>
        </el-menu-item>
        <el-menu-item index="/devices">
          <el-icon><Monitor /></el-icon>
          <template #title>设备管理</template>
        </el-menu-item>
        <el-menu-item index="/files">
          <el-icon><Folder /></el-icon>
          <template #title>文件管理</template>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="20"
            @click="isCollapsed = !isCollapsed"
          >
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag :type="isRecording ? 'danger' : 'success'" effect="dark" size="small">
            <el-icon v-if="isRecording"><VideoCameraFilled /></el-icon>
            {{ isRecording ? '录制中' : '待机' }}
          </el-tag>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useRecordingStore } from '@/stores/recording'

const route = useRoute()
const recording = useRecordingStore()
const isCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '录音录像')
const isRecording = computed(() => recording.isRecording)
</script>
