from __future__ import annotations

import os
import re
from dataclasses import dataclass


def _load_dotenv() -> None:
    env_path = os.path.join(os.getcwd(), ".env")
    if not os.path.exists(env_path):
        return
    with open(env_path, encoding="utf-8") as file:
        for raw_line in file:
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip('"').strip("'")
            if key and key not in os.environ:
                os.environ[key] = value


_load_dotenv()


def _env(name: str, default: str = "") -> str:
    value = os.getenv(name)
    return default if value is None or value == "" else value


def _env_int(name: str, default: int) -> int:
    try:
        return int(_env(name, str(default)))
    except ValueError:
        return default


def _env_float(name: str, default: float) -> float:
    try:
        return float(_env(name, str(default)))
    except ValueError:
        return default


def _env_bool(name: str, default: bool) -> bool:
    value = _env(name, str(default)).strip().lower()
    return value in {"1", "true", "yes", "y", "on"}


def _duration_seconds(name: str, default: float) -> float:
    raw = _env(name, str(default)).strip().lower()
    match = re.fullmatch(r"([0-9]+(?:\.[0-9]+)?)(ms|s|m|h)?", raw)
    if not match:
        return default
    value = float(match.group(1))
    unit = match.group(2) or "s"
    if unit == "ms":
        return value / 1000
    if unit == "m":
        return value * 60
    if unit == "h":
        return value * 3600
    return value


@dataclass(frozen=True)
class Settings:
    service_name: str = _env("AI_SERVICE_NAME", "tcmseek-ai-service-langchain")
    host: str = _env("AI_HOST", "0.0.0.0")
    port: int = _env_int("AI_PORT", 8088)

    deepseek_api_key: str = _env("DEEPSEEK_API_KEY", "")
    deepseek_base_url: str = _env("DEEPSEEK_BASE_URL", "https://api.deepseek.com")
    deepseek_chat_model: str = _env("DEEPSEEK_CHAT_MODEL", "deepseek-chat")
    deepseek_temperature: float = _env_float("DEEPSEEK_TEMPERATURE", 0.2)
    deepseek_max_tokens: int = _env_int("DEEPSEEK_MAX_TOKENS", 2048)

    tcm_reason_base_url: str = _env(
        "TCM_REASON_BASE_URL",
        _env("TCM_LLM_BASE_URL", ""),
    )
    tcm_reason_api_key: str = _env("TCM_REASON_API_KEY", "")
    tcm_reason_model: str = _env("TCM_REASON_MODEL", _env("TCM_LLM_MODEL", "TCMReason"))
    tcm_reason_temperature: float = _env_float("TCM_REASON_TEMPERATURE", 0.3)
    tcm_reason_max_tokens: int = _env_int("TCM_REASON_MAX_TOKENS", 1024)
    tcm_reason_verify: bool = _env_bool("TCM_REASON_VERIFY", False)
    tcm_reason_connect_timeout: float = _duration_seconds("TCM_REASON_CONNECT_TIMEOUT", 10)
    tcm_reason_read_timeout: float = _duration_seconds("TCM_REASON_READ_TIMEOUT", 120)

    neo4j_uri: str = _env("TCM_NEO4J_URI", "neo4j://localhost:7687")
    neo4j_username: str = _env("TCM_NEO4J_USERNAME", "neo4j")
    neo4j_password: str = _env("TCM_NEO4J_PASSWORD", "")

    postgres_url: str = _env("AI_POSTGRES_URL", "jdbc:postgresql://127.0.0.1:5434/tcmseek_ai")
    postgres_username: str = _env("AI_POSTGRES_USERNAME", "tcmseek")
    postgres_password: str = _env("AI_POSTGRES_PASSWORD", "tcmseek_dev")

    conversation_persistence_enabled: bool = _env_bool("AI_CONVERSATION_PERSISTENCE_ENABLED", True)
    conversation_context_message_limit: int = _env_int("AI_CONVERSATION_CONTEXT_MESSAGE_LIMIT", 20)
    conversation_summary_enabled: bool = _env_bool("AI_CONVERSATION_SUMMARY_ENABLED", True)
    conversation_summary_trigger_message_count: int = _env_int("AI_CONVERSATION_SUMMARY_TRIGGER_MESSAGE_COUNT", 12)
    conversation_summary_refresh_message_count: int = _env_int("AI_CONVERSATION_SUMMARY_REFRESH_MESSAGE_COUNT", 6)
    conversation_summary_source_message_limit: int = _env_int("AI_CONVERSATION_SUMMARY_SOURCE_MESSAGE_LIMIT", 40)
    memory_enabled: bool = _env_bool("AI_MEMORY_ENABLED", True)
    memory_max_items: int = _env_int("AI_MEMORY_MAX_ITEMS", 5)
    memory_source_message_limit: int = _env_int("AI_MEMORY_SOURCE_MESSAGE_LIMIT", 20)

    vector_enabled: bool = _env_bool("AI_VECTOR_ENABLED", False)
    vector_postgres_url: str = _env("AI_VECTOR_POSTGRES_URL", postgres_url)
    vector_postgres_username: str = _env("AI_VECTOR_POSTGRES_USERNAME", postgres_username)
    vector_postgres_password: str = _env("AI_VECTOR_POSTGRES_PASSWORD", postgres_password)
    vector_dimension: int = _env_int("AI_VECTOR_DIMENSION", 1024)
    vector_search_limit: int = _env_int("AI_VECTOR_SEARCH_LIMIT", 30)
    vector_index_limit: int = _env_int("AI_VECTOR_INDEX_LIMIT", 1000)
    embedding_base_url: str = _env("QWEN_EMBEDDING_BASE_URL", "http://127.0.0.1:8001/v1")
    embedding_api_key: str = _env("QWEN_EMBEDDING_API_KEY", "local")
    embedding_model: str = _env("QWEN_EMBEDDING_MODEL", "BAAI/bge-m3")

    request_timeout: float = _duration_seconds("AI_REQUEST_TIMEOUT", 60)
    max_retries: int = _env_int("AI_MAX_RETRIES", 0)
    retry_backoff: float = _duration_seconds("AI_RETRY_BACKOFF", 0.5)
    tool_query_limit: int = _env_int("AI_TOOL_QUERY_LIMIT", 1000)
    tool_answer_item_limit: int = _env_int("AI_TOOL_ANSWER_ITEM_LIMIT", 8)
    max_reply_chars: int = _env_int("AI_MAX_REPLY_CHARS", 4000)
    cors_origins: str = _env("AI_CORS_ORIGINS", "*")


settings = Settings()
