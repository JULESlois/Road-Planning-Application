package com.example.navigation.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标准响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse {
    private boolean success;
    private String message;

    public static StandardResponse success(String message) {
        return new StandardResponse(true, message);
    }

    public static StandardResponse error(String message) {
        return new StandardResponse(false, message);
    }
} 