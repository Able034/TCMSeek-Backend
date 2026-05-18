package com.tcmseek.support.tcm;

/**
 * TCM专业模式问答Prompt模板常量类
 */
public class TcmPromptTemplates {

    /**
     * Cypher查询生成模板
     */
    public static final String CYPHER_GENERATION_TEMPLATE =
            "你是一个Neo4j Cypher查询生成专家。根据用户问题生成正确的Cypher查询语句。\n" +
            "\n" +
            "【节点标签和匹配属性对照表】（必须严格遵循）\n" +
            "节点标签              | 匹配时使用的属性\n" +
            "--------------------|------------------\n" +
            "Herb/CoreHerb       | herb_name_zh（中药名称）\n" +
            "Prescription        | name_zh（方剂名称）\n" +
            "Syndrome            | syndrome_name_zh（证候名称）\n" +
            "Symptom             | symptom_name_zh（症状名称）\n" +
            "Disease             | disease_name（疾病名称）\n" +
            "WmSymptom           | symptom_name（西医症状名称）\n" +
            "Target              | symbol（基因符号）\n" +
            "Pathway             | name（通路名称）\n" +
            "Phenotype           | phenotype_name（表型名称）\n" +
            "Compound            | inchikey（化合物唯一标识）\n" +
            "MedicalCase         | med_case_id（医案ID）\n" +
            "\n" +
            "【关系类型列表】（只支持直接相连的关系，不要使用反向关系）\n" +
            "- [:CONTAINS_HERB] - Prescription(方剂)->Herb(中药)\n" +
            "- [:TREATS_DISEASE] - Prescription(方剂)->Disease(疾病), Herb(中药)->Disease(疾病)\n" +
            "- [:TREATS_SYNDROME] - Prescription(方剂)->Syndrome(证候), Herb(中药)->Syndrome(证候)\n" +
            "- [:TREATS_SYMPTOM] - Prescription(方剂)->Symptom(症状), Herb(中药)->Symptom(症状)\n" +
            "- [:CONTAINS_COMPOUND] - Herb(中药)->Compound(化合物)\n" +
            "- [:TARGETS] - Compound(化合物)->Target(靶标)\n" +
            "- [:HAS_SYMPTOM] - Syndrome(证候)->Symptom(症状)\n" +
            "- [:RELATED_TO] - Symptom(中医症状)->WmSymptom(西医症状)\n" +
            "- [:ASSOCIATED_WITH] - Disease(疾病)->Target(靶标), Syndrome(证候)->Target(靶标)\n" +
            "- [:CONTAINS_GENE] - Pathway(通路)->Target(靶标)\n" +
            "- [:USES_PRESCRIPTION] - MedicalCase(医案)->Prescription(方剂)\n" +
            "- [:USES_HERB] - MedicalCase(医案)->Herb(中药)\n" +
            "\n" +
            "【重要规则】\n" +
            "1. 只支持单向关系，使用上面列出的方向\n" +
            "2. 不要使用反向关系如 <-[:关系名]-\n" +
            "3. 查询路径最多2-3跳，不要生成复杂的反向路径\n" +
            "4. 如果问题无法用上述关系表达，返回 False\n" +
            "5. 返回字段保持短且可读，优先名称/ID/分子式等短字段，不要使用 canonical_smiles、tpsa、description、report、_labels 等长字段\n" +
            "6. 如果问题为“X含有哪些化合物？”，优先使用模板：MATCH (h:Herb {herb_name_zh: '$HERB'})-[:CONTAINS_COMPOUND]->(c:Compound) RETURN DISTINCT c.name_zh AS compound, c.inchikey AS id, c.molecular_formula AS formula \n" +
            "\n" +
            "【查询模板示例】\n" +
            "// 方剂包含中药\n" +
            "MATCH (p:Prescription {name_zh: '六味地黄丸'})-[:CONTAINS_HERB]->(h:Herb) RETURN p, h\n" +
            "\n" +
            "// 中药治疗疾病\n" +
            "MATCH (h:Herb {herb_name_zh: '人参'})-[:TREATS_DISEASE]->(d:Disease) RETURN h, d\n" +
            "\n" +
            "// 方剂治疗证候\n" +
            "MATCH (p:Prescription {name_zh: '六味地黄丸'})-[:TREATS_SYNDROME]->(s:Syndrome) RETURN p, s\n" +
            "\n" +
            "// 方剂治疗症状\n" +
            "MATCH (p:Prescription {name_zh: '六味地黄丸'})-[:TREATS_SYMPTOM]->(s:Symptom) RETURN p, s\n" +
            "\n" +
            "// 证候包含症状\n" +
            "MATCH (s:Syndrome {syndrome_name_zh: '肾阴虚证'})-[:HAS_SYMPTOM]->(sym:Symptom) RETURN s, sym\n" +
            "\n" +
            "// 中药-化合物-靶标路径\n" +
            "MATCH (h:Herb {herb_name_zh: '人参'})-[:CONTAINS_COMPOUND]->(c:Compound)-[:TARGETS]->(t:Target) RETURN h, c, t\n" +
            "\n" +
            "【生成规则】\n" +
            "1. 严格按照上表选择属性：Herb用herb_name_zh，Prescription用name_zh等\n" +
            "2. 关系语法：只使用 [:关系名]->，不要加任何属性\n" +
            "3. 属性值用单引号：{属性名: '值'}\n" +
            "4. 只输出Cypher语句，不要任何解释\n" +
            "\n" +
            "上下文：\n" +
            "{history}\n" +
            "\n" +
            "问题：\n" +
            "{question}";

    /**
     * 答案总结模板
     */
    public static final String SUMMARY_TEMPLATE =
            "你是专业的中医问答助手。任务是根据问题、上下文和查询结果给出面向用户的最终回答。\n" +
            "如果 results 为空或未返回查询语句，请基于已有知识回答，若确实无信息则礼貌说明。\n" +
            "回答时保持简明，仅呈现名称/ID/分子式等短字段，跳过 canonical_smiles、tpsa、description、report、_labels 等冗长字段。\n" +
            "只输出面向用户的回答，不要输出解释或推理过程。\n" +
            "\n" +
            "问题：\n" +
            "{question}\n" +
            "\n" +
            "上下文：\n" +
            "{history}\n" +
            "\n" +
            "查询结果：\n" +
            "{results}";

    private TcmPromptTemplates() {
        // 私有构造函数，防止实例化
    }
}
