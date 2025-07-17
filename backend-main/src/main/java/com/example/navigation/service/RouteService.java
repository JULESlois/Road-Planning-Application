package com.example.navigation.service;

import com.example.navigation.model.dto.route.RouteResponse;
import com.example.navigation.model.entity.Edge;
import com.example.navigation.model.entity.Node;
import com.example.navigation.model.entity.Flow;
import com.example.navigation.repository.EdgeRepository;
import com.example.navigation.repository.NodeRepository;
import com.example.navigation.repository.FlowRepository;
import com.example.navigation.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 路径规划服务类
 * 提供最短路径、最快路径和避堵路径算法
 */
@Service
@RequiredArgsConstructor
public class RouteService {
    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);
    
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final FlowRepository flowRepository;
    private final PythonModelService pythonModelService;

    @Value("${route.planner.congestion-alpha:0.05}")
    private double congestionAlpha;

    @Value("${route.planner.default-speed-kmh:50.0}")
    private double defaultSpeedKmh;

    /**
     * 规划路线
     */
    public RouteResponse planRoute(double startLat, double startLng, double endLat, double endLng, String routeType) {
        logger.info("开始规划路线: 起点({}, {}), 终点({}, {}), 路线类型: {}", 
                   startLat, startLng, endLat, endLng, routeType);
        
        // 1. 找到最近的起点和终点节点
        Node startNode = findNearestNode(startLat, startLng);
        Node endNode = findNearestNode(endLat, endLng);

        if (startNode == null || endNode == null) {
            logger.error("无法找到起点或终点附近的道路节点");
            throw new IllegalArgumentException("无法找到起点或终点附近的道路节点");
        }

        logger.debug("找到起点节点: ID={}, 坐标({}, {})", 
                    startNode.getNodeID(), startNode.getLatitude(), startNode.getLongitude());
        logger.debug("找到终点节点: ID={}, 坐标({}, {})", 
                    endNode.getNodeID(), endNode.getLatitude(), endNode.getLongitude());

        // 2. 根据路线类型规划路径
        List<Node> pathNodes;
        switch (routeType.toLowerCase()) {
            case "shortest":
                pathNodes = findShortestPath(startNode, endNode);
                break;
            case "avoidingtraffic":
                pathNodes = findAvoidingTrafficPath(startNode, endNode);
                break;
            case "fastest":
            default:
                pathNodes = findFastestPath(startNode, endNode);
        }

        logger.info("路径规划完成，找到 {} 个节点", pathNodes.size());

        // 3. 转换为响应格式，包含实际起终点连接
        return convertToRouteResponse(pathNodes, routeType, startLat, startLng, endLat, endLng);
    }

    /**
     * 查找最近的道路节点
     */
    private Node findNearestNode(double lat, double lng) {
        // 查询附近节点（实际应用中应限制查询范围）
        List<Node> nearbyNodes = nodeRepository.findAll();
        if (nearbyNodes.isEmpty()) {
            logger.warn("数据库中没有找到任何节点");
            return null;
        }

        // 计算距离并找到最近的节点
        return nearbyNodes.stream()
                .min(Comparator.comparingDouble(node -> {
                    try {
                        return DistanceCalculator.calculateDistance(lat, lng, 
                            Double.parseDouble(node.getLatitude()), 
                            Double.parseDouble(node.getLongitude()));
                    } catch (NumberFormatException e) {
                        logger.error("节点坐标格式错误: ID={}, lat={}, lng={}", 
                                   node.getNodeID(), node.getLatitude(), node.getLongitude());
                        return Double.MAX_VALUE;
                    }
                }))
                .orElse(null);
    }

    /**
     * 最短路径算法（Dijkstra算法）
     */
    private List<Node> findShortestPath(Node start, Node end) {
        logger.debug("使用最短路径算法规划路线");
        return findPath(start, end, false);
    }

    /**
     * 最快路径算法（考虑交通状况）
     */
    private List<Node> findFastestPath(Node start, Node end) {
        logger.debug("使用最快路径算法规划路线");
        return findPath(start, end, true);
    }

    /**
     * 避堵路径算法（优先选择交通畅通的道路）
     */
    private List<Node> findAvoidingTrafficPath(Node start, Node end) {
        logger.debug("使用避堵路径算法规划路线");
        
        // 获取当前日期（这里简化为固定值，实际应用中应获取当前日期）
        Integer currentDay = getCurrentDay();
        List<Flow> recentFlow = flowRepository.findByDay(currentDay);

        // 构建高流量节点集合（简化逻辑：认为高流量节点为拥堵节点）
        Set<Integer> congestedNodeIds = recentFlow.stream()
                .filter(flow -> flow.getFlow() > getHighFlowThreshold())
                .map(Flow::getNodeID)
                .collect(Collectors.toSet());

        logger.debug("检测到 {} 个拥堵节点", congestedNodeIds.size());

        // 使用Dijkstra算法，避开拥堵节点
        return findPathWithAvoidance(start, end, congestedNodeIds);
    }

    /**
     * 带规避的路径查找
     */
    private List<Node> findPathWithAvoidance(Node start, Node end, Set<Integer> avoidNodeIds) {
        // 实现带规避的Dijkstra算法
        Map<Integer, List<Edge>> adjacencyList = buildAdjacencyList();
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> predecessors = new HashMap<>();
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        // 初始化
        List<Node> allNodes = nodeRepository.findAll();
        for (Node node : allNodes) {
            distances.put(node.getNodeID(), node.getNodeID().equals(start.getNodeID()) ? 0.0 : Double.MAX_VALUE);
        }
        priorityQueue.add(start.getNodeID());

        while (!priorityQueue.isEmpty()) {
            Integer currentNodeID = priorityQueue.poll();

            // 如果是需要避开的节点，跳过
            if (avoidNodeIds.contains(currentNodeID) && !currentNodeID.equals(start.getNodeID())) {
                continue;
            }

            // 如果到达终点，结束搜索
            if (currentNodeID.equals(end.getNodeID())) {
                break;
            }

            // 遍历相邻节点
            for (Edge edge : adjacencyList.getOrDefault(currentNodeID, Collections.emptyList())) {
                Integer neighborID = edge.getNodeID2();
                if (avoidNodeIds.contains(neighborID)) {
                    continue; // 跳过拥堵节点
                }

                double weight = edge.getDistance().doubleValue();
                double newDistance = distances.get(currentNodeID) + weight;

                if (newDistance < distances.getOrDefault(neighborID, Double.MAX_VALUE)) {
                    distances.put(neighborID, newDistance);
                    predecessors.put(neighborID, currentNodeID);
                    priorityQueue.add(neighborID);
                }
            }
        }

        return reconstructPath(predecessors, start.getNodeID(), end.getNodeID());
    }

    /**
     * Dijkstra算法实现
     */
    private List<Node> findPath(Node start, Node end, boolean considerTraffic) {
        logger.debug("开始Dijkstra算法: 起点ID={}, 终点ID={}, 考虑交通={}", 
                    start.getNodeID(), end.getNodeID(), considerTraffic);
        
        // 图的邻接表表示
        Map<Integer, List<Edge>> adjacencyList = buildAdjacencyList();
        
        // 距离表：记录从起点到每个节点的最短距离
        Map<Integer, Double> distances = new HashMap<>();
        // 前驱节点表：记录路径
        Map<Integer, Integer> predecessors = new HashMap<>();
        // 优先队列：按距离排序节点
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(nodeId -> distances.getOrDefault(nodeId, Double.MAX_VALUE)));

        // 初始化
        List<Node> allNodes = nodeRepository.findAll();
        for (Node node : allNodes) {
            distances.put(node.getNodeID(), node.getNodeID().equals(start.getNodeID()) ? 0.0 : Double.MAX_VALUE);
        }
        priorityQueue.add(start.getNodeID());

        while (!priorityQueue.isEmpty()) {
            Integer currentNodeID = priorityQueue.poll();

            // 如果到达终点，结束搜索
            if (currentNodeID.equals(end.getNodeID())) {
                break;
            }

            // 遍历相邻节点
            for (Edge edge : adjacencyList.getOrDefault(currentNodeID, Collections.emptyList())) {
                Integer neighborID = edge.getNodeID2();
                
                // 检查边的距离是否为null
                if (edge.getDistance() == null) {
                    logger.warn("边的距离为null: 从节点{}到节点{}", edge.getNodeID1(), edge.getNodeID2());
                    continue; // 跳过这条边
                }
                
                // 根据是否考虑交通状况选择权重计算方法
                double weight;
                if (considerTraffic) {
                    // 使用改进的机器学习模型预测 + 拥堵系数计算
                    weight = calculateCongestionWeight(edge, getCurrentTimeStamp());
                } else {
                    // 只使用距离
                    weight = edge.getDistance().doubleValue();
                }
                
                double currentDistance = distances.getOrDefault(currentNodeID, Double.MAX_VALUE);
                double neighborDistance = distances.getOrDefault(neighborID, Double.MAX_VALUE);
                double newDistance = currentDistance + weight;

                if (newDistance < neighborDistance) {
                    distances.put(neighborID, newDistance);
                    predecessors.put(neighborID, currentNodeID);
                    priorityQueue.add(neighborID);
                }
            }
        }

        return reconstructPath(predecessors, start.getNodeID(), end.getNodeID());
    }

    /**
     * 构建图的邻接表
     */
    private Map<Integer, List<Edge>> buildAdjacencyList() {
        List<Edge> allEdges = edgeRepository.findAll();
        Map<Integer, List<Edge>> adjacencyList = new HashMap<>();

        for (Edge edge : allEdges) {
            adjacencyList.computeIfAbsent(edge.getNodeID1(), k -> new ArrayList<>()).add(edge);
            // 假设所有道路都是双向的
            Edge reverseEdge = new Edge();
            reverseEdge.setId(edge.getId());
            reverseEdge.setNodeID1(edge.getNodeID2());
            reverseEdge.setNodeID2(edge.getNodeID1());
            reverseEdge.setDistance(edge.getDistance());
            adjacencyList.computeIfAbsent(edge.getNodeID2(), k -> new ArrayList<>()).add(reverseEdge);
        }

        return adjacencyList;
    }

    /**
     * 计算边的行驶时间（考虑交通状况）
     * 优先使用机器学习模型预测，回退到数据库查询
     */
    private double getEdgeTravelTime(Edge edge) {
        logger.debug("计算边的行驶时间: 从节点{}到节点{}", edge.getNodeID1(), edge.getNodeID2());
        
        // 获取当前时间点
        Integer currentTimeStamp = getCurrentTimeStamp();
        
        // 尝试使用机器学习模型预测流量
        double averageFlow = getAverageFlowWithML(edge.getNodeID1(), edge.getNodeID2(), currentTimeStamp);
        
        // 如果ML模型不可用，回退到数据库查询
        if (averageFlow < 0) {
            averageFlow = getAverageFlowFromDatabase(edge, currentTimeStamp);
        }
        
        // 检查距离是否为null
        if (edge.getDistance() == null) {
            logger.warn("边的距离为null，使用默认距离1.0km: 从节点{}到节点{}", edge.getNodeID1(), edge.getNodeID2());
            return 1.0; // 返回默认行驶时间
        }
        
        // 使用改进的拥堵系数计算公式：时间 = 距离 * (1 + α * 流量) / 速度
        double congestionFactor = 1.0 + congestionAlpha * averageFlow;
        double travelTime = edge.getDistance() * congestionFactor / defaultSpeedKmh * 60; // 转换为分钟
        
        logger.debug("边的行驶时间计算完成: 距离={}, 平均流量={}, 拥堵系数={}, 行驶时间={}分钟", 
                    edge.getDistance(), averageFlow, congestionFactor, travelTime);
        
        return travelTime;
    }

    /**
     * 使用机器学习模型获取边的平均流量
     */
    private double getAverageFlowWithML(Integer nodeID1, Integer nodeID2, Integer timePoint) {
        try {
            if (!pythonModelService.isServiceAvailable()) {
                logger.debug("Python模型服务不可用，回退到数据库查询");
                return -1; // 返回负值表示ML不可用
            }

            // 预测两个节点的流量
            double flow1 = pythonModelService.predictVolume(nodeID1, timePoint);
            double flow2 = pythonModelService.predictVolume(nodeID2, timePoint);
            
            double averageFlow = (flow1 + flow2) / 2.0;
            
            logger.debug("机器学习模型预测流量: 节点{}={}, 节点{}={}, 平均流量={}", 
                        nodeID1, flow1, nodeID2, flow2, averageFlow);
            
            return averageFlow;
        } catch (Exception e) {
            logger.warn("机器学习模型预测失败: {}", e.getMessage());
            return -1; // 返回负值表示预测失败
        }
    }

    /**
     * 从数据库获取边的平均流量（备用方法）
     */
    private double getAverageFlowFromDatabase(Edge edge, Integer timeStamp) {
        Integer currentDay = getCurrentDay();
        
        Flow nodeFlow1 = flowRepository.findByNodeIDAndDayAndTimeStamp(edge.getNodeID1(), currentDay, timeStamp);
        Flow nodeFlow2 = flowRepository.findByNodeIDAndDayAndTimeStamp(edge.getNodeID2(), currentDay, timeStamp);

        double averageFlow = 0;
        int flowCount = 0;
        
        if (nodeFlow1 != null) {
            averageFlow += nodeFlow1.getFlow();
            flowCount++;
        }
        if (nodeFlow2 != null) {
            averageFlow += nodeFlow2.getFlow();
            flowCount++;
        }

        if (flowCount > 0) {
            averageFlow /= flowCount;
        } else {
            averageFlow = 50.0; // 默认中等流量
        }
        
        logger.debug("数据库查询流量: 节点{}={}, 节点{}={}, 平均流量={}", 
                    edge.getNodeID1(), nodeFlow1 != null ? nodeFlow1.getFlow() : "null",
                    edge.getNodeID2(), nodeFlow2 != null ? nodeFlow2.getFlow() : "null",
                    averageFlow);
        
        return averageFlow;
    }

    /**
     * 计算拥堵系数权重（mode1算法）
     * 使用公式：weight = distance * (1 + α * averageFlow)
     */
    private double calculateCongestionWeight(Edge edge, Integer timePoint) {
        logger.debug("计算拥堵系数权重: 边从节点{}到节点{}", edge.getNodeID1(), edge.getNodeID2());
        
        // 使用机器学习模型预测流量
        double averageFlow = getAverageFlowWithML(edge.getNodeID1(), edge.getNodeID2(), timePoint);
        
        // 如果ML模型不可用，回退到数据库查询
        if (averageFlow < 0) {
            averageFlow = getAverageFlowFromDatabase(edge, timePoint);
        }
        
        // 检查距离是否为null
        if (edge.getDistance() == null) {
            logger.warn("边的距离为null，使用默认距离1.0km: 从节点{}到节点{}", edge.getNodeID1(), edge.getNodeID2());
            return 1.0; // 返回默认权重
        }
        
        // 计算拥堵系数权重
        double weight = edge.getDistance() * (1.0 + congestionAlpha * averageFlow);
        
        logger.debug("拥堵系数权重计算完成: 距离={}, 流量={}, α={}, 权重={}", 
                    edge.getDistance(), averageFlow, congestionAlpha, weight);
        
        return weight;
    }

    /**
     * 重建路径
     */
    private List<Node> reconstructPath(Map<Integer, Integer> predecessors, Integer startID, Integer endID) {
        List<Integer> nodeIds = new ArrayList<>();
        Integer current = endID;

        while (current != null && !current.equals(startID)) {
            nodeIds.add(current);
            current = predecessors.get(current);
        }

        if (current == null) {
            logger.warn("无法找到从节点 {} 到节点 {} 的路径", startID, endID);
            return Collections.emptyList();
        }

        nodeIds.add(startID);
        Collections.reverse(nodeIds);

        // 转换为Node对象
        return nodeIds.stream()
                .map(nodeRepository::findByNodeID)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 转换为路线响应DTO
     */
    private RouteResponse convertToRouteResponse(List<Node> pathNodes, String routeType, 
                                               double startLat, double startLng, 
                                               double endLat, double endLng) {
        if (pathNodes.isEmpty()) {
            throw new IllegalArgumentException("无法规划路径");
        }

        // 计算总距离和总时间
        double totalDistance = 0.0;

        // 构建路线点列表，包含实际起点和终点
        List<RouteResponse.RoutePoint> points = new ArrayList<>();
        
        // 1. 添加实际起点
        points.add(new RouteResponse.RoutePoint(startLat, startLng));
        
        // 2. 计算起点到第一个路径节点的距离
        if (!pathNodes.isEmpty()) {
            Node firstNode = pathNodes.get(0);
            try {
                totalDistance += DistanceCalculator.calculateDistance(
                        String.valueOf(startLat), String.valueOf(startLng),
                        firstNode.getLatitude(), firstNode.getLongitude());
            } catch (NumberFormatException e) {
                logger.warn("计算起点到第一个节点距离时出错: {}", e.getMessage());
            }
        }
        
        // 3. 添加路径节点
        for (int i = 0; i < pathNodes.size(); i++) {
            Node node = pathNodes.get(i);
            try {
                double lat = Double.parseDouble(node.getLatitude());
                double lng = Double.parseDouble(node.getLongitude());
                points.add(new RouteResponse.RoutePoint(lat, lng));

                // 计算节点间距离
                if (i > 0) {
                    Node prevNode = pathNodes.get(i - 1);
                    totalDistance += DistanceCalculator.calculateDistance(
                            prevNode.getLatitude(), prevNode.getLongitude(),
                            node.getLatitude(), node.getLongitude());
                }
            } catch (NumberFormatException e) {
                logger.error("节点坐标格式错误: ID={}, lat={}, lng={}", 
                           node.getNodeID(), node.getLatitude(), node.getLongitude());
                throw new IllegalArgumentException("节点坐标格式错误");
            }
        }
        
        // 4. 添加实际终点
        points.add(new RouteResponse.RoutePoint(endLat, endLng));
        
        // 5. 计算最后一个路径节点到终点的距离
        if (!pathNodes.isEmpty()) {
            Node lastNode = pathNodes.get(pathNodes.size() - 1);
            try {
                totalDistance += DistanceCalculator.calculateDistance(
                        lastNode.getLatitude(), lastNode.getLongitude(),
                        String.valueOf(endLat), String.valueOf(endLng));
            } catch (NumberFormatException e) {
                logger.warn("计算最后一个节点到终点距离时出错: {}", e.getMessage());
            }
        }

        // 简单估算时间（实际应根据交通状况计算）
        double totalTime = totalDistance / 50 * 60; // 假设平均速度50km/h

        logger.info("路径规划结果: 总距离={}km, 预计时间={}分钟, 路径类型={}, 包含起终点连接", 
                   String.format("%.2f", totalDistance), 
                   String.format("%.2f", totalTime), 
                   routeType);

        return new RouteResponse(points, totalDistance, totalTime, routeType);
    }

    // 辅助方法
    private Integer getCurrentDay() {
        // 简化实现，实际应该根据当前日期计算
        return 1; // 假设当前是第1天
    }

    private Integer getCurrentTimeStamp() {
        // 简化实现，实际应该根据当前时间计算
        return 8; // 假设当前是8点
    }

    private int getHighFlowThreshold() {
        return 100; // 高流量阈值
    }

    private int getMediumFlowThreshold() {
        return 50; // 中等流量阈值
    }
}