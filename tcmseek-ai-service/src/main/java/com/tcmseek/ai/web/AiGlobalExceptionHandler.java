package com.tcmseek.ai.web;

import com.tcmseek.ai.dto.ErrorResponse;
import com.tcmseek.ai.exception.AiServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class AiGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AiGlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return build(request, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex,
                                                          HttpServletRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                              HttpServletRequest request) {
        if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
                && request.getRequestURI() != null
                && request.getRequestURI().contains("/exports/")) {
            return build(request, ex.getStatusCode(), "EXPORT_NOT_AVAILABLE",
                    "完整 CSV 结果已过期或不存在，请重新提问生成。");
        }
        return build(request, ex.getStatusCode(), "REQUEST_FAILED", ex.getReason());
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiService(AiServiceException ex,
                                                         HttpServletRequest request) {
        log.warn("ai service error code={} status={} path={} message={}",
                ex.getCode(), ex.getStatus().value(), request.getRequestURI(), ex.getMessage(), ex);
        return build(request, ex.getStatus(), ex.getCode(), ex.getUserMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        if (hasCauseClassName(ex, "org.neo4j.driver")) {
            log.warn("neo4j error path={} message={}", request.getRequestURI(), ex.getMessage(), ex);
            return build(request, HttpStatus.SERVICE_UNAVAILABLE, "NEO4J_UNAVAILABLE",
                    "知识图谱服务暂时不可用，请确认 Neo4j 已启动且连接配置正确。");
        }
        if (hasCauseClassName(ex, "org.springframework.ai")
                || hasCauseClassName(ex, "org.springframework.web.client")
                || hasCauseClassName(ex, "openai")) {
            log.warn("ai provider error path={} message={}", request.getRequestURI(), ex.getMessage(), ex);
            return build(request, HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE",
                    "AI 模型服务暂时不可用，请确认 DeepSeek API Key、网络和模型配置正确。");
        }
        log.error("unhandled ai service error path={} message={}", request.getRequestURI(), ex.getMessage(), ex);
        return build(request, HttpStatus.INTERNAL_SERVER_ERROR, "AI_SERVICE_ERROR",
                "AI 服务处理请求失败，请稍后重试或根据 requestId 查看日志。");
    }

    private ResponseEntity<ErrorResponse> build(HttpServletRequest request,
                                                HttpStatusCode status,
                                                String code,
                                                String message) {
        String requestId = request.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        ErrorResponse response = new ErrorResponse(
                status.value(),
                code,
                message == null || message.isBlank() ? "请求处理失败" : message,
                request.getRequestURI(),
                requestId);
        return ResponseEntity.status(status).body(response);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    private boolean hasCauseClassName(Throwable throwable, String keyword) {
        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName().toLowerCase();
            String message = current.getMessage() == null ? "" : current.getMessage().toLowerCase();
            String expected = keyword.toLowerCase();
            if (className.contains(expected) || message.contains(expected)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
