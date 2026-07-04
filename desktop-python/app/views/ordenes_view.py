import customtkinter as ctk
from tkinter import ttk

from app.api_client import API
from app.components.table_widget import TableWidget
from app.components.form_widgets import FormEntry, FormCombo, FormText
from app.components.dialogs import ConfirmDialog
from app.utils.helpers import (
    ESTADOS_ORDEN, COLOR_ESTADOS, format_fecha, format_decimal,
)


class OrdenesView(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)
        self.vehiculos_cache: list[dict] = []
        self.mecanicos_cache: dict[str, str] = {}

        top = ctk.CTkFrame(self, fg_color="transparent")
        top.grid(row=0, column=0, sticky="ew", pady=(0, 15))
        top.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            top,
            text="Órdenes de Trabajo",
            font=ctk.CTkFont(size=24, weight="bold"),
        ).grid(row=0, column=0, sticky="w")

        self.estado_filter = FormCombo(
            top, "", ["Todas"] + ESTADOS_ORDEN
        )
        self.estado_filter.grid(row=0, column=1, padx=5)
        self.estado_filter.combo.configure(
            command=lambda e: self._load_data(), width=150
        )

        ctk.CTkButton(
            top, text="+ Nueva Orden", height=35, command=self._crear
        ).grid(row=0, column=2, padx=5)

        columns = [
            ("vehiculo", 150),
            ("estado", 110),
            ("problema", 200),
            ("mecanico", 130),
            ("fecha", 110),
            ("total", 100),
        ]
        self.table = TableWidget(
            self,
            columns=columns,
            on_select=self._on_select,
            on_double_click=self._ver_detalle,
        )
        self.table.grid(row=1, column=0, sticky="nsew")

        bottom = ctk.CTkFrame(self, fg_color="transparent", height=50)
        bottom.grid(row=2, column=0, sticky="ew", pady=(10, 0))

        self.detalle_btn = ctk.CTkButton(
            bottom,
            text="Ver Detalle",
            width=100,
            state="disabled",
            command=self._ver_detalle,
        )
        self.detalle_btn.grid(row=0, column=0, padx=5)

        self.estado_btn = ctk.CTkButton(
            bottom,
            text="Cambiar Estado",
            width=130,
            state="disabled",
            command=self._cambiar_estado,
        )
        self.estado_btn.grid(row=0, column=1, padx=5)

        self._load_caches()
        self._load_data()

    def _load_caches(self):
        self.vehiculos_cache = API.list_vehiculos()
        resumen = API.dashboard_resumen()
        if resumen:
            ordenes = API.dashboard_ordenes_activas()
            for o in ordenes:
                if o.get("mecanico") and o.get("mecanico") not in self.mecanicos_cache.values():
                    pass  # keep using raw name from server

    def _on_select(self, row: dict):
        self.detalle_btn.configure(state="normal")
        self.estado_btn.configure(state="normal")

    def _load_data(self):
        filtro = self.estado_filter.get_value()
        estado = "" if filtro in ("", "Todas") else filtro
        data = API.list_ordenes(estado=estado)
        display = []
        for o in data:
            vehiculo_str = o.get("id_vehiculo", "")[:8]
            for v in self.vehiculos_cache:
                if v.get("id") == o.get("id_vehiculo"):
                    vehiculo_str = f"{v.get('marca', '')} {v.get('modelo', '')} ({v.get('matricula', '')})"
                    break
            display.append({
                "id": o.get("id", ""),
                "vehiculo": vehiculo_str,
                "estado": o.get("estado", ""),
                "problema": (o.get("descripcion_problema", "") or "")[:50],
                "mecanico": o.get("id_mecanico_asignado", "") or "",
                "fecha": format_fecha(o.get("fecha_entrada", "")),
                "total": format_decimal(o.get("presupuesto_total")),
            })
        self.table.load(display)

    def _crear(self):
        vehiculos = API.list_vehiculos()
        vehiculo_opts = [
            f"{v.get('marca', '')} {v.get('modelo', '')} ({v.get('matricula', '')})"
            for v in vehiculos
        ]

        dialog = ctk.CTkToplevel(self)
        dialog.title("Nueva Orden de Trabajo")
        dialog.geometry("550x500")
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        scroll = ctk.CTkScrollableFrame(frame)
        scroll.pack(fill="both", expand=True)

        v_w = FormCombo(scroll, "Vehículo *", vehiculo_opts)
        v_w.pack(fill="x", pady=2)
        problema = FormText(scroll, "Descripción del Problema", height=80)
        problema.pack(fill="x", pady=2)
        notas = FormText(scroll, "Notas Internas", height=60)
        notas.pack(fill="x", pady=2)

        def save():
            idx = vehiculo_opts.index(v_w.get_value()) if v_w.get_value() in vehiculo_opts else -1
            if idx < 0 or not problema.get_value():
                return
            payload = {
                "id_vehiculo": vehiculos[idx]["id"],
                "descripcion_problema": problema.get_value(),
                "notas_internas": notas.get_value(),
            }
            API.create_orden(payload)
            dialog.destroy()
            self._load_data()

        btn_frame = ctk.CTkFrame(frame, fg_color="transparent")
        btn_frame.pack(pady=(15, 0))
        ctk.CTkButton(
            btn_frame, text="Cancelar", fg_color="gray", command=dialog.destroy
        ).pack(side="left", padx=5)
        ctk.CTkButton(btn_frame, text="Crear Orden", command=save).pack(
            side="left", padx=5
        )

    def _ver_detalle(self, _=None):
        row = self.table.get_selected()
        if not row:
            return
        orden = API.get_orden(row["id"])
        if not orden:
            return

        dialog = ctk.CTkToplevel(self)
        dialog.title(f"Orden #{str(orden.get('id', ''))[:8]}")
        dialog.geometry("700x550")
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        ctk.CTkLabel(
            frame,
            text=f"Estado: {orden.get('estado', '')}",
            font=ctk.CTkFont(size=16, weight="bold"),
            text_color=COLOR_ESTADOS.get(orden.get("estado", ""), "white"),
        ).pack(anchor="w")

        ctk.CTkLabel(
            frame,
            text=f"Problema: {orden.get('descripcion_problema', '')}",
            font=ctk.CTkFont(size=13),
        ).pack(anchor="w", pady=(5, 0))

        ctk.CTkLabel(
            frame,
            text=f"Diagnóstico: {orden.get('diagnostico', '')}",
            font=ctk.CTkFont(size=13),
        ).pack(anchor="w")

        if orden.get("presupuesto_total"):
            ctk.CTkLabel(
                frame,
                text=f"Presupuesto: {format_decimal(orden.get('presupuesto_total'))}",
                font=ctk.CTkFont(size=13),
            ).pack(anchor="w")

        ctk.CTkLabel(
            frame,
            text="\nDetalles:",
            font=ctk.CTkFont(size=14, weight="bold"),
        ).pack(anchor="w", pady=(10, 5))

        detalles = orden.get("detalles", [])
        if detalles:
            columns = [("descripcion", 300), ("estado", 100), ("precio", 100)]
            from app.components.table_widget import configurar_estilo_treeview
            tree = ttk.Treeview(
                frame, columns=[c[0] for c in columns], show="headings", height=5
            )
            configurar_estilo_treeview()
            for col_id, width in columns:
                tree.column(col_id, width=width)
                tree.heading(col_id, text=col_id)
            for d in detalles:
                tree.insert(
                    "",
                    "end",
                    values=(
                        d.get("descripcion", ""),
                        d.get("estado", ""),
                        format_decimal(d.get("precio_real") or d.get("precio_estimado")),
                    ),
                )
            tree.pack(fill="x")
        else:
            ctk.CTkLabel(
                frame, text="Sin detalles", text_color="gray"
            ).pack(anchor="w")

        ctk.CTkButton(
            frame, text="Cerrar", command=dialog.destroy
        ).pack(pady=(15, 0))

    def _cambiar_estado(self):
        row = self.table.get_selected()
        if not row:
            return
        current = row.get("estado", "")
        disponibles = [e for e in ESTADOS_ORDEN if e != current]

        dialog = ctk.CTkToplevel(self)
        dialog.title("Cambiar Estado")
        dialog.geometry("350x200")
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        ctk.CTkLabel(
            frame,
            text=f"Estado actual: {current}",
            font=ctk.CTkFont(size=14),
        ).pack(pady=(0, 10))

        combo = ctk.CTkComboBox(
            frame, values=disponibles, state="readonly"
        )
        combo.pack(fill="x", pady=(0, 15))
        if disponibles:
            combo.set(disponibles[0])

        def save():
            API.cambiar_estado_orden(
                self.table.get_selected_id(), combo.get()
            )
            dialog.destroy()
            self._load_data()

        btn_frame = ctk.CTkFrame(frame, fg_color="transparent")
        btn_frame.pack()
        ctk.CTkButton(
            btn_frame, text="Cancelar", fg_color="gray", command=dialog.destroy
        ).pack(side="left", padx=5)
        ctk.CTkButton(btn_frame, text="Cambiar", command=save).pack(
            side="left", padx=5
        )
