package com.tcmseek.ai.service;

import com.tcmseek.ai.graph.TcmGraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class EntityNormalizeService {

    private static final List<AliasRule> DISEASE_ALIASES = List.of(
            new AliasRule("2型糖尿病", "type 2 diabetes mellitus"),
            new AliasRule("二型糖尿病", "type 2 diabetes mellitus"),
            new AliasRule("ii型糖尿病", "type 2 diabetes mellitus"),
            new AliasRule("type2diabetes", "type 2 diabetes mellitus"),
            new AliasRule("1型糖尿病", "type 1 diabetes mellitus"),
            new AliasRule("一型糖尿病", "type 1 diabetes mellitus"),
            new AliasRule("i型糖尿病", "type 1 diabetes mellitus"),
            new AliasRule("type1diabetes", "type 1 diabetes mellitus"),
            new AliasRule("糖尿病", "diabetes mellitus"),
            new AliasRule("高血压", "hypertension"),
            new AliasRule("冠心病", "coronary artery disease"),
            new AliasRule("新型冠状病毒感染", "Covid-19"),
            new AliasRule("新冠肺炎", "Covid-19"),
            new AliasRule("新冠", "Covid-19"),
            new AliasRule("肺癌", "lung cancer"),
            new AliasRule("乳腺癌", "breast cancer"),
            new AliasRule("结直肠癌", "colorectal cancer"),
            new AliasRule("胃癌", "stomach cancer"),
            new AliasRule("肝癌", "liver cancer"),
            new AliasRule("哮喘", "asthma"),
            new AliasRule("肥胖", "obesity"),
            new AliasRule("抑郁症", "depressive disorder")
    );

    private final TcmGraphRepository graphRepository;

    public EntityNormalizeService(TcmGraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    public String normalizeDisease(String input) {
        String cleaned = clean(input);
        if (!StringUtils.hasText(cleaned)) {
            return "";
        }
        String compact = compact(cleaned);
        for (AliasRule rule : DISEASE_ALIASES) {
            if (compact.contains(rule.alias())) {
                return rule.normalized();
            }
        }
        return findDiseaseName(cleaned, cleaned);
    }

    public String normalizeHerb(String input) {
        String cleaned = clean(input);
        if (!StringUtils.hasText(cleaned)) {
            return "";
        }
        String cypher = ""
                + "MATCH (h) "
                + "WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb']) "
                + "AND (h.herb_name_zh = $name "
                + "OR h.herb_name_zh CONTAINS $name "
                + "OR $name CONTAINS h.herb_name_zh "
                + "OR toLower(coalesce(h.english_name, '')) = toLower($name) "
                + "OR toLower(coalesce(h.pinyin_name, '')) = toLower($name)) "
                + "RETURN DISTINCT h.herb_name_zh AS value, "
                + "CASE "
                + "WHEN h.herb_name_zh = $name THEN 0 "
                + "WHEN $name CONTAINS h.herb_name_zh THEN 1 "
                + "WHEN h.herb_name_zh CONTAINS $name THEN 2 "
                + "ELSE 3 END AS score "
                + "ORDER BY score, size(h.herb_name_zh) DESC "
                + "LIMIT 1";
        return firstValueOrDefault(cypher, Map.of("name", cleaned), cleaned);
    }

    public String normalizePrescription(String input) {
        String cleaned = clean(input);
        if (!StringUtils.hasText(cleaned)) {
            return "";
        }
        String cypher = ""
                + "MATCH (p:Prescription) "
                + "WHERE p.name_zh = $name "
                + "OR p.name_zh CONTAINS $name "
                + "OR $name CONTAINS p.name_zh "
                + "RETURN DISTINCT p.name_zh AS value, "
                + "CASE "
                + "WHEN p.name_zh = $name THEN 0 "
                + "WHEN $name CONTAINS p.name_zh THEN 1 "
                + "WHEN p.name_zh CONTAINS $name THEN 2 "
                + "ELSE 3 END AS score "
                + "ORDER BY score, size(p.name_zh) DESC "
                + "LIMIT 1";
        return firstValueOrDefault(cypher, Map.of("name", cleaned), cleaned);
    }

    public String normalizeSyndrome(String input) {
        String cleaned = clean(input);
        if (!StringUtils.hasText(cleaned)) {
            return "";
        }
        String cypher = ""
                + "MATCH (s:Syndrome) "
                + "WHERE s.syndrome_name_zh = $name "
                + "OR s.syndrome_name_zh CONTAINS $name "
                + "OR $name CONTAINS s.syndrome_name_zh "
                + "RETURN DISTINCT s.syndrome_name_zh AS value, "
                + "CASE WHEN s.syndrome_name_zh = $name THEN 0 ELSE 1 END AS score "
                + "ORDER BY score, size(s.syndrome_name_zh) DESC "
                + "LIMIT 1";
        return firstValueOrDefault(cypher, Map.of("name", cleaned), cleaned);
    }

    public String normalizePathway(String input) {
        String cleaned = clean(input);
        if (!StringUtils.hasText(cleaned)) {
            return "";
        }
        String cypher = ""
                + "MATCH (p:Pathway) "
                + "WHERE p.name = $name "
                + "OR p.pathway_id = $name "
                + "OR toLower(p.name) CONTAINS toLower($name) "
                + "RETURN DISTINCT coalesce(p.name, p.pathway_id) AS value, "
                + "CASE WHEN p.name = $name OR p.pathway_id = $name THEN 0 ELSE 1 END AS score "
                + "ORDER BY score, size(value) "
                + "LIMIT 1";
        return firstValueOrDefault(cypher, Map.of("name", cleaned), cleaned);
    }

    private String findDiseaseName(String cleaned, String defaultValue) {
        String cypher = ""
                + "WITH toLower($name) AS q "
                + "MATCH (d:Disease) "
                + "WHERE toLower(d.disease_name) = q "
                + "OR toLower(d.disease_name) CONTAINS q "
                + "OR q CONTAINS toLower(d.disease_name) "
                + "RETURN DISTINCT d.disease_name AS value, "
                + "CASE "
                + "WHEN toLower(d.disease_name) = q THEN 0 "
                + "WHEN toLower(d.disease_name) STARTS WITH q THEN 1 "
                + "WHEN q CONTAINS toLower(d.disease_name) THEN 2 "
                + "ELSE 3 END AS score "
                + "ORDER BY score, size(d.disease_name) "
                + "LIMIT 1";
        return firstValueOrDefault(cypher, Map.of("name", cleaned), defaultValue);
    }

    private String firstValueOrDefault(String cypher, Map<String, Object> params, String defaultValue) {
        try {
            List<Map<String, Object>> rows = graphRepository.query(cypher, params);
            if (!rows.isEmpty()) {
                Object value = rows.get(0).get("value");
                if (value != null && StringUtils.hasText(value.toString())) {
                    return value.toString();
                }
            }
        } catch (Exception ignored) {
            return defaultValue;
        }
        return defaultValue;
    }

    private String clean(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replaceAll("[“”\"'`]", "")
                .replaceAll("^(中药|方剂|疾病|证候|症状|通路|靶点|基因)", "")
                .replaceAll("(相关|关联|治疗|主治|功效|作用|用途|适应症|适应证)$", "")
                .replaceAll("^[：:，,。\\s]+", "")
                .replaceAll("[：:，,。？?\\s]+$", "")
                .trim();
    }

    private String compact(String value) {
        return value.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("Ⅱ", "ii")
                .replace("Ⅰ", "i");
    }

    private record AliasRule(String alias, String normalized) {
        AliasRule {
            alias = alias.toLowerCase().replaceAll("\\s+", "");
        }
    }
}
