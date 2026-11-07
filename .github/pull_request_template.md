## Summary

<!-- What changed and why? Link the issue or risk item. -->

## Security impact

<!-- Check all that apply. -->

- [ ] No security-relevant change
- [ ] Authentication, session or token handling
- [ ] Authorization, roles or tenant isolation
- [ ] Audit logging or sensitive-data handling
- [ ] Cryptography, secrets or dependency changes
- [ ] CI/CD, release or infrastructure configuration
- [ ] Database schema or migration

If any box above is checked, describe the threat considered and the mitigation:

<!-- e.g. "Added ADMIN check to DELETE /clientes; covered by test_x" -->

## Testing

<!-- Commands run and results. Attach CI run URL when available. -->

- [ ] `cd server && pytest`
- [ ] `cd server && ruff check . && bandit -r app -q && pip-audit -r requirements.txt`
- [ ] `cd server && alembic upgrade head && alembic check` (migration changes)
- [ ] `cd desktop-python && pytest && ruff check .`
- [ ] `./gradlew detekt testDebugUnitTest :shared:test`
- [ ] Manual verification:

## Database and deployment

- [ ] No migration
- [ ] Migration included and reviewed for locking/data volume
- [ ] Deployment requires a config or secret change (describe below)
- [ ] Rollback plan documented

## Checklist

- [ ] No secrets, tokens, real customer data or PINs in the diff or logs
- [ ] New external input is validated and bounded
- [ ] Error messages do not leak account existence or internals
- [ ] The change is covered by an automated test where practical
- [ ] Documentation updated (README, SECURITY, SOC 2 mapping, runbooks)

## Reviewer notes

<!-- For single-maintainer changes: record the compensating review and date. -->
