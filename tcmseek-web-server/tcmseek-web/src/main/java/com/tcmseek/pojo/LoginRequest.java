package com.tcmseek.pojo;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 账号 */
    private String account;

    /** 密码 */
    private String password;

}



