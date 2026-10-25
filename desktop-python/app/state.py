from collections.abc import Callable
from dataclasses import dataclass, field


@dataclass
class AppState:
    token: str = ""
    id_usuario: str = ""
    id_taller: str = ""
    rol: str = ""
    nombre: str = ""
    server_url: str = "http://localhost:8000"
    logged_in: bool = False
    _listeners: list[Callable] = field(default_factory=list)

    def login(self, token_data: dict, server_url: str):
        self.token = token_data["access_token"]
        self.id_usuario = token_data.get("id_usuario", "")
        self.id_taller = token_data.get("id_taller", "")
        self.rol = token_data.get("rol", "")
        self.nombre = token_data.get("nombre", "")
        self.server_url = server_url
        self.logged_in = True
        self._notify()

    def logout(self):
        self.token = ""
        self.id_usuario = ""
        self.id_taller = ""
        self.rol = ""
        self.nombre = ""
        self.logged_in = False
        self._notify()

    def add_listener(self, listener: Callable):
        self._listeners.append(listener)

    def _notify(self):
        for listener in self._listeners:
            listener()


STATE = AppState()
