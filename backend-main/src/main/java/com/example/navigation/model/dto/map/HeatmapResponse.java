package com.example.navigation.model.dto.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 热力图数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapResponse {
    private boolean success;
    private List<HeatmapPoint> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapPoint {
        private double lat;        // 纬度
        private double lng;        // 经度
        private double intensity;  // 热度强度
    }
} 