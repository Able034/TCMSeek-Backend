package com.tcmseek.tcmseekagentservice.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.io.Serializable;

@Data
public class Neo4jEntity implements Serializable {

    @Description("Neo4j 节点标签，只能从知识图谱 schema 的节点标签中选择")
    private String label;

    @Description("用户原文中明确出现的实体名称")
    private String name;
}
