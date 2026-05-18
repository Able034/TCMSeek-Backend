package com.tcmseek.service;

import com.tcmseek.dto.llm.LlmChatRequest;
import com.tcmseek.dto.llm.LlmChatResponse;

import javax.servlet.http.HttpServletResponse;

public interface LlmService {

    /**
     * 调用上游 LLM 聊天完成端点。
     */
    LlmChatResponse chat(LlmChatRequest request);

    LlmChatResponse aichat(LlmChatRequest request);

    /**
     * 导出共同结果 CSV。
     */
    void exportCommonTargetsCsv(String herbA, String herbB, String cypher, String key, HttpServletResponse response);
}
