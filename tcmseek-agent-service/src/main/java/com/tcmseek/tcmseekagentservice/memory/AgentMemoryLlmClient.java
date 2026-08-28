package com.tcmseek.tcmseekagentservice.memory;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AgentMemoryLlmClient {

    @SystemMessage("你是 TCMSeek 的会话记忆整理助手。请严格按用户要求输出，不要补充解释。")
    @UserMessage("{{prompt}}")
    String complete(@V("prompt") String prompt);
}
