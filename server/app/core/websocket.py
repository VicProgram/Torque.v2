import logging

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from app import database
from app.core.security import decode_access_token
from app.models.usuario import Usuario

logger = logging.getLogger("torque.websocket")
websocket_router = APIRouter()


class ConnectionManager:
    def __init__(self):
        self.active_connections: dict[str, list[WebSocket]] = {}

    async def connect(self, taller_id: str, websocket: WebSocket):
        await websocket.accept()
        if taller_id not in self.active_connections:
            self.active_connections[taller_id] = []
        self.active_connections[taller_id].append(websocket)

    def disconnect(self, taller_id: str, websocket: WebSocket):
        if taller_id in self.active_connections:
            try:
                self.active_connections[taller_id].remove(websocket)
            except ValueError:
                pass
            if not self.active_connections[taller_id]:
                del self.active_connections[taller_id]

    async def broadcast_to_taller(self, taller_id: str, message: dict):
        if taller_id not in self.active_connections:
            return
        for connection in list(self.active_connections[taller_id]):
            try:
                await connection.send_json(message)
            except Exception:
                logger.exception("websocket_send_failed")
                self.disconnect(taller_id, connection)

    async def broadcast_to_all(self, message: dict):
        for taller_id in list(self.active_connections.keys()):
            await self.broadcast_to_taller(taller_id, message)


manager = ConnectionManager()


def _authorize_websocket(token: str | None, taller_id: str) -> bool:
    if not token:
        return False
    payload = decode_access_token(token)
    if payload is None:
        return False
    if str(payload.get("id_taller", "")) != str(taller_id):
        return False

    db = database.SessionLocal()
    try:
        user = db.query(Usuario).filter(Usuario.id == payload.get("sub")).first()
        if user is None or not user.activo:
            return False
        if int(payload.get("ver", 0)) != int(user.token_version or 0):
            return False
        return True
    finally:
        db.close()


@websocket_router.websocket("/ws/taller/{taller_id}")
async def taller_websocket(websocket: WebSocket, taller_id: str):
    # Browsers cannot set an Authorization header on a WebSocket handshake, so
    # the short-lived JWT is passed as a query parameter. It is never logged.
    token = websocket.query_params.get("token")
    if not _authorize_websocket(token, taller_id):
        logger.warning(
            "websocket_denied",
            extra={
                "path": websocket.url.path,
                "client": websocket.client.host if websocket.client else None,
            },
        )
        await websocket.close(code=1008)
        return

    await manager.connect(taller_id, websocket)
    try:
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        manager.disconnect(taller_id, websocket)
    except Exception:
        logger.exception("websocket_error")
        manager.disconnect(taller_id, websocket)
        await websocket.close(code=1011)
