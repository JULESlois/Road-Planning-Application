# Frontend 实现说明

## 概述

本前端项目基于Vue 3 + Vite构建，实现了完整的智能交通系统用户界面，与backend-main的API完全集成。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **地图**: 高德地图 JavaScript API
- **样式**: 原生CSS (响应式设计)

## 项目结构

```
src/
├── components/          # 页面组件
│   ├── Home.vue        # 仪表盘首页
│   ├── Navigation.vue  # 智能导航
│   ├── MapQuery.vue    # 地图查询
│   └── UserManagement.vue # 用户管理
├── views/              # 视图组件
│   ├── LoginView.vue   # 登录注册页
│   └── MainView.vue    # 主页面布局
├── services/           # API服务
│   └── api.js         # 统一API管理
├── stores/             # 状态管理
│   └── auth.js        # 认证状态管理
└── router/             # 路由配置
    └── index.js       # 路由守卫
```

## 核心功能

### 1. 用户认证系统

**登录页面 (LoginView.vue)**
- 支持用户名/邮箱双重登录方式
- 用户注册功能（含邮箱验证码）
- 实时表单验证和错误提示
- 自动token管理和状态保持

**认证状态管理 (stores/auth.js)**
- JWT token自动管理
- 用户信息缓存和同步
- 登录状态持久化
- 统一错误处理

### 2. 智能仪表盘

**首页组件 (Home.vue)**
- 实时交通拥挤度显示
- 拥堵路段统计
- 系统状态监控
- 自动数据刷新（5分钟间隔）
- 响应式数据可视化

### 3. 用户管理中心

**用户管理 (UserManagement.vue)**
- 认证信息管理（邮箱、密码）
- 个人资料管理（姓名、电话、年龄、性别）
- 实时编辑功能
- 数据验证和错误处理

### 4. 地图数据可视化

**地图查询 (MapQuery.vue)**
- 交通热力图显示
- 拥挤度分析可视化
- 图层控制面板
- 实时数据刷新
- 交互式地图标记

### 5. 智能路线规划

**导航组件 (Navigation.vue)**
- 点击地图设置起终点
- 多种路线偏好（最快/最短/避堵）
- 实时路线规划
- 路线详情展示（距离、时间、节点数）
- 地图路径可视化

## API集成

### 认证API
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/send-code` - 发送验证码
- `POST /api/auth/login` - 邮箱登录
- `POST /api/auth/login_by_username` - 用户名登录
- `POST /api/auth/logout` - 用户登出

### 用户信息API
- `GET /api/user/auth` - 获取认证信息
- `PUT /api/user/auth` - 更新认证信息
- `GET /api/user/profile` - 获取个人资料
- `PUT /api/user/profile` - 更新个人资料

### 数据分析API
- `GET /api/dashboard/stats` - 仪表盘统计
- `GET /api/map/heatmap` - 热力图数据
- `GET /api/map/congestion` - 拥挤度数据

### 路线规划API
- `POST /api/routes/plan` - 智能路线规划

## 特性亮点

### 1. 响应式设计
- 移动端适配
- 弹性布局
- 触控友好的交互

### 2. 用户体验
- 加载状态指示
- 错误处理和重试机制
- 友好的成功/错误提示
- 直观的数据可视化

### 3. 安全性
- JWT token自动管理
- 路由守卫保护
- API请求拦截
- 敏感信息加密存储

### 4. 性能优化
- 组件懒加载
- API请求缓存
- 地图资源管理
- 内存泄漏防护

## 使用指南

### 启动项目
```bash
cd java/frontend
npm install
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 配置说明

**API基础地址** (src/services/api.js)
```javascript
const API_BASE_URL = 'http://localhost:8080'
```

**地图API密钥** (各地图组件)
```javascript
key: "2bec7f50935812fcf29c98e01c419f02"
securityJsCode: 'aceef7681b1b9bcfe0b886af40c120f1'
```

## 部署说明

1. 确保backend-main服务正常运行
2. 配置正确的API基础地址
3. 构建生产版本
4. 部署到Web服务器（Nginx/Apache）
5. 配置CORS和代理（如需要）

## 故障排除

### 常见问题

1. **地图无法加载**
   - 检查高德地图API密钥是否有效
   - 确认网络连接和防火墙设置

2. **API请求失败**
   - 检查后端服务是否启动
   - 验证API基础地址配置
   - 查看浏览器控制台错误信息

3. **登录状态丢失**
   - 检查localStorage中的token
   - 验证JWT token是否过期
   - 确认后端认证配置

4. **地图功能异常**
   - 清除浏览器缓存
   - 检查地图组件错误日志
   - 验证坐标数据格式

## 开发注意事项

1. **状态管理**: 使用Pinia进行全局状态管理，避免prop drilling
2. **API调用**: 统一使用services/api.js中的封装方法
3. **错误处理**: 在组件层面捕获和处理API错误
4. **样式规范**: 使用scoped CSS避免样式污染
5. **性能考虑**: 合理使用计算属性和监听器

## 扩展建议

1. **国际化支持**: 使用vue-i18n添加多语言支持
2. **主题切换**: 实现深色/浅色主题切换
3. **数据可视化**: 集成Chart.js或ECharts进行数据图表展示
4. **离线支持**: 使用Service Worker实现离线功能
5. **实时通信**: 集成WebSocket实现实时数据推送

---

*本文档最后更新时间: 2024年12月* 