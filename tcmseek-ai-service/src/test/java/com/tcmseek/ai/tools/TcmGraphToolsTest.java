package com.tcmseek.ai.tools;

import com.tcmseek.ai.config.AiToolProperties;
import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.graph.TcmGraphRepository;
import com.tcmseek.ai.service.EntityNormalizeService;
import com.tcmseek.ai.service.SemanticSearchService;
import com.tcmseek.ai.service.ToolExecutionRecorder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TcmGraphToolsTest {

    @Test
    @SuppressWarnings("unchecked")
    void findHerbCompoundTargetsQueriesCompoundTargetPath() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(50);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        when(normalizeService.normalizeHerb("人参")).thenReturn("人参");
        when(repository.query(anyString(), anyMap())).thenReturn(List.of(Map.of(
                "herb", "人参",
                "compound", "Ginsenoside Rg1",
                "target", "AKT1")));

        GraphToolResult result = tools.findHerbCompoundTargets("人参");

        ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(repository).query(cypherCaptor.capture(), paramsCaptor.capture());
        verify(recorder).record("findHerbCompoundTargets", paramsCaptor.getValue(), result);
        assertThat(cypherCaptor.getValue()).contains("CONTAINS_COMPOUND]->(c:Compound)-[:TARGETS]->(t:Target)");
        assertThat(paramsCaptor.getValue())
                .containsEntry("herbName", "人参")
                .containsEntry("originalHerbName", "人参")
                .containsEntry("limit", 50);
        assertThat(result.getQueryType()).isEqualTo("herb_compound_targets");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findHerbCompoundTargetsByTopicFiltersTargetsFromSemanticAnchors() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        when(normalizeService.normalizeHerb("浜哄弬")).thenReturn("浜哄弬");
        List<SemanticSearchService.SemanticAnchor> anchors = List.of(new SemanticSearchService.SemanticAnchor(
                "target:tnf",
                "target",
                "TNF",
                "Target",
                "symbol",
                "TNF",
                List.of(),
                "neo4j.Target",
                "TNF",
                Map.of(),
                0.1));
        when(semanticSearchService.search(anyString(), any(), anyInt())).thenReturn(anchors);
        when(semanticSearchService.exactTopicTargetSymbols("鎶楃値")).thenReturn(List.of("IL6"));
        when(semanticSearchService.targetSymbolsFromAnchors(any())).thenReturn(List.of("TNF"));
        when(repository.query(anyString(), anyMap())).thenReturn(List.of(Map.of(
                "herb", "浜哄弬",
                "compound", "Ginsenoside Rg1",
                "target", "TNF")));

        GraphToolResult result = tools.findHerbCompoundTargetsByTopic("浜哄弬", "鎶楃値");

        ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(repository).query(cypherCaptor.capture(), paramsCaptor.capture());
        verify(recorder).record("findHerbCompoundTargetsByTopic", paramsCaptor.getValue(), result);
        assertThat(cypherCaptor.getValue()).contains("t.symbol IN $targetSymbols");
        assertThat((List<String>) paramsCaptor.getValue().get("targetSymbols")).contains("IL6", "TNF");
        assertThat(result.getQueryType()).isEqualTo("herb_compound_targets_semantic");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findDiseasePrescriptionsSemanticFallbackDoesNotUseTcmSymptomsAsAnchors() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        when(normalizeService.normalizeDisease("抑郁症")).thenReturn("depressive disorder");
        when(repository.query(anyString(), anyMap()))
                .thenReturn(List.of())
                .thenReturn(List.of(Map.of(
                        "category", "semantic_prescription",
                        "prescription", "抑郁丸")));
        List<SemanticSearchService.SemanticAnchor> anchors = List.of(new SemanticSearchService.SemanticAnchor(
                "prescription:depression",
                "prescription",
                "TCMSSD7504",
                "Prescription",
                "tcm_prescription_id",
                "抑郁丸",
                List.of(),
                "prescriptions",
                "TCMSSD7504",
                Map.of(),
                0.2));
        when(semanticSearchService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of())
                .thenReturn(anchors);

        GraphToolResult result = tools.findDiseasePrescriptions("抑郁症");

        ArgumentCaptor<List<String>> typesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(semanticSearchService, times(2)).search(anyString(), typesCaptor.capture(), anyInt());
        verify(recorder).record(anyString(), paramsCaptor.capture(), any());
        assertThat(typesCaptor.getAllValues().get(0)).containsExactly("disease");
        assertThat(typesCaptor.getAllValues().get(1)).containsExactly("prescription", "syndrome", "wm_symptom");
        assertThat((List<String>) paramsCaptor.getValue().get("semanticDiseaseQueryTypes"))
                .containsExactly("disease");
        assertThat((List<String>) paramsCaptor.getValue().get("semanticQueryTypes"))
                .containsExactly("prescription", "syndrome", "wm_symptom");
        assertThat(result.getQueryType()).isEqualTo("disease_prescriptions_semantic_fallback");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findHerbDiseasePrescriptionsUsesNeo4jWithoutSemanticSearch() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        when(normalizeService.normalizeHerb("\u4eba\u53c2")).thenReturn("\u4eba\u53c2");
        when(normalizeService.normalizeDisease("\u766b\u75eb")).thenReturn("epilepsy");
        when(repository.query(anyString(), anyMap())).thenReturn(List.of(Map.of(
                "herb", "\u4eba\u53c2",
                "disease", "epilepsy",
                "prescription", "\u5b9a\u75eb\u4e38")));

        GraphToolResult result = tools.findHerbDiseasePrescriptions("\u4eba\u53c2", "\u766b\u75eb");

        ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(repository).query(cypherCaptor.capture(), paramsCaptor.capture());
        verify(recorder).record("findHerbDiseasePrescriptions", paramsCaptor.getValue(), result);
        verifyNoInteractions(semanticSearchService);
        assertThat(cypherCaptor.getValue()).contains("CONTAINS_HERB", "TREATS_DISEASE");
        assertThat(paramsCaptor.getValue())
                .containsEntry("herbName", "\u4eba\u53c2")
                .containsEntry("diseaseQuery", "epilepsy")
                .containsEntry("conditionQuery", "\u766b\u75eb");
        assertThat(result.getQueryType()).isEqualTo("herb_disease_prescriptions");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByGraphIntentExactUsesNeo4jRelationsWithoutSemanticSearch() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        when(repository.query(anyString(), anyMap())).thenReturn(List.of(Map.of(
                "conditionType", "disease",
                "condition", "\u611f\u5192",
                "prescription", "\u94f6\u7fd8\u6563")));

        String question = "\u611f\u5192\u4e86\u6709\u4ec0\u4e48\u4e2d\u533b\u65b9\u5242\u53ef\u4ee5\u559d";
        GraphToolResult result = tools.findByGraphIntentExact(question);

        ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(repository).query(cypherCaptor.capture(), paramsCaptor.capture());
        verify(recorder).record("findByGraphIntentExact", paramsCaptor.getValue(), result);
        verifyNoInteractions(semanticSearchService);
        assertThat(cypherCaptor.getValue())
                .contains("TREATS_DISEASE", "TREATS_SYMPTOM", "TREATS_SYNDROME", "Prescription")
                .doesNotContain("embedding");
        assertThat((List<String>) paramsCaptor.getValue().get("terms")).contains("\u611f\u5192");
        assertThat((List<String>) paramsCaptor.getValue().get("textTerms")).contains("\u611f\u5192");
        assertThat(result.getQueryType()).isEqualTo("relation_intent_exact");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findDiseasePrescriptionsUsesSemanticDiseaseBeforeAnswerFallback() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        when(normalizeService.normalizeDisease("抑郁症")).thenReturn("depressive disorder");
        when(repository.query(anyString(), anyMap()))
                .thenReturn(List.of())
                .thenReturn(List.of(Map.of(
                        "category", "semantic_disease_prescription",
                        "disease", "depressive disorder",
                        "prescription", "解郁安神颗粒")));
        List<SemanticSearchService.SemanticAnchor> diseaseAnchors = List.of(new SemanticSearchService.SemanticAnchor(
                "disease:depressive-disorder",
                "disease",
                "D123",
                "Disease",
                "disease_id",
                "depressive disorder",
                List.of(),
                "diseases",
                "D123",
                Map.of(),
                0.2));
        when(semanticSearchService.search(anyString(), any(), anyInt())).thenReturn(diseaseAnchors);

        GraphToolResult result = tools.findDiseasePrescriptions("抑郁症");

        ArgumentCaptor<List<String>> typesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(semanticSearchService).search(anyString(), typesCaptor.capture(), anyInt());
        verify(recorder).record(anyString(), paramsCaptor.capture(), any());
        assertThat(typesCaptor.getValue()).containsExactly("disease");
        assertThat((List<String>) paramsCaptor.getValue().get("semanticDiseaseQueryTypes"))
                .containsExactly("disease");
        assertThat(paramsCaptor.getValue()).doesNotContainKey("semanticFallbackQueryTypes");
        assertThat(result.getQueryType()).isEqualTo("disease_prescriptions_semantic_disease");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findDiseasePrescriptionsStopsWhenSemanticDiseaseHasNoDirectPrescriptionRelations() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        when(normalizeService.normalizeDisease("抑郁症")).thenReturn("depressive disorder");
        when(repository.query(anyString(), anyMap()))
                .thenReturn(List.of())
                .thenReturn(List.of());
        List<SemanticSearchService.SemanticAnchor> diseaseAnchors = List.of(new SemanticSearchService.SemanticAnchor(
                "disease:depressive-disorder",
                "disease",
                "D123",
                "Disease",
                "disease_id",
                "depressive disorder",
                List.of(),
                "diseases",
                "D123",
                Map.of(),
                0.2));
        when(semanticSearchService.search(anyString(), any(), anyInt())).thenReturn(diseaseAnchors);

        GraphToolResult result = tools.findDiseasePrescriptions("抑郁症");

        ArgumentCaptor<List<String>> typesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(semanticSearchService).search(anyString(), typesCaptor.capture(), anyInt());
        verify(recorder).record(anyString(), paramsCaptor.capture(), any());
        assertThat(typesCaptor.getValue()).containsExactly("disease");
        assertThat(paramsCaptor.getValue()).containsEntry("finalEvidenceLevel", "no_direct_disease_prescription_relation");
        assertThat(paramsCaptor.getValue()).doesNotContainKey("semanticFallbackQueryTypes");
        assertThat(result.getQueryType()).isEqualTo("disease_prescriptions_no_direct_relation");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0))
                .containsEntry("evidenceType", "semantic_disease_anchor_no_direct_prescription")
                .containsEntry("prescriptionRelCount", 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBySemanticIntentPrefersConditionAnchorsForPrescriptionTreatmentQuestions() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        List<SemanticSearchService.SemanticAnchor> anchors = List.of(new SemanticSearchService.SemanticAnchor(
                "symptom:muscle-pain",
                "tcm_symptom",
                "SYM001",
                "Symptom",
                "tcm_symptom_id",
                "肌肉酸痛",
                List.of(),
                "tcm_symptoms",
                "SYM001",
                Map.of(),
                0.2));
        when(semanticSearchService.search(anyString(), any(), anyInt())).thenReturn(anchors);
        when(repository.query(anyString(), anyMap())).thenReturn(List.of(Map.of(
                "category", "semantic_symptom_prescription",
                "symptom", "肌肉酸痛",
                "prescription", "舒筋止痛方")));

        GraphToolResult result = tools.findBySemanticIntent("可以治疗肌肉酸痛的方剂");

        ArgumentCaptor<List<String>> typesCaptor = ArgumentCaptor.forClass(List.class);
        verify(semanticSearchService).search(anyString(), typesCaptor.capture(), anyInt());
        assertThat(typesCaptor.getValue()).containsExactly("disease", "wm_symptom", "tcm_symptom", "syndrome");
        assertThat(result.getQueryType()).isEqualTo("semantic_condition_prescriptions");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBySemanticIntentStopsAtNoDirectConditionRelation() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        List<SemanticSearchService.SemanticAnchor> anchors = List.of(new SemanticSearchService.SemanticAnchor(
                "symptom:muscle-pain",
                "tcm_symptom",
                "SYM001",
                "Symptom",
                "tcm_symptom_id",
                "muscle pain",
                List.of(),
                "tcm_symptoms",
                "SYM001",
                Map.of(),
                0.2));
        when(semanticSearchService.search(anyString(), any(), anyInt())).thenReturn(anchors);
        when(repository.query(anyString(), anyMap())).thenReturn(List.of());

        GraphToolResult result = tools.findBySemanticIntent("muscle pain which formula");

        ArgumentCaptor<List<String>> typesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(semanticSearchService).search(anyString(), typesCaptor.capture(), anyInt());
        verify(repository).query(anyString(), anyMap());
        verify(recorder).record(anyString(), paramsCaptor.capture(), any());
        assertThat(typesCaptor.getValue()).containsExactly("disease", "wm_symptom", "tcm_symptom", "syndrome");
        assertThat(paramsCaptor.getValue()).containsEntry("finalEvidenceLevel", "no_direct_condition_prescription_relation");
        assertThat(result.getQueryType()).isEqualTo("condition_prescriptions_no_direct_relation");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0))
                .containsEntry("evidenceType", "semantic_condition_anchor_no_direct_prescription")
                .containsEntry("prescriptionRelCount", 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBySemanticIntentExpandsThroatInflammationConditionQuery() {
        TcmGraphRepository repository = mock(TcmGraphRepository.class);
        EntityNormalizeService normalizeService = mock(EntityNormalizeService.class);
        ToolExecutionRecorder recorder = mock(ToolExecutionRecorder.class);
        SemanticSearchService semanticSearchService = mock(SemanticSearchService.class);
        AiToolProperties properties = new AiToolProperties();
        properties.setQueryLimit(100);
        TcmGraphTools tools = new TcmGraphTools(repository, normalizeService, recorder, properties, semanticSearchService);
        List<SemanticSearchService.SemanticAnchor> anchors = List.of(new SemanticSearchService.SemanticAnchor(
                "symptom:sore-throat",
                "tcm_symptom",
                "SYM002",
                "Symptom",
                "tcm_symptom_id",
                "\u54bd\u5589\u80bf\u75db",
                List.of(),
                "tcm_symptoms",
                "SYM002",
                Map.of(),
                0.2));
        when(semanticSearchService.search(anyString(), any(), anyInt())).thenReturn(anchors);
        when(repository.query(anyString(), anyMap())).thenReturn(List.of(Map.of(
                "category", "semantic_symptom_prescription",
                "symptom", "\u54bd\u5589\u80bf\u75db",
                "prescription", "\u7518\u6854\u6c64")));

        GraphToolResult result = tools.findBySemanticIntent("\u6cbb\u7597\u5589\u5499\u53d1\u708e\u7684\u65b9\u5242\u6709\u4ec0\u4e48");

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(semanticSearchService).search(queryCaptor.capture(), any(), anyInt());
        assertThat(queryCaptor.getValue()).contains("pharyngitis", "\u54bd\u5589\u80bf\u75db");
        assertThat(result.getQueryType()).isEqualTo("semantic_condition_prescriptions");
        assertThat(result.getItems()).hasSize(1);
    }
}
