/**
 * 错误处理工具类
 * 用于解析不同类型的错误并提供详细的错误信息
 */

/**
 * 错误类型枚举
 */
export const ErrorType = {
  NETWORK: 'network',
  AUTH: 'auth',
  VALIDATION: 'validation',
  SERVER: 'server',
  UNKNOWN: 'unknown'
}

/**
 * 错误原因映射
 */
const ERROR_MESSAGES = {
  // 网络错误
  'Network Error': {
    title: '网络连接失败',
    message: '无法连接到服务器，请检查网络连接或稍后重试'
  },
  'Failed to fetch': {
    title: '网络请求失败',
    message: '网络请求被中断，请检查网络连接'
  },
  'timeout': {
    title: '请求超时',
    message: '服务器响应超时，请稍后重试'
  },
  
  // 认证错误
  '401': {
    title: '认证失败',
    message: '用户名或密码错误，请重新登录'
  },
  '403': {
    title: '访问被拒绝',
    message: '您没有权限访问此资源'
  },
  'token': {
    title: '登录已过期',
    message: '您的登录已过期，请重新登录'
  },
  'unauthorized': {
    title: '未授权访问',
    message: '请先登录后再进行操作'
  },
  
  // 验证错误
  'validation': {
    title: '输入验证失败',
    message: '请检查输入信息是否正确'
  },
  'email': {
    title: '邮箱格式错误',
    message: '请输入正确的邮箱地址'
  },
  'password': {
    title: '密码格式错误',
    message: '密码长度至少6位，包含字母和数字'
  },
  'username': {
    title: '用户名格式错误',
    message: '用户名长度3-20位，只能包含字母、数字和下划线'
  },
  'verification': {
    title: '验证码错误',
    message: '验证码不正确或已过期，请重新获取'
  },
  
  // 服务器错误
  '500': {
    title: '服务器内部错误',
    message: '服务器出现异常，请稍后重试'
  },
  '502': {
    title: '网关错误',
    message: '服务器网关异常，请稍后重试'
  },
  '503': {
    title: '服务不可用',
    message: '服务器暂时不可用，请稍后重试'
  },
  '504': {
    title: '网关超时',
    message: '服务器响应超时，请稍后重试'
  },
  
  // 业务错误
  'user_exists': {
    title: '用户已存在',
    message: '该用户名或邮箱已被注册'
  },
  'user_not_found': {
    title: '用户不存在',
    message: '找不到指定的用户信息'
  },
  'email_send_failed': {
    title: '邮件发送失败',
    message: '验证码发送失败，请稍后重试'
  },
  'route_not_found': {
    title: '路线规划失败',
    message: '无法找到合适的路线，请检查起终点设置'
  },
  'map_load_failed': {
    title: '地图加载失败',
    message: '地图服务暂时不可用，请刷新页面重试'
  },
  'data_load_failed': {
    title: '数据加载失败',
    message: '无法获取数据，请检查网络连接'
  },
  'file_not_found': {
    title: '文件未找到',
    message: '请求的文件不存在或已被删除'
  }
}

/**
 * 解析错误信息
 * @param {Error|string} error - 错误对象或错误消息
 * @returns {Object} 解析后的错误信息
 */
export function parseError(error) {
  let errorMessage = ''
  let errorType = ErrorType.UNKNOWN
  
  // 处理不同类型的错误
  if (typeof error === 'string') {
    errorMessage = error
  } else if (error instanceof Error) {
    errorMessage = error.message
  } else if (error && typeof error === 'object') {
    // 优先处理后端响应对象
    if (error.response && error.response.data) {
      const data = error.response.data
      if (typeof data === 'string') {
        errorMessage = data
      } else if (typeof data.message === 'string') {
        errorMessage = data.message
      } else if (typeof data.error === 'string') {
        errorMessage = data.error
      } else if (typeof data.error === 'object' && data.error !== null) {
        // 递归提取对象中的 message
        errorMessage = data.error.message || JSON.stringify(data.error)
      } else {
        // 处理ErrorResponse格式: {message, details, statusCode, errorCode, timestamp}
        errorMessage = data.message || data.details || JSON.stringify(data)
      }
    } else {
      errorMessage = error.message || error.error || JSON.stringify(error)
    }
  } else {
    errorMessage = '未知错误'
  }
  
  // 转换为小写进行匹配
  const lowerMessage = (errorMessage + '').toLowerCase()
  
  // 根据错误消息判断错误类型
  if (lowerMessage.includes('network') || lowerMessage.includes('fetch') || lowerMessage.includes('timeout')) {
    errorType = ErrorType.NETWORK
  } else if (lowerMessage.includes('401') || lowerMessage.includes('403') || lowerMessage.includes('token') || lowerMessage.includes('unauthorized')) {
    errorType = ErrorType.AUTH
  } else if (lowerMessage.includes('validation') || lowerMessage.includes('email') || lowerMessage.includes('password') || lowerMessage.includes('username')) {
    errorType = ErrorType.VALIDATION
  } else if (lowerMessage.includes('500') || lowerMessage.includes('502') || lowerMessage.includes('503') || lowerMessage.includes('504')) {
    errorType = ErrorType.SERVER
  }
  
  // 查找具体的错误信息
  let title = '操作失败'
  let message = errorMessage
  
  // 按优先级匹配错误信息
  for (const [key, value] of Object.entries(ERROR_MESSAGES)) {
    if (lowerMessage.includes(key.toLowerCase())) {
      title = value.title
      message = value.message
      break
    }
  }
  
  return {
    type: errorType,
    title,
    message,
    originalError: errorMessage
  }
}

