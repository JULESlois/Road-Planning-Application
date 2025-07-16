<template>
  <div class="navigation-view">
    <!-- 数据图层面板 -->
    <div class="layer-control card">
      <h4>数据图层</h4>
      <div class="control-item">
        <input 
          type="checkbox" 
          id="heatmap-toggle" 
          v-model="showHeatmap" 
          @change="toggleHeatmap"
          :disabled="loading"
        >
        <label for="heatmap-toggle">交通热力图</label>
      </div>
    </div>

    <!-- 可拖动AI助手 -->
    <DraggableAI />

    <!-- 地图容器 -->
    <div id="navigation-container" class="map-container"></div>
    
    <!-- 右侧操作面板 -->
    <div class="right-panel card">
      <div class="info-section card">
        <h4>当前位置信息</h4>
        <div class="info-content">
          <p><strong>地址:</strong> {{ locationInfo.address }}</p>
          <p><strong>坐标:</strong> {{ locationInfo.coords }}</p>
        </div>
        <div class="info-actions">
          <button @click="setStartFromInfo" class="btn-set">设为起点</button>
          <button @click="setEndFromInfo" class="btn-set">设为终点</button>
        </div>
      </div>

      <div class="input-section">
        <h3>智能路线规划</h3>
        <!-- 起终点输入等内容保持不变 -->
        <div class="location-inputs">
          <div class="input-group">
            <label>起点</label>
            <input 
              type="text" 
              v-model="startPoint" 
              placeholder="点击地图或��上方选点后设置"
              :disabled="loading"
              class="input"
              readonly
            >
            <small v-if="startCoords.lat" class="coord-display">{{ formatCoords(startCoords) }}</small>
          </div>
          <div class="input-group">
            <label>终点</label>
            <input 
              type="text" 
              v-model="endPoint" 
              placeholder="点击地图或在上方选点后设置"
              :disabled="loading"
              class="input"
              readonly
            >
            <small v-if="endCoords.lat" class="coord-display">{{ formatCoords(endCoords) }}</small>
          </div>
          <div class="input-group">
            <label>路线偏好</label>
            <select v-model="routeType" :disabled="loading" class="input">
              <option value="fastest">最快路线</option>
              <option value="shortest">最短路线</option>
              <option value="avoidingTraffic">避堵路线</option>
            </select>
          </div>
          <div class="input-group">
            <label>出发时间: {{ timeSliderValue }}:00</label>
            <input 
              type="range" 
              min="0" 
              max="23" 
              v-model="timeSliderValue"
              :disabled="loading"
              class="time-slider"
            >
          </div>
          <div class="button-group">
            <button 
              @click="planRoute" 
              class="plan-button btn" 
              :disabled="!canPlan || loading"
            >
              {{ loading ? '规划中...' : '开始规划' }}
            </button>
            <button 
              v-if="routes.length > 0" 
              @click="clearRoutes" 
              class="clear-button btn"
            >
              清除路线
            </button>
          </div>
        </div>
      </div>
      <div class="routes-section">
        <h4>路线方案</h4>
        <div v-if="loading" class="loading-container">
          <p>正在为您规划最佳路线...</p>
        </div>
        <div v-else-if="routes.length > 0" class="route-list">
          <div 
            v-for="(route, index) in routes" 
            :key="index" 
            class="route-item card"
            :class="{ active: selectedRoute === index }"
            @click="selectRoute(index)"
          >
            <div class="route-header">
              <h5>{{ getRouteTypeName(route.routeType) }}</h5>
              <span class="route-badge">方案{{ index + 1 }}</span>
            </div>
            <div class="route-details">
              <div class="detail-item">
                <span class="icon">📏</span>
                <span>{{ route.totalDistance.toFixed(2) }} 公里</span>
              </div>
              <div class="detail-item">
                <span class="icon">⏱️</span>
                <span>{{ route.totalTime.toFixed(0) }} 分钟</span>
              </div>
              <div class="detail-item">
                <span class="icon">🚗</span>
                <span>{{ route.points.length }} 个节点</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>请设置起终点后开始路线规划</p>
        </div>
      </div>
    </div>
    <!-- 地图操作提示 -->
    <div class="map-instructions card">
      <p>💡 点击地图任意位置获取详细地址，并可设为起点或终点</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { routeAPI, dataAPI } from '../services/api.js' // Import dataAPI
import { handleApiError, showSuccess, handleMapError } from '../utils/errorHandler.js'
import { useQueryStore } from '../stores/query.js'
import DraggableAI from './DraggableAI.vue' // Import DraggableAI

