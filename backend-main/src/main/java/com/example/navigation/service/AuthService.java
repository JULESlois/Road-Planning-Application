package com.example.navigation.service;

import com.example.navigation.model.dto.auth.AuthResponse;
import com.example.navigation.model.dto.auth.LoginRequest;
import com.example.navigation.model.dto.auth.LoginByUsernameRequest;
import com.example.navigation.model.dto.auth.RegisterRequest;
import com.example.navigation.enums.BusinessErrorCode;
import com.example.navigation.exception.BusinessException;
import com.example.navigation.model.entity.UserLogInfo;
import com.example.navigation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.example.navigation.service.VerificationCodeService;

/**
 * 认证服务类，处理用户注册和登录业务逻辑
 * 已适配新的UserLogInfo用户表结构
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeService verificationCodeService;

    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        logger.info("开始用户注册: email={}, username={}", 
                   request.getEmail(), request.getUsername());
        
        // 检查邮箱是否已存在
        if (userService.existsByEmail(request.getEmail())) {
            logger.warn("邮箱已被注册: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "注册失败：邮箱已被注册");
        }

        // 检查用户名是否已存在
        if (userService.existsByAccountName(request.getUsername())) {
            logger.warn("用户名已被注册: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "注册失败：用户名已被注册");
        }

        // 验证验证码
        boolean isCodeValid = verificationCodeService.validateCode(request.getEmail(), request.getVerificationCode());
        if (!isCodeValid) {
            logger.warn("验证码验证失败: email={}, code={}", request.getEmail(), request.getVerificationCode());
            throw new BusinessException(BusinessErrorCode.VERIFICATION_CODE_INVALID);
        }

        // 注册用户（暂时使用简化方法，保持向后兼容）
        try {
            userService.registerUser(request.getUsername(), request.getPassword(), request.getEmail());
            logger.info("用户注册成功: username={}, email={}", 
                       request.getUsername(), request.getEmail());
        } catch (Exception e) {
            logger.error("用户注册失败: username={}, email={}, error={}", 
                        request.getUsername(), request.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户通过邮箱登录并生成JWT令牌
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("用户邮箱登录请求: email={}", request.getEmail());
        
        try {
            // 根据邮箱查找用户认证信息
            UserLogInfo userLogInfo = userService.findByEmail(request.getEmail());
            
            // 验证密码
            if (!userService.validatePassword(userLogInfo, request.getPassword())) {
                logger.warn("用户邮箱登录失败，密码错误: email={}, userID={}", 
                           request.getEmail(), userLogInfo.getUserID());
                throw new BusinessException(BusinessErrorCode.PASSWORD_INCORRECT);
            }

            // 生成JWT令牌（使用账户名）
            String token = jwtTokenProvider.generateToken(userLogInfo.getAccountName());
            
            // 检查个人资料完善状态
            boolean needCompleteProfile = userService.needCompleteProfile(userLogInfo.getUserID());
            
            logger.info("用户邮箱登录成功: email={}, userID={}, accountName={}, needCompleteProfile={}", 
                       request.getEmail(), userLogInfo.getUserID(), userLogInfo.getAccountName(), needCompleteProfile);
            
            return new AuthResponse(token, userLogInfo.getAccountName(), needCompleteProfile);
            
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            logger.error("用户邮箱登录失败: email={}, error={}", request.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户通过用户名登录并生成JWT令牌
     */
    public AuthResponse loginByUsername(LoginByUsernameRequest request) {
        logger.info("用户名登录请求: username={}", request.getUsername());
        
        try {
            // 根据用户名查找用户认证信息
            UserLogInfo userLogInfo = userService.findByAccountName(request.getUsername());
            
            // 验证密码
            if (!userService.validatePassword(userLogInfo, request.getPassword())) {
                logger.warn("用户名登录失败，密码错误: username={}, userID={}", 
                           request.getUsername(), userLogInfo.getUserID());
                throw new BusinessException(BusinessErrorCode.PASSWORD_INCORRECT);
            }

            // 生成JWT令牌（使用账户名）
            String token = jwtTokenProvider.generateToken(userLogInfo.getAccountName());
            
            // 检查个人资料完善状态
            boolean needCompleteProfile = userService.needCompleteProfile(userLogInfo.getUserID());
            
            logger.info("用户名登录成功: username={}, userID={}, needCompleteProfile={}", 
                       request.getUsername(), userLogInfo.getUserID(), needCompleteProfile);
            
            return new AuthResponse(token, userLogInfo.getAccountName(), needCompleteProfile);
            
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            logger.error("用户名登录失败: username={}, error={}", request.getUsername(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "登录失败：" + e.getMessage());
        }
    }

    /**
     * 根据账户名获取用户认证信息（用于JWT验证）
     */
    public UserLogInfo getUserByAccountName(String accountName) {
        logger.debug("根据账户名获取用户信息: {}", accountName);
        return userService.findByAccountName(accountName);
    }

    /**
     * 检查用户是否存在
     */
    public boolean userExists(String email) {
        return userService.existsByEmail(email);
    }

    /**
     * 重置密码
     */
    public void resetPassword(String email, String verificationCode, String newPassword) {
        logger.info("重置密码请求: email={}", email);
        
        // 验证验证码
        boolean isCodeValid = verificationCodeService.validateCode(email, verificationCode);
        if (!isCodeValid) {
            logger.warn("密码重置失败，验证码无效: email={}", email);
            throw new BusinessException(BusinessErrorCode.VERIFICATION_CODE_INVALID);
        }

        try {
            // 根据邮箱查找用户
            UserLogInfo userLogInfo = userService.findByEmail(email);
            
            // 更新密码
            userService.updatePassword(userLogInfo.getUserID(), newPassword);
            
            logger.info("密码重置成功: email={}, userID={}", email, userLogInfo.getUserID());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("密码重置失败: email={}, error={}", email, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "密码重置失败：" + e.getMessage());
        }
    }
}