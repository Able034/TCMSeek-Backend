package com.tcmseek.tools.semantic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MysqlEntityReader implements AutoCloseable {

    private final Connection connection;

    public MysqlEntityReader(IndexerConfig config) throws SQLException {
        this.connection = DriverManager.getConnection(
                config.mysqlUrl(),
                config.mysqlUsername(),
                config.mysqlPassword());
    }

    public List<SemanticDocument> read(String type, int limit) throws SQLException {
        return switch (type) {
            case "target" -> readTargets(limit);
            case "disease" -> readDiseases(limit);
            case "herb" -> readHerbs(limit);
            case "prescription" -> readPrescriptions(limit);
            case "tcm_symptom" -> readTcmSymptoms(limit);
            case "wm_symptom" -> readWmSymptoms(limit);
            case "syndrome" -> readSyndromes(limit);
            default -> List.of();
        };
    }

    private List<SemanticDocument> readTargets(int limit) throws SQLException {
        String sql = """
                SELECT tcm_tar_id, symbol, uniprot_id, ensembl_id, description, type_of_gene
                FROM targets
                WHERE symbol IS NOT NULL OR description IS NOT NULL
                ORDER BY id
                LIMIT ?
                """;
        return query(sql, limit).stream()
                .map(row -> document(
                        "target",
                        value(row, "tcm_tar_id", value(row, "symbol")),
                        "Target",
                        "symbol",
                        value(row, "symbol", value(row, "tcm_tar_id")),
                        aliases(value(row, "symbol"), value(row, "uniprot_id"), value(row, "ensembl_id")),
                        "targets",
                        value(row, "tcm_tar_id"),
                        text("Target", row, "symbol", "description", "type_of_gene", "uniprot_id", "ensembl_id"),
                        metadata(row, "symbol", "uniprot_id", "ensembl_id", "type_of_gene")))
                .toList();
    }

    private List<SemanticDocument> readDiseases(int limit) throws SQLException {
        String sql = """
                SELECT disease_id, disease_name, source
                FROM diseases
                WHERE disease_name IS NOT NULL
                ORDER BY id
                LIMIT ?
                """;
        return query(sql, limit).stream()
                .map(row -> document(
                        "disease",
                        value(row, "disease_id", value(row, "disease_name")),
                        "Disease",
                        "disease_name",
                        value(row, "disease_name"),
                        aliases(value(row, "disease_id")),
                        "diseases",
                        value(row, "disease_id"),
                        text("Disease", row, "disease_name", "disease_id", "source"),
                        metadata(row, "disease_id", "source")))
                .toList();
    }

    private List<SemanticDocument> readHerbs(int limit) throws SQLException {
        String sql = """
                SELECT tcm_herb_id, herb_name_zh, pinyin_name, latin_name, english_name,
                       type, efficacy_zh, function_en, efficacy_category,
                       nature_taste_zh, property_en, meridian_zh, meridian_tropism_en,
                       indications_zh, indication_en, classification_zh, use_part,
                       toxicity_zh, toxic_description_zh
                FROM core_tcm_herbs
                WHERE herb_name_zh IS NOT NULL
                ORDER BY id
                LIMIT ?
                """;
        return query(sql, limit).stream()
                .map(row -> document(
                        "herb",
                        value(row, "tcm_herb_id", value(row, "herb_name_zh")),
                        "CoreHerb",
                        "herb_name_zh",
                        value(row, "herb_name_zh"),
                        aliases(value(row, "pinyin_name"), value(row, "latin_name"), value(row, "english_name")),
                        "core_tcm_herbs",
                        value(row, "tcm_herb_id"),
                        text("Herb", row,
                                "herb_name_zh", "pinyin_name", "latin_name", "english_name",
                                "type", "efficacy_zh", "function_en", "efficacy_category",
                                "nature_taste_zh", "property_en", "meridian_zh", "meridian_tropism_en",
                                "indications_zh", "indication_en", "classification_zh", "use_part",
                                "toxicity_zh", "toxic_description_zh"),
                        metadata(row, "tcm_herb_id", "efficacy_category", "nature_taste_zh", "meridian_zh")))
                .toList();
    }

    private List<SemanticDocument> readPrescriptions(int limit) throws SQLException {
        String sql = """
                SELECT tcm_prescription_id, name_zh, pinyin_name, source,
                       indications_zh, indications_en, effects, effects_zh
                FROM tcm_prescriptions
                WHERE name_zh IS NOT NULL
                ORDER BY id
                LIMIT ?
                """;
        return query(sql, limit).stream()
                .map(row -> document(
                        "prescription",
                        value(row, "tcm_prescription_id", value(row, "name_zh")),
                        "Prescription",
                        "name_zh",
                        value(row, "name_zh"),
                        aliases(value(row, "pinyin_name")),
                        "tcm_prescriptions",
                        value(row, "tcm_prescription_id"),
                        text("Prescription", row,
                                "name_zh", "pinyin_name", "source",
                                "indications_zh", "indications_en", "effects", "effects_zh"),
                        metadata(row, "tcm_prescription_id", "source")))
                .toList();
    }

    private List<SemanticDocument> readTcmSymptoms(int limit) throws SQLException {
        String sql = """
                SELECT tcm_symptom_id, symptom_name_zh, symptom_pinyin,
                       symptom_definition, symptom_locus, symptom_property, type
                FROM tcm_symptoms
                WHERE symptom_name_zh IS NOT NULL
                ORDER BY id
                LIMIT ?
                """;
        return query(sql, limit).stream()
                .map(row -> document(
                        "tcm_symptom",
                        value(row, "tcm_symptom_id", value(row, "symptom_name_zh")),
                        "TcmSymptom",
                        "symptom_name_zh",
                        value(row, "symptom_name_zh"),
                        aliases(value(row, "symptom_pinyin")),
                        "tcm_symptoms",
                        value(row, "tcm_symptom_id"),
                        text("TCM Symptom", row,
                                "symptom_name_zh", "symptom_pinyin", "symptom_definition",
                                "symptom_locus", "symptom_property", "type"),
                        metadata(row, "tcm_symptom_id", "symptom_locus", "symptom_property", "type")))
                .toList();
    }

    private List<SemanticDocument> readWmSymptoms(int limit) throws SQLException {
        String sql = """
                SELECT ws.id,
                       ws.wm_symptom_id,
                       ws.symptom_name,
                       ws.umls_id,
                       GROUP_CONCAT(DISTINCT twr.tcm_symptom_id ORDER BY twr.tcm_symptom_id SEPARATOR ',') AS tcm_symptom_ids,
                       GROUP_CONCAT(DISTINCT ts.symptom_name_zh ORDER BY ts.symptom_name_zh SEPARATOR ',') AS tcm_symptom_names,
                       GROUP_CONCAT(DISTINCT t.symbol ORDER BY t.symbol SEPARATOR ',') AS target_symbols
                FROM wm_symptoms ws
                LEFT JOIN tcm_wm_symptom_rel twr ON twr.wm_symptom_id = ws.wm_symptom_id
                LEFT JOIN tcm_symptoms ts ON ts.tcm_symptom_id = twr.tcm_symptom_id
                LEFT JOIN wm_symptom_gene_rel wgr ON wgr.wm_symptom_id = ws.wm_symptom_id
                LEFT JOIN targets t ON t.tcm_tar_id = wgr.tcm_tar_id
                WHERE ws.symptom_name IS NOT NULL
                GROUP BY ws.id, ws.wm_symptom_id, ws.symptom_name, ws.umls_id
                ORDER BY ws.id
                LIMIT ?
                """;
        return query(sql, limit).stream()
                .map(row -> document(
                        "wm_symptom",
                        value(row, "wm_symptom_id", value(row, "symptom_name")),
                        "WmSymptom",
                        "symptom_name",
                        value(row, "symptom_name"),
                        aliases(value(row, "umls_id"), value(row, "tcm_symptom_names")),
                        "wm_symptoms",
                        value(row, "wm_symptom_id"),
                        text("Western Symptom", row,
                                "symptom_name", "umls_id", "tcm_symptom_names", "target_symbols"),
                        metadataWithLists(row,
                                List.of("wm_symptom_id", "umls_id"),
                                Map.of(
                                        "tcmSymptomIds", "tcm_symptom_ids",
                                        "tcmSymptomNames", "tcm_symptom_names",
                                        "targetSymbols", "target_symbols"))))
                .toList();
    }

    private List<SemanticDocument> readSyndromes(int limit) throws SQLException {
        String sql = """
                SELECT tcm_syndrome_id, syndrome_name_zh, syndrome_english, syndrome_pinyin,
                       syndrome_definition_zh, syndrome_description_en,
                       category_zh, category_en, source
                FROM tcm_syndromes
                WHERE syndrome_name_zh IS NOT NULL
                ORDER BY id
                LIMIT ?
                """;
        return query(sql, limit).stream()
                .map(row -> document(
                        "syndrome",
                        value(row, "tcm_syndrome_id", value(row, "syndrome_name_zh")),
                        "Syndrome",
                        "syndrome_name_zh",
                        value(row, "syndrome_name_zh"),
                        aliases(value(row, "syndrome_english"), value(row, "syndrome_pinyin")),
                        "tcm_syndromes",
                        value(row, "tcm_syndrome_id"),
                        text("Syndrome", row,
                                "syndrome_name_zh", "syndrome_english", "syndrome_pinyin",
                                "syndrome_definition_zh", "syndrome_description_en",
                                "category_zh", "category_en", "source"),
                        metadata(row, "tcm_syndrome_id", "category_zh", "category_en", "source")))
                .toList();
    }

    private List<Map<String, Object>> query(String sql, int limit) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metadata = rs.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= metadata.getColumnCount(); i++) {
                        row.put(metadata.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    private SemanticDocument document(String entityType,
                                      String entityId,
                                      String neo4jLabel,
                                      String neo4jKey,
                                      String name,
                                      List<String> aliases,
                                      String sourceTable,
                                      String sourcePk,
                                      String textForEmbedding,
                                      Map<String, Object> metadata) {
        String stableKey = sourcePk == null || sourcePk.isBlank() ? name : sourcePk;
        return new SemanticDocument(
                entityType + ":" + sha1(stableKey),
                entityType,
                entityId,
                neo4jLabel,
                neo4jKey,
                name,
                aliases,
                sourceTable,
                sourcePk,
                textForEmbedding,
                metadata);
    }

    private String text(String label, Map<String, Object> row, String... keys) {
        StringBuilder sb = new StringBuilder(label).append(": ");
        for (String key : keys) {
            String value = value(row, key);
            if (!value.isBlank()) {
                sb.append(key).append("=").append(value).append(". ");
            }
        }
        return sb.toString().trim();
    }

    private Map<String, Object> metadata(Map<String, Object> row, String... keys) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String key : keys) {
            String value = value(row, key);
            if (!value.isBlank()) {
                values.put(key, value);
            }
        }
        return values;
    }

    private Map<String, Object> metadataWithLists(Map<String, Object> row,
                                                  List<String> scalarKeys,
                                                  Map<String, String> listKeys) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String key : scalarKeys) {
            String value = value(row, key);
            if (!value.isBlank()) {
                values.put(key, value);
            }
        }
        for (Map.Entry<String, String> entry : listKeys.entrySet()) {
            List<String> list = splitCsv(value(row, entry.getValue()));
            if (!list.isEmpty()) {
                values.put(entry.getKey(), list);
            }
        }
        return values;
    }

    private List<String> splitCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        Set<String> values = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            String value = part.trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return new ArrayList<>(values);
    }

    private List<String> aliases(String... values) {
        Set<String> aliases = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                aliases.add(value.trim());
            }
        }
        return new ArrayList<>(aliases);
    }

    private String value(Map<String, Object> row, String key) {
        return value(row, key, "");
    }

    private String value(Map<String, Object> row, String key, String fallback) {
        Object value = row.get(key);
        if (value == null) {
            return fallback == null ? "" : fallback;
        }
        String text = value.toString().trim();
        return text.isBlank() ? (fallback == null ? "" : fallback) : text;
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(String.valueOf(value).hashCode());
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
