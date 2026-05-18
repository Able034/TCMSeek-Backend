package com.tcmseek.gateway.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(GatewayAuthFilter.class);

    private static final String USER_ID_HEADER = "X-User-Id";

    private static final String USERNAME_HEADER = "X-User-Name";

    private static final String ACCOUNT_HEADER = "X-User-Account";

    private static final String EMAIL_HEADER = "X-User-Email";

    private final GatewayAuthProperties properties;

    private final WebUserTokenValidator tokenValidator;

    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public GatewayAuthFilter(GatewayAuthProperties properties,
                             WebUserTokenValidator tokenValidator,
                             ObjectMapper objectMapper) {
        this.properties = properties;
        this.tokenValidator = tokenValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange sanitizedExchange = withoutUserHeaders(exchange);
        if (!shouldAuthenticate(sanitizedExchange)) {
            return chain.filter(sanitizedExchange);
        }

        String token = sanitizedExchange.getRequest().getHeaders().getFirst(properties.getTokenHeader());
        String requestId = sanitizedExchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (!StringUtils.hasText(token)) {
            return writeError(sanitizedExchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "未登录或请求未携带 token。");
        }

        return tokenValidator.validate(token, requestId)
                .flatMap(user -> chain.filter(withUserHeaders(sanitizedExchange, user)))
                .onErrorResume(WebUserTokenValidator.AuthRejectedException.class,
                        error -> writeError(sanitizedExchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", error.getMessage()))
                .onErrorResume(WebUserTokenValidator.AuthServiceUnavailableException.class,
                        error -> writeError(sanitizedExchange, HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SERVICE_UNAVAILABLE", error.getMessage()));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private boolean shouldAuthenticate(ServerWebExchange exchange) {
        if (!properties.isEnabled()) {
            return false;
        }
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return false;
        }
        String path = exchange.getRequest().getURI().getRawPath();
        if (properties.getExcludedPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path))) {
            return false;
        }
        return properties.getProtectedPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private ServerWebExchange withoutUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(this::removeSpoofableUserHeaders)
                .build();
        return exchange.mutate().request(request).build();
    }

    private ServerWebExchange withUserHeaders(ServerWebExchange exchange, AuthUser user) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    removeSpoofableUserHeaders(headers);
                    putIfPresent(headers, USER_ID_HEADER, user.getUserId());
                    putIfPresent(headers, USERNAME_HEADER, user.getUsername());
                    putIfPresent(headers, ACCOUNT_HEADER, user.getAccount());
                    putIfPresent(headers, EMAIL_HEADER, user.getEmail());
                })
                .build();
        log.debug("gateway auth passed path={} userId={} username={}",
                request.getURI().getRawPath(), user.getUserId(), user.getUsername());
        return exchange.mutate().request(request).build();
    }

    private void removeSpoofableUserHeaders(HttpHeaders headers) {
        headers.remove(USER_ID_HEADER);
        headers.remove(USERNAME_HEADER);
        headers.remove(ACCOUNT_HEADER);
        headers.remove(EMAIL_HEADER);
    }

    private void putIfPresent(HttpHeaders headers, String name, String value) {
        if (StringUtils.hasText(value)) {
            headers.set(name, value);
        }
    }

    private Mono<Void> writeError(ServerWebExchange exchange,
                                  HttpStatus status,
                                  String code,
                                  String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (StringUtils.hasText(requestId)) {
            exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        body.put("path", exchange.getRequest().getURI().getRawPath());
        body.put("requestId", requestId);

        byte[] bytes = serialize(body);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private byte[] serialize(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            return "{}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
