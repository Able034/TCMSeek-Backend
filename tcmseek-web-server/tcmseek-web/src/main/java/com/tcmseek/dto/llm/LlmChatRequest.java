package com.tcmseek.dto.llm;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * 来自前端的大模型对话请求载荷，只包含消息列表。
 */
@Data
public class LlmChatRequest {

    /**
     * 会话标识，用于后端维护多轮上下文。
     */
    private String sessionId;

    /**
     * 聊天上下文，将直接透传给上游大模型。
     */
    @NotEmpty(message = "messages 不能为空")
    private List<LlmMessage> messages = new ArrayList<>();
}
