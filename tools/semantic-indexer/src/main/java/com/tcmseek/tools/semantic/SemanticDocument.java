package com.tcmseek.tools.semantic;

import java.util.List;
import java.util.Map;

public record SemanticDocument(
        String id,
        String entityType,
        String entityId,
        String neo4jLabel,
        String neo4jKey,
        String name,
        List<String> aliases,
        String sourceTable,
        String sourcePk,
        String textForEmbedding,
        Map<String, Object> metadata) {
}
