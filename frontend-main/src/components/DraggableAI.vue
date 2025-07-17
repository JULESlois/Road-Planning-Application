<template>
  <!-- 悬浮球模式 -->
  <div 
    v-if="isMinimized" 
    class="ai-floating-ball"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
    @mousedown="startDrag"
  >
    <div class="floating-ball-icon">🤖</div>
    <div class="floating-ball-badge" v-if="unreadCount > 0">{{ unreadCount }}</div>
  </div>
  
  <!-- 窗口模式 -->
  <div 
    v-else
    class="ai-assistant-window"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
  >
    <!-- 窗口标题栏（可拖动区域） -->
    <div class="ai-window-header" @mousedown="startDrag">
      <div class="ai-header-content">
        <h4>🤖 AI智能助手</h4>
        <div class="ai-role-selector">
          <select v-model="selectedRole" @change="switchRole(selectedRole)" class="role-select">
            <option value="traffic">交通专家</option>
            <option value="tourist">旅游向导</option>
            <option value="business">商务顾问</option>
            <option value="student">学生助手</option>
            <option value="elderly">老年关怀</option>
          </select>
        </div>
      </div>
      <div class="ai-window-controls">

        <button @click="toggleMinimize" class="minimize-btn" title="最小化">−</button>
      </div>
    </div>
    

    
    <!-- 对话区域 -->
    <div class="chat-container">
      <div class="chat-messages" ref="chatContainer">
        <div 
          v-for="(message, index) in chatMessages" 
          :key="index" 
          class="message"
          :class="message.type"
        >
          <div class="message-avatar">
            {{ message.type === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content">
            <div class="message-text">
              {{ message.text }}
              <!-- 显示打字机光标 -->
              <span v-if="message.isTyping" class="typing-cursor">|</span>
            </div>
            <div class="message-time">{{ formatMessageTime(message.time) }}</div>
          </div>
        </div>
        
        <!-- AI加载状态（仅在初始加载时显示） -->
        <div v-if="aiLoading && !chatMessages.some(msg => msg.isTyping)" class="message ai-typing">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="chat-input">
        <input 
          v-model="userInput" 
          @keyup.enter="sendMessageToAI"
          placeholder="输入您的问题..."
          class="message-input"
          :disabled="aiLoading"
        >
        <button 
          @click="sendMessageToAI" 
          class="send-btn"
          :disabled="!userInput.trim() || aiLoading"
        >
          {{ aiLoading ? '发送中...' : '发送' }}
        </button>
      </div>
    </div>
    
    <!-- 快捷问题 -->
    <div class="quick-questions">
      <h5>💡 快捷问题</h5>
      <div class="question-buttons">
        <button 
          v-for="question in getQuickQuestions()" 
          :key="question"
          @click="askQuickQuestion(question)"
          class="quick-btn"
          :disabled="aiLoading"
        >
          {{ question }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { sendMessage, initAIService, checkAPIConfig } from '@/services/aiService'
import { showSuccess } from '@/utils/errorHandler'

// 窗口状态
const isMinimized = ref(true) // Default to minimized (floating ball)
const position = ref({ x: 20, y: 130 }) // Positioned below the layer control panel
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })

// AI助手状态
const selectedRole = ref('traffic')
const chatMessages = ref([])
const userInput = ref('')
const aiLoading = ref(false)
const chatContainer = ref(null)
const unreadCount = ref(0)



// 拖动相关变量
let startX = 0
let startY = 0

/**
 * 角色对应的快捷问题
 */
const roleQuestions = {
  traffic: [
    '当前路况如何？',
    '推荐最佳路线',
    '避开拥堵路段',
    '实时交通信息'
  ],
  tourist: [
    '附近景点推荐',
    '最佳旅游路线',
    '美食推荐',
    '交通指南'
  ],
  business: [
    '商务区路线',
    '会议室导航',
    '停车场信息',
    '商务中心'
  ],
  student: [
    '校园导航',
    '图书馆路线',
    '食堂位置',
    '宿舍区域'
  ],
  elderly: [
    '无障碍路线',
    '医院导航',
    '公园位置',
    '便民服务'
  ]
}

/**
 * 获取当前角色的快捷问题
 */
const getQuickQuestions = () => {
  return roleQuestions[selectedRole.value] || roleQuestions.traffic
}

/**
 * 切换角色
 */
const switchRole = (role) => {
  addSystemMessage(`已切换到${getRoleName(role)}模式`)
}

/**
 * 获取角色名称
 */
const getRoleName = (role) => {
  const names = {
    traffic: '交通专家',
    tourist: '旅游向导',
    business: '商务顾问',
    student: '学生助手',
    elderly: '老年关怀'
  }
  return names[role] || '交通专家'
}

/**
 * 切换最小化状态
 */
const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
  if (!isMinimized.value) {
    unreadCount.value = 0
    // 窗口还原时滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  }
}

/**
 * 开始拖动
 */
const startDrag = (e) => {
  e.preventDefault()
  isDragging.value = true
  
  const rect = e.currentTarget.getBoundingClientRect()
  dragOffset.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
  
  startX = e.clientX
  startY = e.clientY
  
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
  
  // 防止文本选择
  document.body.style.userSelect = 'none'
}

/**
 * 处理拖动
 */
const handleDrag = (e) => {
  if (!isDragging.value) return
  
  const newX = e.clientX - dragOffset.value.x
  const newY = e.clientY - dragOffset.value.y
  
  // 限制在窗口范围内
  const maxX = window.innerWidth - (isMinimized.value ? 60 : 400)
  const maxY = window.innerHeight - (isMinimized.value ? 60 : 500)
  
  position.value.x = Math.max(0, Math.min(newX, maxX))
  position.value.y = Math.max(0, Math.min(newY, maxY))
}

/**
 * 停止拖动
 */
const stopDrag = (e) => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.body.style.userSelect = ''
  
  // If the mouse moved only a tiny distance, treat it as a click.
  const distance = Math.sqrt(
    Math.pow(e.clientX - startX, 2) + Math.pow(e.clientY - startY, 2)
  )
  
  if (distance < 5) {
    toggleMinimize()
  }
}

