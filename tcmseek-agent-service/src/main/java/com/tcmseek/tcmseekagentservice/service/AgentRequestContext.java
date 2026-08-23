package com.tcmseek.tcmseekagentservice.service;

public class AgentRequestContext {

    private final String requestId;

    private final String userId;

    private final String username;

    private final String account;

    public AgentRequestContext(String requestId, String userId, String username, String account) {
        this.requestId = requestId;
        this.userId = userId;
        this.username = username;
        this.account = account;
    }

    public static AgentRequestContext empty() {
        return new AgentRequestContext(null, null, null, null);
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
