from __future__ import annotations

import json
import uuid
from dataclasses import dataclass
from typing import Any
from urllib.parse import parse_qs, urlparse

from app.config import Settings
from app.models import (
    AiMemoryDto,
    AiMessage,
    ConversationMessageDto,
    ConversationSummaryDto,
    ConversationSummaryState,
    ToolCallResult,
)


def compact_uuid() -> str:
    return uuid.uuid4().hex


class PostgresDatabase:
    def __init__(self, url: str, username: str, password: str) -> None:
        self._url = url
        self._username = username
        self._password = password

    def connect(self, *, autocommit: bool = True) -> Any:
        try:
            import psycopg
            from psycopg.rows import dict_row
        except ImportError as exc:
            raise RuntimeError("psycopg dependency is not installed") from exc
        kwargs = self._connection_kwargs()
        return psycopg.connect(**kwargs, autocommit=autocommit, row_factory=dict_row)

    def ping(self) -> bool:
        try:
            with self.connect() as conn:
                with conn.cursor() as cur:
                    cur.execute("SELECT 1 AS ok")
                    cur.fetchone()
            return True
        except Exception:
            return False

    def _connection_kwargs(self) -> dict[str, Any]:
        url = self._url.strip()
        if url.startswith("jdbc:"):
            url = url[len("jdbc:") :]
        parsed = urlparse(url)
        if parsed.scheme != "postgresql":
            raise ValueError(f"unsupported postgres url: {self._url}")
        params = {key: values[-1] for key, values in parse_qs(parsed.query).items() if values}
        kwargs: dict[str, Any] = {
            "host": parsed.hostname or "127.0.0.1",
            "port": parsed.port or 5432,
            "dbname": parsed.path.lstrip("/") or "postgres",
            "user": self._username,
            "password": self._password,
            "connect_timeout": int(params.pop("connectTimeout", params.pop("connect_timeout", 5))),
        }
        kwargs.update(params)
        return kwargs


@dataclass
class ConversationExchange:
    conversation_id: str
    user_id: str
    mode: str
    user_message: AiMessage
    assistant_message_id: str
    assistant_content: str
    provider: str | None = None
    model: str | None = None
    finish_reason: str | None = None
    total_results: int | None = None
    displayed_results: int | None = None
    csv_export_id: str | None = None
    request_id: str | None = None
    latency_ms: int | None = None
    tool_results: list[ToolCallResult] | None = None


