from __future__ import annotations

import json
import re
import ssl
import time
import urllib.error
import urllib.request
import uuid
from dataclasses import dataclass
from typing import Any

from app.compressor import ToolResultCompressor
from app.config import Settings
from app.csv_store import CsvExportStore
from app.fallback import ToolFallbackRouter
from app.graph_builder import AiGraphBuilder
from app.models import AiChatRequest, AiChatResponse, AiMessage, ToolCallResult, Usage
from app.prompts import SUMMARY_PROMPT_TEMPLATE, SYSTEM_PROMPT, TCM_REASON_SYSTEM_PROMPT
from app.recorder import ToolExecutionRecorder
from app.tools import TcmGraphTools


@dataclass(frozen=True)
class RequestContext:
    request_id: str | None = None
    user_id: str | None = None
    username: str | None = None
    account: str | None = None


class AiServiceError(RuntimeError):
    def __init__(self, status_code: int, code: str, message: str, cause: Exception | None = None) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.code = code
        self.message = message
        self.__cause__ = cause


class AiChatService:
    FALLBACK_FIELD_PRIORITY = [
        "herb",
        "herbName",
        "prescription",
        "prescriptionName",
        "disease",
        "compound",
        "target",
        "formula",
        "symptom",
        "syndrome",
        "pathway",
        "evidenceType",
        "inchikey",
    ]

    def __init__(
        self,
        settings: Settings,
        graph_tools: TcmGraphTools,
        fallback_router: ToolFallbackRouter,
        recorder: ToolExecutionRecorder,
        graph_builder: AiGraphBuilder,
        csv_export_store: CsvExportStore,
        compressor: ToolResultCompressor,
        session_manager: Any | None = None,
    ) -> None:
        self._settings = settings
        self._graph_tools = graph_tools
        self._fallback_router = fallback_router
        self._recorder = recorder
        self._graph_builder = graph_builder
        self._csv_export_store = csv_export_store
        self._compressor = compressor
        self._session_manager = session_manager
        self._agent: Any | None = None
        self._llm: Any | None = None

    def chat(self, request: AiChatRequest, context: RequestContext | None = None) -> AiChatResponse:
        request_context = context or RequestContext()
        started_at = time.time()
        message_context = (
            self._session_manager.build_context(request.sessionId, request.messages, request_context)
            if self._session_manager is not None
            else request.messages
        )
        user_prompt = self._build_user_prompt(message_context)
        latest_question = self._latest_question(request)

        try:
            fallback_results = self._run_fallback(latest_question)
        except Exception as exc:
            raise self._downstream_exception(exc) from exc
        if self._has_useful_tool_observation(fallback_results) or fallback_results:
            response = self._tool_response(
                latest_question=latest_question,
                tool_results=fallback_results,
                finish_reason="tool_observed",
                request_context=request_context,
                reasoning_trace=f"fallback_tool costMs={int((time.time() - started_at) * 1000)}",
            )
            self._append_exchange(request, request_context, response, response.toolResults, started_at)
            return response

        self._recorder.start()
        try:
            reply = self._call_agent(user_prompt)
        except Exception as exc:
            self._recorder.finish()
            raise self._downstream_exception(exc) from exc
        model_tool_results = self._recorder.finish()

        if self._has_useful_tool_observation(model_tool_results) or model_tool_results:
            response = self._tool_response(
                latest_question=latest_question,
                tool_results=model_tool_results,
                finish_reason="tool_observed",
                request_context=request_context,
                reasoning_trace=f"model_tool costMs={int((time.time() - started_at) * 1000)}",
            )
            self._append_exchange(request, request_context, response, response.toolResults, started_at)
            return response

        reply = self._limit_reply_length(self._sanitize_user_facing_reply(reply), None)
        response = AiChatResponse(
            id=str(uuid.uuid4()),
            reply=reply,
            provider="deepseek",
            model=self._settings.deepseek_chat_model,
            finishReason="model_answer",
            reasoningTrace=f"model_only costMs={int((time.time() - started_at) * 1000)}",
        )
        self._append_exchange(request, request_context, response, [], started_at)
        return response

    def _run_fallback(self, latest_question: str) -> list[ToolCallResult]:
        self._recorder.start()
        try:
            self._fallback_router.try_execute(latest_question)
        finally:
            results = self._recorder.finish()
        return results

    def _tool_response(
        self,
        latest_question: str,
        tool_results: list[ToolCallResult],
        finish_reason: str,
        request_context: RequestContext,
        reasoning_trace: str | None = None,
    ) -> AiChatResponse:
        response_tool_results = self._compressor.for_answer(tool_results)
        export_url = self._build_csv_download_url(response_tool_results, request_context)
        if not self._has_any_tool_data(response_tool_results):
            reply = self._no_tool_result_reply()
            final_finish_reason = "tool_no_result"
        else:
            try:
                reply = self._summarize_tool_results(latest_question, response_tool_results, export_url)
                final_finish_reason = "tool_summarized"
            except Exception:
                reply = self._fallback_tool_reply(response_tool_results, export_url)
                final_finish_reason = "tool_summary_fallback"
        reply = self._limit_reply_length(self._sanitize_user_facing_reply(reply), export_url)
        response = AiChatResponse(
            id=str(uuid.uuid4()),
            reply=reply,
            provider="deepseek",
            model=self._settings.deepseek_chat_model,
            finishReason=final_finish_reason or finish_reason,
            toolResults=response_tool_results,
            reasoningTrace=reasoning_trace,
        )
        graph = self._graph_builder.build(response_tool_results)
        if graph is not None:
            response.graph = graph
        total = self._compressor.first_total(response_tool_results)
        displayed = self._compressor.first_displayed(response_tool_results)
        response.totalResults = total if total else None
        response.displayedResults = displayed if displayed else None
        if export_url:
            response.csvDownloadUrl = export_url
        return response

    def _get_llm(self) -> Any:
        if self._llm is None:
            if not self._settings.deepseek_api_key:
                raise AiServiceError(503, "AI_PROVIDER_NOT_CONFIGURED", "DeepSeek API Key 未配置，请设置 DEEPSEEK_API_KEY。")
            try:
                from langchain_openai import ChatOpenAI
            except ImportError as exc:
                raise AiServiceError(503, "LANGCHAIN_NOT_INSTALLED", "LangChain 依赖未安装，请先安装 requirements.txt。", exc) from exc
            self._llm = ChatOpenAI(
                model=self._settings.deepseek_chat_model,
                api_key=self._settings.deepseek_api_key,
                base_url=self._settings.deepseek_base_url,
                temperature=self._settings.deepseek_temperature,
                max_tokens=self._settings.deepseek_max_tokens,
                timeout=self._settings.request_timeout,
                max_retries=self._settings.max_retries,
            )
        return self._llm

    def _get_agent(self) -> Any:
        if self._agent is None:
            try:
                from langchain.agents import create_agent
            except ImportError as exc:
                raise AiServiceError(503, "LANGCHAIN_NOT_INSTALLED", "LangChain 依赖未安装，请先安装 requirements.txt。", exc) from exc
            self._agent = create_agent(
                model=self._get_llm(),
                tools=self._graph_tools.as_langchain_tools(),
                system_prompt=SYSTEM_PROMPT,
            )
        return self._agent

    def _call_agent(self, user_prompt: str) -> str:
        result = self._get_agent().invoke(
            {"messages": [{"role": "user", "content": user_prompt}]},
            config={"recursion_limit": 8},
        )
        return self._extract_langchain_content(result)

    def _summarize_tool_results(self, question: str, tool_results: list[ToolCallResult], csv_download_url: str | None) -> str:
        payload = json.dumps([item.model_dump(mode="json") for item in tool_results], ensure_ascii=False)
        prompt = SUMMARY_PROMPT_TEMPLATE.format(
            question=question or "",
            toolResultJson=payload,
            csvDownloadUrl=csv_download_url or "无",
            answerItemLimit=self._compressor.answer_item_limit,
        )
        result = self._get_llm().invoke([("system", SYSTEM_PROMPT), ("user", prompt)])
        return self._extract_message_content(result)

    def _extract_langchain_content(self, result: Any) -> str:
        if isinstance(result, dict):
            messages = result.get("messages")
            if messages:
                return self._extract_message_content(messages[-1])
            if "output" in result:
                return self._content_to_text(result["output"])
        return self._extract_message_content(result)

    def _extract_message_content(self, message: Any) -> str:
        content = getattr(message, "content", None)
        if content is None and isinstance(message, dict):
            content = message.get("content")
        return self._content_to_text(content)

    def _content_to_text(self, content: Any) -> str:
        if content is None:
            return ""
        if isinstance(content, str):
            return content.strip()
        if isinstance(content, list):
            parts: list[str] = []
            for item in content:
                if isinstance(item, dict):
                    text = item.get("text") or item.get("content") or ""
                    if text:
                        parts.append(str(text))
                else:
                    parts.append(str(item))
            return "".join(parts).strip()
        return str(content).strip()

    def _fallback_tool_reply(self, tool_results: list[ToolCallResult], csv_download_url: str | None) -> str:
        visible_result = self._first_result_with_data(tool_results)
        if visible_result is None:
            return "知识图谱查询已完成，但摘要生成暂时不可用。\n\n核心结论：知识图谱未查询到相关数据。"
        items = visible_result.result.items
        displayed = min(len(items), self._compressor.answer_item_limit)
        total = max(visible_result.result.total, len(items))
        reply = [f"核心结论：本次查询共返回 {total} 条相关结果。"]
        if total > displayed:
            reply[0] += f" 下方仅展示前 {displayed} 条代表性结果。"
        reply.append("\n代表性结果：")
        for index, item in enumerate(items[:displayed], start=1):
            reply.append(f"{index}. {self._format_fallback_item(item)}")
        if csv_download_url:
            reply.append("\n完整明细可点击下载完整结果。")
        return "\n".join(reply)

    def _format_fallback_item(self, item: dict[str, Any]) -> str:
        preferred: list[str] = []
        for field in self.FALLBACK_FIELD_PRIORITY:
            if field in item and item[field] not in (None, ""):
                preferred.append(f"{self._fallback_field_label(field)}：{item[field]}")
            if len(preferred) >= 3:
                break
        if preferred:
            return "；".join(preferred)
        generic = [f"{key}：{value}" for key, value in item.items() if value not in (None, "") and not key.lower().endswith("id")]
        return "；".join(generic[:3]) if generic else "结果项"

    def _fallback_field_label(self, field: str) -> str:
        labels = {
            "herb": "中药",
            "herbName": "中药",
            "prescription": "方剂",
            "prescriptionName": "方剂",
            "disease": "疾病",
            "compound": "化合物",
            "target": "靶点",
            "formula": "分子式",
            "symptom": "症状",
            "syndrome": "证候",
            "pathway": "通路",
            "evidenceType": "证据类型",
            "inchikey": "InChIKey",
        }
        return labels.get(field, field)

    def _first_result_with_data(self, tool_results: list[ToolCallResult]) -> ToolCallResult | None:
        for tool_result in tool_results:
            if tool_result.result.items:
                return tool_result
        return None

    def _no_tool_result_reply(self) -> str:
        return "知识图谱未查询到相关数据。\n\n可以尝试使用更规范的中药、方剂、疾病或化合物名称，或减少查询实体后重试。"

    def _build_csv_download_url(self, tool_results: list[ToolCallResult], context: RequestContext) -> str | None:
        export_id = self._csv_export_store.save_first_export(tool_results, context.user_id)
        return f"/ai/exports/{export_id}" if export_id else None

    def _append_exchange(
        self,
        request: AiChatRequest,
        context: RequestContext,
        response: AiChatResponse,
        tool_results: list[ToolCallResult],
        started_at: float,
    ) -> None:
        if self._session_manager is None:
            return
        self._session_manager.append_exchange(
            request.sessionId,
            request.messages,
            response.reply,
            context,
            response,
            tool_results,
            int((time.time() - started_at) * 1000),
        )

    def _sanitize_user_facing_reply(self, reply: str) -> str:
        if not reply:
            return reply
        sanitized = re.sub(r"[（(]\s*`?[a-z]+(?:[A-Z][A-Za-z0-9]*)+`?\s*[)）]", "", reply)
        sanitized = re.sub(r"`[a-z]+(?:[A-Z][A-Za-z0-9]*)+`", "知识图谱查询", sanitized)
        sanitized = re.sub(r"(?i)CSV[:：]\s*/(?:api/)?ai/exports/[a-zA-Z0-9_-]+", "完整明细可点击下载完整结果", sanitized)
        sanitized = re.sub(r"/(?:api/)?ai/exports/[a-zA-Z0-9_-]+", "完整明细可点击下载完整结果", sanitized)
        return sanitized

    def _limit_reply_length(self, reply: str, csv_download_url: str | None) -> str:
        max_chars = self._settings.max_reply_chars
        if not reply or max_chars <= 0 or len(reply) <= max_chars:
            return reply
        suffix = "\n\n回答已压缩，完整明细请通过 CSV 下载。"
        if csv_download_url and "下载" not in suffix:
            suffix += " 完整明细可点击下载完整结果。"
        return reply[: max(0, max_chars - len(suffix))].strip() + suffix

    def _has_any_tool_data(self, tool_results: list[ToolCallResult]) -> bool:
        return any(tool_result.result.items for tool_result in tool_results)

    def _has_useful_tool_observation(self, tool_results: list[ToolCallResult]) -> bool:
        for tool_result in tool_results:
            query_type = tool_result.result.queryType
            if query_type.endswith("_no_direct_relation") or tool_result.result.items:
                return True
        return False

    def _build_user_prompt(self, messages: list[AiMessage]) -> str:
        return "\n".join(f"{(message.role or 'user').lower()}: {message.content}" for message in messages if message.content)

    def _latest_question(self, request: AiChatRequest) -> str:
        for message in reversed(request.messages):
            if message.content:
                return message.content
        return ""

    def _downstream_exception(self, exc: Exception) -> AiServiceError:
        if isinstance(exc, AiServiceError):
            return exc
        text = f"{exc.__class__.__name__}: {exc}".lower()
        if any(marker in text for marker in ["neo4j", "routing information", "connection refused", "10061", "bolt"]):
            return AiServiceError(503, "NEO4J_UNAVAILABLE", "知识图谱服务暂时不可用，请确认 Neo4j 已启动且连接配置正确。", exc)
        if "timeout" in text:
            return AiServiceError(504, "AI_PROVIDER_TIMEOUT", "AI 模型响应超时，请稍后重试。", exc)
        return AiServiceError(502, "AI_PROVIDER_UNAVAILABLE", "AI 模型服务暂时不可用，请确认 DeepSeek API Key、网络和模型配置正确。", exc)


