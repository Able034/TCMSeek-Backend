package com.tcmseek.tcmseekagentservice.ai.tools.mysql;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import com.tcmseek.tcmseekagentservice.ai.tools.BaseTool;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
//@Component
public class KnowMysqlFrameTool extends BaseTool {
    private static final String SQL_SCHEMA = ResourceUtil.readStr("prompt/SQL_SCHEMA.txt", StandardCharsets.UTF_8);
    @Override
    public String getToolName() {
        return "knowMysqlFrame";
    }

    @Override
    public String getDisplayName() {
        return "获取中医药信息mysql数据库结构";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "[工具调用]Ai查询了mysql的架构";
    }

//    @Tool("获取知识图谱结构定义。\n" +
//            "当需要生成Cypher时调用。")
    public String knowMysqlFrame() {
        log.info("[工具调用]Ai查询了neo4j的架构");
        return SQL_SCHEMA;
    }
}
