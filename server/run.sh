#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

echo "--- Torque API Server ---"
echo ""

# Create database tables if they don't exist
echo "[1/3] Applying database migrations..."
python -m alembic upgrade head 2>/dev/null || python -c "
from app.database import engine, Base
from app.models import *
Base.metadata.create_all(bind=engine)
print('Tables created (no Alembic migrations found).')
"

echo "[2/3] Seeding brand/model catalog..."
python -m scripts.seed_marcas

echo "[3/3] Starting server..."
echo "Server will be available at:"
echo "  Local:   http://localhost:8000"
echo "  Network: http://<your-ip>:8000"
echo "  Docs:    http://localhost:8000/docs"
echo ""

uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
