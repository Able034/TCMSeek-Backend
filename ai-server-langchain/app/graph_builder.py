from __future__ import annotations

import re
from typing import Any

from app.models import AiGraphData, GraphEdge, GraphNode, ToolCallResult


class AiGraphBuilder:
    DISPLAY_LIMIT = 100

    def build(self, tool_results: list[ToolCallResult]) -> AiGraphData | None:
        for tool_result in tool_results:
            graph = self._build_from_tool(tool_result)
            if graph is not None:
                return graph
        return None

    def _build_from_tool(self, call: ToolCallResult) -> AiGraphData | None:
        query_type = call.result.queryType
        if query_type == "common_targets":
            return self._build_common_graph(call, "target", "target", "herb-target")
        if query_type == "common_compounds":
            return self._build_common_graph(call, "compound", "compound", "contains")
        one_to_many = {
            "herb_compounds": ("herbName", "herb", "compound", "compound", "contains"),
            "prescription_herbs": ("prescriptionName", "prescription", "herb", "herb", "contains"),
            "herb_diseases": ("herbName", "herb", "disease", "disease", "treats"),
            "prescription_symptoms": ("prescriptionName", "prescription", "symptom", "symptom", "treats"),
            "prescription_syndromes": ("prescriptionName", "prescription", "syndrome", "syndrome", "treats"),
            "disease_targets": ("diseaseName", "disease", "target", "target", "associated"),
            "syndrome_symptoms": ("syndromeName", "syndrome", "symptom", "symptom", "has"),
            "compound_targets": ("compound", "compound", "target", "target", "targets"),
            "pathway_targets": ("pathwayName", "pathway", "target", "target", "contains"),
            "medicalcase_prescriptions": ("caseId", "medicalcase", "prescription", "prescription", "uses"),
        }
        if query_type in one_to_many:
            return self._build_one_to_many(call, *one_to_many[query_type])
        if query_type in {"herb_compound_targets", "herb_compound_targets_semantic"}:
            return self._build_one_to_many(call, "herbName", "herb", "target", "target", "compound-target")
        return None

    def _build_common_graph(self, call: ToolCallResult, entity_type: str, item_key: str, edge_type: str) -> AiGraphData | None:
        herbs = self._herb_names(call)
        items = self._unique_column_values(call.result.items, item_key)
        if len(herbs) < 2 or not items:
            return None
        graph = self._base_graph(call, entity_type, item_key)
        graph.herbs = herbs
        for herb in herbs:
            graph.nodes.append(GraphNode(id=f"herb:{herb}", label=herb, type="herb"))
        for item in items:
            item_id = f"{entity_type}:{item}"
            graph.nodes.append(GraphNode(id=item_id, label=item, type=entity_type))
            for herb in herbs:
                graph.edges.append(GraphEdge(source=f"herb:{herb}", target=item_id, type=edge_type))
        graph.displayedTargets = len(items)
        return graph

    def _build_one_to_many(
        self,
        call: ToolCallResult,
        main_argument_name: str,
        main_type: str,
        item_key: str,
        entity_type: str,
        edge_type: str,
    ) -> AiGraphData | None:
        main_name = self._as_string(call.arguments.get(main_argument_name))
        items = self._unique_column_values(call.result.items, item_key)
        if not main_name or not items:
            return None
        graph = self._base_graph(call, entity_type, item_key)
        graph.mainName = main_name
        graph.mainType = main_type
        main_id = f"{main_type}:{main_name}"
        graph.nodes.append(GraphNode(id=main_id, label=main_name, type=main_type))
        for item in items:
            item_id = f"{entity_type}:{item}"
            graph.nodes.append(GraphNode(id=item_id, label=item, type=entity_type))
            graph.edges.append(GraphEdge(source=main_id, target=item_id, type=edge_type))
        graph.displayedTargets = len(items)
        return graph

    def _base_graph(self, call: ToolCallResult, entity_type: str, entity_key: str) -> AiGraphData:
        return AiGraphData(totalTargets=call.result.total, entityType=entity_type, entityKey=entity_key)

    def _unique_column_values(self, rows: list[dict[str, Any]], key: str) -> list[str]:
        values: list[str] = []
        for row in rows:
            value = self._as_string(row.get(key))
            if value and value not in values:
                values.append(value)
            if len(values) >= self.DISPLAY_LIMIT:
                break
        return values

    def _herb_names(self, call: ToolCallResult) -> list[str]:
        herbs: list[str] = []
        for key in ["herbNames", "herbs", "herbA", "herbB"]:
            value = call.arguments.get(key)
            if isinstance(value, list):
                for item in value:
                    self._add_herb_values(herbs, self._as_string(item))
            else:
                self._add_herb_values(herbs, self._as_string(value))
        return herbs

    def _add_herb_values(self, herbs: list[str], value: str) -> None:
        cleaned = re.sub(r"[\[\]{}()（）\"'“”‘’]", "", value or "").strip()
        if not cleaned:
            return
        for part in re.split(r"以及|和|与|及|跟|、|,|，|;|；|/", cleaned):
            herb = re.sub(r"^[：:，,、。\s]+", "", part)
            herb = re.sub(r"[：:，,、。？?\s]+$", "", herb).strip()
            if herb and herb not in herbs:
                herbs.append(herb)

    def _as_string(self, value: Any) -> str:
        if value is None:
            return ""
        if isinstance(value, list):
            return "，".join(self._as_string(item) for item in value)
        return str(value)
