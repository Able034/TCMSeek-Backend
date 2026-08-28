package com.tcmseek.tcmseekagentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM 配置属性。
 *
 * <p>统一从 {@code tcmseek.llm} 读取模型地址、模型名和 API Key，
 * 避免在 Java 代码里硬编码密钥。</p>
 */
@ConfigurationProperties(prefix = "tcmseek.llm")
public class LlmProperties {

    /**
     * DeepSeek/OpenAI-compatible 模型配置，用于当前同步 ChatModel。
     */
    private Provider deepseek = new Provider();

    /**
     * Qwen/OpenAI-compatible 模型配置，用于流式或 Responses 模型。
     */
    private Provider qwen = new Provider();

    public Provider getDeepseek() {
        return deepseek;
    }

    public void setDeepseek(Provider deepseek) {
        this.deepseek = deepseek;
    }

    public Provider getQwen() {
        return qwen;
    }

    public void setQwen(Provider qwen) {
        this.qwen = qwen;
    }

    /**
     * 单个模型服务提供方配置。
     */
    public static class Provider {

        /**
         * API Key，建议通过环境变量注入。
         */
        private String apiKey;

        /**
         * 模型名称。
         */
        private String modelName;

        /**
         * OpenAI-compatible baseUrl。
         */
        private String baseUrl;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
