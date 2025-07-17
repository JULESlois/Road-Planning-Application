/**
 * 前端直接调用DeepSeek AI API服务
 */

// DeepSeek配置
const DEEPSEEK_CONFIG = {
  baseURL: 'https://api.deepseek.com/v1',
  model: 'deepseek-chat'
}

// API Key (从api.txt文件读取)
let apiKey = ''

/**
 * 初始化AI服务 - 读取API Key
 */
export const initAIService = async () => {
  try {
    const response = await fetch('/api.txt')
    if (response.ok) {
      apiKey = (await response.text()).trim()
    } else {
      console.error('无法读取API Key文件')
    }
  } catch (error) {
    console.error('读取API Key失败:', error)
  }
}

/**
 * 调用DeepSeek API
 * @param {Array} messages - 消息历史
 * @returns {Promise<string>} AI回复
 */
const callDeepSeek = async (messages) => {
  if (!apiKey) {
    throw new Error('API Key未加载，请刷新页面重试')
  }
  
  const response = await fetch(`${DEEPSEEK_CONFIG.baseURL}/chat/completions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${apiKey}`
    },
    body: JSON.stringify({
      model: DEEPSEEK_CONFIG.model,
      messages: messages,
      max_tokens: 1000,
      temperature: 0.7
    })
  })
  
  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error?.message || 'DeepSeek API调用失败')
  }
  
  const data = await response.json()
  return data.choices[0]?.message?.content || '抱歉，我无法生成回复。'
}

/**
 * 角色系统提示词
 */
const SYSTEM_PROMPTS = {
  assistant: '你是一个世界级的实时交通导航系统AI。你的任务是根据交通数据，给出出行建议。你可以分析交通拥堵情况、推荐最优路线、提供实时交通信息，并基于当前系统数据为用户提供个性化的出行方案。',
  translator: '你是一个专业的翻译助手，请准确翻译用户提供的内容。',
}

/**
 * 获取系统数据
 * @returns {Promise<Object>} 系统数据
 */
const getSystemData = async () => {
  try {
    // 获取仪表盘统计数据
    const dashboardResponse = await fetch('/api/dashboard/stats')
    const dashboardData = dashboardResponse.ok ? await dashboardResponse.json() : null
    
    // 获取拥挤度数据
    const congestionResponse = await fetch('/api/map/congestion')
    const congestionData = congestionResponse.ok ? await congestionResponse.json() : null
    
    return {
      dashboard: dashboardData?.data || {},
      congestion: congestionData?.data || {},
      timestamp: new Date().toLocaleString('zh-CN')
    }
  } catch (error) {
    console.error('获取系统数据失败:', error)
    return {
      dashboard: {},
      congestion: {},
      timestamp: new Date().toLocaleString('zh-CN'),
      error: '系统数据获取失败'
    }
  }
}

/**
 * 发送消息到AI
 * @param {string} message - 用户消息
 * @param {Array} history - 对话历史
 * @param {string} role - 角色类型
 * @returns {Promise<string>} AI回复
 */
export const sendMessage = async (message, history = [], role = 'assistant') => {
  try {
    // 获取系统数据
    const systemData = await getSystemData()
    
    // 构建增强的系统提示词
    let systemPrompt = SYSTEM_PROMPTS[role] || SYSTEM_PROMPTS.assistant
    
    if (role === 'assistant') {
      systemPrompt += `\n\n当前系统数据：\n${JSON.stringify(systemData, null, 2)}\n\n请基于以上实时数据为用户提供准确的交通建议。`
    }
    
    // 构建消息历史
    const messages = [
      {
        role: 'system',
        content: systemPrompt
      },
      ...history,
      {
        role: 'user',
        content: message
      }
    ]
    
    // 调用DeepSeek API
    return await callDeepSeek(messages)
  } catch (error) {
    console.error('AI服务调用失败:', error)
    throw error
  }
}

/**
 * 检查API配置状态
 * @returns {boolean} 配置是否完整
 */
export const checkAPIConfig = () => {
  return !!apiKey
}

/**
 * 获取当前提供商
 * @returns {string} 当前AI提供商
 */
export const getCurrentProvider = () => {
  return 'deepseek'
}

/**
 * 获取支持的提供商列表
 * @returns {Array} 支持的AI提供商列表
 */
export const getSupportedProviders = () => {
  return ['deepseek']
}