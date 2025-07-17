package com.example.navigation.controller;

import com.example.navigation.model.dto.dashboard.DashboardStatsResponse;
import com.example.navigation.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 * 处理交通统计信息相关接口
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    private final DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        logger.info("获取仪表盘统计数据请求");
        
        try {
            DashboardStatsResponse response = dashboardService.getDashboardStats();
            logger.info("获取仪表盘统计数据成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取仪表盘统计数据失败: error={}", e.getMessage());
            throw e;
        }
    }
} 