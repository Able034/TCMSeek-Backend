package com.tcmseek.webmanage.domain;

import com.tcmseek.common.annotation.Sensitive;
import com.tcmseek.common.enums.DesensitizedType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 用户信息对象 user_web
 * 
 * @author able
 * @date 2025-11-13
 */
public class UserWeb extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增主键ID */
    private Long id;

    /** 用户名 */
    @Excel(name = "用户名")
    private String username;

    /** 账号 */
    @Excel(name = "账号")
    private String account;

    /** 密码 */
    @Excel(name = "密码")
    @Sensitive(desensitizedType = DesensitizedType.PASSWORD)
    private String password;

    /** 邮箱 */
    @Excel(name = "邮箱")
    @Sensitive(desensitizedType = DesensitizedType.EMAIL)
    private String email;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setUsername(String username) 
    {
        this.username = username;
    }

    public String getUsername() 
    {
        return username;
    }

    public void setAccount(String account) 
    {
        this.account = account;
    }

    public String getAccount() 
    {
        return account;
    }

    public void setPassword(String password) 
    {
        this.password = password;
    }

    public String getPassword() 
    {
        return password;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("username", getUsername())
            .append("account", getAccount())
            .append("password", getPassword())
            .append("email", getEmail())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
