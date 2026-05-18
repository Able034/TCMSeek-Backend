package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.UserWeb;

/**
 * 用户信息Service接口
 * 
 * @author able
 * @date 2025-11-13
 */
public interface IUserWebMService
{
    /**
     * 查询用户信息
     * 
     * @param id 用户信息主键
     * @return 用户信息
     */
    public UserWeb selectUserWebById(Long id);

    /**
     * 查询用户信息列表
     * 
     * @param userWeb 用户信息
     * @return 用户信息集合
     */
    public List<UserWeb> selectUserWebList(UserWeb userWeb);

    /**
     * 新增用户信息
     * 
     * @param userWeb 用户信息
     * @return 结果
     */
    public int insertUserWeb(UserWeb userWeb);

    /**
     * 修改用户信息
     * 
     * @param userWeb 用户信息
     * @return 结果
     */
    public int updateUserWeb(UserWeb userWeb);

    /**
     * 批量删除用户信息
     * 
     * @param ids 需要删除的用户信息主键集合
     * @return 结果
     */
    public int deleteUserWebByIds(Long[] ids);

    /**
     * 删除用户信息信息
     * 
     * @param id 用户信息主键
     * @return 结果
     */
    public int deleteUserWebById(Long id);
}
