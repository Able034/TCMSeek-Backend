package com.tcmseek.tcmseekagentservice.ai.model.workflow;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 证据完整性判断结果。
 *
 * <p>该对象由 judge 节点生成，用于决定进入 summary、replan，
 * 还是直接向用户追问补充信息。</p>
 */
@Data
@Description("判断现有证据是否足够回答用户问题")
public class AnswerJudgeResult implements Serializable {

    /**
     * 当前 Neo4j/MySQL 证据是否足够回答用户问题。
     */
    @Description("现有证据是否足够回答用户问题")
    private Boolean answerable;

    /**
     * 判断后的下一步动作，只能是 FINISH、REPLAN、ASK_USER。
     */
    @Description("建议下一步动作，只能是 FINISH、REPLAN、ASK_USER 之一")
    private String nextAction;

    /**
     * 仍然缺失的信息列表。
     */
    @Description("仍然缺失的信息")
    private List<String> missingInfo;

    /**
     * 判断原因。
     */
    @Description("判断原因")
    private String reason;
}
