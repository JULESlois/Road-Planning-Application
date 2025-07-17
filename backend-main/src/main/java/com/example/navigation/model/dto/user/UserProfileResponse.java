package com.example.navigation.model.dto.user;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 用户个人资料响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String name;           // 真实姓名
    private String phoneNumber;    // 电话号码
    private Integer age;           // 年龄
    private String sex;            // 性别
    private boolean isProfileComplete; // 个人资料是否完善
    
    /**
     * 判断个人资料是否完善
     * 所有必要字段都不为空且不为默认值
     */
    public boolean isProfileComplete() {
        return name != null && !name.trim().isEmpty() && !name.equals("未知") &&
               phoneNumber != null && !phoneNumber.trim().isEmpty() &&
               age != null && age > 0 &&
               sex != null && !sex.trim().isEmpty() && !sex.equals("未知");
    }
} 