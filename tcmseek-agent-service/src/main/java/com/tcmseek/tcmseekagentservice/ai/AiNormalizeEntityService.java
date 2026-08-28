package com.tcmseek.tcmseekagentservice.ai;

import com.tcmseek.tcmseekagentservice.ai.model.NormalizeEntityResult;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AiNormalizeEntityService {
    /**
     * 获取查询意图后分别出Query里面的实体
     */
    @SystemMessage(fromResource = "prompt/uerMessageNormalizeEntity-promt.txt")
    @UserMessage("{{userMessage}}")
    @Agent("从用户 Query 中识别中医知识图谱实体的专家")
    NormalizeEntityResult normalizeEntity(
            @MemoryId String memoryId,
            @V("userMessage")String userMessage);


}
