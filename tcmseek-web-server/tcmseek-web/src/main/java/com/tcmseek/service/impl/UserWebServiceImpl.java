package com.tcmseek.service.impl;

import com.tcmseek.common.core.domain.entity.SysUser;
import com.tcmseek.common.core.domain.model.LoginUser;
import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.common.utils.SecurityUtils;
import com.tcmseek.dao.UserWebMapper;
import com.tcmseek.framework.web.service.TokenService;
import com.tcmseek.pojo.LoginRequest;
import com.tcmseek.pojo.LoginResponse;
import com.tcmseek.pojo.UserWeb;
import com.tcmseek.service.UserWebService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户信息Service实现类
 */
@Service
public class UserWebServiceImpl implements UserWebService {

    @Autowired
    private UserWebMapper userWebMapper;

    @Autowired
    private TokenService tokenService;

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 先根据账号查询用户
        UserWeb userWeb = userWebMapper.selectByAccount(loginRequest.getAccount());

        // 用户不存在
        if (userWeb == null) {
            return null;
        }

        // 验证密码（使用BCrypt的matches方法）
        if (!SecurityUtils.matchesPassword(loginRequest.getPassword(), userWeb.getPassword())) {
            return null;
        }

        // 封装返回结果
        LoginResponse response = new LoginResponse();
        BeanUtils.copyProperties(userWeb, response);

        // 生成token
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userWeb.getId().longValue());
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userWeb.getId().longValue());
        sysUser.setUserName(userWeb.getUsername());
        sysUser.setNickName(userWeb.getAccount());
        sysUser.setEmail(userWeb.getEmail());
        loginUser.setUser(sysUser);
        
        String token = tokenService.createToken(loginUser);
        response.setToken(token);

        return response;
    }

    /**
     * 用户注册
     * @param userWeb 用户信息
     * @return 是否注册成功
     */
    @Override
    public boolean regist(UserWeb userWeb) {
        try {
            //写入创建时间和更新时间
            userWeb.setCreateTime(LocalDateTime.now());
            userWeb.setUpdateTime(LocalDateTime.now());
            return userWebMapper.insert(userWeb) > 0;
        } catch (DuplicateKeyException e) {
            // 处理唯一键冲突异常
            String errorMsg = e.getMessage();
            if (errorMsg.contains("uniq_account")) {
                throw new ServiceException("账号已存在，请更换账号");
            } else if (errorMsg.contains("uniq_email")) {
                throw new ServiceException("邮箱已被注册，请更换邮箱");
            } else {
                throw new ServiceException("注册失败，数据重复");
            }
        } catch (Exception e) {
            throw new ServiceException("注册失败：" + e.getMessage());
        }
    }

}
