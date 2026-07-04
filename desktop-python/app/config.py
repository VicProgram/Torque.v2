from dataclasses import dataclass, field
import os
import json


@dataclass
class Config:
    server_url: str = "http://localhost:8000"
    api_prefix: str = "/api/v1"
    token_file: str = field(
        default_factory=lambda: os.path.expanduser("~/.torque/token.json")
    )

    @property
    def api_url(self) -> str:
        return f"{self.server_url}{self.api_prefix}"

    def save_token(self, token_data: dict):
        os.makedirs(os.path.dirname(self.token_file), exist_ok=True)
        fd = os.open(self.token_file, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
        with os.fdopen(fd, "w") as f:
            json.dump(token_data, f)

    def load_token(self) -> dict | None:
        try:
            with open(self.token_file) as f:
                return json.load(f)
        except (FileNotFoundError, json.JSONDecodeError):
            return None

    def clear_token(self):
        try:
            os.remove(self.token_file)
        except FileNotFoundError:
            pass


CONFIG = Config()
