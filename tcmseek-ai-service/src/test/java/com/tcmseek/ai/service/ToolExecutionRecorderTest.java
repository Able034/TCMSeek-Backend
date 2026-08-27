package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class ToolExecutionRecorderTest {

    @Test
    void propagatesRecordingContextToWorkerThread() throws Exception {
        ToolExecutionRecorder recorder = new ToolExecutionRecorder();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            recorder.start();
            Supplier<String> workerCall = recorder.propagate(() -> {
                recorder.record(
                        "findHerbCompounds",
                        Map.of("herbName", "人参"),
                        GraphToolResult.of("herb_compounds", List.of(Map.of("compound", "ginsenoside"))));
                return "ok";
            });

            assertThat(executor.submit(workerCall::get).get()).isEqualTo("ok");

            List<ToolCallResult> calls = recorder.finish();
            assertThat(calls).hasSize(1);
            assertThat(calls.get(0).getToolName()).isEqualTo("findHerbCompounds");
            assertThat(calls.get(0).getArguments()).containsEntry("herbName", "人参");
        } finally {
            executor.shutdownNow();
        }
    }
}
