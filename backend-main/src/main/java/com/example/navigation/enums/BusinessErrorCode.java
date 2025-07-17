package com.example.navigation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务错误码枚举
 */
@Getter
@AllArgsConstructor
public enum BusinessErrorCode {
    // 用户相关错误
    USER_NOT_FOUND(10001, "用户不存在", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(10002, "用户名已存在", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_REGISTERED(10003, "邮箱已被注册", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(10004, "密码错误", HttpStatus.UNAUTHORIZED),
    
    // 验证码相关错误
    VERIFICATION_CODE_INVALID(20001, "验证码无效或已过期", HttpStatus.BAD_REQUEST),
    
    // 系统错误
    SYSTEM_ERROR(99999, "系统内部错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;           // 业务错误码
    private final String message;     // 错误消息
    private final HttpStatus status;  // HTTP状态码
}