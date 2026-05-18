package com.tcmseek.ai.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphToolResult {

    private String queryType;

    private int total;

    private List<Map<String, Object>> items = new ArrayList<>();

    public GraphToolResult() {
    }

    public GraphToolResult(String queryType, int total, List<Map<String, Object>> items) {
        this.queryType = queryType;
        this.total = total;
        this.items = items;
    }

    public static GraphToolResult of(String queryType, List<Map<String, Object>> items) {
        return new GraphToolResult(queryType, items == null ? 0 : items.size(), items);
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }
}