// Pinia store
const queryStore = useQueryStore()

// 时间滑动条的值与 store 双向绑定
const timeSliderValue = computed({
  get: () => queryStore.selectedTime,
  set: (val) => queryStore.setTime(parseInt(val, 10))
})

// 地图相关
let map = null
let geocoder = null
let heatmap = null // For heatmap layer
let currentPolylines = []
let startMarker = null
let endMarker = null
let selectionMarker = null // For the green selection dot

// Heatmap reactive data
const showHeatmap = ref(false)
const heatmapData = ref([])

// Responsive data
const locationInfo = ref({
  address: '点击地图获取位置信息',
  coords: '',
  lng: null,
  lat: null,
})
const startPoint = ref('')
const endPoint = ref('')
const startCoords = ref({ lat: null, lng: null })
const endCoords = ref({ lat: null, lng: null })
const routeType = ref('fastest')
const routes = ref([])
const selectedRoute = ref(-1)
const loading = ref(false)

// 计算属性
const canPlan = computed(() => {
  return startCoords.value.lat && startCoords.value.lng && 
         endCoords.value.lat && endCoords.value.lng
})

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
    plugins: ['AMap.Polyline', 'AMap.Marker', 'AMap.Geocoder', 'AMap.HeatMap'], // Add AMap.HeatMap
  }).then((AMap) => {
    map = new AMap.Map("navigation-container", {
      viewMode: "3D",
      zoom: 16, // Zoom level for a campus view
      center: [116.3428, 39.9522], // Beijing Jiaotong University
    })

    // Initialize Geocoder
    geocoder = new AMap.Geocoder({
      city: "全国"
    });

    // Initialize Heatmap
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
    });

    // Map click event
    map.on('click', (e) => {
      const { lng, lat } = e.lnglat
      
      // Update current location info
      updateLocationInfo(lng, lat)
    })

    console.log('导航地图初始化完成')
  }).catch(e => {
    console.error("地图加载失败：", e)
    handleMapError(e)
  })
})

/**
 * 更新当前点击位置的信息
 */
const updateLocationInfo = (lng, lat) => {
  // First, update the location info in the panel regardless
  locationInfo.value.coords = `${lng.toFixed(6)}, ${lat.toFixed(6)}`
  locationInfo.value.lng = lng
  locationInfo.value.lat = lat
  
  geocoder.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result.info === 'OK') {
      locationInfo.value.address = result.regeocode.formattedAddress
    } else {
      locationInfo.value.address = '无法获取地址信息'
      console.error('逆地理编码失败:', result)
    }
  })

  // If both start and end points are already set, do not show the green dot.
  if (startCoords.value.lat && endCoords.value.lat) {
    if (selectionMarker) {
      selectionMarker.hide();
    }
    return;
  }

  // Create or move the green selection marker
  if (selectionMarker) {
    selectionMarker.setPosition([lng, lat]);
    selectionMarker.show();
  } else {
    selectionMarker = new AMap.Marker({
      position: [lng, lat],
      content: `<div style="background-color: #67c23a; width: 16px; height: 16px; border-radius: 50%; border: 2px solid #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.4);"></div>`,
      offset: new AMap.Pixel(-9, -9),
      title: '当前选择'
    });
    selectionMarker.setMap(map);
  }
}

/**
 * Toggle Heatmap Layer
 */
const toggleHeatmap = async () => {
  if (showHeatmap.value) {
    await loadHeatmapData();
  } else {
    if (heatmap) {
      heatmap.hide();
    }
  }
}

/**
 * Load Heatmap Data from API
 */
const loadHeatmapData = async () => {
  loading.value = true; // Use the existing loading state
  try {
    const response = await dataAPI.getHeatmapData();
    heatmapData.value = response.data;
    
    if (heatmap && heatmapData.value.length > 0) {
      heatmap.setDataSet({
        data: heatmapData.value.map(item => ({
          lng: item.longitude,
          lat: item.latitude,
          count: item.flow
        })),
        max: Math.max(...heatmapData.value.map(item => item.flow))
      });
      heatmap.show();
      showSuccess('加载成功', '热力图数据已更新');
    }
  } catch (err) {
    handleApiError(err, '加载热力图数据');
    showHeatmap.value = false; // Turn off toggle if loading fails
  } finally {
    loading.value = false;
  }
}

/**
 * 从信息面板设置起点
 */
