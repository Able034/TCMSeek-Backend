package com.tcmseek.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class ConversationListResponse {

    private List<ConversationSummaryDto> items = new ArrayList<>();

    private long total;

    public ConversationListResponse() {
    }

    public ConversationListResponse(List<ConversationSummaryDto> items, long total) {
        this.items = items;
        this.total = total;
    }

    public List<ConversationSummaryDto> getItems() {
        return items;
    }

    public void setItems(List<ConversationSummaryDto> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
