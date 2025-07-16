<template>
  <div class="map-query-view">
    <div id="map-query-container" class="map-container"></div>
    
    <!-- 图层控制面板 -->
    <div class="layer-control card">
      <h4>数据图层</h4>
      
      <!-- 热力图控制 -->
      <div class="control-item">
        <input 
          type="checkbox" 
          id="heatmap-toggle" 
          v-model="showHeatmap" 
          @change="toggleHeatmap"
          :disabled="loading"
        >
        <label for="heatmap-toggle">交通热力图</label>
        <span v-if="loading && loadingType === '热力图数据'" class="loading-indicator">加载中...</span>
      </div>
      
      <!-- 拥挤度控制 -->
      <div class="control-item">
        <input 
          type="checkbox" 
          id="congestion-toggle" 
          v-model="showCongestion" 
          @change="toggleCongestion"
          :disabled="loading"
        >
        <label for="congestion-toggle">拥挤度分析</label>
        <span v-if="loading && loadingType === '拥挤度数据'" class="loading-indicator">加载中...</span>
      </div>
      
      <!-- 刷新按钮 -->
      <div class="control-item">
        <button @click="refreshData" class="refresh-btn btn" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新数据' }}
        </button>
      </div>
      
      <!-- 数据更新时间 -->
      <div v-if="lastUpdated" class="update-time">
        <small>更新: {{ formatTime(lastUpdated) }}</small>
      </div>
    </div>
    
    <!-- 拥挤度统计面板 -->
    <div 
      v-if="congestionStats && showCongestion" 
      class="congestion-stats draggable-panel"
      :style="{ left: panelPosition.x + 'px', top: panelPosition.y + 'px' }"
      @mousedown="startDragPanel"
    >
      <div class="panel-header" @mousedown="startDragPanel">
        <h4>拥挤度统计</h4>
        <div class="drag-indicator">⋮⋮</div>
      </div>
      <div class="stats-grid">
        <div 
          v-for="region in congestionStats.regions" 
          :key="region.level" 
          class="stat-item"
          :class="getCongestionClass(region.level)"
        >
          <span class="level">{{ region.level }}</span>
          <span class="value">{{ region.value.toFixed(1) }}%</span>
        </div>
      </div>
      <div class="update-info">
        <small>数据时间: {{ congestionStats.updateTime }}</small>
      </div>
    </div>
    
    <!-- 可拖动AI助手 -->
    <DraggableAI />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { dataAPI } from '../services/api.js'
import { handleApiError, showSuccess, handleMapError } from '../utils/errorHandler.js'
import DraggableAI from './DraggableAI.vue'

// 地图相关
let map = null
let heatmap = null
let infoWindow = null
let congestionMarkers = []

// 响应式数据
const showHeatmap = ref(false)
const showCongestion = ref(false)
const loading = ref(false)
const loadingType = ref('')
const lastUpdated = ref(null)
const panelPosition = ref({ x: window.innerWidth - 320, y: 80 })
const isDraggingPanel = ref(false)
const dragOffset = ref({ x: 0, y: 0 })

// 数据
const heatmapData = ref([])
const congestionStats = ref(null)

// 配置安全密钥
window._AMapSecurityConfig = {
  securityJsCode: 'aceef7681b1b9bcfe0b886af40c120f1',
}

/**
 * 初始化地图
 */
onMounted(() => {
  AMapLoader.load({
    key: "2bec7f50935812fcf29c98e01c419f02",
    version: "2.0",
    plugins: ['AMap.HeatMap', 'AMap.InfoWindow', 'AMap.Marker'],
  }).then((AMap) => {
    map = new AMap.Map("map-query-container", {
      viewMode: "3D",
      zoom: 16, // Zoom level for a campus view
      center: [116.3428, 39.9522], // Beijing Jiaotong University
    })

    // 初始化信息窗体
    infoWindow = new AMap.InfoWindow({
      offset: new AMap.Pixel(0, -30),
      closeWhenClickMap: true
    })

    // 地图点击显示经纬度
    map.on('click', (e) => {
      const { lng, lat } = e.lnglat
      infoWindow.setContent(`<div style='font-size:14px;'>经度: ${lng.toFixed(6)}<br>纬度: ${lat.toFixed(6)}</div>`)
      infoWindow.open(map, [lng, lat])
    })

    // 初始化热力图
    heatmap = new AMap.HeatMap(map, {
      radius: 30,
      opacity: [0, 0.8],
      gradient: {
        0.4: 'blue',
        0.6: 'cyan',
        0.7: 'lime',
        0.8: 'yellow',
        1.0: 'red'
      }
    })

    console.log('地图查询页面初始化完成')
    
    // 自动加载默认数据
    refreshData()
  }).catch(e => {
    console.error("地图加载失败：", e)
    handleMapError(e)
  })
})

