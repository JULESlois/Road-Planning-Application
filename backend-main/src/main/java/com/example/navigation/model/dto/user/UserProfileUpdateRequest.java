package com.example.navigation.model.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户个人资料更新请求DTO
 */
@Data
public class UserProfileUpdateRequest {
    @Size(min = 2, max = 10, message = "真实姓名长度必须在2到10个字符之间")
    private String name;         // 真实姓名(可选)
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;  // 手机号(可选)
    
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄不能超过150")
    private Integer age;         // 年龄(可选)
    
    @Pattern(regexp = "^(男|女|其他)$", message = "性别只能选择：男、女、其他")
    private String sex;          // 性别(可选)
} 