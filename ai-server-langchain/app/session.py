from __future__ import annotations

import json
import re
import threading
import time
from typing import Any

from app.config import Settings
from app.models import AiChatResponse, AiMemoryDto, AiMessage, ToolCallResult
from app.postgres import AiConversationRepository, ConversationExchange
from app.services_types import RequestContextLike


class AiConversationMemoryService:
    MEMORY_TYPES = {"preference", "research_topic", "entity_focus", "constraint"}

    def __init__(self, repository: AiConversationRepository, settings: Settings) -> None:
        self._repository = repository
        self._settings = settings
        self._llm: Any | None = None

    def enrich_context(self, user_id: str | None, conversation_id: str, recent_messages: list[AiMessage]) -> list[AiMessage]:
        if not self._settings.conversation_persistence_enabled:
            return recent_messages
        parts: list[str] = []
        try:
            if self._settings.memory_enabled:
                memories = self._repository.find_memories(user_id, self._memory_limit())
                if memories:
                    parts.append(
                        "用户长期记忆：\n"
                        + "\n".join(f"- {self._memory_label(memory.memoryType)}：{memory.content}" for memory in memories)
                    )
            if self._settings.conversation_summary_enabled:
                summary = self._repository.find_summary(user_id, conversation_id)
                if summary and summary.summary.strip():
                    parts.append("当前会话摘要：\n" + summary.summary)
        except Exception:
            return recent_messages
        if not parts:
            return recent_messages
        context = (
            "以下上下文仅用于理解用户意图，不要向用户直接复述，也不要编造其中没有的信息。\n\n"
            + "\n\n".join(parts)
        )
        return [AiMessage(role="system", content=context), *recent_messages]

    def refresh_after_exchange_async(self, conversation_id: str, user_id: str | None) -> None:
        if not self._settings.conversation_persistence_enabled:
            return
        if not self._settings.conversation_summary_enabled and not self._settings.memory_enabled:
            return
        thread = threading.Thread(target=self._refresh_after_exchange_safe, args=(conversation_id, user_id), daemon=True)
        thread.start()

    def _refresh_after_exchange_safe(self, conversation_id: str, user_id: str | None) -> None:
        try:
            self.refresh_after_exchange(conversation_id, user_id)
        except Exception:
            return

    def refresh_after_exchange(self, conversation_id: str, user_id: str | None) -> None:
        message_count = self._repository.count_messages(user_id, conversation_id)
        current_summary = self._repository.find_summary(user_id, conversation_id)
        if not self._should_refresh(message_count, current_summary.coveredMessageCount if current_summary else 0):
            return
        source_messages = self._repository.find_recent_messages(
            user_id,
            conversation_id,
            max(6, self._settings.conversation_summary_source_message_limit),
        )
        if not source_messages:
            return
        if self._settings.conversation_summary_enabled:
            summary = self._generate_summary(current_summary.summary if current_summary else "", source_messages, message_count)
            if summary:
                self._repository.upsert_summary(user_id, conversation_id, summary, message_count)
        if self._settings.memory_enabled:
            source = source_messages[-max(4, self._settings.memory_source_message_limit) :]
            for memory in self._extract_memories(source)[: self._memory_limit()]:
                self._repository.upsert_memory(user_id, memory.memoryType, memory.content, conversation_id, memory.confidence)

    def _should_refresh(self, message_count: int, covered_count: int) -> bool:
        trigger = max(2, self._settings.conversation_summary_trigger_message_count)
        every = max(1, self._settings.conversation_summary_refresh_message_count)
        return message_count >= trigger and message_count - covered_count >= every

    def _generate_summary(self, previous_summary: str, source_messages: list[AiMessage], message_count: int) -> str:
        prompt = f"""
你要为 TCMSeek 的单个 AI 会话生成可复用摘要。

要求：
- 只总结用户目标、已确认实体、已得出的关键结论、仍待解决的问题。
- 不保留完整表格、完整 CSV、长 ID 列表和工具原始结果。
- 不要写函数名、工具名、接口名。
- 不要把一次性闲聊当作长期事实。
- 输出中文，控制在 800 字以内。

已有摘要：
{previous_summary or "无"}

最近消息：
{self._format_messages(source_messages)}

当前会话总消息数：{message_count}
"""
        return self._limit_text(self._call_model(prompt), 1200)

    def _extract_memories(self, source_messages: list[AiMessage]) -> list[AiMemoryDto]:
        prompt = f"""
你要从 TCMSeek 用户与 AI 的最近对话中提取“长期用户记忆”。

只允许提取这些类型：
- preference：用户明确表达的回答偏好
- research_topic：用户持续关注的研究主题
- entity_focus：用户反复关注的中药、方剂、疾病、靶点、化合物
- constraint：用户明确提出的长期约束

保守规则：
- 一次性问题不要写入记忆。
- 医疗结论、治疗建议、工具查询结果不要写成用户长期记忆。
- 不要保存敏感隐私。
- 不要输出解释文字，只输出 JSON 数组。
- 最多输出 {self._memory_limit()} 条。

JSON 格式：
[
  {{"memoryType":"preference","content":"用户偏好中文简洁回答","confidence":0.8}}
]

最近消息：
{self._format_messages(source_messages)}
"""
        raw = self._call_model(prompt)
        return self._parse_memories(raw)

    def _call_model(self, prompt: str) -> str:
        if not self._settings.deepseek_api_key:
            return ""
        if self._llm is None:
            from langchain_openai import ChatOpenAI

            self._llm = ChatOpenAI(
                model=self._settings.deepseek_chat_model,
                api_key=self._settings.deepseek_api_key,
                base_url=self._settings.deepseek_base_url,
                temperature=0.1,
                timeout=self._settings.request_timeout,
                max_retries=self._settings.max_retries,
            )
        message = self._llm.invoke([("user", prompt)])
        return str(getattr(message, "content", "") or "").strip()

    def _parse_memories(self, raw: str) -> list[AiMemoryDto]:
        if not raw:
            return []
        match = re.search(r"\[[\s\S]*\]", raw)
        if not match:
            return []
        try:
            data = json.loads(match.group(0))
        except json.JSONDecodeError:
            return []
        memories: list[AiMemoryDto] = []
        for item in data if isinstance(data, list) else []:
            memory_type = str(item.get("memoryType", "")).strip().lower()
            content = self._limit_text(str(item.get("content", "")).strip(), 300)
            if memory_type in self.MEMORY_TYPES and content:
                confidence = item.get("confidence", 0.65)
                try:
                    safe_confidence = float(confidence)
                except (TypeError, ValueError):
                    safe_confidence = 0.65
                memories.append(AiMemoryDto(memoryType=memory_type, content=content, confidence=safe_confidence))
        return memories

    def _format_messages(self, messages: list[AiMessage]) -> str:
        if not messages:
            return "无"
        return "\n".join(f"{(message.role or 'user').lower()}: {message.content}" for message in messages if message.content)

    def _memory_label(self, memory_type: str) -> str:
        return {
            "preference": "偏好",
            "research_topic": "研究主题",
            "entity_focus": "关注实体",
            "constraint": "约束",
        }.get(memory_type, "记忆")

    def _memory_limit(self) -> int:
        return max(1, self._settings.memory_max_items)

    def _limit_text(self, value: str, max_length: int) -> str:
        value = (value or "").strip()
        return value if len(value) <= max_length else value[:max_length]


