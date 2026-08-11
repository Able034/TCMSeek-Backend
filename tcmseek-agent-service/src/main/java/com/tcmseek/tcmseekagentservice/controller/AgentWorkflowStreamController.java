package com.tcmseek.tcmseekagentservice.controller;

import com.tcmseek.tcmseekagentservice.service.AgentWorkflowStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 多 Agent 工作流流式 HTTP 入口。
 *
 * <p>该接口使用 SSE 返回：
 * 节点开始/完成事件按事件流输出，最终回答按 answer_delta token 输出。</p>
 */
@RestController
@RequestMapping("/api/agent")
public class AgentWorkflowStreamController {

    /**
     * 流式工作流服务。
     */
    private final AgentWorkflowStreamService agentWorkflowStreamService;

    /**
     * 构造流式 Controller。
     */
    public AgentWorkflowStreamController(AgentWorkflowStreamService agentWorkflowStreamService) {
        this.agentWorkflowStreamService = agentWorkflowStreamService;
    }

    /**
     * 执行一次流式多 Agent 查询。
     *
     * @param request 用户问题和重规划配置
     * @return SSE 事件流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AgentStreamChatRequest request) {
        if(request.maxReplanTimes()==null || request.maxReplanTimes() == 0 ){
            return agentWorkflowStreamService.stream(request.query(),2);
        }
        return agentWorkflowStreamService.stream(request.query(), request.maxReplanTimes());
    }

    /**
     * 流式查询请求体。
     *
     * @param query 用户原始问题
     * @param maxReplanTimes 最大重规划次数，可为空
     */
    public record AgentStreamChatRequest(String query, Integer maxReplanTimes) {
    }
}
