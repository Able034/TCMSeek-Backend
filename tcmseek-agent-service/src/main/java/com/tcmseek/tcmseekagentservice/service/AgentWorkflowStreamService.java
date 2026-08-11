package com.tcmseek.tcmseekagentservice.service;

import com.tcmseek.tcmseekagentservice.ai.model.workflow.AgentWorkflowResult;
import com.tcmseek.tcmseekagentservice.ai.tools.ToolManager;
import com.tcmseek.tcmseekagentservice.graph.node.AgentWorkflowNodes;
import com.tcmseek.tcmseekagentservice.graph.node.WorkflowNodeNames;
import com.tcmseek.tcmseekagentservice.graph.state.WorkflowContext;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 多 Agent 工作流流式服务。
 *
 * <p>同步 {@link AgentWorkflowService} 返回完整结果；
 * 本服务返回 {@link SseEmitter}，边执行图流程边向前端推送节点事件，
 * 并在最终 summary 阶段推送 answer_delta token。</p>
 */
@Service
public class AgentWorkflowStreamService {

    /**
     * SSE 连接超时时间，单位毫秒。
     */
    private static final long SSE_TIMEOUT_MS = 10 * 60 * 1000L;

    /**
     * 同步 ChatModel，用于意图、实体、规划、Neo4j/MySQL 和 judge 等结构化节点。
     */
    private final OpenAiChatModel openAiChatModel;

    /**
     * 流式 ChatModel，只用于最终 summary token 流。
     */
    private final StreamingChatModel streamingChatModel;

    /**
     * 工具管理器，用于给子 Agent 注入受限工具。
     */
    private final ToolManager toolManager;

    /**
     * 构造流式工作流服务。
     *
     * @param openAiChatModel 同步 ChatModel
     * @param streamingChatModel DeepSeek 流式 ChatModel
     * @param toolManager 工具管理器
     */
    public AgentWorkflowStreamService(OpenAiChatModel openAiChatModel,
                                      @Qualifier("streamingChatModel") StreamingChatModel streamingChatModel,
                                      ToolManager toolManager) {
        this.openAiChatModel = openAiChatModel;
        this.streamingChatModel = streamingChatModel;
        this.toolManager = toolManager;
    }

    /**
     * 创建一次 SSE 流式工作流。
     *
     * @param query 用户原始问题
     * @param maxReplanTimes 最大重规划次数
     * @return SSE 连接对象
     */
    public SseEmitter stream(String query, Integer maxReplanTimes) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("query 不能为空");
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        WorkflowContext initialContext = WorkflowContext.start(query.trim(), maxReplanTimes);
        WorkflowEventPublisher publisher = new WorkflowEventPublisher(emitter, initialContext.getTraceId());

        CompletableFuture.runAsync(() -> execute(initialContext, publisher));

        return emitter;
    }

    /**
     * 后台执行图流程，并负责 SSE 生命周期。
     */
    private void execute(WorkflowContext initialContext, WorkflowEventPublisher publisher) {
        try {
            publisher.workflowStarted(initialContext);

            AgentWorkflowNodes nodes = new AgentWorkflowNodes(
                    openAiChatModel,
                    streamingChatModel,
                    toolManager,
                    publisher
            );
            CompiledGraph<MessagesState<String>> graph = buildGraph(nodes);
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
            publisher.workflowDone(toResult(finalContext));
            publisher.complete();
        } catch (Exception e) {
            publisher.error(e);
            publisher.completeWithError(e);
        }
    }

    /**
     * 为单次流式请求构建 LangGraph4j 图。
     *
     * <p>图结构和同步服务保持一致，但 nodes 中带有 SSE publisher，
     * 因此节点执行时会额外推送进度事件。</p>
     */
    private CompiledGraph<MessagesState<String>> buildGraph(AgentWorkflowNodes nodes) {
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
            throw new IllegalStateException("Failed to build TCMSeek streaming agent workflow graph", e);
        }
    }

    /**
     * 将最终上下文转换为和同步接口一致的返回结构，作为 workflow_done 事件负载。
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
