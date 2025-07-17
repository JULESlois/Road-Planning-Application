package com.example.navigation.exception;

import com.example.navigation.enums.BusinessErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int errorCode;
    private final String errorMessage;
    private final int httpStatusCode;

    public BusinessException(BusinessErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getMessage();
        this.httpStatusCode = errorCode.getStatus().value();
    }
}