<template>
  <button class="music-player-btn" @click="$emit('open')">
    <span class="icon">🎵</span>
    <span class="text">音乐播放器</span>
  </button>
</template>

<script setup>
// 无需props，点击emit open事件
</script>

<style scoped>
.music-player-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #2196f3;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(33,150,243,0.08);
  transition: background 0.2s;
}
.music-player-btn:hover {
  background: #1976d2;
}
.icon {
  font-size: 1.3rem;
}
.text {
  font-size: 1rem;
}
</style> 