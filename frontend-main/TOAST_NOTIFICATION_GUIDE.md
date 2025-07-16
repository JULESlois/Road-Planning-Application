# Toast通知系统使用指南

## 概述

本系统提供了一套统一的错误信息和成功信息展示机制，具有以下特点：

- **自动消失**: 错误信息显示5秒，成功信息显示2秒
- **详细错误原因**: 根据错误类型提供具体的错误描述和解决建议
- **美观的UI**: 现代化的Toast通知样式，支持动画效果
- **进度条显示**: 实时显示通知剩余显示时间
- **多种类型**: 支持成功、错误、警告、信息四种类型

## 功能特性

### 🎯 自动消失时间
- **成功信息**: 2秒后自动消失
- **错误信息**: 5秒后自动消失
- **警告信息**: 4秒后自动消失
- **信息提示**: 3秒后自动消失

### 🔍 详细错误分类
系统能够识别并分类以下错误类型：

#### 网络错误
- 网络连接失败
- 请求超时
- 网络请求中断

#### 认证错误
- 用户名或密码错误
- 登录已过期
- 访问权限不足

#### 验证错误
- 邮箱格式错误
- 密码格式错误
- 用户名格式错误
- 验证码错误

#### 服务器错误
- 服务器内部错误
- 网关错误
- 服务不可用

#### 业务错误
- 用户已存在
- 路线规划失败
- 地图加载失败
- 文件未找到

## 使用方法

### 1. 基本用法

```javascript
import { showSuccess, showError, showWarning, showInfo } from '../utils/errorHandler.js'

// 显示成功信息
showSuccess('操作成功', '数据已保存')

// 显示错误信息
showError('操作失败', '网络连接异常')

// 显示警告信息
showWarning('注意', '即将超时')

// 显示信息提示
showInfo('提示', '新功能已上线')
```

### 2. 错误处理

```javascript
import { handleApiError } from '../utils/errorHandler.js'

try {
  const response = await api.getData()
  // 处理成功响应
} catch (error) {
  // 自动解析错误类型并显示相应提示
  handleApiError(error, '获取数据')
}
```

### 3. 特定错误处理

```javascript
import { handleMapError, handleFileError, handleMusicError } from '../utils/errorHandler.js'

// 地图错误处理
try {
  // 地图相关操作
} catch (error) {
  handleMapError(error)
}

// 文件错误处理
try {
  // 文件操作
} catch (error) {
  handleFileError(error, '上传文件')
}

// 音乐播放错误处理
try {
  // 音乐播放操作
} catch (error) {
  handleMusicError(error, '当前曲目')
}
```

### 4. 表单验证错误

```javascript
import { handleValidationError } from '../utils/errorHandler.js'

const errors = {
  email: '邮箱格式不正确',
  password: '密码长度不足'
}

handleValidationError(errors, '注册表单')
```

## 错误信息示例

### 网络错误
```
标题: 网络连接失败
消息: 无法连接到服务器，请检查网络连接或稍后重试
建议: 请检查网络连接，确保能够访问互联网
```

### 认证错误
```
标题: 登录已过期
消息: 您的登录已过期，请重新登录
建议: 请重新登录或联系管理员
```

### 验证错误
```
标题: 邮箱格式错误
消息: 请输入正确的邮箱地址
建议: 请检查输入信息格式是否正确
```

### 业务错误
```
标题: 路线规划失败
消息: 无法找到合适的路线，请检查起终点设置
建议: 请稍后重试或联系技术支持
```

## 自定义配置

### 修改显示时长

```javascript
// 在 ToastNotification.vue 中修改默认时长
const defaultDuration = type === 'success' ? 2000 : 5000
```

### 添加新的错误类型

```javascript
// 在 errorHandler.js 中添加新的错误映射
const ERROR_MESSAGES = {
  'custom_error': {
    title: '自定义错误',
    message: '自定义错误描述'
  }
}
```

### 自定义样式

```css
/* 在 ToastNotification.vue 中修改样式 */
.toast.success {
  border-left-color: #your-color;
}

.toast.error {
  border-left-color: #your-color;
}
```

## 技术实现

### 组件结构
- `ToastNotification.vue`: 主要的Toast通知组件
- `errorHandler.js`: 错误处理工具类
- 集成到 `App.vue`: 全局挂载Toast服务

### 核心功能
- **错误解析**: 自动识别错误类型和原因
- **智能建议**: 根据错误类型提供解决建议
- **动画效果**: 平滑的进入和退出动画
- **进度条**: 实时显示剩余显示时间
- **响应式**: 适配移动端和桌面端

### 性能优化
- **定时器管理**: 自动清理定时器，防止内存泄漏
- **批量处理**: 支持多个通知同时显示
- **降级处理**: 当Toast不可用时自动降级到alert

## 最佳实践

### 1. 错误处理
- 始终使用 `handleApiError` 处理API错误
- 为特定功能使用专门的错误处理函数
- 避免直接使用 `alert` 显示错误

### 2. 成功提示
- 使用 `showSuccess` 确认用户操作成功
- 提供具体的成功信息，而不是泛泛而谈
- 避免过度使用成功提示

### 3. 用户体验
- 错误信息要具体且有帮助
- 提供明确的解决建议
- 保持通知信息简洁明了

### 4. 开发调试
- 错误信息会同时记录到控制台
- 包含原始错误信息便于调试
- 支持错误类型和详细信息的查看

## 故障排除

### 常见问题

**Q: Toast通知不显示？**
A: 检查是否正确导入了 `errorHandler.js`，确保 `window.$toast` 已正确挂载。

**Q: 错误信息不够具体？**
A: 在 `errorHandler.js` 中添加新的错误映射，或使用 `handleApiError` 的第二个参数提供更具体的操作名称。

**Q: 显示时长不合适？**
A: 可以在调用时指定自定义时长，或修改 `ToastNotification.vue` 中的默认时长设置。

**Q: 样式不符合设计要求？**
A: 修改 `ToastNotification.vue` 中的CSS样式，支持完全自定义外观。

---

通过这套Toast通知系统，用户可以获得清晰、及时、有用的反馈信息，大大提升了应用的用户体验。 