package com.example.navigation.config;

import com.example.navigation.model.entity.UserInfo;
import com.example.navigation.model.entity.UserLogInfo;
import com.example.navigation.repository.UserInfoRepository;
import com.example.navigation.repository.UserLogInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 启动时创建默认测试用户
 * 现在使用新的分离用户表结构（UserInfo + UserLogInfo）
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserInfoRepository userInfoRepository;
    private final UserLogInfoRepository userLogInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserInfoRepository userInfoRepository, 
                          UserLogInfoRepository userLogInfoRepository,
                          PasswordEncoder passwordEncoder) {
        this.userInfoRepository = userInfoRepository;
        this.userLogInfoRepository = userLogInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化默认数据...");
        
        // 添加默认测试用户
        if (userLogInfoRepository.findByAccountName("testuser").isEmpty()) {
            try {
                // 1. 创建用户个人信息
                UserInfo testUserInfo = new UserInfo();
                testUserInfo.setUserName("测试用户");
                testUserInfo.setPhone("13800138000");
                testUserInfo.setAge(25);
                testUserInfo.setSex("男");
                UserInfo savedUserInfo = userInfoRepository.save(testUserInfo);
                
                logger.debug("创建测试用户个人信息成功: userID={}", savedUserInfo.getUserID());

                // 2. 创建用户认证信息
                UserLogInfo testUserLogInfo = new UserLogInfo();
                testUserLogInfo.setUserID(savedUserInfo.getUserID());
                testUserLogInfo.setAccountName("testuser");
                testUserLogInfo.setCode(passwordEncoder.encode("testpassword"));
                testUserLogInfo.setEmail("test@example.com");
                userLogInfoRepository.save(testUserLogInfo);

                logger.info("默认测试用户创建成功: accountName=testuser, password=testpassword, email=test@example.com");
                
            } catch (Exception e) {
                logger.error("创建默认测试用户失败: {}", e.getMessage(), e);
            }
        } else {
            logger.info("默认测试用户已存在，跳过创建");
        }

        // 添加管理员用户
        if (userLogInfoRepository.findByAccountName("admin").isEmpty()) {
            try {
                // 1. 创建管理员个人信息
                UserInfo adminUserInfo = new UserInfo();
                adminUserInfo.setUserName("系统管理员");
                adminUserInfo.setPhone("13900139000");
                adminUserInfo.setAge(30);
                adminUserInfo.setSex("其他");
                UserInfo savedAdminInfo = userInfoRepository.save(adminUserInfo);
                
                logger.debug("创建管理员个人信息成功: userID={}", savedAdminInfo.getUserID());

                // 2. 创建管理员认证信息
                UserLogInfo adminLogInfo = new UserLogInfo();
                adminLogInfo.setUserID(savedAdminInfo.getUserID());
                adminLogInfo.setAccountName("admin");
                adminLogInfo.setCode(passwordEncoder.encode("admin123"));
                adminLogInfo.setEmail("admin@example.com");
                userLogInfoRepository.save(adminLogInfo);

                logger.info("默认管理员用户创建成功: accountName=admin, password=admin123, email=admin@example.com");
                
            } catch (Exception e) {
                logger.error("创建默认管理员用户失败: {}", e.getMessage(), e);
            }
        } else {
            logger.info("默认管理员用户已存在，跳过创建");
        }

        logger.info("数据初始化完成");
    }
}