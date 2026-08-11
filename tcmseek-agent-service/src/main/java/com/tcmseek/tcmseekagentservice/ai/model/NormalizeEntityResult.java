package com.tcmseek.tcmseekagentservice.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户初始Query的主体结果
 */
@Description("用户初始Query的主体结果")
@Data
public class NormalizeEntityResult implements Serializable {
    /**
     * 实体
     */
    @Description("识别出的实体")
    private List<Neo4jEntity> neo4jEntities;
}
