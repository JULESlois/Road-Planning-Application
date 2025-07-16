/**
 * 统一的API服务模块
 * 包含所有后端API调用功能
 */

const API_BASE_URL = 'http://localhost:8080'; // 后端服务地址

/**
 * 通用请求方法
 * @param {string} url - 请求URL
 * @param {Object} options - 请求配置
 * @returns {Promise} 响应数据
 */
async function apiRequest(url, options = {}) {
  const token = localStorage.getItem('token');
  
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    },
    ...options
  };

  try {
    const response = await fetch(`${API_BASE_URL}${url}`, defaultOptions);
    
    // 处理非JSON响应（如文本响应）
    const contentType = response.headers.get('content-type');
    let data;
    
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      throw new Error(data || `HTTP error! status: ${response.status}`);
    }

    return { data, status: response.status };
  } catch (error) {
    console.error('API请求失败:', error);
    throw error;
  }
}

// 认证相关API
export const authAPI = {
  /**
   * 用户注册
   * @param {Object} registerData - 注册数据
   * @returns {Promise<string>} 注册结果消息
   */
  async register(registerData) {
    const { data } = await apiRequest('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(registerData)
    });
    return data;
  },

  /**
   * 发送验证码
   * @param {string} email - 邮箱地址
   * @returns {Promise<string>} 发送结果消息
   */
  async sendVerificationCode(email) {
    const { data } = await apiRequest('/api/auth/send-code', {
      method: 'POST',
      body: JSON.stringify({ email })
    });
    return data;
  },

  /**
   * 邮箱登录
   * @param {Object} loginData - 登录数据
   * @returns {Promise<Object>} 登录响应数据
   */
  async loginByEmail(loginData) {
    const { data } = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(loginData)
    });
    return data;
  },

  /**
   * 用户名登录
   * @param {Object} loginData - 登录数据
   * @returns {Promise<Object>} 登录响应数据
   */
  async loginByUsername(loginData) {
    const { data } = await apiRequest('/api/auth/login_by_username', {
      method: 'POST',
      body: JSON.stringify(loginData)
    });
    return data;
  },

  /**
   * 用户登出
   * @returns {Promise<string>} 登出结果消息
   */
  async logout() {
    const { data } = await apiRequest('/api/auth/logout', {
      method: 'POST'
    });
    return data;
  }
};

// 用户信息相关API
export const userAPI = {
  /**
   * 获取当前用户认证信息
   * @returns {Promise<Object>} 用户认证信息
   */
  async getUserAuth() {
    const { data } = await apiRequest('/api/user/auth');
    return data;
  },

  /**
   * 修改当前用户认证信息
   * @param {Object} updateData - 更新数据
   * @returns {Promise<Object>} 更新结果
   */
  async updateUserAuth(updateData) {
    const { data } = await apiRequest('/api/user/auth', {
      method: 'PUT',
      body: JSON.stringify(updateData)
    });
    return data;
  },

  /**
   * 获取当前用户个人资料
   * @returns {Promise<Object>} 用户个人资料
   */
  async getUserProfile() {
    const { data } = await apiRequest('/api/user/profile');
    return data;
  },

  /**
   * 修改当前用户个人资料
   * @param {Object} updateData - 更新数据
   * @returns {Promise<Object>} 更新结果
   */
  async updateUserProfile(updateData) {
    const { data } = await apiRequest('/api/user/profile', {
      method: 'PUT',
      body: JSON.stringify(updateData)
    });
    return data;
  }
};

// 数据分析相关API
export const dataAPI = {
  /**
   * 获取仪表盘统计数据
   * @returns {Promise<Object>} 统计数据
   */
  async getDashboardStats() {
    const { data } = await apiRequest('/api/dashboard/stats');
    return data;
  },

  /**
   * 获取热力图数据
   * @returns {Promise<Object>} 热力图数据
   */
  async getHeatmapData() {
    const { data } = await apiRequest('/api/map/heatmap');
    return data;
  },

  /**
   * 获取拥挤度数据
   * @returns {Promise<Object>} 拥挤度数据
   */
  async getCongestionData() {
    const { data } = await apiRequest('/api/map/congestion');
    return data;
  }
};

// 路线规划API
export const routeAPI = {
  /**
   * 规划路线
   * @param {Object} routeData - 路线规划参数
   * @returns {Promise<Object>} 路线规划结果
   */
  async planRoute(routeData) {
    const { data } = await apiRequest('/api/routes/plan', {
      method: 'POST',
      body: JSON.stringify(routeData)
    });
    return data;
  }
};

// 工具函数
export const utils = {
  /**
   * 检查token是否存在
   * @returns {boolean} token是否存在
   */
  hasToken() {
    return !!localStorage.getItem('token');
  },

  /**
   * 清除本地存储的认证信息
   */
  clearAuthData() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('needCompleteProfile');
  },

  /**
   * 获取当前用户名
   * @returns {string|null} 用户名
   */
  getCurrentUsername() {
    return localStorage.getItem('username');
  }
};

// AI助手相关API
export const aiAPI = {
  /**
   * 发送消息给AI助手
   * @param {Object} messageData - 消息数据
   * @returns {Promise<Object>} AI响应
   */
  async sendMessage(messageData) {
    const { data } = await apiRequest('/api/ai/chat', {
      method: 'POST',
      body: JSON.stringify(messageData)
    });
    return data;
  },

  /**
   * 获取AI助手状态
   * @returns {Promise<Object>} AI状态信息
   */
  async getStatus() {
    const { data } = await apiRequest('/api/ai/status');
    return data;
  }
}; 