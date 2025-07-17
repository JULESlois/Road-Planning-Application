package com.example.navigation.model.dto.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 拥挤度数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongestionResponse {
    private boolean success;
    private CongestionData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CongestionData {
        private List<CongestionRegion> regions;
        private String updateTime; // 更新时间
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CongestionRegion {
        private String level; // 拥挤级别
        private double value; // 拥挤度值
    }
} 