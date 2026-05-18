package com.tcmseek.ai.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends RuntimeException {

    private final HttpStatus status;

    private final String code;

    private final String userMessage;

    public AiServiceException(HttpStatus status, String code, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.status = status;
        this.code = code;
        this.userMessage = userMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
