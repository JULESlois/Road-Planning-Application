package com.example.navigation.model.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 路径规划响应DTO，用于/api/routes/plan接口的返回数据
 * 包含规划路线的详细信息，如途经节点、总距离、预计时间等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private List<RoutePoint> points;
    private double totalDistance; // 总距离(公里)
    private double totalTime; // 总时间(分钟)
    private String routeType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutePoint {
        private double lat;
        private double lng;
    }
}