/**
 * 添加系统消息
 */
const addSystemMessage = (text) => {
  chatMessages.value.push({
    type: 'assistant',
    text: text,
    time: new Date()
  })
  scrollToBottom()
}

/**
 * 逐字显示文本的打字机效果
 * @param {string} text - 要显示的文本
 * @param {Object} messageObj - 消息对象引用
 * @param {number} speed - 打字速度（毫秒）
 */
const typewriterEffect = async (text, messageObj, speed = 80) => {
  // 清空初始文本
  messageObj.text = ''
  
  // 逐字添加文本
  for (let i = 0; i <= text.length; i++) {
    messageObj.text = text.substring(0, i)
    
    // 强制触发响应式更新
    await nextTick()
    scrollToBottom()
    
    // 如果不是最后一个字符，等待一段时间
    if (i < text.length) {
      await new Promise(resolve => setTimeout(resolve, speed))
    }
  }
  
  // 完成后移除打字状态
  messageObj.isTyping = false
}

/**
 * 发送消息到AI
 */
const sendMessageToAI = async () => {
  if (!userInput.value.trim() || aiLoading.value) return
  
  // 检查API配置
  if (!checkAPIConfig()) {
    showSuccess('API服务未就绪', 'DeepSeek API服务正在初始化，请稍后重试', 'warning')
    return
  }
  
  const message = userInput.value.trim()
  userInput.value = ''
  
  // 添加用户消息
  chatMessages.value.push({
    type: 'user',
    text: message,
    time: new Date()
  })
  
  scrollToBottom()
  aiLoading.value = true
  
  // 创建AI消息占位符
  const aiMessage = reactive({
    type: 'assistant',
    text: '',
    time: new Date(),
    isTyping: true
  })
  chatMessages.value.push(aiMessage)
  scrollToBottom()
  
  try {
    // 构建对话历史
    const history = chatMessages.value
      .slice(-6) // 只传递最近5条消息作为上下文（排除当前正在输入的消息）
      .filter(msg => msg.type !== 'system' && !msg.isTyping)
      .map(msg => ({
        role: msg.type === 'user' ? 'user' : 'assistant',
        content: msg.text
      }))
    
    const reply = await sendMessage(message, history, selectedRole.value)
    
    // 停止加载状态
    aiLoading.value = false
    
    // 使用打字机效果显示AI回复
    await typewriterEffect(reply || '抱歉，我现在无法回答这个问题。', aiMessage)
    
    // 如果窗口是最小化状态，增加未读计数
    if (isMinimized.value) {
      unreadCount.value++
    }
    
  } catch (err) {
    console.error('AI对话失败:', err)
    aiLoading.value = false
    
    showSuccess('AI对话失败', err.message || '服务暂时不可用，请稍后重试', 'error')
    
    // 使用打字机效果显示错误消息
    await typewriterEffect(`抱歉，${err.message || '服务暂时不可用，请稍后重试。'}`, aiMessage)
  } finally {
    scrollToBottom()
  }
}

