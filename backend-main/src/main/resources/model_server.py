#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
高级交通流量预测模型服务器
提供基于XGBoost的节点流量预测API
"""

from flask import Flask, request, jsonify, make_response
import xgboost as xgb
import pandas as pd
import os
import logging
import traceback
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('model_server.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# 全局模型变量
model = None
MODEL_PATH = "models/node_volume_model.json"

def load_model():
    """加载XGBoost模型"""
    global model
    try:
        if os.path.exists(MODEL_PATH):
            model = xgb.XGBRegressor()
            model.load_model(MODEL_PATH)
            logger.info(f"✅ 模型加载成功: {MODEL_PATH}")
            return True
        else:
            logger.error(f"❌ 模型文件不存在: {MODEL_PATH}")
            return False
    except Exception as e:
        logger.error(f"❌ 模型加载失败: {str(e)}")
        logger.error(traceback.format_exc())
        return False

def validate_parameters(node, time):
    """验证输入参数"""
    try:
        node = int(node)
        time = int(time)
        
        if node <= 0:
            return None, None, "节点ID必须大于0"
        
        if not (0 <= time <= 23):
            return None, None, "时间点必须在0-23之间"
            
        return node, time, None
    except (ValueError, TypeError):
        return None, None, "节点ID和时间点必须是整数"

@app.route("/", methods=["GET"])
def home():
    """健康检查端点"""
    return jsonify({
        "service": "Traffic Flow Prediction Model Server",
        "status": "running",
        "model_loaded": model is not None,
        "timestamp": datetime.now().isoformat(),
        "version": "2.0.0"
    })

@app.route("/predict", methods=["GET", "POST"])
def predict_volume():
    """预测节点流量"""
    start_time = datetime.now()
    
    try:
        # 检查模型是否已加载
        if model is None:
            logger.error("模型未加载，尝试重新加载")
            if not load_model():
                return jsonify({
                    "error": "模型服务不可用",
                    "code": "MODEL_NOT_LOADED"
                }), 503
        
        # 获取参数
        if request.method == "POST":
            data = request.get_json()
            if not data:
                return jsonify({"error": "请求体为空"}), 400
            node = data.get("node")
            time = data.get("time")
        else:  # GET 请求
            node = request.args.get("node")
            time = request.args.get("time")
        
        # 验证参数
        node, time, error = validate_parameters(node, time)
        if error:
            return jsonify({"error": error}), 400
        
        # 准备数据并预测
        df = pd.DataFrame([[node, time]], columns=["node", "time"])
        prediction = model.predict(df)[0]
        
        # 确保预测值合理
        volume = max(float(prediction), 1.0)
        
        # 计算处理时间
        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        
        logger.info(f"预测成功 - 节点: {node}, 时间: {time}, 流量: {volume:.2f}, 耗时: {processing_time:.2f}ms")
        
        return jsonify({
            "node": node,
            "time": time,
            "volume": volume,
            "processing_time_ms": round(processing_time, 2),
            "timestamp": datetime.now().isoformat(),
            "success": True
        })
        
    except Exception as e:
        error_msg = str(e)
        logger.error(f"预测失败: {error_msg}")
        logger.error(traceback.format_exc())
        
        return jsonify({
            "error": f"预测过程中发生错误: {error_msg}",
            "code": "PREDICTION_ERROR",
            "success": False
        }), 500

@app.route("/predict/batch", methods=["POST"])
def predict_batch():
    """批量预测多个节点流量"""
    start_time = datetime.now()
    
    try:
        # 检查模型是否已加载
        if model is None:
            if not load_model():
                return jsonify({
                    "error": "模型服务不可用",
                    "code": "MODEL_NOT_LOADED"
                }), 503
        
        data = request.get_json()
        if not data:
            return jsonify({"error": "请求体为空"}), 400
        
        node_ids = data.get("nodeIds", [])
        time_point = data.get("timePoint")
        
        if not node_ids:
            return jsonify({"error": "节点ID列表不能为空"}), 400
        
        # 验证时间点
        _, time_point, error = validate_parameters(1, time_point)
        if error:
            return jsonify({"error": f"时间点错误: {error}"}), 400
        
        # 批量预测
        predictions = {}
        for node_id in node_ids:
            try:
                node_id = int(node_id)
                if node_id <= 0:
                    continue
                    
                df = pd.DataFrame([[node_id, time_point]], columns=["node", "time"])
                prediction = model.predict(df)[0]
                predictions[str(node_id)] = max(float(prediction), 1.0)
            except Exception as e:
                logger.warning(f"节点 {node_id} 预测失败: {str(e)}")
                continue
        
        # 计算处理时间
        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        
        logger.info(f"批量预测成功 - 节点数: {len(predictions)}, 时间: {time_point}, 耗时: {processing_time:.2f}ms")
        
        return jsonify({
            "timePoint": time_point,
            "predictions": predictions,
            "count": len(predictions),
            "processing_time_ms": round(processing_time, 2),
            "timestamp": datetime.now().isoformat(),
            "success": True
        })
        
    except Exception as e:
        error_msg = str(e)
        logger.error(f"批量预测失败: {error_msg}")
        logger.error(traceback.format_exc())
        
        return jsonify({
            "error": f"批量预测过程中发生错误: {error_msg}",
            "code": "BATCH_PREDICTION_ERROR",
            "success": False
        }), 500

@app.route("/status", methods=["GET"])
def get_status():
    """获取服务状态"""
    return jsonify({
        "status": "healthy",
        "model_loaded": model is not None,
        "model_path": MODEL_PATH,
        "model_exists": os.path.exists(MODEL_PATH),
        "timestamp": datetime.now().isoformat(),
        "uptime": "运行中"
    })

@app.route("/reload", methods=["POST"])
def reload_model():
    """重新加载模型"""
    logger.info("收到模型重载请求")
    success = load_model()
    
    return jsonify({
        "success": success,
        "message": "模型重载成功" if success else "模型重载失败",
        "timestamp": datetime.now().isoformat()
    }), 200 if success else 500

@app.errorhandler(404)
def not_found(error):
    return jsonify({
        "error": "API端点不存在",
        "code": "NOT_FOUND",
        "available_endpoints": ["/", "/predict", "/predict/batch", "/status", "/reload"]
    }), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({
        "error": "服务器内部错误",
        "code": "INTERNAL_ERROR"
    }), 500

# 启动时加载模型
@app.before_first_request
def initialize():
    logger.info("🚀 启动交通流量预测模型服务器...")
    load_model()

if __name__ == '__main__':
    print("🚀 启动交通流量预测模型服务器...")
    print(f"📁 模型路径: {MODEL_PATH}")
    
    # 初始加载模型
    load_model()
    
    # 启动Flask应用
    app.run(
        host='0.0.0.0',
        port=5000,
        debug=False,  # 生产环境设为False
        threaded=True  # 支持多线程
    ) 