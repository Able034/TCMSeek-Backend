package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import com.tcmseek.ai.config.AiToolProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolResultCompressor {

    public static final int ANSWER_ITEM_LIMIT = 20;

    private final AiToolProperties toolProperties;

    public ToolResultCompressor(AiToolProperties toolProperties) {
        this.toolProperties = toolProperties;
    }

    public int getAnswerItemLimit() {
        return Math.max(1, toolProperties.getAnswerItemLimit());
    }

    public List<ToolCallResult> forAnswer(List<ToolCallResult> toolResults) {
        if (CollectionUtils.isEmpty(toolResults)) {
            return List.of();
        }
        List<ToolCallResult> compressed = new ArrayList<>();
        for (ToolCallResult toolResult : toolResults) {
            compressed.add(compress(toolResult));
        }
        List<ToolCallResult> resultsWithData = compressed.stream()
                .filter(this::hasData)
                .toList();
        if (!resultsWithData.isEmpty()) {
            List<ToolCallResult> noDirectDiseaseResults = resultsWithData.stream()
                    .filter(this::isNoDirectPrescriptionRelation)
                    .toList();
            if (!noDirectDiseaseResults.isEmpty()) {
                return noDirectDiseaseResults;
            }
            return resultsWithData;
        }
        return compressed;
    }

    public Integer firstTotal(List<ToolCallResult> toolResults) {
        GraphToolResult result = firstResult(toolResults);
        return result == null ? null : result.getTotal();
    }

    public Integer firstDisplayed(List<ToolCallResult> toolResults) {
        GraphToolResult result = firstResult(toolResults);
        if (result == null || result.getItems() == null) {
            return null;
        }
        return Math.min(result.getItems().size(), getAnswerItemLimit());
    }

    public boolean isFirstTruncated(List<ToolCallResult> toolResults) {
        GraphToolResult result = firstResult(toolResults);
        return result != null
                && result.getItems() != null
                && result.getTotal() > getAnswerItemLimit()
                && result.getItems().size() > getAnswerItemLimit();
    }

    private ToolCallResult compress(ToolCallResult source) {
        if (source == null) {
            return null;
        }
        GraphToolResult sourceResult = source.getResult();
        GraphToolResult compressedResult = null;
        if (sourceResult != null) {
            List<Map<String, Object>> limitedItems = limitItems(sourceResult.getItems());
            compressedResult = new GraphToolResult(
                    sourceResult.getQueryType(),
                    sourceResult.getTotal(),
                    limitedItems);
        }
        return new ToolCallResult(
                source.getToolName(),
                source.getArguments() == null ? new HashMap<>() : new HashMap<>(source.getArguments()),
                compressedResult);
    }

    private boolean hasData(ToolCallResult toolResult) {
        return toolResult != null
                && toolResult.getResult() != null
                && toolResult.getResult().getItems() != null
                && !toolResult.getResult().getItems().isEmpty();
    }

    private boolean isNoDirectPrescriptionRelation(ToolCallResult toolResult) {
        if (toolResult == null || toolResult.getResult() == null) {
            return false;
        }
        String queryType = toolResult.getResult().getQueryType();
        if ("disease_prescriptions_no_direct_relation".equals(queryType)
                || "condition_prescriptions_no_direct_relation".equals(queryType)) {
            return true;
        }
        List<Map<String, Object>> items = toolResult.getResult().getItems();
        if (items == null || items.isEmpty()) {
            return false;
        }
        return items.stream()
                .anyMatch(item -> item != null
                        && ("semantic_disease_anchor_no_direct_prescription".equals(item.get("evidenceType"))
                        || "semantic_condition_anchor_no_direct_prescription".equals(item.get("evidenceType"))));
    }

    private List<Map<String, Object>> limitItems(List<Map<String, Object>> sourceItems) {
        if (sourceItems == null || sourceItems.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> limited = new ArrayList<>();
        int size = Math.min(sourceItems.size(), getAnswerItemLimit());
        for (int i = 0; i < size; i++) {
            Map<String, Object> item = sourceItems.get(i);
            limited.add(item == null ? Map.of() : new HashMap<>(item));
        }
        return limited;
    }

    private GraphToolResult firstResult(List<ToolCallResult> toolResults) {
        if (CollectionUtils.isEmpty(toolResults)) {
            return null;
        }
        for (ToolCallResult toolResult : toolResults) {
            if (toolResult != null && toolResult.getResult() != null) {
                return toolResult.getResult();
            }
        }
        return null;
    }
}