class AiConversationRepository:
    def __init__(self, database: PostgresDatabase, settings: Settings) -> None:
        self._database = database
        self._settings = settings

    def enabled(self) -> bool:
        return self._settings.conversation_persistence_enabled

    def find_recent_messages(self, user_id: str | None, conversation_id: str | None, limit: int) -> list[AiMessage]:
        if not self.enabled() or not conversation_id or limit <= 0:
            return []
        sql = """
            select role, content
            from (
                select id, role, content, created_at
                from ai_message
                where user_id = %s
                  and conversation_id = %s
                order by created_at desc, id desc
                limit %s
            ) recent
            order by created_at asc, id asc
        """
        rows = self._query(sql, [self._owner(user_id), conversation_id, limit])
        return [AiMessage(role=row["role"], content=row["content"]) for row in rows]

    def count_messages(self, user_id: str | None, conversation_id: str | None) -> int:
        if not self.enabled() or not conversation_id:
            return 0
        rows = self._query(
            """
            select count(*) AS total
            from ai_message
            where user_id = %s
              and conversation_id = %s
            """,
            [self._owner(user_id), conversation_id],
        )
        return int(rows[0]["total"]) if rows else 0

    def find_summary(self, user_id: str | None, conversation_id: str | None) -> ConversationSummaryState | None:
        if not self.enabled() or not conversation_id:
            return None
        rows = self._query(
            """
            select conversation_id, user_id, summary, covered_message_count,
                   updated_at::text as updated_at
            from ai_conversation_summary
            where user_id = %s
              and conversation_id = %s
            """,
            [self._owner(user_id), conversation_id],
        )
        if not rows:
            return None
        row = rows[0]
        return ConversationSummaryState(
            conversationId=row["conversation_id"],
            userId=row["user_id"],
            summary=row["summary"],
            coveredMessageCount=row["covered_message_count"],
            updatedAt=row["updated_at"],
        )

    def upsert_summary(self, user_id: str | None, conversation_id: str, summary: str, covered_message_count: int) -> None:
        if not self.enabled() or not conversation_id or not summary.strip():
            return
        self._execute(
            """
            insert into ai_conversation_summary
                (conversation_id, user_id, summary, covered_message_count)
            values (%s, %s, %s, %s)
            on conflict (conversation_id) do update set
                summary = excluded.summary,
                covered_message_count = excluded.covered_message_count,
                updated_at = now()
            where ai_conversation_summary.user_id = excluded.user_id
            """,
            [conversation_id, self._owner(user_id), summary.strip(), max(0, covered_message_count)],
        )

    def find_memories(self, user_id: str | None, limit: int) -> list[AiMemoryDto]:
        if not self.enabled():
            return []
        rows = self._query(
            """
            select id, user_id, memory_type, content, confidence,
                   updated_at::text as updated_at
            from ai_memory
            where user_id = %s
              and enabled = true
            order by confidence desc, updated_at desc
            limit %s
            """,
            [self._owner(user_id), max(1, min(50, limit))],
        )
        return [
            AiMemoryDto(
                id=row["id"],
                userId=row["user_id"],
                memoryType=row["memory_type"],
                content=row["content"],
                confidence=float(row["confidence"] or 1.0),
                updatedAt=row["updated_at"],
            )
            for row in rows
        ]

    def upsert_memory(
        self,
        user_id: str | None,
        memory_type: str,
        content: str,
        source_conversation_id: str | None,
        confidence: float,
    ) -> None:
        owner = self._owner(user_id)
        safe_type = (memory_type or "").strip()
        safe_content = " ".join((content or "").split())[:1000]
        if not self.enabled() or not safe_type or not safe_content:
            return
        existing = self._query(
            """
            select id
            from ai_memory
            where user_id = %s
              and memory_type = %s
              and lower(content) = lower(%s)
            order by updated_at desc
            limit 1
            """,
            [owner, safe_type, safe_content],
        )
        safe_confidence = max(0.0, min(1.0, confidence))
        if existing:
            self._execute(
                """
                update ai_memory
                set source_conversation_id = coalesce(%s, source_conversation_id),
                    confidence = greatest(confidence, %s),
                    enabled = true,
                    updated_at = now()
                where id = %s
                """,
                [source_conversation_id, safe_confidence, existing[0]["id"]],
            )
        else:
            self._execute(
                """
                insert into ai_memory
                    (id, user_id, memory_type, content, source_conversation_id, confidence, enabled)
                values (%s, %s, %s, %s, %s, %s, true)
                """,
                [compact_uuid(), owner, safe_type, safe_content, source_conversation_id, safe_confidence],
            )

    def create_conversation(self, user_id: str | None, mode: str | None, title: str | None) -> ConversationSummaryDto:
        owner = self._owner(user_id)
        normalized_mode = self._mode(mode)
        safe_title = (title or "新对话").strip()[:200] or "新对话"
        reusable = self.find_reusable_empty_conversation(owner, normalized_mode, safe_title)
        if reusable is not None:
            return reusable
        conversation_id = f"{normalized_mode}-{compact_uuid()}"
        self._execute(
            """
            insert into ai_conversation (id, user_id, title, mode, status, message_count, last_message_at)
            values (%s, %s, %s, %s, 'active', 0, null)
            """,
            [conversation_id, owner, safe_title, normalized_mode],
        )
        found = self.find_conversation(owner, conversation_id)
        if found is None:
            raise RuntimeError("failed to create conversation")
        return found

    def find_reusable_empty_conversation(self, user_id: str, mode: str | None, title: str | None) -> ConversationSummaryDto | None:
        if title and title.strip() not in {"", "新对话", "新会话"}:
            return None
        rows = self._query(
            """
            select id, title, mode, status, message_count,
                   last_message_at::text as last_message_at,
                   updated_at::text as updated_at
            from ai_conversation
            where user_id = %s
              and mode = %s
              and status = 'active'
              and message_count = 0
              and last_message_at is null
              and (title is null or title = '' or title in ('新对话', '新会话'))
            order by updated_at desc, id desc
            limit 1
            """,
            [self._owner(user_id), self._mode(mode)],
        )
        return self._conversation(rows[0]) if rows else None

    def find_conversation(self, user_id: str | None, conversation_id: str) -> ConversationSummaryDto | None:
        rows = self._query(
            """
            select id, title, mode, status, message_count,
                   last_message_at::text as last_message_at,
                   updated_at::text as updated_at
            from ai_conversation
            where user_id = %s
              and id = %s
              and status <> 'deleted'
            """,
            [self._owner(user_id), conversation_id],
        )
        return self._conversation(rows[0]) if rows else None

    def list_conversations(self, user_id: str | None, mode: str | None, page: int, page_size: int) -> list[ConversationSummaryDto]:
        safe_page = max(1, page)
        safe_page_size = max(1, min(100, page_size))
        rows = self._query(
            """
            select id, title, mode, status, message_count,
                   last_message_at::text as last_message_at,
                   updated_at::text as updated_at
            from ai_conversation
            where user_id = %s
              and mode = %s
              and status = 'active'
            order by updated_at desc, id desc
            limit %s offset %s
            """,
            [self._owner(user_id), self._mode(mode), safe_page_size, (safe_page - 1) * safe_page_size],
        )
        return [self._conversation(row) for row in rows]

    def count_conversations(self, user_id: str | None, mode: str | None) -> int:
        rows = self._query(
            """
            select count(*) AS total
            from ai_conversation
            where user_id = %s
              and mode = %s
              and status = 'active'
            """,
            [self._owner(user_id), self._mode(mode)],
        )
        return int(rows[0]["total"]) if rows else 0

    def find_messages(self, user_id: str | None, conversation_id: str, limit: int) -> list[ConversationMessageDto]:
        rows = self._query(
            """
            select id, role, content, provider, model, finish_reason,
                   total_results, displayed_results, csv_export_id,
                   request_id, latency_ms, created_at::text as created_at
            from (
                select id, role, content, provider, model, finish_reason,
                       total_results, displayed_results, csv_export_id,
                       request_id, latency_ms, created_at
                from ai_message
                where user_id = %s
                  and conversation_id = %s
                order by created_at desc, id desc
                limit %s
            ) recent
            order by created_at asc, id asc
            """,
            [self._owner(user_id), conversation_id, max(1, min(300, limit))],
        )
        return [
            ConversationMessageDto(
                id=row["id"],
                role=row["role"],
                content=row["content"],
                provider=row["provider"],
                model=row["model"],
                finishReason=row["finish_reason"],
                totalResults=row["total_results"],
                displayedResults=row["displayed_results"],
                csvExportId=row["csv_export_id"],
                requestId=row["request_id"],
                latencyMs=row["latency_ms"],
                createdAt=row["created_at"],
            )
            for row in rows
        ]

    def update_conversation_title(self, user_id: str | None, conversation_id: str, title: str | None) -> bool:
        safe_title = (title or "").strip()
        if not safe_title:
            return False
        return self._execute(
            """
            update ai_conversation
            set title = %s, updated_at = now()
            where user_id = %s
              and id = %s
              and status <> 'deleted'
            """,
            [safe_title[:200], self._owner(user_id), conversation_id],
        ) > 0

    def mark_conversation_deleted(self, user_id: str | None, conversation_id: str) -> bool:
        return self._execute(
            """
            update ai_conversation
            set status = 'deleted', updated_at = now()
            where user_id = %s
              and id = %s
              and status <> 'deleted'
            """,
            [self._owner(user_id), conversation_id],
        ) > 0

    def save_exchange(self, exchange: ConversationExchange) -> None:
        if not self.enabled() or not exchange.conversation_id or not exchange.user_message.content or not exchange.assistant_content:
            return
        try:
            from psycopg.types.json import Json
        except ImportError as exc:
            raise RuntimeError("psycopg dependency is not installed") from exc
        title = self._title_from(exchange.user_message.content)
        with self._database.connect(autocommit=False) as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    insert into ai_conversation (id, user_id, title, mode, status, message_count, last_message_at)
                    values (%s, %s, %s, %s, 'active', 0, now())
                    on conflict (id) do update set
                        mode = excluded.mode,
                        title = coalesce(ai_conversation.title, excluded.title),
                        last_message_at = now(),
                        updated_at = now()
                    where ai_conversation.user_id = excluded.user_id
                      and ai_conversation.status <> 'deleted'
                    """,
                    [exchange.conversation_id, exchange.user_id, title, self._mode(exchange.mode)],
                )
                user_message_id = compact_uuid()
                assistant_message_id = exchange.assistant_message_id or compact_uuid()
                cur.execute(
                    """
                    insert into ai_message
                        (id, conversation_id, user_id, role, content, request_id, latency_ms, created_at)
                    values (%s, %s, %s, 'user', %s, %s, %s, clock_timestamp())
                    """,
                    [user_message_id, exchange.conversation_id, exchange.user_id, exchange.user_message.content, exchange.request_id, None],
                )
                cur.execute(
                    """
                    insert into ai_message
                        (id, conversation_id, user_id, role, content, provider, model, finish_reason,
                         total_results, displayed_results, csv_export_id, request_id, latency_ms, created_at)
                    values (%s, %s, %s, 'assistant', %s, %s, %s, %s, %s, %s, %s, %s, %s, clock_timestamp())
                    """,
                    [
                        assistant_message_id,
                        exchange.conversation_id,
                        exchange.user_id,
                        exchange.assistant_content,
                        exchange.provider,
                        exchange.model,
                        exchange.finish_reason,
                        exchange.total_results,
                        exchange.displayed_results,
                        exchange.csv_export_id,
                        exchange.request_id,
                        exchange.latency_ms,
                    ],
                )
                for tool_result in exchange.tool_results or []:
                    result = tool_result.result
                    cur.execute(
                        """
                        insert into ai_tool_call
                            (id, message_id, conversation_id, user_id, tool_name, arguments,
                             result_total, result_preview, success, latency_ms)
                        values (%s, %s, %s, %s, %s, %s, %s, %s, true, %s)
                        """,
                        [
                            compact_uuid(),
                            assistant_message_id,
                            exchange.conversation_id,
                            exchange.user_id,
                            tool_result.toolName,
                            Json(tool_result.arguments or {}),
                            result.total if result else None,
                            Json(result.model_dump(mode="json") if result else {}),
                            exchange.latency_ms,
                        ],
                    )
                cur.execute(
                    """
                    update ai_conversation
                    set message_count = (
                            select count(*)
                            from ai_message
                            where conversation_id = %s
                              and user_id = %s
                        ),
                        title = case
                            when title is null or title = '' or title = '新对话' then %s
                            else title
                        end,
                        last_message_at = now(),
                        updated_at = now()
                    where id = %s
                      and user_id = %s
                    """,
                    [exchange.conversation_id, exchange.user_id, title, exchange.conversation_id, exchange.user_id],
                )
            conn.commit()

    def _query(self, sql: str, params: list[Any]) -> list[dict[str, Any]]:
        with self._database.connect() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params)
                return list(cur.fetchall())

    def _execute(self, sql: str, params: list[Any]) -> int:
        with self._database.connect() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params)
                return cur.rowcount

    def _conversation(self, row: dict[str, Any]) -> ConversationSummaryDto:
        return ConversationSummaryDto(
            id=row["id"],
            title=row["title"],
            mode=row["mode"],
            status=row["status"],
            messageCount=row["message_count"],
            lastMessageAt=row["last_message_at"],
            updatedAt=row["updated_at"],
        )

    def _mode(self, mode: str | None) -> str:
        return "general" if (mode or "").lower() == "general" else "academic"

    def _owner(self, user_id: str | None) -> str:
        return user_id if user_id and user_id.strip() else "anonymous"

    def _title_from(self, content: str) -> str:
        title = " ".join((content or "").split()).strip() or "新会话"
        return title[:80]


def to_json_text(value: Any) -> str:
    return json.dumps(value if value is not None else {}, ensure_ascii=False)
