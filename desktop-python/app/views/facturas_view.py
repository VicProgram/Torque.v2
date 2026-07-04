import customtkinter as ctk

from app.api_client import API
from app.components.table_widget import TableWidget
from app.components.form_widgets import FormEntry, FormCombo, FormDate
from app.utils.helpers import format_fecha, format_decimal, ESTADOS_FACTURA


class FacturasView(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)

        top = ctk.CTkFrame(self, fg_color="transparent")
        top.grid(row=0, column=0, sticky="ew", pady=(0, 15))
        top.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            top,
            text="Facturas",
            font=ctk.CTkFont(size=24, weight="bold"),
        ).grid(row=0, column=0, sticky="w")

        self.estado_filter = FormCombo(
            top, "", ["Todas"] + ESTADOS_FACTURA
        )
        self.estado_filter.grid(row=0, column=1, padx=5)
        self.estado_filter.combo.configure(
            command=lambda e: self._load_data(), width=150
        )

        ctk.CTkButton(
            top, text="+ Nueva Factura", height=35, command=self._crear
        ).grid(row=0, column=2, padx=5)

        columns = [
            ("numero_factura", 130),
            ("fecha_emision", 110),
            ("cliente", 180),
            ("base_imponible", 110),
            ("total", 100),
            ("estado", 100),
        ]
        self.table = TableWidget(
            self,
            columns=columns,
            on_select=self._on_select,
        )
        self.table.grid(row=1, column=0, sticky="nsew")

        bottom = ctk.CTkFrame(self, fg_color="transparent", height=50)
        bottom.grid(row=2, column=0, sticky="ew", pady=(10, 0))

        self.edit_btn = ctk.CTkButton(
            bottom,
            text="Cambiar Estado",
            width=130,
            state="disabled",
            command=self._cambiar_estado,
        )
        self.edit_btn.grid(row=0, column=0, padx=5)

        self._load_data()

    def _on_select(self, row: dict):
        self.edit_btn.configure(state="normal")

    def _load_data(self):
        filtro = self.estado_filter.get_value()
        estado = "" if filtro in ("", "Todas") else filtro
        data = API.list_facturas(estado)
        display = []
        for f in data:
            display.append({
                "id": f.get("id", ""),
                "numero_factura": f.get("numero_factura", ""),
                "fecha_emision": format_fecha(f.get("fecha_emision", "")),
                "cliente": f.get("id_orden_trabajo", "")[:8] if f.get("id_orden_trabajo") else "",
                "base_imponible": format_decimal(f.get("base_imponible")),
                "total": format_decimal(f.get("total")),
                "estado": f.get("estado", ""),
            })
        self.table.load(display)

    def _crear(self):
        ordenes = API.list_ordenes(estado="COMPLETADO")
        if not ordenes:
            return

        dialog = ctk.CTkToplevel(self)
        dialog.title("Nueva Factura")
        dialog.geometry("450x350")
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        num = FormEntry(frame, "Número Factura *")
        num.pack(fill="x", pady=2)
        base = FormEntry(frame, "Base Imponible *")
        base.pack(fill="x", pady=2)
        iva = FormEntry(frame, "IVA %")
        iva.set_value("21.00")
        iva.pack(fill="x", pady=2)

        def save():
            try:
                base_val = float(base.get_value())
                iva_val = float(iva.get_value()) if iva.get_value() else 21.0
                total = base_val * (1 + iva_val / 100)
                payload = {
                    "id_orden_trabajo": ordenes[0]["id"],
                    "numero_factura": num.get_value(),
                    "fecha_emision": datetime.now().strftime("%Y-%m-%d"),
                    "base_imponible": base_val,
                    "iva": iva_val,
                    "total": round(total, 2),
                }
                API.create_factura(payload)
                dialog.destroy()
                self._load_data()
            except ValueError:
                pass

        from datetime import datetime
        btn_frame = ctk.CTkFrame(frame, fg_color="transparent")
        btn_frame.pack(pady=(15, 0))
        ctk.CTkButton(
            btn_frame, text="Cancelar", fg_color="gray", command=dialog.destroy
        ).pack(side="left", padx=5)
        ctk.CTkButton(btn_frame, text="Crear Factura", command=save).pack(
            side="left", padx=5
        )

    def _cambiar_estado(self):
        row = self.table.get_selected()
        if not row:
            return

        dialog = ctk.CTkToplevel(self)
        dialog.title("Cambiar Estado Factura")
        dialog.geometry("350x200")
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        ctk.CTkLabel(
            frame,
            text=f"Factura: {row.get('numero_factura', '')}",
            font=ctk.CTkFont(size=14),
        ).pack(pady=(0, 10))

        combo = ctk.CTkComboBox(
            frame, values=ESTADOS_FACTURA, state="readonly"
        )
        combo.pack(fill="x", pady=(0, 15))
        combo.set(row.get("estado", "EMITIDA"))

        def save():
            API.update_factura(
                row["id"], {"estado": combo.get()}
            )
            dialog.destroy()
            self._load_data()

        btn_frame = ctk.CTkFrame(frame, fg_color="transparent")
        btn_frame.pack()
        ctk.CTkButton(
            btn_frame, text="Cancelar", fg_color="gray", command=dialog.destroy
        ).pack(side="left", padx=5)
        ctk.CTkButton(btn_frame, text="Guardar", command=save).pack(
            side="left", padx=5
        )
