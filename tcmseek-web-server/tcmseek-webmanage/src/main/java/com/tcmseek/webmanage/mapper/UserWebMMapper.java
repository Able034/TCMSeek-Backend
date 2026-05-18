package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.UserWeb;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息Mapper接口
 * 
 * @author able
 * @date 2025-11-13
 */
@Mapper
public interface UserWebMMapper
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
     * 删除用户信息
     * 
     * @param id 用户信息主键
     * @return 结果
     */
    public int deleteUserWebById(Long id);

    /**
     * 批量删除用户信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserWebByIds(Long[] ids);
}
