<template>
  <div class="user-management-container">
    <div class="user-card">
      <h2 class="title">用户中心</h2>
      
      <!-- 加载状态 -->
      <div v-if="authStore.loading" class="loading-text">正在加载用户信息...</div>
      
      <!-- 用户信息展示 -->
      <div v-else-if="userInfo" class="user-content">
        <!-- 认证信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h3>认证信息</h3>
            <button @click="toggleAuthEdit" class="edit-button">
              {{ isEditingAuth ? '取消编辑' : '编辑信息' }}
            </button>
          </div>
          
          <div v-if="!isEditingAuth" class="info-display">
            <div class="info-item">
              <span class="label">用户名:</span>
              <span class="value">{{ userInfo.username || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱:</span>
              <span class="value">{{ userInfo.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色:</span>
              <span class="value">{{ userInfo.role || '普通用户' }}</span>
            </div>
          </div>
          
          <div v-else class="edit-form">
            <div class="input-group">
              <label>邮箱:</label>
              <input 
                type="email" 
                v-model="authEditForm.email" 
                placeholder="请输入邮箱"
                :disabled="authStore.loading"
              >
            </div>
            <div class="input-group">
              <label>新密码:</label>
              <input 
                type="password" 
                v-model="authEditForm.password" 
                placeholder="留空则不修改密码"
                :disabled="authStore.loading"
              >
            </div>
            <div class="form-actions">
              <button @click="saveAuthInfo" class="save-button" :disabled="authStore.loading">
                {{ authStore.loading ? '保存中...' : '保存' }}
              </button>
              <button @click="toggleAuthEdit" class="cancel-button">取消</button>
            </div>
          </div>
        </div>
        
        <!-- 个人资料卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h3>个人资料</h3>
            <button @click="toggleProfileEdit" class="edit-button">
              {{ isEditingProfile ? '取消编辑' : '编辑资料' }}
            </button>
          </div>
          
          <div v-if="!isEditingProfile" class="info-display">
            <div class="info-item">
              <span class="label">姓名:</span>
              <span class="value">{{ userInfo.name || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话:</span>
              <span class="value">{{ userInfo.phoneNumber || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">年龄:</span>
              <span class="value">{{ userInfo.age || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别:</span>
              <span class="value">{{ userInfo.sex || '未设置' }}</span>
            </div>
          </div>
          
          <div v-else class="edit-form">
            <div class="input-group">
              <label>姓名:</label>
              <input 
                type="text" 
                v-model="profileEditForm.name" 
                placeholder="请输入姓名"
                :disabled="authStore.loading"
              >
            </div>
            <div class="input-group">
              <label>电话:</label>
              <input 
                type="tel" 
                v-model="profileEditForm.phoneNumber" 
                placeholder="请输入电话号码"
                :disabled="authStore.loading"
              >
            </div>
            <div class="input-group">
              <label>年龄:</label>
              <input 
                type="number" 
                v-model="profileEditForm.age" 
                placeholder="请输入年龄"
                min="1" 
                max="120"
                :disabled="authStore.loading"
              >
            </div>
            <div class="input-group">
              <label>性别:</label>
              <select v-model="profileEditForm.sex" :disabled="authStore.loading">
                <option value="">请选择</option>
                <option value="男">男</option>
                <option value="女">女</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="form-actions">
              <button @click="saveProfileInfo" class="save-button" :disabled="authStore.loading">
                {{ authStore.loading ? '保存中...' : '保存' }}
              </button>
              <button @click="toggleProfileEdit" class="cancel-button">取消</button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 无用户信息 -->
      <div v-else class="error-text">
        无法加载用户信息，请稍后重试。
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth.js'
import { useThemeStore } from '../stores/theme.js' // Import theme store
import { handleApiError, showSuccess } from '../utils/errorHandler.js'
import dayBg from '../assets/day.png' // Import day background
import nightBg from '../assets/night.png' // Import night background

const authStore = useAuthStore()
const themeStore = useThemeStore() // Initialize theme store

// Computed property for background image
const bgImage = computed(() => themeStore.isDark ? nightBg : dayBg)

// 响应式数据
const isEditingAuth = ref(false)
const isEditingProfile = ref(false)

// 编辑表单数据
const authEditForm = ref({
  email: '',
  password: ''
})

const profileEditForm = ref({
  name: '',
  phoneNumber: '',
  age: '',
  sex: ''
})

// 计算属性 - 用户信息
const userInfo = computed(() => authStore.user)

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    await authStore.fetchUserInfo()
  } catch (err) {
    handleApiError(err, '加载用户信息')
  }
}

/**
 * 切换认证信息编辑状态
 */
const toggleAuthEdit = () => {
  if (isEditingAuth.value) {
    // 取消编辑，重置表单
    authEditForm.value = {
      email: '',
      password: ''
    }
  } else {
    // 开始编辑，初始化表单
    authEditForm.value = {
      email: userInfo.value?.email || '',
      password: ''
    }
  }
  isEditingAuth.value = !isEditingAuth.value
}

/**
 * 切换个人资料编辑状态
 */
const toggleProfileEdit = () => {
  if (isEditingProfile.value) {
    // 取消编辑，重置表单
    profileEditForm.value = {
      name: '',
      phoneNumber: '',
      age: '',
      sex: ''
    }
  } else {
    // 开始编辑，初始化表单
    profileEditForm.value = {
      name: userInfo.value?.name || '',
      phoneNumber: userInfo.value?.phoneNumber || '',
      age: userInfo.value?.age || '',
      sex: userInfo.value?.sex || ''
    }
  }
  isEditingProfile.value = !isEditingProfile.value
}

/**
 * 保存认证信息
 */
const saveAuthInfo = async () => {
  try {
    // 构建更新数据，只包含有值的字段
    const updateData = {}
    
    if (authEditForm.value.email && authEditForm.value.email !== userInfo.value?.email) {
      updateData.email = authEditForm.value.email
    }
    
    if (authEditForm.value.password) {
      updateData.password = authEditForm.value.password
    }
    
    if (Object.keys(updateData).length === 0) {
      showSuccess('提示', '没有需要更新的信息')
      return
    }
    
    await authStore.updateUserAuth(updateData)
    
    // 成功后关闭编辑模式
    isEditingAuth.value = false
    authEditForm.value = {
      email: '',
      password: ''
    }
    
    showSuccess('更新成功', '认证信息已更新')
    
  } catch (err) {
    handleApiError(err, '保存认证信息')
  }
}

/**
 * 保存个人资料
 */
const saveProfileInfo = async () => {
  try {
    // 构建更新数据，只包含有值的字段
    const updateData = {}
    
    if (profileEditForm.value.name) {
      updateData.name = profileEditForm.value.name
    }
    
    if (profileEditForm.value.phoneNumber) {
      updateData.phoneNumber = profileEditForm.value.phoneNumber
    }
    
    if (profileEditForm.value.age) {
      updateData.age = parseInt(profileEditForm.value.age)
    }
    
    if (profileEditForm.value.sex) {
      updateData.sex = profileEditForm.value.sex
    }
    
    if (Object.keys(updateData).length === 0) {
      showSuccess('提示', '没有需要更新的信息')
      return
    }
    
    await authStore.updateUserProfile(updateData)
    
    // 成功后关闭编辑模式
    isEditingProfile.value = false
    profileEditForm.value = {
      name: '',
      phoneNumber: '',
      age: '',
      sex: ''
    }
    
    showSuccess('更新成功', '个人资料已更新')
    
  } catch (err) {
    handleApiError(err, '保存个人资料')
  }
}

/**
 * 组件挂载时加载用户信息
 */
onMounted(() => {
  if (!userInfo.value || !userInfo.value.email) {
    loadUserInfo()
  }
})
</script>

<style scoped>
@import '@/assets/main.css';

.user-management-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  overflow-y: auto;
  background-image: v-bind('`url(${bgImage})`');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
}

.user-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 8px 32px var(--color-shadow);
  max-width: 800px;
  width: 100%;
  margin-top: 20px;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: var(--color-text-primary);
  font-size: 28px;
  font-weight: 700;
}

.loading-text {
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 16px;
  padding: 20px;
}

.error-container {
  text-align: center;
  padding: 40px;
}

.error-text {
  color: var(--color-danger);
  font-size: 1.1rem;
  margin-bottom: 20px;
}

.retry-button {
  background-color: rgba(0, 123, 255, 0.8);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.retry-button:hover {
  background-color: rgba(0, 123, 255, 1);
}

.user-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.info-card {
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border-primary);
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--color-border-primary);
}

.card-header h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 20px;
  font-weight: 600;
}

.edit-button {
  background: var(--color-primary);
  color: white;
  border: 1px solid var(--color-primary);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.edit-button:hover {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.info-display {
  display: grid;
  gap: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid var(--color-border-secondary);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  font-weight: 600;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.info-item .value {
  color: var(--color-text-primary);
  font-size: 14px;
}

.edit-form {
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
  color: var(--color-text-muted);
  font-weight: 500;
  font-size: 0.9rem;
}

.input-group input,
.input-group select {
  padding: 12px;
  border: 1px solid var(--color-border-primary);
  border-radius: 6px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-size: 1rem;
  transition: border-color 0.3s, background 0.3s;
}

.input-group input:focus,
.input-group select:focus {
  outline: none;
  border-color: var(--color-primary);
  background: var(--color-bg-primary);
}

.input-group input::placeholder {
  color: var(--color-text-muted);
}

.input-group input:disabled,
.input-group select:disabled {
  background: var(--color-bg-tertiary);
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.save-button,
.cancel-button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.save-button {
  background: var(--color-success);
  color: white;
  border: 1px solid var(--color-success);
}

.save-button:hover:not(:disabled) {
  background: var(--color-success-hover);
  border-color: var(--color-success-hover);
}

.save-button:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

.cancel-button {
  background: var(--color-bg-tertiary);
  color: white;
  border: 1px solid var(--color-border-primary);
}

.cancel-button:hover {
  background: var(--color-text-muted);
}

.error-text {
  color: var(--color-danger);
  font-size: 1rem;
  margin-bottom: 1rem;
}

@media (max-width: 768px) {
  .user-card {
    padding: 20px;
    margin: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .label {
    width: auto;
  }
  
  .value {
    text-align: left;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>