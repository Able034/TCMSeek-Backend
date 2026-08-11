package com.tcmseek.tcmseekagentservice.ai.tools.neo4j;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import com.tcmseek.tcmseekagentservice.ai.tools.BaseTool;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class KnowNeo4jFrameTool extends BaseTool {

    private static final String KG_SCHEMA = ResourceUtil.readStr("prompt/KG_SCHEMA.txt", StandardCharsets.UTF_8);

    @Tool("获取知识图谱结构定义。\n" +
            "当需要生成Cypher时调用。")
    public String knowNeo4jFrame() {
        log.info("[工具调用]Ai查询了neo4j的架构");
        return KG_SCHEMA;
    }

    @Override
    public String getToolName() {
        return "knowNeo4jFrame";
    }

    @Override
    public String getDisplayName() {
        return "获取neo4j的节点、关系架构";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "[工具调用]Ai查询了neo4j的架构";
    }
}
