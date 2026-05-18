package com.tcmseek.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.dto.llm.LlmChatRequest;
import com.tcmseek.dto.llm.LlmChatResponse;
import com.tcmseek.dto.llm.LlmGraphData;
import com.tcmseek.dto.llm.LlmMessage;
import com.tcmseek.service.LlmService;
import com.tcmseek.service.TcmNeo4jService;
import com.tcmseek.support.deepseek.DeepSeekProperties;
import com.tcmseek.support.llm.LlmProperties;
import com.tcmseek.support.llm.LlmSessionManager;
import com.tcmseek.support.tcm.TcmPromptTemplates;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    private static final int GRAPH_TARGET_LIMIT = 100;
    private static final int SUMMARY_ITEM_LIMIT = 100;
    private static final int FIELD_VALUE_MAX_LEN = 120;

    private final LlmProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final LlmSessionManager sessionManager;
    private final TcmNeo4jService tcmNeo4jService;
    private final DeepSeekProperties deepSeekProperties;

    public LlmServiceImpl(LlmProperties properties, ObjectMapper objectMapper, LlmSessionManager sessionManager, TcmNeo4jService tcmNeo4jService, DeepSeekProperties deepSeekProperties) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
        this.tcmNeo4jService = tcmNeo4jService;
        this.deepSeekProperties = deepSeekProperties;
        this.restTemplate = createRestTemplate(properties);
    }

    @Override
    public LlmChatResponse chat(LlmChatRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getMessages())) {
            throw new ServiceException("消息内容不能为空");
        }
        String sessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId().trim() : "default";
        List<LlmMessage> incoming = sanitizeIncoming(request.getMessages());
        if (incoming.isEmpty()) {
            throw new ServiceException("消息内容不能为空");
        }

        int beforeSize = sessionManager.snapshotSize(sessionId);
        sessionManager.appendUserMessages(sessionId, incoming);

        String endpoint = buildChatCompletionsUrl();
        List<LlmMessage> context = sessionManager.buildContext(sessionId);
        ObjectNode payload = buildPayload(context);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ObjectNode> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                log.warn("LLM 服务返回空响应，状态码：{}", response.getStatusCode());
                throw new ServiceException("大模型服务暂不可用，请稍后再试");
            }
            LlmChatResponse result = parseResponse(response.getBody());
            sessionManager.appendAssistantReply(sessionId, result.getReply());
            log.info("LLM 返回成功：id={}, model={}, finishReason={}, totalTokens={}",
                    result.getId(),
                    result.getModel(),
                    result.getFinishReason(),
                    result.getUsage() != null ? result.getUsage().getTotalTokens() : null);
            return result;
        } catch (RestClientResponseException e) {
            sessionManager.rollbackToSize(sessionId, beforeSize);
            String body = e.getResponseBodyAsString();
            log.error("LLM 服务错误响应，status={} body={}", e.getRawStatusCode(), body);
            String message = extractErrorMessage(body);
            throw new ServiceException(StringUtils.hasText(message) ? message : "大模型服务异常：" + e.getRawStatusCode());
        } catch (Exception e) {
            sessionManager.rollbackToSize(sessionId, beforeSize);
            log.error("调用大模型服务失败", e);
            throw new ServiceException("大模型服务暂不可用，请稍后再试");
        }
    }

    @Override
    public LlmChatResponse aichat(LlmChatRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getMessages())) {
            throw new ServiceException("消息内容不能为空");
        }

        List<LlmMessage> incoming = sanitizeIncoming(request.getMessages());
        if (incoming.isEmpty()) {
            throw new ServiceException("消息内容不能为空");
        }
        String question = incoming.get(incoming.size() - 1).getContent();

        String sessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId().trim() : "default";
        List<LlmMessage> history = sessionManager.buildContext(sessionId);

        List<Map<String, Object>> searchResults = Collections.emptyList();
        String cypherQuery = null;
        String effectiveCypher = null;

        try {
            cypherQuery = generateCypher(question, history);
            effectiveCypher = cypherQuery;
            log.info("生成的Cypher查询: {}", cypherQuery);

            if (!"False".equals(cypherQuery) && StringUtils.hasText(cypherQuery)) {
                searchResults = executeCypherSafely(cypherQuery);
            }

            if (cypherQuery != null && !"False".equals(cypherQuery) &&
                    (searchResults == null || searchResults.isEmpty())) {
                List<Map<String, String>> entities = tcmNeo4jService.extractEntities(cypherQuery);
                log.info("提取的实体 {}", entities);

                Map<String, String> entityMapping = new HashMap<>();
                for (Map<String, String> entityInfo : entities) {
                    String propType = entityInfo.get("propType");
                    String entity = entityInfo.get("entity");

                    List<String> similarEntities = tcmNeo4jService.findSimilarEntities(entity, propType, 1);
                    log.info("寻找相似实体: {}='{}' -> {}", propType, entity, similarEntities);

                    if (!similarEntities.isEmpty()) {
                        String key = propType + ":" + entity;
                        entityMapping.put(key, similarEntities.get(0));
                    }
                }

                if (!entityMapping.isEmpty()) {
                    String newCypher = tcmNeo4jService.replaceEntities(cypherQuery, entityMapping);
                    effectiveCypher = newCypher;
                    log.info("替换实体后的Cypher: {}", newCypher);
                    searchResults = executeCypherSafely(newCypher);
                }
            }

            int totalResults = searchResults != null ? searchResults.size() : 0;
            ResolvedKey resolvedKey = resolveEntityKeyAndType(searchResults);
            LlmGraphData graphData = buildGraphData(effectiveCypher, searchResults, resolvedKey);
            if (graphData == null) {
                graphData = buildContainmentGraph(effectiveCypher, searchResults, resolvedKey);
            }
            String csvDownloadUrl = buildCsvDownloadUrl(graphData, effectiveCypher, resolvedKey);

            String resultsStr = buildCompactResults(searchResults, resolvedKey);
            String textResponse = summarizeAnswer(question, history, resultsStr);
            textResponse = appendGraphNotice(textResponse, graphData);

            LlmChatResponse response = new LlmChatResponse();
            response.setReply(textResponse);
            response.setModel(deepSeekProperties.getChatModel());
            response.setGraph(graphData);
            response.setCsvDownloadUrl(csvDownloadUrl);
            response.setTotalResults(totalResults == 0 ? null : totalResults);
            response.setDisplayedResults(graphData != null ? graphData.getDisplayedTargets() : null);

            sessionManager.appendUserMessages(sessionId, incoming);
            sessionManager.appendAssistantReply(sessionId, textResponse);

            return response;

        } catch (Exception e) {
            log.error("专业模式对话失败", e);
            throw new ServiceException("专业模式对话失败: " + e.getMessage());
        }
    }

    @Override
    public void exportCommonTargetsCsv(String herbA, String herbB, String cypher, String key, HttpServletResponse response) {
        String decodedCypher = cypher;
        if (StringUtils.hasText(cypher)) {
            try {
                decodedCypher = java.net.URLDecoder.decode(cypher, StandardCharsets.UTF_8.name());
            } catch (Exception ignored) {
                decodedCypher = cypher;
            }
        }
        String effectiveCypher = StringUtils.hasText(decodedCypher) ? decodedCypher : buildDefaultCommonTargetCypher(herbA, herbB);
        if (!StringUtils.hasText(effectiveCypher)) {
            throw new ServiceException("缺少导出所需的查询语句");
        }

        List<Map<String, Object>> results = StringUtils.hasText(decodedCypher)
                ? tcmNeo4jService.executeQuery(effectiveCypher)
                : tcmNeo4jService.executeQuery(effectiveCypher, buildHerbParams(herbA, herbB));

        ResolvedKey resolved = resolveEntityKeyAndType(results);
        String entityKey = StringUtils.hasText(key) ? key : (resolved != null ? resolved.key : null);

        MainEntity mainEntity = extractMainEntity(effectiveCypher);
        String mainName = herbA != null ? herbA : (mainEntity != null ? mainEntity.name : "");

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=common-results.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("main_name,item_name");
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            for (Map<String, Object> row : results) {
                if (row == null) {
                    continue;
                }
                ItemExtracted extracted = extractItemFromRow(row, entityKey);
                String itemName = extracted != null ? extracted.label : null;
                if (!StringUtils.hasText(itemName)) {
                    Object fallback = StringUtils.hasText(entityKey) ? row.get(entityKey) : null;
                    if (fallback == null && !row.isEmpty()) {
                        fallback = row.values().stream().filter(Objects::nonNull).findFirst().orElse(null);
                    }
                    if (fallback != null) {
                        itemName = trimValue(fallback.toString());
                    }
                }
                if (!StringUtils.hasText(itemName)) {
                    continue;
                }
                if (itemName.equals(mainName)) {
                    continue;
                }
                if (seen.add(itemName)) {
                    writer.printf("%s,%s%n",
                            escapeCsv(mainName),
                            escapeCsv(itemName));
                }
            }
        } catch (IOException e) {
            log.error("导出共同结果CSV失败", e);
            throw new ServiceException("共同结果CSV导出失败");
        }
    }

    /**
     * 调用LLM生成Cypher查询
     */
    private String generateCypher(String question, List<LlmMessage> history) {
        String historyStr = formatHistory(history);
        String prompt = TcmPromptTemplates.CYPHER_GENERATION_TEMPLATE
                .replace("{history}", historyStr)
                .replace("{question}", question);

        List<LlmMessage> messages = Collections.singletonList(
                createMessage("user", prompt)
        );

        return callDeepSeekInternal(messages, 0.1, 0.8);
    }

    /**
     * 调用LLM生成最终答案
     */
    private String summarizeAnswer(String question, List<LlmMessage> history, String results) {
        String historyStr = formatHistory(history);
        String prompt = TcmPromptTemplates.SUMMARY_TEMPLATE
                .replace("{question}", question)
                .replace("{history}", historyStr)
                .replace("{results}", results);

        List<LlmMessage> messages = Collections.singletonList(
                createMessage("user", prompt)
        );

        return callDeepSeekInternal(messages, 0.2, 0.8);
    }

    /**
     * 调用DeepSeek API（专业模式使用）
     */
    private String callDeepSeekInternal(List<LlmMessage> messages, double temperature, double topP) {
        try {
            String endpoint = deepSeekProperties.getBaseUrl() + "/v1/chat/completions";

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", deepSeekProperties.getChatModel());
            payload.put("temperature", temperature);
            payload.put("max_tokens", 2048);
            payload.put("top_p", topP);
            payload.put("stream", false);

            ArrayNode messagesNode = payload.putArray("messages");
            for (LlmMessage message : messages) {
                ObjectNode messageNode = messagesNode.addObject();
                messageNode.put("role", message.getRole());
                messageNode.put("content", message.getContent());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekProperties.getApiKey());

            HttpEntity<ObjectNode> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                log.warn("DeepSeek 服务返回空响应，状态码：{}", response.getStatusCode());
                throw new ServiceException("DeepSeek服务暂不可用");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.elements().hasNext()) {
                JsonNode firstChoice = choices.get(0);
                JsonNode messageNode = firstChoice.path("message");
                String reply = messageNode.path("content").asText("").trim();
                if (StringUtils.hasText(reply)) {
                    return reply;
                }
            }

            throw new ServiceException("DeepSeek没有返回有效内容");

        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.error("DeepSeek 服务错误响应，status={} body={}", e.getRawStatusCode(), body);
            String message = extractErrorMessage(body);
            throw new ServiceException(StringUtils.hasText(message) ? message : "DeepSeek服务异常：" + e.getRawStatusCode());
        } catch (Exception e) {
            log.error("调用DeepSeek服务失败", e);
            throw new ServiceException("DeepSeek服务暂不可用");
        }
    }

    private List<Map<String, Object>> executeCypherSafely(String cypherQuery) {
        try {
            List<Map<String, Object>> results = tcmNeo4jService.executeQuery(cypherQuery);
            return results != null ? results : Collections.emptyList();
        } catch (Exception e) {
            log.warn("执行Cypher查询失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private LlmGraphData buildGraphData(String cypherQuery, List<Map<String, Object>> searchResults, ResolvedKey resolvedKey) {
        if (CollectionUtils.isEmpty(searchResults)) {
            return null;
        }

        List<String> herbList = extractHerbNames(cypherQuery);
        if (herbList.isEmpty()) {
            return null;
        }
        List<String> herbs = herbList.size() > 2 ? new ArrayList<>(herbList.subList(0, 2)) : herbList;

        if (resolvedKey == null || !StringUtils.hasText(resolvedKey.key)) {
            return null;
        }
        String entityKey = resolvedKey.key;
        String entityType = resolvedKey.type;

        LinkedHashSet<String> targets = new LinkedHashSet<>();
        for (Map<String, Object> row : searchResults) {
            if (row == null) {
                continue;
            }
            Object targetValue = row.get(entityKey);
            if (targetValue != null && StringUtils.hasText(targetValue.toString())) {
                targets.add(targetValue.toString());
                if (targets.size() >= GRAPH_TARGET_LIMIT) {
                    break;
                }
            }
        }

        if (targets.isEmpty()) {
            return null;
        }

        LlmGraphData graphData = new LlmGraphData();
        graphData.setTotalTargets(searchResults.size());
        graphData.setDisplayedTargets(targets.size());
        graphData.setHerbs(new ArrayList<>(herbs));
        graphData.setEntityType(entityType);
        graphData.setEntityKey(entityKey);

        List<LlmGraphData.GraphNode> nodes = new ArrayList<>();
        for (String herb : herbs) {
            LlmGraphData.GraphNode node = new LlmGraphData.GraphNode();
            node.setId("herb:" + herb);
            node.setLabel(herb);
            node.setType("herb");
            nodes.add(node);
        }
        for (String target : targets) {
            LlmGraphData.GraphNode node = new LlmGraphData.GraphNode();
            node.setId("target:" + target);
            node.setLabel(target);
            node.setType(entityType);
            nodes.add(node);
        }
        graphData.setNodes(nodes);

        List<LlmGraphData.GraphEdge> edges = new ArrayList<>();
        for (String herb : herbs) {
            String sourceId = "herb:" + herb;
            for (String target : targets) {
                LlmGraphData.GraphEdge edge = new LlmGraphData.GraphEdge();
                edge.setSource(sourceId);
                edge.setTarget("target:" + target);
                edge.setType("herb-target");
                edges.add(edge);
            }
        }
        graphData.setEdges(edges);
        return graphData;
    }

    private LlmGraphData buildContainmentGraph(String cypherQuery, List<Map<String, Object>> searchResults, ResolvedKey resolvedKey) {
        if (CollectionUtils.isEmpty(searchResults)) {
            return null;
        }
        MainEntity main = extractMainEntity(cypherQuery);
        if (main == null || !StringUtils.hasText(main.name)) {
            return null;
        }

        String entityKey = resolvedKey != null ? resolvedKey.key : null;
        String entityType = resolvedKey != null ? resolvedKey.type : null;

        LinkedHashSet<String> items = new LinkedHashSet<>();
        for (Map<String, Object> row : searchResults) {
            if (row == null) {
                continue;
            }
            ItemExtracted extracted = extractItemFromRow(row, entityKey);
            if (extracted.type != null && entityType == null) {
                entityType = extracted.type;
            }
            if (StringUtils.hasText(extracted.label)) {
                items.add(extracted.label);
                if (items.size() >= GRAPH_TARGET_LIMIT) {
                    break;
                }
            }
        }
        if (items.isEmpty()) {
            return null;
        }

        String finalEntityType = StringUtils.hasText(entityType) ? entityType : "other";

        LlmGraphData graphData = new LlmGraphData();
        graphData.setTotalTargets(searchResults.size());
        graphData.setDisplayedTargets(items.size());
        graphData.setEntityType(finalEntityType);
        graphData.setEntityKey(entityKey);
        graphData.setMainName(main.name);
        graphData.setMainType(main.type);

        List<LlmGraphData.GraphNode> nodes = new ArrayList<>();
        LlmGraphData.GraphNode mainNode = new LlmGraphData.GraphNode();
        mainNode.setId(main.type + ":" + main.name);
        mainNode.setLabel(main.name);
        mainNode.setType(main.type);
        nodes.add(mainNode);

        for (String item : items) {
            LlmGraphData.GraphNode node = new LlmGraphData.GraphNode();
            node.setId(finalEntityType + ":" + item);
            node.setLabel(item);
            node.setType(finalEntityType);
            nodes.add(node);
        }
        graphData.setNodes(nodes);

        List<LlmGraphData.GraphEdge> edges = new ArrayList<>();
        for (String item : items) {
            LlmGraphData.GraphEdge edge = new LlmGraphData.GraphEdge();
            edge.setSource(mainNode.getId());
            edge.setTarget(finalEntityType + ":" + item);
            edge.setType("contains");
            edges.add(edge);
        }
        graphData.setEdges(edges);
        return graphData;
    }

    private ResolvedKey resolveEntityKeyAndType(List<Map<String, Object>> searchResults) {
        if (CollectionUtils.isEmpty(searchResults)) {
            return null;
        }
        Map<String, Object> first = searchResults.get(0);
        if (first == null || first.isEmpty()) {
            return null;
        }
        List<String> targetKeys = Arrays.asList("target", "target_symbol", "symbol");
        for (String key : targetKeys) {
            if (first.containsKey(key)) {
                return new ResolvedKey(key, "target");
            }
        }
        List<String> compoundKeys = Arrays.asList("compound", "compound_name", "inchikey", "name", "compound_name_zh");
        for (String key : compoundKeys) {
            if (first.containsKey(key)) {
                return new ResolvedKey(key, "compound");
            }
        }
        for (String key : first.keySet()) {
            if (StringUtils.hasText(key)) {
                return new ResolvedKey(key, "other");
            }
        }
        return null;
    }

    private List<String> extractHerbNames(String cypherQuery) {
        if (!StringUtils.hasText(cypherQuery)) {
            return Collections.emptyList();
        }
        Pattern pattern = Pattern.compile("herb_name_zh\\s*:\\s*['\\\"]([^'\\\"]+)['\\\"]");
        Matcher matcher = pattern.matcher(cypherQuery);

        LinkedHashSet<String> herbs = new LinkedHashSet<>();
        while (matcher.find()) {
            herbs.add(matcher.group(1));
            if (herbs.size() >= 3) {
                break;
            }
        }
        return new ArrayList<>(herbs);
    }

    private MainEntity extractMainEntity(String cypherQuery) {
        if (!StringUtils.hasText(cypherQuery)) {
            return null;
        }
        List<MainPattern> patterns = Arrays.asList(
                new MainPattern("Prescription", "name_zh"),
                new MainPattern("Herb", "herb_name_zh"),
                new MainPattern("OtherHerb", "herb_name_zh"),
                new MainPattern("Compound", "inchikey"),
                new MainPattern("Target", "symbol"),
                new MainPattern("Disease", "disease_name"),
                new MainPattern("Syndrome", "syndrome_name_zh"),
                new MainPattern("Symptom", "symptom_name_zh"),
                new MainPattern("WmSymptom", "symptom_name"),
                new MainPattern("Phenotype", "phenotype_name"),
                new MainPattern("Pathway", "name"),
                new MainPattern("MedicalCase", "med_case_id")
        );
        for (MainPattern p : patterns) {
            Pattern regex = Pattern.compile(":" + p.label + "\\s*\\{[^}]*" + p.prop + "\\s*:\\s*['\\\"]([^'\\\"]+)['\\\"]");
            Matcher m = regex.matcher(cypherQuery);
            if (m.find()) {
                return new MainEntity(p.label.toLowerCase(Locale.ROOT), m.group(1));
            }
        }
        return null;
    }

    private String buildCsvDownloadUrl(LlmGraphData graphData, String cypher, ResolvedKey resolvedKey) {
        if (graphData == null || resolvedKey == null || !StringUtils.hasText(resolvedKey.key)) {
            return null;
        }
        try {
            String key = URLEncoder.encode(resolvedKey.key, StandardCharsets.UTF_8.name());
            String encodedCypher = StringUtils.hasText(cypher)
                    ? URLEncoder.encode(cypher, StandardCharsets.UTF_8.name())
                    : "";
            StringBuilder sb = new StringBuilder("/tcmseek/llm/aichat/common-targets/export?key=").append(key);
            if (graphData.getHerbs() != null && graphData.getHerbs().size() >= 2) {
                String herbA = URLEncoder.encode(graphData.getHerbs().get(0), StandardCharsets.UTF_8.name());
                String herbB = URLEncoder.encode(graphData.getHerbs().get(1), StandardCharsets.UTF_8.name());
                sb.append("&herbA=").append(herbA).append("&herbB=").append(herbB);
            }
            if (StringUtils.hasText(encodedCypher)) {
                sb.append("&cypher=").append(encodedCypher);
            }
            if (StringUtils.hasText(graphData.getMainName())) {
                sb.append("&mainName=").append(URLEncoder.encode(graphData.getMainName(), StandardCharsets.UTF_8.name()));
                if (StringUtils.hasText(graphData.getMainType())) {
                    sb.append("&mainType=").append(URLEncoder.encode(graphData.getMainType(), StandardCharsets.UTF_8.name()));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("构建CSV下载链接失败: {}", e.getMessage());
            return null;
        }
    }

    private String appendGraphNotice(String reply, LlmGraphData graphData) {
        if (graphData == null || graphData.getDisplayedTargets() <= 0) {
            return reply;
        }
        StringBuilder sb = new StringBuilder(StringUtils.hasText(reply) ? reply.trim() : "");
        if (sb.length() > 0) {
            sb.append("\n\n");
        }
        String typeLabel = "结果";
        if ("target".equalsIgnoreCase(graphData.getEntityType())) {
            typeLabel = "靶点";
        } else if ("compound".equalsIgnoreCase(graphData.getEntityType())) {
            typeLabel = "化合物";
        }
        sb.append("提示：共找到")
                .append(graphData.getTotalTargets())
                .append("个共同")
                .append(typeLabel)
                .append("，图中展示前")
                .append(graphData.getDisplayedTargets())
                .append("个。可下载完整CSV查看全部列表。");
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private Map<String, Object> buildHerbParams(String herbA, String herbB) {
        Map<String, Object> params = new HashMap<>();
        params.put("herbA", herbA);
        params.put("herbB", herbB);
        return params;
    }

    private String buildDefaultCommonTargetCypher(String herbA, String herbB) {
        if (!StringUtils.hasText(herbA) || !StringUtils.hasText(herbB)) {
            return null;
        }
        return "MATCH (h1:Herb {herb_name_zh: $herbA})-[:CONTAINS_COMPOUND]->(c1:Compound)-[:TARGETS]->(t:Target)<-[:TARGETS]-(c2:Compound)<-[:CONTAINS_COMPOUND]-(h2:Herb {herb_name_zh: $herbB}) RETURN DISTINCT t.symbol AS target";
    }

    private static class ResolvedKey {
        final String key;
        final String type;

        ResolvedKey(String key, String type) {
            this.key = key;
            this.type = type;
        }
    }

    private static class MainEntity {
        final String type;
        final String name;

        MainEntity(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private static class MainPattern {
        final String label;
        final String prop;

        MainPattern(String label, String prop) {
            this.label = label;
            this.prop = prop;
        }
    }

    private static class ItemExtracted {
        final String label;
        final String type;

        ItemExtracted(String label, String type) {
            this.label = label;
            this.type = type;
        }
    }

    private static class NameId {
        final String name;
        final String id;
        final String type;

        NameId(String name, String id, String type) {
            this.name = name;
            this.id = id;
            this.type = type;
        }
    }

    private NameId extractNameId(Object value) {
        if (value == null) {
            return new NameId("", "", null);
        }
        List<String> nameKeys = Arrays.asList(
                "herb_name_zh", "name_zh", "symbol", "inchikey", "disease_name",
                "phenotype_name", "pathway_id", "name", "symptom_name_zh", "symptom_name",
                "syndrome_name_zh", "med_case_id", "tcm_prescription_id", "tcm_tar_id",
                "tcm_herb_id", "tcm_herb2_id"
        );
        List<String> idKeys = Arrays.asList(
                "tcm_prescription_id", "tcm_tar_id", "tcm_herb_id", "tcm_herb2_id",
                "inchikey", "disease_id", "phenotype_id", "pathway_id",
                "tcm_symptom_id", "wm_symptom_id", "med_case_id", "symbol"
        );

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            String type = detectTypeFromNodeMap(map);
            String name = null;
            for (String k : nameKeys) {
                if (map.containsKey(k) && map.get(k) != null) {
                    name = trimValue(map.get(k).toString());
                    break;
                }
            }
            String id = null;
            for (String k : idKeys) {
                if (map.containsKey(k) && map.get(k) != null) {
                    id = trimValue(map.get(k).toString());
                    break;
                }
            }
            if (name == null && id != null) {
                name = id;
            }
            if (id == null && name != null) {
                id = name;
            }
            return new NameId(name != null ? name : "", id != null ? id : "", type);
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object item : list) {
                NameId inner = extractNameId(item);
                if (StringUtils.hasText(inner.name)) {
                    return inner;
                }
            }
        }
        String val = trimValue(value.toString());
        return new NameId(val, val, null);
    }

    private String buildCompactResults(List<Map<String, Object>> searchResults, ResolvedKey resolvedKey) {
        if (CollectionUtils.isEmpty(searchResults)) {
            return "无相关信息";
        }
        List<String> lines = new ArrayList<>();
        int limit = Math.min(searchResults.size(), SUMMARY_ITEM_LIMIT);
        for (int i = 0; i < limit; i++) {
            Map<String, Object> row = searchResults.get(i);
            if (row == null) {
                continue;
            }
            lines.add(compactRow(row, resolvedKey));
        }
        if (searchResults.size() > limit) {
            lines.add("... 其余 " + (searchResults.size() - limit) + " 条已截断，可下载CSV查看全量。");
        }
        return String.join("\n", lines);
    }

    private String compactRow(Map<String, Object> row, ResolvedKey resolvedKey) {
        // 优先使用检测到的实体字段
        if (resolvedKey != null && row.containsKey(resolvedKey.key)) {
            Object v = row.get(resolvedKey.key);
            if (v != null) {
                return trimValue(v.toString());
            }
        }

        // 遍历行中的节点或常见列，挑关键字段
        List<String> keyPriority = Arrays.asList(
                "herb_name_zh", "name_zh", "symbol", "inchikey", "disease_name",
                "phenotype_name", "pathway_id", "name", "symptom_name_zh", "symptom_name",
                "syndrome_name_zh", "med_case_id", "tcm_prescription_id", "tcm_tar_id",
                "tcm_herb_id", "tcm_herb2_id"
        );
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            Object v = entry.getValue();
            if (v instanceof Map) {
                String picked = pickFromNodeMap((Map<String, Object>) v, keyPriority);
                if (StringUtils.hasText(picked)) {
                    return picked;
                }
            } else if (v instanceof List) {
                String picked = pickFromList((List<?>) v, keyPriority);
                if (StringUtils.hasText(picked)) {
                    return picked;
                }
            } else if (v != null) {
                String key = entry.getKey();
                if (keyPriority.contains(key)) {
                    return trimValue(v.toString());
                }
            }
        }

        // Fallback: 拼出少量 key:value
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (count >= 3) {
                break;
            }
            String k = entry.getKey();
            Object v = entry.getValue();
            if (v == null) {
                continue;
            }
            if (isLongField(k)) {
                continue;
            }
            sb.append(k).append(": ").append(trimValue(v.toString())).append("; ");
            count++;
        }
        return sb.length() > 0 ? sb.toString() : "[空行]";
    }

    private String pickFromNodeMap(Map<String, Object> nodeMap, List<String> keyPriority) {
        for (String k : keyPriority) {
            if (nodeMap.containsKey(k)) {
                Object v = nodeMap.get(k);
                if (v != null) {
                    return trimValue(v.toString());
                }
            }
        }
        return null;
    }

    private String detectTypeFromNodeMap(Map<String, Object> nodeMap) {
        Object labelsObj = nodeMap.get("_labels");
        if (labelsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> labels = (List<Object>) labelsObj;
            for (Object l : labels) {
                String label = l != null ? l.toString() : "";
                if ("Herb".equalsIgnoreCase(label) || "CoreHerb".equalsIgnoreCase(label) || "OtherHerb".equalsIgnoreCase(label)) {
                    return "herb";
                }
                if ("Prescription".equalsIgnoreCase(label)) {
                    return "prescription";
                }
                if ("Compound".equalsIgnoreCase(label)) {
                    return "compound";
                }
                if ("Target".equalsIgnoreCase(label)) {
                    return "target";
                }
                if ("Disease".equalsIgnoreCase(label)) {
                    return "disease";
                }
                if ("Syndrome".equalsIgnoreCase(label)) {
                    return "syndrome";
                }
                if ("Symptom".equalsIgnoreCase(label) || "WmSymptom".equalsIgnoreCase(label)) {
                    return "symptom";
                }
                if ("Phenotype".equalsIgnoreCase(label)) {
                    return "phenotype";
                }
                if ("Pathway".equalsIgnoreCase(label)) {
                    return "pathway";
                }
                if ("MedicalCase".equalsIgnoreCase(label)) {
                    return "medicalcase";
                }
            }
        }
        return null;
    }

    private String pickFromList(List<?> list, List<String> keyPriority) {
        for (Object item : list) {
            if (item instanceof Map) {
                String picked = pickFromNodeMap((Map<String, Object>) item, keyPriority);
                if (StringUtils.hasText(picked)) {
                    return picked;
                }
            } else if (item != null && StringUtils.hasText(item.toString())) {
                return trimValue(item.toString());
            }
        }
        return null;
    }

    private ItemExtracted extractItemFromRow(Map<String, Object> row, String entityKey) {
        List<String> keyPriority = Arrays.asList(
                "herb_name_zh", "name_zh", "symbol", "inchikey", "disease_name",
                "phenotype_name", "pathway_id", "name", "symptom_name_zh", "symptom_name",
                "syndrome_name_zh", "med_case_id", "tcm_prescription_id", "tcm_tar_id",
                "tcm_herb_id", "tcm_herb2_id"
        );
        String type = null;

        if (StringUtils.hasText(entityKey) && row.containsKey(entityKey)) {
            Object v = row.get(entityKey);
            if (v != null) {
                if (v instanceof Map) {
                    Map<String, Object> nodeMap = (Map<String, Object>) v;
                    String picked = pickFromNodeMap(nodeMap, keyPriority);
                    type = detectTypeFromNodeMap(nodeMap);
                    if (StringUtils.hasText(picked)) {
                        return new ItemExtracted(trimValue(picked), type);
                    }
                }
                return new ItemExtracted(trimValue(v.toString()), null);
            }
        }

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            Object v = entry.getValue();
            if (v instanceof Map) {
                Map<String, Object> nodeMap = (Map<String, Object>) v;
                String picked = pickFromNodeMap(nodeMap, keyPriority);
                if (StringUtils.hasText(picked)) {
                    type = detectTypeFromNodeMap(nodeMap);
                    return new ItemExtracted(trimValue(picked), type);
                }
            } else if (v instanceof List) {
                String picked = pickFromList((List<?>) v, keyPriority);
                if (StringUtils.hasText(picked)) {
                    return new ItemExtracted(trimValue(picked), null);
                }
            } else if (v != null && keyPriority.contains(entry.getKey())) {
                return new ItemExtracted(trimValue(v.toString()), null);
            }
        }

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            Object v = entry.getValue();
            if (v != null && !isLongField(entry.getKey())) {
                return new ItemExtracted(trimValue(v.toString()), null);
            }
        }
        return new ItemExtracted(null, type);
    }

    private boolean isLongField(String key) {
        if (!StringUtils.hasText(key)) {
            return true;
        }
        String lower = key.toLowerCase(Locale.ROOT);
        return lower.contains("smiles")
                || lower.contains("description")
                || lower.contains("report")
                || lower.contains("symptoms")
                || lower.contains("indication")
                || lower.contains("canonical");
    }

    private String trimValue(String val) {
        if (val == null) {
            return "";
        }
        String trimmed = val.trim();
        if (trimmed.length() > FIELD_VALUE_MAX_LEN) {
            return trimmed.substring(0, FIELD_VALUE_MAX_LEN) + "…";
        }
        return trimmed;
    }

    /**
     * 格式化历史记录为字符串
     */
    private String formatHistory(List<LlmMessage> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (LlmMessage msg : history) {
            String role = msg.getRole() != null ? msg.getRole() : "user";
            String content = msg.getContent() != null ? msg.getContent() : "";
            sb.append(role).append(": ").append(content).append("\n");
        }
        return sb.toString();
    }

    /**
     * 创建消息对象
     */
    private LlmMessage createMessage(String role, String content) {
        LlmMessage message = new LlmMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    /**
     * 创建 LLM RestTemplate
     */
    private RestTemplate createRestTemplate(LlmProperties properties) {
        if (!properties.isVerify()) {
            try {
                SSLContext sslContext = SSLContextBuilder
                        .create()
                        .loadTrustMaterial(null, (certificate, authType) -> true)
                        .build();
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                        sslContext,
                        NoopHostnameVerifier.INSTANCE);
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setSSLSocketFactory(socketFactory)
                        .build();
                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
                applyTimeout(factory);
                return new RestTemplate(factory);
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                log.warn("配置 LLM RestTemplate SSL 时失败，将回退到默认实现", e);
            }
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        applyTimeout(factory);
        return new RestTemplate(factory);
    }

    private void applyTimeout(SimpleClientHttpRequestFactory factory) {
        if (properties.getConnectTimeout() != null) {
            factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        }
        if (properties.getReadTimeout() != null) {
            factory.setReadTimeout((int) properties.getReadTimeout().toMillis());
        }
    }

    private void applyTimeout(HttpComponentsClientHttpRequestFactory factory) {
        if (properties.getConnectTimeout() != null) {
            factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        }
        if (properties.getReadTimeout() != null) {
            factory.setReadTimeout((int) properties.getReadTimeout().toMillis());
        }
    }

    /**
     * 构造请求体，由后端按固定配置拼接参数。
     */
    private ObjectNode buildPayload(List<LlmMessage> context) {
        ObjectNode body = objectMapper.createObjectNode();
        String model = properties.getModel();
        if (!StringUtils.hasText(model)) {
            throw new ServiceException("LLM 模型未配置");
        }

        double temperature = properties.getDefaultTemperature() != null
                ? properties.getDefaultTemperature()
                : 0.3d;
        int maxTokens = properties.getDefaultMaxTokens() != null
                ? properties.getDefaultMaxTokens()
                : 1024;
        boolean stream = Boolean.TRUE.equals(properties.getStream());

        body.put("model", model);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        body.put("stream", stream);

        ArrayNode messagesNode = body.putArray("messages");
        for (LlmMessage message : context) {
            ObjectNode messageNode = messagesNode.addObject();
            String role = StringUtils.hasText(message.getRole()) ? message.getRole() : "user";
            messageNode.put("role", role);
            messageNode.put("content", message.getContent());
        }

        log.info("发送LLM 请求：model={}, temperature={}, maxTokens={}, stream={}, messages={}",
                model, temperature, maxTokens, stream, context.size());

        return body;
    }

    private String buildChatCompletionsUrl() {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new ServiceException("尚未配置大模型服务地址");
        }
        String base = properties.getBaseUrl().endsWith("/")
                ? properties.getBaseUrl().substring(0, properties.getBaseUrl().length() - 1)
                : properties.getBaseUrl();
        return base + "/v1/chat/completions";
    }

    private LlmChatResponse parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        LlmChatResponse response = new LlmChatResponse();
        response.setId(root.path("id").asText(null));
        response.setModel(root.path("model").asText(null));

        JsonNode choices = root.path("choices");
        if (!choices.isArray() || !choices.elements().hasNext()) {
            log.warn("LLM 响应中没有 choices 字段：{}", responseBody);
            throw new ServiceException("大模型服务暂不可用，请稍后再试");
        }
        JsonNode firstChoice = choices.get(0);
        JsonNode messageNode = firstChoice.path("message");
        String reply = messageNode.path("content").asText("").trim();
        if (!StringUtils.hasText(reply)) {
            throw new ServiceException("大模型没有返回有效内容");
        }
        response.setReply(reply);
        response.setFinishReason(firstChoice.path("finish_reason").asText(null));

        JsonNode usageNode = root.path("usage");
        if (!usageNode.isMissingNode()) {
            LlmChatResponse.Usage usage = new LlmChatResponse.Usage();
            usage.setPromptTokens(usageNode.path("prompt_tokens").isInt() ? usageNode.path("prompt_tokens").asInt() : null);
            usage.setCompletionTokens(usageNode.path("completion_tokens").isInt() ? usageNode.path("completion_tokens").asInt() : null);
            usage.setTotalTokens(usageNode.path("total_tokens").isInt() ? usageNode.path("total_tokens").asInt() : null);
            response.setUsage(usage);
        }
        return response;
    }

    private String extractErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("error")) {
                JsonNode errorNode = node.get("error");
                if (errorNode.isTextual()) {
                    return errorNode.asText();
                }
                if (errorNode.has("message")) {
                    return errorNode.get("message").asText();
                }
            }
            if (node.has("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            log.debug("解析 LLM 错误响应失败，body={}", body);
        }
        return null;
    }

    private List<LlmMessage> sanitizeIncoming(List<LlmMessage> rawMessages) {
        return rawMessages.stream()
                .filter(m -> m != null && StringUtils.hasText(m.getContent()))
                .map(m -> {
                    LlmMessage clean = new LlmMessage();
                    clean.setRole(StringUtils.hasText(m.getRole()) ? m.getRole() : "user");
                    clean.setContent(m.getContent());
                    return clean;
                })
                .collect(Collectors.toList());
    }
}
