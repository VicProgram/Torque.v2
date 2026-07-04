from fastapi import APIRouter, WebSocket, WebSocketDisconnect

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
            self.active_connections[taller_id].remove(websocket)
            if not self.active_connections[taller_id]:
                del self.active_connections[taller_id]

    async def broadcast_to_taller(self, taller_id: str, message: dict):
        if taller_id not in self.active_connections:
            return
        for connection in self.active_connections[taller_id]:
            try:
                await connection.send_json(message)
            except Exception:
                pass

    async def broadcast_to_all(self, message: dict):
        for taller_id in list(self.active_connections.keys()):
            await self.broadcast_to_taller(taller_id, message)


manager = ConnectionManager()


@websocket_router.websocket("/ws/taller/{taller_id}")
async def taller_websocket(websocket: WebSocket, taller_id: str):
    await manager.connect(taller_id, websocket)
    try:
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        manager.disconnect(taller_id, websocket)
