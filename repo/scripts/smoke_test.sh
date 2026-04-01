#!/usr/bin/env bash
set -euo pipefail

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

run_check "Backend health endpoint" "curl -fsS http://localhost:8080/api/health >/dev/null"
run_check "Backend version endpoint" "curl -fsS http://localhost:8080/api/meta/version >/dev/null"
run_check "Frontend health endpoint" "curl -fsS http://localhost:4200/health >/dev/null"

echo "Smoke summary: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
