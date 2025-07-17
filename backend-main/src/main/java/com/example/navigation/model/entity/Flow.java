package com.example.navigation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节点监测的车流量实体类
 * 对应数据库表：flow
 */
@Entity
@Table(name = "flow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_num", nullable = false)
    private Integer day;

    @Column(name = "time_stamp", nullable = false)
    private Integer timeStamp;

    @Column(name = "node_id", nullable = false)
    private Integer nodeID;

    @Column(nullable = false)
    private Integer flow;
} 