const setStartFromInfo = () => {
  if (locationInfo.value.lat && locationInfo.value.lng) {
    setStartPoint(locationInfo.value.lng, locationInfo.value.lat, locationInfo.value.address)
    if (selectionMarker) {
      selectionMarker.hide();
    }
  } else {
    showSuccess('请先点击地图选择位置', '未选择任何有效点')
  }
}

/**
 * 从信息面板设置终点
 */
const setEndFromInfo = () => {
  if (locationInfo.value.lat && locationInfo.value.lng) {
    setEndPoint(locationInfo.value.lng, locationInfo.value.lat, locationInfo.value.address)
    if (selectionMarker) {
      selectionMarker.hide();
    }
  } else {
    showSuccess('请先点击地图选择位置', '未选择任何有效点')
  }
}

/**
 * 设置起点
 */
const setStartPoint = (lng, lat, address) => {
  startCoords.value = { lng, lat }
  startPoint.value = address || `起点: ${lng.toFixed(4)}, ${lat.toFixed(4)}`
  
  // 清除旧的起点标记
  if (startMarker) {
    startMarker.setMap(null)
  }
  
  // 创建新的起点标记
  startMarker = new AMap.Marker({
    position: [lng, lat],
    content: `<div style="background-color: #f56c6c; width: 16px; height: 16px; border-radius: 50%; border: 2px solid #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.4);"></div>`,
    offset: new AMap.Pixel(-9, -9), // Adjust offset for the center of the dot
    title: '起点'
  })
  
  startMarker.setMap(map)
  showSuccess('起点设置成功', address)
}

/**
 * 设置终点
 */
const setEndPoint = (lng, lat, address) => {
  endCoords.value = { lng, lat }
  endPoint.value = address || `终点: ${lng.toFixed(4)}, ${lat.toFixed(4)}`
  
  // 清除旧的终点标记
  if (endMarker) {
    endMarker.setMap(null)
  }
  
  // 创建新的终点标记
  endMarker = new AMap.Marker({
    position: [lng, lat],
    content: `<div style="background-color: #409eff; width: 16px; height: 16px; border-radius: 50%; border: 2px solid #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.4);"></div>`,
    offset: new AMap.Pixel(-9, -9), // Adjust offset for the center of the dot
    title: '终点'
  })
  
  endMarker.setMap(map)
  showSuccess('终点设置成功', address)
}

/**
 * 规划路线
 */
const planRoute = async () => {
  if (!canPlan.value) return
  
  loading.value = true
  
  try {
    const response = await routeAPI.planRoute({
      startLat: startCoords.value.lat,
      startLng: startCoords.value.lng,
      endLat: endCoords.value.lat,
      endLng: endCoords.value.lng,
      routeType: routeType.value
      // 注意：time参数已被移除，因为它未在当前后端接口中使用
    })
    
    routes.value = response.data
    
    if (routes.value.length > 0) {
      selectRoute(0) // 默认选择第一条路线
      showSuccess('路线规划成功', `找到 ${routes.value.length} 条可选路线`)
    } else {
      showSuccess('规划完成', '未找到合适的路线，请调整起终点位置')
    }
    
  } catch (err) {
    handleApiError(err, '路线规划')
  } finally {
    loading.value = false
  }
}

/**
 * 选择路线
 */
const selectRoute = (index) => {
  selectedRoute.value = index
  const route = routes.value[index]
  
  if (route && route.points) {
    displayRoute(route)
  }
}

/**
 * 在地图上显示路线
 */
const displayRoute = (route) => {
  // 清除之前的路线
  clearPolylines()
  
  // 创建路线折线
  const path = route.points.map(point => [point.longitude, point.latitude])
  
  const polyline = new AMap.Polyline({
    path: path,
    borderWeight: 2,
    strokeColor: "#3366FF",
    strokeOpacity: 0.8,
    strokeWeight: 6,
    strokeStyle: "solid"
  })
  
  polyline.setMap(map)
  currentPolylines.push(polyline)
  
  // 调整地图视野以包含整条路线
  map.setFitView([polyline])
}

/**
 * 清除地图上的路线
 */
const clearPolylines = () => {
  currentPolylines.forEach(polyline => {
    polyline.setMap(null)
  })
  currentPolylines = []
}

/**
 * 清除所有路线
 */
