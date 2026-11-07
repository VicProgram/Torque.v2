# SOC 2 hardening and control mapping

This document maps the Torque codebase and operating procedures to the AICPA
Trust Services Criteria (TSC, 2017 edition). It is not a SOC 2 report or a
certification. A SOC 2 opinion requires an independent auditor, an agreed system
description and evidence that the controls operated over the audit period.

The goal of this mapping is to make the technical controls auditable and to
separate them clearly from organizational controls that must be operated outside
the repository.

## 1. System boundary

| Component | Technology | Security relevance |
|---|---|---|
| `server/` | FastAPI, SQLAlchemy, Alembic, PostgreSQL/SQLite | Authentication, authorization, tenant isolation, audit trail, PDF generation |
| `app/` + `data/` + `core/` + `shared/` | Kotlin, Jetpack Compose, Room, Retrofit | Mechanic client, offline data, PIN login |
| `desktop-python/` | Python, CustomTkinter, httpx | Workshop office client |
| CI/CD | GitHub Actions, Dependabot | Build, test, dependency and migration gates |
| Deployment | Customer/workshop network, reverse proxy, PostgreSQL | TLS termination, backups, secrets, network isolation |

**In scope for these controls:** application code, API contracts, database
schema/migrations, Android and desktop client behaviour, CI configuration and
the security documentation in this repository.

**Out of scope / organization-operated:** hosting provider, physical security,
employee background checks, endpoint protection, network firewalls, SIEM,
backup storage, key management, vendor contracts, formal risk assessment,
penetration testing and the audit itself.

## 2. Status legend

| Status | Meaning |
|---|---|
| **Implemented** | Enforced by code or CI and covered by automated tests or a deterministic command. |
| **Partial** | Technical control exists but needs an operational process, shared infrastructure or additional coverage. |
| **Organizational** | Policy/process control; the repository can only provide templates and evidence instructions. |
| **Gap** | Not implemented; tracked in the remediation roadmap. |

## 3. Control mapping

### CC6 — Logical and physical access controls

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| CC6.1 | Logical access is restricted to authorized users | bcrypt passwords and PINs; JWT signature/issuer/audience/expiry validation; per-user `token_version` revocation; account lockout; generic auth errors | Implemented |
| CC6.1 | Access is removed when no longer required | `activo` flag checked on every request; `/auth/logout` and password/PIN changes increment `token_version`; admin can deactivate users | Implemented |
| CC6.1 | Authentication attempts are monitored | `audit_logs` records `auth.login`, `auth.login_pin`, failures, lockouts and password/PIN changes; JSON logs include request ID and actor | Implemented |
| CC6.2 | Users are registered and authorized before access | User creation is admin-only; passwords and PINs are validated by policy; duplicate PINs are rejected via the HMAC lookup | Partial — no formal joiner/mover/leaver workflow or approval ticket in the repo |
| CC6.3 | Role-based access follows least privilege | `ADMIN` required for deletes and invoice create/update; tenant scoping on every workshop-owned query; WebSocket checks tenant and token version | Implemented — roles are currently `ADMIN`/`MECANICO`; finer segregation needs product work |
| CC6.6 | External threats are mitigated | Rate limiting, account lockout, body-size cap, explicit CORS, TrustedHost, security headers, HTTPS redirect, TLS-only Android release | Partial — WAF/DDoS protection and a shared rate-limit store are deployment responsibilities |
| CC6.7 | Data transmission is protected | HSTS, HTTPS redirect in production, TLS-only release network security config, desktop redirect protection and cleartext warning | Implemented for clients; TLS certificate lifecycle is organizational |
| CC6.8 | Malicious software is prevented/detected | `pip-audit`, `bandit`, Dependabot, pinned dependency ranges, `python-jose` removal | Partial — no endpoint EDR or container image scanning configured here |

