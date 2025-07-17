package com.example.navigation.repository;

import com.example.navigation.model.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FlowRepository extends JpaRepository<Flow, Long> {
    
    /**
     * 根据节点ID查询车流量数据
     */
    List<Flow> findByNodeID(Integer nodeID);
    
    /**
     * 根据节点ID和日期查询车流量数据
     */
    List<Flow> findByNodeIDAndDay(Integer nodeID, Integer day);
    
    /**
     * 根据节点ID、日期和时间戳查询车流量数据
     */
    Flow findByNodeIDAndDayAndTimeStamp(Integer nodeID, Integer day, Integer timeStamp);
    
    /**
     * 查询指定节点在指定日期的所有车流量数据，按时间戳排序
     */
    @Query("SELECT f FROM Flow f WHERE f.nodeID = :nodeID AND f.day = :day ORDER BY f.timeStamp")
    List<Flow> findByNodeIDAndDayOrderByTimeStamp(@Param("nodeID") Integer nodeID, @Param("day") Integer day);
    
    /**
     * 查询指定日期的所有车流量数据
     */
    List<Flow> findByDay(Integer day);
    
    /**
     * 查询指定时间段内的车流量数据
     */
    @Query("SELECT f FROM Flow f WHERE f.day BETWEEN :startDay AND :endDay")
    List<Flow> findByDayBetween(@Param("startDay") Integer startDay, @Param("endDay") Integer endDay);
} 