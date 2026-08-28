package com.tcmseek.tcmseekagentservice.ai;

import com.tcmseek.tcmseekagentservice.ai.model.ClassifyCodeResult;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AiClassifyService {


    /**
     * 获取查询意图
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/uerMessageClassify-promt.txt")
    @UserMessage("{{userMessage}}")
    @Agent("一个可以从用户的初始Query中识别用户意图的专家")
    ClassifyCodeResult classifyQuery(
            @MemoryId String memoryId,
            @V("userMessage")String userMessage);
}
