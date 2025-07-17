package com.example.navigation.controller;

import com.example.navigation.model.dto.map.HeatmapResponse;
import com.example.navigation.model.dto.map.CongestionResponse;
import com.example.navigation.service.MapService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 地图控制器
 * 处理地图数据相关接口，包括热力图和拥挤度数据
 */
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {
    
    private static final Logger logger = LoggerFactory.getLogger(MapController.class);
    
    private final MapService mapService;

    /**
     * 获取热力图数据
     */
    @GetMapping("/heatmap")
    public ResponseEntity<HeatmapResponse> getHeatmapData() {
        logger.info("获取热力图数据请求");
        
        try {
            HeatmapResponse response = mapService.getHeatmapData();
            logger.info("获取热力图数据成功，数据点数量: {}", response.getData().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取热力图数据失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取拥挤度数据
     */
    @GetMapping("/congestion")
    public ResponseEntity<CongestionResponse> getCongestionData() {
        logger.info("获取拥挤度数据请求");
        
        try {
            CongestionResponse response = mapService.getCongestionData();
            logger.info("获取拥挤度数据成功，区域数量: {}", response.getData().getRegions().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取拥挤度数据失败: error={}", e.getMessage());
            throw e;
        }
    }
} 