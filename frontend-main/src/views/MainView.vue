<template>
  <div class="main-view">
    <!-- 侧边栏遮罩层（移动端/展开时显示） -->
    <div v-if="sidebarVisible && isMobile" class="sidebar-mask" @click="closeSidebar"></div>
    
    <!-- 新增：侧边栏触发器 -->
    <div 
      class="sidebar-trigger"
      @mouseenter="handleSidebarEnter"
      @mouseleave="handleSidebarLeave"
    ></div>

    <!-- 悬浮式侧边栏 -->
    <div 
      class="left-menu"
      :class="{ expanded: sidebarVisible }"
      @mouseenter="handleSidebarEnter"
      @mouseleave="handleSidebarLeave"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
    >
      <div class="menu-header">
        <h3>交通系统</h3>
        <div class="user-info" v-if="authStore.user">
          <span class="welcome-text">欢迎, {{ authStore.currentUsername }}</span>
        </div>
      </div>
      <div class="menu-items">
        <TransitionGroup name="slide-card" tag="div">
          <router-link to="/main/home" class="menu-item" key="home">首页</router-link> 
          <router-link to="/main/navigation" class="menu-item" key="navigation">导航</router-link>
          <router-link to="/main/user-management" class="menu-item" key="user-management">用户中心</router-link>
        </TransitionGroup>
      </div>
      
      <!-- 主题切换 -->
      <div class="theme-section">
        <button @click="toggleTheme" class="theme-toggle-btn" :title="getThemeTooltip()">
          <span class="theme-icon">{{ getThemeIcon() }}</span>
          <span class="theme-text">{{ getThemeText() }}</span>
        </button>
      </div>
      
      <!-- 音乐播放器区域 -->
      <div class="music-section">
        <MusicPlayerButton @open="showMusicPlayer = true" />
      </div>
      
      <div class="menu-footer">
        <button @click="handleLogout" class="logout-button" :disabled="authStore.loading">
          {{ authStore.loading ? '登出中...' : '退出登录' }}
        </button>
      </div>
    </div>
    <div class="content-area">
      <router-view></router-view>
    </div>
    
    <!-- 个人资料完善引导对话框 -->
    <ProfileCompleteDialog 
      v-model="showProfileDialog"
      @later="handleLater"
      @confirm="handleConfirm"
    />
    <!-- 音乐播放器弹窗 -->
    <Teleport to="body">
      <div v-if="showMusicPlayer" class="music-player-modal" @click.self="showMusicPlayer = false">
        <div class="music-player-popup">
          <MusicPlayer />
          <button class="close-music-btn" @click="showMusicPlayer = false">×</button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useThemeStore } from '../stores/theme.js'
import ProfileCompleteDialog from '../components/ProfileCompleteDialog.vue'
import MusicPlayer from '../components/MusicPlayer.vue'
import MusicPlayerButton from '../components/MusicPlayerButton.vue'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const showProfileDialog = ref(false)
const showMusicPlayer = ref(false)

// 侧边栏展开状态
const sidebarVisible = ref(false)
const isMobile = ref(false)
let touchStartX = 0
let touchCurrentX = 0

const checkIsMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 主题相关函数
const toggleTheme = () => {
  themeStore.toggleMode()
}

const getThemeIcon = () => {
  switch (themeStore.mode) {
    case 'light':
      return '☀️'
    case 'dark':
      return '🌙'
    case 'auto':
      return '🔄'
    default:
      return '🔄'
  }
}

const getThemeText = () => {
  switch (themeStore.mode) {
    case 'light':
      return '浅色'
    case 'dark':
      return '暗色'
    case 'auto':
      return '跟随系统'
    default:
      return '跟随系统'
  }
}

const getThemeTooltip = () => {
  switch (themeStore.mode) {
    case 'light':
      return '当前：浅色模式，点击切换到暗色模式'
    case 'dark':
      return '当前：暗色模式，点击切换到跟随系统'
    case 'auto':
      return '当前：跟随系统，点击切换到浅色模式'
    default:
      return '点击切换主题模式'
  }
}

/**
 * 检查是否需要显示个人资料完善引导
 */
const checkProfileComplete = () => {
  // 延迟检查，确保页面加载完成
  setTimeout(() => {
    if (authStore.isAuthenticated && authStore.needCompleteProfile) {
      showProfileDialog.value = true
    }
  }, 1000)
}

/**
 * 用户选择稍后完善
 */
const handleLater = () => {
  // 暂时关闭提醒，可以设置一个标记避免频繁提醒
  localStorage.setItem('profileReminderDismissed', Date.now().toString())
}

/**
 * 用户确认前往完善资料
 */
const handleConfirm = () => {
  // 标记用户已响应提醒
  authStore.needCompleteProfile = false
  localStorage.setItem('needCompleteProfile', 'false')
}

/**
 * 处理登出
 */
const handleLogout = async () => {
  try {
    await authStore.logout()
    // 登出成功，跳转到登录页
    router.push('/')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出API失败，也要清除本地状态并跳转
    authStore.clearAuthData()
    router.push('/')
  }
}

// 主题清理函数
let themeCleanup = null

/**
 * 组件挂载时检查认证状态
 */
onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/')
    return
  }
  
  // 初始化主题
  themeCleanup = themeStore.initTheme()
  
  // 检查是否需要显示个人资料完善引导
  checkProfileComplete()
})

onUnmounted(() => {
  // 清理主题监听器
  if (themeCleanup) {
    themeCleanup()
  }
})

