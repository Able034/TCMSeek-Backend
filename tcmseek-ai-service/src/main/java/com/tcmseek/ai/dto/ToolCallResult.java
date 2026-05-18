package com.tcmseek.ai.dto;

import java.util.HashMap;
import java.util.Map;

public class ToolCallResult {

    private String toolName;

    private Map<String, Object> arguments = new HashMap<>();

    private GraphToolResult result;

    public ToolCallResult() {
    }

    public ToolCallResult(String toolName, Map<String, Object> arguments, GraphToolResult result) {
        this.toolName = toolName;
        this.arguments = arguments;
        this.result = result;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    public GraphToolResult getResult() {
        return result;
    }

    public void setResult(GraphToolResult result) {
        this.result = result;
    }
}
