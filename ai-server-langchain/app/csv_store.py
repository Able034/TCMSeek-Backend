from __future__ import annotations

import csv
import io
import time
import uuid
from dataclasses import dataclass, field
from typing import Any

from app.models import ToolCallResult


@dataclass
class ExportRecord:
    filename: str
    headers: list[str]
    rows: list[list[str]]
    created_at: float = field(default_factory=time.time)


class CsvExportStore:
    EXPORT_TTL_SECONDS = 30 * 60

    def __init__(self) -> None:
        self._records: dict[str, ExportRecord] = {}

    def save_first_export(self, tool_results: list[ToolCallResult], user_id: str | None = None) -> str | None:
        self._cleanup_expired()
        for tool_result in tool_results:
            record = self._to_export_record(tool_result)
            if record is None:
                continue
            export_id = uuid.uuid4().hex
            self._records[self._scoped_key(user_id, export_id)] = record
            return export_id
        return None

    def get(self, export_id: str, user_id: str | None = None) -> ExportRecord | None:
        self._cleanup_expired()
        return self._records.get(self._scoped_key(user_id, export_id))

    def render(self, record: ExportRecord) -> str:
        output = io.StringIO()
        writer = csv.writer(output, lineterminator="\n")
        writer.writerow(record.headers)
        writer.writerows(record.rows)
        return output.getvalue()

    def _to_export_record(self, tool_result: ToolCallResult) -> ExportRecord | None:
        result = tool_result.result
        if not result.items:
            return None
        query_type = result.queryType
        if query_type == "common_targets":
            herb_names = self._herb_names(tool_result)
            rows = [[herb_names, self._as_string(item.get("target")), self._as_string(item.get("targetId"))] for item in result.items]
            return ExportRecord("common-targets.csv", ["herb_names", "target", "target_id"], rows)
        if query_type == "common_compounds":
            herb_names = self._herb_names(tool_result)
            rows = [
                [
                    herb_names,
                    self._as_string(item.get("compound")),
                    self._as_string(item.get("inchikey")),
                    self._as_string(item.get("formula")),
                ]
                for item in result.items
            ]
            return ExportRecord("common-compounds.csv", ["herb_names", "compound", "inchikey", "formula"], rows)
        if query_type == "herb_compounds":
            herb_name = self._as_string(tool_result.arguments.get("herbName"))
            rows = [
                [
                    herb_name,
                    self._as_string(item.get("compound")),
                    self._as_string(item.get("inchikey")),
                    self._as_string(item.get("formula")),
                ]
                for item in result.items
            ]
            return ExportRecord("herb-compounds.csv", ["herb_name", "compound", "inchikey", "formula"], rows)
        if query_type in {"herb_compound_targets", "herb_compound_targets_semantic"}:
            herb_name = self._as_string(tool_result.arguments.get("herbName"))
            rows = [
                [
                    herb_name,
                    self._as_string(item.get("compound")),
                    self._as_string(item.get("inchikey")),
                    self._as_string(item.get("formula")),
                    self._as_string(item.get("target")),
                    self._as_string(item.get("targetId")),
                ]
                for item in result.items
            ]
            return ExportRecord(
                "herb-compound-targets.csv",
                ["herb_name", "compound", "inchikey", "formula", "target", "target_id"],
                rows,
            )
        if query_type == "prescription_herbs":
            prescription_name = self._as_string(tool_result.arguments.get("prescriptionName"))
            rows = [[prescription_name, self._as_string(item.get("herb"))] for item in result.items]
            return ExportRecord("prescription-herbs.csv", ["prescription_name", "herb"], rows)
        single_input_exports = {
            "herb_diseases": ("herbName", "herb-diseases.csv", ["herb_name", "disease"], "disease"),
            "prescription_symptoms": ("prescriptionName", "prescription-symptoms.csv", ["prescription_name", "symptom"], "symptom"),
            "prescription_syndromes": ("prescriptionName", "prescription-syndromes.csv", ["prescription_name", "syndrome"], "syndrome"),
            "disease_targets": ("diseaseName", "disease-targets.csv", ["disease_name", "target"], "target"),
            "syndrome_symptoms": ("syndromeName", "syndrome-symptoms.csv", ["syndrome_name", "symptom"], "symptom"),
            "compound_targets": ("compound", "compound-targets.csv", ["compound", "target"], "target"),
            "pathway_targets": ("pathwayName", "pathway-targets.csv", ["pathway", "target"], "target"),
            "medicalcase_prescriptions": ("caseId", "medicalcase-prescriptions.csv", ["case_id", "prescription"], "prescription"),
        }
        if query_type in single_input_exports:
            input_key, filename, headers, item_key = single_input_exports[query_type]
            input_value = self._as_string(tool_result.arguments.get(input_key))
            rows = [[input_value, self._as_string(item.get(item_key))] for item in result.items]
            return ExportRecord(filename, headers, rows)
        return self._generic_export(tool_result)

    def _generic_export(self, tool_result: ToolCallResult) -> ExportRecord:
        query_type = tool_result.result.queryType
        argument_keys = list(tool_result.arguments.keys())
        item_keys: list[str] = []
        for item in tool_result.result.items:
            for key in item.keys():
                if key not in item_keys:
                    item_keys.append(key)
        headers = ["query_type", *[f"arg_{key}" for key in argument_keys], *item_keys]
        rows: list[list[str]] = []
        for item in tool_result.result.items:
            row = [query_type]
            row.extend(self._as_string(tool_result.arguments.get(key)) for key in argument_keys)
            row.extend(self._as_string(item.get(key)) for key in item_keys)
            rows.append(row)
        filename = f"{query_type or 'graph-result'}.csv".replace("/", "-")
        return ExportRecord(filename, headers, rows)

    def _herb_names(self, tool_result: ToolCallResult) -> str:
        herb_names = tool_result.arguments.get("herbNames")
        if isinstance(herb_names, list):
            return "，".join(self._as_string(item) for item in herb_names if self._as_string(item))
        if herb_names is not None:
            return self._as_string(herb_names)
        values = [self._as_string(tool_result.arguments.get("herbA")), self._as_string(tool_result.arguments.get("herbB"))]
        return "，".join(value for value in values if value)

    def _cleanup_expired(self) -> None:
        now = time.time()
        expired = [key for key, value in self._records.items() if now - value.created_at > self.EXPORT_TTL_SECONDS]
        for key in expired:
            self._records.pop(key, None)

    def _scoped_key(self, user_id: str | None, export_id: str) -> str:
        owner = user_id.strip() if user_id and user_id.strip() else "anonymous"
        return f"{owner}:{export_id}"

    def _as_string(self, value: Any) -> str:
        if value is None:
            return ""
        if isinstance(value, list):
            return "；".join(self._as_string(item) for item in value)
        return str(value)
