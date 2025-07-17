package com.example.navigation.repository;

import com.example.navigation.model.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EdgeRepository extends JpaRepository<Edge, Long> {
    
    /**
     * 根据源节点ID查询边
     */
    List<Edge> findByNodeID1(Integer nodeID1);

    /**
     * 根据目标节点ID查询边
     */
    List<Edge> findByNodeID2(Integer nodeID2);

    /**
     * 根据源节点和目标节点ID查询边
     */
    List<Edge> findByNodeID1AndNodeID2(Integer nodeID1, Integer nodeID2);

    /**
     * 查询包含指定节点的所有边（作为起点或终点）
     */
    List<Edge> findByNodeID1OrNodeID2(Integer nodeID, Integer nodeID2);
}