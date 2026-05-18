package com.tcmseek.ai.dto;

import java.time.OffsetDateTime;

public class ErrorResponse {

    private String timestamp;

    private int status;

    private String code;

    private String message;

    private String path;

    private String requestId;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String code, String message, String path, String requestId) {
        this.timestamp = OffsetDateTime.now().toString();
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
