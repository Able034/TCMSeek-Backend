from __future__ import annotations

from collections.abc import Mapping
from typing import Any

from app.config import Settings


class TcmGraphRepository:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._driver: Any | None = None

    def _connect(self) -> Any:
        if self._driver is None:
            try:
                from neo4j import GraphDatabase
            except ImportError as exc:
                raise RuntimeError("neo4j dependency is not installed") from exc
            auth = (self._settings.neo4j_username, self._settings.neo4j_password)
            self._driver = GraphDatabase.driver(self._settings.neo4j_uri, auth=auth)
        return self._driver

    def query(self, cypher: str, params: Mapping[str, Any] | None = None) -> list[dict[str, Any]]:
        driver = self._connect()
        rows: list[dict[str, Any]] = []
        with driver.session() as session:
            result = session.run(cypher, dict(params or {}))
            for record in result:
                rows.append({key: self._plain(record[key]) for key in record.keys()})
        return rows

    def verify(self) -> bool:
        try:
            self.query("RETURN 1 AS ok", {})
            return True
        except Exception:
            return False

    def close(self) -> None:
        if self._driver is not None:
            self._driver.close()
            self._driver = None

    def _plain(self, value: Any) -> Any:
        if value is None:
            return None
        if isinstance(value, (str, int, float, bool)):
            return value
        if isinstance(value, list):
            return [self._plain(item) for item in value]
        if isinstance(value, dict):
            return {str(key): self._plain(item) for key, item in value.items()}
        if hasattr(value, "labels") and hasattr(value, "items"):
            node = {str(key): self._plain(item) for key, item in value.items()}
            node["_labels"] = list(value.labels)
            return node
        if hasattr(value, "items"):
            return {str(key): self._plain(item) for key, item in value.items()}
        return value
