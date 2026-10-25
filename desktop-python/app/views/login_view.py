import customtkinter as ctk

from app.api_client import API
from app.config import CONFIG
from app.state import STATE


class LoginView(ctk.CTkFrame):
    def __init__(self, master, on_login_success, **kwargs):
        super().__init__(master, **kwargs)
        self.on_login_success = on_login_success
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(0, weight=1)

        self.card = ctk.CTkFrame(self, corner_radius=15, width=400)
        card = self.card
        card.grid(row=0, column=0)
        card.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            card, text="TORQUE", font=ctk.CTkFont(size=32, weight="bold")
        ).grid(row=0, column=0, pady=(30, 5))
        ctk.CTkLabel(
            card,
            text="Sistema de Gestión de Taller",
            font=ctk.CTkFont(size=14),
            text_color="gray",
        ).grid(row=1, column=0, pady=(0, 25))

        self.server_entry = ctk.CTkEntry(
            card,
            placeholder_text="URL del servidor",
            height=40,
        )
        self.server_entry.grid(row=2, column=0, padx=30, pady=(0, 10), sticky="ew")
        self.server_entry.insert(0, CONFIG.server_url)

        self.email_entry = ctk.CTkEntry(
            card, placeholder_text="Email", height=40
        )
        self.email_entry.grid(row=3, column=0, padx=30, pady=(0, 10), sticky="ew")

        self.password_entry = ctk.CTkEntry(
            card, placeholder_text="Contraseña", height=40, show="*"
        )
        self.password_entry.grid(
            row=4, column=0, padx=30, pady=(0, 20), sticky="ew"
        )

        self.login_btn = ctk.CTkButton(
            card,
            text="Iniciar Sesión",
            height=45,
            font=ctk.CTkFont(size=15, weight="bold"),
            command=self._do_login,
        )
        self.login_btn.grid(row=5, column=0, padx=30, pady=(0, 10), sticky="ew")

        self.error_label = ctk.CTkLabel(
            card, text="", text_color="#EF4444", font=ctk.CTkFont(size=12)
        )
        self.error_label.grid(row=6, column=0, pady=(0, 20))

        self.pin_btn = ctk.CTkButton(
            card,
            text="Entrar con PIN",
            fg_color="transparent",
            text_color="gray",
            hover_color="#2D3748",
            command=self._show_pin_login,
        )
        self.pin_btn.grid(row=7, column=0, pady=(0, 20))

        self.bind("<Return>", lambda e: self._do_login())
        self.email_entry.focus()

    def _do_login(self):
        server_url = self.server_entry.get().strip()
        email = self.email_entry.get().strip()
        password = self.password_entry.get()

        if not server_url or not email or not password:
            self.error_label.configure(text="Todos los campos son obligatorios")
            return

        CONFIG.server_url = server_url
        API.client.base_url = f"{server_url}/api/v1"

        self.login_btn.configure(text="Conectando...", state="disabled")
        result = API.login(email, password)
        self.login_btn.configure(text="Iniciar Sesión", state="normal")

        if result:
            CONFIG.save_token(result)
            STATE.login(result, server_url)
            self.error_label.configure(text="")
            self.on_login_success()
        else:
            self.error_label.configure(
                text="Error de conexión o credenciales inválidas"
            )

    def _show_pin_login(self):
        self.email_entry.grid_remove()
        self.password_entry.grid_remove()
        self.login_btn.configure(text="Entrar con PIN", command=self._do_pin_login)

        self.pin_entry = ctk.CTkEntry(
            self.card,
            placeholder_text="PIN de mecánico",
            height=40,
            show="*",
        )
        self.pin_entry.grid(
            row=3, column=0, padx=30, pady=(0, 20), sticky="ew"
        )
        self.pin_entry.focus()

        ctk.CTkButton(
            self.card,
            text="Volver",
            fg_color="transparent",
            text_color="gray",
            hover_color="#2D3748",
            command=self._show_email_login,
        ).grid(row=7, column=0, pady=(0, 5))

    def _show_email_login(self):
        if hasattr(self, "pin_entry"):
            self.pin_entry.grid_remove()
        self.email_entry.grid()
        self.password_entry.grid()
        self.login_btn.configure(text="Iniciar Sesión", command=self._do_login)
        self.pin_btn.grid()

    def _do_pin_login(self):
        pin = self.pin_entry.get().strip()
        if not pin:
            self.error_label.configure(text="Introduce el PIN")
            return
        server_url = self.server_entry.get().strip()
        if not server_url:
            self.error_label.configure(text="Introduce la URL del servidor")
            return
        CONFIG.server_url = server_url
        API.client.base_url = f"{server_url}/api/v1"
        self.login_btn.configure(text="Conectando...", state="disabled")
        result = API.login_pin(pin)
        self.login_btn.configure(text="Entrar con PIN", state="normal")
        if result:
            CONFIG.save_token(result)
            STATE.login(result, server_url)
            self.on_login_success()
        else:
            self.error_label.configure(text="PIN inválido")
