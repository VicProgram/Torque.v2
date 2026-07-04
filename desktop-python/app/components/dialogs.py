from typing import Any
import customtkinter as ctk


class ConfirmDialog(ctk.CTkToplevel):
    def __init__(self, master, title: str, message: str, on_confirm=None):
        super().__init__(master)
        self.title(title)
        self.geometry("400x200")
        self.resizable(False, False)
        self.on_confirm = on_confirm
        self.result = False

        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(0, weight=1)

        frame = ctk.CTkFrame(self)
        frame.grid(row=0, column=0, sticky="nsew", padx=20, pady=20)
        frame.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            frame, text=message, font=ctk.CTkFont(size=14), wraplength=350
        ).pack(pady=(20, 20))

        btn_frame = ctk.CTkFrame(frame, fg_color="transparent")
        btn_frame.pack(pady=(0, 10))
        ctk.CTkButton(
            btn_frame,
            text="Cancelar",
            fg_color="gray",
            command=self._cancel,
        ).pack(side="left", padx=5)
        ctk.CTkButton(
            btn_frame,
            text="Confirmar",
            fg_color="#EF4444",
            command=self._confirm,
        ).pack(side="left", padx=5)

        self.transient(master)
        self.grab_set()
        self.wait_window()

    def _confirm(self):
        self.result = True
        if self.on_confirm:
            self.on_confirm()
        self.destroy()

    def _cancel(self):
        self.destroy()


class FormDialog(ctk.CTkToplevel):
    def __init__(
        self,
        master,
        title: str,
        fields: list[dict],
        on_save: callable = None,
        width: int = 500,
        height: int = 400,
    ):
        super().__init__(master)
        self.title(title)
        self.geometry(f"{width}x{height}")
        self.resizable(False, False)
        self.on_save = on_save
        self.fields_widgets: list = []
        self.result: dict | None = None

        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(0, weight=1)

        main = ctk.CTkFrame(self)
        main.grid(row=0, column=0, sticky="nsew", padx=20, pady=20)
        main.grid_columnconfigure(0, weight=1)

        from app.components.form_widgets import FormEntry, FormCombo, FormText

        for i, field in enumerate(fields):
            label = field.get("label", "")
            kind = field.get("type", "entry")
            if kind == "entry":
                w = FormEntry(main, label)
            elif kind == "combo":
                w = FormCombo(main, label, field.get("values", []))
            elif kind == "text":
                w = FormText(main, label, height=80)
            else:
                w = FormEntry(main, label)
            w.pack(fill="x", pady=2)
            if "value" in field and field["value"] is not None:
                w.set_value(field["value"])
            self.fields_widgets.append((field.get("key", ""), w))

        btn_frame = ctk.CTkFrame(main, fg_color="transparent")
        btn_frame.pack(pady=(15, 0))
        ctk.CTkButton(
            btn_frame, text="Cancelar", fg_color="gray", command=self._cancel
        ).pack(side="left", padx=5)
        ctk.CTkButton(
            btn_frame, text="Guardar", command=self._save
        ).pack(side="left", padx=5)

        self.transient(master)
        self.grab_set()

    def _save(self):
        self.result = {}
        for key, widget in self.fields_widgets:
            self.result[key] = widget.get_value()
        if self.on_save:
            self.on_save(self.result)
        self.destroy()

    def _cancel(self):
        self.destroy()
