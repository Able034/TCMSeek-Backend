package com.tcmseek.tcmseekagentservice.ai.guardrail.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 领域准入模型的结构化输出。
 */
@Data
@Description("TCMReason 输入领域准入结论")
public class TcmDomainGuardrailDecision {

    @Description("领域结论，只能是 TCM、TCM_RELATED 或 OUT_OF_SCOPE")
    private TcmDomain domain;
}
