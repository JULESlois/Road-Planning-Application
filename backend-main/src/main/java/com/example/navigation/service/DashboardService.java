package com.example.navigation.service;

import com.example.navigation.model.dto.dashboard.DashboardStatsResponse;
import com.example.navigation.model.entity.Flow;
import com.example.navigation.repository.FlowRepository;
import com.example.navigation.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仪表盘服务类
 * 提供交通统计信息和数据分析功能
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;

    /**
     * 获取仪表盘统计数据
     */
    public DashboardStatsResponse getDashboardStats() {
        logger.info("开始计算仪表盘统计数据");
        
        try {
            // 获取当前时间的交通数据
            Integer currentDay = getCurrentDay();
            List<Flow> currentFlowData = flowRepository.findByDay(currentDay);
            
            // 计算拥挤级别和拥挤路段数量
            String congestionLevel = calculateCongestionLevel(currentFlowData);
            Integer congestedSections = calculateCongestedSections(currentFlowData);
            
            DashboardStatsResponse.DashboardStatsData data = 
                new DashboardStatsResponse.DashboardStatsData(congestionLevel, congestedSections);
            
            DashboardStatsResponse response = new DashboardStatsResponse(true, data);
            
            logger.info("仪表盘统计数据计算完成: 拥挤级别={}, 拥挤路段数={}", 
                       congestionLevel, congestedSections);
            
            return response;
        } catch (Exception e) {
            logger.error("计算仪表盘统计数据失败: error={}", e.getMessage());
            throw new RuntimeException("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 计算整体拥挤级别
     */
    private String calculateCongestionLevel(List<Flow> flowData) {
        if (flowData.isEmpty()) {
            return "畅通";
        }
        
        // 计算平均流量
        double averageFlow = flowData.stream()
                .mapToInt(Flow::getFlow)
                .average()
                .orElse(0.0);
        
        // 根据平均流量确定拥挤级别
        if (averageFlow >= 100) {
            return "严重拥堵";
        } else if (averageFlow >= 70) {
            return "拥堵";
        } else if (averageFlow >= 40) {
            return "缓慢";
        } else {
            return "畅通";
        }
    }

    /**
     * 计算拥挤路段数量
     */
    private Integer calculateCongestedSections(List<Flow> flowData) {
        // 计算流量超过拥堵阈值的节点数量
        long congestedNodes = flowData.stream()
                .filter(flow -> flow.getFlow() >= 70) // 拥堵阈值
                .count();
        
        return (int) congestedNodes;
    }

    /**
     * 获取当前日期
     */
    private Integer getCurrentDay() {
        // 简化实现，实际应该根据当前日期计算
        return 1; // 假设当前是第1天
    }
} 