from __future__ import annotations

import json
from collections.abc import Iterator

from fastapi import FastAPI, Header, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, Response, StreamingResponse

from app.compressor import ToolResultCompressor
from app.config import settings
from app.csv_store import CsvExportStore
from app.fallback import ToolFallbackRouter
from app.graph_builder import AiGraphBuilder
from app.models import (
    AiChatRequest,
    ApiResult,
    ConversationCreateRequest,
    ConversationListResponse,
    ConversationMessagesResponse,
    ConversationUpdateRequest,
)
from app.normalize import EntityNormalizeService
from app.postgres import AiConversationRepository, PostgresDatabase
from app.recorder import ToolExecutionRecorder
from app.repository import TcmGraphRepository
from app.semantic import QwenEmbeddingClient, SemanticSearchService
from app.session import AiConversationMemoryService, AiSessionManager
from app.services import AiChatService, AiServiceError, RequestContext, TcmReasonChatService
from app.tools import TcmGraphTools


app = FastAPI(title="TCMSeek AI Service LangChain", version="0.1.0")

origins = ["*"] if settings.cors_origins.strip() == "*" else [item.strip() for item in settings.cors_origins.split(",") if item.strip()]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

recorder = ToolExecutionRecorder()
graph_repository = TcmGraphRepository(settings)
normalizer = EntityNormalizeService(graph_repository)
postgres_database = PostgresDatabase(settings.postgres_url, settings.postgres_username, settings.postgres_password)
conversation_repository = AiConversationRepository(postgres_database, settings)
memory_service = AiConversationMemoryService(conversation_repository, settings)
session_manager = AiSessionManager(conversation_repository, memory_service, settings)
vector_database = PostgresDatabase(settings.vector_postgres_url, settings.vector_postgres_username, settings.vector_postgres_password)
embedding_client = QwenEmbeddingClient(settings)
semantic_search_service = SemanticSearchService(vector_database, embedding_client, settings)
graph_tools = TcmGraphTools(graph_repository, normalizer, recorder, settings, semantic_search_service)
fallback_router = ToolFallbackRouter(graph_tools)
csv_export_store = CsvExportStore()
compressor = ToolResultCompressor(settings)
graph_builder = AiGraphBuilder()
ai_chat_service = AiChatService(
    settings,
    graph_tools,
    fallback_router,
    recorder,
    graph_builder,
    csv_export_store,
    compressor,
    session_manager,
)
tcm_reason_chat_service = TcmReasonChatService(settings, session_manager)


@app.exception_handler(AiServiceError)
async def ai_service_error_handler(_, exc: AiServiceError) -> JSONResponse:
    return JSONResponse(status_code=exc.status_code, content={"code": exc.code, "message": exc.message})


@app.on_event("shutdown")
def shutdown() -> None:
    graph_repository.close()


@app.get("/health")
def health() -> dict[str, object]:
    return {
        "status": "UP",
        "service": settings.service_name,
        "framework": "fastapi-langchain",
        "neo4j": graph_repository.verify(),
        "postgres": postgres_database.ping() if settings.conversation_persistence_enabled else None,
        "vector": semantic_search_service.status().model_dump(mode="json"),
    }


@app.get("/actuator/health")
def actuator_health() -> dict[str, object]:
    neo4j_up = graph_repository.verify()
    return {
        "status": "UP" if neo4j_up else "DEGRADED",
        "components": {
            "neo4j": {"status": "UP" if neo4j_up else "DOWN"},
            "postgres": {
                "status": "UP" if (not settings.conversation_persistence_enabled or postgres_database.ping()) else "DOWN"
            },
            "pgvector": semantic_search_service.status().model_dump(mode="json"),
            "deepseek": {"status": "UNKNOWN", "details": {"model": settings.deepseek_chat_model}},
        },
    }


@app.get("/ai/conversations")
def list_conversations(
    x_user_id: str | None = Header(None, alias="X-User-Id"),
    mode: str = Query("academic"),
    page: int = Query(1),
    pageSize: int = Query(20),
) -> ConversationListResponse:
    items = conversation_repository.list_conversations(x_user_id, mode, page, pageSize)
    total = conversation_repository.count_conversations(x_user_id, mode)
    return ConversationListResponse(items=items, total=total)


@app.post("/ai/conversations")
def create_conversation(
    request: ConversationCreateRequest | None = None,
    x_user_id: str | None = Header(None, alias="X-User-Id"),
):
    return conversation_repository.create_conversation(x_user_id, request.mode if request else "academic", request.title if request else None)


@app.get("/ai/conversations/{conversation_id}/messages")
def conversation_messages(
    conversation_id: str,
    x_user_id: str | None = Header(None, alias="X-User-Id"),
    limit: int = Query(100),
) -> ConversationMessagesResponse:
    if conversation_repository.find_conversation(x_user_id, conversation_id) is None:
        raise HTTPException(status_code=404, detail="conversation not found")
    messages = conversation_repository.find_messages(x_user_id, conversation_id, limit)
    return ConversationMessagesResponse(conversationId=conversation_id, messages=messages)


