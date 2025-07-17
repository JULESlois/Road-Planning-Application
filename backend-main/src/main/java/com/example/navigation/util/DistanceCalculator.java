package com.example.navigation.util;

/**
 * 距离计算工具类
 * 支持处理String和Double类型的坐标
 */
public class DistanceCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 使用Haversine公式计算两个经纬度坐标之间的距离（公里）
     * @param lat1 起点纬度（Double类型）
     * @param lon1 起点经度（Double类型）
     * @param lat2 终点纬度（Double类型）
     * @param lon2 终点经度（Double类型）
     * @return 距离（公里）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 计算纬度和经度的差值
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine公式
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离（公里）
        return EARTH_RADIUS_KM * c;
    }

    /**
     * 使用Haversine公式计算两个经纬度坐标之间的距离（公里）
     * @param lat1 起点纬度（String类型）
     * @param lon1 起点经度（String类型）
     * @param lat2 终点纬度（String类型）
     * @param lon2 终点经度（String类型）
     * @return 距离（公里）
     * @throws NumberFormatException 当坐标字符串无法转换为数字时抛出
     */
    public static double calculateDistance(String lat1, String lon1, String lat2, String lon2) {
        try {
            double lat1Double = Double.parseDouble(lat1);
            double lon1Double = Double.parseDouble(lon1);
            double lat2Double = Double.parseDouble(lat2);
            double lon2Double = Double.parseDouble(lon2);
            
            return calculateDistance(lat1Double, lon1Double, lat2Double, lon2Double);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("无法解析坐标字符串为数值: " + e.getMessage());
        }
    }

    /**
     * 混合类型坐标距离计算（String和Double混合）
     */
    public static double calculateDistance(String lat1, String lon1, double lat2, double lon2) {
        return calculateDistance(Double.parseDouble(lat1), Double.parseDouble(lon1), lat2, lon2);
    }

    /**
     * 混合类型坐标距离计算（Double和String混合）
     */
    public static double calculateDistance(double lat1, double lon1, String lat2, String lon2) {
        return calculateDistance(lat1, lon1, Double.parseDouble(lat2), Double.parseDouble(lon2));
    }
}