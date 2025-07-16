<template>
  <div v-if="visible" class="dialog-overlay" @click="handleOverlayClick">
    <div class="dialog-content" @click.stop>
      <div class="dialog-header">
        <h3>完善个人资料</h3>
        <button class="close-btn" @click="closeDialog">×</button>
      </div>
      
      <div class="dialog-body">
        <div class="icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="#4CAF50">
            <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 5.5V8.5C15 9.03 14.85 9.5 14.6 9.9L19 11.5V12C19 15.87 15.87 19 12 19S5 15.87 5 12V11.5L9.4 9.9C9.15 9.5 9 9.03 9 8.5V5.5L3 7V9C3 17.67 9.33 24 18 24S33 17.67 33 9Z"/>
          </svg>
        </div>
        
        <p class="message">
          欢迎来到智能交通系统！为了为您提供更好的个性化服务，建议您完善个人资料信息。
        </p>
        
        <div class="benefits">
          <h4>完善资料的好处：</h4>
          <ul>
            <li>获得个性化的路线推荐</li>
            <li>接收重要的交通提醒</li>
            <li>享受更好的用户体验</li>
          </ul>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="btn-cancel" @click="laterAction">稍后再说</button>
        <button class="btn-confirm" @click="goToProfile">立即完善</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'close', 'later', 'confirm'])

const router = useRouter()
const visible = ref(props.modelValue)

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

// 监听visible变化，同步到父组件
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

/**
 * 关闭对话框
 */
const closeDialog = () => {
  visible.value = false
  emit('close')
}

/**
 * 处理遮罩层点击
 */
const handleOverlayClick = () => {
  closeDialog()
}

/**
 * 稍后完善
 */
const laterAction = () => {
  visible.value = false
  emit('later')
}

/**
 * 前往个人资料页面
 */
const goToProfile = () => {
  visible.value = false
  emit('confirm')
  router.push('/main/user-management')
}
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 480px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  animation: dialogSlideIn 0.3s ease-out;
}

@keyframes dialogSlideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 0 24px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.dialog-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: #f5f5f5;
  color: #666;
}

.dialog-body {
  padding: 0 24px 20px;
  text-align: center;
}

.icon {
  margin-bottom: 16px;
}

.message {
  font-size: 1rem;
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.benefits {
  text-align: left;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.benefits h4 {
  margin: 0 0 12px 0;
  font-size: 0.9rem;
  color: #333;
}

.benefits ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  font-size: 0.9rem;
}

.benefits li {
  margin-bottom: 6px;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  padding: 0 24px 24px;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background-color: #e9ecef;
}

.btn-confirm {
  background-color: #007bff;
  color: white;
}

.btn-confirm:hover {
  background-color: #0056b3;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dialog-content {
    width: 95%;
    margin: 20px;
  }
  
  .dialog-header,
  .dialog-body,
  .dialog-footer {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style> 