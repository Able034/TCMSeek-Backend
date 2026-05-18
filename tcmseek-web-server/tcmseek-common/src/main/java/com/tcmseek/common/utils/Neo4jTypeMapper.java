package com.tcmseek.common.utils;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Neo4jTypeMapper {

    // 中心节点类型映射 (前端类型 -> Neo4j标签)
    private static final Map<String, String> CENTER_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("herb", "Herb");
        put("prescription", "Prescription");
        put("compound", "Compound");
        put("gene", "Target");
        put("disease", "Disease");
        put("symptom", "Symptom");
        put("syndrome", "Syndrome");
        put("wm_symptom", "WmSymptom");
        put("phenotype", "Phenotype");
        put("pathway", "Pathway");
    }});

    private static final Map<String, List<String>> DEFAULT_RELATION_MAP = createDefaultRelationMap();
    private static final Map<String, Map<String, List<String>>> RELATION_MATRIX = createRelationMatrix();

    // ID属性映射 (前端类型 -> Neo4j属性名)
    private static final Map<String, String> ID_PROPERTY_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("herb", "tcm_herb_id");
        put("prescription", "tcm_prescription_id");
        put("compound", "inchikey");
        put("gene", "tcm_tar_id");
        put("disease", "disease_id");
        put("symptom", "tcm_symptom_id");
        put("syndrome", "tcm_syndrome_id");
        put("wm_symptom", "wm_symptom_id");
    }});

    // 目标节点标签映射 (用于WHERE条件)
    private static final Map<String, String> TARGET_LABEL_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("herb", "Herb");
        put("prescription", "Prescription");
        put("compound", "Compound");
        put("gene", "Target");
        put("disease", "Disease");
        put("symptom", "Symptom");
        put("syndrome", "Syndrome");
        put("wm_symptom", "WmSymptom");
    }});

    public String getCenterLabel(String centerType) {
        String label = CENTER_TYPE_MAP.get(centerType);
        if (label == null) {
            throw new IllegalArgumentException("无效的中心节点类型: " + centerType);
        }
        return label;
    }

    public String getIdProperty(String centerType) {
        String property = ID_PROPERTY_MAP.get(centerType);
        if (property == null) {
            throw new IllegalArgumentException("无效的中心节点类型: " + centerType);
        }
        return property;
    }

    public List<String> getTargetLabels(List<String> relationTypes) {
        return relationTypes.stream()
                .map(type -> {
                    String label = TARGET_LABEL_MAP.get(type);
                    if (label == null) {
                        throw new IllegalArgumentException("无效的关系类型: " + type);
                    }
                    return label;
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getValidRelations(String centerType, List<String> relationTypes) {
        return relationTypes.stream()
                .flatMap(type -> getRelationsForPair(centerType, type).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getRelationsForPair(String centerType, String relationType) {
        Map<String, List<String>> centerRelations = RELATION_MATRIX.get(centerType);
        if (centerRelations != null) {
            List<String> customRelations = centerRelations.get(relationType);
            if (customRelations != null && !customRelations.isEmpty()) {
                return customRelations;
            }
        }
        return DEFAULT_RELATION_MAP.getOrDefault(relationType, Collections.emptyList());
    }

    public boolean isValidCenterType(String centerType) {
        return CENTER_TYPE_MAP.containsKey(centerType);
    }

    public boolean isValidRelationType(String relationType) {
        return TARGET_LABEL_MAP.containsKey(relationType);
    }

    private static Map<String, List<String>> createDefaultRelationMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("compound", Arrays.asList("CONTAINS_COMPOUND", "TARGETS"));
        map.put("disease", Arrays.asList("TREATS_DISEASE", "ASSOCIATED_WITH"));
        map.put("syndrome", Arrays.asList("TREATS_SYNDROME", "HAS_SYMPTOM", "ASSOCIATED_WITH"));
        map.put("symptom", Arrays.asList("TREATS_SYMPTOM", "HAS_SYMPTOM"));
        map.put("wm_symptom", Arrays.asList("RELATED_TO", "ASSOCIATED_WITH"));
        map.put("prescription", Arrays.asList("CONTAINS_HERB", "USES_PRESCRIPTION"));
        map.put("herb", Arrays.asList("CONTAINS_HERB", "USES_HERB", "CONTAINS_COMPOUND", "TREATS_DISEASE", "TREATS_SYNDROME", "TREATS_SYMPTOM"));
        map.put("gene", Arrays.asList("TARGETS", "ASSOCIATED_WITH", "CONTAINS_GENE"));
        map.put("phenotype", Collections.singletonList("ASSOCIATED_WITH"));
        map.put("pathway", Collections.singletonList("CONTAINS_GENE"));
        return map;
    }

    private static Map<String, Map<String, List<String>>> createRelationMatrix() {
        Map<String, Map<String, List<String>>> matrix = new HashMap<>();

        registerRelations(matrix, "herb", "compound", "CONTAINS_COMPOUND");
        registerRelations(matrix, "compound", "herb", "CONTAINS_COMPOUND");

        registerRelations(matrix, "herb", "disease", "TREATS_DISEASE", "ASSOCIATED_WITH");
        registerRelations(matrix, "disease", "herb", "TREATS_DISEASE", "ASSOCIATED_WITH");

        registerRelations(matrix, "herb", "syndrome", "TREATS_SYNDROME", "ASSOCIATED_WITH");
        registerRelations(matrix, "syndrome", "herb", "TREATS_SYNDROME", "ASSOCIATED_WITH");

        registerRelations(matrix, "herb", "symptom", "TREATS_SYMPTOM");
        registerRelations(matrix, "symptom", "herb", "TREATS_SYMPTOM");

        registerRelations(matrix, "herb", "prescription", "CONTAINS_HERB");
        registerRelations(matrix, "prescription", "herb", "CONTAINS_HERB");

        registerRelations(matrix, "prescription", "disease", "TREATS_DISEASE");
        registerRelations(matrix, "disease", "prescription", "TREATS_DISEASE");

        registerRelations(matrix, "prescription", "syndrome", "TREATS_SYNDROME");
        registerRelations(matrix, "syndrome", "prescription", "TREATS_SYNDROME");

        registerRelations(matrix, "prescription", "symptom", "TREATS_SYMPTOM");
        registerRelations(matrix, "symptom", "prescription", "TREATS_SYMPTOM");

        registerRelations(matrix, "compound", "gene", "TARGETS");
        registerRelations(matrix, "gene", "compound", "TARGETS");

        registerRelations(matrix, "gene", "disease", "ASSOCIATED_WITH");
        registerRelations(matrix, "disease", "gene", "ASSOCIATED_WITH");

        registerRelations(matrix, "gene", "syndrome", "ASSOCIATED_WITH");
        registerRelations(matrix, "syndrome", "gene", "ASSOCIATED_WITH");

        registerRelations(matrix, "gene", "wm_symptom", "ASSOCIATED_WITH");
        registerRelations(matrix, "wm_symptom", "gene", "ASSOCIATED_WITH");

        registerRelations(matrix, "symptom", "syndrome", "HAS_SYMPTOM");
        registerRelations(matrix, "syndrome", "symptom", "HAS_SYMPTOM");

        registerRelations(matrix, "symptom", "wm_symptom", "RELATED_TO");
        registerRelations(matrix, "wm_symptom", "symptom", "RELATED_TO");

        registerRelations(matrix, "gene", "phenotype", "ASSOCIATED_WITH");
        registerRelations(matrix, "phenotype", "gene", "ASSOCIATED_WITH");

        registerRelations(matrix, "gene", "pathway", "CONTAINS_GENE");
        registerRelations(matrix, "pathway", "gene", "CONTAINS_GENE");

        registerRelations(matrix, "disease", "syndrome", "ASSOCIATED_WITH");
        registerRelations(matrix, "syndrome", "disease", "ASSOCIATED_WITH");

        return matrix;
    }

    private static void registerRelations(Map<String, Map<String, List<String>>> matrix,
                                          String centerType,
                                          String relationType,
                                          String... relations) {
        matrix.computeIfAbsent(centerType, key -> new HashMap<>())
                .computeIfAbsent(relationType, key -> new ArrayList<>())
                .addAll(Arrays.asList(relations));
    }
}
