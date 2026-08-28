from __future__ import annotations

import json
import urllib.request
from typing import Any

from app.config import Settings
from app.models import SemanticAnchor, VectorStatus
from app.postgres import PostgresDatabase


class QwenEmbeddingClient:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    def is_configured(self) -> bool:
        return (
            self._settings.vector_enabled
            and bool(self._settings.embedding_api_key.strip())
            and bool(self._settings.embedding_model.strip())
        )

    def embed(self, input_text: str) -> list[float]:
        if not self.is_configured():
            raise RuntimeError("embedding is not configured")
        if not input_text or not input_text.strip():
            raise ValueError("embedding input must not be blank")
        endpoint = self._endpoint()
        payload = {
            "model": self._settings.embedding_model,
            "input": input_text,
            "dimensions": self._settings.vector_dimension,
        }
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        request = urllib.request.Request(
            endpoint,
            data=data,
            headers={
                "Content-Type": "application/json",
                "Accept": "application/json",
                "Authorization": f"Bearer {self._settings.embedding_api_key}",
            },
            method="POST",
        )
        with urllib.request.urlopen(request, timeout=self._settings.request_timeout) as response:
            body = json.loads(response.read().decode("utf-8"))
        embedding = ((body.get("data") or [{}])[0] or {}).get("embedding")
        if not isinstance(embedding, list) or not embedding:
            raise RuntimeError("embedding response is empty")
        if len(embedding) != self._settings.vector_dimension:
            raise RuntimeError(
                f"embedding dimension mismatch: expected {self._settings.vector_dimension}, got {len(embedding)}"
            )
        return [float(item or 0) for item in embedding]

    def embed_as_vector_literal(self, input_text: str) -> str:
        return self.to_vector_literal(self.embed(input_text))

    @staticmethod
    def to_vector_literal(embedding: list[float]) -> str:
        return "[" + ",".join(str(item if item is not None else 0) for item in embedding) + "]"

    def _endpoint(self) -> str:
        base = self._settings.embedding_base_url.strip().rstrip("/")
        if base.endswith("/embeddings"):
            return base
        return f"{base}/embeddings"


