package com.example.navigation.controller;

import com.example.navigation.service.PythonModelService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器学习模型控制器
 * 提供流量预测和模型状态相关接口
 */
@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
public class ModelController {
    
    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    
    private final PythonModelService pythonModelService;

    /**
     * 预测单个节点的流量
     */
    @GetMapping("/predict")
    public ResponseEntity<Map<String, Object>> predictVolume(
            @RequestParam Integer nodeId,
            @RequestParam Integer timePoint) {
        
        logger.info("收到流量预测请求: nodeId={}, timePoint={}", nodeId, timePoint);
        
        try {
            double volume = pythonModelService.predictVolume(nodeId, timePoint);
            
            Map<String, Object> response = new HashMap<>();
            response.put("nodeId", nodeId);
            response.put("timePoint", timePoint);
            response.put("volume", volume);
            response.put("success", true);
            
            logger.info("流量预测成功: nodeId={}, timePoint={}, volume={}", 
                       nodeId, timePoint, volume);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("流量预测失败: nodeId={}, timePoint={}, 错误: {}", 
                        nodeId, timePoint, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("nodeId", nodeId);
            response.put("timePoint", timePoint);
            response.put("error", e.getMessage());
            response.put("success", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量预测多个节点的流量
     */
    @PostMapping("/predict/batch")
    public ResponseEntity<Map<String, Object>> predictVolumesBatch(
            @RequestBody BatchPredictRequest request) {
        
        logger.info("收到批量流量预测请求: nodeIds={}, timePoint={}", 
                   request.getNodeIds(), request.getTimePoint());
        
        try {
            Map<Integer, Double> predictions = pythonModelService.predictVolumesBatch(
                    request.getNodeIds(), request.getTimePoint());
            
            Map<String, Object> response = new HashMap<>();
            response.put("timePoint", request.getTimePoint());
            response.put("predictions", predictions);
            response.put("success", true);
            response.put("count", predictions.size());
            
            logger.info("批量流量预测成功: 预测了{}个节点", predictions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批量流量预测失败: nodeIds={}, timePoint={}, 错误: {}", 
                        request.getNodeIds(), request.getTimePoint(), e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("timePoint", request.getTimePoint());
            response.put("error", e.getMessage());
            response.put("success", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查模型服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getModelStatus() {
        logger.info("检查模型服务状态");
        
        boolean isAvailable = pythonModelService.isServiceAvailable();
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("message", isAvailable ? "模型服务正常运行" : "模型服务不可用");
        response.put("timestamp", System.currentTimeMillis());
        
        logger.info("模型服务状态: {}", isAvailable ? "可用" : "不可用");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量预测请求DTO
     */
    public static class BatchPredictRequest {
        private List<Integer> nodeIds;
        private Integer timePoint;

        // Getters and Setters
        public List<Integer> getNodeIds() {
            return nodeIds;
        }

        public void setNodeIds(List<Integer> nodeIds) {
            this.nodeIds = nodeIds;
        }

        public Integer getTimePoint() {
            return timePoint;
        }

        public void setTimePoint(Integer timePoint) {
            this.timePoint = timePoint;
        }
    }
} 