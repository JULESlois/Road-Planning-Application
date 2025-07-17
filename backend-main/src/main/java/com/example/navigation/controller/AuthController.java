package com.example.navigation.controller;

import com.example.navigation.model.dto.auth.AuthResponse;
import com.example.navigation.model.dto.auth.LoginRequest;
import com.example.navigation.model.dto.auth.LoginByUsernameRequest;
import com.example.navigation.model.dto.auth.RegisterRequest;
import com.example.navigation.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.navigation.model.dto.VerificationCodeRequest;
import com.example.navigation.service.EmailService;
import com.example.navigation.service.VerificationCodeService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 处理用户注册、登录和验证码相关接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("收到用户注册请求: email={}, username={}", request.getEmail(), request.getUsername());
        
        try {
            authService.register(request);
            logger.info("用户注册成功: email={}, username={}", request.getEmail(), request.getUsername());
            return ResponseEntity.ok("注册成功");
        } catch (Exception e) {
            logger.error("用户注册失败: email={}, username={}, error={}", 
                        request.getEmail(), request.getUsername(), e.getMessage());
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }

    /**
     * 发送邮箱验证码
     * @param request 包含用户邮箱的请求DTO
     * @return 发送结果
     */
    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@Valid @RequestBody VerificationCodeRequest request) {
        logger.info("收到发送验证码请求: email={}", request.getEmail());
        
        try {
            // 生成验证码
            String code = verificationCodeService.generateCode(request.getEmail());
            logger.debug("为邮箱 {} 生成验证码", request.getEmail());
            
            // 发送验证码邮件
            emailService.sendVerificationCodeEmail(request.getEmail(), code);
            logger.info("验证码邮件发送成功: email={}", request.getEmail());
            
            return ResponseEntity.ok("验证码已发送至您的邮箱，请注意查收");
        } catch (MessagingException e) {
            logger.error("验证码邮件发送失败: email={}, error={}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(500).body("邮件发送失败，请稍后重试");
        } catch (Exception e) {
            logger.error("发送验证码过程中发生异常: email={}, error={}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(500).body("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 用户通过邮箱登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("收到用户邮箱登录请求: email={}", request.getEmail());
        
        try {
            AuthResponse response = authService.login(request);
            logger.info("用户邮箱登录成功: email={}, username={}", request.getEmail(), response.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("用户邮箱登录失败: email={}, error={}", request.getEmail(), e.getMessage());
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }

    /**
     * 用户通过用户名登录接口
     */
    @PostMapping("/login_by_username")
    public ResponseEntity<AuthResponse> loginByUsername(@Valid @RequestBody LoginByUsernameRequest request) {
        logger.info("收到用户名登录请求: username={}", request.getUsername());
        
        try {
            AuthResponse response = authService.loginByUsername(request);
            logger.info("用户名登录成功: username={}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("用户名登录失败: username={}, error={}", request.getUsername(), e.getMessage());
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        logger.info("收到用户登出请求");
        
        try {
            // 在无状态JWT系统中，登出通常由前端处理（删除token）
            // 这里可以添加token黑名单等逻辑
            logger.info("用户登出成功");
            return ResponseEntity.ok("登出成功");
        } catch (Exception e) {
            logger.error("用户登出过程中发生异常: error={}", e.getMessage());
            return ResponseEntity.status(500).body("服务器内部错误");
        }
    }
}