class SemanticSearchService:
    TARGET_SYMBOL_ANCHOR_TYPES = {"topic", "target"}

    def __init__(self, database: PostgresDatabase, embedding_client: QwenEmbeddingClient, settings: Settings) -> None:
        self._database = database
        self._embedding_client = embedding_client
        self._settings = settings

    def is_ready(self) -> bool:
        return self._settings.vector_enabled and self._embedding_client.is_configured() and self._database.ping()

    def status(self) -> VectorStatus:
        count = 0
        jdbc_available = False
        if self._settings.vector_enabled:
            try:
                with self._database.connect() as conn:
                    jdbc_available = True
                    with conn.cursor() as cur:
                        cur.execute("SELECT count(*) AS total FROM ai_semantic_doc WHERE enabled = true")
                        row = cur.fetchone()
                        count = int(row["total"]) if row else 0
            except Exception:
                jdbc_available = False
        return VectorStatus(
            enabled=self._settings.vector_enabled,
            jdbcAvailable=jdbc_available,
            embeddingConfigured=self._embedding_client.is_configured(),
            dimension=self._settings.vector_dimension,
            documentCount=count,
        )

    def search(self, query: str, entity_types: list[str] | None, limit: int) -> list[SemanticAnchor]:
        if not self._settings.vector_enabled or not query or not query.strip() or not self._embedding_client.is_configured():
            return []
        try:
            vector_literal = self._embedding_client.embed_as_vector_literal(query)
        except Exception:
            return []
        types = self._normalize_types(entity_types)
        safe_limit = max(1, min(limit, self._settings.vector_search_limit))
        type_filter = ""
        params: list[Any] = [vector_literal]
        if types:
            type_filter = " AND entity_type IN (" + ",".join(["%s"] * len(types)) + ")"
            params.extend(types)
        params.extend([vector_literal, safe_limit])
        sql = f"""
            SELECT id, entity_type, entity_id, neo4j_label, neo4j_key, name, aliases,
                   source_table, source_pk, metadata::text AS metadata,
                   embedding <=> %s::vector AS distance
            FROM ai_semantic_doc
            WHERE enabled = true
            {type_filter}
            ORDER BY embedding <=> %s::vector
            LIMIT %s
        """
        try:
            rows = self._query(sql, params)
        except Exception:
            return []
        return [self._anchor(row) for row in rows]

    def exact_topic_target_symbols(self, topic: str | None) -> list[str]:
        if not self._settings.vector_enabled or not topic or not topic.strip():
            return []
        sql = """
            SELECT metadata::text AS metadata
            FROM ai_semantic_doc
            WHERE enabled = true
              AND entity_type = 'topic'
              AND (lower(name) = lower(%s)
                OR EXISTS (
                    SELECT 1
                    FROM unnest(aliases) AS alias(alias_name)
                    WHERE lower(alias.alias_name) = lower(%s)
                ))
            LIMIT 5
        """
        try:
            rows = self._query(sql, [topic, topic])
        except Exception:
            return []
        symbols: list[str] = []
        for row in rows:
            for symbol in self.target_symbols_from_metadata(self.read_metadata(row.get("metadata"))):
                if symbol not in symbols:
                    symbols.append(symbol)
        return symbols

    def target_symbols_from_anchors(self, anchors: list[SemanticAnchor], max_distance: float = float("inf")) -> list[str]:
        symbols: list[str] = []
        for anchor in anchors or []:
            entity_type = (anchor.entityType or "").strip().lower()
            if entity_type not in self.TARGET_SYMBOL_ANCHOR_TYPES or anchor.distance > max_distance:
                continue
            if entity_type == "target":
                self._add(symbols, anchor.name)
                self._add(symbols, anchor.entityId)
            for symbol in self.target_symbols_from_metadata(anchor.metadata):
                self._add(symbols, symbol)
        return symbols

    def target_symbols_from_metadata(self, metadata: dict[str, Any] | None) -> list[str]:
        if not metadata:
            return []
        symbols: list[str] = []
        for key in ["targetSymbols", "targetAliases"]:
            value = metadata.get(key)
            if isinstance(value, list):
                for item in value:
                    self._add(symbols, str(item) if item is not None else "")
            elif isinstance(value, str):
                for part in value.replace("，", ",").replace("、", ",").replace("；", ",").split(","):
                    self._add(symbols, part)
        return symbols

    def read_metadata(self, metadata: Any) -> dict[str, Any]:
        if isinstance(metadata, dict):
            return metadata
        if not metadata:
            return {}
        try:
            value = json.loads(str(metadata))
        except json.JSONDecodeError:
            return {}
        return value if isinstance(value, dict) else {}

    def _query(self, sql: str, params: list[Any]) -> list[dict[str, Any]]:
        with self._database.connect() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params)
                return list(cur.fetchall())

    def _anchor(self, row: dict[str, Any]) -> SemanticAnchor:
        aliases = row.get("aliases") or []
        if isinstance(aliases, str):
            aliases = [aliases]
        return SemanticAnchor(
            id=row["id"],
            entityType=row["entity_type"],
            entityId=row.get("entity_id"),
            neo4jLabel=row.get("neo4j_label"),
            neo4jKey=row.get("neo4j_key"),
            name=row["name"],
            aliases=list(aliases),
            sourceTable=row.get("source_table"),
            sourcePk=row.get("source_pk"),
            metadata=self.read_metadata(row.get("metadata")),
            distance=float(row.get("distance") or 0),
        )

    def _normalize_types(self, entity_types: list[str] | None) -> list[str]:
        values: list[str] = []
        for entity_type in entity_types or []:
            normalized = entity_type.strip().lower()
            if normalized and normalized not in values:
                values.append(normalized)
        return values

    def _add(self, values: list[str], value: str | None) -> None:
        cleaned = (value or "").strip()
        if cleaned and cleaned not in values:
            values.append(cleaned)
