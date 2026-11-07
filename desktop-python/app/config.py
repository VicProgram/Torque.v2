import json
import logging
import os
import stat
from dataclasses import dataclass, field
from urllib.parse import urlparse

logger = logging.getLogger(__name__)

LOOPBACK_HOSTS = {"localhost", "127.0.0.1", "::1"}


@dataclass
class Config:
    server_url: str = field(
        default_factory=lambda: os.environ.get(
            "TORQUE_SERVER_URL", "http://localhost:8000"
        )
    )
    api_prefix: str = "/api/v1"
    token_file: str = field(
        default_factory=lambda: os.path.expanduser("~/.torque/token.json")
    )

    def __post_init__(self):
        self.server_url = self.server_url.rstrip("/")
        parsed = urlparse(self.server_url)
        if parsed.scheme not in {"http", "https"} or not parsed.netloc:
            raise ValueError(
                "TORQUE_SERVER_URL must be an absolute http(s) URL, "
                f"got {self.server_url!r}"
            )
        if parsed.scheme == "http" and (parsed.hostname or "") not in LOOPBACK_HOSTS:
            logger.warning(
                "Desktop client is using cleartext HTTP against %s. "
                "Use HTTPS outside local development.",
                parsed.hostname,
            )

    @property
    def api_url(self) -> str:
        return f"{self.server_url}{self.api_prefix}"

    def save_token(self, token_data: dict):
        directory = os.path.dirname(self.token_file)
        os.makedirs(directory, mode=0o700, exist_ok=True)
        fd = os.open(self.token_file, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
        with os.fdopen(fd, "w") as f:
            json.dump(token_data, f)

    def load_token(self) -> dict | None:
        try:
            self._restrict_token_permissions()
            with open(self.token_file) as f:
                return json.load(f)
        except (FileNotFoundError, json.JSONDecodeError, OSError):
            return None

    def clear_token(self):
        try:
            os.remove(self.token_file)
        except FileNotFoundError:
            pass

    def _restrict_token_permissions(self):
        """Prevent other local users from reading a token written by older builds."""
        if os.name != "posix":
            return
        mode = stat.S_IMODE(os.stat(self.token_file).st_mode)
        if mode & 0o077:
            os.chmod(self.token_file, 0o600)


CONFIG = Config()
