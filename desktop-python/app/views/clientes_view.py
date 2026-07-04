import customtkinter as ctk

from app.api_client import API
from app.components.table_widget import TableWidget
from app.components.form_widgets import FormEntry, FormText
from app.components.dialogs import ConfirmDialog


class ClientesView(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)

        top = ctk.CTkFrame(self, fg_color="transparent")
        top.grid(row=0, column=0, sticky="ew", pady=(0, 15))
        top.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            top,
            text="Clientes",
            font=ctk.CTkFont(size=24, weight="bold"),
        ).grid(row=0, column=0, sticky="w")

        self.search_entry = ctk.CTkEntry(
            top, placeholder_text="Buscar cliente...", width=300, height=35
        )
        self.search_entry.grid(row=0, column=1, padx=10)
        self.search_entry.bind(
            "<KeyRelease>", lambda e: self._load_data()
        )

        ctk.CTkButton(
            top,
            text="+ Nuevo Cliente",
            height=35,
            command=self._crear,
        ).grid(row=0, column=2)

        columns = [
            ("nombre", 200),
            ("telefono", 130),
            ("email", 200),
            ("nif", 120),
        ]
        self.table = TableWidget(
            self,
            columns=columns,
            on_select=self._on_select,
            on_double_click=self._editar,
        )
        self.table.grid(row=1, column=0, sticky="nsew")

        bottom = ctk.CTkFrame(self, fg_color="transparent", height=50)
        bottom.grid(row=2, column=0, sticky="ew", pady=(10, 0))
        bottom.grid_columnconfigure(0, weight=1)

        self.edit_btn = ctk.CTkButton(
            bottom, text="Editar", width=100, state="disabled", command=self._editar
        )
        self.edit_btn.grid(row=0, column=0, padx=5)
        self.delete_btn = ctk.CTkButton(
            bottom,
            text="Eliminar",
            width=100,
            state="disabled",
            fg_color="#EF4444",
            hover_color="#DC2626",
            command=self._eliminar,
        )
        self.delete_btn.grid(row=0, column=1, padx=5)

        self._load_data()

    def _on_select(self, row: dict):
        self.edit_btn.configure(state="normal")
        self.delete_btn.configure(state="normal")

    def _load_data(self):
        q = self.search_entry.get().strip()
        data = API.list_clientes(q)
        display = []
        for c in data:
            display.append({
                "id": c.get("id", ""),
                "nombre": c.get("nombre", ""),
                "telefono": c.get("telefono", ""),
                "email": c.get("email", ""),
                "nif": c.get("nif", ""),
            })
        self.table.load(display)

    def _crear(self):
        self._open_form()

    def _editar(self, _=None):
        row = self.table.get_selected()
        if row:
            self._open_form(cliente_id=row["id"])

    def _eliminar(self):
        row = self.table.get_selected()
        if not row:
            return

        def confirm():
            API.delete_cliente(row["id"])
            self._load_data()
            self.edit_btn.configure(state="disabled")
            self.delete_btn.configure(state="disabled")

        ConfirmDialog(
            self,
            "Eliminar Cliente",
            f"¿Eliminar a {row['nombre']}?",
            on_confirm=confirm,
        )

    def _open_form(self, cliente_id: str | None = None):
        data = None
        if cliente_id:
            data = API.get_cliente(cliente_id)

        dialog = ctk.CTkToplevel(self)
        dialog.title("Editar Cliente" if data else "Nuevo Cliente")
        dialog.geometry("500x450")
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        nombre = FormEntry(frame, "Nombre *")
        nombre.pack(fill="x", pady=2)
        telefono = FormEntry(frame, "Teléfono")
        telefono.pack(fill="x", pady=2)
        email_w = FormEntry(frame, "Email")
        email_w.pack(fill="x", pady=2)
        nif = FormEntry(frame, "NIF")
        nif.pack(fill="x", pady=2)
        notas = FormText(frame, "Notas", height=60)
        notas.pack(fill="x", pady=2)

        if data:
            nombre.set_value(data.get("nombre", ""))
            telefono.set_value(data.get("telefono", ""))
            email_w.set_value(data.get("email", ""))
            nif.set_value(data.get("nif", ""))
            notas.set_value(data.get("notas", ""))

        def save():
            payload = {
                "nombre": nombre.get_value(),
                "telefono": telefono.get_value(),
                "email": email_w.get_value(),
                "nif": nif.get_value(),
                "notas": notas.get_value(),
            }
            if not payload["nombre"]:
                return
            if cliente_id:
                API.update_cliente(cliente_id, payload)
            else:
                API.create_cliente(payload)
            dialog.destroy()
            self._load_data()

        btn_frame = ctk.CTkFrame(frame, fg_color="transparent")
        btn_frame.pack(pady=(15, 0))
        ctk.CTkButton(
            btn_frame, text="Cancelar", fg_color="gray", command=dialog.destroy
        ).pack(side="left", padx=5)
        ctk.CTkButton(btn_frame, text="Guardar", command=save).pack(
            side="left", padx=5
        )
