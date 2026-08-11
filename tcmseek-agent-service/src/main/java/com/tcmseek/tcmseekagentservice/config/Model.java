package com.tcmseek.tcmseekagentservice.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * LangChain4j 模型 Bean 配置。
 *
 * <p>当前服务同时保留 DeepSeek 同步/流式模型和 Qwen 流式/Responses 模型 Bean。
 * API Key 统一来自 {@link LlmProperties}，避免将密钥写死在代码中。</p>
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class Model {

    /**
     * LLM 配置属性。
     */
    private final LlmProperties llmProperties;

    /**
     * 构造模型配置类。
     */
    public Model(LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    /**
     * DeepSeek 流式 ChatModel。
     */
    @Bean
    public StreamingChatModel streamingChatModel() {

        OpenAiChatRequestParameters requestParameters = OpenAiChatRequestParameters.builder()
                .customParameters(Map.of(
                        "thinking", Map.of("type", "enabled")
                ))
                .reasoningEffort("high")
                .build();

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(required(llmProperties.getDeepseek().getApiKey(), "tcmseek.llm.deepseek.api-key"))
                .modelName(valueOrDefault(llmProperties.getDeepseek().getModelName(), "deepseek-v4-flash"))
                .baseUrl(valueOrDefault(llmProperties.getDeepseek().getBaseUrl(), "https://api.deepseek.com"))
                .returnThinking(true)
                .defaultRequestParameters(requestParameters)
                .build();
        return model;
    }

    /**
     * DeepSeek 同步 ChatModel，当前多 Agent 工作流主要使用这个 Bean。
     */
    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(required(llmProperties.getDeepseek().getApiKey(), "tcmseek.llm.deepseek.api-key"))
                .modelName(valueOrDefault(llmProperties.getDeepseek().getModelName(), "deepseek-v4-flash"))
                .baseUrl(valueOrDefault(llmProperties.getDeepseek().getBaseUrl(), "https://api.deepseek.com"))
                .temperature(0.7)
                .build();
    }

    /**
     * Qwen 流式 ChatModel。
     */
    @Bean
    public StreamingChatModel streamingChatModelQwen(){
        OpenAiChatRequestParameters requestParameters = OpenAiChatRequestParameters.builder()
                .customParameters(Map.of(
                        "enable_thinking", true
                ))
                .build();

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(required(llmProperties.getQwen().getApiKey(), "tcmseek.llm.qwen.api-key"))
                .modelName(valueOrDefault(llmProperties.getQwen().getModelName(), "qwen3.7-max"))
                .baseUrl(valueOrDefault(llmProperties.getQwen().getBaseUrl(), "https://llm-cb8m35te86kmvufg.cn-beijing.maas.aliyuncs.com/compatible-mode/v1"))
                .sendThinking(true)
                .returnThinking(true)
                .defaultRequestParameters(requestParameters)
                .build();
        return model;

    }

    /**
     * Qwen Responses 流式模型，保留 web_search server tool 配置。
     */
    @Bean
    public OpenAiResponsesStreamingChatModel openAiResponsesChatModel(){

//        /compatible-mode/v1/responses
        OpenAiResponsesStreamingChatModel model = OpenAiResponsesStreamingChatModel.builder()
                .apiKey(required(llmProperties.getQwen().getApiKey(), "tcmseek.llm.qwen.api-key"))
                .modelName(valueOrDefault(llmProperties.getQwen().getModelName(), "qwen3.7-max"))
                .baseUrl(valueOrDefault(llmProperties.getQwen().getBaseUrl(), "https://llm-cb8m35te86kmvufg.cn-beijing.maas.aliyuncs.com/compatible-mode/v1"))
                .reasoningEffort("high")
                .serverTools(List.of(
                        Map.of("type", "web_search")
                ))
                .build();

        return model;
    }

    /**
     * 读取必填配置；缺失时在启动阶段快速失败。
     */
    private String required(String value, String propertyName) {
        return Optional.ofNullable(value)
                .filter(v -> !v.isBlank())
                .orElseThrow(() -> new IllegalStateException("Missing required property: " + propertyName));
    }

    /**
     * 配置为空时使用默认值。
     */
    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

}
