<template>
  <div class="toast-container">
    <TransitionGroup name="toast" tag="div">
      <div 
        v-for="toast in toasts" 
        :key="toast.id"
        class="toast"
        :class="toast.type"
      >
        <div class="toast-icon">
          <span v-if="toast.type === 'success'">✅</span>
          <span v-else-if="toast.type === 'error'">❌</span>
          <span v-else-if="toast.type === 'warning'">⚠️</span>
          <span v-else>ℹ️</span>
        </div>
        <div class="toast-content">
          <div class="toast-title">{{ toast.title }}</div>
          <div class="toast-message">{{ toast.message }}</div>
        </div>
        <button @click="removeToast(toast.id)" class="toast-close">×</button>
        <div class="toast-progress" :style="{ width: toast.progress + '%' }"></div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// Toast通知列表
const toasts = ref([])
let toastId = 0

/**
 * 添加Toast通知
 * @param {Object} options - 通知配置
 * @param {string} options.type - 通知类型: 'success', 'error', 'warning', 'info'
 * @param {string} options.title - 通知标题
 * @param {string} options.message - 通知消息
 * @param {number} options.duration - 显示时长(毫秒)，默认根据类型设置
 */
const addToast = (options) => {
  const { type = 'info', title, message, duration } = options
  
  // 根据类型设置默认时长
  const defaultDuration = type === 'success' ? 2000 : 5000
  const toastDuration = duration || defaultDuration
  
  const toast = {
    id: ++toastId,
    type,
    title,
    message,
    duration: toastDuration,
    progress: 100,
    startTime: Date.now()
  }
  
  toasts.value.push(toast)
  
  // 设置自动消失
  const timer = setTimeout(() => {
    removeToast(toast.id)
  }, toastDuration)
  
  // 设置进度条动画
  const progressTimer = setInterval(() => {
    const elapsed = Date.now() - toast.startTime
    const remaining = Math.max(0, 100 - (elapsed / toastDuration) * 100)
    
    const toastIndex = toasts.value.findIndex(t => t.id === toast.id)
    if (toastIndex !== -1) {
      toasts.value[toastIndex].progress = remaining
    }
    
    if (remaining <= 0) {
      clearInterval(progressTimer)
    }
  }, 50)
  
  // 保存定时器引用以便清理
  toast._timer = timer
  toast._progressTimer = progressTimer
}

/**
 * 移除Toast通知
 * @param {number} id - Toast ID
 */
const removeToast = (id) => {
  const index = toasts.value.findIndex(toast => toast.id === id)
  if (index !== -1) {
    const toast = toasts.value[index]
    
    // 清理定时器
    if (toast._timer) {
      clearTimeout(toast._timer)
    }
    if (toast._progressTimer) {
      clearInterval(toast._progressTimer)
    }
    
    toasts.value.splice(index, 1)
  }
}

/**
 * 清除所有Toast通知
 */
const clearAllToasts = () => {
  toasts.value.forEach(toast => {
    if (toast._timer) {
      clearTimeout(toast._timer)
    }
    if (toast._progressTimer) {
      clearInterval(toast._progressTimer)
    }
  })
  toasts.value = []
}

/**
 * 显示成功通知
 */
const showSuccess = (title, message) => {
  addToast({
    type: 'success',
    title,
    message,
    duration: 2000
  })
}

/**
 * 显示错误通知
 */
const showError = (title, message) => {
  addToast({
    type: 'error',
    title,
    message,
    duration: 5000
  })
}

/**
 * 显示警告通知
 */
const showWarning = (title, message) => {
  addToast({
    type: 'warning',
    title,
    message,
    duration: 4000
  })
}

/**
 * 显示信息通知
 */
const showInfo = (title, message) => {
  addToast({
    type: 'info',
    title,
    message,
    duration: 3000
  })
}

// 暴露方法给全局使用
const toastService = {
  addToast,
  removeToast,
  clearAllToasts,
  showSuccess,
  showError,
  showWarning,
  showInfo
}

// 挂载到全局
if (typeof window !== 'undefined') {
  window.$toast = toastService
}

// 组件卸载时清理所有定时器
onUnmounted(() => {
  clearAllToasts()
})

// 暴露方法给父组件
defineExpose({
  addToast,
  removeToast,
  clearAllToasts,
  showSuccess,
  showError,
  showWarning,
  showInfo
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
}

.toast {
  position: relative;
  background: var(--color-bg-primary);
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 4px 20px var(--color-shadow);
  border-left: 4px solid;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 300px;
  overflow: hidden;
}

.toast.success {
  border-left-color: var(--color-success);
}

.toast.error {
  border-left-color: var(--color-danger);
}

.toast.warning {
  border-left-color: var(--color-warning);
}

.toast.info {
  border-left-color: var(--color-info);
}

.toast-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
  margin-top: 2px;
}

.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-weight: 600;
  font-size: 0.95rem;
  margin-bottom: 4px;
  color: var(--color-text-primary);
}

.toast-message {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  line-height: 1.4;
  word-wrap: break-word;
}

.toast-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0;
  line-height: 1;
  flex-shrink: 0;
  transition: color 0.2s;
}

.toast-close:hover {
  color: var(--color-text-secondary);
}

.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-success), #8BC34A);
  transition: width 0.05s linear;
}

.toast.error .toast-progress {
  background: linear-gradient(90deg, var(--color-danger), #ff5722);
}

.toast.warning .toast-progress {
  background: linear-gradient(90deg, var(--color-warning), #ffc107);
}

.toast.info .toast-progress {
  background: linear-gradient(90deg, var(--color-info), #03a9f4);
}

/* 动画效果 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.toast-move {
  transition: transform 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toast-container {
    top: 10px;
    right: 10px;
    left: 10px;
    max-width: none;
  }
  
  .toast {
    min-width: auto;
    width: 100%;
  }
}
</style> 