class TcmReasonChatService:
    def __init__(self, settings: Settings, session_manager: Any | None = None) -> None:
        self._settings = settings
        self._session_manager = session_manager

    def chat(self, request: AiChatRequest, context: RequestContext | None = None) -> AiChatResponse:
        request_context = context or RequestContext()
        started_at = time.time()
        message_context = (
            self._session_manager.build_context(request.sessionId, request.messages, request_context)
            if self._session_manager is not None
            else request.messages
        )
        payload = self._build_payload(message_context)
        endpoint = self._chat_completions_url()
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        headers = {"Content-Type": "application/json"}
        if self._settings.tcm_reason_api_key:
            headers["Authorization"] = f"Bearer {self._settings.tcm_reason_api_key}"
        req = urllib.request.Request(endpoint, data=body, headers=headers, method="POST")
        ssl_context = None if self._settings.tcm_reason_verify else ssl._create_unverified_context()
        try:
            with urllib.request.urlopen(req, timeout=self._settings.tcm_reason_read_timeout, context=ssl_context) as response:
                response_body = response.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            message = self._extract_error_message(exc.read().decode("utf-8", errors="ignore"))
            raise AiServiceError(502, "TCMREASON_UNAVAILABLE", message or f"TCMReason 模型服务返回异常：{exc.code}", exc) from exc
        except TimeoutError as exc:
            raise AiServiceError(504, "TCMREASON_TIMEOUT", "TCMReason 模型响应超时，请稍后重试。", exc) from exc
        except Exception as exc:
            raise AiServiceError(502, "TCMREASON_UNAVAILABLE", "TCMReason 模型服务暂时不可用，请确认 TCMReason 地址、网络和模型配置正确。", exc) from exc
        parsed = self._parse_response(response_body)
        if self._session_manager is not None:
            self._session_manager.append_exchange(
                request.sessionId,
                request.messages,
                parsed.reply,
                request_context,
                parsed,
                [],
                int((time.time() - started_at) * 1000),
            )
        return parsed

    def _build_payload(self, messages: list[AiMessage]) -> dict[str, Any]:
        payload_messages = [{"role": "system", "content": TCM_REASON_SYSTEM_PROMPT}]
        for message in messages:
            role = (message.role or "user").lower()
            if role not in {"user", "assistant", "system"}:
                role = "user"
            payload_messages.append({"role": role, "content": message.content})
        return {
            "model": self._settings.tcm_reason_model,
            "temperature": self._settings.tcm_reason_temperature,
            "max_tokens": self._settings.tcm_reason_max_tokens,
            "stream": False,
            "messages": payload_messages,
        }

    def _parse_response(self, response_body: str) -> AiChatResponse:
        root = json.loads(response_body)
        choices = root.get("choices") or []
        if not choices:
            raise AiServiceError(502, "TCMREASON_BAD_RESPONSE", "TCMReason 没有返回有效回答。")
        reply = (((choices[0] or {}).get("message") or {}).get("content") or "").strip()
        if not reply:
            raise AiServiceError(502, "TCMREASON_BAD_RESPONSE", "TCMReason 没有返回有效回答。")
        usage = root.get("usage") or {}
        return AiChatResponse(
            id=root.get("id") or str(uuid.uuid4()),
            reply=reply,
            provider="tcmreason",
            model=root.get("model") or self._settings.tcm_reason_model,
            finishReason="tcmreason_answer",
            usage=Usage(
                promptTokens=usage.get("prompt_tokens"),
                completionTokens=usage.get("completion_tokens"),
                totalTokens=usage.get("total_tokens"),
            )
            if usage
            else None,
        )

    def _chat_completions_url(self) -> str:
        base = self._settings.tcm_reason_base_url.strip().rstrip("/")
        if not base:
            raise AiServiceError(503, "TCMREASON_NOT_CONFIGURED", "TCMReason 模型地址未配置，请设置 TCM_REASON_BASE_URL。")
        if base.endswith("/v1/chat/completions"):
            return base
        if base.endswith("/v1"):
            return f"{base}/chat/completions"
        return f"{base}/v1/chat/completions"

    def _extract_error_message(self, body: str) -> str | None:
        if not body:
            return None
        try:
            root = json.loads(body)
        except json.JSONDecodeError:
            return None
        error = root.get("error")
        if isinstance(error, str):
            return error
        if isinstance(error, dict) and error.get("message"):
            return str(error["message"])
        if root.get("message"):
            return str(root["message"])
        return None
