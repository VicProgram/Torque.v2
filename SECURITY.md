# Security Policy

Torque is a workshop-management platform composed of a FastAPI backend, an
Android client and a Python desktop client. This document describes how to
report vulnerabilities, which versions receive fixes and the security controls
that are implemented in the repository.

The SOC 2 control mapping, incident-response process and change-management
process live in:

- [`docs/SOC2_HARDENING.md`](docs/SOC2_HARDENING.md)
- [`docs/INCIDENT_RESPONSE.md`](docs/INCIDENT_RESPONSE.md)
- [`docs/CHANGE_MANAGEMENT.md`](docs/CHANGE_MANAGEMENT.md)

SOC 2 is an attestation of organizational controls, not a product feature. The
documents above separate what the code enforces from the controls that must be
operated by the organization (access reviews, backups, vendor management,
penetration tests, etc.).

## Reporting a vulnerability

**Do not open a public GitHub issue for a security problem.**

Use GitHub's private vulnerability reporting flow:

1. Open the repository **Security** tab.
2. Select **Report a vulnerability** (GitHub Security Advisories).
3. Include the affected component (`server`, `app`, `data`, `desktop-python`),
   the commit or version, reproduction steps, impact and any suggested fix.

Please test only against a local instance with synthetic data. Do not access,
modify or exfiltrate real customer, employee or workshop data.

Response targets (best effort, not a contractual SLA):

| Stage | Target |
|---|---|
| Acknowledgement | 3 business days |
| Initial severity assessment | 10 business days |
| Critical/High fix or mitigation | 30 days |
| Medium/Low fix | Next regular release |

There is currently no bug-bounty program. Reporters are credited in the
advisory unless they ask to remain anonymous.

## Supported versions

The project does not maintain long-lived release branches yet. Security fixes
are applied to the latest commit on `main` and shipped in the next build. Older
APKs, desktop bundles or server images are not supported.

## Implemented security controls

### Authentication and sessions

- Passwords are hashed with bcrypt (cost 12). Password policy: 8–128 characters,
  at least one letter and one digit.
- New PINs are exactly six digits and are stored as a bcrypt hash plus a
  keyed HMAC-SHA256 lookup value (`PIN_PEPPER`, falling back to `SECRET_KEY`).
  Legacy four-to-six-digit PINs remain accepted and are transparently upgraded
  to the hashed format after a successful login.
- JWTs are signed with HS256 and validated for `exp`, `nbf`, `iat`, `iss`,
  `aud`, subject and token type. Tokens include a unique `jti`.
- Every user has a `token_version`. Changing the PIN or password and calling
  `/auth/logout` increments it, revoking all previously issued tokens.
- Failed logins are rate-limited per client IP and lock the account after the
  configured threshold. Authentication failures return generic messages to
  avoid account enumeration; a dummy bcrypt verification equalises timing for
  unknown accounts.
- The WebSocket endpoint requires a valid token, checks tenant ownership, the
  active-user flag and the token version, and closes with code `1008` on
  rejection. Tokens are never written to Uvicorn access logs.

### Authorization and tenant isolation

- Every workshop-owned record is scoped by `id_taller` in the query layer.
- Destructive operations and invoice creation/update require the `ADMIN` role.
- RBAC denials and authentication failures are written to the audit trail.

### Audit trail

- `audit_logs` records authentication, authorization, password/PIN changes and
  every ORM create/update/delete performed during an authenticated request.
- Rows include actor, workshop, entity, result, client IP, user agent and
  request ID. Secret values and password/PIN hashes are never stored.
- The API exposes a read-only, `ADMIN`-only, tenant-scoped audit listing at
  `GET /api/v1/auditoria/eventos`.
- Structured JSON application logs include request IDs but never tokens,
  passwords, PINs or query strings.

### Transport and browser hardening

- Production refuses to start without a strong `SECRET_KEY`, explicit
  `TRUSTED_HOSTS`, `FORCE_HTTPS=true` and `DEBUG=false`.
- The backend sends HSTS, `X-Content-Type-Options`, `X-Frame-Options`,
  `Referrer-Policy`, a restrictive Content-Security-Policy, COOP/CORP and
  `Cache-Control: no-store` on authentication responses.
