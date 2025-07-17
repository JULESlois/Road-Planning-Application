package com.example.navigation.repository;

import com.example.navigation.model.entity.UserLogInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserLogInfoRepository extends JpaRepository<UserLogInfo, Long> {
    
    /**
     * 根据账户名查询用户登录信息
     */
    Optional<UserLogInfo> findByAccountName(String accountName);
    
    /**
     * 根据邮箱查询用户登录信息
     */
    Optional<UserLogInfo> findByEmail(String email);
    
    /**
     * 根据用户ID查询用户登录信息
     */
    Optional<UserLogInfo> findByUserID(Integer userID);
    
    /**
     * 检查账户名是否已存在
     */
    boolean existsByAccountName(String accountName);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查用户ID是否已存在
     */
    boolean existsByUserID(Integer userID);
} 