package com.tcmseek.tcmseekagentservice.ai.guardrail;

import com.tcmseek.tcmseekagentservice.ai.guardrail.model.TcmDomain;
import com.tcmseek.tcmseekagentservice.ai.guardrail.model.TcmDomainGuardrailDecision;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

/**
 * 由独立模型判定输入是否属于中医药领域的准入护轨。
 *
 * <p>该类不包含关键词或题目类型白名单；具体边界由
 * {@code prompt/tcm-domain-input-guardrail.txt} 中的语义判定提示词定义。</p>
 */
public class TcmDomainInputGuardrail implements InputGuardrail {

    private static final String UNAVAILABLE_MESSAGE = "暂时无法完成问题领域判断，请稍后重试。";

    private static final String OUT_OF_SCOPE_MESSAGE = "该问题不属于中医药领域。请提出与中医药相关的问题，我会尽力协助。";

    private final TcmDomainGuardrailService guardrailService;

    public TcmDomainInputGuardrail(TcmDomainGuardrailService guardrailService) {
        if (guardrailService == null) {
            throw new IllegalArgumentException("guardrailService cannot be null");
        }
        this.guardrailService = guardrailService;
    }

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage == null ? null : userMessage.singleText();
        if (input == null || input.isBlank()) {
            return fatal("输入内容不能为空。");
        }

        TcmDomainGuardrailDecision decision;
        try {
            decision = guardrailService.assess(input);
        } catch (RuntimeException exception) {
            return fatal(UNAVAILABLE_MESSAGE);
        }

        if (decision == null || decision.getDomain() == null) {
            return fatal(UNAVAILABLE_MESSAGE);
        }

        if (decision.getDomain() == TcmDomain.TCM
                || decision.getDomain() == TcmDomain.TCM_RELATED) {
            return success();
        }

        if (decision.getDomain() == TcmDomain.OUT_OF_SCOPE) {
            return fatal(OUT_OF_SCOPE_MESSAGE);
        }

        return fatal(UNAVAILABLE_MESSAGE);
    }
}
