package com.tcmseek.tools.semantic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class PgvectorWriter implements AutoCloseable {

    private final IndexerConfig config;

    private final Connection connection;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PgvectorWriter(IndexerConfig config) throws SQLException {
        this.config = config;
        this.connection = DriverManager.getConnection(
                config.pgvectorUrl(),
                config.pgvectorUsername(),
                config.pgvectorPassword());
    }

    public void initializeSchema() throws SQLException {
        execute("CREATE EXTENSION IF NOT EXISTS vector");
        execute("""
                CREATE TABLE IF NOT EXISTS ai_semantic_doc (
                  id varchar(128) PRIMARY KEY,
                  entity_type varchar(50) NOT NULL,
                  entity_id varchar(100),
                  neo4j_label varchar(50),
                  neo4j_key varchar(50),
                  name varchar(300) NOT NULL,
                  aliases text[],
                  source_table varchar(100),
                  source_pk varchar(100),
                  text_for_embedding text NOT NULL,
                  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
                """ + "  embedding vector(" + config.embeddingDimension() + ") NOT NULL,\n" + """
                  enabled boolean NOT NULL DEFAULT true,
                  updated_at timestamptz NOT NULL DEFAULT now()
                )
                """);
        execute("CREATE INDEX IF NOT EXISTS idx_ai_semantic_doc_entity_type ON ai_semantic_doc(entity_type)");
        execute("CREATE INDEX IF NOT EXISTS idx_ai_semantic_doc_name ON ai_semantic_doc(name)");
        execute("CREATE INDEX IF NOT EXISTS idx_ai_semantic_doc_source ON ai_semantic_doc(source_table, source_pk)");
        execute("CREATE INDEX IF NOT EXISTS idx_ai_semantic_doc_metadata ON ai_semantic_doc USING gin(metadata)");
        execute("""
                CREATE INDEX IF NOT EXISTS idx_ai_semantic_doc_embedding_hnsw
                ON ai_semantic_doc USING hnsw (embedding vector_cosine_ops)
                """);
    }

    public synchronized void upsert(SemanticDocument document, String vectorLiteral) throws SQLException {
        String sql = """
                INSERT INTO ai_semantic_doc (
                  id, entity_type, entity_id, neo4j_label, neo4j_key, name, aliases,
                  source_table, source_pk, text_for_embedding, metadata, embedding, enabled, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::vector, true, now())
                ON CONFLICT (id) DO UPDATE SET
                  entity_type = EXCLUDED.entity_type,
                  entity_id = EXCLUDED.entity_id,
                  neo4j_label = EXCLUDED.neo4j_label,
                  neo4j_key = EXCLUDED.neo4j_key,
                  name = EXCLUDED.name,
                  aliases = EXCLUDED.aliases,
                  source_table = EXCLUDED.source_table,
                  source_pk = EXCLUDED.source_pk,
                  text_for_embedding = EXCLUDED.text_for_embedding,
                  metadata = EXCLUDED.metadata,
                  embedding = EXCLUDED.embedding,
                  enabled = true,
                  updated_at = now()
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            ps.setString(index++, document.id());
            ps.setString(index++, document.entityType());
            ps.setString(index++, document.entityId());
            ps.setString(index++, document.neo4jLabel());
            ps.setString(index++, document.neo4jKey());
            ps.setString(index++, document.name());
            ps.setArray(index++, connection.createArrayOf("text", document.aliases().toArray(String[]::new)));
            ps.setString(index++, document.sourceTable());
            ps.setString(index++, document.sourcePk());
            ps.setString(index++, document.textForEmbedding());
            ps.setString(index++, toJson(document.metadata()));
            ps.setString(index, vectorLiteral);
            ps.executeUpdate();
        }
    }

    private void execute(String sql) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        }
    }

    private String toJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
