import customtkinter as ctk


class Sidebar(ctk.CTkFrame):
    def __init__(self, master, on_navigate, on_logout, **kwargs):
        super().__init__(master, width=200, corner_radius=0, **kwargs)
        self.on_navigate = on_navigate
        self.pack_propagate(False)

        self.title_label = ctk.CTkLabel(
            self, text="TORQUE", font=ctk.CTkFont(size=20, weight="bold")
        )
        self.title_label.pack(pady=(20, 30))

        self.nav_buttons: dict[str, ctk.CTkButton] = {}
        items = [
            ("📊 Panel", "dashboard"),
            ("👥 Clientes", "clientes"),
            ("🚗 Vehículos", "vehiculos"),
            ("📋 Órdenes", "ordenes"),
            ("📦 Inventario", "articulos"),
            ("📅 Agenda", "citas"),
            ("💰 Facturas", "facturas"),
        ]

        for text, key in items:
            btn = ctk.CTkButton(
                self,
                text=text,
                anchor="w",
                fg_color="transparent",
                hover_color=("#3B3B3B", "#2D3748"),
                command=lambda k=key: self._navigate(k),
            )
            btn.pack(fill="x", padx=10, pady=2)
            self.nav_buttons[key] = btn

        self.logout_btn = ctk.CTkButton(
            self,
            text="🚪 Salir",
            anchor="w",
            fg_color="transparent",
            hover_color="#EF4444",
            text_color="#EF4444",
            command=on_logout,
        )
        self.logout_btn.pack(side="bottom", fill="x", padx=10, pady=(0, 20))

    def _navigate(self, key: str):
        for btn in self.nav_buttons.values():
            btn.configure(fg_color="transparent")
        self.nav_buttons[key].configure(fg_color=("#3B3B3B", "#2D3748"))
        self.on_navigate(key)

    def highlight(self, key: str):
        for k, btn in self.nav_buttons.items():
            btn.configure(
                fg_color=("#3B3B3B", "#2D3748") if k == key else "transparent"
            )
