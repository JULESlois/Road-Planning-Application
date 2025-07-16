<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { showSuccess, showError, handleApiError } from '../utils/errorHandler.js'

const router = useRouter()
const authStore = useAuthStore()

// 页面状态
const isLogin = ref(true)
const loginBy = ref('username')
const countdown = ref(0)
let timer = null

// 表单数据
const loginForm = ref({
  identifier: '', // 用于用户名或邮箱
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  email: '',
  verificationCode: ''
})

/**
 * 处理登录
 */
const handleLogin = async () => {
  try {
    authStore.clearError()
    
    const loginData = {
      [loginBy.value]: loginForm.value.identifier,
      password: loginForm.value.password
    }

    if (loginBy.value === 'username') {
      await authStore.loginByUsername(loginData)
    } else {
      await authStore.loginByEmail(loginData)
    }

    // 登录成功提示
    showSuccess('登录成功', '欢迎回来！正在跳转到主页...')
    
    // 登录成功，跳转到主页
    router.push('/main')
  } catch (error) {
    // 使用新的错误处理
    handleApiError(error, '登录')
  }
}

/**
 * 处理注册
 */
const handleRegister = async () => {
  try {
    authStore.clearError()
    
    const response = await authStore.register(registerForm.value)
    
    // 注册成功提示
    showSuccess('注册成功', response || '账户创建成功，请登录')
    
    // 2秒后切换到登录页面
    setTimeout(() => {
      switchToLogin()
    }, 2000)
    
  } catch (error) {
    // 使用新的错误处理
    handleApiError(error, '注册')
  }
}

/**
 * 获取验证码
 */
const getVerificationCode = async () => {
  if (countdown.value > 0 || !registerForm.value.email) return

  try {
    authStore.clearError()
    
    const response = await authStore.sendVerificationCode(registerForm.value.email)
    
    // 显示成功消息
    showSuccess('验证码已发送', response || '验证码已发送至您的邮箱，请注意查收')
    
    // 开始60秒倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
  } catch (error) {
    // 使用新的错误处理
    handleApiError(error, '发送验证码')
  }
}

/**
 * 切换到注册页面
 */
const switchToRegister = () => {
  isLogin.value = false
  authStore.clearError()
}

/**
 * 切换到登录页面
 */
const switchToLogin = () => {
  isLogin.value = true
  authStore.clearError()
}

/**
 * 检查用户是否已登录
 */
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/main')
  }
})

/**
 * 清理定时器
 */
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="form-container">
        <!-- 登录表单 -->
        <form v-if="isLogin" @submit.prevent="handleLogin">
          <h2>平台登录</h2>
          
          <div class="input-group">
            <input 
              type="text" 
              v-model="loginForm.identifier" 
              :placeholder="loginBy === 'username' ? '用户名' : '邮箱'" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="password" 
              v-model="loginForm.password" 
              placeholder="密码" 
              required
              :disabled="authStore.loading"
            >
          </div>
          
          <!-- 登录按钮和切换按钮区域 -->
          <div class="login-actions">
            <!-- 切换登录方式按钮 -->
            <button 
              type="button" 
              class="btn-switch" 
              @click="loginBy = loginBy === 'username' ? 'email' : 'username'"
              :disabled="authStore.loading"
            >
              {{ loginBy === 'username' ? '邮箱登录' : '用户名登录' }}
            </button>
            
            <!-- 登录按钮 -->
            <button type="submit" class="btn-submit" :disabled="authStore.loading">
              {{ authStore.loading ? '登录中...' : '登录' }}
            </button>
            
            <!-- 注册账号按钮 -->
            <button 
              type="button" 
              class="btn-register" 
              @click="switchToRegister"
              :disabled="authStore.loading"
            >
              注册账号
            </button>
          </div>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister">
          <h2>用户注册</h2>
          
          <div class="input-group">
            <input 
              type="text" 
              v-model="registerForm.username" 
              placeholder="用户名 (长度3-20)" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="password" 
              v-model="registerForm.password" 
              placeholder="密码 (不少于6位)" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="email" 
              v-model="registerForm.email" 
              placeholder="邮箱" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group verification-code">
            <input 
              type="text" 
              v-model="registerForm.verificationCode" 
              placeholder="验证码" 
              required
              :disabled="authStore.loading"
            >
            <button 
              type="button" 
              @click="getVerificationCode" 
              class="btn-code" 
              :disabled="countdown > 0 || authStore.loading || !registerForm.email"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
          </div>
          <button type="submit" class="btn" :disabled="authStore.loading">
            {{ authStore.loading ? '注册中...' : '注册' }}
          </button>
          <!-- 返回登录按钮 -->
          <div class="register-actions">
            <button 
              type="button" 
              class="btn-back-login" 
              @click="switchToLogin"
              :disabled="authStore.loading"
            >
              返回登录
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: var(--color-bg-primary) !important;
}

