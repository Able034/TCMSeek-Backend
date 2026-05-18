package com.tcmseek.gateway.auth;

public class AuthUser {

    private final String userId;

    private final String username;

    private final String account;

    private final String email;

    public AuthUser(String userId, String username, String account, String email) {
        this.userId = userId;
        this.username = username;
        this.account = account;
        this.email = email;
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

    public String getEmail() {
        return email;
    }
}
