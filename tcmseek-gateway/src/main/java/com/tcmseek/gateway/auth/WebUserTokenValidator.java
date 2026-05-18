package com.tcmseek.gateway.auth;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class WebUserTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(WebUserTokenValidator.class);

    private final WebClient webClient;

    private final GatewayAuthProperties properties;

    public WebUserTokenValidator(WebClient.Builder loadBalancedWebClientBuilder,
                                 GatewayAuthProperties properties) {
        this.webClient = loadBalancedWebClientBuilder.build();
        this.properties = properties;
    }

    public Mono<AuthUser> validate(String token, String requestId) {
        return webClient.get()
                .uri(properties.getValidateUrl())
                .header(properties.getTokenHeader(), token)
                .header(GatewayAuthFilter.REQUEST_ID_HEADER, requestId)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .flatMap(this::parseAuthUser);
                    }
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                log.warn("web user token validation rejected status={} requestId={} body={}",
                                        response.statusCode().value(), requestId, abbreviate(body));
                                return Mono.error(new AuthRejectedException("登录状态已失效，请重新登录。"));
                            });
                })
                .timeout(timeout())
                .doOnSuccess(user -> log.debug("web user token validation passed requestId={} userId={} username={}",
                        requestId, user.getUserId(), user.getUsername()))
                .onErrorMap(error -> {
                    if (error instanceof AuthRejectedException) {
                        return error;
                    }
                    log.warn("web user token validation failed requestId={} message={}", requestId, error.getMessage());
                    return new AuthServiceUnavailableException("用户认证服务暂时不可用，请确认 tcmseek-admin 已启动并注册到 Nacos。", error);
                });
    }

    private Mono<AuthUser> parseAuthUser(JsonNode body) {
        if (body == null || body.path("code").asInt(HttpStatus.UNAUTHORIZED.value()) != 200) {
            return Mono.error(new AuthRejectedException("登录状态已失效，请重新登录。"));
        }
        JsonNode user = body.path("data");
        if (user.isMissingNode() || user.isNull()) {
            user = body.path("user");
        }
        String userId = firstText(user, "id", "userId");
        String username = firstText(user, "username", "userName");
        String account = firstText(user, "account", "nickName");
        String email = firstText(user, "email");
        if (userId == null || userId.isBlank() || username == null || username.isBlank()) {
            return Mono.error(new AuthRejectedException("登录状态已失效，请重新登录。"));
        }
        return Mono.just(new AuthUser(userId, username, account, email));
    }

    private Duration timeout() {
        return properties.getTimeout() == null ? Duration.ofSeconds(5) : properties.getTimeout();
    }

    private String firstText(JsonNode node, String... names) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        for (String name : names) {
            JsonNode value = node.path(name);
            if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }

    private String abbreviate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 160 ? body : body.substring(0, 160) + "...";
    }

    public static class AuthRejectedException extends RuntimeException {
        public AuthRejectedException(String message) {
            super(message);
        }
    }

    public static class AuthServiceUnavailableException extends RuntimeException {
        public AuthServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
