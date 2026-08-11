package com.tcmseek.tcmseekagentservice.ai.tools.mysql;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tcmseek.tcmseekagentservice.ai.tools.BaseTool;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 中医药实体详情查询工具。
 *
 * <p>设计原则：
 * <ul>
 *     <li>模型只传实体类型和实体名称，不允许模型传 SQL。</li>
 *     <li>后端通过 switch 白名单选择固定 SQL，避免 SQL 注入和表字段幻觉。</li>
 *     <li>查询结果统一返回 JSON 字符串，方便模型继续组织最终回答。</li>
 * </ul>
 */
@Slf4j
@Component
public class GetTcmEntityDetailsTool extends BaseTool {
    /** 单次工具调用最多返回的总记录数，防止结果过大挤占模型上下文。 */
    private static final int MAX_TOTAL_ROWS = 20;

    private final JdbcTemplate jdbcTemplate;

    public GetTcmEntityDetailsTool(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.jdbcTemplate = mysqlJdbcTemplate;
    }

    @Override
    public String getToolName() {
        return "getEntityDetails";
    }

    @Override
    public String getDisplayName() {
        return "获取中医药相关实体信息";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "[工具调用]getEntityDetails 参数：" + arguments;
    }

    /**
     * 按实体类型查询实体详情。
     *
     * <p>这里暴露给模型的参数只有 entityType 和 names，模型不能控制表名、字段名或 SQL 片段。
     */
    @Tool(name = "getEntityDetails",
            value = """
        查询中医药数据库中某类实体的详细信息。

        使用场景：
        - 当已经通过 Neo4j 或上下文确定了要查询的实体类型和实体名称时调用。
        - 该工具用于补充实体的 MySQL 详情字段，不用于查询实体之间的关系。
        - 不要传入自然语言问题，只传实体类型和实体名称。
        - 本工具不会执行模型生成的 SQL，只会根据后端白名单查询固定表和字段。

        参数说明：
        - entityType 必须是以下值之一：
          HERB, COMPOUND, ADMET, TARGET, DISEASE, PHENOTYPE, PATHWAY,
          PRESCRIPTION, TCM_SYMPTOM, TCM_SYNDROME, WM_SYMPTOM, MEDICAL_CASE
        - names 是实体名称或业务ID，多个实体用英文逗号分隔。
          例如：黄芪,甘草 或 TP53,TNF 或 HERB_1,HERB_2

        示例：
        - 查询中药黄芪详情：entityType=HERB, names=黄芪
        - 查询基因 TNF 详情：entityType=TARGET, names=TNF
        - 查询方剂四君子汤详情：entityType=PRESCRIPTION, names=四君子汤
        """)
    public String getEntityDetails(
            @P("""
        实体类型。必须是以下值之一：
        HERB=中药,
        COMPOUND=化合物,
        ADMET=化合物ADMET,
        TARGET=靶标/基因,
        DISEASE=疾病,
        PHENOTYPE=表型,
        PATHWAY=通路,
        PRESCRIPTION=方剂,
        TCM_SYMPTOM=中医症状,
        TCM_SYNDROME=中医证候,
        WM_SYMPTOM=西医症状,
        MEDICAL_CASE=医案
        """) String entityType,

            @P("""
        实体名称或业务ID。多个值用英文逗号分隔。
        例如：
        黄芪
        四君子汤
        TNF
        DOID:11832
        HERB_1,HERB_2
        """) String names) {

        log.info("[工具调用]getEntityDetails entityType={}, names={}", entityType, names);

        // 统一实体类型格式，兼容模型传入小写、横线或空格的情况。
        String normalizedEntityType = normalizeEntityType(entityType);
        // 将“黄芪,甘草”这类输入拆成多个查询词，逐个查库后合并结果。
        List<String> terms = splitNames(names);

        if (!StringUtils.hasText(normalizedEntityType)) {
            return error("entityType 不能为空");
        }
        if (terms.isEmpty()) {
            return error("names 不能为空");
        }

//        log.info("[工具调用]Ai查询了getEntityDetails"+names);
        try {
            // 白名单路由：每种实体类型只会走对应的固定 SQL。
            List<Map<String, Object>> rows = switch (normalizedEntityType) {
                case "HERB" -> queryHerbs(terms);
                case "COMPOUND" -> queryCompounds(terms);
                case "ADMET" -> queryAdmet(terms);
                case "TARGET" -> queryTargets(terms);
                case "DISEASE" -> queryDiseases(terms);
                case "PHENOTYPE" -> queryPhenotypes(terms);
                case "PATHWAY" -> queryPathways(terms);
                case "PRESCRIPTION" -> queryPrescriptions(terms);
                case "TCM_SYMPTOM" -> queryTcmSymptoms(terms);
                case "TCM_SYNDROME" -> queryTcmSyndromes(terms);
                case "WM_SYMPTOM" -> queryWmSymptoms(terms);
                case "MEDICAL_CASE" -> queryMedicalCases(terms);
                default -> null;
            };

            if (rows == null) {
                return error("不支持的 entityType: " + entityType);
            }

            // 返回结构化 JSON，便于模型明确知道查询类型、查询词和结果数量。
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("entityType", normalizedEntityType);
            result.put("queryNames", terms);
            result.put("count", rows.size());
            result.put("results", rows);

            return JSONUtil.toJsonStr(result);
        } catch (Exception e) {
            log.warn("getEntityDetails failed, entityType={}, names={}", entityType, names, e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /** 查询中药核心信息。 */
    private List<Map<String, Object>> queryHerbs(List<String> terms) {
        String sql = """
                SELECT tcm_herb_id, herb_name_zh, latin_name, type, efficacy_zh,
                       efficacy_category, toxicity_zh, toxic_description_zh,
                       pharmacopoeia_record, classification_zh, use_part,
                       nature_taste_zh, meridian_zh, indications_zh
                FROM core_tcm_herbs
                WHERE tcm_herb_id = ? OR herb_name_zh LIKE ? OR latin_name LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE, MatchMode.LIKE);
    }

    /** 查询化合物基础理化信息。 */
    private List<Map<String, Object>> queryCompounds(List<String> terms) {
        String sql = """
                SELECT inchikey, compound_name, canonical_smiles, pubchem_cid, molecular_formula
                FROM tcm_compounds
                WHERE inchikey = ? OR compound_name LIKE ? OR CAST(pubchem_cid AS CHAR) = ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE, MatchMode.EXACT);
    }

    /** 查询化合物 ADMET 预测结果，并通过 inchikey 补充化合物名称。 */
    private List<Map<String, Object>> queryAdmet(List<String> terms) {
        String sql = """
                SELECT a.inchikey, c.compound_name,
                       a.ames, a.bbbp, a.bioavailability, a.caco2, a.carcinogens,
                       a.clearance_microsome, a.clintox, a.cyp1a2_inhibition,
                       a.cyp2c19_inhibition, a.cyp2c9_inhibition, a.cyp2c9_substrate,
                       a.cyp2d6_inhibition, a.cyp2d6_substrate, a.cyp3a4_inhibition,
                       a.cyp3a4_substrate, a.dili, a.freesolv, a.herg_blockers,
                       a.herg_karim, a.hia, a.ld50, a.lipophilicity, a.pampa,
                       a.pgp, a.ppbr, a.skin, a.solubility
                FROM compound_admet a
                LEFT JOIN tcm_compounds c ON c.inchikey = a.inchikey
                WHERE a.inchikey = ? OR c.compound_name LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE);
    }

    /** 查询靶标/基因信息，支持基因符号、业务ID、UniProt、Ensembl、Entrez ID。 */
    private List<Map<String, Object>> queryTargets(List<String> terms) {
        String sql = """
                SELECT tcm_tar_id, gene_entrez_id, symbol, uniprot_id,
                       ensembl_id, description, type_of_gene
                FROM targets
                WHERE tcm_tar_id = ? OR symbol LIKE ? OR uniprot_id = ?
                   OR ensembl_id = ? OR CAST(gene_entrez_id AS CHAR) = ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms,
                MatchMode.EXACT, MatchMode.LIKE, MatchMode.EXACT, MatchMode.EXACT, MatchMode.EXACT);
    }

    /** 查询疾病信息。 */
    private List<Map<String, Object>> queryDiseases(List<String> terms) {
        String sql = """
                SELECT disease_id, disease_name, source
                FROM diseases
                WHERE disease_id = ? OR disease_name LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE);
    }

    /** 查询表型信息。 */
    private List<Map<String, Object>> queryPhenotypes(List<String> terms) {
        String sql = """
                SELECT phenotype_id, phenotype_name, source
                FROM phenotypes
                WHERE phenotype_id = ? OR phenotype_name LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE);
    }

