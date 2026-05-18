package com.tcmseek.dto.llm;

import lombok.Data;

/**
 * 聊天消息。
 */
@Data
public class LlmMessage {


    private String role;

    private String content;
}
