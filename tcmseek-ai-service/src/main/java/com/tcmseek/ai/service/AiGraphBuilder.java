package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.AiGraphData;
import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AiGraphBuilder {

    private static final int DISPLAY_LIMIT = 100;

    public AiGraphData build(List<ToolCallResult> toolResults) {
        if (CollectionUtils.isEmpty(toolResults)) {
            return null;
        }
        for (ToolCallResult toolResult : toolResults) {
            AiGraphData graph = buildFromTool(toolResult);
            if (graph != null) {
                return graph;
            }
        }
        return null;
    }

    private AiGraphData buildFromTool(ToolCallResult call) {
        if (call == null || call.getResult() == null) {
            return null;
        }
        String queryType = call.getResult().getQueryType();
        if ("common_targets".equals(queryType)) {
            return buildCommonTargetGraph(call);
        }
        if ("common_compounds".equals(queryType)) {
            return buildCommonCompoundGraph(call);
        }
        if ("herb_compounds".equals(queryType)) {
            return buildOneToManyGraph(call, "herbName", "herb", "compound", "compound", "contains");
        }
        if ("prescription_herbs".equals(queryType)) {
            return buildOneToManyGraph(call, "prescriptionName", "prescription", "herb", "herb", "contains");
        }
        if ("herb_diseases".equals(queryType)) {
            return buildOneToManyGraph(call, "herbName", "herb", "disease", "disease", "treats");
        }
        if ("prescription_symptoms".equals(queryType)) {
            return buildOneToManyGraph(call, "prescriptionName", "prescription", "symptom", "symptom", "treats");
        }
        if ("prescription_syndromes".equals(queryType)) {
            return buildOneToManyGraph(call, "prescriptionName", "prescription", "syndrome", "syndrome", "treats");
        }
        if ("disease_targets".equals(queryType)) {
            return buildOneToManyGraph(call, "diseaseName", "disease", "target", "target", "associated");
        }
        if ("syndrome_symptoms".equals(queryType)) {
            return buildOneToManyGraph(call, "syndromeName", "syndrome", "symptom", "symptom", "has");
        }
        if ("compound_targets".equals(queryType)) {
            return buildOneToManyGraph(call, "compound", "compound", "target", "target", "targets");
        }
        if ("pathway_targets".equals(queryType)) {
            return buildOneToManyGraph(call, "pathwayName", "pathway", "target", "target", "contains");
        }
        if ("medicalcase_prescriptions".equals(queryType)) {
            return buildOneToManyGraph(call, "caseId", "medicalcase", "prescription", "prescription", "uses");
        }
        return null;
    }

    private AiGraphData buildCommonTargetGraph(ToolCallResult call) {
        GraphToolResult result = call.getResult();
        List<String> herbs = herbNames(call);
        Set<String> targets = uniqueColumnValues(result.getItems(), "target");
        if (herbs.size() < 2 || targets.isEmpty()) {
            return null;
        }

        AiGraphData graph = baseGraph(result, "target", "target");
        graph.setHerbs(herbs);
        for (String herb : herbs) {
            graph.getNodes().add(new AiGraphData.GraphNode("herb:" + herb, herb, "herb"));
        }
        for (String target : targets) {
            String targetId = "target:" + target;
            graph.getNodes().add(new AiGraphData.GraphNode(targetId, target, "target"));
            for (String herb : herbs) {
                graph.getEdges().add(new AiGraphData.GraphEdge("herb:" + herb, targetId, "herb-target"));
            }
        }
        graph.setDisplayedTargets(targets.size());
        return graph;
    }

    private AiGraphData buildCommonCompoundGraph(ToolCallResult call) {
        GraphToolResult result = call.getResult();
        List<String> herbs = herbNames(call);
        Set<String> compounds = uniqueColumnValues(result.getItems(), "compound");
        if (herbs.size() < 2 || compounds.isEmpty()) {
            return null;
        }

        AiGraphData graph = baseGraph(result, "compound", "compound");
        graph.setHerbs(herbs);
        for (String herb : herbs) {
            graph.getNodes().add(new AiGraphData.GraphNode("herb:" + herb, herb, "herb"));
        }
        for (String compound : compounds) {
            String compoundId = "compound:" + compound;
            graph.getNodes().add(new AiGraphData.GraphNode(compoundId, compound, "compound"));
            for (String herb : herbs) {
                graph.getEdges().add(new AiGraphData.GraphEdge("herb:" + herb, compoundId, "contains"));
            }
        }
        graph.setDisplayedTargets(compounds.size());
        return graph;
    }

    private AiGraphData buildOneToManyGraph(ToolCallResult call,
                                            String mainArgumentName,
                                            String mainType,
                                            String itemKey,
                                            String entityType,
                                            String edgeType) {
        GraphToolResult result = call.getResult();
        String mainName = asString(call.getArguments().get(mainArgumentName));
        Set<String> items = uniqueColumnValues(result.getItems(), itemKey);
        if (!StringUtils.hasText(mainName) || items.isEmpty()) {
            return null;
        }

        AiGraphData graph = baseGraph(result, entityType, itemKey);
        graph.setMainName(mainName);
        graph.setMainType(mainType);
        String mainId = mainType + ":" + mainName;
        graph.getNodes().add(new AiGraphData.GraphNode(mainId, mainName, mainType));
        for (String item : items) {
            String itemId = entityType + ":" + item;
            graph.getNodes().add(new AiGraphData.GraphNode(itemId, item, entityType));
            graph.getEdges().add(new AiGraphData.GraphEdge(mainId, itemId, edgeType));
        }
        graph.setDisplayedTargets(items.size());
        return graph;
    }

    private AiGraphData baseGraph(GraphToolResult result, String entityType, String entityKey) {
        AiGraphData graph = new AiGraphData();
        graph.setTotalTargets(result.getTotal());
        graph.setEntityType(entityType);
        graph.setEntityKey(entityKey);
        return graph;
    }

    private Set<String> uniqueColumnValues(List<Map<String, Object>> rows, String key) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (rows == null) {
            return values;
        }
        for (Map<String, Object> row : rows) {
            if (row == null || !row.containsKey(key)) {
                continue;
            }
            String value = asString(row.get(key));
            if (StringUtils.hasText(value)) {
                values.add(value);
            }
            if (values.size() >= DISPLAY_LIMIT) {
                break;
            }
        }
        return values;
    }

    private List<String> herbNames(ToolCallResult call) {
        LinkedHashSet<String> herbs = new LinkedHashSet<>();
        if (call == null || call.getArguments() == null) {
            return new ArrayList<>();
        }
        addHerbValues(herbs, call.getArguments().get("herbNames"));
        addHerbValues(herbs, call.getArguments().get("herbs"));
        if (herbs.isEmpty()) {
            addHerbValues(herbs, call.getArguments().get("herbA"));
            addHerbValues(herbs, call.getArguments().get("herbB"));
        }
        return new ArrayList<>(herbs);
    }

    private void addHerbValues(Set<String> herbs, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Iterable<?> values) {
            for (Object item : values) {
                addHerbValues(herbs, item);
            }
            return;
        }
        String cleaned = asString(value)
                .replaceAll("[\\[\\]{}()（）\"'“”‘’]", "")
                .trim();
        if (!StringUtils.hasText(cleaned)) {
            return;
        }
        String[] parts = cleaned.split("以及|和|与|及|跟|、|,|，|;|；|/");
        for (String part : parts) {
            String herb = part
                    .replaceAll("^[：:，,、。\\s]+", "")
                    .replaceAll("[：:，,、。？?\\s]+$", "")
                    .trim();
            if (StringUtils.hasText(herb)) {
                herbs.add(herb);
            }
        }
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }
}
