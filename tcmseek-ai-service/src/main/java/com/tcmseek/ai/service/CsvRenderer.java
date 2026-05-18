package com.tcmseek.ai.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CsvRenderer {

    public String render(CsvExportStore.ExportRecord record) {
        StringBuilder sb = new StringBuilder();
        appendRow(sb, record.getHeaders());
        for (List<String> row : record.getRows()) {
            appendRow(sb, row);
        }
        return sb.toString();
    }

    private void appendRow(StringBuilder sb, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(values.get(i)));
        }
        sb.append('\n');
    }

    private String escape(String value) {
        String raw = value == null ? "" : value;
        boolean needQuotes = raw.contains(",") || raw.contains("\"") || raw.contains("\n") || raw.contains("\r");
        String escaped = raw.replace("\"", "\"\"");
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }
}
