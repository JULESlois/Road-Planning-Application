package com.example.navigation.service;

import com.example.navigation.enums.BusinessErrorCode;
import com.example.navigation.exception.BusinessException;
import com.example.navigation.model.entity.UserInfo;
import com.example.navigation.model.entity.UserLogInfo;
import com.example.navigation.repository.UserInfoRepository;
import com.example.navigation.repository.UserLogInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * 用户服务类，处理用户注册、查询和密码验证等业务逻辑
 * 现在使用分离的UserInfo和UserLogInfo表结构
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserInfoRepository userInfoRepository;
    private final UserLogInfoRepository userLogInfoRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * @param accountName 账户名
     * @param password 原始密码（将被加密存储）
     * @param email 用户邮箱
     * @param userName 真实姓名
     * @param phone 手机号
     * @param age 年龄
     * @param sex 性别
     * @return 注册成功的用户ID
     */
    @Transactional
    public Integer registerUser(String accountName, String password, String email, 
                               String userName, String phone, Integer age, String sex) {
        logger.info("开始注册用户: accountName={}, email={}, userName={}", accountName, email, userName);
        
        // 检查账户名是否已存在
        if (userLogInfoRepository.existsByAccountName(accountName)) {
            logger.warn("账户名已存在: {}", accountName);
            throw new IllegalArgumentException("账户名已存在");
        }

        // 检查邮箱是否已存在
        if (userLogInfoRepository.existsByEmail(email)) {
            logger.warn("邮箱已被注册: {}", email);
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (userInfoRepository.existsByPhone(phone)) {
            logger.warn("手机号已被注册: {}", phone);
            throw new IllegalArgumentException("手机号已被注册");
        }

        try {
            // 1. 创建用户个人信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUserName(userName);
            userInfo.setPhone(phone);
            userInfo.setAge(age);
            userInfo.setSex(sex);
            UserInfo savedUserInfo = userInfoRepository.save(userInfo);
            
            logger.debug("用户个人信息保存成功: userID={}", savedUserInfo.getUserID());

            // 2. 创建用户认证信息
            UserLogInfo userLogInfo = new UserLogInfo();
            userLogInfo.setUserID(savedUserInfo.getUserID());
            userLogInfo.setAccountName(accountName);
            userLogInfo.setCode(passwordEncoder.encode(password)); // 加密密码
            userLogInfo.setEmail(email.toLowerCase()); // 邮箱转小写
            userLogInfoRepository.save(userLogInfo);
            
            logger.info("用户注册成功: userID={}, accountName={}", savedUserInfo.getUserID(), accountName);
            return savedUserInfo.getUserID();
            
        } catch (Exception e) {
            logger.error("用户注册失败: accountName={}, error={}", accountName, e.getMessage());
            throw new RuntimeException("用户注册失败: " + e.getMessage());
        }
    }

    /**
     * 简化的用户注册方法（向后兼容）
     */
    @Transactional
    public UserLogInfo registerUser(String username, String password, String email) {
        logger.info("使用简化方法注册用户: username={}, email={}", username, email);
        
        // 使用默认值创建完整用户信息
        Integer userID = registerUser(username, password, email, 
                                     username, // 使用账户名作为真实姓名
                                     "", // 空手机号
                                     0,  // 默认年龄
                                     "未知" // 默认性别
                                     );
        
        return userLogInfoRepository.findByUserID(userID)
                .orElseThrow(() -> new RuntimeException("注册失败：无法获取用户信息"));
    }

    /**
     * 根据账户名查询用户认证信息
     */
    public UserLogInfo findByAccountName(String accountName) {
        logger.debug("根据账户名查询用户: {}", accountName);
        return userLogInfoRepository.findByAccountName(accountName)
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", accountName);
                    return new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 根据用户名查询用户（向后兼容方法）
     * 实际上调用findByAccountName方法
     */
    public UserLogInfo findByUsername(String username) {
        return findByAccountName(username);
    }

    /**
     * 检查邮箱是否已存在
     */
    public boolean existsByEmail(String email) {
        return userLogInfoRepository.existsByEmail(email);
    }

    /**
     * 根据邮箱查询用户认证信息
     */
    public UserLogInfo findByEmail(String email) {
        logger.debug("根据邮箱查询用户: {}", email);
        return userLogInfoRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("邮箱对应的用户不存在: {}", email);
                    return new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 根据用户ID查询用户个人信息
     */
    public UserInfo findUserInfoByID(Integer userID) {
        logger.debug("根据用户ID查询个人信息: {}", userID);
        return userInfoRepository.findByUserID(userID)
                .orElseThrow(() -> {
                    logger.warn("用户个人信息不存在: userID={}", userID);
                    return new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 根据用户ID查询用户认证信息
     */
    public UserLogInfo findUserLogInfoByID(Integer userID) {
        logger.debug("根据用户ID查询认证信息: {}", userID);
        return userLogInfoRepository.findByUserID(userID)
                .orElseThrow(() -> {
                    logger.warn("用户认证信息不存在: userID={}", userID);
                    return new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 验证用户密码
     */
    public boolean validatePassword(UserLogInfo userLogInfo, String rawPassword) {
        logger.debug("验证用户密码: userID={}", userLogInfo.getUserID());
        boolean isValid = passwordEncoder.matches(rawPassword, userLogInfo.getCode());
        if (!isValid) {
            logger.warn("密码验证失败: userID={}", userLogInfo.getUserID());
        }
        return isValid;
    }

    /**
     * 更新用户个人信息
     */
    @Transactional
    public UserInfo updateUserInfo(Integer userID, String userName, String phone, Integer age, String sex) {
        logger.info("更新用户个人信息: userID={}", userID);
        
        UserInfo userInfo = findUserInfoByID(userID);
        userInfo.setUserName(userName);
        userInfo.setPhone(phone);
        userInfo.setAge(age);
        userInfo.setSex(sex);
        
        UserInfo updatedUserInfo = userInfoRepository.save(userInfo);
        logger.info("用户个人信息更新成功: userID={}", userID);
        return updatedUserInfo;
    }

    /**
     * 更新用户密码
     */
    @Transactional
    public void updatePassword(Integer userID, String newPassword) {
        logger.info("更新用户密码: userID={}", userID);
        
        UserLogInfo userLogInfo = findUserLogInfoByID(userID);
        userLogInfo.setCode(passwordEncoder.encode(newPassword));
        userLogInfoRepository.save(userLogInfo);
        
        logger.info("用户密码更新成功: userID={}", userID);
    }

    /**
     * 根据手机号查询用户个人信息
     */
    public UserInfo findByPhone(String phone) {
        logger.debug("根据手机号查询用户: {}", phone);
        return userInfoRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    logger.warn("手机号对应的用户不存在: {}", phone);
                    return new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 检查账户名是否已存在
     */
    public boolean existsByAccountName(String accountName) {
        return userLogInfoRepository.existsByAccountName(accountName);
    }

    /**
     * 检查手机号是否已存在
     */
    public boolean existsByPhone(String phone) {
        return userInfoRepository.existsByPhone(phone);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Integer userID) {
        logger.info("删除用户: userID={}", userID);
        
        // 先删除认证信息（通过userID查找然后删除）
        Optional<UserLogInfo> userLogInfo = userLogInfoRepository.findByUserID(userID);
        if (userLogInfo.isPresent()) {
            userLogInfoRepository.delete(userLogInfo.get());
            logger.debug("用户认证信息删除成功: userID={}", userID);
        }
        
        // 再删除个人信息
        if (userInfoRepository.existsById(userID)) {
            userInfoRepository.deleteById(userID);
            logger.debug("用户个人信息删除成功: userID={}", userID);
        }
        
        logger.info("用户删除完成: userID={}", userID);
    }

    /**
     * 检查用户是否需要完善个人资料
     */
    public boolean needCompleteProfile(Integer userID) {
        logger.debug("检查用户个人资料完善状态: userID={}", userID);
        
        try {
            UserInfo userInfo = findUserInfoByID(userID);
            
            // 判断个人资料是否完善（所有字段都不为空且不为默认值）
            boolean isComplete = userInfo.getUserName() != null && 
                               !userInfo.getUserName().trim().isEmpty() && 
                               !userInfo.getUserName().equals("未知") &&
                               userInfo.getPhone() != null && 
                               !userInfo.getPhone().trim().isEmpty() &&
                               userInfo.getAge() != null && 
                               userInfo.getAge() > 0 &&
                               userInfo.getSex() != null && 
                               !userInfo.getSex().trim().isEmpty() && 
                               !userInfo.getSex().equals("未知");
            
            logger.debug("用户个人资料状态: userID={}, isComplete={}", userID, isComplete);
            return !isComplete; // 返回是否需要完善（与完善状态相反）
            
        } catch (Exception e) {
            logger.warn("检查用户个人资料状态失败: userID={}, error={}", userID, e.getMessage());
            return true; // 出错时默认需要完善
        }
    }
}