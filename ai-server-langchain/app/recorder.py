from __future__ import annotations

from contextvars import ContextVar
from typing import Any

from app.models import GraphToolResult, ToolCallResult


_recorded_calls: ContextVar[list[ToolCallResult] | None] = ContextVar(
    "recorded_tool_calls",
    default=None,
)


class ToolExecutionRecorder:
    def start(self) -> None:
        _recorded_calls.set([])

    def record(self, tool_name: str, arguments: dict[str, Any], result: GraphToolResult) -> None:
        calls = _recorded_calls.get()
        if calls is None:
            calls = []
            _recorded_calls.set(calls)
        calls.append(ToolCallResult(toolName=tool_name, arguments=arguments, result=result))

    def finish(self) -> list[ToolCallResult]:
        calls = _recorded_calls.get() or []
        _recorded_calls.set(None)
        return list(calls)