/**
 * 切换热力图显示
 */
const toggleHeatmap = async () => {
  if (showHeatmap.value) {
    await loadHeatmapData()
  } else {
    if (heatmap) {
      heatmap.hide()
    }
  }
}

/**
 * 切换拥挤度显示
 */
const toggleCongestion = async () => {
  if (showCongestion.value) {
    await loadCongestionData()
  } else {
    clearCongestionMarkers()
    congestionStats.value = null
  }
}

/**
 * 加载热力图数据
 */
const loadHeatmapData = async () => {
  loading.value = true
  loadingType.value = '热力图数据'
  
  try {
    const response = await dataAPI.getHeatmapData()
    heatmapData.value = response.data
    
    if (heatmap && heatmapData.value.length > 0) {
      heatmap.setDataSet({
        data: heatmapData.value.map(item => ({
          lng: item.longitude,
          lat: item.latitude,
          count: item.flow
        })),
        max: Math.max(...heatmapData.value.map(item => item.flow))
      })
      heatmap.show()
      
      showSuccess('加载成功', '热力图数据已更新')
    }
    
  } catch (err) {
    handleApiError(err, '加载热力图数据')
    showHeatmap.value = false
  } finally {
    loading.value = false
    loadingType.value = ''
  }
}

/**
 * 加载拥挤度数据
 */
const loadCongestionData = async () => {
  loading.value = true
  loadingType.value = '拥挤度数据'
  
  try {
    const response = await dataAPI.getCongestionData()
    congestionStats.value = response.data
    
    // 在地图上标注拥挤度点位
    displayCongestionMarkers(response.data.regions)
    
    showSuccess('加载成功', '拥挤度数据已更新')
    
  } catch (err) {
    handleApiError(err, '加载拥挤度数据')
    showCongestion.value = false
  } finally {
    loading.value = false
    loadingType.value = ''
  }
}

/**
 * 显示拥挤度标记
 */
const displayCongestionMarkers = (regions) => {
  clearCongestionMarkers()
  
  regions.forEach(region => {
    if (region.coordinates) {
      const marker = new AMap.Marker({
        position: [region.coordinates.lng, region.coordinates.lat],
        icon: getCongestionIcon(region.level),
        title: `拥挤度: ${region.level} (${region.value.toFixed(1)}%)`
      })
      
      marker.on('click', () => {
        infoWindow.setContent(`
          <div class="congestion-info">
            <h4>${region.level}</h4>
            <p>拥挤度: ${region.value.toFixed(1)}%</p>
            <p>区域: ${region.name || '未知区域'}</p>
          </div>
        `)
        infoWindow.open(map, marker.getPosition())
      })
      
      map.add(marker)
      congestionMarkers.push(marker)
    }
  })
}

/**
 * 获取拥挤度图标
 */
const getCongestionIcon = (level) => {
  const colors = {
    '畅通': '#4CAF50',
    '缓慢': '#FF9800',
    '拥堵': '#F44336',
    '严重拥堵': '#9C27B0'
  }
  
  return `data:image/svg+xml,${encodeURIComponent(`
    <svg width="20" height="20" xmlns="http://www.w3.org/2000/svg">
      <circle cx="10" cy="10" r="8" fill="${colors[level] || '#999'}" stroke="white" stroke-width="2"/>
    </svg>
  `)}`
}

/**
 * 清除拥挤度标记
 */
const clearCongestionMarkers = () => {
  congestionMarkers.forEach(marker => {
    map.remove(marker)
  })
  congestionMarkers = []
}

/**
 * 获取拥挤度样式类
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
 * 刷新数据
 */
const refreshData = async () => {
  lastUpdated.value = new Date()
  
  // 刷新当前启用的数据层
  const promises = []
  
  if (showHeatmap.value) {
    promises.push(loadHeatmapData())
  }
  
  if (showCongestion.value) {
    promises.push(loadCongestionData())
  }
  
  // 如果没有启用任何数据层，默认加载热力图
  if (promises.length === 0) {
    showHeatmap.value = true
    promises.push(loadHeatmapData())
  }
  
  await Promise.allSettled(promises)
}