.login-card {
  background: var(--color-bg-primary);
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 4px 30px var(--color-shadow);
  width: 100%;
  max-width: 400px;
  border: 1px solid var(--color-border-primary);
  text-align: center;
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: var(--color-text-primary);
}

/* 登录方式提示 */
.login-method-hint {
  text-align: center;
  margin-bottom: 1rem;
}

.method-indicator {
  display: inline-block;
  padding: 0.5rem 1rem;
  background-color: #e3f2fd;
  color: #1976d2;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  border: 1px solid #bbdefb;
}

.login-toggle {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.login-toggle label {
  cursor: pointer;
  color: #333;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.input-group {
  margin-bottom: 1rem;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border-primary);
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.3s;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

input:focus {
  outline: none;
  border-color: var(--color-primary);
}

input:disabled {
  background: var(--color-bg-tertiary);
  cursor: not-allowed;
}

.btn {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background: var(--color-primary);
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.3s;
}

.btn:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.btn:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

/* 登录操作按钮区域 */
.login-actions {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.btn-switch {
  flex: 1;
  padding: 0.75rem 0.5rem;
  border: 1px solid var(--color-primary);
  border-radius: 4px;
  background: transparent;
  color: var(--color-primary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-switch:hover:not(:disabled) {
  background: var(--color-primary);
  color: white;
}

.btn-switch:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

.btn-submit {
  flex: 2;
  margin: 0 0.5rem;
}

.btn-register {
  flex: 1;
  padding: 0.75rem 0.5rem;
  border: 1px solid var(--color-success);
  border-radius: 4px;
  background: transparent;
  color: var(--color-success);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-register:hover:not(:disabled) {
  background: var(--color-success);
  color: white;
}

.btn-register:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

/* 注册页面操作按钮 */
.register-actions {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

.btn-back-login {
  padding: 0.75rem 1.5rem;
  border: 1px solid #6c757d;
  border-radius: 4px;
  background-color: transparent;
  color: #6c757d;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-back-login:hover:not(:disabled) {
  background-color: #6c757d;
  color: white;
}

.btn-back-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.switch-form {
  text-align: center;
  margin-top: 1rem;
  color: #333;
}

.switch-form a {
  color: #007bff;
  text-decoration: none;
}

.switch-form a:hover {
  text-decoration: underline;
}

.verification-code {
  display: flex;
}

.verification-code input {
  flex-grow: 1;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.btn-code {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-primary);
  border-radius: 4px;
  background: transparent;
  color: var(--color-primary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-code:hover:not(:disabled) {
  background: var(--color-primary);
  color: white;
}

.btn-code:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.3s;
  background-color: white;
}

.form-select:focus {
  outline: none;
  border-color: #007bff;
}

.form-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-text {
  color: var(--color-danger);
  font-size: 1rem;
  margin-bottom: 1rem;
}

@media (max-width: 600px) {
  .login-card {
    padding: 1rem;
    max-width: 95vw;
  }
}
</style>