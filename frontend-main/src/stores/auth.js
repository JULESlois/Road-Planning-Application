/**
 * 认证状态管理
 * 使用Pinia管理用户登录状态和JWT token
 */
import { defineStore } from 'pinia'
import { authAPI, userAPI, utils } from '../services/api.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // 用户是否已登录
    isAuthenticated: false,
    // 当前用户信息
    user: null,
    // JWT token
    token: null,
    // 是否需要完善个人资料
    needCompleteProfile: false,
    // 加载状态
    loading: false,
    // 错误信息
    error: null
  }),

  getters: {
    /**
     * 获取当前用户名
     */
    currentUsername: (state) => state.user?.username || utils.getCurrentUsername(),
    
    /**
     * 检查是否有有效的token
     */
    hasValidToken: (state) => !!state.token || utils.hasToken()
  },

  actions: {
    /**
     * 初始化认证状态
     * 从localStorage恢复用户登录状态
     */
    initializeAuth() {
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      const needCompleteProfile = localStorage.getItem('needCompleteProfile') === 'true';
      
      if (token) {
        this.token = token;
        this.isAuthenticated = true;
        this.user = { username };
        this.needCompleteProfile = needCompleteProfile;
      }
    },

    /**
     * 用户登录（邮箱）
     * @param {Object} loginData - 登录数据
     */
    async loginByEmail(loginData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await authAPI.loginByEmail(loginData);
        this.setAuthData(response.token, response.username, response.needCompleteProfile);
        return response;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 用户登录（用户名）
     * @param {Object} loginData - 登录数据
     */
    async loginByUsername(loginData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await authAPI.loginByUsername(loginData);
        this.setAuthData(response.token, response.username, response.needCompleteProfile);
        return response;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 用户注册
     * @param {Object} registerData - 注册数据
     */
    async register(registerData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await authAPI.register(registerData);
        return response;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 发送验证码
     * @param {string} email - 邮箱地址
     */
    async sendVerificationCode(email) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await authAPI.sendVerificationCode(email);
        return response;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 用户登出
     */
    async logout() {
      this.loading = true;
      this.error = null;
      
      try {
        await authAPI.logout();
      } catch (error) {
        console.warn('登出API调用失败，但仍清除本地数据:', error);
      } finally {
        this.clearAuthData();
        this.loading = false;
      }
    },

    /**
     * 获取用户信息
     */
    async fetchUserInfo() {
      if (!this.isAuthenticated) return;
      
      this.loading = true;
      this.error = null;
      
      try {
        // 获取用户认证信息
        const authData = await userAPI.getUserAuth();
        
        // 获取用户个人资料
        let profileData = null;
        try {
          profileData = await userAPI.getUserProfile();
        } catch (error) {
          console.warn('获取用户个人资料失败:', error);
        }
        
        // 合并用户信息
        this.user = {
          ...authData.data,
          ...(profileData?.data || {})
        };
        
        return this.user;
      } catch (error) {
        this.error = error.message;
        // 如果token无效，清除登录状态
        if (error.message.includes('token') || error.message.includes('401')) {
          this.clearAuthData();
        }
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 更新用户认证信息
     * @param {Object} updateData - 更新数据
     */
    async updateUserAuth(updateData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await userAPI.updateUserAuth(updateData);
        // 重新获取用户信息
        await this.fetchUserInfo();
        return response;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 更新用户个人资料
     * @param {Object} updateData - 更新数据
     */
    async updateUserProfile(updateData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await userAPI.updateUserProfile(updateData);
        // 重新获取用户信息
        await this.fetchUserInfo();
        return response;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 设置认证数据
     * @param {string} token - JWT token
     * @param {string} username - 用户名
     * @param {boolean} needCompleteProfile - 是否需要完善个人资料
     */
    setAuthData(token, username, needCompleteProfile = false) {
      this.token = token;
      this.isAuthenticated = true;
      this.user = { username };
      this.needCompleteProfile = needCompleteProfile;
      
      // 保存到localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('username', username);
      localStorage.setItem('needCompleteProfile', needCompleteProfile.toString());
    },

    /**
     * 清除认证数据
     */
    clearAuthData() {
      this.isAuthenticated = false;
      this.user = null;
      this.token = null;
      this.needCompleteProfile = false;
      this.error = null;
      
      // 清除localStorage
      utils.clearAuthData();
    },

    /**
     * 清除错误信息
     */
    clearError() {
      this.error = null;
    }
  }
}); 