<template>
  <div class="home-container">
    <div class="welcome-banner">
      <h1>欢迎使用智能交通系统！</h1>
      <p>在这里，您可以一览全局，轻松管理交通信息。</p>
    </div>

    <!-- 当前时间显示 -->
    <div class="time-display">
      <div class="current-time">{{ currentTime }}</div>
      <div class="current-date">{{ currentDate }}</div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <p class="loading-text">正在加载统计数据...</p>
    </div>

    <!-- 统计数据展示 -->
    <div v-else class="metrics-grid">
      <div class="metric-card">
        <h2>交通拥挤度</h2>
        <p class="metric-value" :class="getCongestionClass(dashboardData?.congestionLevel)">
          {{ dashboardData?.congestionLevel || '暂无数据' }}
        </p>
        <p class="metric-desc">当前城市整体交通状况</p>
      </div>
      <div class="metric-card">
        <h2>拥堵路段</h2>
        <p class="metric-value">
          {{ dashboardData?.congestedSections ?? '--' }} 
          <span class="metric-unit">条</span>
        </p>
        <p class="metric-desc">需要关注的拥堵路段数量</p>
      </div>
      <div class="metric-card">
        <h2>系统状态</h2>
        <p class="metric-value" style="color: #67c23a;">良好</p>
        <p class="metric-desc">所有服务均正常运行</p>
      </div>
    </div>

    <!-- 数据更新时间 -->
    <div v-if="dashboardData && lastUpdated" class="update-info">
      <p>数据更新时间: {{ formatTime(lastUpdated) }}</p>
      <button @click="refreshData" class="refresh-button" :disabled="loading">
        {{ loading ? '更新中...' : '手动刷新' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { dataAPI } from '../services/api.js'
import { handleApiError, showSuccess } from '../utils/errorHandler.js'
import { useThemeStore } from '../stores/theme.js'
import dayBg from '../assets/day.png'
import nightBg from '../assets/night.png'

// 响应式数据
const dashboardData = ref(null)
const loading = ref(true)
const lastUpdated = ref(null)
const currentTime = ref('')
const currentDate = ref('')

// 主题相关
const themeStore = useThemeStore()
const bgImage = computed(() => themeStore.isDark ? nightBg : dayBg)

// 自动刷新定时器
let refreshTimer = null
let timeTimer = null

/**
 * 更新当前时间显示
 */
const updateCurrentTime = () => {
  const now = new Date()
  
  // 格式化时间 (HH:MM:SS)
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
  
  // 格式化日期 (YYYY年MM月DD日 星期X)
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

/**
 * 获取仪表盘数据
 */
const fetchDashboardData = async () => {
  loading.value = true
  
  try {
    const response = await dataAPI.getDashboardStats()
    dashboardData.value = response.data
    lastUpdated.value = new Date()
    console.log('仪表盘数据获取成功:', response)
  } catch (err) {
    handleApiError(err, '获取仪表盘数据')
  } finally {
    loading.value = false
  }
}

/**
 * 根据拥挤程度返回对应的CSS类
 */
const getCongestionClass = (level) => {
  switch (level) {
    case '畅通':
      return 'congestion-smooth'
    case '缓慢':
      return 'congestion-slow'
    case '拥堵':
      return 'congestion-busy'
    case '严重拥堵':
      return 'congestion-severe'
    default:
      return 'congestion-unknown'
  }
}

/**
 * 格式化时间显示
 */
const formatTime = (date) => {
  if (!date) return '未知'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 手动刷新数据
 */
const refreshData = async () => {
  await fetchDashboardData()
  showSuccess('数据更新成功', '仪表盘数据已刷新')
}

/**
 * 组件挂载时获取数据并设置自动刷新
 */
onMounted(() => {
  // 立即更新一次时间
  updateCurrentTime()
  
  // 设置时间更新定时器（每秒更新）
  timeTimer = setInterval(() => {
    updateCurrentTime()
  }, 1000)
  
  fetchDashboardData()
  
  // 设置5分钟自动刷新
  refreshTimer = setInterval(() => {
    fetchDashboardData()
  }, 5 * 60 * 1000) // 5分钟
})

/**
 * 组件卸载时清除定时器
 */
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style scoped>
.home-container {
  padding: 40px;
  color: var(--color-text-primary);
  height: 100%;
  overflow-y: auto;
  background-image: v-bind('`url(${bgImage})`');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.welcome-banner {
  text-align: center;
  margin-bottom: 30px;
}

.welcome-banner h1 {
  color: var(--color-text-primary);
  margin-bottom: 10px;
}

.welcome-banner p {
  color: var(--color-text-secondary);
}

.time-display {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.current-time {
  font-size: 2.5rem;
  font-weight: bold;
  color: var(--color-primary);
  margin-bottom: 8px;
}

.current-date {
  font-size: 1.2rem;
  color: var(--color-text-secondary);
}

.loading-container {
  text-align: center;
  padding: 60px 20px;
}

.loading-text {
  font-size: 1.2rem;
  color: var(--color-text-muted);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.metric-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--color-text-primary);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 30px;
  text-align: center;
  transition: all 0.3s ease;
}

.metric-card:hover {
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.metric-card h2 {
  margin: 0 0 15px 0;
  color: var(--color-text-primary);
  font-size: 1.4rem;
  font-weight: 600;
}

.metric-value {
  font-size: 2.5rem;
  font-weight: bold;
  margin: 15px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.metric-unit {
  font-size: 1.2rem;
  color: var(--color-text-secondary);
}

.metric-desc {
  color: var(--color-text-secondary);
  font-size: 0.95rem;
  margin: 0;
  line-height: 1.4;
}

/* 拥挤度等级颜色 */
.congestion-smooth {
  color: var(--color-success) !important;
}

.congestion-slow {
  color: var(--color-warning) !important;
}

.congestion-busy {
  color: var(--color-danger) !important;
}

.congestion-severe {
  color: #9C27B0 !important;
}

.congestion-unknown {
  color: var(--color-text-muted) !important;
}

.update-info {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.update-info p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
}

.refresh-button {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--color-text-primary);
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.refresh-button:hover:not(:disabled) {
  background: rgba(var(--color-primary-rgb), 0.8);
  color: white;
  border-color: rgba(var(--color-primary-rgb), 0.8);
}

.refresh-button:disabled {
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .home-container {
    padding: 20px;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .metric-card {
    padding: 20px;
  }
  
  .metric-value {
    font-size: 2rem;
  }
  
  .current-time {
    font-size: 2rem;
  }
  
  .current-date {
    font-size: 1rem;
  }
  
  .update-info {
    flex-direction: column;
    text-align: center;
  }
}
</style>