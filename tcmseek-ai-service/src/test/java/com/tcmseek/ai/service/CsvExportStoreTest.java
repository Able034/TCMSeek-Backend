package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CsvExportStoreTest {

    @Test
    void exportsHerbCompoundTargetsWithStableColumns() {
        AiRedisCache redisCache = mock(AiRedisCache.class);
        CsvExportStore store = new CsvExportStore(redisCache);
        ToolCallResult toolResult = new ToolCallResult(
                "findHerbCompoundTargets",
                Map.of("herbName", "人参"),
                GraphToolResult.of("herb_compound_targets", List.of(Map.of(
                        "compound", "Ginsenoside Rg1",
                        "inchikey", "JPUKWEQWGBDDQB-UHFFFAOYSA-N",
                        "formula", "C42H72O14",
                        "target", "AKT1",
                        "targetId", "TAR0001"))));

        String exportId = store.saveFirstExport(List.of(toolResult), "11");
        CsvExportStore.ExportRecord record = store.get(exportId, "11");

        assertThat(record.getFilename()).isEqualTo("herb-compound-targets.csv");
        assertThat(record.getHeaders()).containsExactly(
                "herb_name", "compound", "inchikey", "formula", "target", "target_id");
        assertThat(record.getRows()).containsExactly(List.of(
                "人参",
                "Ginsenoside Rg1",
                "JPUKWEQWGBDDQB-UHFFFAOYSA-N",
                "C42H72O14",
                "AKT1",
                "TAR0001"));
        verify(redisCache).putExport("11", exportId, record);
    }
}
