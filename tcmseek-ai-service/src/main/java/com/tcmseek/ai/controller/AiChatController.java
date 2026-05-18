package com.tcmseek.ai.controller;

import com.tcmseek.ai.dto.AiChatRequest;
import com.tcmseek.ai.dto.AiChatResponse;
import com.tcmseek.ai.service.AiRequestContext;
import com.tcmseek.ai.service.AiChatService;
import com.tcmseek.ai.service.CsvExportStore;
import com.tcmseek.ai.service.CsvRenderer;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final String USER_ID_HEADER = "X-User-Id";

    private static final String USERNAME_HEADER = "X-User-Name";

    private static final String ACCOUNT_HEADER = "X-User-Account";

    private final AiChatService aiChatService;

    private final CsvExportStore csvExportStore;

    private final CsvRenderer csvRenderer;

    public AiChatController(AiChatService aiChatService,
                            CsvExportStore csvExportStore,
                            CsvRenderer csvRenderer) {
        this.aiChatService = aiChatService;
        this.csvExportStore = csvExportStore;
        this.csvRenderer = csvRenderer;
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public AiChatResponse chat(@Valid @RequestBody AiChatRequest request,
                               @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
                               @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                               @RequestHeader(value = USERNAME_HEADER, required = false) String username,
                               @RequestHeader(value = ACCOUNT_HEADER, required = false) String account) {
        return aiChatService.chat(request, new AiRequestContext(requestId, userId, username, account));
    }

    @PostMapping(value = "/aichat", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public AiChatResponse aichat(@Valid @RequestBody AiChatRequest request,
                                 @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
                                 @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                 @RequestHeader(value = USERNAME_HEADER, required = false) String username,
                                 @RequestHeader(value = ACCOUNT_HEADER, required = false) String account) {
        return aiChatService.chat(request, new AiRequestContext(requestId, userId, username, account));
    }

    @GetMapping(value = "/exports/{exportId}", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportCsv(@PathVariable String exportId,
                                            @RequestHeader(value = USER_ID_HEADER, required = false) String userId) {
        CsvExportStore.ExportRecord record = csvExportStore.get(exportId, userId);
        if (record == null) {
            throw new ResponseStatusException(NOT_FOUND, "export not found or expired");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + record.getFilename() + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body("\uFEFF" + csvRenderer.render(record));
    }
}
