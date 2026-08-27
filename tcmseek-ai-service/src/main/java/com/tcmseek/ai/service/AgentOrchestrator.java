package com.tcmseek.ai.service;

import com.tcmseek.ai.config.AiRuntimeProperties;
import com.tcmseek.ai.dto.ToolCallResult;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class AgentOrchestrator {

    private final ToolExecutionRecorder toolExecutionRecorder;

    private final ToolFallbackService toolFallbackService;

    private final AiRuntimeProperties runtimeProperties;

    public AgentOrchestrator(ToolExecutionRecorder toolExecutionRecorder,
                             ToolFallbackService toolFallbackService,
                             AiRuntimeProperties runtimeProperties) {
        this.toolExecutionRecorder = toolExecutionRecorder;
        this.toolFallbackService = toolFallbackService;
        this.runtimeProperties = runtimeProperties;
    }

    public AgentRunResult run(String latestQuestion, Supplier<String> modelCall) {
        return run(latestQuestion, modelCall, runtimeProperties.isSemanticFallbackEnabled());
    }

    public AgentRunResult run(String latestQuestion, Supplier<String> modelCall, boolean semanticFallbackEnabled) {
        List<AgentStep> steps = new ArrayList<>();
        int maxActions = maxActions();
        int actionsUsed = 0;

        steps.add(new AgentStep("classify", StringUtils.hasText(latestQuestion) ? "question_received" : "empty_question"));
        steps.add(new AgentStep("plan", "rule_fallback_first_then_model_answer"));

        FallbackObservation fallbackObservation = callFallback(latestQuestion, semanticFallbackEnabled);
        if (fallbackObservation.executed()) {
            actionsUsed++;
        }
        steps.add(new AgentStep("act", "rule_fallback"));
        steps.add(new AgentStep("observe", "fallback_tool_calls=" + fallbackObservation.toolResults().size()));
        if (hasUsefulToolObservation(fallbackObservation.toolResults())
                || (semanticFallbackEnabled && fallbackObservation.executed()
                && !CollectionUtils.isEmpty(fallbackObservation.toolResults()))) {
            steps.add(new AgentStep("verify", "fallback_tool_observation_available"));
            return new AgentRunResult(
                    "",
                    fallbackObservation.toolResults(),
                    AgentOutcome.FALLBACK_TOOL,
                    steps,
                    actionsUsed);
        }

        if (actionsUsed < maxActions) {
            ModelObservation modelObservation = callModel(modelCall);
            actionsUsed++;
            steps.add(new AgentStep("replan", fallbackObservation.executed()
                    ? "fallback_observation_not_useful"
                    : "rule_fallback_not_applicable"));
            steps.add(new AgentStep("act", "model_without_tools"));
            steps.add(new AgentStep("observe", "model_tool_calls=" + modelObservation.toolResults().size()));
            if (hasUsefulToolObservation(modelObservation.toolResults())) {
                steps.add(new AgentStep("verify", "tool_observation_available"));
                return new AgentRunResult(
                        modelObservation.reply(),
                        modelObservation.toolResults(),
                        AgentOutcome.MODEL_TOOL,
                        steps,
                        actionsUsed);
            }
            if (!CollectionUtils.isEmpty(modelObservation.toolResults())) {
                steps.add(new AgentStep("verify", "tool_observation_empty"));
            }
            steps.add(new AgentStep("verify", "model_only_answer"));
            return new AgentRunResult(
                    modelObservation.reply(),
                    List.of(),
                    AgentOutcome.MODEL_ONLY,
                    steps,
                    actionsUsed);
        } else {
            steps.add(new AgentStep("replan", "action_budget_exhausted"));
        }

        steps.add(new AgentStep("verify", "no_usable_observation"));
        return new AgentRunResult(
                "",
                List.of(),
                AgentOutcome.MODEL_ONLY,
                steps,
                actionsUsed);
    }

    private ModelObservation callModel(Supplier<String> modelCall) {
        List<ToolCallResult> toolResults;
        String reply;
        toolExecutionRecorder.start();
        try {
            reply = modelCall.get();
        } finally {
            toolResults = toolExecutionRecorder.finish();
        }
        return new ModelObservation(reply, toolResults);
    }

    private FallbackObservation callFallback(String latestQuestion, boolean semanticFallbackEnabled) {
        List<ToolCallResult> toolResults;
        boolean executed = false;
        toolExecutionRecorder.start();
        try {
            executed = toolFallbackService.tryExecute(latestQuestion, semanticFallbackEnabled);
        } finally {
            toolResults = toolExecutionRecorder.finish();
        }
        return new FallbackObservation(executed, toolResults);
    }

    private int maxActions() {
        return Math.max(1, runtimeProperties.getAgentMaxSteps());
    }

    private boolean hasUsefulToolObservation(List<ToolCallResult> toolResults) {
        if (CollectionUtils.isEmpty(toolResults)) {
            return false;
        }
        return toolResults.stream().anyMatch(toolResult -> {
            if (toolResult == null || toolResult.getResult() == null) {
                return false;
            }
            String queryType = toolResult.getResult().getQueryType();
            if (StringUtils.hasText(queryType) && queryType.endsWith("_no_direct_relation")) {
                return true;
            }
            return toolResult.getResult().getItems() != null
                    && !toolResult.getResult().getItems().isEmpty();
        });
    }

    public enum AgentOutcome {
        MODEL_ONLY,
        MODEL_TOOL,
        FALLBACK_TOOL
    }

    public record AgentRunResult(String reply,
                                 List<ToolCallResult> toolResults,
                                 AgentOutcome outcome,
                                 List<AgentStep> steps,
                                 int actionsUsed) {

        public String describeSteps() {
            if (steps == null || steps.isEmpty()) {
                return "";
            }
            return steps.stream()
                    .map(step -> step.phase() + ":" + step.detail())
                    .collect(Collectors.joining(" -> "));
        }
    }

    public record AgentStep(String phase, String detail) {
    }

    private record ModelObservation(String reply, List<ToolCallResult> toolResults) {
    }

    private record FallbackObservation(boolean executed, List<ToolCallResult> toolResults) {
    }
}
