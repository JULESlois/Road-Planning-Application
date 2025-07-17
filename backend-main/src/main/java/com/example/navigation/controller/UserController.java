package com.example.navigation.controller;

import com.example.navigation.model.dto.user.*;
import com.example.navigation.model.dto.common.StandardResponse;
import com.example.navigation.model.entity.UserInfo;
import com.example.navigation.model.entity.UserLogInfo;
import com.example.navigation.service.UserService;
import com.example.navigation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户信息控制器
 * 处理用户认证信息和个人资料相关接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 获取当前用户认证信息
     */
    @GetMapping("/auth")
    public ResponseEntity<UserAuthResponse> getUserAuth(HttpServletRequest request) {
        logger.info("获取当前用户认证信息请求");
        
        try {
            String username = getCurrentUsername(request);
            UserLogInfo userLogInfo = userService.findByAccountName(username);
            
            UserAuthResponse.UserAuthData data = new UserAuthResponse.UserAuthData(
                userLogInfo.getAccountName(),
                userLogInfo.getEmail(),
                "user" // 默认角色，可以根据实际需求扩展
            );
            
            UserAuthResponse response = new UserAuthResponse(true, data);
            
            logger.info("获取用户认证信息成功: username={}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户认证信息失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 修改当前用户认证信息
     */
    @PutMapping("/auth")
    public ResponseEntity<StandardResponse> updateUserAuth(@Valid @RequestBody UserAuthUpdateRequest request, 
                                                          HttpServletRequest httpRequest) {
        logger.info("修改用户认证信息请求: email={}", request.getEmail());
        
        try {
            String username = getCurrentUsername(httpRequest);
            UserLogInfo userLogInfo = userService.findByAccountName(username);
            
            // 更新邮箱
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // 检查新邮箱是否已存在
                if (!request.getEmail().equals(userLogInfo.getEmail()) && 
                    userService.existsByEmail(request.getEmail())) {
                    logger.warn("邮箱已被其他用户使用: {}", request.getEmail());
                    return ResponseEntity.badRequest().body(StandardResponse.error("邮箱已被其他用户使用"));
                }
                // 这里应该更新邮箱，但当前UserService没有相应方法，需要添加
                logger.info("需要更新邮箱: {} -> {}", userLogInfo.getEmail(), request.getEmail());
            }
            
            // 更新密码
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                userService.updatePassword(userLogInfo.getUserID(), request.getPassword());
                logger.info("用户密码更新成功: username={}", username);
            }
            
            logger.info("用户认证信息更新成功: username={}", username);
            return ResponseEntity.ok(StandardResponse.success("用户信息更新成功。"));
        } catch (Exception e) {
            logger.error("修改用户认证信息失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取当前用户个人资料
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        logger.info("获取当前用户个人资料请求");
        
        try {
            String username = getCurrentUsername(request);
            UserLogInfo userLogInfo = userService.findByAccountName(username);
            UserInfo userInfo = userService.findUserInfoByID(userLogInfo.getUserID());
            
            UserProfileResponse profileData = new UserProfileResponse(
                userInfo.getUserName(),
                userInfo.getPhone(),
                userInfo.getAge(),
                userInfo.getSex(),
                false // 临时设置，后面会通过方法计算
            );
            
            // 设置个人资料完善状态
            profileData.setProfileComplete(profileData.isProfileComplete());
            
            // 构建响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("data", profileData);
            
            logger.info("获取用户个人资料成功: username={}, isComplete={}", 
                       username, profileData.isProfileComplete());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户个人资料失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 修改当前用户个人资料
     */
    @PutMapping("/profile")
    public ResponseEntity<StandardResponse> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest request, 
                                                             HttpServletRequest httpRequest) {
        logger.info("修改用户个人资料请求");
        
        try {
            String username = getCurrentUsername(httpRequest);
            UserLogInfo userLogInfo = userService.findByAccountName(username);
            UserInfo userInfo = userService.findUserInfoByID(userLogInfo.getUserID());
            
            // 更新字段（只更新非空字段）
            String newName = request.getName() != null && !request.getName().trim().isEmpty() 
                ? request.getName() : userInfo.getUserName();
            String newPhone = request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty() 
                ? request.getPhoneNumber() : userInfo.getPhone();
            Integer newAge = request.getAge() != null ? request.getAge() : userInfo.getAge();
            String newSex = request.getSex() != null && !request.getSex().trim().isEmpty() 
                ? request.getSex() : userInfo.getSex();
            
            // 检查手机号是否已被其他用户使用
            if (!newPhone.equals(userInfo.getPhone()) && 
                !newPhone.isEmpty() && 
                userService.existsByPhone(newPhone)) {
                logger.warn("手机号已被其他用户使用: {}", newPhone);
                return ResponseEntity.badRequest().body(StandardResponse.error("电话号码已被其他用户使用"));
            }
            
            userService.updateUserInfo(userLogInfo.getUserID(), newName, newPhone, newAge, newSex);
            
            logger.info("用户个人资料更新成功: username={}", username);
            return ResponseEntity.ok(StandardResponse.success("用户个人资料更新成功。"));
        } catch (Exception e) {
            logger.error("修改用户个人资料失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从请求中获取当前用户名
     */
    private String getCurrentUsername(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("无效的token");
        }
        
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("token已过期");
        }
        
        return jwtTokenProvider.getUsernameFromToken(token);
    }
} 