### CC7 — System operations

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| CC7.1 | Configuration and vulnerabilities are monitored | CI runs Ruff, Bandit, pytest and `alembic check`; Dependabot opens weekly update PRs; production config refuses insecure defaults | Partial — no runtime configuration-drift detection or infrastructure scanning |
| CC7.2 | Security events are monitored | Append-only audit table, structured JSON logs, request IDs, audit listing endpoint for admins | Partial — logs must be shipped to a SIEM/retention platform and alerts configured |
| CC7.3 | Security incidents are evaluated | Severity model and triage steps in [`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) | Organizational |
| CC7.4 | Incidents are contained and resolved | Response phases, evidence preservation, communication and post-mortem templates | Organizational |
| CC7.5 | Recovery from incidents | `/healthz`, pool pre-ping/timeouts, migration-based schema recovery; restore procedure is deployment-specific | Partial — backup frequency and restore drills are organizational |
| CC7.2/7.3 | Audit records are protected | Audit writes are application-level append-only; no update/delete API; secrets excluded; DB role should deny `UPDATE`/`DELETE` | Partial — immutability must also be enforced by database grants |

### CC8 — Change management

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| CC8.1 | Changes are authorized, tested and approved | PR template, CI gates, Alembic migration check, tests, Dependabot, change process in [`CHANGE_MANAGEMENT.md`](CHANGE_MANAGEMENT.md) | Partial — branch protection and reviewer assignment are GitHub organization settings |
| CC8.1 | Emergency changes are controlled | Documented emergency-change path with retrospective review and audit evidence | Organizational |
| CC8.1 | Changes are traceable | Git history, PR links, CI run logs, release tags and migration revisions | Implemented |

### CC9 — Risk mitigation

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| CC9.1 | Risks are identified and mitigated | This mapping, the residual-gap list in [`SECURITY.md`](../SECURITY.md) and the remediation roadmap below | Partial — a formal risk register and periodic review are organizational |
| CC9.2 | Vendor and business-partner risk is managed | Dependabot and `pip-audit` cover software suppliers; hosting/DB/provider contracts are outside the repo | Organizational |

### A1 — Availability

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| A1.1 | Capacity is managed | Pool sizing, timeouts, request-size cap, health endpoint | Partial — capacity monitoring and autoscaling are deployment responsibilities |
| A1.2 | Environmental protections and backups exist | SQLite WAL/foreign keys, PostgreSQL recommended; backup/restore documented as deployment control | Organizational |
| A1.3 | Recovery is tested | No automated restore test in this repository | Gap — schedule and document restore drills |

### C1 — Confidentiality

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| C1.1 | Confidential information is identified and protected | Tenant isolation by `id_taller`, role checks on destructive/billing operations, no secrets in logs, TLS in production | Implemented at application level |
| C1.2 | Confidential information is disposed of securely | Deletes are audited; backup/media sanitisation and retention are organizational | Organizational |

### P — Privacy

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| P4.1 | Personal information is used only for stated purposes | Data minimisation in audit rows (no passwords/PINs/tokens); customer data limited to workshop operations | Partial — privacy notice and consent records are organizational |
| P4.2 | Data retention and disposal are defined | Not implemented in code | Gap — define retention periods for customers, vehicles, invoices and audit logs |
| P6.1 | Data subject requests are handled | Manual process required | Organizational |
| P7.1 | Data quality and correction | CRUD endpoints and audit trail support correction | Partial |

### PI1 — Processing integrity

| TSC | Control objective | Implementation and evidence | Status |
|---|---|---|---|
| PI1.1 | Processing is complete, valid and accurate | Pydantic validation, database constraints, migration/model drift check, 50 server tests plus Android/desktop suites | Partial — business reconciliation and monitoring are organizational |
| PI1.4 | Output is complete and accurate | Invoice totals and PDF generation covered by tests; generated PDFs should be archived immutably | Partial |

## 4. Evidence collection

Run these commands from a clean checkout and retain the output with the release
record (CI logs are acceptable evidence):

```bash
# Backend
cd server
. .venv/bin/activate
pytest
ruff check .
bandit -r app -q
pip-audit -r requirements.txt
DATABASE_URL=sqlite:////tmp/torque-evidence.db alembic upgrade head
DATABASE_URL=sqlite:////tmp/torque-evidence.db alembic check

# Desktop
cd ../desktop-python
pytest
ruff check .
bandit -r app -q
pip-audit -r requirements.txt

# Android
cd ..
./gradlew detekt
./gradlew testDebugUnitTest :shared:test
./gradlew assembleRelease
```

Additional evidence to capture each quarter:

| Evidence | Frequency | Owner |
|---|---|---|
| CI run URLs for `main` and release commit | Per release | Engineering |
| `pip-audit`/Dependabot reports and remediation PRs | Monthly | Engineering |
| Access review of workshop `ADMIN` users | Quarterly | Workshop owner |
| Audit-log export and retention confirmation | Monthly | Operations |
| Backup restore test result | Quarterly | Operations |
| Incident tabletop and post-mortem actions | Annual | Security lead |
| Penetration-test report and remediation status | Annual | Security lead |
| Risk-register review | Quarterly | Management |

## 5. Control ownership

Small teams can keep this simple, but each control needs a named owner:

| Role | Responsibilities |
|---|---|
| Engineering owner | Code controls, CI, dependency patches, migrations, security fixes |
| Operations owner | TLS, reverse proxy, database, backups, log shipping, uptime |
| Workshop administrator | User lifecycle, role assignment, quarterly access review |
| Security lead | Risk register, incident commander, pen test, auditor liaison |
| Management | Approves risk acceptance, funds remediation, reviews metrics |

## 6. Remediation roadmap

Priority order for closing the remaining gaps:

1. **High — shared rate limiting and WAF/reverse-proxy limits.** Move the
   in-process limiter to Redis or the gateway before running multiple workers.
2. **High — log shipping and alerting.** Send JSON logs and audit rows to an
   immutable SIEM with alerts for repeated auth failures, RBAC denials, audit
   write failures and token-version changes.
3. **High — database immutability.** Use a least-privilege runtime role and
   deny `UPDATE`/`DELETE` on `audit_logs`; test the grants.
4. **High — backup and restore drills.** Automate encrypted backups and record
   a successful restore at least quarterly.
5. **Medium — MFA for administrators.** Add TOTP or an OIDC provider for
   `ADMIN` accounts.
6. **Medium — secrets management.** Replace `.env` files with a managed secret
   store and rotate `SECRET_KEY`/`PIN_PEPPER` on a defined schedule.
7. **Medium — retention policy.** Define and implement retention for customer
   data, invoices and audit logs, including legal holds.
8. **Medium — annual penetration test** and remediation tracking.
9. **Low — desktop token storage** in the OS keychain instead of a `0600` file.
10. **Low — formal risk register** with likelihood/impact scoring and quarterly
    review.

## 7. Auditor notes

- The control period matters: a control that was added after the period start
  cannot be claimed for the whole period. Keep dated PRs, CI runs and release
  tags.
- “Implemented in code” is not the same as “operating effectively”. The auditor
  will ask for samples: a PR with review, a CI run, an access review, a restore
  test and an incident record.
- Document exceptions and risk acceptances explicitly. An accepted risk with an
  owner and expiry date is auditable; an undocumented gap is a finding.
- Re-run this mapping after material architecture changes (new identity
  provider, new database, new hosting region, new client).
