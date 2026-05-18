package com.tcmseek.pojo;

import lombok.Data;
import java.io.Serializable;

/**
 * 发送验证码请求DTO
 */
@Data
public class SendCodeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 邮箱地址 */
    private String email;

}



