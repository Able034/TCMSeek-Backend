package com.tcmseek.service;

import com.tcmseek.pojo.LoginRequest;
import com.tcmseek.pojo.LoginResponse;
import com.tcmseek.pojo.UserWeb;

/**
 * 用户信息Service接口
 */
public interface UserWebService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     * @param userWeb 注册信息
     * @return 是否注册成功
     */
    boolean regist(UserWeb userWeb);
}



