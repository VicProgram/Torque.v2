# Change management

This document defines how changes to Torque are proposed, reviewed, tested,
released and rolled back. It supports the SOC 2 change-management criteria
(CC8.1) and gives auditors a repeatable evidence trail.

## 1. Principles

- Every production change is traceable to a pull request, a reviewer, a CI run
  and a release.
- Automated gates are mandatory; a red CI run cannot be merged.
- Database changes are versioned and forward-only. Application rollback must
  not require an automatic destructive downgrade.
- Security-relevant changes get explicit review, even in a small team.
- Emergency changes are allowed, but they are documented and reviewed
  retrospectively.

## 2. Change types

| Type | Examples | Required path |
|---|---|---|
| Standard | Feature, bug fix, dependency update, docs | PR + CI + review + release |
| Normal infrastructure | Reverse-proxy, DNS, TLS, database parameter | Change ticket + review + rollback plan |
| Emergency | Critical security fix, outage mitigation | PR or documented hotfix + post-hoc review within 2 business days |
| Database migration | Schema or data change | PR + migration review + backup + CI `alembic check` |
| Secret rotation | `SECRET_KEY`, `PIN_PEPPER`, DB password | Change ticket + dual control where possible |

## 3. Branching and pull requests

1. Create a short-lived branch from `main`.
2. Open a pull request. The template asks for summary, security impact, tests,
   migration notes and rollback.
3. Keep PRs focused; unrelated refactors make review and rollback harder.
4. Link the issue or risk item.
5. Wait for CI to pass and at least one approval.
6. Squash or merge according to repository policy; never force-push `main`.

### Review requirements

- At least one reviewer who did not author the change.
- Security-sensitive changes (auth, crypto, permissions, audit, dependencies,
  CI, infrastructure) require a reviewer with security context.
- In a single-maintainer situation, use a compensating control:
  - self-review checklist in the PR,
  - CI security gates,
  - a second pair of eyes from a trusted contributor for auth/crypto changes
    when available,
  - post-merge review within one business day,
  - a dated note in the PR explaining the exception.

## 4. CI gates

The GitHub Actions workflow must pass:

| Gate | Command | Blocks merge |
|---|---|---|
| Backend lint | `ruff check .` | Yes |
| Backend tests | `pytest --cov=app` | Yes |
| Backend security | `bandit -r app -q` | Yes |
| Backend dependencies | `pip-audit -r requirements.txt` | Yes |
| Migration drift | `alembic upgrade head` + `alembic check` | Yes |
| Desktop lint/tests/security | Ruff, pytest, Bandit, pip-audit | Yes |
| Android static analysis | `./gradlew detekt` | Yes |
| Android tests | `./gradlew testDebugUnitTest :shared:test` | Yes |
| Android build | `./gradlew assembleDebug` / `assembleRelease` | Yes |

Branch protection on `main` must require the CI checks, disallow force pushes
and require review. Branch protection is a GitHub organization setting and must
be verified during the audit.

## 5. Database migrations

- Alembic is the source of truth. `Base.metadata.create_all()` is only for
  tests and the seed script, not for production schema changes.
- Every model change ships with a migration in `server/alembic/versions/`.
- CI runs `alembic upgrade head` against a fresh database and `alembic check`
  to detect model/migration drift.
- Migrations must be reviewed for locking, data volume and rollback impact.
- Use expand/contract for zero-downtime changes: add nullable column, backfill,
  switch reads/writes, then drop the old column in a later release.
- Back up the database before applying a migration to production.
- Destructive migrations require an explicit rollback plan and, where possible,
  a tested restore.

### Existing databases created before Alembic

The repository's first revision is a baseline that creates the full schema. For
a database that already contains the tables but has no `alembic_version` row:

1. Take a backup.
2. Compare the live schema with the baseline (columns, indexes, constraints).
3. If they match, stamp the baseline without re-running DDL:

   ```bash
   cd server
   DATABASE_URL=postgresql://... alembic stamp head
   ```

4. If they do not match, generate a follow-up migration that reconciles the
   differences; do not edit the baseline after it has been applied anywhere.
5. Verify with `alembic current` and `alembic check`.

## 6. Dependency management

- Dependabot opens weekly grouped PRs for pip, Gradle and GitHub Actions.
- Security updates are prioritized over feature updates.
- Review changelogs for breaking changes and known regressions.
- Run the full test suite plus `pip-audit` before merging.
- Do not add a dependency with a known advisory, an unmaintained package or an
  unnecessary transitive footprint.
- Pin major versions and review lockfiles where present. Never commit secrets or
  private registry credentials.

### Secret rotation

- `SECRET_KEY` rotation invalidates every issued JWT. Users must log in again;
  no database change is required.
- Set a dedicated `PIN_PEPPER` in production. If it is empty, `SECRET_KEY` is
  used as the PIN pepper, so rotating `SECRET_KEY` would also invalidate every
  stored `pin_lookup` value.
- Rotating `PIN_PEPPER` always invalidates existing PIN lookups because the
  stored HMACs were derived with the old value. Plan PIN re-enrollment (or a
  controlled backfill that re-hashes each PIN) before rotating it; otherwise
  PIN login fails until every PIN is reset.
- Record the rotation date, operator, affected users and rollback value in the
  change ticket. Never keep the old secret active longer than the rollback
  window.

## 7. Release process

1. Confirm CI is green on the release commit.
2. Review the changelog and security-relevant changes.
3. Tag the release (`vX.Y.Z`) and attach build artifacts or image digests.
4. Back up the database and record the current migration revision.
5. Apply migrations, deploy the backend, then verify `/healthz` and a smoke
   login.
6. Ship client builds only after the backend is compatible.
7. Record the release evidence: commit SHA, tag, CI run URL, migration revision,
   approver and deployment time.
8. Announce the release and any required user action (for example, re-login
   after a JWT claim change).

## 8. Rollback

- Application rollback: redeploy the previous image/commit.
- Database rollback: prefer a forward fix. If a down-migration is safe and
  tested, use it; otherwise restore from the pre-change backup.
- Client rollback: keep the previous APK/desktop bundle available and document
  the minimum supported server version.
- After a rollback, record the reason, the data impact and follow-up actions.

## 9. Emergency changes

1. Declare an incident and assign an incident commander.
2. Make the smallest safe change; prefer a configuration toggle or feature flag.
3. Test what can be tested under time pressure; state what was skipped.
4. Deploy with a second person observing when possible.
5. Open a retrospective PR or change record within two business days with the
   diff, rationale and follow-up tests.
6. Update the incident record and risk register.

## 10. Evidence retention

Retain for at least three years (or the audit period defined by the auditor):

- PRs and review approvals (GitHub history).
- CI run logs and security scan results.
- Release tags, artifact digests and deployment records.
- Migration revisions applied per environment.
- Emergency-change retrospectives.
- Risk acceptances with owner and expiry date.

## 11. Segregation of duties

In a small team the same person may develop, review and deploy. Document the
compensating controls explicitly:

- CI enforces tests and security scans independently of the author.
- Production secrets are held in a secret manager with access logging.
- Deployments are recorded with commit SHA and approver.
- Database backups and audit exports are stored in a separate account.
- A trusted contributor or advisor reviews authentication, cryptography and
  permission changes when available.
