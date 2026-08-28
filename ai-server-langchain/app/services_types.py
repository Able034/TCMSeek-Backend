from __future__ import annotations

from typing import Protocol


class RequestContextLike(Protocol):
    request_id: str | None
    user_id: str | None
    username: str | None
    account: str | None
