import customtkinter as ctk
from tkinter import ttk

from app.api_client import API
from app.state import STATE
from app.utils.helpers import format_decimal


class DashboardView(ctk.CTkFrame):
    def __init__(self, master, on_navigate, **kwargs):
        super().__init__(master, **kwargs)
        self.on_navigate = on_navigate
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)

        ctk.CTkLabel(
            self,
            text=f"Panel de Control — {STATE.nombre}",
            font=ctk.CTkFont(size=24, weight="bold"),
        ).grid(row=0, column=0, pady=(0, 20), sticky="w")

        self.kpi_frame = ctk.CTkFrame(self)
        self.kpi_frame.grid(row=1, column=0, sticky="ew", pady=(0, 20))
        self.kpi_frame.grid_columnconfigure(
            (0, 1, 2, 3, 4), weight=1
        )

        self.kpi_labels = {}
        kpi_config = [
            ("ordenes_activas", "Órdenes Activas", "#3B82F6"),
            ("ordenes_en_curso", "En Curso", "#EAB308"),
            ("ordenes_presupuesto", "Presupuestos", "#8B5CF6"),
            ("citas_hoy", "Citas Hoy", "#22C55E"),
            ("ingresos_mes", "Ingresos Mes", "#135BEC"),
        ]

        for i, (key, label, color) in enumerate(kpi_config):
            card = ctk.CTkFrame(
                self.kpi_frame, fg_color=color, corner_radius=12
            )
            card.grid(row=0, column=i, padx=5, sticky="nsew")
            ctk.CTkLabel(
                card,
                text=label,
                font=ctk.CTkFont(size=12),
                text_color="white",
            ).pack(pady=(10, 2))
            value_label = ctk.CTkLabel(
                card,
                text="—",
                font=ctk.CTkFont(size=28, weight="bold"),
                text_color="white",
            )
            value_label.pack(pady=(0, 10))
            self.kpi_labels[key] = value_label

        bottom = ctk.CTkFrame(self)
        bottom.grid(row=2, column=0, sticky="nsew")
        bottom.grid_columnconfigure(0, weight=1)
        bottom.grid_rowconfigure(0, weight=1)

        ctk.CTkLabel(
            bottom,
            text="Órdenes Activas",
            font=ctk.CTkFont(size=16, weight="bold"),
        ).grid(row=0, column=0, pady=(0, 10), sticky="w")

        columns = [
            ("vehiculo", 150),
            ("problema", 250),
            ("estado", 120),
            ("mecanico", 150),
        ]
        self.tree = ttk.Treeview(
            bottom, columns=[c[0] for c in columns], show="headings", height=8
        )
        style = ttk.Style()
        style.configure(
            "Treeview",
            background="#1C2331",
            foreground="white",
            rowheight=30,
            fieldbackground="#1C2331",
        )
        style.configure(
            "Treeview.Heading",
            background="#135BEC",
            foreground="white",
        )
        style.map(
            "Treeview",
            background=[("selected", "#135BEC")],
            foreground=[("selected", "white")],
        )

        for col_id, width in columns:
            self.tree.column(col_id, width=width)
            self.tree.heading(col_id, text=col_id)

        scrollbar = ctk.CTkScrollbar(
            bottom, command=self.tree.yview
        )
        self.tree.configure(yscrollcommand=scrollbar.set)
        self.tree.grid(row=1, column=0, sticky="nsew")
        scrollbar.grid(row=1, column=1, sticky="ns")

        self.tree.bind("<Double-1>", lambda e: on_navigate("ordenes"))

        self.load_data()

    def load_data(self):
        resumen = API.dashboard_resumen()
        if resumen:
            for key, label in self.kpi_labels.items():
                val = resumen.get(key, "—")
                if key == "ingresos_mes":
                    label.configure(text=format_decimal(val))
                else:
                    label.configure(text=str(val))

        ordenes = API.dashboard_ordenes_activas()
        for item in self.tree.get_children():
            self.tree.delete(item)
        for row in ordenes:
            self.tree.insert(
                "",
                "end",
                values=(
                    row.get("vehiculo", ""),
                    row.get("problema", "")[:50],
                    row.get("estado", ""),
                    row.get("mecanico", ""),
                ),
            )
