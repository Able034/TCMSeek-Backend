package com.tcmseek.ai.service;

import com.tcmseek.ai.config.AiRuntimeProperties;
import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentOrchestratorTest {

    private ToolExecutionRecorder recorder;

    private ToolFallbackService fallbackService;

    private AiRuntimeProperties properties;

    private AgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        recorder = mock(ToolExecutionRecorder.class);
        fallbackService = mock(ToolFallbackService.class);
        properties = mock(AiRuntimeProperties.class);
        when(properties.getAgentMaxSteps()).thenReturn(2);
        when(properties.isSemanticFallbackEnabled()).thenReturn(false);
        orchestrator = new AgentOrchestrator(recorder, fallbackService, properties);
    }

    @Test
    void usesRuleFallbackBeforeModelCall() {
        ToolCallResult fallbackTool = toolCall("findHerbCompounds");
        AtomicBoolean modelCalled = new AtomicBoolean(false);
        when(recorder.finish()).thenReturn(List.of(fallbackTool));
        when(fallbackService.tryExecute("ginseng compounds", false)).thenReturn(true);

        AgentOrchestrator.AgentRunResult result = orchestrator.run("ginseng compounds", () -> {
            modelCalled.set(true);
            return "model reply";
        });

        assertThat(result.reply()).isBlank();
        assertThat(result.toolResults()).containsExactly(fallbackTool);
        assertThat(result.outcome()).isEqualTo(AgentOrchestrator.AgentOutcome.FALLBACK_TOOL);
        assertThat(modelCalled).isFalse();
        verify(fallbackService).tryExecute("ginseng compounds", false);
    }

    @Test
    void keepsEmptyFallbackToolObservationForNoResultAnswer() {
        when(properties.isSemanticFallbackEnabled()).thenReturn(true);
        ToolCallResult emptyFallbackTool = emptyToolCall("findDiseasePrescriptions");
        when(recorder.finish()).thenReturn(List.of(emptyFallbackTool));
        when(fallbackService.tryExecute("depression formulas", true)).thenReturn(true);

        AgentOrchestrator.AgentRunResult result = orchestrator.run("depression formulas", () -> "model reply");

        assertThat(result.toolResults()).containsExactly(emptyFallbackTool);
        assertThat(result.outcome()).isEqualTo(AgentOrchestrator.AgentOutcome.FALLBACK_TOOL);
        verify(fallbackService).tryExecute("depression formulas", true);
    }

    @Test
    void treatsNoDirectRelationAsUsefulFallbackObservation() {
        ToolCallResult noDirect = new ToolCallResult(
                "findDiseasePrescriptions",
                Map.of("diseaseName", "depression"),
                GraphToolResult.of("disease_prescriptions_no_direct_relation", List.of()));
        when(recorder.finish()).thenReturn(List.of(noDirect));
        when(fallbackService.tryExecute("depression formulas", false)).thenReturn(true);

        AgentOrchestrator.AgentRunResult result = orchestrator.run("depression formulas", () -> "model reply");

        assertThat(result.toolResults()).containsExactly(noDirect);
        assertThat(result.outcome()).isEqualTo(AgentOrchestrator.AgentOutcome.FALLBACK_TOOL);
        verify(fallbackService).tryExecute("depression formulas", false);
    }

    @Test
    void returnsModelOnlyWhenFallbackIsNotApplicable() {
        when(recorder.finish())
                .thenReturn(List.<ToolCallResult>of())
                .thenReturn(List.<ToolCallResult>of());
        when(fallbackService.tryExecute("hello", false)).thenReturn(false);

        AgentOrchestrator.AgentRunResult result = orchestrator.run("hello", () -> "hello");

        assertThat(result.reply()).isEqualTo("hello");
        assertThat(result.toolResults()).isEmpty();
        assertThat(result.outcome()).isEqualTo(AgentOrchestrator.AgentOutcome.MODEL_ONLY);
        verify(fallbackService).tryExecute("hello", false);
    }

    @Test
    void stillCallsModelWhenFallbackIsNotApplicableAndActionBudgetIsOne() {
        when(properties.getAgentMaxSteps()).thenReturn(1);
        when(recorder.finish())
                .thenReturn(List.<ToolCallResult>of())
                .thenReturn(List.<ToolCallResult>of());
        when(fallbackService.tryExecute("ginseng effects", false)).thenReturn(false);

        AgentOrchestrator.AgentRunResult result = orchestrator.run("ginseng effects", () -> "model only");

        assertThat(result.reply()).isEqualTo("model only");
        assertThat(result.toolResults()).isEmpty();
        assertThat(result.outcome()).isEqualTo(AgentOrchestrator.AgentOutcome.MODEL_ONLY);
        verify(fallbackService).tryExecute("ginseng effects", false);
    }

    private ToolCallResult toolCall(String toolName) {
        return new ToolCallResult(
                toolName,
                Map.of("name", "ginseng"),
                GraphToolResult.of("test_result", List.of(Map.of("item", "value"))));
    }

    private ToolCallResult emptyToolCall(String toolName) {
        return new ToolCallResult(
                toolName,
                Map.of("name", "missing"),
                GraphToolResult.of("test_result", List.of()));
    }
}
