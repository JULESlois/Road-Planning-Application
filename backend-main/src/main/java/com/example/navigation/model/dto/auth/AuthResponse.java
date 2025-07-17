package com.example.navigation.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private boolean needCompleteProfile; // 是否需要完善个人资料
    
    // 保持向后兼容的构造函数
    public AuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
        this.needCompleteProfile = false;
    }
}