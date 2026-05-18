package com.tcmseek.webmanage.service.impl;

import java.util.List;
import com.tcmseek.common.utils.DateUtils;
import com.tcmseek.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.UserWebMMapper;
import com.tcmseek.webmanage.domain.UserWeb;
import com.tcmseek.webmanage.service.IUserWebMService;

/**
 * 用户信息Service业务层处理
 * 
 * @author able
 * @date 2025-11-13
 */
@Service
public class UserWebMServiceImpl implements IUserWebMService
{
    @Autowired
    private UserWebMMapper userWebMMapper;

    /**
     * 查询用户信息
     * 
     * @param id 用户信息主键
     * @return 用户信息
     */
    @Override
    public UserWeb selectUserWebById(Long id)
    {
        return userWebMMapper.selectUserWebById(id);
    }

    /**
     * 查询用户信息列表
     * 
     * @param userWeb 用户信息
     * @return 用户信息
     */
    @Override
    public List<UserWeb> selectUserWebList(UserWeb userWeb)
    {
        return userWebMMapper.selectUserWebList(userWeb);
    }

    /**
     * 新增用户信息
     * 
     * @param userWeb 用户信息
     * @return 结果
     */
    @Override
    public int insertUserWeb(UserWeb userWeb)
    {
        userWeb.setCreateTime(DateUtils.getNowDate());
        return userWebMMapper.insertUserWeb(userWeb);
    }

    /**
     * 修改用户信息
     * 
     * @param userWeb 用户信息
     * @return 结果
     */
    @Override
    public int updateUserWeb(UserWeb userWeb)
    {
        //密码需要经过哈希加密
        userWeb.setPassword(SecurityUtils.encryptPassword(userWeb.getPassword()));

        userWeb.setUpdateTime(DateUtils.getNowDate());
        return userWebMMapper.updateUserWeb(userWeb);
    }

    /**
     * 批量删除用户信息
     * 
     * @param ids 需要删除的用户信息主键
     * @return 结果
     */
    @Override
    public int deleteUserWebByIds(Long[] ids)
    {
        return userWebMMapper.deleteUserWebByIds(ids);
    }

    /**
     * 删除用户信息信息
     * 
     * @param id 用户信息主键
     * @return 结果
     */
    @Override
    public int deleteUserWebById(Long id)
    {
        return userWebMMapper.deleteUserWebById(id);
    }
}
