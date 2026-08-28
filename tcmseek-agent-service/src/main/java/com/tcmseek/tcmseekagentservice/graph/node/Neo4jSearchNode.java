package com.tcmseek.tcmseekagentservice.graph.node;

import com.tcmseek.tcmseekagentservice.graph.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 旧版 Neo4j 节点占位类。
 *
 * <p>正式工作流的 Neo4j 节点已经迁移到 {@link AgentWorkflowNodes#neo4jNode(MessagesState)}。
 * 保留该类是为了兼容外部临时引用，后续确认没有引用后可以删除。</p>
 */
@Slf4j
@Deprecated
public class Neo4jSearchNode {

    /**
     * 创建一个兼容旧代码的空节点动作。
     *
     * <p>该节点不会执行真实查询，只会把原始上下文写回 state。</p>
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行旧版 Neo4jSearchNode 占位节点");
            String userMessage = buildUserMessage(context);
            log.debug("旧版 Neo4jSearchNode 收到输入: {}", userMessage);
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 从上下文中取出增强后的问题，兼容旧节点的输入构造方式。
     */
    private static String buildUserMessage(WorkflowContext context) {
        return context == null ? "" : context.getEnhancedPrompt();
    }
}
