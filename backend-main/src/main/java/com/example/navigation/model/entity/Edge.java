package com.example.navigation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路径实体类
 * 对应数据库表：edges
 */
@Entity
@Table(name = "edges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id1", nullable = false)
    private Integer nodeID1;

    @Column(name = "node_id2", nullable = false)
    private Integer nodeID2;

    @Column(nullable = false)
    private Float distance;
}