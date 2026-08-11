package com.tcmseek.tcmseekagentservice.graph.node;

import cn.hutool.json.JSONUtil;
import com.tcmseek.tcmseekagentservice.ai.AiAnswerJudgeService;
import com.tcmseek.tcmseekagentservice.ai.AiClassifyService;
import com.tcmseek.tcmseekagentservice.ai.AiMysqlSearchService;
import com.tcmseek.tcmseekagentservice.ai.AiNormalizeEntityService;
import com.tcmseek.tcmseekagentservice.ai.AiPlannerService;
import com.tcmseek.tcmseekagentservice.ai.AiSearchService;
import com.tcmseek.tcmseekagentservice.ai.AiSummaryService;
import com.tcmseek.tcmseekagentservice.ai.model.ClassifyCodeResult;
import com.tcmseek.tcmseekagentservice.ai.model.NormalizeEntityResult;
import com.tcmseek.tcmseekagentservice.ai.model.workflow.AnswerJudgeResult;
import com.tcmseek.tcmseekagentservice.ai.model.workflow.PlanResult;
import com.tcmseek.tcmseekagentservice.ai.tools.ToolManager;
import com.tcmseek.tcmseekagentservice.graph.state.WorkflowContext;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.ENTITY;
import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.INTENT;
import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.JUDGE;
import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.MYSQL;
import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.NEO4J;
import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.PLANNER;
import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.REPLAN;
import static com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames.SUMMARY;

/**
 * TCMSeek LangGraph4j 节点动作集合。
 *
 * <p>这里承载“节点如何执行”和“节点之间如何路由”的细节；
 * {@code AgentWorkflowService} 只负责把这些节点装配成图并对外暴露运行入口。</p>
 */
public class AgentWorkflowNodes {

    /**
     * 意图识别 Agent，负责输出 workflowType、needNeo4j、needMysql 等规划依据。
     */
    private final AiClassifyService aiClassifyService;

    /**
     * 实体识别 Agent，负责抽取 Neo4j 起点实体，不直接查询数据库。
     */
    private final AiNormalizeEntityService aiNormalizeEntityService;

    /**
     * 主控规划 Agent，负责决定下一步调用哪个子 Agent 或是否进入判断/总结。
     */
    private final AiPlannerService aiPlannerService;

    /**
     * Neo4j 子 Agent，只绑定图谱 schema 和图谱查询工具。
     */
    private final AiSearchService aiSearchService;

    /**
     * MySQL 子 Agent，只绑定实体详情查询工具，避免模型自由生成 SQL。
     */
    private final AiMysqlSearchService aiMysqlSearchService;

    /**
     * 证据完整性判断 Agent，不绑定工具，只判断能否回答。
     */
    private final AiAnswerJudgeService aiAnswerJudgeService;

    /**
     * 最终总结 Agent，不绑定工具，只根据上游证据组织回答。
     */
    private final AiSummaryService aiSummaryService;