const handleSidebarEnter = () => {
  if (!isMobile.value) sidebarVisible.value = true
}
const handleSidebarLeave = () => {
  if (!isMobile.value) sidebarVisible.value = false
}
const closeSidebar = () => {
  sidebarVisible.value = false
}
// 移动端滑动展开/收起
const handleTouchStart = (e) => {
  if (!isMobile.value) return
  touchStartX = e.touches[0].clientX
  touchCurrentX = touchStartX
}
const handleTouchMove = (e) => {
  if (!isMobile.value) return
  touchCurrentX = e.touches[0].clientX
  // 从最左侧滑动右移超过30px展开
  if (!sidebarVisible.value && touchStartX < 30 && touchCurrentX - touchStartX > 30) {
    sidebarVisible.value = true
  }
  // 从侧边栏内左滑收起
  if (sidebarVisible.value && touchStartX < 300 && touchCurrentX - touchStartX < -50) {
    sidebarVisible.value = false
  }
}
const handleTouchEnd = () => {
  touchStartX = 0
  touchCurrentX = 0
}
</script>

<style scoped>
.main-view {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg-primary);
  position: relative;
}

.sidebar-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.18);
  z-index: 1001;
}

.sidebar-trigger {
  position: fixed;
  top: 0;
  left: 0;
  width: 15px; /* A small, invisible area to trigger the hover */
  height: 100vh;
  z-index: 1003;
}

.left-menu {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 280px; /* Fixed width */
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  border-right: 1px solid var(--color-border-primary);
  box-shadow: 2px 0 16px rgba(0,0,0,0.08);
  z-index: 1002;
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow: hidden;
  transform: translateX(-100%); /* Hide off-screen by default */
  transition: transform 0.3s cubic-bezier(.4,0,.2,1); /* Animate transform for performance */
}
.left-menu.expanded {
  transform: translateX(0); /* Bring on-screen when expanded */
}

.left-menu .menu-header,
.left-menu .menu-items,
.left-menu .theme-section,
.left-menu .music-section,
.left-menu .menu-footer {
  /* The opacity transition can remain as it is also performant */
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}
.left-menu.expanded .menu-header,
.left-menu.expanded .menu-items,
.left-menu.expanded .theme-section,
.left-menu.expanded .music-section,
.left-menu.expanded .menu-footer {
  opacity: 1;
  pointer-events: auto;
}

.menu-header h3 {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
  color: var(--color-text-primary);
}

.user-info {
  text-align: center;
  margin-bottom: 20px;
}

.welcome-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  background: var(--color-bg-tertiary);
  padding: 8px 12px;
  border-radius: 6px;
  display: inline-block;
}

.menu-items {
  flex-grow: 1;
}

.menu-item {
  display: block;
  padding: 15px 20px;
  color: var(--color-text-primary);
  text-decoration: none;
  border-radius: 8px;
  margin-bottom: 10px;
  transition: background-color 0.3s;
}

.menu-item:hover,
.router-link-active {
  background: var(--color-bg-tertiary);
}

/* 主题切换区域 */
.theme-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-primary);
}

.theme-toggle-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border-primary);
  color: var(--color-text-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.theme-toggle-btn:hover {
  background: var(--color-bg-tertiary);
  border-color: var(--color-border-hover);
}

.theme-icon {
  font-size: 16px;
}

.theme-text {
  font-size: 14px;
}

/* 音乐播放器区域 */
.music-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-primary);
}

.menu-footer {
  margin-top: auto;
}

.logout-button {
  width: 100%;
  padding: 15px;
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-danger);
  color: var(--color-danger);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-button:hover:not(:disabled) {
  background: var(--color-danger);
  color: white;
}

.logout-button:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  border-color: var(--color-border-primary);
  cursor: not-allowed;
}

.content-area {
  flex-grow: 1;
  padding: 0;
  overflow: auto;
  background: var(--color-bg-primary);
  min-height: 100vh;
  /* The margin-left and its transition are removed to prevent layout reflow */
}

/* 侧边栏菜单滑动卡片动画 */
.slide-card-enter-active, .slide-card-leave-active {
  transition: all 0.4s cubic-bezier(.4,0,.2,1);
}
.slide-card-enter-from {
  opacity: 0;
  transform: translateX(-30px) scale(0.96);
}
.slide-card-leave-to {
  opacity: 0;
  transform: translateX(30px) scale(0.96);
}
.slide-card-move {
  transition: transform 0.4s cubic-bezier(.4,0,.2,1);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .left-menu {
    width: 0;
    min-width: 0;
    padding: 0;
  }
  .left-menu.expanded {
    width: 80vw;
    min-width: 220px;
    max-width: 320px;
    padding: 20px;
  }
  .content-area {
    margin-left: 0;
  }
}

.music-player-modal {
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.18);
  z-index: 2000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.music-player-popup {
  background: transparent;
  border-radius: 16px 16px 0 0;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  padding: 24px 16px 16px 16px;
  min-width: 320px;
  max-width: 95vw;
  min-height: 220px;
  position: relative;
  animation: popupUp 0.3s cubic-bezier(.4,0,.2,1);
}
@keyframes popupUp {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
.close-music-btn {
  position: absolute;
  top: 8px;
  right: 12px;
  background: none;
  border: none;
  font-size: 1.8rem;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color 0.2s;
}
.close-music-btn:hover {
  color: var(--color-danger);
}
</style>