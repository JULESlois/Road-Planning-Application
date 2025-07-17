package com.example.navigation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 道路节点实体类（位于交叉路口）
 * 对应数据库表：nodes
 */
@Entity
@Table(name = "nodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_id")
    private Integer nodeID;

    @Column(nullable = false)
    private String latitude;

    @Column(nullable = false)
    private String longitude;
}