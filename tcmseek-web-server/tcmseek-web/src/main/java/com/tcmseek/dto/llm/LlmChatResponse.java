package com.tcmseek.dto.llm;

import lombok.Data;

/**
 * 响应的基本信息、回复内容、结束原因以及token使用情况
 */
@Data
public class LlmChatResponse {

    private String id;

    private String model;

    private String reply;

    private String finishReason;

    private Usage usage;

    /**
     * Graph snapshot for visualization (optional, only returned in academic mode when applicable).
     */
    private LlmGraphData graph;

    /**
     * Download link for full results CSV (optional).
     */
    private String csvDownloadUrl;

    /**
     * Total rows returned by the underlying Neo4j query (optional).
     */
    private Integer totalResults;

    /**
     * Rows included in the lightweight graph payload (optional).
     */
    private Integer displayedResults;

    @Data
    public static class Usage {

        private Integer promptTokens;

        private Integer completionTokens;

        private Integer totalTokens;
    }
}
