package com.example.navigation.model.dto.route;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 路径规划请求DTO，用于/api/routes/plan接口的请求参数
 * 包含起点终点坐标和路线偏好类型，供路径规划服务使用
 */
@Data
public class RouteRequest {
    @NotNull(message = "起点纬度不能为空")
    private Double startLat;     // 起点纬度坐标

    @NotNull(message = "起点经度不能为空")
    private Double startLng;     // 起点经度坐标

    @NotNull(message = "终点纬度不能为空")
    private Double endLat;       // 终点纬度坐标

    @NotNull(message = "终点经度不能为空")
    private Double endLng;       // 终点经度坐标

    // 路线类型：fastest(最快), shortest(最短), avoidingTraffic(避堵)
    private String routeType = "fastest";

    // Getters
    public Double getStartLat() { return startLat; }
    public Double getStartLng() { return startLng; }
    public Double getEndLat() { return endLat; }
    public Double getEndLng() { return endLng; }
    public String getRouteType() { return routeType; }

    // Setters
    public void setStartLat(Double startLat) { this.startLat = startLat; }
    public void setStartLng(Double startLng) { this.startLng = startLng; }
    public void setEndLat(Double endLat) { this.endLat = endLat; }
    public void setEndLng(Double endLng) { this.endLng = endLng; }
    public void setRouteType(String routeType) { this.routeType = routeType; }
}