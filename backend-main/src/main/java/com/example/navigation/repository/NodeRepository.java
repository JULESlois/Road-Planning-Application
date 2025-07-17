package com.example.navigation.repository;

import com.example.navigation.model.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NodeRepository extends JpaRepository<Node, Integer> {
    
    /**
     * 根据坐标范围查询节点
     * 注意：现在经纬度是String类型，需要转换为数值进行比较
     */
    @Query("SELECT n FROM Node n WHERE " +
           "CAST(n.latitude AS double) BETWEEN :minLat AND :maxLat AND " +
           "CAST(n.longitude AS double) BETWEEN :minLng AND :maxLng")
    List<Node> findNodesInBounds(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng);

    /**
     * 根据节点ID查询节点
     */
    Node findByNodeID(Integer nodeID);

    /**
     * 检查节点ID是否存在
     */
    boolean existsByNodeID(Integer nodeID);
}