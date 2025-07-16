<template>
  <div class="music-player">
    <div class="player-header">
      <h3>🎵 音乐播放器</h3>
      <div class="player-status" :class="{ playing: isPlaying }">
        {{ isPlaying ? '播放中' : '已暂停' }}
      </div>
    </div>

    <!-- 文件缺失提示 -->
    <div v-if="showFileWarning" class="file-warning">
      <div class="warning-content">
        <span class="warning-icon">⚠️</span>
        <span class="warning-text">音乐文件未找到，请添加音频文件到 assets/music 目录</span>
      </div>
      <button @click="showFileWarning = false" class="warning-close">×</button>
    </div>

    <!-- 当前播放信息 -->
    <div class="current-track" v-if="currentTrack">
      <div class="track-info">
        <div class="track-title">{{ currentTrack.title }}</div>
        <div class="track-artist">{{ currentTrack.artist }}</div>
      </div>
      <div class="track-cover">
        <div class="cover-placeholder">🎵</div>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="progress-container">
      <div class="time-display">
        <span>{{ formatTime(currentTime) }}</span>
        <span>{{ formatTime(duration) }}</span>
      </div>
      <div class="progress-bar" @click="seekTo">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        <div class="progress-thumb" :style="{ left: progressPercent + '%' }"></div>
      </div>
    </div>

    <!-- 控制按钮 -->
    <div class="controls">
      <button @click="previousTrack" class="control-btn" :disabled="!canGoPrevious">
        ⏮️
      </button>
      <button @click="togglePlay" class="play-btn">
        {{ isPlaying ? '⏸️' : '▶️' }}
      </button>
      <button @click="nextTrack" class="control-btn" :disabled="!canGoNext">
        ⏭️
      </button>
      <button @click="toggleShuffle" class="control-btn" :class="{ active: shuffle }">
        🔀
      </button>
      <button @click="toggleRepeat" class="control-btn" :class="{ active: repeat }">
        🔁
      </button>
    </div>

    <!-- 音量控制 -->
    <div class="volume-control">
      <span class="volume-icon">🔊</span>
      <input 
        type="range" 
        min="0" 
        max="100" 
        v-model="volume" 
        @input="updateVolume"
        class="volume-slider"
      >
      <span class="volume-value">{{ volume }}%</span>
    </div>

    <!-- 播放列表 -->
    <div class="playlist-section">
      <div class="playlist-header">
        <h4>播放列表 ({{ playlist.length }})</h4>
        <button @click="togglePlaylist" class="toggle-btn">
          {{ showPlaylist ? '收起' : '展开' }}
        </button>
      </div>
      
      <div v-if="showPlaylist" class="playlist">
        <div 
          v-for="(track, index) in playlist" 
          :key="index"
          class="playlist-item"
          :class="{ active: currentTrackIndex === index }"
          @click="playTrack(index)"
        >
          <div class="track-number">{{ index + 1 }}</div>
          <div class="track-details">
            <div class="track-name">{{ track.title }}</div>
            <div class="track-duration">{{ formatTime(track.duration) }}</div>
          </div>
          <div class="track-status">
            <span v-if="currentTrackIndex === index && isPlaying">▶️</span>
            <span v-else-if="currentTrackIndex === index">⏸️</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

// 响应式数据
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(50)
const shuffle = ref(false)
const repeat = ref(false)
const showPlaylist = ref(false)
const currentTrackIndex = ref(0)
const showFileWarning = ref(false)

// 音频元素
let audio = null

// 播放列表 - 自动读取 assets/music 下所有 mp3 文件
const musicModules = import.meta.glob('../assets/music/*.mp3', { eager: true });
const playlist = ref(
  Object.keys(musicModules).map((path) => {
    // 提取文件名（去除扩展名）
    const fileName = decodeURIComponent(path.split('/').pop().replace('.mp3', ''));
    return {
      title: fileName,
      artist: '未知',
      src: musicModules[path].default,
      duration: 0 // 稍后自动获取
    };
  })
)

// 计算属性
const currentTrack = computed(() => {
  return playlist.value[currentTrackIndex.value] || null
})

const progressPercent = computed(() => {
  return duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0
})

const canGoPrevious = computed(() => {
  return currentTrackIndex.value > 0 || repeat.value
})

const canGoNext = computed(() => {
  return currentTrackIndex.value < playlist.value.length - 1 || repeat.value
})

/**
 * 初始化音频播放器
 */
