package com.example.navigation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务类，负责生成、存储和验证邮箱验证码
 */
@Service
public class VerificationCodeService {

    // 使用线程安全的Map存储验证码信息，key为邮箱，value为验证码和过期时间
    private final Map<String, CodeInfo> codeCache = new ConcurrentHashMap<>();

    @Value("${verification.code.expire-time}")
    private int expireTime; // 验证码过期时间(分钟)

    @Value("${verification.code.length}")
    private int codeLength; // 验证码长度

    /**
     * 生成验证码并存储到缓存
     * @param email 用户邮箱
     * @return 生成的验证码
     */
    public String generateCode(String email) {
        // 生成指定长度的随机数字验证码
        String code = generateRandomCode(codeLength);
        
        // 计算过期时间戳
        long expireAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expireTime);
        codeCache.put(email, new CodeInfo(code, expireAt));
        
        return code;
    }

    /**
     * 验证验证码是否有效
     * @param email 用户邮箱
     * @param code 待验证的验证码
     * @return 验证结果(true:有效, false:无效或过期)
     */
    public boolean validateCode(String email, String code) {
        CodeInfo codeInfo = codeCache.get(email);
        if (codeInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.expireAt) {
            codeCache.remove(email);
            return false;
        }
        
        // 验证验证码是否匹配
        boolean isValid = codeInfo.code.equals(code);
        if (isValid) {
            codeCache.remove(email); // 验证成功后移除验证码，防止重复使用
        }
        return isValid;
    }

    /**
     * 生成随机数字验证码
     * @param length 验证码长度
     * @return 随机数字验证码
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            codeBuilder.append(random.nextInt(10));
        }
        return codeBuilder.toString();
    }

    /**
     * 内部类：存储验证码和过期时间
     */
    private static class CodeInfo {
        String code;       // 验证码
        long expireAt;     // 过期时间戳(毫秒)

        CodeInfo(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}