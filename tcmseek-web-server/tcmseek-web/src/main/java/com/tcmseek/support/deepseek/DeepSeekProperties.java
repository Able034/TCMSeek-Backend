package com.tcmseek.support.deepseek;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API配置 - 专业模式使用
 */
@Data
@Component
@ConfigurationProperties(prefix = "tcm.deepseek")
public class DeepSeekProperties {

    /**
     * DeepSeek API Key
     */
    private String apiKey;

    /**
     * DeepSeek Base URL
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 聊天模型
     */
    private String chatModel = "deepseek-chat";

    /**
     * 嵌入模型
     */
    private String embeddingModel = "deepseek-embedding";
}
