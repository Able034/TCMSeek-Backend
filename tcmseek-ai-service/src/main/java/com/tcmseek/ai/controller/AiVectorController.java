package com.tcmseek.ai.controller;

import com.tcmseek.ai.service.SemanticSearchService;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/ai/vector", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class AiVectorController {

    private final SemanticSearchService semanticSearchService;

    public AiVectorController(SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }

    @GetMapping("/status")
    public SemanticSearchService.VectorStatus status() {
        return semanticSearchService.status();
    }

    @GetMapping("/search")
    public List<SemanticSearchService.SemanticAnchor> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "topic,target,pathway,disease,phenotype") String types,
            @RequestParam(defaultValue = "10") int limit) {
        return semanticSearchService.search(query, splitTypes(types), limit);
    }

    private List<String> splitTypes(String types) {
        if (!StringUtils.hasText(types)) {
            return List.of();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

}