const clearRoutes = () => {
  routes.value = []
  selectedRoute.value = -1
  clearPolylines()

  // Also clear start and end points and their markers
  startPoint.value = ''
  endPoint.value = ''
  startCoords.value = { lat: null, lng: null }
  endCoords.value = { lat: null, lng: null }
  locationInfo.value.address = '点击地图获取位置信息'
  locationInfo.value.coords = ''

  if (startMarker) {
    startMarker.setMap(null)
    startMarker = null
  }
  if (endMarker) {
    endMarker.setMap(null)
    endMarker = null
  }
  if (selectionMarker) {
    selectionMarker.setMap(null)
    selectionMarker = null
  }

  showSuccess('路线已清除', '所有路线及起终点已清理')
}

/**
 * 获取路线类型名称
 */
const getRouteTypeName = (type) => {
  const typeNames = {
    fastest: '最快路线',
    shortest: '最短路线',
    avoidingTraffic: '避堵路线'
  }
  return typeNames[type] || '推荐路线'
}

/**
 * 格式化坐标显示
 */
const formatCoords = (coords) => {
  if (!coords.lat || !coords.lng) return ''
  return `${coords.lng.toFixed(4)}, ${coords.lat.toFixed(4)}`
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
/* Layer Control Panel */
.layer-control {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 220px;
  padding: 16px;
  z-index: 100;
  background: var(--color-bg-overlay);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
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

/* AI Assistant is positioned by its own component logic, this is a placeholder if needed */
/* The DraggableAI component should handle its own positioning. */
/* We assume it will appear on the page and be draggable. */

.navigation-view {
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  min-height: 100vh;
  display: flex;
  flex-direction: row; /* Reverted to original */
  width: 100%;
  height: 100%;
  position: relative;
}

.map-container {
  flex: 1;
  position: relative;
  min-width: 0;
  min-height: 100vh;
}

.right-panel {
  width: 400px;
  background: var(--color-bg-primary);
  border-left: 1px solid var(--color-border-primary);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 20px;
  box-shadow: -2px 0 8px rgba(0,0,0,0.04);
  z-index: 10;
  gap: 20px;
}

.info-section {
  padding: 16px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
}

.info-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
}

.info-content p {
  margin: 4px 0;
  font-size: 14px;
  color: var(--color-text-secondary);
  word-break: break-all;
}

.info-content p strong {
  color: var(--color-text-primary);
}

.info-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

.btn-set {
  flex: 1;
  padding: 8px;
  font-size: 14px;
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border-primary);
}

.btn-set:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.input-section {
  margin-bottom: 0; /* Adjusted from 30px */
}

.input-section h3 {
  margin: 0 0 20px 0;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
}

.location-inputs {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  color: var(--color-text-secondary);
  font-weight: 500;
  font-size: 14px;
}

.time-slider {
  width: 100%;
  cursor: pointer;
}

.coord-display {
  color: var(--color-text-muted);
  font-size: 12px;
  font-style: italic;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.plan-button {
  background: var(--color-primary);
  color: white;
  border: 1px solid var(--color-primary);
}

.plan-button:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.plan-button:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  border-color: var(--color-border-primary);
  cursor: not-allowed;
}

.clear-button {
  background: var(--color-bg-secondary);
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
}

.clear-button:hover {
  background: var(--color-danger);
  color: white;
}

.routes-section {
  flex: 1;
  padding: 20px;
  background: var(--color-bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--color-border-primary);
}

.routes-section h4 {
  margin: 0 0 16px 0;
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
}

.loading-container {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-muted);
}

.route-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-item {
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border-primary);
}

.route-item:hover {
  border-color: var(--color-border-hover);
  background: var(--color-bg-secondary);
}

.route-item.active {
  border-color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 5%, var(--color-bg-primary));
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.route-header h5 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.route-badge {
  background: var(--color-primary);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.route-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.detail-item .icon {
  width: 16px;
  font-size: 12px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-muted);
}

.map-instructions {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  background: var(--color-bg-overlay);
  z-index: 100;
  text-align: center;
  backdrop-filter: blur(8px);
}

.map-instructions p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .navigation-view {
    flex-direction: column; /* Panel on top */
  }
  
  .right-panel {
    width: 100%;
    max-height: 400px; /* Increased height */
    border-left: none;
    border-bottom: 1px solid var(--color-border-primary);
  }
  
  .routes-section {
    max-height: 200px;
    overflow-y: auto;
  }
}

@media (max-width: 768px) {
  .right-panel {
    padding: 16px;
    max-height: 350px; /* Adjusted height */
  }
  
  .input-section h3 {
    font-size: 16px;
  }
  
  .location-inputs {
    gap: 16px;
  }
  
  .map-instructions {
    left: 10px;
    right: 10px;
    transform: none;
    padding: 8px 12px;
  }
  
  .map-instructions p {
    font-size: 12px;
  }
}
</style>