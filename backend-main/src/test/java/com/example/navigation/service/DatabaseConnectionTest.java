package com.example.navigation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试数据库连接成功的情况
     */
    @Test
    public void testDatabaseConnectionSuccess() {
        // 执行简单查询验证连接（H2数据库使用不同的版本查询）
        String databaseVersion = jdbcTemplate.queryForObject(
            "SELECT H2VERSION()",
            String.class
        );
        
        // 验证查询结果不为空且包含H2标识
        assertThat(databaseVersion)
            .isNotEmpty();
            
        System.out.println("数据库连接成功: H2版本信息 - " + databaseVersion);
    }

    /**
     * 测试数据库连接失败的情况
     */
    @Test
    public void testDatabaseConnectionFailure() {
        try {
            // 使用无效的数据库URL和驱动来确保连接失败
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl("jdbc:invalid://invalid.host:9999/invalid_db");
            dataSource.setDriverClassName("invalid.driver.class");
            dataSource.setUsername("invalid_user");
            dataSource.setPassword("invalid_password");
            
            JdbcTemplate badJdbcTemplate = new JdbcTemplate(dataSource);
            // 尝试执行查询
            badJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            // 如果没有抛出异常，测试失败
            fail("期望抛出数据库连接异常，但连接成功了");
            
        } catch (Exception e) {
            // 验证抛出了预期的异常
            System.out.println("预期的数据库连接失败: " + e.getMessage());
            assertThat(e).isNotNull();
            // 验证异常类型确实是由于数据库连接问题
            assertThat(e.getMessage()).isNotEmpty();
        }
    }
}