/**
 * 快捷问题
 */
const askQuickQuestion = (question) => {
  userInput.value = question
  sendMessageToAI()
}



/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

/**
 * 格式化消息时间
 */
const formatMessageTime = (time) => {
  return time.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 窗口大小变化时调整位置
 */
const handleResize = () => {
  const maxX = window.innerWidth - (isMinimized.value ? 60 : 400)
  const maxY = window.innerHeight - (isMinimized.value ? 60 : 500)
  
  position.value.x = Math.min(position.value.x, maxX)
  position.value.y = Math.min(position.value.y, maxY)
}

onMounted(async () => {
  // 初始化AI服务
  await initAIService()
  // 添加欢迎消息
  addSystemMessage(`欢迎使用AI助手！我是您的${getRoleName(selectedRole.value)}，有什么可以帮助您的吗？`)
})

onUnmounted(() => {
  // No more resize listener to remove
})
</script>

<style scoped>
/* 悬浮球样式 */
.ai-floating-ball {
  position: fixed;
  width: 60px;
  height: 60px;
  background: var(--color-primary);
  border-radius: 50%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
  user-select: none;
}

.ai-floating-ball:hover {
  transform: scale(1.1);
}

.floating-ball-icon {
  font-size: 24px;
  color: white;
}

.floating-ball-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: var(--color-danger);
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

/* 窗口样式 */
.ai-assistant-window {
  position: fixed;
  width: 400px;
  height: 500px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border-primary);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  z-index: 999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-window-header {
  background: var(--color-bg-secondary);
  border-bottom: 1px solid var(--color-border-primary);
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: move;
  user-select: none;
}

.ai-header-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.ai-header-content h4 {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-primary);
}

.role-select {
  padding: 4px 8px;
  border: 1px solid var(--color-border-primary);
  border-radius: 4px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-size: 12px;
  cursor: pointer;
}

.ai-window-controls {
  display: flex;
  gap: 4px;
}

.settings-btn,
.minimize-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  line-height: 1;
}

.minimize-btn:hover {
  background: var(--color-border-hover);
}

/* 对话区域 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message-content {
  max-width: 80%;
  min-width: 0;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  background: var(--color-bg-secondary);
  padding: 8px 12px;
  border-radius: 12px;
  color: var(--color-text-primary);
  font-size: 14px;
  line-height: 1.4;
  word-wrap: break-word;
  display: inline-block;
  max-width: 100%;
  min-width: 40px;
  width: fit-content;
  white-space: pre-wrap;
}

.message.user .message-text {
  background: var(--color-primary);
  color: white;
}

.message-time {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

/* AI输入中状态 */
.ai-typing .message-text {
  background: var(--color-bg-secondary);
  padding: 12px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-text-muted);
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  30% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 打字机光标样式 */
.typing-cursor {
  color: var(--color-primary);
  font-weight: bold;
  animation: blink 1s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

/* 输入区域 */
.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--color-border-primary);
  display: flex;
  gap: 8px;
  background: var(--color-bg-secondary);
}

.message-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid var(--color-border-primary);
  border-radius: 20px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-size: 14px;
  outline: none;
}

.message-input:focus {
  border-color: var(--color-primary);
}

.send-btn {
  padding: 8px 16px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.send-btn:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.send-btn:disabled {
  background: var(--color-border-primary);
  cursor: not-allowed;
}

/* 快捷问题 */
.quick-questions {
  padding: 12px 16px;
  border-top: 1px solid var(--color-border-primary);
  background: var(--color-bg-secondary);
}

.quick-questions h5 {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.question-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.quick-btn {
  padding: 4px 8px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border-primary);
  border-radius: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.3s;
}

.quick-btn:hover:not(:disabled) {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}

.quick-btn:disabled {
  color: var(--color-text-muted);
  cursor: not-allowed;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .ai-assistant-window {
    width: 350px;
    height: 450px;
  }
}
</style>