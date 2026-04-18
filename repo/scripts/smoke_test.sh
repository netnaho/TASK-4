#!/usr/bin/env bash
set -Eeuo pipefail

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:4200}"

PASS=0
FAIL=0

run_check() {
  local name="$1"
  local command="$2"

  if eval "$command"; then
    echo "[PASS] $name"
    PASS=$((PASS + 1))
  else
    echo "[FAIL] $name"
    FAIL=$((FAIL + 1))
  fi
}

run_check "Backend health endpoint" "curl -fsS ${BACKEND_URL}/api/health >/dev/null"
run_check "Backend version endpoint" "curl -fsS ${BACKEND_URL}/api/meta/version >/dev/null"
run_check "Frontend health endpoint" "curl -fsS ${FRONTEND_URL}/health >/dev/null"

echo "Smoke summary: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
