# Incident response plan

This plan covers security and privacy incidents affecting Torque: the FastAPI
backend, Android and desktop clients, the database and the deployment
environment. It is a template for a small team; assign the named roles before
an incident occurs.

## 1. Roles

| Role | Responsibility | Default |
|---|---|---|
| Incident commander | Owns the timeline, decisions and communication | Security lead |
| Technical lead | Investigates, contains and fixes | Engineering owner |
| Operations lead | Infrastructure, TLS, database, backups, network | Operations owner |
| Communications lead | Customer/workshop and regulator communication | Management |
| Scribe | Maintains the incident log and evidence index | Any available engineer |
| Legal/privacy advisor | Notification obligations and privilege | External counsel |

One person may hold several roles in a small team, but the incident commander
must not also be the only investigator for a critical incident.

## 2. Severity levels

| Severity | Examples | Target acknowledgement | Target containment |
|---|---|---|---|
| **Critical** | Confirmed data breach, admin account takeover, ransomware, secret leak allowing production access | 1 hour | 4 hours |
| **High** | Authentication bypass, cross-tenant data exposure, audit log tampering, exploitable RCE | 4 hours | 24 hours |
| **Medium** | Stored XSS, privilege escalation requiring a valid account, denial of service with business impact | 1 business day | 5 business days |
| **Low** | Information disclosure without sensitive data, missing header, dependency issue without known exploit | 3 business days | Next release |

When in doubt, start at the higher severity and downgrade later.

## 3. Detection sources

- GitHub Security Advisories and private vulnerability reports.
- Dependabot and `pip-audit` alerts.
- Failed CI security jobs (`bandit`, `pip-audit`, `alembic check`).
- Application logs: repeated `auth.login_failed`, `auth.locked`,
  `authz.denied`, `audit_write_failed`, unusual `request_id` patterns.
- Audit table: bulk deletes, role changes, password/PIN changes outside a
  maintenance window, access from unexpected IPs.
- Database/provider alerts: CPU, storage, connection exhaustion, failed backups.
- Workshop user reports (lost device, suspicious login, data visible to the
  wrong workshop).
- External notification (researcher, customer, provider).

## 4. Response phases

### 4.1 Prepare

- Keep the contact list and this plan accessible offline.
- Maintain a private incident channel and an evidence folder.
- Verify that audit logs are shipped off-host and retained.
- Run a tabletop exercise at least annually (scenarios in section 7).
- Keep emergency credentials in a sealed/break-glass process with audit.

### 4.2 Identify

1. Open an incident record with an ID, start time, reporter and initial severity.
2. Preserve evidence **before** changing systems:
   - export relevant application logs and `audit_logs` rows;
   - record `request_id`, actor, IP, user agent and timestamps;
   - snapshot the database or affected volumes if safe;
   - save CI, provider and SIEM alerts.
3. Determine scope: which workshops, users, records and time window.
4. Assign severity and notify the incident commander.

Do not delete logs, rebuild hosts or rotate evidence away before the snapshot is
stored in a separate location.

### 4.3 Contain

Choose the least disruptive effective action:

- Revoke sessions by incrementing `token_version` for affected users or by
  disabling the account (`activo=false`).
- Change compromised secrets: `SECRET_KEY`, `PIN_PEPPER`, database passwords,
  provider tokens. Rotating `SECRET_KEY` invalidates all JWTs.
- Block the source IP or range at the reverse proxy/firewall (watch for shared
  NAT and lockout side effects).
- Isolate the host/container from the network if compromise is suspected.
- Disable the affected integration or endpoint if exploitation is ongoing.

Record every action with time, actor and reason.

### 4.4 Eradicate

- Identify and fix the root cause (code, configuration, dependency, credential).
- Add a regression test and, where relevant, a migration.
- Patch dependencies and rebuild artifacts from a clean environment.
- Remove attacker persistence: unauthorized users, tokens, keys, scheduled jobs
  and database triggers.

### 4.5 Recover

- Restore from a verified clean backup if integrity is in doubt.
- Re-enable services gradually and monitor error/security logs.
- Force password/PIN resets for affected accounts.
- Confirm audit logging works and that no gaps remain.
- Communicate restoration status to affected workshops.

### 4.6 Lessons learned

Within five business days of closure:

- Produce a blameless post-mortem: timeline, root cause, impact, detection gap,
  response effectiveness and action items with owners and due dates.
- Add or update automated tests, alerts, runbooks and this plan.
- Track action items to completion; the incident is not closed until they are
  done or formally risk-accepted.

## 5. Evidence handling

| Evidence | Where to store | Retention |
|---|---|---|
| Incident record and timeline | Private incident tracker | 3 years minimum |
| Application/audit log export | Encrypted evidence store, separate account | Per retention policy |
| Database/volume snapshot | Encrypted backup account | Until closure + policy |
| CI/provider alerts | Incident folder | 3 years |
| Communication records | Incident folder | 3 years |

Preserve integrity: record hashes for exported files, restrict access to the
incident team and keep a chain-of-custody note.

## 6. Communication and notification

- **Internal:** incident channel, status updates every 4 hours for Critical/High.
- **Workshops:** only the communications lead speaks to customers. State what is
  known, what they must do and when the next update will arrive; do not speculate.
- **Regulators:** GDPR requires notification to the supervisory authority within
  72 hours of becoming aware of a personal-data breach where required, and
  communication to data subjects without undue delay when the risk is high.
  Other jurisdictions have their own deadlines. Involve legal counsel
  immediately; this document is not legal advice.
- **Law enforcement:** decide with counsel; preserve evidence first.
- **Public disclosure:** coordinate with counsel and affected parties. Do not
  publish exploit details before mitigations are available.

## 7. Tabletop scenarios

Run at least one per year:

1. **Stolen admin token.** A desktop token file is copied from a shared office
   PC. Exercise revocation, audit review, user notification and token storage
   improvement.
2. **Cross-tenant data exposure.** A query misses `id_taller`. Exercise
   detection, scope calculation, fix, regression test and customer notice.
3. **Dependency compromise.** A malicious package version is published.
   Exercise lockfile review, `pip-audit`, artifact rebuild and secret rotation.
4. **Lost backup / ransomware.** Exercise restore from an immutable backup,
   key access and downtime communication.

## 8. Contacts

Fill in before relying on this plan:

| Role | Name | Primary contact | Backup contact |
|---|---|---|---|
| Incident commander | TBD | TBD | TBD |
| Technical lead | TBD | TBD | TBD |
| Operations lead | TBD | TBD | TBD |
| Communications lead | TBD | TBD | TBD |
| Legal/privacy advisor | TBD | TBD | TBD |
| Hosting provider support | TBD | TBD | TBD |
| Database provider support | TBD | TBD | TBD |

## 9. Metrics

Track and review quarterly:

- Mean time to detect (MTTD).
- Mean time to acknowledge (MTTA).
- Mean time to contain (MTTC).
- Percentage of incidents with a completed post-mortem within five days.
- Percentage of post-mortem actions closed on time.
- Number of repeat incidents with the same root cause.
