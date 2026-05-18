package com.tcmseek.dao;

import com.tcmseek.pojo.UserWeb;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户信息Mapper接口
 */
@Mapper
public interface UserWebMapper {

    /**
     * 根据账号查询用户
     * @param account 账号
     * @return 用户信息
     */
    UserWeb selectByAccount(@Param("account") String account);

    /**
     * 根据账号和密码查询用户
     * @param account 账号
     * @param password 密码
     * @return 用户信息
     */
    UserWeb selectByAccountAndPassword(@Param("account") String account, @Param("password") String password);

    /**
     * 新增用户
     * @param userWeb 用户信息
     * @return 影响行数
     */
    int insert(UserWeb userWeb);
}



