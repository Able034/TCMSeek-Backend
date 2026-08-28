from __future__ import annotations

from app.config import Settings
from app.models import GraphToolResult, ToolCallResult


class ToolResultCompressor:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    @property
    def answer_item_limit(self) -> int:
        return max(1, self._settings.tool_answer_item_limit)

    def for_answer(self, tool_results: list[ToolCallResult]) -> list[ToolCallResult]:
        compressed = [self._compress(item) for item in tool_results if item is not None]
        with_data = [item for item in compressed if item.result.items]
        if with_data:
            no_direct = [item for item in with_data if self._is_no_direct_prescription_relation(item)]
            return no_direct or with_data
        return compressed

    def first_total(self, tool_results: list[ToolCallResult]) -> int | None:
        first = self._first_result(tool_results)
        return first.total if first else None

    def first_displayed(self, tool_results: list[ToolCallResult]) -> int | None:
        first = self._first_result(tool_results)
        if not first:
            return None
        return min(len(first.items), self.answer_item_limit)

    def _compress(self, source: ToolCallResult) -> ToolCallResult:
        result = GraphToolResult(
            queryType=source.result.queryType,
            total=source.result.total,
            items=[dict(item) for item in source.result.items[: self.answer_item_limit]],
        )
        return ToolCallResult(toolName=source.toolName, arguments=dict(source.arguments), result=result)

    def _is_no_direct_prescription_relation(self, tool_result: ToolCallResult) -> bool:
        if tool_result.result.queryType in {"disease_prescriptions_no_direct_relation", "condition_prescriptions_no_direct_relation"}:
            return True
        return any(
            item.get("evidenceType")
            in {"semantic_disease_anchor_no_direct_prescription", "semantic_condition_anchor_no_direct_prescription"}
            for item in tool_result.result.items
        )

    def _first_result(self, tool_results: list[ToolCallResult]) -> GraphToolResult | None:
        for tool_result in tool_results:
            if tool_result.result is not None:
                return tool_result.result
        return None
