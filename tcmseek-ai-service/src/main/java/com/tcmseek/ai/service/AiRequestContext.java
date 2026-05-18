package com.tcmseek.ai.service;

public class AiRequestContext {

    private final String requestId;

    private final String userId;

    private final String username;

    private final String account;

    public AiRequestContext(String requestId, String userId, String username, String account) {
        this.requestId = requestId;
        this.userId = userId;
        this.username = username;
        this.account = account;
    }

    public static AiRequestContext empty() {
        return new AiRequestContext(null, null, null, null);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getAccount() {
        return account;
    }
}
