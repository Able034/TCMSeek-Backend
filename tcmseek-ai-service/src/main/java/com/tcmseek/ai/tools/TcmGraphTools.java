package com.tcmseek.ai.tools;

import com.tcmseek.ai.config.AiToolProperties;
import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.graph.TcmGraphRepository;
import com.tcmseek.ai.service.EntityNormalizeService;
import com.tcmseek.ai.service.ToolExecutionRecorder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TcmGraphTools {

    private static final Pattern HERB_NAME_SEPARATOR = Pattern.compile("以及|和|与|及|跟|、|,|，|;|；|/");

    private final TcmGraphRepository graphRepository;

    private final EntityNormalizeService entityNormalizeService;

    private final ToolExecutionRecorder recorder;

    private final AiToolProperties toolProperties;

    public TcmGraphTools(TcmGraphRepository graphRepository,
                         EntityNormalizeService entityNormalizeService,
                         ToolExecutionRecorder recorder,
                         AiToolProperties toolProperties) {
        this.graphRepository = graphRepository;
        this.entityNormalizeService = entityNormalizeService;
        this.recorder = recorder;
        this.toolProperties = toolProperties;
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
        recorder.record("findDiseasePrescriptions", params, result);
        return result;
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
