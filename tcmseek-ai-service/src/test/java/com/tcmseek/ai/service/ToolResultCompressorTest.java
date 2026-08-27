package com.tcmseek.ai.service;

import com.tcmseek.ai.config.AiToolProperties;
import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolResultCompressorTest {

    @Test
    void dropsEmptyResultsWhenAnyResultHasData() {
        ToolResultCompressor compressor = new ToolResultCompressor(new AiToolProperties());
        ToolCallResult empty = new ToolCallResult(
                "findDiseasePrescriptions",
                Map.of("diseaseName", "depression"),
                GraphToolResult.of("disease_prescriptions", List.of()));
        ToolCallResult semantic = new ToolCallResult(
                "findBySemanticIntent",
                Map.of("question", "depression formula"),
                GraphToolResult.of("semantic_intent", List.of(Map.of("prescription", "Jieyu formula"))));

        List<ToolCallResult> results = compressor.forAnswer(List.of(empty, semantic));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getToolName()).isEqualTo("findBySemanticIntent");
        assertThat(results.get(0).getResult().getItems()).hasSize(1);
    }

    @Test
    void keepsEmptyResultsWhenAllResultsAreEmpty() {
        ToolResultCompressor compressor = new ToolResultCompressor(new AiToolProperties());
        ToolCallResult empty = new ToolCallResult(
                "findDiseasePrescriptions",
                Map.of("diseaseName", "unknown"),
                GraphToolResult.of("disease_prescriptions", List.of()));

        List<ToolCallResult> results = compressor.forAnswer(List.of(empty));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getToolName()).isEqualTo("findDiseasePrescriptions");
        assertThat(results.get(0).getResult().getItems()).isEmpty();
    }

    @Test
    void noDirectDiseasePrescriptionResultSuppressesWeakSemanticCandidates() {
        ToolResultCompressor compressor = new ToolResultCompressor(new AiToolProperties());
        ToolCallResult noDirect = new ToolCallResult(
                "findDiseasePrescriptions",
                Map.of("diseaseName", "depression"),
                GraphToolResult.of("disease_prescriptions_no_direct_relation", List.of(Map.of(
                        "disease", "depressive disorder",
                        "evidenceType", "semantic_disease_anchor_no_direct_prescription"))));
        ToolCallResult semantic = new ToolCallResult(
                "findBySemanticIntent",
                Map.of("question", "depression formula"),
                GraphToolResult.of("semantic_intent", List.of(Map.of("prescription", "Jieyu formula"))));

        List<ToolCallResult> results = compressor.forAnswer(List.of(noDirect, semantic));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getResult().getQueryType()).isEqualTo("disease_prescriptions_no_direct_relation");
    }

    @Test
    void noDirectConditionPrescriptionResultSuppressesWeakSemanticCandidates() {
        ToolResultCompressor compressor = new ToolResultCompressor(new AiToolProperties());
        ToolCallResult noDirect = new ToolCallResult(
                "findBySemanticIntent",
                Map.of("question", "muscle pain formula"),
                GraphToolResult.of("condition_prescriptions_no_direct_relation", List.of(Map.of(
                        "condition", "muscle pain",
                        "evidenceType", "semantic_condition_anchor_no_direct_prescription"))));
        ToolCallResult semantic = new ToolCallResult(
                "findBySemanticIntent",
                Map.of("question", "muscle pain formula"),
                GraphToolResult.of("semantic_intent", List.of(Map.of("prescription", "name-similar formula"))));

        List<ToolCallResult> results = compressor.forAnswer(List.of(noDirect, semantic));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getResult().getQueryType()).isEqualTo("condition_prescriptions_no_direct_relation");
    }
}
