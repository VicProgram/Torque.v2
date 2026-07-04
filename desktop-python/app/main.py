import customtkinter as ctk

from app.config import CONFIG
from app.state import STATE
from app.api_client import API
from app.components.sidebar import Sidebar
from app.views.login_view import LoginView
from app.views.dashboard_view import DashboardView
from app.views.clientes_view import ClientesView
from app.views.vehiculos_view import VehiculosView
from app.views.ordenes_view import OrdenesView
from app.views.articulos_view import ArticulosView
from app.views.citas_view import CitasView
from app.views.facturas_view import FacturasView

ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")


class TorqueDesktop(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title("Torque Workshop — Gestión de Taller")
        self.geometry("1280x800")
        self.minsize(1024, 600)

        self.grid_columnconfigure(0, weight=0)
        self.grid_columnconfigure(1, weight=1)
        self.grid_rowconfigure(0, weight=1)

        self.sidebar: Sidebar | None = None
        self.views: dict[str, ctk.CTkFrame] = {}
        self.current_view: str = ""

        self._check_saved_session()

    def _check_saved_session(self):
        token_data = CONFIG.load_token()
        if token_data and token_data.get("access_token"):
            API.set_token(token_data["access_token"])
            user = API.get_me()
            if user:
                STATE.login(token_data, CONFIG.server_url)
                self._init_main_view()
                return
        self._init_login_view()

    def _init_login_view(self):
        self.login_view = LoginView(self, on_login_success=self._on_login)
        self.login_view.grid(row=0, column=0, columnspan=2, sticky="nsew")

    def _on_login(self):
        self.login_view.grid_remove()
        self._init_main_view()

    def _init_main_view(self):
        self.sidebar = Sidebar(
            self,
            on_navigate=self._navigate,
            on_logout=self._logout,
        )
        self.sidebar.grid(row=0, column=0, sticky="ns")

        self._create_views()
        self._navigate("dashboard")

    def _create_views(self):
        self.views = {
            "dashboard": DashboardView(self, on_navigate=self._navigate),
            "clientes": ClientesView(self),
            "vehiculos": VehiculosView(self),
            "ordenes": OrdenesView(self),
            "articulos": ArticulosView(self),
            "citas": CitasView(self),
            "facturas": FacturasView(self),
        }

    def _navigate(self, view_key: str):
        if self.current_view and self.current_view in self.views:
            self.views[self.current_view].grid_remove()

        if view_key in self.views:
            self.views[view_key].grid(
                row=0, column=1, sticky="nsew", padx=(15, 15), pady=15
            )
            self.current_view = view_key
            if self.sidebar:
                self.sidebar.highlight(view_key)

            view = self.views[view_key]
            if hasattr(view, "load_data"):
                view.load_data()

    def _logout(self):
        CONFIG.clear_token()
        STATE.logout()
        API.clear_token()
        if self.sidebar:
            self.sidebar.grid_remove()
        for v in self.views.values():
            v.grid_remove()
        self._init_login_view()


if __name__ == "__main__":
    app = TorqueDesktop()
    app.mainloop()
