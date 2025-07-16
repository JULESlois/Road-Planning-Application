import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 主题模式：'light' | 'dark' | 'auto'
  const mode = ref(localStorage.getItem('theme-mode') || 'auto')
  
  // 系统是否为暗色模式
  const systemDark = ref(window.matchMedia('(prefers-color-scheme: dark)').matches)
  
  // 当前实际应用的主题
  const currentTheme = computed(() => {
    if (mode.value === 'auto') {
      return systemDark.value ? 'dark' : 'light'
    }
    return mode.value
  })
  
  // 是否为暗色模式
  const isDark = computed(() => currentTheme.value === 'dark')
  
  /**
   * 设置主题模式
   * @param {string} newMode - 'light' | 'dark' | 'auto'
   */
  const setMode = (newMode) => {
    mode.value = newMode
    localStorage.setItem('theme-mode', newMode)
    applyTheme()
  }
  
  /**
   * 切换主题模式
   */
  const toggleMode = () => {
    const modes = ['light', 'dark', 'auto']
    const currentIndex = modes.indexOf(mode.value)
    const nextIndex = (currentIndex + 1) % modes.length
    setMode(modes[nextIndex])
  }
  
  /**
   * 应用主题到HTML根元素
   */
  const applyTheme = () => {
    const html = document.documentElement
    html.setAttribute('data-theme', currentTheme.value)
    
    if (isDark.value) {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
  }
  
  /**
   * 监听系统主题变化
   */
  const initSystemThemeListener = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    const handleChange = (e) => {
      systemDark.value = e.matches
    }
    
    mediaQuery.addEventListener('change', handleChange)
    
    // 返回清理函数
    return () => {
      mediaQuery.removeEventListener('change', handleChange)
    }
  }
  
  /**
   * 初始化主题
   */
  const initTheme = () => {
    // 应用当前主题
    applyTheme()
    
    // 开始监听系统主题变化
    const cleanup = initSystemThemeListener()
    
    // 监听计算属性变化，自动应用主题
    watch(currentTheme, applyTheme, { immediate: false })
    
    return cleanup
  }
  
  return {
    mode,
    currentTheme,
    isDark,
    setMode,
    toggleMode,
    initTheme
  }
}) 