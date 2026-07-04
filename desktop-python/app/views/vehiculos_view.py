import customtkinter as ctk

from app.api_client import API
from app.components.table_widget import TableWidget
from app.components.form_widgets import FormEntry, FormCombo, FormDate
from app.components.dialogs import ConfirmDialog


TIPO_VEHICULO = ["MOTO", "COCHE", "FURGONETA", "CAMION", "OTRO"]
COMBUSTIBLE = ["", "GASOLINA", "DIESEL", "ELECTRICO", "HIBRIDO", "GLP"]
TRANSMISION = ["MANUAL", "AUTOMATICA", "CVT", "SEMIAUTOMATICA"]


class VehiculosView(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)

        top = ctk.CTkFrame(self, fg_color="transparent")
        top.grid(row=0, column=0, sticky="ew", pady=(0, 15))
        top.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            top,
            text="Vehículos",
            font=ctk.CTkFont(size=24, weight="bold"),
        ).grid(row=0, column=0, sticky="w")

        self.search_entry = ctk.CTkEntry(
            top, placeholder_text="Buscar por matrícula, marca...", width=300, height=35
        )
        self.search_entry.grid(row=0, column=1, padx=10)
        self.search_entry.bind("<KeyRelease>", lambda e: self._load_data())

        ctk.CTkButton(
            top, text="+ Nuevo Vehículo", height=35, command=self._crear
        ).grid(row=0, column=2)

        columns = [
            ("matricula", 120),
            ("marca", 130),
            ("modelo", 130),
            ("anio", 70),
            ("tipo_vehiculo", 100),
            ("kilometros", 90),
            ("cliente", 150),
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

        self.clientes_cache: list[dict] = []
        self._load_data()
        self._load_clientes()

    def _load_clientes(self):
        self.clientes_cache = API.list_clientes()

    def _on_select(self, row: dict):
        self.edit_btn.configure(state="normal")
        self.delete_btn.configure(state="normal")

    def _load_data(self):
        q = self.search_entry.get().strip()
        data = API.list_vehiculos(q)
        display = []
        for v in data:
            cliente_nombre = ""
            if v.get("id_cliente"):
                for c in self.clientes_cache:
                    if c.get("id") == v["id_cliente"]:
                        cliente_nombre = c.get("nombre", "")
                        break
            display.append({
                "id": v.get("id", ""),
                "matricula": v.get("matricula", ""),
                "marca": v.get("marca", ""),
                "modelo": v.get("modelo", ""),
                "anio": v.get("anio", ""),
                "tipo_vehiculo": v.get("tipo_vehiculo", ""),
                "kilometros": v.get("kilometros", ""),
                "cliente": cliente_nombre,
            })
        self.table.load(display)

    def _crear(self):
        self._open_form()

    def _editar(self, _=None):
        row = self.table.get_selected()
        if row:
            self._open_form(vehiculo_id=row["id"])

    def _eliminar(self):
        row = self.table.get_selected()
        if not row:
            return

        def confirm():
            API.delete_vehiculo(row["id"])
            self._load_data()
            self.edit_btn.configure(state="disabled")
            self.delete_btn.configure(state="disabled")

        ConfirmDialog(
            self,
            "Eliminar Vehículo",
            f"¿Eliminar {row['marca']} {row['modelo']} ({row['matricula']})?",
            on_confirm=confirm,
        )

    def _open_form(self, vehiculo_id: str | None = None):
        data = None
        if vehiculo_id:
            data = API.get_vehiculo(vehiculo_id)

        dialog = ctk.CTkToplevel(self)
        dialog.title("Editar Vehículo" if data else "Nuevo Vehículo")
        dialog.geometry("600x650")
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        scroll = ctk.CTkScrollableFrame(frame)
        scroll.pack(fill="both", expand=True)

        cliente_opts = [""] + [
            c.get("nombre", "") for c in self.clientes_cache
        ]

        cliente_w = FormCombo(scroll, "Cliente", cliente_opts)
        cliente_w.pack(fill="x", pady=2)
        matricula = FormEntry(scroll, "Matrícula")
        matricula.pack(fill="x", pady=2)
        marca = FormEntry(scroll, "Marca *")
        marca.pack(fill="x", pady=2)
        modelo = FormEntry(scroll, "Modelo *")
        modelo.pack(fill="x", pady=2)
        anio = FormEntry(scroll, "Año")
        anio.pack(fill="x", pady=2)
        tipo = FormCombo(scroll, "Tipo Vehículo", TIPO_VEHICULO)
        tipo.pack(fill="x", pady=2)
        bastidor = FormEntry(scroll, "Bastidor/VIN")
        bastidor.pack(fill="x", pady=2)
        combustible = FormCombo(scroll, "Combustible", COMBUSTIBLE)
        combustible.pack(fill="x", pady=2)
        transmision = FormCombo(scroll, "Transmisión", TRANSMISION)
        transmision.pack(fill="x", pady=2)
        cilindrada = FormEntry(scroll, "Cilindrada")
        cilindrada.pack(fill="x", pady=2)
        caballos = FormEntry(scroll, "Caballos")
        caballos.pack(fill="x", pady=2)
        color = FormEntry(scroll, "Color")
        color.pack(fill="x", pady=2)
        kilometros = FormEntry(scroll, "Kilómetros")
        kilometros.pack(fill="x", pady=2)

        if data:
            if data.get("id_cliente"):
                for c in self.clientes_cache:
                    if c.get("id") == data["id_cliente"]:
                        cliente_w.set_value(c.get("nombre", ""))
                        break
            matricula.set_value(data.get("matricula", ""))
            marca.set_value(data.get("marca", ""))
            modelo.set_value(data.get("modelo", ""))
            anio.set_value(data.get("anio", ""))
            tipo.set_value(data.get("tipo_vehiculo", "MOTO"))
            bastidor.set_value(data.get("bastidor", ""))
            combustible.set_value(data.get("tipo_combustible", ""))
            transmision.set_value(data.get("transmision", "MANUAL"))
            cilindrada.set_value(data.get("cilindrada", ""))
            caballos.set_value(data.get("caballos", ""))
            color.set_value(data.get("color", ""))
            kilometros.set_value(data.get("kilometros", ""))

        def save():
            id_cliente = None
            nombre_cliente = cliente_w.get_value()
            if nombre_cliente:
                for c in self.clientes_cache:
                    if c.get("nombre") == nombre_cliente:
                        id_cliente = c.get("id")
                        break

            payload = {
                "id_cliente": id_cliente,
                "matricula": matricula.get_value(),
                "marca": marca.get_value(),
                "modelo": modelo.get_value(),
                "anio": int(anio.get_value()) if anio.get_value().isdigit() else 0,
                "tipo_vehiculo": tipo.get_value(),
                "bastidor": bastidor.get_value(),
                "tipo_combustible": combustible.get_value(),
                "transmision": transmision.get_value(),
                "cilindrada": cilindrada.get_value(),
                "caballos": caballos.get_value(),
                "color": color.get_value(),
                "kilometros": int(kilometros.get_value()) if kilometros.get_value().isdigit() else 0,
            }
            if not payload["marca"] or not payload["modelo"]:
                return
            if vehiculo_id:
                API.update_vehiculo(vehiculo_id, payload)
            else:
                API.create_vehiculo(payload)
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
