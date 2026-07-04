import customtkinter as ctk
from datetime import datetime, timedelta

from app.api_client import API
from app.components.table_widget import TableWidget
from app.components.form_widgets import FormEntry, FormCombo, FormText
from app.components.dialogs import ConfirmDialog
from app.utils.helpers import ESTADOS_CITA, format_fecha


class CitasView(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)

        top = ctk.CTkFrame(self, fg_color="transparent")
        top.grid(row=0, column=0, sticky="ew", pady=(0, 15))
        top.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            top,
            text="Agenda / Citas",
            font=ctk.CTkFont(size=24, weight="bold"),
        ).grid(row=0, column=0, sticky="w")
        ctk.CTkButton(
            top, text="+ Nueva Cita", height=35, command=self._crear
        ).grid(row=0, column=2)

        columns = [
            ("fecha_hora", 150),
            ("cliente", 180),
            ("motivo", 200),
            ("estado", 110),
            ("mecanico", 130),
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

        self.edit_btn = ctk.CTkButton(
            bottom,
            text="Editar",
            width=100,
            state="disabled",
            command=self._editar,
        )
        self.edit_btn.grid(row=0, column=0, padx=5)
        self.delete_btn = ctk.CTkButton(
            bottom,
            text="Cancelar Cita",
            width=120,
            state="disabled",
            fg_color="#EF4444",
            hover_color="#DC2626",
            command=self._eliminar,
        )
        self.delete_btn.grid(row=0, column=1, padx=5)

        self.clientes_cache: list[dict] = []
        self.vehiculos_cache: list[dict] = []
        self._load_data()
        self._load_caches()

    def _load_caches(self):
        self.clientes_cache = API.list_clientes()
        self.vehiculos_cache = API.list_vehiculos()

    def _on_select(self, row: dict):
        self.edit_btn.configure(state="normal")
        self.delete_btn.configure(state="normal")

    def _load_data(self):
        today = datetime.now().strftime("%Y-%m-%d")
        month_end = (datetime.now() + timedelta(days=30)).strftime("%Y-%m-%d")
        data = API.list_citas(fecha_desde=today, fecha_hasta=month_end)
        display = []
        for c in data:
            display.append({
                "id": c.get("id", ""),
                "fecha_hora": format_fecha(c.get("fecha_hora", "")),
                "cliente": "",
                "motivo": c.get("motivo", ""),
                "estado": c.get("estado", ""),
                "mecanico": "",
            })
        self.table.load(display)

    def _crear(self):
        self._open_form()

    def _editar(self, _=None):
        row = self.table.get_selected()
        if row:
            self._open_form(cita_id=row["id"])

    def _eliminar(self):
        row = self.table.get_selected()
        if not row:
            return

        def confirm():
            API.delete_cita(row["id"])
            self._load_data()
            self.edit_btn.configure(state="disabled")
            self.delete_btn.configure(state="disabled")

        ConfirmDialog(
            self,
            "Cancelar Cita",
            f"¿Cancelar la cita del {row.get('fecha_hora', '')}?",
            on_confirm=confirm,
        )

    def _open_form(self, cita_id: str | None = None):
        data = None
        if cita_id:
            data = API.get_cita(cita_id) if hasattr(API, 'get_cita') else None

        dialog = ctk.CTkToplevel(self)
        dialog.title("Editar Cita" if data else "Nueva Cita")
        dialog.geometry("500x450")
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        cliente_opts = [""] + [
            c.get("nombre", "") for c in self.clientes_cache
        ]
        cliente_w = FormCombo(frame, "Cliente", cliente_opts)
        cliente_w.pack(fill="x", pady=2)

        fecha = FormEntry(frame, "Fecha (YYYY-MM-DD)")
        fecha.pack(fill="x", pady=2)
        hora = FormEntry(frame, "Hora (HH:MM)")
        hora.pack(fill="x", pady=2)
        motivo = FormEntry(frame, "Motivo")
        motivo.pack(fill="x", pady=2)
        notas = FormText(frame, "Notas", height=60)
        notas.pack(fill="x", pady=2)

        def save():
            cliente_nombre = cliente_w.get_value()
            id_cliente = None
            for c in self.clientes_cache:
                if c.get("nombre") == cliente_nombre:
                    id_cliente = c.get("id")
                    break

            fecha_str = fecha.get_value()
            hora_str = hora.get_value()
            try:
                dt = datetime.fromisoformat(f"{fecha_str}T{hora_str}:00")
            except (ValueError, TypeError):
                return

            payload = {
                "id_cliente": id_cliente,
                "fecha_hora": dt.isoformat(),
                "motivo": motivo.get_value(),
                "notas": notas.get_value(),
                "duracion_estimada": 60,
            }
            if cita_id:
                API.update_cita(cita_id, payload)
            else:
                API.create_cita(payload)
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