/**
 * 显示错误通知
 * @param {Error|string} error - 错误对象或错误消息
 */
export function showError(error) {
  const parsedError = parseError(error)
  
  if (window.$toast) {
    window.$toast.showError(parsedError.title, parsedError.message)
  } else {
    // 降级处理：使用alert
    alert(`${parsedError.title}: ${parsedError.message}`)
  }
  
  // 记录错误日志
  console.error('错误详情:', {
    type: parsedError.type,
    title: parsedError.title,
    message: parsedError.message,
    originalError: parsedError.originalError
  })
}

/**
 * 显示成功通知
 * @param {string} title - 成功标题
 * @param {string} message - 成功消息
 */
export function showSuccess(title, message) {
  if (window.$toast) {
    window.$toast.showSuccess(title, message)
  } else {
    // 降级处理：使用alert
    alert(`${title}: ${message}`)
  }
}

/**
 * 显示警告通知
 * @param {string} title - 警告标题
 * @param {string} message - 警告消息
 */
export function showWarning(title, message) {
  if (window.$toast) {
    window.$toast.showWarning(title, message)
  } else {
    // 降级处理：使用alert
    alert(`${title}: ${message}`)
  }
}

/**
 * 显示信息通知
 * @param {string} title - 信息标题
 * @param {string} message - 信息消息
 */
export function showInfo(title, message) {
  if (window.$toast) {
    window.$toast.showInfo(title, message)
  } else {
    // 降级处理：使用alert
    alert(`${title}: ${message}`)
  }
}

/**
 * 处理API错误
 * @param {Error} error - API错误对象
 * @param {string} operation - 操作名称
 */
export function handleApiError(error, operation = '操作') {
  const parsedError = parseError(error)
  
  // 根据错误类型提供更具体的建议
  let suggestion = ''
  switch (parsedError.type) {
    case ErrorType.NETWORK:
      suggestion = '请检查网络连接，确保能够访问互联网'
      break
    case ErrorType.AUTH:
      suggestion = '请重新登录或联系管理员'
      break
    case ErrorType.VALIDATION:
      suggestion = '请检查输入信息格式是否正确'
      break
    case ErrorType.SERVER:
      suggestion = '服务器暂时不可用，请稍后重试'
      break
    default:
      suggestion = '请稍后重试或联系技术支持'
  }
  
  const message = `${parsedError.message}${suggestion ? `。${suggestion}` : ''}`
  
  if (window.$toast) {
    window.$toast.showError(`${operation}失败`, message)
  } else {
    alert(`${operation}失败: ${message}`)
  }
  
  console.error(`${operation}失败:`, {
    error: parsedError.originalError,
    type: parsedError.type,
    suggestion
  })
}

/**
 * 处理表单验证错误
 * @param {Object} errors - 验证错误对象
 * @param {string} formName - 表单名称
 */
export function handleValidationError(errors, formName = '表单') {
  if (!errors || typeof errors !== 'object') {
    showError('表单验证失败')
    return
  }
  
  const errorMessages = []
  
  for (const [field, message] of Object.entries(errors)) {
    if (message) {
      errorMessages.push(message)
    }
  }
  
  if (errorMessages.length > 0) {
    const message = errorMessages.join('；')
    showError(`${formName}验证失败`, message)
  }
}

/**
 * 处理文件上传错误
 * @param {Error} error - 文件上传错误
 * @param {string} fileName - 文件名
 */
export function handleFileError(error, fileName = '文件') {
  const parsedError = parseError(error)
  
  let message = parsedError.message
  if (fileName) {
    message = `${fileName}${message}`
  }
  
  showError('文件操作失败', message)
}

/**
 * 处理地图相关错误
 * @param {Error} error - 地图错误
 */
export function handleMapError(error) {
  const parsedError = parseError(error)
  
  let message = parsedError.message
  if (parsedError.type === ErrorType.NETWORK) {
    message += '，地图服务需要网络连接'
  }
  
  showError('地图加载失败', message)
}

/**
 * 处理音乐播放错误
 * @param {Error} error - 音乐播放错误
 * @param {string} trackName - 曲目名称
 */
export function handleMusicError(error, trackName = '音乐') {
  const parsedError = parseError(error)
  
  let message = parsedError.message
  if (trackName) {
    message = `${trackName}${message}`
  }
  
  showError('音乐播放失败', message)
} 