class AiSessionManager:
    MAX_SESSIONS = 5000
    SESSION_TTL_SECONDS = 60 * 60

    def __init__(
        self,
        repository: AiConversationRepository,
        memory_service: AiConversationMemoryService,
        settings: Settings,
    ) -> None:
        self._repository = repository
        self._memory_service = memory_service
        self._settings = settings
        self._sessions: dict[str, tuple[float, list[AiMessage]]] = {}

    def build_context(self, session_id: str | None, incoming: list[AiMessage], context: RequestContextLike | None) -> list[AiMessage]:
        user_id = getattr(context, "user_id", None) if context else None
        conversation_id = self._conversation_key(session_id, user_id)
        state_key = self._state_key(user_id, conversation_id)
        incoming_messages = self._sanitize(incoming)
        stored_messages = self._load_stored_messages(user_id, conversation_id)
        if not stored_messages:
            stored_messages = self._local_messages(state_key)
        merged = self._merge_context(stored_messages, incoming_messages)
        trimmed = self._trim(merged)
        return self._memory_service.enrich_context(user_id, conversation_id, trimmed)

    def append_exchange(
        self,
        session_id: str | None,
        incoming: list[AiMessage],
        reply: str,
        context: RequestContextLike | None,
        response: AiChatResponse,
        tool_results: list[ToolCallResult],
        latency_ms: int,
    ) -> None:
        latest_user = self._latest_user_message(incoming)
        if latest_user is None or not reply:
            return
        user_id = getattr(context, "user_id", None) if context else None
        request_id = getattr(context, "request_id", None) if context else None
        owner = user_id if user_id and user_id.strip() else "anonymous"
        conversation_id = self._conversation_key(session_id, user_id)
        state_key = self._state_key(user_id, conversation_id)
        self._update_local_state(state_key, latest_user, reply)
        if self._settings.conversation_persistence_enabled:
            try:
                self._repository.save_exchange(
                    ConversationExchange(
                        conversation_id=conversation_id,
                        user_id=owner,
                        mode=self._mode_from_conversation_id(conversation_id),
                        user_message=latest_user,
                        assistant_message_id=response.id or "",
                        assistant_content=reply,
                        provider=response.provider,
                        model=response.model,
                        finish_reason=response.finishReason,
                        total_results=response.totalResults,
                        displayed_results=response.displayedResults,
                        csv_export_id=self._export_id_from_url(response.csvDownloadUrl),
                        request_id=request_id,
                        latency_ms=max(0, min(2_147_483_647, int(latency_ms))),
                        tool_results=tool_results,
                    )
                )
                self._memory_service.refresh_after_exchange_async(conversation_id, user_id)
            except Exception:
                pass
        self._cleanup()

    def _load_stored_messages(self, user_id: str | None, conversation_id: str) -> list[AiMessage]:
        if not self._settings.conversation_persistence_enabled:
            return []
        try:
            return self._repository.find_recent_messages(user_id, conversation_id, self._context_limit())
        except Exception:
            return []

    def _merge_context(self, stored: list[AiMessage], incoming: list[AiMessage]) -> list[AiMessage]:
        if not stored:
            return list(incoming)
        if not incoming:
            return list(stored)
        fingerprint = self._fingerprint(stored[-1])
        matched_index = next((i for i in range(len(incoming) - 1, -1, -1) if self._fingerprint(incoming[i]) == fingerprint), -1)
        if matched_index >= 0:
            return [*stored, *incoming[matched_index + 1 :]]
        if len(incoming) == 1:
            return [*stored, *incoming]
        return list(incoming)

    def _local_messages(self, state_key: str) -> list[AiMessage]:
        state = self._sessions.get(state_key)
        if state is None:
            return []
        self._sessions[state_key] = (time.time(), state[1])
        return list(state[1])

    def _update_local_state(self, state_key: str, user_message: AiMessage, reply: str) -> None:
        messages = self._local_messages(state_key)
        messages.append(AiMessage(role=user_message.role or "user", content=user_message.content))
        messages.append(AiMessage(role="assistant", content=reply))
        self._sessions[state_key] = (time.time(), self._trim(messages))

    def _sanitize(self, messages: list[AiMessage] | None) -> list[AiMessage]:
        return [AiMessage(role=message.role or "user", content=message.content) for message in messages or [] if message and message.content]

    def _latest_user_message(self, messages: list[AiMessage] | None) -> AiMessage | None:
        for message in reversed(messages or []):
            if message.content and (message.role or "user").lower() == "user":
                return AiMessage(role=message.role or "user", content=message.content)
        return None

    def _trim(self, messages: list[AiMessage]) -> list[AiMessage]:
        limit = self._context_limit()
        return messages[-limit:] if len(messages) > limit else messages

    def _context_limit(self) -> int:
        return max(1, self._settings.conversation_context_message_limit)

    def _conversation_key(self, session_id: str | None, user_id: str | None) -> str:
        if session_id and session_id.strip():
            return session_id
        return f"default-{user_id}" if user_id and user_id.strip() else "default"

    def _state_key(self, user_id: str | None, conversation_id: str) -> str:
        owner = user_id if user_id and user_id.strip() else "anonymous"
        return f"{owner}:{conversation_id}"

    def _mode_from_conversation_id(self, conversation_id: str) -> str:
        return "general" if conversation_id.lower().startswith("general") else "academic"

    def _export_id_from_url(self, csv_download_url: str | None) -> str | None:
        if not csv_download_url:
            return None
        return csv_download_url.rsplit("/", 1)[-1]

    def _fingerprint(self, message: AiMessage) -> str:
        return f"{(message.role or 'user').lower()}\n{message.content or ''}"

    def _cleanup(self) -> None:
        now = time.time()
        self._sessions = {key: state for key, state in self._sessions.items() if now - state[0] <= self.SESSION_TTL_SECONDS}
        if len(self._sessions) <= self.MAX_SESSIONS:
            return
        victims = sorted(self._sessions.items(), key=lambda item: item[1][0])[: len(self._sessions) - self.MAX_SESSIONS]
        for key, _ in victims:
            self._sessions.pop(key, None)
