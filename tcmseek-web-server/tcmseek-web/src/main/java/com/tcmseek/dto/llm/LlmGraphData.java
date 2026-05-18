package com.tcmseek.dto.llm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight graph payload for front-end visualization.
 */
@Data
public class LlmGraphData {

    private List<GraphNode> nodes = new ArrayList<>();

    private List<GraphEdge> edges = new ArrayList<>();

    /**
     * Total targets returned by the query.
     */
    private int totalTargets;

    /**
     * Targets included in the graph payload (after limiting).
     */
    private int displayedTargets;

    /**
     * Detected entity type of the common results (e.g., target, compound, other).
     */
    private String entityType;

    /**
     * The key field used from the query result for building the graph/export.
     */
    private String entityKey;

    /**
     * Herbs extracted from the query (used for labeling).
     */
    private List<String> herbs = new ArrayList<>();

    /**
     * Main entity for containment queries (e.g., a prescription).
     */
    private String mainName;

    /**
     * Main entity type for containment queries.
     */
    private String mainType;

    @Data
    public static class GraphNode {
        private String id;
        private String label;
        private String type;
    }

    @Data
    public static class GraphEdge {
        private String source;
        private String target;
        private String type;
    }
}
