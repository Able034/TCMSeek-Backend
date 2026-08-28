# TCMSeek AI Service - FastAPI + LangChain

这是 `tcmseek-ai-service` SpringAI agent 的 Python 初始化复刻版，保留同一组主要 HTTP 契约和知识图谱工具名，便于在网关侧平滑切换或并行验证。

## 已初始化能力

- `POST /ai/chat`：TCMReason OpenAI-compatible 聊天接口。
- `POST /ai/aichat`：DeepSeek + LangChain agent + Neo4j 工具问答。
- `POST /ai/aichat/stream`：兼容 SSE 事件形状，当前为整答分块输出。
- `GET /ai/exports/{exportId}`：工具结果 CSV 下载。
- `GET/POST/PATCH/DELETE /ai/conversations`：PostgreSQL 会话、消息、工具调用、摘要、长期记忆持久化。
- `GET /ai/vector/status`、`GET /ai/vector/search`：pgvector 语义锚点状态与检索。
- `/tcmseek/llm/*`：旧路径兼容包装。
- `GET /actuator/health`：接近 Spring Boot actuator 的健康检查。

## 本地启动

```powershell
cd D:\TCMseek\services\TCMSeek-Backend-main\ai-server-langchain
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
```

按需设置环境变量后启动：

```powershell
$env:DEEPSEEK_API_KEY="your-deepseek-api-key"
$env:TCM_NEO4J_URI="neo4j://localhost:7687"
$env:TCM_NEO4J_USERNAME="neo4j"
$env:TCM_NEO4J_PASSWORD="password"
$env:AI_POSTGRES_URL="jdbc:postgresql://localhost:5434/tcmseek_ai"
$env:AI_POSTGRES_USERNAME="tcmseek"
$env:AI_POSTGRES_PASSWORD="tcmseek_dev"
$env:AI_VECTOR_ENABLED="true"
$env:AI_VECTOR_POSTGRES_URL="jdbc:postgresql://localhost:5434/tcmseek_ai"
$env:QWEN_EMBEDDING_BASE_URL="http://127.0.0.1:8001/v1"
$env:QWEN_EMBEDDING_API_KEY="local"
$env:QWEN_EMBEDDING_MODEL="BAAI/bge-m3"
uvicorn main:app --host 0.0.0.0 --port 8088 --reload
```

## 请求示例

```http
POST http://localhost:8088/ai/aichat
Content-Type: application/json

{
  "sessionId": "demo",
  "messages": [
    {
      "role": "user",
      "content": "人参和黄芪有哪些共同靶点？"
    }
  ]
}
```

响应字段对齐 SpringAI 版：`reply`、`graph`、`csvDownloadUrl`、`totalResults`、`displayedResults`、`toolResults`。

## 说明

当前版本已经接入 PostgreSQL 会话持久化和 pgvector 语义检索：会话上下文会从 `ai_message` 恢复并写回，达到阈值后会异步刷新 `ai_conversation_summary` 和 `ai_memory`；语义工具会读取 `ai_semantic_doc`，调用 OpenAI-compatible embedding 服务生成 query vector。Redis 缓存仍未迁移，当前以进程内上下文缓存 + PostgreSQL 为主。

PostgreSQL 表结构参考：

- `tcmseek-ai-service/docs/postgres-ai-conversation-schema.md`
- `tools/semantic-indexer/README.md`
