# 机器学习模型整合说明

## 概述

本文档说明了如何将mode1文件夹中的Python机器学习模型整合到backend-main项目中，以实现更智能的路径规划算法。

## 集成组件

### 1. Python模型服务 (`model_server.py`)

位置：`src/main/resources/model_server.py`

**功能**：
- 加载XGBoost模型进行节点流量预测
- 提供RESTful API接口
- 支持单个和批量预测
- 提供健康检查和状态监控

**启动方式**：
```bash
# Windows
start_model_server.bat

# 手动启动
cd src/main/resources
python model_server.py
```

**API端点**：
- `GET /` - 健康检查
- `GET/POST /predict?node=1&time=8` - 单个节点流量预测
- `POST /predict/batch` - 批量节点流量预测
- `GET /status` - 服务状态
- `POST /reload` - 重新加载模型

### 2. Java模型服务客户端 (`PythonModelService`)

位置：`src/main/java/com/example/navigation/service/PythonModelService.java`

**功能**：
- 调用Python模型服务API
- 处理HTTP通信和错误处理
- 提供批量预测功能
- 支持服务可用性检查

**配置参数**：
```properties
model.service.url=http://localhost:5000
model.service.timeout=5000
```

### 3. 增强的路径规划算法 (`RouteService`)

**新特性**：
- 集成机器学习流量预测
- 改进的拥堵系数计算：`weight = distance * (1 + α * averageFlow)`
- 支持ML模型预测回退到数据库查询
- 实时交通状况考虑

**算法参数**：
```properties
route.planner.congestion-alpha=0.05
route.planner.default-speed-kmh=50.0
```

### 4. 模型预测API控制器 (`ModelController`)

位置：`src/main/java/com/example/navigation/controller/ModelController.java`

**端点**：
- `GET /api/model/predict?nodeId=1&timePoint=8` - 单个预测
- `POST /api/model/predict/batch` - 批量预测
- `GET /api/model/status` - 模型服务状态

## 使用方法

### 1. 启动Python模型服务

```bash
# 确保Python环境已安装必要包
pip install flask xgboost pandas

# 启动模型服务器
cd backend-main
start_model_server.bat
```

### 2. 启动Java后端

```bash
cd backend-main
mvn spring-boot:run
```

### 3. 测试API

**路径规划（使用ML预测）**：
```bash
curl -X POST http://localhost:8080/api/routes/plan \
  -H "Content-Type: application/json" \
  -d '{
    "startLat": 39.915,
    "startLng": 116.397,
    "endLat": 39.920,
    "endLng": 116.400,
    "routeType": "fastest"
  }'
```

**单个节点流量预测**：
```bash
curl "http://localhost:8080/api/model/predict?nodeId=1&timePoint=8"
```

**模型服务状态检查**：
```bash
curl "http://localhost:8080/api/model/status"
```

## 算法改进

### 传统算法 vs 机器学习算法

**传统方法**：
- 基于历史固定数据
- 简单的阈值判断
- 无法适应动态变化

**ML增强方法**：
- XGBoost模型实时预测
- 考虑时间和节点特征
- 动态拥堵系数计算
- 更准确的流量估计

### 拥堵系数计算

```
传统: weight = distance
ML增强: weight = distance * (1 + α * predictedFlow)

其中：
- α = 0.05（拥堵权重参数）
- predictedFlow = XGBoost模型预测值
```

## 容错机制

1. **模型服务不可用时**：自动回退到数据库查询
2. **网络超时**：使用默认流量值（50.0）
3. **预测失败**：记录日志并使用备用方案
4. **服务重启**：自动重新连接和重试

## 性能优化

1. **批量预测**：减少HTTP请求次数
2. **连接池**：复用HTTP连接
3. **缓存机制**：可考虑添加预测结果缓存
4. **异步处理**：可扩展为异步预测

## 监控和日志

- 所有组件都有详细的日志记录
- 支持模型服务健康检查
- 预测性能指标监控
- 错误处理和恢复机制

## 文件结构

```
backend-main/
├── src/main/
│   ├── java/com/example/navigation/
│   │   ├── controller/ModelController.java
│   │   └── service/PythonModelService.java
│   │       RouteService.java (增强版)
│   └── resources/
│       ├── models/node_volume_model.json
│       ├── model_server.py
│       └── api_server.py (原版)
├── start_model_server.bat
└── README_ML_INTEGRATION.md
```

## 依赖项

**Maven依赖**（已添加）：
- Apache HttpClient
- Jackson JSON处理

**Python依赖**：
- Flask
- XGBoost
- Pandas

## 故障排除

1. **模型服务启动失败**：检查Python环境和模型文件
2. **连接超时**：检查防火墙和端口5000
3. **预测错误**：查看模型服务日志
4. **内存不足**：调整JVM和Python内存设置

## 未来扩展

- 支持多个模型版本
- 模型热更新机制
- GPU加速预测
- 分布式模型服务
- 实时模型训练和更新 