- CORS requires explicit origins; wildcard origins are rejected.
- Android release builds use a network security configuration that forbids
  cleartext traffic. Cleartext is allowed only in debug builds. The release
  default server URL is empty so an HTTPS endpoint must be configured.
- The desktop client disables HTTP redirects so the bearer token cannot be
  forwarded to another host, stores its token with mode `0600` (and repairs
  permissions written by older builds) and warns when a non-loopback server
  is configured over plain HTTP.

### Input, abuse and data protection

- Request bodies are capped at `MAX_REQUEST_BYTES` (10 MiB by default) and the
  limit should be mirrored at the reverse proxy.
- Pydantic schemas validate and bound all external input; SQLAlchemy uses
  parameterised queries.
- Login, PIN login and registration are rate-limited. The in-process limiter is
  suitable for a single worker; multi-worker or multi-host deployments must use
  a shared limiter (Redis or the API gateway).
- SQLite runs with foreign keys and WAL enabled. The engine uses pool
  pre-ping, recycle and timeouts. PostgreSQL is required for production.
- Database backups, encryption at rest and key management are deployment
  responsibilities and are documented in the SOC 2 mapping.

### Supply chain and CI

- `bandit`, `ruff` and `pytest` run in CI. `pip-audit` audits server and desktop
  dependencies. `alembic check` fails the build if models and migrations drift.
- Dependabot opens weekly grouped update PRs for pip, Gradle and GitHub Actions.
- `python-jose`/`ecdsa` were removed in favour of PyJWT to eliminate known
  dependency vulnerabilities.

## Secure deployment checklist

- [ ] Generate unique `SECRET_KEY` and `PIN_PEPPER` values with a CSPRNG and
      store them in a secrets manager, never in the repository or an image.
      `PIN_PEPPER` must be dedicated (not empty): an empty pepper makes
      `SECRET_KEY` the PIN pepper, so rotating the JWT secret would also
      invalidate every stored PIN lookup.
- [ ] Set `ENVIRONMENT=production`, `DEBUG=false`, `ENABLE_DOCS=false`.
- [ ] Set explicit `TRUSTED_HOSTS` and `ALLOWED_ORIGINS`; do not use `*`.
- [ ] Terminate TLS at a trusted reverse proxy and keep `FORCE_HTTPS=true`.
- [ ] Use PostgreSQL with a dedicated least-privilege application role. Deny
      `UPDATE`/`DELETE` on `audit_logs` for that role.
- [ ] Enforce the request-body limit and rate limits at the proxy as well.
- [ ] Disable query-string logging at the proxy (the WebSocket token is a query
      parameter because browsers cannot set an Authorization header).
- [ ] Keep the server and desktop clients on a private network or VPN; never
      expose the development server directly to the Internet.
- [ ] Enable encrypted, tested backups and define a restore drill.
- [ ] Ship Android release builds only with HTTPS endpoints; debug builds are
      not distribution artifacts.
- [ ] Run `pytest`, `ruff check .`, `bandit -r app -q`, `pip-audit -r
      requirements.txt` and `alembic check` before every release.

## Known gaps and compensating controls

These items are outside the codebase or only partially implemented. They are
tracked in the SOC 2 mapping:

- No MFA or external identity provider yet. Compensating controls: short-lived
  tokens, token revocation, rate limiting, lockout and audit monitoring.
- No centralized SIEM or log retention pipeline. JSON logs and the audit table
  must be shipped to the organization's log platform.
- No application-level encryption at rest or customer-managed keys; rely on
  database/volume encryption and secrets management.
- No automated backup/restore verification in this repository.
- No third-party penetration test or formal vulnerability-disclosure program in
  the repository; schedule an annual test and track remediation.
- The in-process rate limiter is not shared across workers.
- WebSocket authorization is checked at connection time; long-lived connections
  are not revalidated until the client reconnects. Close idle connections at the
  proxy and keep token lifetimes short.
- The audit table is append-only by application design; database grants must
  enforce immutability.
- The desktop token is stored in a `0600` file rather than the OS keychain.
- CI runs on GitHub-hosted runners; runner supply-chain and branch-protection
  settings are organization controls.
