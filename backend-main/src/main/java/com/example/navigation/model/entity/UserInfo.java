package com.example.navigation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户个人信息实体类
 * 对应数据库表：user_info
 */
@Entity
@Table(name = "user_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userID;

    @Column(name = "user_name", nullable = false)
    private String userName; // 真实姓名

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String sex;
} 