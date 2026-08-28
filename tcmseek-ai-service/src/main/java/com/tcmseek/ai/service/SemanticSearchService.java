package com.tcmseek.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.ai.config.AiVectorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SemanticSearchService {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private static final Set<String> TARGET_SYMBOL_ANCHOR_TYPES = Set.of("topic", "target");

    private final AiVectorProperties properties;

    private final ObjectProvider<JdbcTemplate> vectorJdbcTemplateProvider;

    private final QwenEmbeddingClient embeddingClient;

    private final ObjectMapper objectMapper;

    public SemanticSearchService(AiVectorProperties properties,
                                 @Qualifier("vectorJdbcTemplate") ObjectProvider<JdbcTemplate> vectorJdbcTemplateProvider,
                                 QwenEmbeddingClient embeddingClient,
                                 ObjectMapper objectMapper) {
        this.properties = properties;
        this.vectorJdbcTemplateProvider = vectorJdbcTemplateProvider;
        this.embeddingClient = embeddingClient;
        this.objectMapper = objectMapper;
    }

    public boolean isReady() {
        return properties.isEnabled()
                && vectorJdbcTemplateProvider.getIfAvailable() != null
                && embeddingClient.isConfigured();
    }

    public VectorStatus status() {
        JdbcTemplate jdbcTemplate = vectorJdbcTemplateProvider.getIfAvailable();
        long count = 0;
        if (properties.isEnabled() && jdbcTemplate != null) {
            try {
                Long value = jdbcTemplate.queryForObject("SELECT count(*) FROM ai_semantic_doc WHERE enabled = true", Long.class);
                count = value == null ? 0 : value;
            } catch (RuntimeException ex) {
                log.warn("query semantic doc count failed message={}", ex.getMessage());
            }
        }
        return new VectorStatus(properties.isEnabled(), jdbcTemplate != null, embeddingClient.isConfigured(),
                properties.getDimension(), count);
    }

    public List<SemanticAnchor> search(String query, List<String> entityTypes, int limit) {
        if (!isReady() || !StringUtils.hasText(query)) {
            return List.of();
        }
        JdbcTemplate jdbcTemplate = vectorJdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            return List.of();
        }
        String vectorLiteral;
        try {
            vectorLiteral = embeddingClient.embedAsVectorLiteral(query);
        } catch (RuntimeException ex) {
            log.warn("embed semantic query failed query={} message={}", query, ex.getMessage());
            return List.of();
        }

        List<String> types = normalizeTypes(entityTypes);
        int safeLimit = Math.max(1, Math.min(limit, properties.getSearchLimit()));
        String typeFilter = "";
        if (!types.isEmpty()) {
            typeFilter = " AND entity_type IN (" + "?,".repeat(types.size());
            typeFilter = typeFilter.substring(0, typeFilter.length() - 1) + ")";
        }
        String sql = """
                SELECT id, entity_type, entity_id, neo4j_label, neo4j_key, name, aliases,
                       source_table, source_pk, metadata::text AS metadata,
                       embedding <=> ?::vector AS distance
                FROM ai_semantic_doc
                WHERE enabled = true
                """ + typeFilter + """

                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;

        try {
            return jdbcTemplate.query(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                int index = 1;
                ps.setString(index++, vectorLiteral);
                for (String type : types) {
                    ps.setString(index++, type);
                }
                ps.setString(index++, vectorLiteral);
                ps.setInt(index, safeLimit);
                return ps;
            }, (rs, rowNum) -> new SemanticAnchor(
                    rs.getString("id"),
                    rs.getString("entity_type"),
                    rs.getString("entity_id"),
                    rs.getString("neo4j_label"),
                    rs.getString("neo4j_key"),
                    rs.getString("name"),
                    readAliases(rs.getArray("aliases")),
                    rs.getString("source_table"),
                    rs.getString("source_pk"),
                    readMetadata(rs.getString("metadata")),
                    rs.getDouble("distance")));
        } catch (RuntimeException ex) {
            log.warn("semantic vector search failed query={} message={}", query, ex.getMessage());
            return List.of();
        }
    }

    public List<String> exactTopicTargetSymbols(String topic) {
        JdbcTemplate jdbcTemplate = vectorJdbcTemplateProvider.getIfAvailable();
        if (!properties.isEnabled() || jdbcTemplate == null || !StringUtils.hasText(topic)) {
            return List.of();
        }
        String sql = """
                SELECT metadata::text AS metadata
                FROM ai_semantic_doc
                WHERE enabled = true
                  AND entity_type = 'topic'
                  AND (lower(name) = lower(?)
                    OR EXISTS (
                        SELECT 1
                        FROM unnest(aliases) AS alias(alias_name)
                        WHERE lower(alias.alias_name) = lower(?)
                    ))
                LIMIT 5
                """;
        try {
            List<String> metadataValues = jdbcTemplate.query(sql,
                    ps -> {
                        ps.setString(1, topic);
                        ps.setString(2, topic);
                    },
                    (rs, rowNum) -> rs.getString("metadata"));
            Set<String> symbols = new LinkedHashSet<>();
            for (String metadata : metadataValues) {
                symbols.addAll(targetSymbolsFromMetadata(readMetadata(metadata)));
            }
            return new ArrayList<>(symbols);
        } catch (RuntimeException ex) {
            log.warn("semantic exact topic lookup failed topic={} message={}", topic, ex.getMessage());
            return List.of();
        }
    }

    public List<String> targetSymbolsFromAnchors(List<SemanticAnchor> anchors) {
        return targetSymbolsFromAnchors(anchors, Double.POSITIVE_INFINITY);
    }

    public List<String> targetSymbolsFromAnchors(List<SemanticAnchor> anchors, double maxDistance) {
        if (CollectionUtils.isEmpty(anchors)) {
            return List.of();
        }
        double safeMaxDistance = Double.isFinite(maxDistance) ? maxDistance : Double.POSITIVE_INFINITY;
        Set<String> symbols = new LinkedHashSet<>();
        for (SemanticAnchor anchor : anchors) {
            if (anchor == null) {
                continue;
            }
            String entityType = anchor.entityType() == null
                    ? ""
                    : anchor.entityType().trim().toLowerCase(Locale.ROOT);
            if (!TARGET_SYMBOL_ANCHOR_TYPES.contains(entityType) || anchor.distance() > safeMaxDistance) {
                continue;
            }
            if ("target".equals(entityType)) {
                addIfText(symbols, anchor.name());
                addIfText(symbols, anchor.entityId());
            }
            symbols.addAll(targetSymbolsFromMetadata(anchor.metadata()));
        }
        return new ArrayList<>(symbols);
    }

    @SuppressWarnings("unchecked")
    public List<String> targetSymbolsFromMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return List.of();
        }
        Set<String> symbols = new LinkedHashSet<>();
        Object value = metadata.get("targetSymbols");
        if (value instanceof Iterable<?> values) {
            for (Object item : values) {
                addIfText(symbols, item == null ? null : item.toString());
            }
        } else if (value instanceof String text) {
            for (String part : text.split("[,;锛寍銆乚\\s]+")) {
                addIfText(symbols, part);
            }
        }
        Object aliases = metadata.get("targetAliases");
        if (aliases instanceof Iterable<?> values) {
            for (Object item : values) {
                addIfText(symbols, item == null ? null : item.toString());
            }
        }
        return new ArrayList<>(symbols);
    }

    public Map<String, Object> readMetadata(String metadata) {
        if (!StringUtils.hasText(metadata)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadata, MAP_TYPE);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private List<String> normalizeTypes(List<String> entityTypes) {
        if (CollectionUtils.isEmpty(entityTypes)) {
            return List.of();
        }
        Set<String> types = new LinkedHashSet<>();
        for (String entityType : entityTypes) {
            if (StringUtils.hasText(entityType)) {
                types.add(entityType.trim().toLowerCase(Locale.ROOT));
            }
        }
        return new ArrayList<>(types);
    }

    private List<String> readAliases(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        Object raw = array.getArray();
        if (raw instanceof String[] values) {
            return List.of(values);
        }
        if (raw instanceof Object[] values) {
            List<String> aliases = new ArrayList<>();
            for (Object value : values) {
                if (value != null) {
                    aliases.add(value.toString());
                }
            }
            return aliases;
        }
        return List.of();
    }

    private void addIfText(Set<String> values, String value) {
        if (StringUtils.hasText(value)) {
            values.add(value.trim());
        }
    }

    public record SemanticAnchor(
            String id,
            String entityType,
            String entityId,
            String neo4jLabel,
            String neo4jKey,
            String name,
            List<String> aliases,
            String sourceTable,
            String sourcePk,
            Map<String, Object> metadata,
            double distance) {
    }

    public record VectorStatus(
            boolean enabled,
            boolean jdbcAvailable,
            boolean embeddingConfigured,
            int dimension,
            long documentCount) {
    }
}
