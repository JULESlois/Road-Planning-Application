package com.example.navigation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通用错误响应DTO
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;          // 错误消息
    private String details;          // 详细错误信息
    private int statusCode;          // HTTP状态码
    private int errorCode;           // 业务错误码
    private LocalDateTime timestamp; // 错误发生时间

    // 新增构造方法
    public ErrorResponse(String message, String details, int statusCode, int errorCode) {
        this(message, details, statusCode, errorCode, LocalDateTime.now());
    }
}