package com.tcmseek.ai.tools;

import com.tcmseek.ai.config.AiToolProperties;
import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.graph.TcmGraphRepository;
import com.tcmseek.ai.service.EntityNormalizeService;
import com.tcmseek.ai.service.SemanticSearchService;
import com.tcmseek.ai.service.ToolExecutionRecorder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TcmGraphTools {

    private static final Pattern HERB_NAME_SEPARATOR = Pattern.compile("以及|和|与|及|跟|、|,|，|;|；|/");

    private static final Pattern GRAPH_INTENT_SEPARATOR = Pattern.compile("[\\p{Punct}，。！？；：、（）()【】\\[\\]“”‘’\\s]+");

    private static final List<String> GRAPH_INTENT_STOP_WORDS = List.of(
            "可以治疗", "可以调理", "可以缓解", "可以改善", "可以喝", "可以吃",
            "能够治疗", "能够调理", "能够缓解", "能够改善", "能不能", "能治疗", "能调理", "能缓解", "能改善",
            "用什么", "吃什么", "喝什么", "开什么", "有什么", "有哪些", "有哪几种", "有哪", "有何", "有啥",
            "中医方剂", "中药方剂", "治疗方剂", "相关方剂", "治疗中药", "相关中药",
            "请问", "帮我", "帮忙", "查询", "查看", "列出", "找出", "检索", "搜索", "推荐", "介绍", "说说", "讲讲",
            "方剂", "方子", "药方", "处方", "经方", "中医", "中药", "中草药", "药材", "草药",
            "治疗", "调理", "缓解", "改善", "适合", "适用于", "用于", "主治", "相关", "关联", "对应",
            "症状", "证候", "证型", "哪些", "什么", "怎么", "如何", "可治", "能治", "治",
            "服用", "饮用", "喝", "吃", "用", "我想", "想", "问问", "一下", "的", "了", "吗", "么", "呢", "吧");

    private static final List<String> TOPIC_TARGET_SEMANTIC_TYPES = List.of("topic", "target");

    private static final List<String> GENERAL_SEMANTIC_TYPES = List.of(
            "prescription", "syndrome", "tcm_symptom", "wm_symptom", "disease", "herb", "topic", "target");

    private static final List<String> DISEASE_SEMANTIC_TYPES = List.of("disease");

    private static final List<String> DISEASE_PRESCRIPTION_FALLBACK_TYPES = List.of(
            "prescription", "syndrome", "wm_symptom");

    private static final List<String> CONDITION_PRESCRIPTION_SEMANTIC_TYPES = List.of(
            "disease", "wm_symptom", "tcm_symptom", "syndrome");

    private static final double TOPIC_MAX_DISTANCE = 0.45D;

    private static final double TARGET_MAX_DISTANCE = 0.38D;

    private static final Map<String, Double> GENERAL_SEMANTIC_MAX_DISTANCE = Map.of(
            "prescription", 0.42D,
            "syndrome", 0.42D,
            "tcm_symptom", 0.40D,
            "wm_symptom", 0.42D,
            "disease", 0.36D,
            "herb", 0.36D,
            "topic", 0.45D,
            "target", 0.38D);

    private static final List<String> ANTI_INFLAMMATION_TARGETS = List.of(
            "TNF", "IL6", "IL1B", "PTGS2", "NFKB1", "RELA", "CXCL8", "TLR4",
            "MAPK1", "MAPK3", "JUN", "STAT3", "NOS2", "NLRP3", "CCL2");

    private static final List<String> MACROPHAGE_INFLAMMATION_TARGETS = List.of(
            "TNF", "IL6", "IL1B", "TLR4", "NFKB1", "RELA", "MAPK1", "MAPK3",
            "JUN", "STAT3", "PTGS2", "NOS2", "NLRP3", "CCL2", "CXCL8", "IL10");

    private final TcmGraphRepository graphRepository;

    private final EntityNormalizeService entityNormalizeService;

    private final ToolExecutionRecorder recorder;

    private final AiToolProperties toolProperties;

    private final SemanticSearchService semanticSearchService;

    public TcmGraphTools(TcmGraphRepository graphRepository,
                         EntityNormalizeService entityNormalizeService,
                         ToolExecutionRecorder recorder,
                         AiToolProperties toolProperties,
                         SemanticSearchService semanticSearchService) {
        this.graphRepository = graphRepository;
        this.entityNormalizeService = entityNormalizeService;
        this.recorder = recorder;
        this.toolProperties = toolProperties;
        this.semanticSearchService = semanticSearchService;
    }

    @Tool(description = "查询某味中药包含的化合物。参数必须是中药中文名，例如 人参、黄芪。")
    public GraphToolResult findHerbCompounds(
            @ToolParam(description = "中药中文名") String herbName) {
        String normalizedHerbName = entityNormalizeService.normalizeHerb(herbName);
        String cypher = ""
                + "MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "RETURN DISTINCT "
                + "coalesce(c.name_zh, c.name, c.inchikey) AS compound, "
                + "c.inchikey AS inchikey, "
                + "c.molecular_formula AS formula "
                + "LIMIT $limit";

        Map<String, Object> params = new HashMap<>();
        params.put("herbName", normalizedHerbName);
        params.put("originalHerbName", herbName);
        params.put("limit", toolLimit());
        GraphToolResult result = GraphToolResult.of("herb_compounds", graphRepository.query(cypher, params));
        recorder.record("findHerbCompounds", params, result);
        return result;
    }

    @Tool(description = "一次性查询某味中药包含的化合物，以及这些化合物直接作用的靶点或靶标。适合用户问“人参含有哪些化合物，这些化合物作用哪些靶标”“人参成分对应哪些靶点”。参数必须是中药中文名。")
    public GraphToolResult findHerbCompoundTargets(
            @ToolParam(description = "中药中文名，例如 人参、黄芪") String herbName) {
        String normalizedHerbName = entityNormalizeService.normalizeHerb(herbName);
        String cypher = ""
                + "MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound)-[:TARGETS]->(t:Target) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "RETURN DISTINCT "
                + "h.herb_name_zh AS herb, "
                + "coalesce(c.name_zh, c.name, c.inchikey) AS compound, "
                + "c.inchikey AS inchikey, "
                + "c.molecular_formula AS formula, "
                + "t.symbol AS target, "
                + "t.tcm_tar_id AS targetId "
                + "ORDER BY compound, target "
                + "LIMIT $limit";

        Map<String, Object> params = new HashMap<>();
        params.put("herbName", normalizedHerbName);
        params.put("originalHerbName", herbName);
        params.put("limit", toolLimit());
        GraphToolResult result = GraphToolResult.of("herb_compound_targets", graphRepository.query(cypher, params));
        recorder.record("findHerbCompoundTargets", params, result);
        return result;
    }

    @Tool(description = "查询两味或多味中药共同包含的化合物或共同活性成分。适合用户问“人参和黄芪有什么共同化合物”“人参、黄芪和甘草有哪些共同成分”。参数使用逗号、顿号或“和”分隔。")
    public GraphToolResult findCommonCompounds(
            @ToolParam(description = "两味或多味中药中文名，例如 人参、黄芪、甘草") String herbNames) {
        List<String> normalizedHerbs = normalizeHerbNames(herbNames);
        Map<String, Object> params = new HashMap<>();
        params.put("herbNames", normalizedHerbs);
        params.put("originalHerbNames", herbNames);
        params.put("limit", toolLimit());
        if (normalizedHerbs.size() < 2) {
            GraphToolResult result = GraphToolResult.of("common_compounds", List.of());
            recorder.record("findCommonCompounds", params, result);
            return result;
        }

        String cypher = ""
                + "MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh IN $herbNames "
                + "WITH c, collect(DISTINCT h.herb_name_zh) AS matchedHerbs "
                + "WHERE size(matchedHerbs) = size($herbNames) "
                + "RETURN DISTINCT "
                + "coalesce(c.name_zh, c.name, c.inchikey) AS compound, "
                + "c.inchikey AS inchikey, "
                + "c.molecular_formula AS formula, "
                + "matchedHerbs AS herbs "
                + "ORDER BY compound "
                + "LIMIT $limit";

        GraphToolResult result = GraphToolResult.of("common_compounds", graphRepository.query(cypher, params));
        recorder.record("findCommonCompounds", params, result);
        return result;
    }

    @Tool(description = "综合查询某味中药的功效主治、关联症状、关联证候、直接治疗疾病；适合用户问“人参可以治什么病”“人参有什么功效”“人参主治什么”。如果没有直接疾病关系，会返回包含该中药的方剂关联疾病作为间接证据。")
    public GraphToolResult findHerbClinicalUse(
            @ToolParam(description = "中药中文名，例如 人参、黄芪") String herbName) {
        Map<String, Object> params = baseParams("herbName", entityNormalizeService.normalizeHerb(herbName));
        params.put("originalHerbName", herbName);
        List<Map<String, Object>> rows = new ArrayList<>();

        String profileCypher = ""
                + "MATCH (h) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "RETURN DISTINCT "
                + "'herb_profile' AS category, "
                + "h.herb_name_zh AS herb, "
                + "coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId, "
                + "h.efficacy_zh AS efficacy, "
                + "h.indications_zh AS indications, "
                + "h.nature_taste_zh AS natureTaste, "
                + "h.meridian_zh AS meridians, "
                + "h.latin_name AS latinName, "
                + "h.english_name AS englishName "
                + "LIMIT 5";
        rows.addAll(graphRepository.query(profileCypher, params));

        String symptomCypher = ""
                + "MATCH (h)-[:TREATS_SYMPTOM]->(s) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "RETURN DISTINCT "
                + "'symptom' AS category, "
                + "coalesce(s.symptom_name_zh, s.symptom_name) AS name, "
                + "s.tcm_symptom_id AS id "
                + "ORDER BY name "
                + "LIMIT 30";
        rows.addAll(graphRepository.query(symptomCypher, params));

        String syndromeCypher = ""
                + "MATCH (h)-[:TREATS_SYNDROME]->(s:Syndrome) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "RETURN DISTINCT "
                + "'syndrome' AS category, "
                + "s.syndrome_name_zh AS name, "
                + "s.tcm_syndrome_id AS id "
                + "ORDER BY name "
                + "LIMIT 30";
        rows.addAll(graphRepository.query(syndromeCypher, params));

        String directDiseaseCypher = ""
                + "MATCH (h)-[:TREATS_DISEASE]->(d:Disease) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "RETURN DISTINCT "
                + "'direct_disease' AS category, "
                + "d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "'direct_herb_disease' AS evidenceType "
                + "ORDER BY disease "
                + "LIMIT 30";
        List<Map<String, Object>> directDiseases = graphRepository.query(directDiseaseCypher, params);
        rows.addAll(directDiseases);

        if (directDiseases.isEmpty()) {
            String indirectDiseaseCypher = ""
                    + "MATCH (h)<-[:CONTAINS_HERB]-(p:Prescription)-[:TREATS_DISEASE]->(d:Disease) "
                    + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                    + "AND h.herb_name_zh = $herbName "
                    + "WITH d, count(DISTINCT p) AS evidenceCount, collect(DISTINCT p.name_zh)[0..5] AS evidencePrescriptions "
                    + "RETURN DISTINCT "
                    + "'indirect_prescription_disease' AS category, "
                    + "d.disease_name AS disease, "
                    + "d.disease_id AS diseaseId, "
                    + "evidenceCount AS evidenceCount, "
                    + "evidencePrescriptions AS evidencePrescriptions, "
                    + "'prescription_contains_herb' AS evidenceType "
                    + "ORDER BY evidenceCount DESC, disease "
                    + "LIMIT 30";
            rows.addAll(graphRepository.query(indirectDiseaseCypher, params));
        }

        GraphToolResult result = GraphToolResult.of("herb_clinical_use", rows);
        recorder.record("findHerbClinicalUse", params, result);
        return result;
    }

    @Tool(description = "查询某味中药治疗或关联的疾病。优先返回直接中药-疾病关系；若没有直接关系，返回包含该中药的方剂所关联疾病作为间接证据。参数必须是中药中文名。")
    public GraphToolResult findHerbDiseases(
            @ToolParam(description = "中药中文名") String herbName) {
        String normalizedHerbName = entityNormalizeService.normalizeHerb(herbName);
        String cypher = ""
                + "MATCH (h)-[:TREATS_DISEASE]->(d:Disease) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "RETURN DISTINCT d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "'direct_herb_disease' AS evidenceType "
                + "LIMIT $limit";

        Map<String, Object> params = baseParams("herbName", normalizedHerbName);
        params.put("originalHerbName", herbName);
        List<Map<String, Object>> rows = graphRepository.query(cypher, params);
        if (rows.isEmpty()) {
            String indirectCypher = ""
                    + "MATCH (p:Prescription)-[:CONTAINS_HERB]->(h) "
                    + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                    + "AND h.herb_name_zh = $herbName "
                    + "WITH DISTINCT p, h "
                    + "MATCH (p)-[:TREATS_DISEASE]->(d:Disease) "
                    + "WITH d, collect(DISTINCT p.name_zh)[0..5] AS prescriptions "
                    + "RETURN DISTINCT d.disease_name AS disease, "
                    + "d.disease_id AS diseaseId, "
                    + "prescriptions AS evidencePrescriptions, "
                    + "'prescription_contains_herb' AS evidenceType "
                    + "ORDER BY disease "
                    + "LIMIT $limit";
            rows = graphRepository.query(indirectCypher, params);
        }
        GraphToolResult result = GraphToolResult.of("herb_diseases", rows);
        recorder.record("findHerbDiseases", params, result);
        return result;
    }

    @Tool(description = "查询两味或多味中药通过化合物共同作用的靶点。适合用户问“人参和黄芪共同靶点”“人参、黄芪、甘草共同作用靶点”。参数使用逗号、顿号或“和”分隔。")
    public GraphToolResult findCommonTargets(
            @ToolParam(description = "两味或多味中药中文名，例如 人参、黄芪、甘草") String herbNames) {
        List<String> normalizedHerbs = normalizeHerbNames(herbNames);
        Map<String, Object> params = new HashMap<>();
        params.put("herbNames", normalizedHerbs);
        params.put("originalHerbNames", herbNames);
        params.put("limit", toolLimit());
        if (normalizedHerbs.size() < 2) {
            GraphToolResult result = GraphToolResult.of("common_targets", List.of());
            recorder.record("findCommonTargets", params, result);
            return result;
        }

        String cypher = ""
                + "MATCH (h)-[:CONTAINS_COMPOUND]->(:Compound)-[:TARGETS]->(t:Target) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh IN $herbNames "
                + "WITH t, collect(DISTINCT h.herb_name_zh) AS matchedHerbs "
                + "WHERE size(matchedHerbs) = size($herbNames) "
                + "RETURN DISTINCT "
                + "t.symbol AS target, "
                + "t.tcm_tar_id AS targetId, "
                + "matchedHerbs AS herbs "
                + "ORDER BY target "
                + "LIMIT $limit";

        GraphToolResult result = GraphToolResult.of("common_targets", graphRepository.query(cypher, params));
        recorder.record("findCommonTargets", params, result);
        return result;
    }

    @Tool(description = "查询某个方剂包含的中药。参数必须是方剂中文名，例如 六味地黄丸。")
    public GraphToolResult findPrescriptionHerbs(
            @ToolParam(description = "方剂中文名") String prescriptionName) {
        String normalizedPrescriptionName = entityNormalizeService.normalizePrescription(prescriptionName);
        String cypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName})-[:CONTAINS_HERB]->(h) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "RETURN DISTINCT h.herb_name_zh AS herb "
                + "LIMIT $limit";

        Map<String, Object> params = new HashMap<>();
        params.put("prescriptionName", normalizedPrescriptionName);
        params.put("originalPrescriptionName", prescriptionName);
        params.put("limit", toolLimit());
        GraphToolResult result = GraphToolResult.of("prescription_herbs", graphRepository.query(cypher, params));
        recorder.record("findPrescriptionHerbs", params, result);
        return result;
    }

    @Tool(description = "综合查询某个方剂的组成中药、功效主治、关联疾病、关联证候、关联症状；适合用户问“六味地黄丸治什么”“某方剂有什么作用”。参数必须是方剂中文名。")
    public GraphToolResult findPrescriptionClinicalUse(
            @ToolParam(description = "方剂中文名，例如 六味地黄丸") String prescriptionName) {
        Map<String, Object> params = baseParams("prescriptionName", entityNormalizeService.normalizePrescription(prescriptionName));
        params.put("originalPrescriptionName", prescriptionName);
        List<Map<String, Object>> rows = new ArrayList<>();

        String profileCypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName}) "
                + "RETURN DISTINCT "
                + "'prescription_profile' AS category, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "p.effects_zh AS effects, "
                + "p.indications_zh AS indications, "
                + "p.source AS source "
                + "LIMIT 5";
        rows.addAll(graphRepository.query(profileCypher, params));

        String herbsCypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName})-[:CONTAINS_HERB]->(h) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "RETURN DISTINCT "
                + "'herb' AS category, "
                + "h.herb_name_zh AS herb, "
                + "coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId "
                + "ORDER BY herb "
                + "LIMIT 50";
        rows.addAll(graphRepository.query(herbsCypher, params));

        String diseasesCypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_DISEASE]->(d:Disease) "
                + "RETURN DISTINCT "
                + "'disease' AS category, "
                + "d.disease_name AS disease, "
                + "d.disease_id AS diseaseId "
                + "ORDER BY disease "
                + "LIMIT 30";
        rows.addAll(graphRepository.query(diseasesCypher, params));

        String syndromesCypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYNDROME]->(s:Syndrome) "
                + "RETURN DISTINCT "
                + "'syndrome' AS category, "
                + "s.syndrome_name_zh AS name, "
                + "s.tcm_syndrome_id AS id "
                + "ORDER BY name "
                + "LIMIT 30";
        rows.addAll(graphRepository.query(syndromesCypher, params));

        String symptomsCypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYMPTOM]->(s) "
                + "RETURN DISTINCT "
                + "'symptom' AS category, "
                + "coalesce(s.symptom_name_zh, s.symptom_name) AS name, "
                + "s.tcm_symptom_id AS id "
                + "ORDER BY name "
                + "LIMIT 30";
        rows.addAll(graphRepository.query(symptomsCypher, params));

        GraphToolResult result = GraphToolResult.of("prescription_clinical_use", rows);
        recorder.record("findPrescriptionClinicalUse", params, result);
        return result;
    }

    @Tool(description = "查询某个方剂治疗或关联的症状。参数必须是方剂中文名。")
    public GraphToolResult findPrescriptionSymptoms(
            @ToolParam(description = "方剂中文名") String prescriptionName) {
        String normalizedPrescriptionName = entityNormalizeService.normalizePrescription(prescriptionName);
        String cypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYMPTOM]->(s:Symptom) "
                + "RETURN DISTINCT coalesce(s.symptom_name_zh, s.symptom_name) AS symptom "
                + "LIMIT $limit";

        Map<String, Object> params = baseParams("prescriptionName", normalizedPrescriptionName);
        params.put("originalPrescriptionName", prescriptionName);
        GraphToolResult result = GraphToolResult.of("prescription_symptoms", graphRepository.query(cypher, params));
        recorder.record("findPrescriptionSymptoms", params, result);
        return result;
    }

    @Tool(description = "查询某个方剂治疗或关联的证候。参数必须是方剂中文名。")
    public GraphToolResult findPrescriptionSyndromes(
            @ToolParam(description = "方剂中文名") String prescriptionName) {
        String normalizedPrescriptionName = entityNormalizeService.normalizePrescription(prescriptionName);
        String cypher = ""
                + "MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYNDROME]->(s:Syndrome) "
                + "RETURN DISTINCT s.syndrome_name_zh AS syndrome "
                + "LIMIT $limit";

        Map<String, Object> params = baseParams("prescriptionName", normalizedPrescriptionName);
        params.put("originalPrescriptionName", prescriptionName);
        GraphToolResult result = GraphToolResult.of("prescription_syndromes", graphRepository.query(cypher, params));
        recorder.record("findPrescriptionSyndromes", params, result);
        return result;
    }

    @Tool(description = "查询某个疾病关联的靶点。参数必须是疾病中文名或图谱中的疾病名。")
    public GraphToolResult findDiseaseTargets(
            @ToolParam(description = "疾病名称") String diseaseName) {
        Map<String, Object> params = baseParams("diseaseName", diseaseName);
        params.put("diseaseQuery", entityNormalizeService.normalizeDisease(diseaseName));
        if (params.get("diseaseQuery").toString().isBlank()) {
            GraphToolResult result = GraphToolResult.of("disease_targets", List.of());
            recorder.record("findDiseaseTargets", params, result);
            return result;
        }

        String cypher = ""
                + "WITH toLower($diseaseQuery) AS q "
                + "MATCH (d:Disease)-[:ASSOCIATED_WITH]-(t:Target) "
                + "WHERE toLower(d.disease_name) = q "
                + "OR toLower(d.disease_name) CONTAINS q "
                + "WITH DISTINCT d, t, q, "
                + "CASE "
                + "WHEN toLower(d.disease_name) = q THEN 0 "
                + "WHEN toLower(d.disease_name) STARTS WITH q THEN 1 "
                + "ELSE 2 END AS matchScore "
                + "RETURN DISTINCT d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "t.symbol AS target, "
                + "t.tcm_tar_id AS targetId, "
                + "matchScore AS matchScore "
                + "ORDER BY matchScore, disease, target "
                + "LIMIT $limit";

        GraphToolResult result = GraphToolResult.of("disease_targets", graphRepository.query(cypher, params));
        recorder.record("findDiseaseTargets", params, result);
        return result;
    }

    @Tool(description = "查询某个疾病关联的中药。适合用户问“糖尿病有哪些相关中药”“哪些中药治疗糖尿病”。疾病参数可以是中文名或图谱英文名。")
    public GraphToolResult findDiseaseHerbs(
            @ToolParam(description = "疾病名称，例如 糖尿病 或 diabetes mellitus") String diseaseName) {
        Map<String, Object> params = baseParams("diseaseName", diseaseName);
        params.put("diseaseQuery", entityNormalizeService.normalizeDisease(diseaseName));
        if (params.get("diseaseQuery").toString().isBlank()) {
            GraphToolResult result = GraphToolResult.of("disease_herbs", List.of());
            recorder.record("findDiseaseHerbs", params, result);
            return result;
        }

        String cypher = ""
                + "WITH toLower($diseaseQuery) AS q "
                + "MATCH (h)-[:TREATS_DISEASE]->(d:Disease) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND (toLower(d.disease_name) = q OR toLower(d.disease_name) CONTAINS q) "
                + "WITH DISTINCT h, d, q, "
                + "CASE "
                + "WHEN toLower(d.disease_name) = q THEN 0 "
                + "WHEN toLower(d.disease_name) STARTS WITH q THEN 1 "
                + "ELSE 2 END AS matchScore "
                + "RETURN DISTINCT "
                + "d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "h.herb_name_zh AS herb, "
                + "coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId, "
                + "matchScore AS matchScore "
                + "ORDER BY matchScore, disease, herb "
                + "LIMIT $limit";

        GraphToolResult result = GraphToolResult.of("disease_herbs", graphRepository.query(cypher, params));
        recorder.record("findDiseaseHerbs", params, result);
        return result;
    }

    @Tool(description = "查询某个疾病关联的方剂。适合用户问“糖尿病有哪些相关方剂”“哪些方剂治疗糖尿病”。疾病参数可以是中文名或图谱英文名。")
    public GraphToolResult findDiseasePrescriptions(
            @ToolParam(description = "疾病名称，例如 糖尿病 或 diabetes mellitus") String diseaseName) {
        Map<String, Object> params = baseParams("diseaseName", diseaseName);
        params.put("diseaseQuery", entityNormalizeService.normalizeDisease(diseaseName));
        if (params.get("diseaseQuery").toString().isBlank()) {
            GraphToolResult result = GraphToolResult.of("disease_prescriptions", List.of());
            recorder.record("findDiseasePrescriptions", params, result);
            return result;
        }

        String cypher = ""
                + "WITH toLower($diseaseQuery) AS q "
                + "MATCH (p:Prescription)-[:TREATS_DISEASE]->(d:Disease) "
                + "WHERE toLower(d.disease_name) = q OR toLower(d.disease_name) CONTAINS q "
                + "WITH DISTINCT p, d, q, "
                + "CASE "
                + "WHEN toLower(d.disease_name) = q THEN 0 "
                + "WHEN toLower(d.disease_name) STARTS WITH q THEN 1 "
                + "ELSE 2 END AS matchScore "
                + "RETURN DISTINCT "
                + "d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "matchScore AS matchScore "
                + "ORDER BY matchScore, disease, prescription "
                + "LIMIT $limit";

        GraphToolResult result = GraphToolResult.of("disease_prescriptions", graphRepository.query(cypher, params));
        if (result.getItems().isEmpty()) {
            result = findDiseasePrescriptionsBySemanticDisease(diseaseName, params);
        }
        if (result.getItems().isEmpty()) {
            result = findDiseasePrescriptionsBySemanticFallback(diseaseName, params);
        }
        recorder.record("findDiseasePrescriptions", params, result);
        return result;
    }

    public GraphToolResult findDiseasePrescriptionsExact(String diseaseName) {
        Map<String, Object> params = baseParams("diseaseName", diseaseName);
        params.put("diseaseQuery", entityNormalizeService.normalizeDisease(diseaseName));
        if (params.get("diseaseQuery").toString().isBlank()) {
            GraphToolResult result = GraphToolResult.of("disease_prescriptions", List.of());
            recorder.record("findDiseasePrescriptionsExact", params, result);
            return result;
        }

        String cypher = ""
                + "WITH toLower($diseaseQuery) AS q "
                + "MATCH (p:Prescription)-[:TREATS_DISEASE]->(d:Disease) "
                + "WHERE toLower(d.disease_name) = q OR toLower(d.disease_name) CONTAINS q "
                + "WITH DISTINCT p, d, q, "
                + "CASE "
                + "WHEN toLower(d.disease_name) = q THEN 0 "
                + "WHEN toLower(d.disease_name) STARTS WITH q THEN 1 "
                + "ELSE 2 END AS matchScore "
                + "RETURN DISTINCT "
                + "d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "matchScore AS matchScore "
                + "ORDER BY matchScore, disease, prescription "
                + "LIMIT $limit";

        GraphToolResult result = GraphToolResult.of("disease_prescriptions", graphRepository.query(cypher, params));
        recorder.record("findDiseasePrescriptionsExact", params, result);
        return result;
    }

    @Tool(description = "Query prescriptions that contain a given herb and treat or mention a given disease/condition. Uses Neo4j exact graph/text matching only; no vector search.")
    public GraphToolResult findHerbDiseasePrescriptions(
            @ToolParam(description = "Herb Chinese name, for example \u4eba\u53c2 or \u9ec4\u82aa") String herbName,
            @ToolParam(description = "Disease or condition name, for example \u766b\u75eb, \u5bd2, epilepsy, or diabetes mellitus") String diseaseName) {
        String normalizedHerbName = entityNormalizeService.normalizeHerb(herbName);
        String diseaseQuery = entityNormalizeService.normalizeDisease(diseaseName);
        String conditionQuery = diseaseName == null ? "" : diseaseName.trim();
        Map<String, Object> params = baseParams("diseaseName", diseaseName);
        params.put("herbName", normalizedHerbName);
        params.put("originalHerbName", herbName);
        params.put("diseaseQuery", diseaseQuery);
        params.put("conditionQuery", conditionQuery);
        List<String> conditionQueries = List.of(conditionQuery, diseaseQuery).stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        params.put("conditionQueries", conditionQueries);
        if (!StringUtils.hasText(normalizedHerbName) || conditionQueries.isEmpty()) {
            GraphToolResult result = GraphToolResult.of("herb_disease_prescriptions", List.of());
            recorder.record("findHerbDiseasePrescriptions", params, result);
            return result;
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        if (StringUtils.hasText(diseaseQuery)) {
            String cypher = ""
                    + "WITH toLower($diseaseQuery) AS q "
                    + "MATCH (p:Prescription)-[:CONTAINS_HERB]->(h) "
                    + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                    + "AND h.herb_name_zh = $herbName "
                    + "MATCH (p)-[:TREATS_DISEASE]->(d:Disease) "
                    + "WHERE toLower(d.disease_name) = q "
                    + "OR toLower(d.disease_name) CONTAINS q "
                    + "OR q CONTAINS toLower(d.disease_name) "
                    + "WITH DISTINCT p, h, d, q, "
                    + "CASE "
                    + "WHEN toLower(d.disease_name) = q THEN 0 "
                    + "WHEN toLower(d.disease_name) STARTS WITH q THEN 1 "
                    + "WHEN q CONTAINS toLower(d.disease_name) THEN 2 "
                    + "ELSE 3 END AS matchScore "
                    + "OPTIONAL MATCH (p)-[:CONTAINS_HERB]->(allHerb) "
                    + "WHERE allHerb IS NULL OR any(label IN labels(allHerb) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                    + "WITH p, h, d, matchScore, collect(DISTINCT allHerb.herb_name_zh)[0..12] AS herbs "
                    + "RETURN DISTINCT "
                    + "'disease_relation' AS category, "
                    + "h.herb_name_zh AS herb, "
                    + "d.disease_name AS disease, "
                    + "d.disease_id AS diseaseId, "
                    + "p.name_zh AS prescription, "
                    + "p.tcm_prescription_id AS prescriptionId, "
                    + "p.effects_zh AS effects, "
                    + "p.indications_zh AS indications, "
                    + "herbs AS herbs, "
                    + "'prescription_contains_herb_treats_disease' AS evidenceType, "
                    + "matchScore AS matchScore "
                    + "ORDER BY matchScore, disease, prescription "
                    + "LIMIT $limit";
            rows.addAll(graphRepository.query(cypher, params));
        }

        if (rows.isEmpty()) {
            String textCypher = ""
                    + "MATCH (p:Prescription)-[:CONTAINS_HERB]->(h) "
                    + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                    + "AND h.herb_name_zh = $herbName "
                    + "AND any(conditionQuery IN $conditionQueries WHERE "
                    + "coalesce(p.effects_zh, '') CONTAINS conditionQuery "
                    + "OR coalesce(p.indications_zh, '') CONTAINS conditionQuery) "
                    + "OPTIONAL MATCH (p)-[:CONTAINS_HERB]->(allHerb) "
                    + "WHERE allHerb IS NULL OR any(label IN labels(allHerb) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                    + "WITH p, h, collect(DISTINCT allHerb.herb_name_zh)[0..12] AS herbs "
                    + "RETURN DISTINCT "
                    + "'prescription_text_condition' AS category, "
                    + "h.herb_name_zh AS herb, "
                    + "$conditionQuery AS disease, "
                    + "p.name_zh AS prescription, "
                    + "p.tcm_prescription_id AS prescriptionId, "
                    + "p.effects_zh AS effects, "
                    + "p.indications_zh AS indications, "
                    + "herbs AS herbs, "
                    + "'prescription_contains_herb_condition_text' AS evidenceType, "
                    + "4 AS matchScore "
                    + "ORDER BY prescription "
                    + "LIMIT $limit";
            rows.addAll(graphRepository.query(textCypher, params));
        }

        GraphToolResult result = GraphToolResult.of("herb_disease_prescriptions", rows);
        recorder.record("findHerbDiseasePrescriptions", params, result);
        return result;
    }

    private GraphToolResult findDiseasePrescriptionsBySemanticDisease(String diseaseName, Map<String, Object> params) {
        String diseaseQuery = String.valueOf(params.getOrDefault("diseaseQuery", ""));
        String semanticQuery = diseaseName + " " + diseaseQuery + " disease";
        List<SemanticSearchService.SemanticAnchor> candidates = semanticSearchService.search(
                semanticQuery,
                DISEASE_SEMANTIC_TYPES,
                Math.min(10, Math.max(5, toolLimit())));
        List<SemanticSearchService.SemanticAnchor> anchors = acceptedGeneralSemanticAnchors(candidates);
        params.put("detectedEntityType", "disease");
        params.put("semanticDiseaseQuery", semanticQuery);
        params.put("semanticDiseaseQueryTypes", DISEASE_SEMANTIC_TYPES);
        params.put("semanticDiseaseCandidateAnchors", anchorSummaries(candidates, 5));
        params.put("semanticDiseaseAnchors", anchorSummaries(anchors, 5));
        params.put("semanticDiseaseCandidateCount", candidates.size());
        params.put("semanticDiseaseAnchorCount", anchors.size());

        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> routedKeys = new LinkedHashSet<>();
        for (SemanticSearchService.SemanticAnchor anchor : anchors.stream().limit(3).toList()) {
            String key = anchor.entityType() + ":" + anchor.name();
            if (routedKeys.add(key)) {
                rows.addAll(annotateSemanticRows(
                        querySemanticDiseasePrescriptions(anchor, Math.max(3, Math.min(12, toolLimit()))),
                        anchor));
            }
        }
        params.put("routedDiseaseAnchorCount", routedKeys.size());
        if (rows.isEmpty()) {
            params.put("semanticDiseaseDirectRowCount", 0);
            if (!anchors.isEmpty()) {
                params.put("finalEvidenceLevel", "no_direct_disease_prescription_relation");
                return GraphToolResult.of(
                        "disease_prescriptions_no_direct_relation",
                        noDirectDiseasePrescriptionRows(anchors));
            }
            return GraphToolResult.of("disease_prescriptions", List.of());
        }
        params.put("semanticDiseaseDirectRowCount", rows.size());
        params.put("finalEvidenceLevel", "semantic_disease_direct_relation");
        return GraphToolResult.of("disease_prescriptions_semantic_disease", rows);
    }

    private GraphToolResult findDiseasePrescriptionsBySemanticFallback(String diseaseName, Map<String, Object> params) {
        String diseaseQuery = String.valueOf(params.getOrDefault("diseaseQuery", ""));
        String semanticQuery = diseaseName + " " + diseaseQuery + " \u6cbb\u7597 \u65b9\u5242 prescription formula";
        List<SemanticSearchService.SemanticAnchor> candidates = semanticSearchService.search(
                semanticQuery,
                DISEASE_PRESCRIPTION_FALLBACK_TYPES,
                Math.min(20, Math.max(8, toolLimit())));
        List<SemanticSearchService.SemanticAnchor> anchors = acceptedGeneralSemanticAnchors(candidates);
        params.put("semanticQuery", semanticQuery);
        params.put("semanticQueryTypes", DISEASE_PRESCRIPTION_FALLBACK_TYPES);
        params.put("semanticFallbackQuery", semanticQuery);
        params.put("semanticFallbackQueryTypes", DISEASE_PRESCRIPTION_FALLBACK_TYPES);
        params.put("semanticCandidateAnchors", anchorSummaries(candidates, 8));
        params.put("semanticAnchors", anchorSummaries(anchors, 8));
        params.put("semanticCandidateCount", candidates.size());
        params.put("semanticAnchorCount", anchors.size());
        List<Map<String, Object>> semanticRows = new ArrayList<>();
        Set<String> routedKeys = new LinkedHashSet<>();
        for (SemanticSearchService.SemanticAnchor anchor : anchors.stream().limit(6).toList()) {
            String key = anchor.entityType() + ":" + anchor.name();
            if (routedKeys.add(key)) {
                semanticRows.addAll(routeSemanticAnchor(diseaseName, anchor, Math.max(3, Math.min(12, toolLimit()))));
            }
        }
        params.put("routedAnchorCount", routedKeys.size());
        if (semanticRows.isEmpty()) {
            return GraphToolResult.of("disease_prescriptions", List.of());
        }
        params.put("finalEvidenceLevel", "semantic_prescription_fallback");
        return GraphToolResult.of("disease_prescriptions_semantic_fallback", semanticRows);
    }

    @Tool(description = "查询某个证候包含或关联的症状。参数必须是证候中文名。")
    public GraphToolResult findSyndromeSymptoms(
            @ToolParam(description = "证候中文名") String syndromeName) {
        String normalizedSyndromeName = entityNormalizeService.normalizeSyndrome(syndromeName);
        String cypher = ""
                + "MATCH (s:Syndrome {syndrome_name_zh: $syndromeName})-[:HAS_SYMPTOM]->(sym:Symptom) "
                + "RETURN DISTINCT coalesce(sym.symptom_name_zh, sym.symptom_name) AS symptom "
                + "LIMIT $limit";

        Map<String, Object> params = baseParams("syndromeName", normalizedSyndromeName);
        params.put("originalSyndromeName", syndromeName);
        GraphToolResult result = GraphToolResult.of("syndrome_symptoms", graphRepository.query(cypher, params));
        recorder.record("findSyndromeSymptoms", params, result);
        return result;
    }

    @Tool(description = "查询某个化合物作用的靶点。参数可以是 InChIKey、化合物中文名或英文名。")
    public GraphToolResult findCompoundTargets(
            @ToolParam(description = "化合物标识，可以是 InChIKey、中文名或英文名") String compound) {
        String cypher = ""
                + "MATCH (c:Compound)-[:TARGETS]->(t:Target) "
                + "WHERE c.inchikey = $compound OR c.name = $compound OR c.name_zh = $compound "
                + "RETURN DISTINCT t.symbol AS target "
                + "LIMIT $limit";

        Map<String, Object> params = baseParams("compound", compound);
        GraphToolResult result = GraphToolResult.of("compound_targets", graphRepository.query(cypher, params));
        recorder.record("findCompoundTargets", params, result);
        return result;
    }

    @Tool(description = "查询某个通路包含的靶点或基因。参数可以是通路名称或通路ID。")
    public GraphToolResult findPathwayTargets(
            @ToolParam(description = "通路名称或通路ID") String pathwayName) {
        String normalizedPathwayName = entityNormalizeService.normalizePathway(pathwayName);
        String cypher = ""
                + "MATCH (p:Pathway)-[:CONTAINS_GENE]->(t:Target) "
                + "WHERE p.name = $pathwayName OR p.pathway_id = $pathwayName "
                + "RETURN DISTINCT t.symbol AS target "
                + "LIMIT $limit";

        Map<String, Object> params = baseParams("pathwayName", normalizedPathwayName);
        params.put("originalPathwayName", pathwayName);
        GraphToolResult result = GraphToolResult.of("pathway_targets", graphRepository.query(cypher, params));
        recorder.record("findPathwayTargets", params, result);
        return result;
    }

    @Tool(description = "查询某个医案使用的方剂。参数必须是医案ID。")
    public GraphToolResult findMedicalCasePrescriptions(
            @ToolParam(description = "医案ID") String caseId) {
        String cypher = ""
                + "MATCH (m:MedicalCase {med_case_id: $caseId})-[:USES_PRESCRIPTION]->(p:Prescription) "
                + "RETURN DISTINCT p.name_zh AS prescription "
                + "LIMIT $limit";

        Map<String, Object> params = baseParams("caseId", caseId);
        GraphToolResult result = GraphToolResult.of("medicalcase_prescriptions", graphRepository.query(cypher, params));
        recorder.record("findMedicalCasePrescriptions", params, result);
        return result;
    }

    @Tool(description = "General semantic router for vague, folk, symptom-like, syndrome-like, or broad TCM questions when exact graph entity names are uncertain. It first searches pgvector for prescriptions, herbs, diseases, syndromes, symptoms, targets, and topics, then routes accepted anchors to Neo4j. Prefer this for questions like 上火吃什么方剂, 口舌生疮用什么方, 怕冷乏力可能相关中药, or any natural-language query that may not exactly match a graph disease name.")
    public GraphToolResult findBySemanticIntent(
            @ToolParam(description = "Original full user question. Keep all symptoms, folk terms, disease names, herbs, and purpose words.") String question) {
        Map<String, Object> params = new HashMap<>();
        params.put("question", question);
        params.put("limit", toolLimit());
        params.put("semanticQueryTypes", GENERAL_SEMANTIC_TYPES);
        params.put("semanticMaxDistanceByType", GENERAL_SEMANTIC_MAX_DISTANCE);

        if (asksPrescriptionForCondition(question)) {
            GraphToolResult conditionResult = findPrescriptionsBySemanticCondition(question, params);
            if (!conditionResult.getItems().isEmpty()) {
                recorder.record("findBySemanticIntent", params, conditionResult);
                return conditionResult;
            }
        }

        List<SemanticSearchService.SemanticAnchor> candidates = semanticSearchService.search(
                question,
                GENERAL_SEMANTIC_TYPES,
                Math.min(40, Math.max(12, toolLimit())));
        List<SemanticSearchService.SemanticAnchor> anchors = acceptedGeneralSemanticAnchors(candidates);
        params.put("semanticCandidateAnchors", anchorSummaries(candidates, 12));
        params.put("semanticAnchors", anchorSummaries(anchors, 12));
        params.put("semanticCandidateCount", candidates.size());
        params.put("semanticAnchorCount", anchors.size());

        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> routedKeys = new LinkedHashSet<>();
        int maxAnchors = Math.min(8, anchors.size());
        int perAnchorLimit = Math.max(3, Math.min(12, toolLimit()));
        for (int i = 0; i < maxAnchors && rows.size() < toolLimit(); i++) {
            SemanticSearchService.SemanticAnchor anchor = anchors.get(i);
            String key = anchor.entityType() + ":" + anchor.name();
            if (!routedKeys.add(key)) {
                continue;
            }
            rows.addAll(routeSemanticAnchor(question, anchor, perAnchorLimit));
        }
        params.put("routedAnchorCount", routedKeys.size());

        GraphToolResult result = GraphToolResult.of("semantic_intent", rows);
        recorder.record("findBySemanticIntent", params, result);
        return result;
    }

    @Tool(description = "通用 Neo4j 精确关系检索。用于展示页等不使用向量检索的自然语言中医药关系问题；从完整问题中抽取候选实体词，再匹配疾病、症状、证候与方剂的直接关系和处方文本。不要使用向量数据库。")
    public GraphToolResult findByGraphIntentExact(
            @ToolParam(description = "用户完整问题，保留病症、症状、证候、方剂、中药等原始表达。") String question) {
        List<String> terms = graphIntentTerms(question);
        List<String> textTerms = terms.stream()
                .filter(this::usableGraphTextTerm)
                .toList();
        if (textTerms.isEmpty()) {
            textTerms = terms;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("question", question);
        params.put("terms", terms);
        params.put("textTerms", textTerms);
        params.put("limit", toolLimit());

        if (terms.isEmpty()) {
            GraphToolResult result = GraphToolResult.of("relation_intent_exact", List.of());
            recorder.record("findByGraphIntentExact", params, result);
            return result;
        }

        String relationCypher = ""
                + "WITH $terms AS terms "
                + "CALL { "
                + "WITH terms "
                + "UNWIND terms AS term "
                + "WITH term, toLower(term) AS q "
                + "MATCH (d:Disease) "
                + "WITH d, term, q, toLower(coalesce(d.disease_name, '')) AS name "
                + "WHERE q <> '' AND name <> '' AND (name = q OR name CONTAINS q OR q CONTAINS name) "
                + "MATCH (p:Prescription)-[:TREATS_DISEASE]->(d) "
                + "WITH p, d, collect(DISTINCT term)[0..3] AS matchedTerms, "
                + "min(CASE WHEN name = q THEN 0 WHEN name STARTS WITH q THEN 1 WHEN q CONTAINS name THEN 2 ELSE 3 END) AS matchScore "
                + "OPTIONAL MATCH (p)-[:CONTAINS_HERB]->(h) "
                + "WHERE h IS NULL OR any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "WITH p, d, matchedTerms, matchScore, collect(DISTINCT h.herb_name_zh)[0..12] AS herbs "
                + "RETURN 'condition_prescription_relation' AS category, "
                + "'disease' AS conditionType, d.disease_name AS condition, d.disease_id AS conditionId, "
                + "d.disease_name AS disease, null AS symptom, null AS syndrome, "
                + "p.name_zh AS prescription, p.tcm_prescription_id AS prescriptionId, "
                + "p.effects_zh AS effects, p.indications_zh AS indications, p.source AS source, herbs AS herbs, "
                + "'direct_disease_prescription_relation' AS evidenceType, matchedTerms AS matchedTerms, matchScore AS matchScore "
                + "UNION "
                + "WITH terms "
                + "UNWIND terms AS term "
                + "WITH term, toLower(term) AS q "
                + "MATCH (sym) "
                + "WHERE any(label IN labels(sym) WHERE label IN ['Symptom','TcmSymptom']) "
                + "WITH sym, term, q, "
                + "toLower(coalesce(sym.symptom_name_zh, '')) AS zh, toLower(coalesce(sym.symptom_name, '')) AS name "
                + "WHERE q <> '' AND ((zh <> '' AND (zh = q OR zh CONTAINS q OR q CONTAINS zh)) "
                + "OR (name <> '' AND (name = q OR name CONTAINS q OR q CONTAINS name))) "
                + "MATCH (p:Prescription)-[:TREATS_SYMPTOM]->(sym) "
                + "WITH p, sym, collect(DISTINCT term)[0..3] AS matchedTerms, "
                + "min(CASE "
                + "WHEN (zh <> '' AND zh = q) OR (name <> '' AND name = q) THEN 0 "
                + "WHEN (zh <> '' AND zh STARTS WITH q) OR (name <> '' AND name STARTS WITH q) THEN 1 "
                + "WHEN (zh <> '' AND q CONTAINS zh) OR (name <> '' AND q CONTAINS name) THEN 2 "
                + "ELSE 3 END) AS matchScore "
                + "OPTIONAL MATCH (p)-[:CONTAINS_HERB]->(h) "
                + "WHERE h IS NULL OR any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "WITH p, sym, matchedTerms, matchScore, collect(DISTINCT h.herb_name_zh)[0..12] AS herbs "
                + "RETURN 'condition_prescription_relation' AS category, "
                + "'symptom' AS conditionType, coalesce(sym.symptom_name_zh, sym.symptom_name) AS condition, sym.tcm_symptom_id AS conditionId, "
                + "null AS disease, coalesce(sym.symptom_name_zh, sym.symptom_name) AS symptom, null AS syndrome, "
                + "p.name_zh AS prescription, p.tcm_prescription_id AS prescriptionId, "
                + "p.effects_zh AS effects, p.indications_zh AS indications, p.source AS source, herbs AS herbs, "
                + "'direct_symptom_prescription_relation' AS evidenceType, matchedTerms AS matchedTerms, matchScore AS matchScore "
                + "UNION "
                + "WITH terms "
                + "UNWIND terms AS term "
                + "WITH term, toLower(term) AS q "
                + "MATCH (s:Syndrome) "
                + "WITH s, term, q, toLower(coalesce(s.syndrome_name_zh, '')) AS name "
                + "WHERE q <> '' AND name <> '' AND (name = q OR name CONTAINS q OR q CONTAINS name) "
                + "MATCH (p:Prescription)-[:TREATS_SYNDROME]->(s) "
                + "WITH p, s, collect(DISTINCT term)[0..3] AS matchedTerms, "
                + "min(CASE WHEN name = q THEN 0 WHEN name STARTS WITH q THEN 1 WHEN q CONTAINS name THEN 2 ELSE 3 END) AS matchScore "
                + "OPTIONAL MATCH (p)-[:CONTAINS_HERB]->(h) "
                + "WHERE h IS NULL OR any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "WITH p, s, matchedTerms, matchScore, collect(DISTINCT h.herb_name_zh)[0..12] AS herbs "
                + "RETURN 'condition_prescription_relation' AS category, "
                + "'syndrome' AS conditionType, s.syndrome_name_zh AS condition, s.tcm_syndrome_id AS conditionId, "
                + "null AS disease, null AS symptom, s.syndrome_name_zh AS syndrome, "
                + "p.name_zh AS prescription, p.tcm_prescription_id AS prescriptionId, "
                + "p.effects_zh AS effects, p.indications_zh AS indications, p.source AS source, herbs AS herbs, "
                + "'direct_syndrome_prescription_relation' AS evidenceType, matchedTerms AS matchedTerms, matchScore AS matchScore "
                + "} "
                + "RETURN DISTINCT category, conditionType, condition, conditionId, disease, symptom, syndrome, "
                + "prescription, prescriptionId, effects, indications, source, herbs, evidenceType, matchedTerms, matchScore "
                + "ORDER BY matchScore, category, condition, prescription "
                + "LIMIT $limit";

        List<Map<String, Object>> rows = graphRepository.query(relationCypher, params);
        if (rows.isEmpty() && !textTerms.isEmpty()) {
            String textCypher = ""
                + "WITH $textTerms AS textTerms "
                + "MATCH (p:Prescription) "
                + "UNWIND textTerms AS term "
                + "WITH p, term "
                + "WHERE term <> '' AND (coalesce(p.name_zh, '') CONTAINS term "
                + "OR coalesce(p.effects_zh, '') CONTAINS term "
                + "OR coalesce(p.indications_zh, '') CONTAINS term) "
                + "WITH p, collect(DISTINCT term)[0..3] AS matchedTerms, "
                + "min(CASE WHEN coalesce(p.name_zh, '') CONTAINS term THEN 4 ELSE 5 END) AS matchScore "
                + "OPTIONAL MATCH (p)-[:CONTAINS_HERB]->(h) "
                + "WHERE h IS NULL OR any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "WITH p, matchedTerms, matchScore, collect(DISTINCT h.herb_name_zh)[0..12] AS herbs "
                + "RETURN DISTINCT 'prescription_text_match' AS category, "
                + "'prescription_text' AS conditionType, head(matchedTerms) AS condition, null AS conditionId, "
                + "null AS disease, null AS symptom, null AS syndrome, "
                + "p.name_zh AS prescription, p.tcm_prescription_id AS prescriptionId, "
                + "p.effects_zh AS effects, p.indications_zh AS indications, p.source AS source, herbs AS herbs, "
                + "'prescription_text_contains_condition' AS evidenceType, matchedTerms AS matchedTerms, matchScore AS matchScore "
                + "ORDER BY matchScore, category, condition, prescription "
                + "LIMIT $limit";
            rows = graphRepository.query(textCypher, params);
        }

        GraphToolResult result = GraphToolResult.of("relation_intent_exact", rows);
        recorder.record("findByGraphIntentExact", params, result);
        return result;
    }

    private GraphToolResult findPrescriptionsBySemanticCondition(String question, Map<String, Object> params) {
        String conditionQuery = expandConditionQuestion(question);
        List<SemanticSearchService.SemanticAnchor> candidates = semanticSearchService.search(
                conditionQuery,
                CONDITION_PRESCRIPTION_SEMANTIC_TYPES,
                Math.min(30, Math.max(10, toolLimit())));
        List<SemanticSearchService.SemanticAnchor> anchors = acceptedGeneralSemanticAnchors(candidates);
        params.put("semanticIntentMode", "condition_to_prescription");
        params.put("conditionSemanticQuery", conditionQuery);
        params.put("conditionSemanticQueryTypes", CONDITION_PRESCRIPTION_SEMANTIC_TYPES);
        params.put("conditionSemanticCandidateAnchors", anchorSummaries(candidates, 10));
        params.put("conditionSemanticAnchors", anchorSummaries(anchors, 10));
        params.put("conditionSemanticCandidateCount", candidates.size());
        params.put("conditionSemanticAnchorCount", anchors.size());

        List<Map<String, Object>> rows = new ArrayList<>();
        List<SemanticSearchService.SemanticAnchor> diseaseAnchors = new ArrayList<>();
        List<SemanticSearchService.SemanticAnchor> conditionAnchors = new ArrayList<>();
        Set<String> routedKeys = new LinkedHashSet<>();
        int perAnchorLimit = Math.max(3, Math.min(12, toolLimit()));
        for (SemanticSearchService.SemanticAnchor anchor : anchors.stream().limit(8).toList()) {
            String key = anchor.entityType() + ":" + anchor.name();
            if (!routedKeys.add(key)) {
                continue;
            }
            conditionAnchors.add(anchor);
            if ("disease".equals(anchorType(anchor))) {
                diseaseAnchors.add(anchor);
            }
            rows.addAll(routeConditionPrescriptionAnchor(anchor, perAnchorLimit));
            if (rows.size() >= toolLimit()) {
                break;
            }
        }
        params.put("conditionRoutedAnchorCount", routedKeys.size());
        params.put("conditionPrescriptionRowCount", rows.size());
        if (rows.isEmpty() && !diseaseAnchors.isEmpty()) {
            params.put("finalEvidenceLevel", "no_direct_disease_prescription_relation");
            return GraphToolResult.of(
                    "disease_prescriptions_no_direct_relation",
                    noDirectDiseasePrescriptionRows(diseaseAnchors));
        }
        if (rows.isEmpty() && !conditionAnchors.isEmpty()) {
            params.put("finalEvidenceLevel", "no_direct_condition_prescription_relation");
            return GraphToolResult.of(
                    "condition_prescriptions_no_direct_relation",
                    noDirectConditionPrescriptionRows(conditionAnchors));
        }
        if (rows.isEmpty()) {
            return GraphToolResult.of("semantic_condition_prescriptions", List.of());
        }
        params.put("finalEvidenceLevel", "semantic_condition_direct_relation");
        return GraphToolResult.of("semantic_condition_prescriptions", rows);
    }

    @Tool(description = "Semantic topic filtered query for herb compounds and their targets. Use for topic-related herb compound targets, such as anti-inflammatory ginseng compounds and targets. When filling topic, keep the user's full biological topic phrase and all qualifiers, including cell type, tissue, disease, pathway, mechanism, syndrome, or phenotype; do not simplify it to a broad category.")
    public GraphToolResult findHerbCompoundTargetsByTopic(
            @ToolParam(description = "Herb Chinese name, for example 人参 or 黄芪") String herbName,
            @ToolParam(description = "Semantic topic copied from the user's question as a full phrase, for example 抗炎, 巨噬细胞炎症, 肿瘤免疫微环境, 肝纤维化, 神经炎症, 免疫调节, 降糖, 神经保护") String topic) {
        String normalizedHerbName = entityNormalizeService.normalizeHerb(herbName);
        Map<String, Object> params = new HashMap<>();
        params.put("herbName", normalizedHerbName);
        params.put("originalHerbName", herbName);
        params.put("topic", topic);
        params.put("limit", toolLimit());
        params.put("semanticQueryTypes", TOPIC_TARGET_SEMANTIC_TYPES);

        List<SemanticSearchService.SemanticAnchor> anchors = semanticSearchService.search(
                topic,
                TOPIC_TARGET_SEMANTIC_TYPES,
                Math.min(30, Math.max(10, toolLimit())));
        List<SemanticSearchService.SemanticAnchor> acceptedAnchors = acceptedSemanticAnchors(anchors);
        Set<String> targetSymbols = new LinkedHashSet<>(semanticSearchService.exactTopicTargetSymbols(topic));
        targetSymbols.addAll(curatedTopicTargetSymbols(topic));
        targetSymbols.addAll(semanticSearchService.targetSymbolsFromAnchors(acceptedAnchors));
        List<String> targetSymbolList = targetSymbols.stream()
                .filter(StringUtils::hasText)
                .limit(Math.max(20, Math.min(200, toolLimit())))
                .toList();
        List<String> upperTargetSymbolList = targetSymbolList.stream()
                .map(String::toUpperCase)
                .toList();
        params.put("semanticCandidateAnchors", anchors.stream()
                .limit(10)
                .map(anchor -> Map.of(
                        "type", anchor.entityType(),
                        "name", anchor.name(),
                        "distance", anchor.distance()))
                .toList());
        params.put("semanticAnchors", acceptedAnchors.stream()
                .limit(10)
                .map(anchor -> Map.of(
                        "type", anchor.entityType(),
                        "name", anchor.name(),
                        "distance", anchor.distance()))
                .toList());
        params.put("targetSymbols", targetSymbolList);
        params.put("targetSymbolsUpper", upperTargetSymbolList);
        params.put("semanticMaxDistanceByType", Map.of(
                "topic", TOPIC_MAX_DISTANCE,
                "target", TARGET_MAX_DISTANCE));
        params.put("semanticCandidateCount", anchors.size());
        params.put("semanticAnchorCount", acceptedAnchors.size());

        if (!StringUtils.hasText(normalizedHerbName) || !StringUtils.hasText(topic) || targetSymbolList.isEmpty()) {
            GraphToolResult result = GraphToolResult.of("herb_compound_targets_semantic", List.of());
            recorder.record("findHerbCompoundTargetsByTopic", params, result);
            return result;
        }

        String cypher = ""
                + "MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound)-[:TARGETS]->(t:Target) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND h.herb_name_zh = $herbName "
                + "AND (t.symbol IN $targetSymbols "
                + "OR toUpper(t.symbol) IN $targetSymbolsUpper "
                + "OR t.tcm_tar_id IN $targetSymbols) "
                + "RETURN DISTINCT "
                + "h.herb_name_zh AS herb, "
                + "coalesce(c.name_zh, c.name, c.inchikey) AS compound, "
                + "c.inchikey AS inchikey, "
                + "c.molecular_formula AS formula, "
                + "t.symbol AS target, "
                + "t.tcm_tar_id AS targetId, "
                + "$topic AS semanticTopic, "
                + "'semantic_target_anchor' AS evidenceType "
                + "ORDER BY target, compound "
                + "LIMIT $limit";

        GraphToolResult result = GraphToolResult.of("herb_compound_targets_semantic", graphRepository.query(cypher, params));
        recorder.record("findHerbCompoundTargetsByTopic", params, result);
        return result;
    }

    private List<SemanticSearchService.SemanticAnchor> acceptedGeneralSemanticAnchors(List<SemanticSearchService.SemanticAnchor> anchors) {
        if (anchors == null || anchors.isEmpty()) {
            return List.of();
        }
        return anchors.stream()
                .filter(anchor -> anchor != null && anchor.distance() <= maxGeneralDistanceForAnchor(anchor))
                .toList();
    }

    private double maxGeneralDistanceForAnchor(SemanticSearchService.SemanticAnchor anchor) {
        return GENERAL_SEMANTIC_MAX_DISTANCE.getOrDefault(anchorType(anchor), -1D);
    }

    private List<Map<String, Object>> anchorSummaries(List<SemanticSearchService.SemanticAnchor> anchors, int limit) {
        if (anchors == null || anchors.isEmpty()) {
            return List.of();
        }
        return anchors.stream()
                .limit(Math.max(1, limit))
                .map(this::anchorSummary)
                .toList();
    }

    private Map<String, Object> anchorSummary(SemanticSearchService.SemanticAnchor anchor) {
        Map<String, Object> row = new HashMap<>();
        row.put("type", anchor.entityType());
        row.put("name", anchor.name());
        row.put("entityId", anchor.entityId());
        row.put("distance", anchor.distance());
        return row;
    }

    private List<Map<String, Object>> routeSemanticAnchor(String question,
                                                          SemanticSearchService.SemanticAnchor anchor,
                                                          int limit) {
        String type = anchorType(anchor);
        List<Map<String, Object>> rows = switch (type) {
            case "prescription" -> querySemanticPrescription(anchor, limit);
            case "syndrome" -> querySemanticSyndrome(anchor, limit);
            case "tcm_symptom" -> querySemanticSymptom(anchor, limit);
            case "wm_symptom" -> querySemanticWmSymptom(anchor, limit);
            case "disease" -> querySemanticDisease(anchor, limit);
            case "herb" -> querySemanticHerb(anchor, limit);
            case "topic", "target" -> List.of(semanticAnchorOnlyRow(question, anchor));
            default -> List.of();
        };
        return annotateSemanticRows(rows, anchor);
    }

    private List<Map<String, Object>> querySemanticPrescription(SemanticSearchService.SemanticAnchor anchor, int limit) {
        String cypher = ""
                + "MATCH (p:Prescription) "
                + "WHERE p.name_zh = $name OR p.tcm_prescription_id = $entityId "
                + "OPTIONAL MATCH (p)-[:TREATS_SYNDROME]->(sy:Syndrome) "
                + "WITH p, collect(DISTINCT sy.syndrome_name_zh)[0..8] AS syndromes "
                + "OPTIONAL MATCH (p)-[:TREATS_SYMPTOM]->(sym) "
                + "WITH p, syndromes, collect(DISTINCT coalesce(sym.symptom_name_zh, sym.symptom_name))[0..8] AS symptoms "
                + "OPTIONAL MATCH (p)-[:TREATS_DISEASE]->(d:Disease) "
                + "WITH p, syndromes, symptoms, collect(DISTINCT d.disease_name)[0..8] AS diseases "
                + "RETURN DISTINCT "
                + "'semantic_prescription' AS category, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "p.effects_zh AS effects, "
                + "p.indications_zh AS indications, "
                + "p.source AS source, "
                + "syndromes AS syndromes, "
                + "symptoms AS symptoms, "
                + "diseases AS diseases "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private List<Map<String, Object>> querySemanticSyndrome(SemanticSearchService.SemanticAnchor anchor, int limit) {
        String cypher = ""
                + "MATCH (s:Syndrome) "
                + "WHERE s.syndrome_name_zh = $name OR s.tcm_syndrome_id = $entityId "
                + "OPTIONAL MATCH (p:Prescription)-[:TREATS_SYNDROME]->(s) "
                + "WITH s, collect(DISTINCT p.name_zh)[0..12] AS prescriptions "
                + "OPTIONAL MATCH (s)-[:HAS_SYMPTOM]->(sym) "
                + "WITH s, prescriptions, collect(DISTINCT coalesce(sym.symptom_name_zh, sym.symptom_name))[0..12] AS symptoms "
                + "RETURN DISTINCT "
                + "'semantic_syndrome' AS category, "
                + "s.syndrome_name_zh AS syndrome, "
                + "s.tcm_syndrome_id AS syndromeId, "
                + "s.syndrome_definition_zh AS definition, "
                + "s.category_zh AS syndromeCategory, "
                + "prescriptions AS prescriptions, "
                + "symptoms AS symptoms "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private List<Map<String, Object>> querySemanticSymptom(SemanticSearchService.SemanticAnchor anchor, int limit) {
        String cypher = ""
                + "MATCH (sym) "
                + "WHERE any(label IN labels(sym) WHERE label IN ['Symptom','TcmSymptom']) "
                + "AND (sym.symptom_name_zh = $name OR sym.symptom_name = $name OR sym.tcm_symptom_id = $entityId) "
                + "OPTIONAL MATCH (p:Prescription)-[:TREATS_SYMPTOM]->(sym) "
                + "WITH sym, collect(DISTINCT p.name_zh)[0..12] AS prescriptions "
                + "OPTIONAL MATCH (s:Syndrome)-[:HAS_SYMPTOM]->(sym) "
                + "WITH sym, prescriptions, collect(DISTINCT s.syndrome_name_zh)[0..12] AS syndromes "
                + "RETURN DISTINCT "
                + "'semantic_symptom' AS category, "
                + "coalesce(sym.symptom_name_zh, sym.symptom_name) AS symptom, "
                + "sym.tcm_symptom_id AS symptomId, "
                + "sym.symptom_definition AS definition, "
                + "sym.symptom_locus AS locus, "
                + "sym.symptom_property AS property, "
                + "prescriptions AS prescriptions, "
                + "syndromes AS syndromes "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private List<Map<String, Object>> querySemanticWmSymptom(SemanticSearchService.SemanticAnchor anchor, int limit) {
        List<String> tcmSymptomIds = metadataStringList(anchor.metadata(), "tcmSymptomIds");
        if (tcmSymptomIds.isEmpty()) {
            return List.of(semanticAnchorOnlyRow(null, anchor));
        }
        String cypher = ""
                + "MATCH (sym) "
                + "WHERE any(label IN labels(sym) WHERE label IN ['Symptom','TcmSymptom']) "
                + "AND sym.tcm_symptom_id IN $tcmSymptomIds "
                + "OPTIONAL MATCH (p:Prescription)-[:TREATS_SYMPTOM]->(sym) "
                + "WITH sym, collect(DISTINCT p.name_zh)[0..12] AS prescriptions "
                + "OPTIONAL MATCH (s:Syndrome)-[:HAS_SYMPTOM]->(sym) "
                + "WITH sym, prescriptions, collect(DISTINCT s.syndrome_name_zh)[0..12] AS syndromes "
                + "RETURN DISTINCT "
                + "'semantic_wm_symptom' AS category, "
                + "$wmSymptom AS westernSymptom, "
                + "$umlsId AS umlsId, "
                + "coalesce(sym.symptom_name_zh, sym.symptom_name) AS mappedTcmSymptom, "
                + "sym.tcm_symptom_id AS symptomId, "
                + "sym.symptom_definition AS definition, "
                + "sym.symptom_locus AS locus, "
                + "sym.symptom_property AS property, "
                + "prescriptions AS prescriptions, "
                + "syndromes AS syndromes, "
                + "$targetSymbols AS targetSymbols "
                + "LIMIT $limit";
        Map<String, Object> params = semanticAnchorParams(anchor, limit);
        params.put("tcmSymptomIds", tcmSymptomIds);
        params.put("wmSymptom", anchor.name());
        params.put("umlsId", scalarMetadata(anchor.metadata(), "umls_id"));
        params.put("targetSymbols", metadataStringList(anchor.metadata(), "targetSymbols"));
        return graphRepository.query(cypher, params);
    }

    private List<Map<String, Object>> routeConditionPrescriptionAnchor(SemanticSearchService.SemanticAnchor anchor,
                                                                       int limit) {
        List<Map<String, Object>> rows = switch (anchorType(anchor)) {
            case "disease" -> querySemanticDiseasePrescriptions(anchor, limit);
            case "syndrome" -> querySemanticSyndromePrescriptions(anchor, limit);
            case "tcm_symptom" -> querySemanticSymptomPrescriptions(anchor, limit);
            case "wm_symptom" -> querySemanticWmSymptomPrescriptions(anchor, limit);
            default -> List.of();
        };
        return annotateSemanticRows(rows, anchor);
    }

    private List<Map<String, Object>> querySemanticDiseasePrescriptions(SemanticSearchService.SemanticAnchor anchor,
                                                                        int limit) {
        String cypher = ""
                + "MATCH (d:Disease) "
                + "WHERE d.disease_name = $name OR d.disease_id = $entityId "
                + "MATCH (p:Prescription)-[:TREATS_DISEASE]->(d) "
                + "RETURN DISTINCT "
                + "'semantic_disease_prescription' AS category, "
                + "d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "'semantic_disease_anchor' AS evidenceType "
                + "ORDER BY disease, prescription "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private List<Map<String, Object>> querySemanticSyndromePrescriptions(SemanticSearchService.SemanticAnchor anchor,
                                                                         int limit) {
        String cypher = ""
                + "MATCH (s:Syndrome) "
                + "WHERE s.syndrome_name_zh = $name OR s.tcm_syndrome_id = $entityId "
                + "MATCH (p:Prescription)-[:TREATS_SYNDROME]->(s) "
                + "RETURN DISTINCT "
                + "'semantic_syndrome_prescription' AS category, "
                + "s.syndrome_name_zh AS syndrome, "
                + "s.tcm_syndrome_id AS syndromeId, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "'semantic_syndrome_direct_relation' AS evidenceType "
                + "ORDER BY syndrome, prescription "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private List<Map<String, Object>> querySemanticSymptomPrescriptions(SemanticSearchService.SemanticAnchor anchor,
                                                                        int limit) {
        String cypher = ""
                + "MATCH (sym) "
                + "WHERE any(label IN labels(sym) WHERE label IN ['Symptom','TcmSymptom']) "
                + "AND (sym.symptom_name_zh = $name OR sym.symptom_name = $name OR sym.tcm_symptom_id = $entityId) "
                + "MATCH (p:Prescription)-[:TREATS_SYMPTOM]->(sym) "
                + "RETURN DISTINCT "
                + "'semantic_symptom_prescription' AS category, "
                + "coalesce(sym.symptom_name_zh, sym.symptom_name) AS symptom, "
                + "sym.tcm_symptom_id AS symptomId, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "'semantic_symptom_direct_relation' AS evidenceType "
                + "ORDER BY symptom, prescription "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private List<Map<String, Object>> querySemanticWmSymptomPrescriptions(SemanticSearchService.SemanticAnchor anchor,
                                                                          int limit) {
        List<String> tcmSymptomIds = metadataStringList(anchor.metadata(), "tcmSymptomIds");
        if (tcmSymptomIds.isEmpty()) {
            return List.of();
        }
        String cypher = ""
                + "MATCH (sym) "
                + "WHERE any(label IN labels(sym) WHERE label IN ['Symptom','TcmSymptom']) "
                + "AND sym.tcm_symptom_id IN $tcmSymptomIds "
                + "MATCH (p:Prescription)-[:TREATS_SYMPTOM]->(sym) "
                + "RETURN DISTINCT "
                + "'semantic_wm_symptom_prescription' AS category, "
                + "$wmSymptom AS westernSymptom, "
                + "$umlsId AS umlsId, "
                + "coalesce(sym.symptom_name_zh, sym.symptom_name) AS mappedTcmSymptom, "
                + "sym.tcm_symptom_id AS symptomId, "
                + "p.name_zh AS prescription, "
                + "p.tcm_prescription_id AS prescriptionId, "
                + "'semantic_wm_symptom_mapped_direct_relation' AS evidenceType "
                + "ORDER BY mappedTcmSymptom, prescription "
                + "LIMIT $limit";
        Map<String, Object> params = semanticAnchorParams(anchor, limit);
        params.put("tcmSymptomIds", tcmSymptomIds);
        params.put("wmSymptom", anchor.name());
        params.put("umlsId", scalarMetadata(anchor.metadata(), "umls_id"));
        return graphRepository.query(cypher, params);
    }

    private List<Map<String, Object>> querySemanticDisease(SemanticSearchService.SemanticAnchor anchor, int limit) {
        String cypher = ""
                + "MATCH (d:Disease) "
                + "WHERE d.disease_name = $name OR d.disease_id = $entityId "
                + "OPTIONAL MATCH (p:Prescription)-[:TREATS_DISEASE]->(d) "
                + "WITH d, collect(DISTINCT p.name_zh)[0..12] AS prescriptions "
                + "OPTIONAL MATCH (h)-[:TREATS_DISEASE]->(d) "
                + "WHERE h IS NULL OR any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "WITH d, prescriptions, collect(DISTINCT h.herb_name_zh)[0..12] AS herbs "
                + "RETURN DISTINCT "
                + "'semantic_disease' AS category, "
                + "d.disease_name AS disease, "
                + "d.disease_id AS diseaseId, "
                + "prescriptions AS prescriptions, "
                + "herbs AS herbs "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private List<Map<String, Object>> querySemanticHerb(SemanticSearchService.SemanticAnchor anchor, int limit) {
        String cypher = ""
                + "MATCH (h) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND (h.herb_name_zh = $name OR coalesce(h.tcm_herb_id, h.tcm_herb2_id) = $entityId) "
                + "OPTIONAL MATCH (h)-[:TREATS_SYMPTOM]->(sym) "
                + "WITH h, collect(DISTINCT coalesce(sym.symptom_name_zh, sym.symptom_name))[0..8] AS symptoms "
                + "OPTIONAL MATCH (h)-[:TREATS_SYNDROME]->(s:Syndrome) "
                + "WITH h, symptoms, collect(DISTINCT s.syndrome_name_zh)[0..8] AS syndromes "
                + "OPTIONAL MATCH (h)-[:TREATS_DISEASE]->(d:Disease) "
                + "WITH h, symptoms, syndromes, collect(DISTINCT d.disease_name)[0..8] AS diseases "
                + "RETURN DISTINCT "
                + "'semantic_herb' AS category, "
                + "h.herb_name_zh AS herb, "
                + "coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId, "
                + "h.efficacy_zh AS efficacy, "
                + "h.indications_zh AS indications, "
                + "symptoms AS symptoms, "
                + "syndromes AS syndromes, "
                + "diseases AS diseases "
                + "LIMIT $limit";
        return graphRepository.query(cypher, semanticAnchorParams(anchor, limit));
    }

    private Map<String, Object> semanticAnchorOnlyRow(String question, SemanticSearchService.SemanticAnchor anchor) {
        Map<String, Object> row = new HashMap<>();
        row.put("category", "semantic_anchor");
        row.put("question", question);
        row.put("entityType", anchor.entityType());
        row.put("name", anchor.name());
        row.put("entityId", anchor.entityId());
        row.put("targetSymbols", semanticSearchService.targetSymbolsFromMetadata(anchor.metadata()));
        row.put("tcmSymptomIds", metadataStringList(anchor.metadata(), "tcmSymptomIds"));
        row.put("tcmSymptomNames", metadataStringList(anchor.metadata(), "tcmSymptomNames"));
        return row;
    }

    private List<Map<String, Object>> noDirectDiseasePrescriptionRows(List<SemanticSearchService.SemanticAnchor> anchors) {
        if (anchors == null || anchors.isEmpty()) {
            return List.of();
        }
        return anchors.stream()
                .limit(5)
                .map(anchor -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("category", "no_direct_disease_prescription_relation");
                    row.put("disease", anchor.name());
                    row.put("diseaseId", anchor.entityId());
                    row.put("semanticAnchorType", anchor.entityType());
                    row.put("semanticAnchorName", anchor.name());
                    row.put("semanticAnchorDistance", anchor.distance());
                    row.put("semanticAnchorId", anchor.entityId());
                    row.put("prescriptionRelCount", 0);
                    row.put("evidenceType", "semantic_disease_anchor_no_direct_prescription");
                    row.put("conclusion", "知识图谱中识别到相关疾病，但未找到该疾病与方剂的直接治疗关系。");
                    row.put("answerGuidance", "不要把语义相近的方剂表述为可治疗该疾病；可建议补充疾病-方剂关系数据。");
                    return row;
                })
                .toList();
    }

    private List<Map<String, Object>> noDirectConditionPrescriptionRows(List<SemanticSearchService.SemanticAnchor> anchors) {
        if (anchors == null || anchors.isEmpty()) {
            return List.of();
        }
        return anchors.stream()
                .limit(5)
                .map(anchor -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("category", "no_direct_condition_prescription_relation");
                    row.put("conditionType", anchorType(anchor));
                    row.put("condition", anchor.name());
                    row.put("conditionId", anchor.entityId());
                    row.put("semanticAnchorType", anchor.entityType());
                    row.put("semanticAnchorName", anchor.name());
                    row.put("semanticAnchorDistance", anchor.distance());
                    row.put("semanticAnchorId", anchor.entityId());
                    row.put("prescriptionRelCount", 0);
                    row.put("evidenceType", "semantic_condition_anchor_no_direct_prescription");
                    row.put("conclusion", "知识图谱中识别到相关条件锚点，但未找到该条件与方剂的直接治疗关系。");
                    row.put("answerGuidance", "不要退回方剂名相似候选并表述为可治疗；应说明当前图谱没有直接关系。");
                    return row;
                })
                .toList();
    }

    private Map<String, Object> semanticAnchorParams(SemanticSearchService.SemanticAnchor anchor, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", anchor.name());
        params.put("entityId", anchor.entityId());
        params.put("limit", Math.max(1, limit));
        return params;
    }

    private List<Map<String, Object>> annotateSemanticRows(List<Map<String, Object>> rows,
                                                           SemanticSearchService.SemanticAnchor anchor) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> annotated = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> copy = new HashMap<>();
            copy.put("semanticAnchorType", anchor.entityType());
            copy.put("semanticAnchorName", anchor.name());
            copy.put("semanticAnchorDistance", anchor.distance());
            copy.put("semanticAnchorId", anchor.entityId());
            copy.putAll(row);
            annotated.add(copy);
        }
        return annotated;
    }

    private String anchorType(SemanticSearchService.SemanticAnchor anchor) {
        return anchor == null || anchor.entityType() == null
                ? ""
                : anchor.entityType().trim().toLowerCase(Locale.ROOT);
    }

    private List<String> metadataStringList(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.isEmpty()) {
            return List.of();
        }
        Object value = metadata.get(key);
        if (value instanceof Iterable<?> values) {
            List<String> result = new ArrayList<>();
            for (Object item : values) {
                if (item != null && StringUtils.hasText(item.toString())) {
                    result.add(item.toString().trim());
                }
            }
            return result;
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            List<String> result = new ArrayList<>();
            for (String part : text.split("[,;\\s]+")) {
                if (StringUtils.hasText(part)) {
                    result.add(part.trim());
                }
            }
            return result;
        }
        return List.of();
    }

    private String scalarMetadata(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        Object value = metadata.get(key);
        return value == null ? "" : value.toString();
    }

    private List<SemanticSearchService.SemanticAnchor> acceptedSemanticAnchors(List<SemanticSearchService.SemanticAnchor> anchors) {
        if (anchors == null || anchors.isEmpty()) {
            return List.of();
        }
        return anchors.stream()
                .filter(anchor -> anchor != null && anchor.distance() <= maxDistanceForAnchor(anchor))
                .toList();
    }

    private double maxDistanceForAnchor(SemanticSearchService.SemanticAnchor anchor) {
        String entityType = anchor.entityType() == null
                ? ""
                : anchor.entityType().trim().toLowerCase(Locale.ROOT);
        if ("topic".equals(entityType)) {
            return TOPIC_MAX_DISTANCE;
        }
        if ("target".equals(entityType)) {
            return TARGET_MAX_DISTANCE;
        }
        return -1D;
    }

    private List<String> curatedTopicTargetSymbols(String topic) {
        if (!StringUtils.hasText(topic)) {
            return List.of();
        }
        String normalizedTopic = topic.toLowerCase();
        Set<String> symbols = new LinkedHashSet<>();
        if (containsAny(normalizedTopic, List.of(
                "\u6297\u708e",
                "\u708e\u75c7",
                "\u708e\u75c7\u53cd\u5e94",
                "anti-inflammatory",
                "inflammation",
                "inflammatory"))) {
            symbols.addAll(ANTI_INFLAMMATION_TARGETS);
        }
        if (containsAny(normalizedTopic, List.of(
                "\u5de8\u566c\u7ec6\u80de",
                "\u5355\u6838\u5de8\u566c",
                "\u541e\u566c\u7ec6\u80de",
                "macrophage",
                "macrophages"))) {
            symbols.addAll(MACROPHAGE_INFLAMMATION_TARGETS);
        }
        return new ArrayList<>(symbols);
    }

    private List<String> graphIntentTerms(String question) {
        if (!StringUtils.hasText(question)) {
            return List.of();
        }
        String normalized = question
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
        String cleaned = normalized;
        for (String stopWord : GRAPH_INTENT_STOP_WORDS) {
            cleaned = cleaned.replace(stopWord, " ");
        }
        cleaned = GRAPH_INTENT_SEPARATOR.matcher(cleaned).replaceAll(" ");

        Set<String> terms = new LinkedHashSet<>();
        for (String segment : GRAPH_INTENT_SEPARATOR.split(cleaned)) {
            addGraphIntentTerm(terms, segment);
        }
        return terms.stream()
                .limit(12)
                .toList();
    }

    private void addGraphIntentTerm(Set<String> terms, String candidate) {
        String cleaned = cleanGraphIntentTerm(candidate);
        if (usableGraphIntentTerm(cleaned)) {
            terms.add(cleaned);
        }
    }

    private String cleanGraphIntentTerm(String candidate) {
        if (candidate == null) {
            return "";
        }
        return candidate
                .replaceAll("^[：:，,。；;、？?！!\\s]+", "")
                .replaceAll("[：:，,。；;、？?！!\\s]+$", "")
                .trim();
    }

    private boolean usableGraphIntentTerm(String term) {
        if (!StringUtils.hasText(term) || GRAPH_INTENT_STOP_WORDS.contains(term)) {
            return false;
        }
        if (term.length() > 30) {
            return false;
        }
        if (containsCjk(term)) {
            return term.length() >= 1;
        }
        return term.length() >= 3;
    }

    private boolean usableGraphTextTerm(String term) {
        if (!StringUtils.hasText(term)) {
            return false;
        }
        if (containsCjk(term)) {
            return term.length() >= 2;
        }
        return term.length() >= 3;
    }

    private boolean containsCjk(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            Character.UnicodeScript script = Character.UnicodeScript.of(text.charAt(i));
            if (script == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private boolean asksPrescriptionForCondition(String question) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        String normalized = question.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        boolean asksPrescription = containsAny(normalized, List.of(
                "\u65b9\u5242",
                "\u65b9\u5b50",
                "\u836f\u65b9",
                "\u5904\u65b9",
                "formula",
                "prescription"));
        boolean asksTreatment = containsAny(normalized, List.of(
                "\u6cbb\u7597",
                "\u53ef\u4ee5\u6cbb",
                "\u80fd\u6cbb",
                "\u4e3b\u6cbb",
                "\u7f13\u89e3",
                "\u9002\u7528",
                "treat",
                "relieve"));
        boolean asksPrescriptionQuestion = containsAny(normalized, List.of(
                "\u7528\u4ec0\u4e48\u65b9\u5242",
                "\u7528\u4ec0\u4e48\u65b9\u5b50",
                "\u7528\u4ec0\u4e48\u836f\u65b9",
                "\u6709\u54ea\u4e9b\u65b9\u5242",
                "\u54ea\u4e9b\u65b9\u5242",
                "\u4ec0\u4e48\u65b9\u5242",
                "\u65b9\u5242\u6709\u54ea\u4e9b",
                "\u65b9\u5242\u6709\u4ec0\u4e48",
                "\u63a8\u8350\u65b9\u5242",
                "\u63a8\u8350\u65b9\u5b50",
                "\u5403\u4ec0\u4e48\u65b9",
                "\u5f00\u4ec0\u4e48\u65b9",
                "whatformula",
                "whichformula",
                "recommendedformula"));
        return asksPrescription && (asksTreatment || asksPrescriptionQuestion);
    }

    private String expandConditionQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            return "";
        }
        String normalized = question.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        Set<String> terms = new LinkedHashSet<>();
        terms.add(question);
        if (containsAny(normalized, List.of(
                "\u5589\u5499\u53d1\u708e",
                "\u55d3\u5b50\u53d1\u708e",
                "\u54bd\u5589\u53d1\u708e",
                "\u5589\u5499\u75db",
                "\u55d3\u5b50\u75db",
                "\u54bd\u75db",
                "\u54bd\u708e",
                "sorethroat",
                "pharyngitis"))) {
            terms.addAll(List.of(
                    "\u54bd\u5589\u80bf\u75db",
                    "\u54bd\u75db",
                    "\u5589\u75f9",
                    "\u54bd\u708e",
                    "sore throat",
                    "pharyngitis",
                    "throat inflammation"));
        }
        if (containsAny(normalized, List.of(
                "\u773c\u775b\u75b2\u52b3",
                "\u89c6\u75b2\u52b3",
                "\u773c\u75b2\u52b3",
                "eyestrain"))) {
            terms.addAll(List.of(
                    "\u89c6\u75b2\u52b3",
                    "\u773c\u5e72",
                    "\u76ee\u6da9",
                    "eye strain",
                    "asthenopia"));
        }
        if (containsAny(normalized, List.of(
                "\u808c\u8089\u9178\u75db",
                "\u808c\u75db",
                "\u7b4b\u75db",
                "musclepain",
                "myalgia"))) {
            terms.addAll(List.of(
                    "\u808c\u75db",
                    "\u7b4b\u9aa8\u75db",
                    "\u8eab\u75db",
                    "muscle pain",
                    "myalgia"));
        }
        if (containsAny(normalized, List.of(
                "\u4e0a\u706b",
                "\u53e3\u820c\u751f\u75ae",
                "\u7259\u9f88\u80bf\u75db"))) {
            terms.addAll(List.of(
                    "\u70ed\u8bc1",
                    "\u706b\u70ed\u8bc1",
                    "\u53e3\u820c\u751f\u75ae",
                    "\u54bd\u5589\u80bf\u75db",
                    "\u7259\u9f88\u80bf\u75db"));
        }
        return String.join(" ", terms);
    }

    private boolean containsAny(String text, List<String> keywords) {
        if (!StringUtils.hasText(text) || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> baseParams(String key, String value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        params.put("limit", toolLimit());
        return params;
    }

    private int toolLimit() {
        return Math.max(1, toolProperties.getQueryLimit());
    }

    private List<String> normalizeHerbNames(String herbNames) {
        if (herbNames == null || herbNames.isBlank()) {
            return List.of();
        }
        String cleaned = herbNames
                .replaceAll("[\\[\\]{}()（）\"'“”‘’]", "")
                .replace("中药", "")
                .trim();
        String[] parts = HERB_NAME_SEPARATOR.split(cleaned);
        Set<String> names = new LinkedHashSet<>();
        for (String part : parts) {
            String candidate = cleanHerbNamePart(part);
            if (!candidate.isBlank()) {
                String normalized = entityNormalizeService.normalizeHerb(candidate);
                if (normalized != null && !normalized.isBlank()) {
                    names.add(normalized);
                }
            }
        }
        return new ArrayList<>(names);
    }

    private String cleanHerbNamePart(String part) {
        if (part == null) {
            return "";
        }
        return part
                .replaceAll("^(药材|中草药|草药)", "")
                .replaceAll("^[：:，,、。\\s]+", "")
                .replaceAll("[：:，,、。？?\\s]+$", "")
                .trim();
    }

}
