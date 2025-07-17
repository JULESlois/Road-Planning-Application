package com.example.navigation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Python机器学习模型服务类
 * 用于调用Python模型预测节点流量，支持高级路径规划算法
 */
@Service
public class PythonModelService {

    private static final Logger logger = LoggerFactory.getLogger(PythonModelService.class);

    @Value("${model.service.url:http://localhost:5000}")
    private String modelServiceUrl;

    @Value("${model.service.timeout:5000}")
    private int timeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 预测指定节点在指定时间的流量
     * @param nodeId 节点ID
     * @param timePoint 时间点（0-23小时格式）
     * @return 预测的流量值，如果预测失败返回默认值50
     */
    public double predictVolume(int nodeId, int timePoint) {
        logger.debug("开始预测节点流量: nodeId={}, timePoint={}", nodeId, timePoint);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = new URIBuilder(modelServiceUrl + "/predict")
                    .setParameter("node", String.valueOf(nodeId))
                    .setParameter("time", String.valueOf(timePoint))
                    .build();

            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    double volume = jsonNode.get("volume").asDouble();
                    
                    logger.debug("节点流量预测成功: nodeId={}, timePoint={}, volume={}", 
                               nodeId, timePoint, volume);
                    return Math.max(volume, 1.0); // 确保流量值至少为1
                } else {
                    logger.warn("模型服务返回错误状态码: {}, 响应: {}", statusCode, responseBody);
                    return getDefaultVolume();
                }
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("调用Python模型服务时发生错误: nodeId={}, timePoint={}, 错误: {}", 
                        nodeId, timePoint, e.getMessage());
            return getDefaultVolume();
        }
    }

    /**
     * 批量预测多个节点的流量
     * @param nodeIds 节点ID列表
     * @param timePoint 时间点
     * @return 节点ID到流量值的映射
     */
    public java.util.Map<Integer, Double> predictVolumesBatch(java.util.List<Integer> nodeIds, int timePoint) {
        logger.debug("开始批量预测节点流量: nodeIds={}, timePoint={}", nodeIds, timePoint);
        
        java.util.Map<Integer, Double> results = new java.util.HashMap<>();
        
        for (Integer nodeId : nodeIds) {
            double volume = predictVolume(nodeId, timePoint);
            results.put(nodeId, volume);
        }
        
        logger.debug("批量预测完成，预测了{}个节点的流量", results.size());
        return results;
    }

    /**
     * 检查Python模型服务是否可用
     * @return 服务是否可用
     */
    public boolean isServiceAvailable() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 发送一个简单的预测请求来测试服务
            URI uri = new URIBuilder(modelServiceUrl + "/predict")
                    .setParameter("node", "1")
                    .setParameter("time", "8")
                    .build();

            HttpGet httpGet = new HttpGet(uri);
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                boolean available = statusCode == 200;
                
                if (available) {
                    logger.info("Python模型服务可用: {}", modelServiceUrl);
                } else {
                    logger.warn("Python模型服务不可用，状态码: {}", statusCode);
                }
                
                return available;
            }
        } catch (Exception e) {
            logger.error("检查Python模型服务可用性时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取默认流量值（当模型服务不可用时使用）
     * @return 默认流量值
     */
    private double getDefaultVolume() {
        return 50.0; // 默认中等流量水平
    }
} 