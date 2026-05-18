package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.ToolCallResult;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class CsvExportStore {

    private static final Duration EXPORT_TTL = Duration.ofMinutes(30);

    private final ConcurrentMap<String, ExportRecord> records = new ConcurrentHashMap<>();

    private final AiRedisCache redisCache;

    public CsvExportStore(AiRedisCache redisCache) {
        this.redisCache = redisCache;
    }

    public String saveFirstExport(List<ToolCallResult> toolResults, String userId) {
        if (CollectionUtils.isEmpty(toolResults)) {
            return null;
        }
        for (ToolCallResult toolResult : toolResults) {
            ExportRecord record = toExportRecord(toolResult);
            if (record != null) {
                String id = UUID.randomUUID().toString().replace("-", "");
                records.put(scopedKey(userId, id), record);
                redisCache.putExport(userId, id, record);
                cleanupExpired();
                return id;
            }
        }
        return null;
    }

    public ExportRecord get(String exportId, String userId) {
        cleanupExpired();
        if (exportId == null) {
            return null;
        }
        ExportRecord redisRecord = redisCache.getExport(userId, exportId);
        if (redisRecord != null) {
            records.put(scopedKey(userId, exportId), redisRecord);
            return redisRecord;
        }
        return records.get(scopedKey(userId, exportId));
    }

    private ExportRecord toExportRecord(ToolCallResult toolResult) {
        if (toolResult == null || toolResult.getResult() == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(toolResult.getResult().getItems())) {
            return null;
        }
        String queryType = toolResult.getResult().getQueryType();
        if ("common_targets".equals(queryType)) {
            return buildCommonTargetsExport(toolResult);
        }
        if ("common_compounds".equals(queryType)) {
            return buildCommonCompoundsExport(toolResult);
        }
        if ("herb_compounds".equals(queryType)) {
            return buildHerbCompoundsExport(toolResult);
        }
        if ("prescription_herbs".equals(queryType)) {
            return buildPrescriptionHerbsExport(toolResult);
        }
        if ("herb_diseases".equals(queryType)) {
            return buildSingleInputExport(toolResult, "herbName", "herb-diseases.csv",
                    Arrays.asList("herb_name", "disease"), "disease");
        }
        if ("prescription_symptoms".equals(queryType)) {
            return buildSingleInputExport(toolResult, "prescriptionName", "prescription-symptoms.csv",
                    Arrays.asList("prescription_name", "symptom"), "symptom");
        }
        if ("prescription_syndromes".equals(queryType)) {
            return buildSingleInputExport(toolResult, "prescriptionName", "prescription-syndromes.csv",
                    Arrays.asList("prescription_name", "syndrome"), "syndrome");
        }
        if ("disease_targets".equals(queryType)) {
            return buildSingleInputExport(toolResult, "diseaseName", "disease-targets.csv",
                    Arrays.asList("disease_name", "target"), "target");
        }
        if ("syndrome_symptoms".equals(queryType)) {
            return buildSingleInputExport(toolResult, "syndromeName", "syndrome-symptoms.csv",
                    Arrays.asList("syndrome_name", "symptom"), "symptom");
        }
        if ("compound_targets".equals(queryType)) {
            return buildSingleInputExport(toolResult, "compound", "compound-targets.csv",
                    Arrays.asList("compound", "target"), "target");
        }
        if ("pathway_targets".equals(queryType)) {
            return buildSingleInputExport(toolResult, "pathwayName", "pathway-targets.csv",
                    Arrays.asList("pathway", "target"), "target");
        }
        if ("medicalcase_prescriptions".equals(queryType)) {
            return buildSingleInputExport(toolResult, "caseId", "medicalcase-prescriptions.csv",
                    Arrays.asList("case_id", "prescription"), "prescription");
        }
        return buildGenericExport(toolResult);
    }

    private ExportRecord buildCommonTargetsExport(ToolCallResult toolResult) {
        String herbNames = herbNames(toolResult);
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, Object> item : toolResult.getResult().getItems()) {
            rows.add(Arrays.asList(
                    herbNames,
                    asString(item.get("target")),
                    asString(item.get("targetId"))));
        }
        return new ExportRecord("common-targets.csv", Arrays.asList("herb_names", "target", "target_id"), rows);
    }

    private ExportRecord buildCommonCompoundsExport(ToolCallResult toolResult) {
        String herbNames = herbNames(toolResult);
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, Object> item : toolResult.getResult().getItems()) {
            rows.add(Arrays.asList(
                    herbNames,
                    asString(item.get("compound")),
                    asString(item.get("inchikey")),
                    asString(item.get("formula"))));
        }
        return new ExportRecord("common-compounds.csv", Arrays.asList("herb_names", "compound", "inchikey", "formula"), rows);
    }

    private ExportRecord buildHerbCompoundsExport(ToolCallResult toolResult) {
        String herbName = asString(toolResult.getArguments().get("herbName"));
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, Object> item : toolResult.getResult().getItems()) {
            rows.add(Arrays.asList(
                    herbName,
                    asString(item.get("compound")),
                    asString(item.get("inchikey")),
                    asString(item.get("formula"))));
        }
        return new ExportRecord("herb-compounds.csv", Arrays.asList("herb_name", "compound", "inchikey", "formula"), rows);
    }

    private ExportRecord buildPrescriptionHerbsExport(ToolCallResult toolResult) {
        String prescriptionName = asString(toolResult.getArguments().get("prescriptionName"));
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, Object> item : toolResult.getResult().getItems()) {
            rows.add(Arrays.asList(prescriptionName, asString(item.get("herb"))));
        }
        return new ExportRecord("prescription-herbs.csv", Arrays.asList("prescription_name", "herb"), rows);
    }

    private ExportRecord buildSingleInputExport(ToolCallResult toolResult,
                                                String inputArgument,
                                                String filename,
                                                List<String> headers,
                                                String itemKey) {
        String input = asString(toolResult.getArguments().get(inputArgument));
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, Object> item : toolResult.getResult().getItems()) {
            rows.add(Arrays.asList(input, asString(item.get(itemKey))));
        }
        return new ExportRecord(filename, headers, rows);
    }

    private ExportRecord buildGenericExport(ToolCallResult toolResult) {
        String queryType = toolResult.getResult().getQueryType();
        Map<String, Object> arguments = toolResult.getArguments() == null ? Map.of() : toolResult.getArguments();
        List<String> argumentKeys = new ArrayList<>(arguments.keySet());
        List<String> itemKeys = collectItemKeys(toolResult);

        List<String> headers = new ArrayList<>();
        headers.add("query_type");
        for (String argumentKey : argumentKeys) {
            headers.add("arg_" + argumentKey);
        }
        headers.addAll(itemKeys);

        List<List<String>> rows = new ArrayList<>();
        for (Map<String, Object> item : toolResult.getResult().getItems()) {
            List<String> row = new ArrayList<>();
            row.add(queryType);
            for (String argumentKey : argumentKeys) {
                row.add(asString(arguments.get(argumentKey)));
            }
            for (String itemKey : itemKeys) {
                row.add(asString(item.get(itemKey)));
            }
            rows.add(row);
        }
        String filename = queryType == null || queryType.isBlank()
                ? "graph-result.csv"
                : queryType.replaceAll("[^a-zA-Z0-9_-]", "-") + ".csv";
        return new ExportRecord(filename, headers, rows);
    }

    private List<String> collectItemKeys(ToolCallResult toolResult) {
        Set<String> keys = new LinkedHashSet<>();
        for (Map<String, Object> item : toolResult.getResult().getItems()) {
            if (item != null) {
                keys.addAll(item.keySet());
            }
        }
        return new ArrayList<>(keys);
    }

    private String herbNames(ToolCallResult toolResult) {
        Map<String, Object> arguments = toolResult.getArguments() == null ? Map.of() : toolResult.getArguments();
        Object herbNames = arguments.get("herbNames");
        if (herbNames != null) {
            return asJoinedString(herbNames);
        }
        List<String> values = new ArrayList<>();
        String herbA = asString(arguments.get("herbA"));
        String herbB = asString(arguments.get("herbB"));
        if (!herbA.isBlank()) {
            values.add(herbA);
        }
        if (!herbB.isBlank()) {
            values.add(herbB);
        }
        return String.join("，", values);
    }

    private String asJoinedString(Object value) {
        if (value instanceof Iterable<?> values) {
            List<String> parts = new ArrayList<>();
            for (Object item : values) {
                String part = asString(item);
                if (!part.isBlank()) {
                    parts.add(part);
                }
            }
            return String.join("，", parts);
        }
        return asString(value);
    }

    private void cleanupExpired() {
        long now = Instant.now().toEpochMilli();
        records.entrySet().removeIf(entry -> now - entry.getValue().getCreatedAt() > EXPORT_TTL.toMillis());
    }

    private String scopedKey(String userId, String exportId) {
        String owner = userId == null || userId.isBlank() ? "anonymous" : userId;
        return owner + ":" + exportId;
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    public static class ExportRecord {

        private final String filename;

        private final List<String> headers;

        private final List<List<String>> rows;

        private final long createdAt = Instant.now().toEpochMilli();

        public ExportRecord(String filename, List<String> headers, List<List<String>> rows) {
            this.filename = filename;
            this.headers = headers;
            this.rows = rows;
        }

        public String getFilename() {
            return filename;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<List<String>> getRows() {
            return rows;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}
