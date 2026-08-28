from __future__ import annotations

from typing import Any

from pydantic import BaseModel, ConfigDict, Field, model_validator


class AiMessage(BaseModel):
    role: str = "user"
    content: str = Field(min_length=1)


class AiChatRequest(BaseModel):
    sessionId: str | None = None
    messages: list[AiMessage] = Field(default_factory=list)

    @model_validator(mode="after")
    def _messages_required(self) -> "AiChatRequest":
        if not self.messages:
            raise ValueError("messages cannot be empty")
        return self


class Usage(BaseModel):
    promptTokens: int | None = None
    completionTokens: int | None = None
    totalTokens: int | None = None


class GraphToolResult(BaseModel):
    queryType: str
    total: int = 0
    items: list[dict[str, Any]] = Field(default_factory=list)

    @classmethod
    def of(cls, query_type: str, items: list[dict[str, Any]] | None) -> "GraphToolResult":
        rows = items or []
        return cls(queryType=query_type, total=len(rows), items=rows)


class ToolCallResult(BaseModel):
    toolName: str
    arguments: dict[str, Any] = Field(default_factory=dict)
    result: GraphToolResult


class GraphNode(BaseModel):
    id: str
    label: str
    type: str


class GraphEdge(BaseModel):
    source: str
    target: str
    type: str


class AiGraphData(BaseModel):
    nodes: list[GraphNode] = Field(default_factory=list)
    edges: list[GraphEdge] = Field(default_factory=list)
    totalTargets: int = 0
    displayedTargets: int = 0
    entityType: str | None = None
    entityKey: str | None = None
    herbs: list[str] = Field(default_factory=list)
    mainName: str | None = None
    mainType: str | None = None


class AiChatResponse(BaseModel):
    model_config = ConfigDict(extra="ignore")

    id: str | None = None
    reply: str
    provider: str
    model: str
    finishReason: str | None = None
    usage: Usage | None = None
    graph: AiGraphData | None = None
    csvDownloadUrl: str | None = None
    totalResults: int | None = None
    displayedResults: int | None = None
    reasoningTrace: str | None = None
    toolResults: list[ToolCallResult] = Field(default_factory=list)


class ApiResult(BaseModel):
    code: int = 200
    msg: str = "操作成功"
    data: Any | None = None

    @classmethod
    def success(cls, data: Any) -> "ApiResult":
        return cls(data=data)


class ErrorResponse(BaseModel):
    code: str
    message: str
    requestId: str | None = None


class ConversationCreateRequest(BaseModel):
    mode: str | None = "academic"
    title: str | None = None


class ConversationUpdateRequest(BaseModel):
    title: str | None = None


class ConversationSummaryDto(BaseModel):
    id: str
    title: str | None = None
    mode: str = "academic"
    status: str = "active"
    messageCount: int = 0
    lastMessageAt: str | None = None
    updatedAt: str | None = None


class ConversationListResponse(BaseModel):
    items: list[ConversationSummaryDto] = Field(default_factory=list)
    total: int = 0


class ConversationMessageDto(BaseModel):
    id: str
    role: str
    content: str
    provider: str | None = None
    model: str | None = None
    finishReason: str | None = None
    totalResults: int | None = None
    displayedResults: int | None = None
    csvExportId: str | None = None
    requestId: str | None = None
    latencyMs: int | None = None
    createdAt: str | None = None


class ConversationMessagesResponse(BaseModel):
    conversationId: str
    messages: list[ConversationMessageDto] = Field(default_factory=list)


class AiMemoryDto(BaseModel):
    id: str | None = None
    userId: str | None = None
    memoryType: str
    content: str
    confidence: float = 1.0
    updatedAt: str | None = None


class ConversationSummaryState(BaseModel):
    conversationId: str
    userId: str
    summary: str
    coveredMessageCount: int = 0
    updatedAt: str | None = None


class SemanticAnchor(BaseModel):
    id: str
    entityType: str
    entityId: str | None = None
    neo4jLabel: str | None = None
    neo4jKey: str | None = None
    name: str
    aliases: list[str] = Field(default_factory=list)
    sourceTable: str | None = None
    sourcePk: str | None = None
    metadata: dict[str, Any] = Field(default_factory=dict)
    distance: float = 0


class VectorStatus(BaseModel):
    enabled: bool
    jdbcAvailable: bool
    embeddingConfigured: bool
    dimension: int
    documentCount: int
