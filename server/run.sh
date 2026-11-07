#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

echo "--- Torque API Server ---"
echo ""

echo "[1/3] Applying database migrations..."
if ! python -m alembic upgrade head; then
  echo ""
  echo "ERROR: database migrations failed." >&2
  echo "If this database predates Alembic, back it up, verify that its schema" >&2
  echo "matches the baseline, then stamp it with:" >&2
  echo "  python -m alembic stamp head" >&2
  exit 1
fi

echo "[2/3] Seeding brand/model catalog..."
python -m scripts.seed_marcas

echo "[3/3] Starting server..."
echo "Server will be available at:"
echo "  Local:   http://localhost:8000"
echo "  Network: http://<your-ip>:8000"
echo "  Docs:    http://localhost:8000/docs"
echo ""

# Access logging is handled by the application (JSON, query-string free) so
# WebSocket tokens passed in the query string are never written to disk by Uvicorn.
RELOAD_FLAG=""
if [ "${ENVIRONMENT:-development}" != "production" ]; then
  RELOAD_FLAG="--reload"
fi

uvicorn app.main:app \
  --host 0.0.0.0 \
  --port 8000 \
  ${RELOAD_FLAG:+"$RELOAD_FLAG"} \
  --no-access-log \
  --no-server-header \
  --proxy-headers
