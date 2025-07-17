package com.example.navigation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户认证信息实体类
 * 对应数据库表：user_log_info
 */
@Entity
@Table(name = "user_log_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLogInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userID; // 外键，引用UserInfo的主键

    @Column(name = "account_name", nullable = false, unique = true)
    private String accountName; // 账户名

    @Column(nullable = false)
    private String code; // 密码

    @Column(nullable = false, unique = true)
    private String email;
} 