    /**
     * 构造所有节点所需的 LangChain4j Agent 代理。
     *
     * @param openAiChatModel 当前服务统一使用的 ChatModel
     * @param toolManager 工具管理器，用于给不同子 Agent 注入受限工具集合
     */
    public AgentWorkflowNodes(OpenAiChatModel openAiChatModel, ToolManager toolManager) {
        this.aiClassifyService = AiServices.builder(AiClassifyService.class)
                .chatModel(openAiChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        this.aiNormalizeEntityService = AiServices.builder(AiNormalizeEntityService.class)
                .chatModel(openAiChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        this.aiPlannerService = AiServices.builder(AiPlannerService.class)
                .chatModel(openAiChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        this.aiSearchService = AiServices.builder(AiSearchService.class)
                .chatModel(openAiChatModel)
                .tools(
                        toolManager.getTool("knowNeo4jFrame"),
                        toolManager.getTool("neo4jSearch")
                )
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no tool called " + toolExecutionRequest.name())
                )
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        this.aiMysqlSearchService = AiServices.builder(AiMysqlSearchService.class)
                .chatModel(openAiChatModel)
                .tools(toolManager.getTool("getEntityDetails"))
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no tool called " + toolExecutionRequest.name())
                )
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        this.aiAnswerJudgeService = AiServices.builder(AiAnswerJudgeService.class)
                .chatModel(openAiChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        this.aiSummaryService = AiServices.builder(AiSummaryService.class)
                .chatModel(openAiChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    /**
     * 意图识别节点。
     *
     * <p>失败时降级为 UNSURE，保证图流程可以继续进入后续判断，而不是整条链路中断。</p>
     */
    public Map<String, Object> intentNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(INTENT);
        try {
            ClassifyCodeResult result = aiClassifyService.classifyQuery(memoryId(context, INTENT), context.getOriginalPrompt());
            context.setIntentResult(result);
            context.recordCall(INTENT, "AiClassifyService", "classifyQuery",
                    Map.of("query", context.getOriginalPrompt()), result, elapsed(start));
        } catch (Exception e) {
            ClassifyCodeResult fallback = fallbackIntent(e);
            context.setIntentResult(fallback);
            context.recordError(INTENT, "AiClassifyService", "classifyQuery",
                    Map.of("query", context.getOriginalPrompt()), e, elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * 实体识别节点。
     *
     * <p>只提取用户原文中的实体，后续 Neo4j 节点必须基于这些实体查询，降低编造实体风险。</p>
     */
    public Map<String, Object> entityNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(ENTITY);
        try {
            NormalizeEntityResult result = aiNormalizeEntityService.normalizeEntity(memoryId(context, ENTITY), context.getOriginalPrompt());
            context.setEntityResult(result);
            context.recordCall(ENTITY, "AiNormalizeEntityService", "normalizeEntity",
                    Map.of("query", context.getOriginalPrompt()), result, elapsed(start));
        } catch (Exception e) {
            NormalizeEntityResult fallback = new NormalizeEntityResult();
            fallback.setNeo4jEntities(List.of());
            context.setEntityResult(fallback);
            context.recordError(ENTITY, "AiNormalizeEntityService", "normalizeEntity",
                    Map.of("query", context.getOriginalPrompt()), e, elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * 初始规划节点。
     *
     * <p>主 Agent 会结合意图、实体和已有结果输出 nextAction；
     * 如果模型输出为空或不规范，使用规则兜底，保证路由有确定方向。</p>
     */
    public Map<String, Object> plannerNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(PLANNER);
        try {
            PlanResult result = aiPlannerService.plan(
                    memoryId(context, PLANNER),
                    context.getOriginalPrompt(),
                    json(context.getIntentResult()),
                    json(context.getEntityResult()),
                    context.getNeo4jResult(),
                    context.getMysqlResult(),
                    "初始规划",
                    context.getReplanTimes()
            );
            context.setPlanResult(applyPlanFallbacks(result, context));
            context.recordCall(PLANNER, "AiPlannerService", "plan",
                    Map.of("query", context.getOriginalPrompt()), context.getPlanResult(), elapsed(start));
        } catch (Exception e) {
            context.setPlanResult(ruleBasedPlan(context, "规划 Agent 调用失败：" + e.getMessage()));
            context.recordError(PLANNER, "AiPlannerService", "plan",
                    Map.of("query", context.getOriginalPrompt()), e, elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * Neo4j 查询节点。
     *
     * <p>该节点只调用 Neo4j 子 Agent，子 Agent 仅拥有图谱结构和图谱查询工具。</p>
     */
    public Map<String, Object> neo4jNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(NEO4J);
        try {
            String result = aiSearchService.searchByNeo4j(
                    memoryId(context, NEO4J),
                    context.getEntityResult() == null ? List.of() : context.getEntityResult().getNeo4jEntities(),
                    context.getOriginalPrompt()
            );
            context.setNeo4jResult(result);
            context.recordCall(NEO4J, "AiSearchService", "searchByNeo4j",
                    Map.of("query", context.getOriginalPrompt(), "entity", json(context.getEntityResult())),
                    result,
                    elapsed(start));
        } catch (Exception e) {
            context.setNeo4jResult("Neo4j 查询失败：" + e.getMessage());
            context.recordError(NEO4J, "AiSearchService", "searchByNeo4j",
                    Map.of("query", context.getOriginalPrompt(), "entity", json(context.getEntityResult())),
                    e,
                    elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * MySQL 详情查询节点。
     *
     * <p>该节点只调用 MySQL 子 Agent，子 Agent 只能通过白名单工具查询固定 SQL。</p>
     */
    public Map<String, Object> mysqlNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(MYSQL);
        try {
            String result = aiMysqlSearchService.searchMysqlDetails(
                    memoryId(context, MYSQL),
                    context.getOriginalPrompt(),
                    json(context.getIntentResult()),
                    json(context.getEntityResult()),
                    context.getNeo4jResult()
            );
            context.setMysqlResult(result);
            context.recordCall(MYSQL, "AiMysqlSearchService", "searchMysqlDetails",
                    Map.of("query", context.getOriginalPrompt(),
                            "intent", json(context.getIntentResult()),
                            "entity", json(context.getEntityResult()),
                            "neo4jResult", context.getNeo4jResult()),
                    result,
                    elapsed(start));
        } catch (Exception e) {
            context.setMysqlResult("MySQL 查询失败：" + e.getMessage());
            context.recordError(MYSQL, "AiMysqlSearchService", "searchMysqlDetails",
                    Map.of("query", context.getOriginalPrompt()),
                    e,
                    elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * 证据判断节点。
     *
     * <p>判断节点不查询任何数据，只决定“可以回答、需要重规划、需要用户补充”三类结果。</p>
     */
    public Map<String, Object> judgeNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(JUDGE);
        try {
            AnswerJudgeResult result = aiAnswerJudgeService.judge(
                    memoryId(context, JUDGE),
                    context.getOriginalPrompt(),
                    json(context.getIntentResult()),
                    json(context.getEntityResult()),
                    json(context.getPlanResult()),
                    context.getNeo4jResult(),
                    context.getMysqlResult()
            );
            context.setAnswerJudgeResult(applyJudgeFallbacks(result, context));
            context.recordCall(JUDGE, "AiAnswerJudgeService", "judge",
                    Map.of("query", context.getOriginalPrompt()), context.getAnswerJudgeResult(), elapsed(start));
        } catch (Exception e) {
            context.setAnswerJudgeResult(fallbackJudge(context, "判断 Agent 调用失败：" + e.getMessage()));
            context.recordError(JUDGE, "AiAnswerJudgeService", "judge",
                    Map.of("query", context.getOriginalPrompt()), e, elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * 重规划节点。
     *
     * <p>只在 judge 判定证据不足且还没达到最大重规划次数时进入；
     * 每次进入都会递增 replanTimes，防止循环无限执行。</p>
     */
    public Map<String, Object> replanNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(REPLAN);
        context.increaseReplanTimes();
        String reason = context.getAnswerJudgeResult() == null
                ? "证据不足，需要重新规划"
                : context.getAnswerJudgeResult().getReason() + "；缺失信息：" + context.getAnswerJudgeResult().getMissingInfo();
        try {
            PlanResult result = aiPlannerService.plan(
                    memoryId(context, REPLAN + "-" + context.getReplanTimes()),
                    context.getOriginalPrompt(),
                    json(context.getIntentResult()),
                    json(context.getEntityResult()),
                    context.getNeo4jResult(),
                    context.getMysqlResult(),
                    reason,
                    context.getReplanTimes()
            );
            context.setPlanResult(applyPlanFallbacks(result, context));
            context.recordCall(REPLAN, "AiPlannerService", "replan",
                    Map.of("query", context.getOriginalPrompt(), "reason", reason),
                    context.getPlanResult(),
                    elapsed(start));
        } catch (Exception e) {
            context.setPlanResult(ruleBasedPlan(context, "重规划 Agent 调用失败：" + e.getMessage()));
            context.recordError(REPLAN, "AiPlannerService", "replan",
                    Map.of("query", context.getOriginalPrompt(), "reason", reason),
                    e,
                    elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * 最终总结节点。
     *
     * <p>如果上游判断需要用户补充信息，直接返回澄清提示；
     * 否则将查询结果和证据判断一起交给总结 Agent 生成最终回答。</p>
     */
    public Map<String, Object> summaryNode(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        long start = System.currentTimeMillis();
        context.setCurrentStep(SUMMARY);
        if (needsUserInput(context)) {
            String result = buildAskUserAnswer(context);
            context.setFinalAnswer(result);
            context.recordCall(SUMMARY, "System", "askUser",
                    Map.of("query", context.getOriginalPrompt()),
                    result,
                    elapsed(start));
            return WorkflowContext.saveContext(context);
        }
        try {
            String mysqlResultWithJudge = context.getMysqlResult()
                    + "\n\n证据完整性判断："
                    + json(context.getAnswerJudgeResult());
            String result = aiSummaryService.summarize(
                    memoryId(context, SUMMARY),
                    context.getOriginalPrompt(),
                    json(context.getIntentResult()),
                    json(context.getEntityResult()),
                    context.getNeo4jResult(),
                    mysqlResultWithJudge
            );
            context.setFinalAnswer(result);
            context.recordCall(SUMMARY, "AiSummaryService", "summarize",
                    Map.of("query", context.getOriginalPrompt()),
                    result,
                    elapsed(start));
        } catch (Exception e) {
            context.setFinalAnswer("抱歉，当前无法生成最终回答：" + e.getMessage());
            context.recordError(SUMMARY, "AiSummaryService", "summarize",
                    Map.of("query", context.getOriginalPrompt()),
                    e,
                    elapsed(start));
        }
        return WorkflowContext.saveContext(context);
    }

    /**
     * 普通路由逻辑。
     *
     * <p>planner/replan 之后优先尊重 plan.nextAction；
     * Neo4j 执行完后根据意图决定是否继续补 MySQL 详情；
     * MySQL 执行完统一进入 judge。</p>
     */
    public String routeNext(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        String currentStep = context.getCurrentStep();
        if (NEO4J.equals(currentStep)) {
            return shouldRunMysql(context.getIntentResult()) ? "MYSQL" : "JUDGE";
        }
        if (MYSQL.equals(currentStep)) {
            return "JUDGE";
        }

        String plannedAction = normalizeAction(context.getPlanResult() == null ? null : context.getPlanResult().getNextAction());
        if ("NEO4J".equals(plannedAction)) {
            return "NEO4J";
        }
        if ("MYSQL".equals(plannedAction)) {
            return "MYSQL";
        }
        if ("JUDGE".equals(plannedAction)) {
            return "JUDGE";
        }
        if ("SUMMARY".equals(plannedAction) || "ASK_USER".equals(plannedAction)) {
            return "SUMMARY";
        }
        if (shouldRunNeo4j(context.getIntentResult())) {
            return "NEO4J";
        }
        if (shouldRunMysql(context.getIntentResult())) {
            return "MYSQL";
        }
        return "JUDGE";
    }

    /**
     * judge 节点后的路由逻辑。
     *
     * <p>可以回答或需要用户补充时进入 summary；
     * 证据不足且还有重试预算时进入 replan；
     * 超过预算后直接 summary，让模型基于已有证据说明不足。</p>
     */
    public String routeAfterJudge(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.requireContext(state);
        AnswerJudgeResult judgeResult = context.getAnswerJudgeResult();
        if (judgeResult == null) {
            return "SUMMARY";
        }
        String nextAction = normalizeAction(judgeResult.getNextAction());
        if (Boolean.TRUE.equals(judgeResult.getAnswerable()) || "FINISH".equals(nextAction)) {
            return "SUMMARY";
        }
        if ("ASK_USER".equals(nextAction)) {
            return "SUMMARY";
        }
        if ("REPLAN".equals(nextAction) && context.canReplan()) {
            return "REPLAN";
        }
        return "SUMMARY";
    }

    /**
     * 条件边返回值到实际节点名的映射。
     */
    public Map<String, String> routeMappings() {
        return Map.of(
                "NEO4J", NEO4J,
                "MYSQL", MYSQL,
                "JUDGE", JUDGE,
                "REPLAN", REPLAN,
                "SUMMARY", SUMMARY,
                "ASK_USER", SUMMARY,
                "END", WorkflowNodeNames.END
        );
    }

    /**
     * 规范化主 Agent 的计划输出。
     */
    private PlanResult applyPlanFallbacks(PlanResult result, WorkflowContext context) {
        if (result == null) {
            return ruleBasedPlan(context, "规划 Agent 返回空结果");
        }
        result.setNextAction(normalizeAction(result.getNextAction()));
        if (!StringUtils.hasText(result.getNextAction())) {
            result.setNextAction(routeByIntent(context));
        }
        return result;
    }

    /**
     * 规范化 judge Agent 的输出。
     */
    private AnswerJudgeResult applyJudgeFallbacks(AnswerJudgeResult result, WorkflowContext context) {
        if (result == null) {
            return fallbackJudge(context, "判断 Agent 返回空结果");
        }
        result.setNextAction(normalizeAction(result.getNextAction()));
        if (!StringUtils.hasText(result.getNextAction())) {
            result.setNextAction(Boolean.TRUE.equals(result.getAnswerable()) ? "FINISH" : "REPLAN");
        }
        return result;
    }

    /**
     * 意图识别失败时的保守兜底。
     */
    private ClassifyCodeResult fallbackIntent(Exception e) {
        ClassifyCodeResult result = new ClassifyCodeResult();
        result.setClassifyCode("UNSURE");
        result.setWorkflowType("UNSURE");
        result.setQueryIntent("意图识别失败");
        result.setNeedNeo4j(false);
        result.setNeedMysql(false);
        result.setMysqlTiming("NONE");
        result.setMysqlEntityTypes(List.of());
        result.setDescription(e.getMessage());
        return result;
    }

    /**
     * 当规划 Agent 失败时，用意图识别结果生成最小可执行计划。
     */
    private PlanResult ruleBasedPlan(WorkflowContext context, String reason) {
        PlanResult result = new PlanResult();
        String nextAction = routeByIntent(context);
        result.setNextAction(nextAction);
        result.setRequiredSources(requiredSources(context.getIntentResult()));
        result.setSteps(List.of("根据意图结果选择子 Agent", "执行查询后交给证据判断节点"));
        result.setMissingInfo(List.of());
        result.setStopCondition("查询结果足以覆盖用户核心问题，或达到最大重规划次数");
        result.setReason(reason);
        return result;
    }

    /**
     * 判断 Agent 失败时的保守兜底。
     */
    private AnswerJudgeResult fallbackJudge(WorkflowContext context, String reason) {
        AnswerJudgeResult result = new AnswerJudgeResult();
        boolean hasExecutedQuery = hasExecuted(context.getNeo4jResult()) || hasExecuted(context.getMysqlResult());
        result.setAnswerable(hasExecutedQuery || !shouldRunNeo4j(context.getIntentResult()) && !shouldRunMysql(context.getIntentResult()));
        result.setNextAction(result.getAnswerable() ? "FINISH" : "REPLAN");
        result.setMissingInfo(result.getAnswerable() ? List.of() : List.of("缺少可用于回答的查询结果"));
        result.setReason(reason);
        return result;
    }

    /**
     * 从意图识别结果中整理需要使用的数据源。
     */
    private List<String> requiredSources(ClassifyCodeResult intentResult) {
        if (intentResult == null) {
            return List.of();
        }
        if (shouldRunNeo4j(intentResult) && shouldRunMysql(intentResult)) {
            return List.of("NEO4J", "MYSQL");
        }
        if (shouldRunNeo4j(intentResult)) {
            return List.of("NEO4J");
        }
        if (shouldRunMysql(intentResult)) {
            return List.of("MYSQL");
        }
        return List.of();
    }

    /**
     * 规划 Agent 没有明确 nextAction 时，按意图结果选择默认路由。
     */
    private String routeByIntent(WorkflowContext context) {
        if (shouldRunNeo4j(context.getIntentResult())) {
            return "NEO4J";
        }
        if (shouldRunMysql(context.getIntentResult())) {
            return "MYSQL";
        }
        return "JUDGE";
    }

    /**
     * 判断当前意图是否需要图谱查询。
     */
    private boolean shouldRunNeo4j(ClassifyCodeResult intentResult) {
        if (intentResult == null) {
            return false;
        }
        if (Boolean.TRUE.equals(intentResult.getNeedNeo4j())) {
            return true;
        }
        String workflowType = normalizeAction(intentResult.getWorkflowType());
        return "GRAPH_RELATION_ONLY".equals(workflowType)
                || "GRAPH_THEN_ENTITY_DETAIL".equals(workflowType)
                || "MIXED_GRAPH_AND_ENTITY_DETAIL".equals(workflowType);
    }

    /**
     * 判断当前意图是否需要 MySQL 实体详情查询。
     */
    private boolean shouldRunMysql(ClassifyCodeResult intentResult) {
        if (intentResult == null) {
            return false;
        }
        if (Boolean.TRUE.equals(intentResult.getNeedMysql())) {
            return true;
        }
        String workflowType = normalizeAction(intentResult.getWorkflowType());
        return "ENTITY_DETAIL_ONLY".equals(workflowType)
                || "GRAPH_THEN_ENTITY_DETAIL".equals(workflowType)
                || "MIXED_GRAPH_AND_ENTITY_DETAIL".equals(workflowType);
    }

    /**
     * 判断某一路查询是否已经执行过。
     */
    private boolean hasExecuted(String result) {
        return StringUtils.hasText(result) && !result.startsWith("未执行");
    }

    /**
     * 判断是否应该直接向用户追问，而不是让 summary Agent 硬答。
     */
    private boolean needsUserInput(WorkflowContext context) {
        String judgeAction = normalizeAction(context.getAnswerJudgeResult() == null
                ? null
                : context.getAnswerJudgeResult().getNextAction());
        String planAction = normalizeAction(context.getPlanResult() == null
                ? null
                : context.getPlanResult().getNextAction());
        return "ASK_USER".equals(judgeAction) || "ASK_USER".equals(planAction);
    }

    /**
     * 生成需要用户补充信息时的提示。
     */
    private String buildAskUserAnswer(WorkflowContext context) {
        List<String> missingInfo = context.getAnswerJudgeResult() != null
                && context.getAnswerJudgeResult().getMissingInfo() != null
                ? context.getAnswerJudgeResult().getMissingInfo()
                : context.getPlanResult() == null ? List.of() : context.getPlanResult().getMissingInfo();
        if (missingInfo == null || missingInfo.isEmpty()) {
            return "当前问题还不够明确，请补充具体的中药、方剂、疾病、症状或靶点名称。";
        }
        return "当前还不能准确回答，请补充：" + String.join("；", missingInfo);
    }

    /**
     * 将模型输出中的动作名统一转成大写枚举风格，方便路由比较。
     */
    private String normalizeAction(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    /**
     * 使用 traceId + nodeName 作为 LangChain4j 记忆隔离 ID。
     */
    private String memoryId(WorkflowContext context, String nodeName) {
        return context.getTraceId() + ":" + nodeName;
    }

    /**
     * 计算节点调用耗时。
     */
    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    /**
     * 将结构化结果转成 JSON 字符串传给下游 Agent，避免 Java toString 不稳定。
     */
    private String json(Object value) {
        return value == null ? "" : JSONUtil.toJsonStr(value);
    }
}
