from typing import Any
import customtkinter as ctk
from datetime import date


class FormField(ctk.CTkFrame):
    def __init__(self, master, label: str, **kwargs):
        super().__init__(master, fg_color="transparent", **kwargs)
        self.label = ctk.CTkLabel(
            self, text=label, anchor="w", font=ctk.CTkFont(size=13)
        )
        self.label.pack(anchor="w", padx=(0, 10))

    def get_value(self) -> str:
        return ""

    def set_value(self, value: Any):
        pass


class FormEntry(FormField):
    def __init__(self, master, label: str, **kwargs):
        super().__init__(master, label)
        self.entry = ctk.CTkEntry(self, height=35, **kwargs)
        self.entry.pack(fill="x", pady=(2, 10))

    def get_value(self) -> str:
        return self.entry.get().strip()

    def set_value(self, value: Any):
        if value is not None:
            self.entry.delete(0, "end")
            self.entry.insert(0, str(value))
        else:
            self.entry.delete(0, "end")


class FormCombo(FormField):
    def __init__(self, master, label: str, values: list[str], **kwargs):
        super().__init__(master, label)
        self.combo = ctk.CTkComboBox(
            self, values=values, height=35, state="readonly", **kwargs
        )
        self.combo.pack(fill="x", pady=(2, 10))

    def get_value(self) -> str:
        return self.combo.get()

    def set_value(self, value: Any):
        if value and str(value) in self.combo.cget("values"):
            self.combo.set(str(value))
        elif self.combo.cget("values"):
            self.combo.set(self.combo.cget("values")[0])


class FormText(FormField):
    def __init__(self, master, label: str, height: int = 80, **kwargs):
        super().__init__(master, label)
        self.text = ctk.CTkTextbox(self, height=height, **kwargs)
        self.text.pack(fill="x", pady=(2, 10))

    def get_value(self) -> str:
        return self.text.get("1.0", "end-1c").strip()

    def set_value(self, value: Any):
        self.text.delete("1.0", "end")
        if value:
            self.text.insert("1.0", str(value))


class FormSwitch(FormField):
    def __init__(self, master, label: str, **kwargs):
        super().__init__(master, label)
        self.switch = ctk.CTkSwitch(
            self, text="", **kwargs
        )
        self.switch.pack(anchor="w", pady=(2, 10))

    def get_value(self) -> bool:
        return bool(self.switch.get())

    def set_value(self, value: Any):
        self.switch.select() if value else self.switch.deselect()


class FormDate(FormField):
    def __init__(self, master, label: str, **kwargs):
        super().__init__(master, label)
        frame = ctk.CTkFrame(self, fg_color="transparent")
        frame.pack(fill="x", pady=(2, 10))
        self.day = ctk.CTkEntry(frame, width=50, placeholder_text="DD")
        self.day.pack(side="left", padx=(0, 5))
        self.month = ctk.CTkEntry(frame, width=50, placeholder_text="MM")
        self.month.pack(side="left", padx=(0, 5))
        self.year = ctk.CTkEntry(frame, width=80, placeholder_text="YYYY")
        self.year.pack(side="left")

    def get_value(self) -> str:
        d = self.day.get().strip()
        m = self.month.get().strip()
        y = self.year.get().strip()
        if d and m and y:
            return f"{y}-{m.zfill(2)}-{d.zfill(2)}"
        return ""

    def set_value(self, value: Any):
        if value:
            date_str = str(value)[:10]
            parts = date_str.split("-")
            if len(parts) == 3:
                y, m, d = parts
                self.day.delete(0, "end")
                self.day.insert(0, str(int(d)))
                self.month.delete(0, "end")
                self.month.insert(0, str(int(m)))
                self.year.delete(0, "end")
                self.year.insert(0, y)
