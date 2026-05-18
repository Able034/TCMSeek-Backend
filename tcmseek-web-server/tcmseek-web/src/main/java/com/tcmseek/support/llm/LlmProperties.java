package com.tcmseek.support.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * LLM 代理相关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /**
     * 上游大模型服务地址，例如 https://host:8443。
     */
    private String baseUrl;

    /**
     * 默认模型名称。
     */
    private String model = "TCMReason";

    /**
     * 默认生成温度。
     */
    private Double defaultTemperature = 0.3d;

    /**
     * 默认最大片段长度（tokens）。
     */
    private Integer defaultMaxTokens = 1024;

    /**
     * 是否启用流式输出。当前前端仍采用非流式模式。
     */
    private Boolean stream = Boolean.FALSE;

    /**
     * 是否校验证书，默认为 false（与示例 verify=False 相同，表示跳过校验）。
     */
    private boolean verify = false;

    /**
     * 全局 system prompt。
     */
    private String systemPrompt =
            "你是一位资深中医专家，精通辨证论治、中药方剂和经典理论。"
                    + "请根据患者的症状、舌象、脉象等信息，进行严谨的中医辨证分析，"
                    + "并提供治法、方剂（含剂量）、加减建议和生活调护指导。"
                    + "回答需专业、清晰、有理有据，避免西医术语。";

    /**
     * 消息总条数超过该阈值时触发摘要。
     */
    private int summaryTriggerMessages = 12;

    /**
     * 摘要后保留的最近消息条数。
     */
    private int summaryKeepRecent = 6;

    /**
     * 摘要最大长度（字符）。
     */
    private int summaryMaxLength = 480;

    /**
     * 连接超时时间。
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * 响应超时时间。
     */
    private Duration readTimeout = Duration.ofSeconds(120);
}
