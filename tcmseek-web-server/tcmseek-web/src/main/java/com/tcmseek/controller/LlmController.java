package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.dto.llm.LlmChatRequest;
import com.tcmseek.dto.llm.LlmChatResponse;
import com.tcmseek.service.LlmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@Anonymous
@RestController
@RequiredArgsConstructor
@RequestMapping("/tcmseek/llm")
@Api(tags = "大模型")
@Deprecated
@ConditionalOnProperty(prefix = "tcmseek.legacy-llm", name = "enabled", havingValue = "true")
public class LlmController {

    private final LlmService llmService;

    @ApiOperation("通用模式对话")
    @PostMapping("/chat")
    public AjaxResult chat(@Valid @RequestBody LlmChatRequest request) {
        log.info("请求参数：{}", request);
        try{
            LlmChatResponse chatres = llmService.chat(request);
            return AjaxResult.success(chatres);
        }catch (Exception e){
            log.error("服务请求失败：{}", e.getMessage());
            return AjaxResult.error("服务请求失败，请联系管理员！");
        }
//        return AjaxResult.success(llmService.chat(request));
    }

    @ApiOperation("学术/专业模式对话")
    @PostMapping("/aichat")
    public AjaxResult aichat(@Valid @RequestBody LlmChatRequest request) {
        log.info("请求参数：{}", request);
        try{
            LlmChatResponse aichat = llmService.aichat(request);
            return AjaxResult.success(aichat);
        }catch (Exception e){
            log.error("服务请求失败：{}", e.getMessage());
            return AjaxResult.error("服务请求失败，请联系管理员！");
        }
        //return AjaxResult.success(llmService.aichat(request));
    }

    @ApiOperation("共同结果CSV导出（学术模式）")
    @GetMapping(value = "/aichat/common-targets/export", produces = "text/csv")
    public void exportCommonTargets(@RequestParam(value = "herbA", required = false) String herbA,
                                    @RequestParam(value = "herbB", required = false) String herbB,
                                    @RequestParam(value = "cypher", required = false) String cypher,
                                    @RequestParam(value = "key", required = false) String key,
                                    HttpServletResponse response) {
        llmService.exportCommonTargetsCsv(herbA, herbB, cypher, key, response);
    }

}