/**
 * 格式化时间
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
 * 开始拖动面板
 */
const startDragPanel = (e) => {
  e.preventDefault()
  isDraggingPanel.value = true
  const rect = e.currentTarget.getBoundingClientRect()
  dragOffset.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
  document.addEventListener('mousemove', handleDragPanel)
  document.addEventListener('mouseup', stopDragPanel)
  document.body.style.userSelect = 'none'
}

/**
 * 处理面板拖动
 */
const handleDragPanel = (e) => {
  if (!isDraggingPanel.value) return
  const newX = e.clientX - dragOffset.value.x
  const newY = e.clientY - dragOffset.value.y
  const maxX = window.innerWidth - 300
  const maxY = window.innerHeight - 200
  panelPosition.value.x = Math.max(0, Math.min(newX, maxX))
  panelPosition.value.y = Math.max(0, Math.min(newY, maxY))
}

/**
 * 停止拖动面板
 */
const stopDragPanel = () => {
  isDraggingPanel.value = false
  document.removeEventListener('mousemove', handleDragPanel)
  document.removeEventListener('mouseup', stopDragPanel)
  document.body.style.userSelect = ''
}

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})
</script>

<style scoped>
.map-query-view {
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  min-height: 100vh;
  position: relative;
  width: 100%;
  height: 100%;
}

.map-container {
  width: 100%;
  height: 100vh;
}

/* 图层控制面板 */
.layer-control {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 300px;
  padding: 20px;
  z-index: 100;
}

.layer-control h4 {
  margin: 0 0 16px 0;
  color: var(--color-text-primary);
  font-size: 16px;
}

.control-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 8px 0;
}

.control-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

.control-item label {
  flex: 1;
  color: var(--color-text-primary);
  font-size: 14px;
  cursor: pointer;
}

.refresh-btn {
  flex: 1;
  padding: 8px 16px;
  font-size: 14px;
}

.loading-indicator {
  color: var(--color-text-muted);
  font-size: 12px;
  font-style: italic;
}

.update-time {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-primary);
}

.update-time small {
  color: var(--color-text-secondary);
  font-size: 12px;
}

/* 拥挤度统计面板 */
.draggable-panel {
  position: absolute;
  top: 80px;
  right: 20px;
  width: 280px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 100;
  cursor: move;
  user-select: none;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  cursor: move;
}

.panel-header h4 {
  margin: 0 0 16px 0;
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
}

.drag-indicator {
  color: var(--color-text-muted);
  font-size: 12px;
  cursor: move;
  opacity: 0.6;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(5px);
}

.stat-item .level {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.stat-item .value {
  font-size: 16px;
  font-weight: bold;
  color: var(--color-text-primary);
}

/* 拥挤度等级颜色 */
.congestion-smooth {
  border-color: rgba(var(--color-success-rgb), 0.3);
  background: rgba(var(--color-success-rgb), 0.1);
}

.congestion-smooth .value {
  color: var(--color-success);
}

.congestion-slow {
  border-color: rgba(var(--color-warning-rgb), 0.3);
  background: rgba(var(--color-warning-rgb), 0.1);
}

.congestion-slow .value {
  color: var(--color-warning);
}

.congestion-busy {
  border-color: rgba(var(--color-danger-rgb), 0.3);
  background: rgba(var(--color-danger-rgb), 0.1);
}

.congestion-busy .value {
  color: var(--color-danger);
}

.congestion-severe {
  border-color: rgba(156, 39, 176, 0.3);
  background: rgba(156, 39, 176, 0.1);
}

.congestion-severe .value {
  color: #9C27B0;
}

.update-info {
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
}

.update-info small {
  color: var(--color-text-muted);
  font-size: 12px;
}

/* 信息窗口样式 */
.congestion-info {
  padding: 12px;
  min-width: 150px;
}

.congestion-info h4 {
  margin: 0 0 8px 0;
  color: var(--color-text-primary);
}

.congestion-info p {
  margin: 4px 0;
  color: var(--color-text-secondary);
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .layer-control,
  .congestion-stats {
    width: 240px;
    padding: 16px;
  }
  
  .layer-control {
    top: 10px;
    left: 10px;
  }
  
  .congestion-stats {
    top: 10px;
    right: 10px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>