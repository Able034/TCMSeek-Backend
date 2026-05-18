package com.tcmseek.gateway.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.gateway.filter.GatewayTraceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Order(-2)
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorWebExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GatewayErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        String requestId = requestId(exchange);
        HttpStatusCode status = status(ex);
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(GatewayTraceFilter.REQUEST_ID_HEADER, requestId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("code", code(status));
        body.put("message", message(status, ex));
        body.put("path", exchange.getRequest().getURI().getRawPath());
        body.put("requestId", requestId);

        log.warn("gateway handled error requestId={} status={} path={} message={}",
                requestId, status.value(), exchange.getRequest().getURI().getRawPath(), ex.getMessage());

        byte[] bytes = serialize(body);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private String requestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(GatewayTraceFilter.REQUEST_ID_HEADER);
        return requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
    }

    private HttpStatusCode status(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            return responseStatusException.getStatusCode();
        }
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (message.contains("unable to find instance")
                || message.contains("connection refused")
                || message.contains("connection timed out")
                || message.contains("timeout")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String code(HttpStatusCode status) {
        if (status.value() == HttpStatus.NOT_FOUND.value()) {
            return "GATEWAY_ROUTE_NOT_FOUND";
        }
        if (status.value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return "DOWNSTREAM_SERVICE_UNAVAILABLE";
        }
        return "GATEWAY_ERROR";
    }

    private String message(HttpStatusCode status, Throwable ex) {
        if (status.value() == HttpStatus.NOT_FOUND.value()) {
            return "网关未匹配到可用路由，请检查请求路径。";
        }
        if (status.value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return "下游服务暂时不可用，请确认目标服务已启动并已注册到 Nacos。";
        }
        return "网关处理请求失败，请稍后重试或根据 requestId 查看日志。";
    }

    private byte[] serialize(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            return "{}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
