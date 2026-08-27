import os
import time
from threading import Semaphore
from typing import Any, Dict, List

import uvicorn
import torch
from fastapi import FastAPI, HTTPException, Request
from sentence_transformers import SentenceTransformer


MODEL_NAME = os.getenv("EMBEDDING_MODEL_NAME", "BAAI/bge-m3")
HOST = os.getenv("EMBEDDING_HOST", "0.0.0.0")
PORT = int(os.getenv("EMBEDDING_PORT", "8001"))
BATCH_SIZE = int(os.getenv("EMBEDDING_BATCH_SIZE", "32"))
MAX_CONCURRENT = int(os.getenv("EMBEDDING_MAX_CONCURRENT", "1"))
NORMALIZE = os.getenv("EMBEDDING_NORMALIZE", "true").lower() in {"1", "true", "yes"}
DEVICE = os.getenv("EMBEDDING_DEVICE", "cuda" if torch.cuda.is_available() else "cpu")

app = FastAPI(title="TCMSeek Local Embedding Server")
model = SentenceTransformer(MODEL_NAME, device=DEVICE)
model_gate = Semaphore(max(1, MAX_CONCURRENT))


@app.get("/health")
def health() -> Dict[str, Any]:
    dimension = model.get_sentence_embedding_dimension()
    return {
        "status": "ok",
        "model": MODEL_NAME,
        "dimension": dimension,
        "device": DEVICE,
        "cudaAvailable": torch.cuda.is_available(),
        "cudaDeviceName": torch.cuda.get_device_name(0) if torch.cuda.is_available() else None,
        "batchSize": BATCH_SIZE,
        "maxConcurrent": MAX_CONCURRENT,
        "normalize": NORMALIZE,
    }


@app.post("/v1/embeddings")
async def embeddings(request: Request) -> Dict[str, Any]:
    try:
        payload = await request.json()
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"invalid json body: {exc}") from exc

    raw_input = payload.get("input") if isinstance(payload, dict) else None
    texts = raw_input if isinstance(raw_input, list) else [raw_input]
    if not texts:
        raise HTTPException(status_code=400, detail="input is empty")
    if any(text is None or str(text).strip() == "" for text in texts):
        raise HTTPException(status_code=400, detail="input contains empty text")

    dimensions = payload.get("dimensions")
    started = time.perf_counter()
    with model_gate:
        vectors = model.encode(
            texts,
            batch_size=BATCH_SIZE,
            normalize_embeddings=NORMALIZE,
            show_progress_bar=False,
        )
    vectors = vectors.tolist()
    actual_dimension = len(vectors[0]) if vectors else 0
    if dimensions is not None and dimensions != actual_dimension:
        raise HTTPException(
            status_code=400,
            detail=f"dimension mismatch requested={dimensions} actual={actual_dimension}",
        )

    return {
        "object": "list",
        "model": payload.get("model") or MODEL_NAME,
        "data": [
            {
                "object": "embedding",
                "index": index,
                "embedding": vector,
            }
            for index, vector in enumerate(vectors)
        ],
        "usage": {
            "prompt_tokens": 0,
            "total_tokens": 0,
            "elapsed_ms": int((time.perf_counter() - started) * 1000),
        },
    }


if __name__ == "__main__":
    uvicorn.run(app, host=HOST, port=PORT)