    /** 查询 KEGG 通路信息。 */
    private List<Map<String, Object>> queryPathways(List<String> terms) {
        String sql = """
                SELECT pathway_id, name, source
                FROM pathways
                WHERE pathway_id = ? OR name LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE);
    }

    /** 查询方剂/中成药信息。 */
    private List<Map<String, Object>> queryPrescriptions(List<String> terms) {
        String sql = """
                SELECT tcm_prescription_id, name_zh, source, indications_zh, effects_zh
                FROM tcm_prescriptions
                WHERE tcm_prescription_id = ? OR name_zh LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE);
    }

    /** 查询中医症状信息。 */
    private List<Map<String, Object>> queryTcmSymptoms(List<String> terms) {
        String sql = """
                SELECT tcm_symptom_id, symptom_name_zh, symptom_definition,
                       symptom_locus, symptom_property, type
                FROM tcm_symptoms
                WHERE tcm_symptom_id = ? OR symptom_name_zh LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE);
    }

    /** 查询中医证候信息。 */
    private List<Map<String, Object>> queryTcmSyndromes(List<String> terms) {
        String sql = """
                SELECT tcm_syndrome_id, syndrome_name_zh, syndrome_definition_zh,
                       category_zh, source
                FROM tcm_syndromes
                WHERE tcm_syndrome_id = ? OR syndrome_name_zh LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE);
    }

    /** 查询西医症状信息。 */
    private List<Map<String, Object>> queryWmSymptoms(List<String> terms) {
        String sql = """
                SELECT wm_symptom_id, symptom_name, umls_id
                FROM wm_symptoms
                WHERE wm_symptom_id = ? OR symptom_name LIKE ? OR umls_id = ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms, MatchMode.EXACT, MatchMode.LIKE, MatchMode.EXACT);
    }

    /** 查询中医医案信息。 */
    private List<Map<String, Object>> queryMedicalCases(List<String> terms) {
        String sql = """
                SELECT med_case_id, case_report, physician, tcm_disease, wm_disease,
                       tcm_symptoms, wm_symptoms, urination_defecation, pulse_condition,
                       tongue_appearance, tcm_syndrome, tcm_treatment, prescription,
                       herb_composition
                FROM medical_cases
                WHERE med_case_id = ? OR physician LIKE ? OR tcm_disease LIKE ?
                   OR wm_disease LIKE ? OR prescription LIKE ?
                LIMIT ?
                """;
        return queryByTerms(sql, terms,
                MatchMode.EXACT, MatchMode.LIKE, MatchMode.LIKE, MatchMode.LIKE, MatchMode.LIKE);
    }

    /**
     * 逐个查询词执行固定 SQL，并对结果去重。
     *
     * @param sql 固定 SQL，最后一个占位符必须是 LIMIT
     * @param terms 查询词列表
     * @param matchModes LIMIT 之前每个占位符的匹配模式
     */
    private List<Map<String, Object>> queryByTerms(String sql, List<String> terms, MatchMode... matchModes) {
        List<Map<String, Object>> results = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String term : terms) {
            if (results.size() >= MAX_TOTAL_ROWS) {
                break;
            }

            Object[] args = buildArgs(term, MAX_TOTAL_ROWS - results.size(), matchModes);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
            for (Map<String, Object> row : rows) {
                // 使用整行 JSON 做去重 key，避免多个 names 命中同一条记录时重复返回。
                String key = JSONUtil.toJsonStr(row);
                if (seen.add(key)) {
                    results.add(row);
                }
                if (results.size() >= MAX_TOTAL_ROWS) {
                    break;
                }
            }
        }

        return results;
    }

    /**
     * 构造 JdbcTemplate 参数数组。
     *
     * <p>EXACT 用原始查询词；LIKE 会自动包裹百分号；最后追加 LIMIT 参数。
     */
    private Object[] buildArgs(String term, int limit, MatchMode... matchModes) {
        Object[] args = new Object[matchModes.length + 1];
        for (int i = 0; i < matchModes.length; i++) {
            args[i] = matchModes[i] == MatchMode.LIKE ? "%" + term + "%" : term;
        }
        args[matchModes.length] = limit;
        return args;
    }

    /**
     * 标准化实体类型，减少模型传参格式差异导致的失败。
     */
    private String normalizeEntityType(String entityType) {
        if (!StringUtils.hasText(entityType)) {
            return "";
        }
        return entityType.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    /**
     * 拆分多个实体名称，并保持输入顺序去重。
     */
    private List<String> splitNames(String names) {
        if (!StringUtils.hasText(names)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String name : names.split("[,，;；\\n]+")) {
            String term = name.trim();
            if (StringUtils.hasText(term) && seen.add(term)) {
                result.add(term);
            }
        }
        return result;
    }

    /**
     * 统一错误返回格式，避免工具异常时模型拿到非结构化内容。
     */
    private String error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("error", message);
        return JSONUtil.toJsonStr(result);
    }

    /** SQL 占位符匹配模式。 */
    private enum MatchMode {
        /** 精确匹配：字段 = ? */
        EXACT,
        /** 模糊匹配：字段 LIKE ? */
        LIKE
    }
}
