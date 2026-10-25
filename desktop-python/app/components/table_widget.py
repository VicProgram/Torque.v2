from collections.abc import Callable
from tkinter import ttk

import customtkinter as ctk


def configurar_estilo_treeview():
    style = ttk.Style()
    style.theme_use("clam")
    style.configure(
        "Treeview",
        background="#1C2331",
        foreground="white",
        rowheight=30,
        fieldbackground="#1C2331",
        borderwidth=0,
    )
    style.configure(
        "Treeview.Heading",
        background="#135BEC",
        foreground="white",
        relief="flat",
    )
    style.map(
        "Treeview",
        background=[("selected", "#135BEC")],
        foreground=[("selected", "white")],
    )


configurar_estilo_treeview()


class TableWidget(ctk.CTkFrame):
    def __init__(
        self,
        master,
        columns: list[tuple[str, int]],
        on_select: Callable[[dict], None] | None = None,
        on_double_click: Callable[[str], None] | None = None,
        **kwargs,
    ):
        super().__init__(master, **kwargs)
        self.columns = columns
        self.on_select = on_select
        self.on_double_click = on_double_click
        self.data: list[dict] = []
        self._selected_id: str | None = None

        self.tree = ttk.Treeview(
            self,
            columns=[c[0] for c in columns],
            show="headings",
            selectmode="browse",
        )

        for col_id, width in columns:
            self.tree.column(col_id, width=width, anchor="w")
            self.tree.heading(col_id, text=col_id)

        scrollbar = ctk.CTkScrollbar(self, command=self.tree.yview)
        self.tree.configure(yscrollcommand=scrollbar.set)
        self.tree.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

        self.tree.bind("<<TreeviewSelect>>", self._on_select)
        self.tree.bind("<Double-1>", self._on_double_click)

    def _on_select(self, event):
        selection = self.tree.selection()
        if selection and self.data:
            idx = self.tree.index(selection[0])
            if 0 <= idx < len(self.data):
                self._selected_id = self.data[idx].get("id")
                if self.on_select:
                    self.on_select(self.data[idx])

    def _on_double_click(self, event):
        if self._selected_id and self.on_double_click:
            self.on_double_click(self._selected_id)

    def load(self, data: list[dict]):
        self.data = data
        self._selected_id = None
        for item in self.tree.get_children():
            self.tree.delete(item)
        for row in data:
            values = []
            for col_id, _ in self.columns:
                val = row.get(col_id, "")
                values.append(str(val) if val is not None else "")
            self.tree.insert("", "end", values=values)

    def get_selected(self) -> dict | None:
        if self._selected_id:
            for row in self.data:
                if row.get("id") == self._selected_id:
                    return row
        return None

    def get_selected_id(self) -> str | None:
        return self._selected_id

    def clear_selection(self):
        self._selected_id = None
        for item in self.tree.selection():
            self.tree.selection_remove(item)