const initAudio = () => {
  audio = new Audio()
  audio.preload = 'metadata'
  
  // 设置初始音量
  audio.volume = volume.value / 100
  
  // 事件监听
  audio.addEventListener('loadedmetadata', () => {
    duration.value = audio.duration
  })
  
  audio.addEventListener('timeupdate', () => {
    currentTime.value = audio.currentTime
  })
  
  audio.addEventListener('ended', () => {
    handleTrackEnd()
  })
  
  audio.addEventListener('error', (e) => {
    console.error('音频播放错误:', e)
    handleAudioError()
  })
  
  // 检查第一个文件是否存在
  if (playlist.value.length > 0) {
    checkFileExists(playlist.value[0].src).then(exists => {
      if (exists) {
        loadTrack(0)
      } else {
        showFileWarning.value = true
        console.warn('音乐文件不存在，请添加音频文件')
      }
    })
  }
}

/**
 * 加载指定曲目
 */
const loadTrack = (index) => {
  if (index < 0 || index >= playlist.value.length) return
  
  const track = playlist.value[index]
  
  // 检查文件是否存在
  checkFileExists(track.src).then(exists => {
    if (!exists) {
      console.warn(`音频文件不存在: ${track.src}`)
      showFileError(track.title)
      return
    }
    
    audio.src = track.src
    currentTrackIndex.value = index
    currentTime.value = 0
    duration.value = track.duration
    
    console.log('加载曲目:', track.title)
  }).catch(err => {
    console.error('检查文件失败:', err)
    showFileError(track.title)
  })
}

/**
 * 检查文件是否存在
 */
const checkFileExists = async (url) => {
  try {
    const response = await fetch(url, { method: 'HEAD' })
    return response.ok
  } catch (error) {
    return false
  }
}

/**
 * 播放/暂停切换
 */
const togglePlay = () => {
  if (!audio) return
  
  if (isPlaying.value) {
    audio.pause()
    isPlaying.value = false
  } else {
    audio.play().catch(e => {
      console.error('播放失败:', e)
      handleAudioError()
    })
    isPlaying.value = true
  }
}

/**
 * 上一首
 */
const previousTrack = () => {
  if (currentTrackIndex.value > 0) {
    loadTrack(currentTrackIndex.value - 1)
  } else if (repeat.value) {
    loadTrack(playlist.value.length - 1)
  }
  
  if (isPlaying.value) {
    audio.play()
  }
}

/**
 * 下一首
 */
const nextTrack = () => {
  if (currentTrackIndex.value < playlist.value.length - 1) {
    loadTrack(currentTrackIndex.value + 1)
  } else if (repeat.value) {
    loadTrack(0)
  }
  
  if (isPlaying.value) {
    audio.play()
  }
}

/**
 * 播放指定曲目
 */
const playTrack = (index) => {
  loadTrack(index)
  audio.play().then(() => {
    isPlaying.value = true
  }).catch(e => {
    console.error('播放失败:', e)
    handleAudioError()
  })
}

/**
 * 切换随机播放
 */
const toggleShuffle = () => {
  shuffle.value = !shuffle.value
  console.log('随机播放:', shuffle.value ? '开启' : '关闭')
}

/**
 * 切换循环播放
 */
const toggleRepeat = () => {
  repeat.value = !repeat.value
  console.log('循环播放:', repeat.value ? '开启' : '关闭')
}

/**
 * 更新音量
 */
const updateVolume = () => {
  if (audio) {
    audio.volume = volume.value / 100
  }
}

/**
 * 进度条跳转
 */
const seekTo = (event) => {
  if (!audio || !duration.value) return
  
  const rect = event.currentTarget.getBoundingClientRect()
  const percent = (event.clientX - rect.left) / rect.width
  const newTime = percent * duration.value
  
  audio.currentTime = newTime
  currentTime.value = newTime
}

/**
 * 处理曲目结束
 */
const handleTrackEnd = () => {
  isPlaying.value = false
  
  if (shuffle.value) {
    // 随机播放下一首
    const nextIndex = Math.floor(Math.random() * playlist.value.length)
    loadTrack(nextIndex)
    audio.play()
    isPlaying.value = true
  } else if (repeat.value) {
    // 循环播放当前曲目
    audio.currentTime = 0
    audio.play()
    isPlaying.value = true
  } else if (currentTrackIndex.value < playlist.value.length - 1) {
    // 播放下一首
    nextTrack()
  }
}

/**
 * 处理音频错误
 */
const handleAudioError = () => {
  isPlaying.value = false
  
  // 显示文件缺失警告
  showFileWarning.value = true
  
  console.warn('音频文件加载失败，可能是文件不存在')
  
  // 显示当前曲目的错误信息
  if (currentTrack.value) {
    showFileError(currentTrack.value.title)
  }
  
  // 尝试播放下一首
  if (currentTrackIndex.value < playlist.value.length - 1) {
    setTimeout(() => {
      nextTrack()
    }, 1000)
  }
}

