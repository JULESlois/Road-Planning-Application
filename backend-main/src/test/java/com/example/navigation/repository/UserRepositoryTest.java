package com.example.navigation.repository;

import com.example.navigation.model.entity.UserInfo;
import com.example.navigation.model.entity.UserLogInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

/**
 * 用户Repository的集成测试类，验证用户数据的插入和查询操作
 * 现在使用新的分离用户表结构（UserInfo + UserLogInfo）
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Autowired
    private UserLogInfoRepository userLogInfoRepository;

    /**
     * 测试插入新用户并查询的完整流程
     */
    @Test
    void testInsertAndQueryUser() {
        // 1. 创建测试用户个人信息
        UserInfo testUserInfo = new UserInfo();
        testUserInfo.setUserName("张三");
        testUserInfo.setPhone("13800138000");
        testUserInfo.setAge(25);
        testUserInfo.setSex("男");
        
        // 2. 保存用户个人信息到数据库
        UserInfo savedUserInfo = userInfoRepository.save(testUserInfo);

        // 3. 验证个人信息保存结果
        assertThat(savedUserInfo.getUserID()).isNotNull(); // 确保ID已生成
        assertThat(savedUserInfo.getUserName()).isEqualTo("张三");
        assertThat(savedUserInfo.getPhone()).isEqualTo("13800138000");

        // 4. 创建用户认证信息
        UserLogInfo testUserLogInfo = new UserLogInfo();
        testUserLogInfo.setUserID(savedUserInfo.getUserID());
        testUserLogInfo.setAccountName("test_query_user");
        testUserLogInfo.setCode("encryptedPassword123"); // 实际应用中会通过PasswordEncoder加密
        testUserLogInfo.setEmail("test.query@example.com");
        
        // 5. 保存用户认证信息到数据库
        UserLogInfo savedUserLogInfo = userLogInfoRepository.save(testUserLogInfo);

        // 6. 验证认证信息保存结果
        assertThat(savedUserLogInfo.getUserID()).isEqualTo(savedUserInfo.getUserID());
        assertThat(savedUserLogInfo.getAccountName()).isEqualTo("test_query_user");

        // 7. 通过账户名查询用户认证信息
        Optional<UserLogInfo> foundUserLogInfo = userLogInfoRepository.findByAccountName("test_query_user");

        // 8. 验证查询结果
        assertThat(foundUserLogInfo).isPresent();
        assertThat(foundUserLogInfo.get().getEmail()).isEqualTo("test.query@example.com");
        assertThat(foundUserLogInfo.get().getAccountName()).isEqualTo("test_query_user");

        // 9. 通过用户ID查询个人信息
        Optional<UserInfo> foundUserInfo = userInfoRepository.findByUserID(savedUserInfo.getUserID());
        assertThat(foundUserInfo).isPresent();
        assertThat(foundUserInfo.get().getUserName()).isEqualTo("张三");
    }

    /**
     * 测试查询不存在的用户时返回空结果
     */
    @Test
    void testQueryNonExistentUser() {
        Optional<UserLogInfo> nonExistentUser = userLogInfoRepository.findByAccountName("non_existent_user");
        assertThat(nonExistentUser).isNotPresent();
        
        Optional<UserInfo> nonExistentUserInfo = userInfoRepository.findByUserID(999);
        assertThat(nonExistentUserInfo).isNotPresent();
    }

    /**
     * 测试根据邮箱查询用户
     */
    @Test
    void testQueryUserByEmail() {
        // 1. 创建测试用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName("李四");
        userInfo.setPhone("13900139000");
        userInfo.setAge(30);
        userInfo.setSex("女");
        UserInfo savedUserInfo = userInfoRepository.save(userInfo);

        // 2. 创建认证信息
        UserLogInfo userLogInfo = new UserLogInfo();
        userLogInfo.setUserID(savedUserInfo.getUserID());
        userLogInfo.setAccountName("test_email_user");
        userLogInfo.setCode("password123");
        userLogInfo.setEmail("test.email@example.com");
        userLogInfoRepository.save(userLogInfo);

        // 3. 通过邮箱查询
        Optional<UserLogInfo> foundUser = userLogInfoRepository.findByEmail("test.email@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getAccountName()).isEqualTo("test_email_user");
    }

    /**
     * 测试检查邮箱是否已存在
     */
    @Test
    void testEmailExists() {
        // 1. 创建测试用户
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName("王五");
        userInfo.setPhone("13700137000");
        userInfo.setAge(28);
        userInfo.setSex("男");
        UserInfo savedUserInfo = userInfoRepository.save(userInfo);

        UserLogInfo userLogInfo = new UserLogInfo();
        userLogInfo.setUserID(savedUserInfo.getUserID());
        userLogInfo.setAccountName("test_exists_user");
        userLogInfo.setCode("password123");
        userLogInfo.setEmail("exists@example.com");
        userLogInfoRepository.save(userLogInfo);

        // 2. 检查邮箱是否存在
        boolean emailExists = userLogInfoRepository.existsByEmail("exists@example.com");
        assertThat(emailExists).isTrue();

        boolean emailNotExists = userLogInfoRepository.existsByEmail("notexists@example.com");
        assertThat(emailNotExists).isFalse();
    }
}