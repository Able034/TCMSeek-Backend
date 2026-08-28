package com.tcmseek.tcmseekagentservice.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户初始Query的意图识别结果。
 */
@Description("用户初始Query的意图识别结果，用于决定后续执行哪类查询工作流")
@Data
public class ClassifyCodeResult implements Serializable {

    /**
     * 粗粒度领域分类。
     */
    @Description("粗粒度分类，只能是 DATA_QUERY、GENERAL_TCM_QA、NON_TCM、UNSURE 之一")
    private String classifyCode;

    /**
     * 细粒度工作流类型。
     */
    @Description("工作流类型，只能是 GRAPH_RELATION_ONLY、ENTITY_DETAIL_ONLY、GRAPH_THEN_ENTITY_DETAIL、MIXED_GRAPH_AND_ENTITY_DETAIL、GENERAL_TCM_QA、NON_TCM、UNSURE 之一")
    private String workflowType;

    /**
     * 用户真实想询问什么。
     */
    @Description("用一句中文概括用户真实想获得的信息")
    private String queryIntent;

    /**
     * 是否需要 Neo4j 查询实体关系。
     */
    @Description("是否需要调用 Neo4j 查询实体关系、路径或关联对象")
    private Boolean needNeo4j;

    /**
     * 是否需要 MySQL 查询实体详情。
     */
    @Description("是否需要调用 MySQL 查询实体详细字段")
    private Boolean needMysql;

    /**
     * MySQL 查询时机。
     */
    @Description("MySQL 查询时机，只能是 NONE、DIRECT、AFTER_NEO4J、DIRECT_AND_AFTER_NEO4J 之一")
    private String mysqlTiming;

    /**
     * MySQL 需要补充详情的实体类型。
     */
    @Description("MySQL 要查询详情的实体类型数组，例如 HERB、PRESCRIPTION、TARGET")
    private List<String> mysqlEntityTypes;

    /**
     * 分类原因描述。
     */
    @Description("意图识别结果描述，说明为什么选择该工作流")
    private String description;

}
