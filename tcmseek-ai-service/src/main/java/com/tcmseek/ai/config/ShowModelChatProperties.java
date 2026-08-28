package com.tcmseek.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tcmseek.ai.showmodel")
public class ShowModelChatProperties {

    private String systemPrompt = """
            你是 TCMReason 展示页的中医药问答助手。回答要自然、专业、简洁，面向普通用户。
            可以结合中医药知识图谱工具结果和通用中医药知识进行解释，但不要暴露工具名、函数名、接口名、节点/边、CSV、Excel、下载、导出或前端图谱等内部实现。
            如果没有检索到直接结构化结果，不要只回复未查询到；请说明当前没有直接匹配资料，并基于通用中医药知识给出谨慎、可理解的解释。
            涉及疾病、处方、用药、剂量或治疗建议时，必须提醒需由专业中医师辨证，不能替代诊疗。
            """;

    private String summaryPromptTemplate = """
            用户问题：
            {question}

            后端检索结果 JSON：
            {toolResultJson}

            请把检索结果整理成面向展示页用户的自然语言回答：
            1. 不要提工具名、函数名、接口名、知识图谱、节点、边、CSV、Excel、下载、导出或前端图谱。
            2. 优先给出一句核心结论，再列出最多 {answerItemLimit} 条代表性结果。
            3. 如果结果里有 prescription、herb、disease、effects、indications、evidenceType 等字段，只展示用户能理解的信息。
            4. 如果 evidenceType 表示间接证据，请说明是“包含该中药的方剂与病症相关”，不要说成该中药直接治疗该病。
            5. 不要编造检索结果里没有的方剂、疾病、剂量或疗效。
            6. 末尾用一句话提醒：具体用药需由专业中医师辨证。
            """;

    private String noToolResultMessage =
            "当前没有检索到直接匹配的结构化资料，可先从通用中医药知识角度谨慎参考；具体用药仍需专业中医师辨证。";

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getSummaryPromptTemplate() {
        return summaryPromptTemplate;
    }

    public void setSummaryPromptTemplate(String summaryPromptTemplate) {
        this.summaryPromptTemplate = summaryPromptTemplate;
    }

    public String getNoToolResultMessage() {
        return noToolResultMessage;
    }

    public void setNoToolResultMessage(String noToolResultMessage) {
        this.noToolResultMessage = noToolResultMessage;
    }
}
