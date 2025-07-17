package com.example.navigation.service;

import com.example.navigation.model.dto.map.HeatmapResponse;
import com.example.navigation.model.dto.map.CongestionResponse;
import com.example.navigation.model.entity.Flow;
import com.example.navigation.model.entity.Node;
import com.example.navigation.repository.FlowRepository;
import com.example.navigation.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地图服务类
 * 提供热力图和拥挤度数据分析功能
 */
@Service
@RequiredArgsConstructor
public class MapService {
    
    private static final Logger logger = LoggerFactory.getLogger(MapService.class);
    
    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;

    /**
     * 获取热力图数据
     */
    public HeatmapResponse getHeatmapData() {
        logger.info("开始生成热力图数据");
        
        try {
            // 获取当前时间的流量数据
            Integer currentDay = getCurrentDay();
            List<Flow> flowData = flowRepository.findByDay(currentDay);
            
            // 获取所有节点信息
            List<Node> nodes = nodeRepository.findAll();
            Map<Integer, Node> nodeMap = nodes.stream()
                    .collect(Collectors.toMap(Node::getNodeID, node -> node));
            
            // 生成热力图数据点
            List<HeatmapResponse.HeatmapPoint> heatmapPoints = flowData.stream()
                    .filter(flow -> nodeMap.containsKey(flow.getNodeID()))
                    .map(flow -> {
                        Node node = nodeMap.get(flow.getNodeID());
                        try {
                            double lat = Double.parseDouble(node.getLatitude());
                            double lng = Double.parseDouble(node.getLongitude());
                            double intensity = normalizeIntensity(flow.getFlow());
                            return new HeatmapResponse.HeatmapPoint(lat, lng, intensity);
                        } catch (NumberFormatException e) {
                            logger.warn("节点坐标格式错误: nodeID={}, lat={}, lng={}", 
                                       node.getNodeID(), node.getLatitude(), node.getLongitude());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            HeatmapResponse response = new HeatmapResponse(true, heatmapPoints);
            
            logger.info("热力图数据生成完成，数据点数量: {}", heatmapPoints.size());
            return response;
        } catch (Exception e) {
            logger.error("生成热力图数据失败: error={}", e.getMessage());
            throw new RuntimeException("获取热力图数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取拥挤度数据
     */
    public CongestionResponse getCongestionData() {
        logger.info("开始生成拥挤度数据");
        
        try {
            // 获取当前时间的流量数据
            Integer currentDay = getCurrentDay();
            List<Flow> flowData = flowRepository.findByDay(currentDay);
            
            // 按拥挤级别分组统计
            Map<String, Long> congestionStats = flowData.stream()
                    .collect(Collectors.groupingBy(
                            this::getCongestionLevel,
                            Collectors.counting()
                    ));
            
            // 计算各拥挤级别的拥挤度值（百分比）
            long totalSections = flowData.size();
            List<CongestionResponse.CongestionRegion> regions = congestionStats.entrySet().stream()
                    .map(entry -> {
                        String level = entry.getKey();
                        double value = totalSections > 0 ? (entry.getValue() * 100.0 / totalSections) : 0.0;
                        return new CongestionResponse.CongestionRegion(level, value);
                    })
                    .collect(Collectors.toList());
            
            // 添加缺失的拥挤级别（值为0）
            Set<String> existingLevels = regions.stream()
                    .map(CongestionResponse.CongestionRegion::getLevel)
                    .collect(Collectors.toSet());
            
            Arrays.asList("畅通", "缓慢", "拥堵", "严重拥堵").forEach(level -> {
                if (!existingLevels.contains(level)) {
                    regions.add(new CongestionResponse.CongestionRegion(level, 0.0));
                }
            });
            
            // 按拥挤程度排序
            regions.sort(Comparator.comparingDouble(r -> {
                switch (r.getLevel()) {
                    case "畅通": return 1;
                    case "缓慢": return 2;
                    case "拥堵": return 3;
                    case "严重拥堵": return 4;
                    default: return 0;
                }
            }));
            
            String updateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            CongestionResponse.CongestionData data = 
                new CongestionResponse.CongestionData(regions, updateTime);
            
            CongestionResponse response = new CongestionResponse(true, data);
            
            logger.info("拥挤度数据生成完成，区域数量: {}", regions.size());
            return response;
        } catch (Exception e) {
            logger.error("生成拥挤度数据失败: error={}", e.getMessage());
            throw new RuntimeException("获取拥挤度数据失败: " + e.getMessage());
        }
    }

    /**
     * 标准化热力图强度值（0-1之间）
     */
    private double normalizeIntensity(int flow) {
        // 假设最大流量为200，将流量值标准化到0-1之间
        double maxFlow = 200.0;
        return Math.min(flow / maxFlow, 1.0);
    }

    /**
     * 根据流量值确定拥挤级别
     */
    private String getCongestionLevel(Flow flow) {
        int flowValue = flow.getFlow();
        if (flowValue >= 100) {
            return "严重拥堵";
        } else if (flowValue >= 70) {
            return "拥堵";
        } else if (flowValue >= 40) {
            return "缓慢";
        } else {
            return "畅通";
        }
    }

    /**
     * 获取当前日期
     */
    private Integer getCurrentDay() {
        // 简化实现，实际应该根据当前日期计算
        return 1; // 假设当前是第1天
    }
} 