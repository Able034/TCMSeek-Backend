package com.tcmseek.pojo;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Integer id;

    /** 用户名 */
    private String username;

    /** 账号 */
    private String account;

    /** 邮箱 */
    private String email;

    /** 访问令牌 */
    private String token;

}

