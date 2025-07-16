import { ref } from 'vue'
import { defineStore } from 'pinia'

// 获取当前小时作为默认值
const defaultTime = new Date().getHours()

export const useQueryStore = defineStore('query', () => {
  // 路径规划所选时间 (0-23)
  const selectedTime = ref(defaultTime)

  /**
   * 设置规划时间
   * @param {number} time - 0到23之间的小时数
   */
  function setTime(time) {
    const newTime = Math.max(0, Math.min(23, time)); // 确保值在0-23范围内
    if (selectedTime.value !== newTime) {
      selectedTime.value = newTime
    }
  }

  return { selectedTime, setTime }
})
