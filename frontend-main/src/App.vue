<script setup>
import { ref, onMounted } from 'vue'
import ToastNotification from './components/ToastNotification.vue'

const toastRef = ref(null)

onMounted(() => {
  // 将Toast服务挂载到全局
  if (toastRef.value) {
    window.$toast = toastRef.value
  }
})
</script>

<template>
  <div id="app">
    <Transition name="fade" mode="out-in">
      <router-view />
    </Transition>
    <ToastNotification ref="toastRef" />
  </div>
</template>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  margin: 0;
  padding: 0;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family: Avenir, Helvetica, Arial, sans-serif;
}

/* 全局页面淡入淡出动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.4s cubic-bezier(.4,0,.2,1);
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
.fade-enter-to, .fade-leave-from {
  opacity: 1;
}
</style>