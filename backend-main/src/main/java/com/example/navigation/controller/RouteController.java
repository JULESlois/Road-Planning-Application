package com.example.navigation.controller;

import com.example.navigation.model.dto.route.RouteRequest;
import com.example.navigation.model.dto.route.RouteResponse;
import com.example.navigation.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * 路径规划控制器
 * 处理路径规划相关接口
 */
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
    
    private final RouteService routeService;

    /**
     * 规划路线接口
     */
    @PostMapping("/plan")
    public ResponseEntity<RouteResponse> planRoute(@Valid @RequestBody RouteRequest request) {
        logger.info("收到路径规划请求: 起点({}, {}), 终点({}, {}), 路线类型: {}", 
                   request.getStartLat(), request.getStartLng(),
                   request.getEndLat(), request.getEndLng(),
                   request.getRouteType());
        
        try {
            RouteResponse route = routeService.planRoute(
                    request.getStartLat(), request.getStartLng(),
                    request.getEndLat(), request.getEndLng(),
                    request.getRouteType()
            );
            
            logger.info("路径规划成功: 距离={}km, 时间={}分钟, 节点数={}", 
                       String.format("%.2f", route.getTotalDistance()),
                       String.format("%.2f", route.getTotalTime()),
                       route.getPoints().size());
            
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            logger.error("路径规划失败: 起点({}, {}), 终点({}, {}), 错误: {}", 
                        request.getStartLat(), request.getStartLng(),
                        request.getEndLat(), request.getEndLng(),
                        e.getMessage());
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }
}