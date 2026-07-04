import customtkinter as ctk

from app.api_client import API
from app.components.table_widget import TableWidget
from app.components.form_widgets import FormEntry, FormCombo
from app.components.dialogs import ConfirmDialog, FormDialog
from app.utils.helpers import format_decimal


class ArticulosView(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1)

        top = ctk.CTkFrame(self, fg_color="transparent")
        top.grid(row=0, column=0, sticky="ew", pady=(0, 15))
        top.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            top,
            text="Inventario",
            font=ctk.CTkFont(size=24, weight="bold"),
        ).grid(row=0, column=0, sticky="w")

        self.search_entry = ctk.CTkEntry(
            top, placeholder_text="Buscar artículo...", width=300, height=35
        )
        self.search_entry.grid(row=0, column=1, padx=10)
        self.search_entry.bind("<KeyRelease>", lambda e: self._load_data())

        ctk.CTkButton(
            top, text="+ Nuevo Artículo", height=35, command=self._crear
        ).grid(row=0, column=2)

        columns = [
            ("nombre", 200),
            ("sku", 100),
            ("categoria", 120),
            ("cantidad_stock", 100),
            ("stock_minimo", 100),
            ("precio_venta", 100),
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
        self.stock_btn = ctk.CTkButton(
            bottom,
            text="Ajustar Stock",
            width=120,
            state="disabled",
            command=self._ajustar_stock,
        )
        self.stock_btn.grid(row=0, column=1, padx=5)
        self.delete_btn = ctk.CTkButton(
            bottom,
            text="Eliminar",
            width=100,
            state="disabled",
            fg_color="#EF4444",
            hover_color="#DC2626",
            command=self._eliminar,
        )
        self.delete_btn.grid(row=0, column=2, padx=5)

        self._load_data()

    def _on_select(self, row: dict):
        self.edit_btn.configure(state="normal")
        self.stock_btn.configure(state="normal")
        self.delete_btn.configure(state="normal")

    def _load_data(self):
        q = self.search_entry.get().strip()
        data = API.list_articulos(q)
        display = []
        for a in data:
            display.append({
                "id": a.get("id", ""),
                "nombre": a.get("nombre", ""),
                "sku": a.get("sku", ""),
                "categoria": a.get("categoria", ""),
                "cantidad_stock": a.get("cantidad_stock", 0),
                "stock_minimo": a.get("stock_minimo", 0),
                "precio_venta": format_decimal(a.get("precio_venta")),
            })
        self.table.load(display)

    def _crear(self):
        self._open_form()

    def _editar(self, _=None):
        row = self.table.get_selected()
        if row:
            self._open_form(articulo_id=row["id"])

    def _eliminar(self):
        row = self.table.get_selected()
        if not row:
            return

        def confirm():
            API.delete_articulo(row["id"])
            self._load_data()
            self.edit_btn.configure(state="disabled")
            self.stock_btn.configure(state="disabled")
            self.delete_btn.configure(state="disabled")

        ConfirmDialog(
            self,
            "Eliminar Artículo",
            f"¿Eliminar {row['nombre']}?",
            on_confirm=confirm,
        )

    def _ajustar_stock(self):
        row = self.table.get_selected()
        if not row:
            return

        dialog = ctk.CTkToplevel(self)
        dialog.title(f"Ajustar Stock: {row['nombre']}")
        dialog.geometry("350x200")
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        ctk.CTkLabel(
            frame,
            text=f"Stock actual: {row.get('cantidad_stock', 0)}",
            font=ctk.CTkFont(size=14),
        ).pack(pady=(0, 10))

        entry = ctk.CTkEntry(
            frame, placeholder_text="Cantidad (+ añadir, - quitar)"
        )
        entry.pack(fill="x", pady=(0, 15))

        def save():
            try:
                cantidad = int(entry.get().strip())
                API.ajustar_stock(row["id"], cantidad)
                dialog.destroy()
                self._load_data()
            except ValueError:
                pass

        btn_frame = ctk.CTkFrame(frame, fg_color="transparent")
        btn_frame.pack()
        ctk.CTkButton(
            btn_frame, text="Cancelar", fg_color="gray", command=dialog.destroy
        ).pack(side="left", padx=5)
        ctk.CTkButton(btn_frame, text="Ajustar", command=save).pack(
            side="left", padx=5
        )

    def _open_form(self, articulo_id: str | None = None):
        data = None
        if articulo_id:
            data = API.get_articulo(articulo_id)

        dialog = ctk.CTkToplevel(self)
        dialog.title("Editar Artículo" if data else "Nuevo Artículo")
        dialog.geometry("500x500")
        dialog.resizable(False, False)
        dialog.transient(self)
        dialog.grab_set()

        frame = ctk.CTkFrame(dialog)
        frame.pack(fill="both", expand=True, padx=20, pady=20)

        nombre = FormEntry(frame, "Nombre *")
        nombre.pack(fill="x", pady=2)
        sku = FormEntry(frame, "SKU")
        sku.pack(fill="x", pady=2)
        categoria = FormEntry(frame, "Categoría")
        categoria.pack(fill="x", pady=2)
        cantidad = FormEntry(frame, "Stock Inicial")
        cantidad.pack(fill="x", pady=2)
        stock_min = FormEntry(frame, "Stock Mínimo")
        stock_min.pack(fill="x", pady=2)
        precio_v = FormEntry(frame, "Precio Venta")
        precio_v.pack(fill="x", pady=2)
        precio_c = FormEntry(frame, "Precio Coste")
        precio_c.pack(fill="x", pady=2)
        proveedor = FormEntry(frame, "Proveedor")
        proveedor.pack(fill="x", pady=2)

        if data:
            nombre.set_value(data.get("nombre", ""))
            sku.set_value(data.get("sku", ""))
            categoria.set_value(data.get("categoria", ""))
            cantidad.set_value(data.get("cantidad_stock", 0))
            stock_min.set_value(data.get("stock_minimo", 0))
            precio_v.set_value(data.get("precio_venta", ""))
            precio_c.set_value(data.get("precio_coste", ""))
            proveedor.set_value(data.get("proveedor", ""))

        def save():
            payload = {
                "nombre": nombre.get_value(),
                "sku": sku.get_value(),
                "categoria": categoria.get_value(),
                "cantidad_stock": int(cantidad.get_value()) if cantidad.get_value().isdigit() else 0,
                "stock_minimo": int(stock_min.get_value()) if stock_min.get_value().isdigit() else 0,
                "precio_venta": float(precio_v.get_value()) if precio_v.get_value() else None,
                "precio_coste": float(precio_c.get_value()) if precio_c.get_value() else None,
                "proveedor": proveedor.get_value(),
            }
            if not payload["nombre"]:
                return
            if articulo_id:
                API.update_articulo(articulo_id, payload)
            else:
                API.create_articulo(payload)
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
