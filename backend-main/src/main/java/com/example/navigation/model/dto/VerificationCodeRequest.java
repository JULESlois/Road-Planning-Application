package com.example.navigation.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证码请求DTO，用于接收前端发送的邮箱参数
 */
@Data
public class VerificationCodeRequest {

    /**
     * 用户邮箱，用于接收验证码
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}