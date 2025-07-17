package com.example.navigation.repository;

import com.example.navigation.model.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    
    /**
     * 根据用户ID查询用户信息
     */
    Optional<UserInfo> findByUserID(Integer userID);
    
    /**
     * 根据真实姓名查询用户
     */
    List<UserInfo> findByUserNameContainingIgnoreCase(String userName);
    
    /**
     * 根据手机号查询用户
     */
    Optional<UserInfo> findByPhone(String phone);
    
    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 根据年龄范围查询用户
     */
    List<UserInfo> findByAgeBetween(Integer minAge, Integer maxAge);
    
    /**
     * 根据性别查询用户
     */
    List<UserInfo> findBySex(String sex);
} 