/**
 * 显示文件错误提示
 */
const showFileError = (trackTitle) => {
  // 显示文件缺失警告
  showFileWarning.value = true
  
  console.warn(`音频文件 "${trackTitle}" 不存在，请检查文件路径`)
  
  // 自动尝试播放下一首
  if (currentTrackIndex.value < playlist.value.length - 1) {
    setTimeout(() => {
      nextTrack()
    }, 1000)
  }
}

/**
 * 切换播放列表显示
 */
const togglePlaylist = () => {
  showPlaylist.value = !showPlaylist.value
}

/**
 * 格式化时间显示
 */
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '0:00'
  
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

/**
 * 组件挂载时初始化
 */
onMounted(() => {
  playlist.value.forEach((item, idx) => {
    const audioEl = new Audio(item.src);
    audioEl.addEventListener('loadedmetadata', () => {
      playlist.value[idx].duration = Math.floor(audioEl.duration);
    });
  });
  initAudio()
})

/**
 * 组件卸载时清理资源
 */
onUnmounted(() => {
  if (audio) {
    audio.pause()
    audio.src = ''
    audio = null
  }
})
</script>

<style scoped>
.music-player {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
  padding: 20px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.player-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.player-status {
  font-size: 0.8rem;
  padding: 4px 8px;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.2);
}

.player-status.playing {
  background-color: rgba(76, 175, 80, 0.8);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.file-warning {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.warning-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.warning-icon {
  font-size: 1.2rem;
}

.warning-text {
  font-size: 0.9rem;
  opacity: 0.9;
}

.warning-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: white;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.warning-close:hover {
  opacity: 1;
}

.current-track {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.track-info {
  flex: 1;
}

.track-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 5px;
}

.track-artist {
  font-size: 0.9rem;
  opacity: 0.8;
}

.track-cover {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder {
  font-size: 2rem;
}

.progress-container {
  margin-bottom: 20px;
}

.time-display {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  margin-bottom: 8px;
  opacity: 0.8;
}

.progress-bar {
  position: relative;
  height: 6px;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  cursor: pointer;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  border-radius: 3px;
  transition: width 0.1s ease;
}

.progress-thumb {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background-color: white;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.2s;
}

.progress-bar:hover .progress-thumb {
  opacity: 1;
}

.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.control-btn, .play-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s;
  opacity: 0.8;
}

.control-btn:hover, .play-btn:hover {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.1);
}

.play-btn {
  font-size: 2rem;
  background-color: rgba(255, 255, 255, 0.2);
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.control-btn.active {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.2);
}

.control-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.volume-icon {
  font-size: 1rem;
  opacity: 0.8;
}

.volume-slider {
  flex: 1;
  height: 4px;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  outline: none;
  cursor: pointer;
}

.volume-slider::-webkit-slider-thumb {
  appearance: none;
  width: 12px;
  height: 12px;
  background-color: white;
  border-radius: 50%;
  cursor: pointer;
}

.volume-value {
  font-size: 0.8rem;
  opacity: 0.8;
  min-width: 35px;
}

.playlist-section {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 15px;
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.playlist-header h4 {
  margin: 0;
  font-size: 1rem;
  opacity: 0.9;
}

.toggle-btn {
  background: none;
  border: none;
  color: white;
  font-size: 0.8rem;
  cursor: pointer;
  opacity: 0.8;
  padding: 4px 8px;
  border-radius: 4px;
  transition: opacity 0.3s;
}

.toggle-btn:hover {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.1);
}

.playlist {
  max-height: 200px;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.playlist-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.playlist-item.active {
  background-color: rgba(255, 255, 255, 0.2);
}

.track-number {
  font-size: 0.8rem;
  opacity: 0.6;
  min-width: 20px;
}

.track-details {
  flex: 1;
}

.track-name {
  font-size: 0.9rem;
  margin-bottom: 2px;
}

.track-duration {
  font-size: 0.7rem;
  opacity: 0.7;
}

.track-status {
  font-size: 0.8rem;
  opacity: 0.8;
}

/* 滚动条样式 */
.playlist::-webkit-scrollbar {
  width: 4px;
}

.playlist::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.playlist::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.playlist::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

@media (max-width: 768px) {
  .music-player {
    padding: 15px;
  }
  
  .controls {
    gap: 10px;
  }
  
  .play-btn {
    width: 45px;
    height: 45px;
    font-size: 1.8rem;
  }
  
  .control-btn {
    font-size: 1.3rem;
  }
}
</style> 