package com.tcmseek.tcmseekagentservice.controller;

import com.tcmseek.tcmseekagentservice.ai.model.workflow.AgentWorkflowResult;
import com.tcmseek.tcmseekagentservice.service.AgentWorkflowService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多 Agent 工作流 HTTP 入口。
 *
 * <p>当前提供一个非流式 chat 接口，返回最终答案和完整执行轨迹。</p>
 */
@RestController
@RequestMapping("/api/agent")
public class AgentWorkflowController {

    /**
     * 工作流应用服务。
     */
    private final AgentWorkflowService agentWorkflowService;

    public AgentWorkflowController(AgentWorkflowService agentWorkflowService) {
        this.agentWorkflowService = agentWorkflowService;
    }

    /**
     * 执行一次多 Agent 查询。
     *
     * @param request 用户问题和重规划配置
     * @return 工作流完整执行结果
     */
    @PostMapping("/chat")
    public AgentWorkflowResult chat(@RequestBody AgentChatRequest request) {
        if(request.maxReplanTimes()==null || request.maxReplanTimes() == 0 ){
           return agentWorkflowService.run(request.query(),2);
        }
        return agentWorkflowService.run(request.query(), request.maxReplanTimes());
    }




    /**
     * 多 Agent 查询请求体。
     *
     * @param query 用户原始问题
     * @param maxReplanTimes 最大重规划次数，可为空
     */
    public record AgentChatRequest(String query, Integer maxReplanTimes) {
    }
}
