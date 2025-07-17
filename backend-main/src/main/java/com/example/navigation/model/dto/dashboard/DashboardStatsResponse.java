package com.example.navigation.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘统计数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private boolean success;
    private DashboardStatsData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsData {
        private String congestionLevel;    // 拥挤级别
        private Integer congestedSections; // 拥挤路段数量
    }
} 