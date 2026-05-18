package com.tcmseek.gateway.web;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GatewayInfoController {

    private final RouteDefinitionLocator routeDefinitionLocator;

    public GatewayInfoController(RouteDefinitionLocator routeDefinitionLocator) {
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @GetMapping("/")
    public Mono<Map<String, Object>> index() {
        return routeDefinitions()
                .collectList()
                .map(routes -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("service", "tcmseek-gateway");
                    body.put("status", "UP");
                    body.put("routes", routes);
                    body.put("actuator", "/actuator/health");
                    return body;
                });
    }

    @GetMapping("/gateway/routes")
    public Flux<Map<String, Object>> routes() {
        return routeDefinitions();
    }

    private Flux<Map<String, Object>> routeDefinitions() {
        return routeDefinitionLocator.getRouteDefinitions()
                .map(this::routeSummary);
    }

    private Map<String, Object> routeSummary(RouteDefinition route) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", route.getId());
        summary.put("uri", route.getUri().toString());
        summary.put("predicates", route.getPredicates().stream().map(Object::toString).toList());
        summary.put("filters", route.getFilters().stream().map(Object::toString).toList());
        return summary;
    }
}
