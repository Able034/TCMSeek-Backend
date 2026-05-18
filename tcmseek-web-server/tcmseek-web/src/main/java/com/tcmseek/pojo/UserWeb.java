package com.tcmseek.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息表实体类
 */
@Data
public class UserWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自增主键ID */
    private Integer id;

    /** 用户名 */
    private String username;

    /** 账号 */
    private String account;

    /** 密码 */
    private String password;

    /** 邮箱 */
    private String email;

    /** 创建时间 */
    @JSONField(serialize = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @JSONField(serialize = false)
    private LocalDateTime updateTime;

    /** 邮箱验证码（不存数据库，仅用于注册时验证） */
    @JSONField(serialize = false)
    private String code;

}

