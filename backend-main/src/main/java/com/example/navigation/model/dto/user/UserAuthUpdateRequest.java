package com.example.navigation.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户认证信息更新请求DTO
 */
@Data
public class UserAuthUpdateRequest {
    @Email(message = "邮箱格式不正确")
    private String email;    // 邮箱(可选)

    @Size(min = 6, message = "密码长度不能少于6位")
    private String password; // 密码(可选)
} 