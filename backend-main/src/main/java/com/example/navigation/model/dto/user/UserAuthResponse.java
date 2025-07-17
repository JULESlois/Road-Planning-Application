package com.example.navigation.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户认证信息响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthResponse {
    private boolean success;
    private UserAuthData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAuthData {
        private String username;    // 用户名
        private String email;       // 邮箱
        private String role;        // 角色
    }
} 