package com.tcmseek.tcmseekagentservice.ai.tools;

import com.tcmseek.tcmseekagentservice.ai.tools.neo4j.Neo4jSearchTool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class Neo4jSearchToolSpecTest {

    @Test
    void exposesStableNeo4jSearchToolSchema() {
        ToolSpecification specification = ToolSpecifications.toolSpecificationsFrom(Neo4jSearchTool.class)
                .stream()
                .filter(tool -> "neo4jSearch".equals(tool.name()))
                .findFirst()
                .orElseThrow();

        Map<String, ?> properties = specification.parameters().properties();

        assertThat(specification.description()).contains("查询中医知识图谱");
        assertThat(properties).containsOnlyKeys("startLabel", "startName", "relationships", "limit");
        assertThat(specification.parameters().required())
                .containsExactlyInAnyOrder("startLabel", "startName", "relationships", "limit");
        assertThat(specification.parameters().properties().get("relationships").description())
                .contains("关系列表");
    }
}
