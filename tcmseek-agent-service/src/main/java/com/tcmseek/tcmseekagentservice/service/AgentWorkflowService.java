package com.tcmseek.tcmseekagentservice.service;

import com.tcmseek.tcmseekagentservice.ai.model.workflow.AgentWorkflowResult;
import com.tcmseek.tcmseekagentservice.ai.tools.ToolManager;
import com.tcmseek.tcmseekagentservice.graph.node.AgentWorkflowNodes;
import com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames;
import com.tcmseek.tcmseekagentservice.graph.state.WorkflowContext;
import com.tcmseek.tcmseekagentservice.memory.AgentConversationContext;
import com.tcmseek.tcmseekagentservice.memory.AgentSessionManager;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * TCMSeek 多 Agent 工作流应用服务。
 *
 * <p>这个类只做三件事：
 * 1. 组装 LangGraph4j 状态图；
 * 2. 接收用户 query 并启动图执行；
 * 3. 将最终 WorkflowContext 转成接口返回对象。</p>
 *
 * <p>具体节点逻辑已经抽到 {@link AgentWorkflowNodes}，
 * 避免 service 里混入大量 agent 调用细节。</p>
 */
@Service
public class AgentWorkflowService {

    /**
     * 工作流所有节点动作和路由判断。
     */
    private final AgentWorkflowNodes nodes;

    /**
     * 编译后的 LangGraph4j 图实例，服务启动时构建一次，后续请求复用。
     */
    private final CompiledGraph<MessagesState<String>> graph;

    /**
     * 会话记忆管理器，负责跨请求上下文恢复和最终问答落库。
     */
    private final AgentSessionManager sessionManager;

    /**
     * 构造工作流服务。
     *
     * @param openAiChatModel LangChain4j 使用的同步 ChatModel
     * @param toolManager 工具管理器，用于给子 Agent 注入受限工具
     */
    public AgentWorkflowService(OpenAiChatModel openAiChatModel,
                                ToolManager toolManager,
                                AgentSessionManager sessionManager) {
        this.nodes = new AgentWorkflowNodes(openAiChatModel, toolManager);
        this.graph = buildGraph();
        this.sessionManager = sessionManager;
    }

    /**
     * 运行一次多 Agent 工作流。
     *
     * @param query 用户原始问题
     * @param maxReplanTimes 最大重规划次数，空值时使用 WorkflowContext 默认值
     * @return 带最终答案、过程状态和调用记录的完整结果
     */
    public AgentWorkflowResult run(String query, Integer maxReplanTimes) {
        return run(query, maxReplanTimes, null, AgentRequestContext.empty());
    }

    public AgentWorkflowResult run(String query,
                                   Integer maxReplanTimes,
                                   String sessionId,
                                   AgentRequestContext requestContext) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("query 不能为空");
        }

        AgentRequestContext safeContext = requestContext == null ? AgentRequestContext.empty() : requestContext;
        long startedAt = System.currentTimeMillis();
        String trimmedQuery = query.trim();
        AgentConversationContext conversationContext =
                sessionManager.buildContext(sessionId, trimmedQuery, safeContext);
        WorkflowContext initialContext = WorkflowContext.start(
                trimmedQuery,
                conversationContext.getEnhancedPrompt(),
                maxReplanTimes);
        RunnableConfig config = RunnableConfig.builder()
                .threadId(initialContext.getTraceId())
                .build();

        Optional<MessagesState<String>> finalState = graph.invoke(
                WorkflowContext.saveContext(initialContext),
                config
        );

        WorkflowContext finalContext = finalState
                .map(WorkflowContext::requireContext)
                .orElse(initialContext);

        sessionManager.appendExchange(
                sessionId,
                trimmedQuery,
                finalContext.getFinalAnswer(),
                safeContext,
                System.currentTimeMillis() - startedAt);

        return toResult(finalContext);
    }

    /**
     * 构建 LangGraph4j 状态图。
     *
     * <p>固定链路是 intent -> entity -> planner；
     * planner 之后通过条件边进入 Neo4j、MySQL、judge、summary；
     * judge 判断证据不足时可以进入 replan，再重新路由。</p>
     */
    private CompiledGraph<MessagesState<String>> buildGraph() {
        try {
            StateGraph<MessagesState<String>> stateGraph = new StateGraph<>(
                    MessagesState.SCHEMA,
                    MessagesState::new
            );

            stateGraph.addNode(WorkflowNodeNames.INTENT, node_async(nodes::intentNode));
            stateGraph.addNode(WorkflowNodeNames.ENTITY, node_async(nodes::entityNode));
            stateGraph.addNode(WorkflowNodeNames.PLANNER, node_async(nodes::plannerNode));
            stateGraph.addNode(WorkflowNodeNames.NEO4J, node_async(nodes::neo4jNode));
            stateGraph.addNode(WorkflowNodeNames.MYSQL, node_async(nodes::mysqlNode));
            stateGraph.addNode(WorkflowNodeNames.JUDGE, node_async(nodes::judgeNode));
            stateGraph.addNode(WorkflowNodeNames.REPLAN, node_async(nodes::replanNode));
            stateGraph.addNode(WorkflowNodeNames.SUMMARY, node_async(nodes::summaryNode));

            stateGraph.addEdge(WorkflowNodeNames.START, WorkflowNodeNames.INTENT);
            stateGraph.addEdge(WorkflowNodeNames.INTENT, WorkflowNodeNames.ENTITY);
            stateGraph.addEdge(WorkflowNodeNames.ENTITY, WorkflowNodeNames.PLANNER);

            Map<String, String> routeMappings = nodes.routeMappings();
            stateGraph.addConditionalEdges(WorkflowNodeNames.PLANNER, edge_async(nodes::routeNext), routeMappings);
            stateGraph.addConditionalEdges(WorkflowNodeNames.NEO4J, edge_async(nodes::routeNext), routeMappings);
            stateGraph.addConditionalEdges(WorkflowNodeNames.MYSQL, edge_async(nodes::routeNext), routeMappings);
            stateGraph.addConditionalEdges(WorkflowNodeNames.JUDGE, edge_async(nodes::routeAfterJudge), routeMappings);
            stateGraph.addConditionalEdges(WorkflowNodeNames.REPLAN, edge_async(nodes::routeNext), routeMappings);
            stateGraph.addEdge(WorkflowNodeNames.SUMMARY, WorkflowNodeNames.END);

            CompiledGraph<MessagesState<String>> compiledGraph = stateGraph.compile();
            compiledGraph.setMaxIterations(32);
            return compiledGraph;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build TCMSeek agent workflow graph", e);
        }
    }

    /**
     * 将图状态转换为 HTTP/API 层返回对象。
     */
    private AgentWorkflowResult toResult(WorkflowContext context) {
        return AgentWorkflowResult.builder()
                .traceId(context.getTraceId())
                .query(context.getOriginalPrompt())
                .finalAnswer(context.getFinalAnswer())
                .intentResult(context.getIntentResult())
                .entityResult(context.getEntityResult())
                .planResult(context.getPlanResult())
                .answerJudgeResult(context.getAnswerJudgeResult())
                .neo4jResult(context.getNeo4jResult())
                .mysqlResult(context.getMysqlResult())
                .replanTimes(context.getReplanTimes())
                .callRecords(context.getCallRecords())
                .build();
    }
}