@app.patch("/ai/conversations/{conversation_id}")
def update_conversation(
    conversation_id: str,
    request: ConversationUpdateRequest,
    x_user_id: str | None = Header(None, alias="X-User-Id"),
):
    if not conversation_repository.update_conversation_title(x_user_id, conversation_id, request.title):
        raise HTTPException(status_code=404, detail="conversation not found")
    found = conversation_repository.find_conversation(x_user_id, conversation_id)
    if found is None:
        raise HTTPException(status_code=404, detail="conversation not found")
    return found


@app.delete("/ai/conversations/{conversation_id}", status_code=204)
def delete_conversation(conversation_id: str, x_user_id: str | None = Header(None, alias="X-User-Id")) -> Response:
    if not conversation_repository.mark_conversation_deleted(x_user_id, conversation_id):
        raise HTTPException(status_code=404, detail="conversation not found")
    return Response(status_code=204)


@app.get("/ai/vector/status")
def vector_status():
    return semantic_search_service.status()


@app.get("/ai/vector/search")
def vector_search(
    query: str,
    types: str = Query("topic,target,pathway,disease,phenotype"),
    limit: int = Query(10),
):
    entity_types = [item.strip() for item in types.split(",") if item.strip()]
    return semantic_search_service.search(query, entity_types, limit)


@app.post("/ai/chat")
def chat(
    request: AiChatRequest,
    x_request_id: str | None = Header(None, alias="X-Request-Id"),
    x_user_id: str | None = Header(None, alias="X-User-Id"),
    x_user_name: str | None = Header(None, alias="X-User-Name"),
    x_user_account: str | None = Header(None, alias="X-User-Account"),
):
    context = RequestContext(x_request_id, x_user_id, x_user_name, x_user_account)
    return tcm_reason_chat_service.chat(request, context)


@app.post("/ai/aichat")
def aichat(
    request: AiChatRequest,
    x_request_id: str | None = Header(None, alias="X-Request-Id"),
    x_user_id: str | None = Header(None, alias="X-User-Id"),
    x_user_name: str | None = Header(None, alias="X-User-Name"),
    x_user_account: str | None = Header(None, alias="X-User-Account"),
):
    context = RequestContext(x_request_id, x_user_id, x_user_name, x_user_account)
    return ai_chat_service.chat(request, context)


@app.post("/ai/aichat/stream")
def aichat_stream(
    request: AiChatRequest,
    x_request_id: str | None = Header(None, alias="X-Request-Id"),
    x_user_id: str | None = Header(None, alias="X-User-Id"),
    x_user_name: str | None = Header(None, alias="X-User-Name"),
    x_user_account: str | None = Header(None, alias="X-User-Account"),
) -> StreamingResponse:
    context = RequestContext(x_request_id, x_user_id, x_user_name, x_user_account)

    def generate() -> Iterator[str]:
        yield _sse("start", {"provider": "deepseek"})
        try:
            response = ai_chat_service.chat(request, context)
            for chunk in _chunks(response.reply, 80):
                yield _sse("delta", {"text": chunk})
            yield _sse("metadata", response.model_dump(mode="json"))
            yield _sse("done", {})
        except AiServiceError as exc:
            yield _sse("error", {"code": exc.code, "message": exc.message})

    return StreamingResponse(generate(), media_type="text/event-stream")


@app.get("/ai/exports/{export_id}")
def export_csv(export_id: str, x_user_id: str | None = Header(None, alias="X-User-Id")) -> Response:
    record = csv_export_store.get(export_id, x_user_id)
    if record is None:
        return JSONResponse(status_code=404, content={"code": "EXPORT_NOT_FOUND", "message": "export not found or expired"})
    body = "\ufeff" + csv_export_store.render(record)
    headers = {"Content-Disposition": f'attachment; filename="{record.filename}"'}
    return Response(content=body, media_type="text/csv; charset=utf-8", headers=headers)


@app.post("/tcmseek/llm/chat")
def compatible_chat(
    request: AiChatRequest,
    x_request_id: str | None = Header(None, alias="X-Request-Id"),
    x_user_id: str | None = Header(None, alias="X-User-Id"),
    x_user_name: str | None = Header(None, alias="X-User-Name"),
    x_user_account: str | None = Header(None, alias="X-User-Account"),
) -> ApiResult:
    context = RequestContext(x_request_id, x_user_id, x_user_name, x_user_account)
    return ApiResult.success(tcm_reason_chat_service.chat(request, context))


@app.post("/tcmseek/llm/aichat")
def compatible_aichat(
    request: AiChatRequest,
    x_request_id: str | None = Header(None, alias="X-Request-Id"),
    x_user_id: str | None = Header(None, alias="X-User-Id"),
    x_user_name: str | None = Header(None, alias="X-User-Name"),
    x_user_account: str | None = Header(None, alias="X-User-Account"),
) -> ApiResult:
    context = RequestContext(x_request_id, x_user_id, x_user_name, x_user_account)
    return ApiResult.success(ai_chat_service.chat(request, context))


@app.get("/tcmseek/llm/aichat/exports/{export_id}")
def compatible_export_csv(export_id: str, x_user_id: str | None = Header(None, alias="X-User-Id")) -> Response:
    return export_csv(export_id, x_user_id)


def _sse(event: str, data: object) -> str:
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"


def _chunks(text: str, size: int) -> Iterator[str]:
    if not text:
        return
    for start in range(0, len(text), size):
        yield text[start : start + size]
