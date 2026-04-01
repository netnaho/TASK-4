#!/usr/bin/env bash
set -euo pipefail

TOTAL=0
PASSED=0
FAILED=0

run_step() {
  local label="$1"
  local command="$2"
  TOTAL=$((TOTAL + 1))
  if eval "$command"; then
    echo "[PASS] $label"
    PASSED=$((PASSED + 1))
  else
    echo "[FAIL] $label"
    FAILED=$((FAILED + 1))
  fi
}

echo "[1/4] Starting containers in detached mode"
docker compose down -v --remove-orphans >/dev/null 2>&1 || true
docker compose up -d --build

echo "[2/4] Waiting for services to report healthy"
for i in $(seq 1 40); do
  healthy_count=$(docker compose ps | grep -c "(healthy)" || true)
  if [ "$healthy_count" -ge 3 ]; then
    break
  fi
  sleep 3
done

echo "[3/4] Running verification suites"
run_step "Smoke tests" "bash ./scripts/smoke_test.sh"
run_step "Backend unit tests" "docker run --rm -v \"$PWD/backend\":/workspace -w /workspace maven:3.9.8-eclipse-temurin-17 mvn -q clean test"
run_step "Auth API tests" "bash ./API_tests/auth_api_tests.sh"
run_step "Order lifecycle API tests" "bash ./API_tests/order_lifecycle_api_tests.sh"
run_step "Document center API tests" "bash ./API_tests/document_center_api_tests.sh"
run_step "Check-ins API tests" "bash ./API_tests/checkins_api_tests.sh"
run_step "Critical actions API tests" "bash ./API_tests/critical_actions_api_tests.sh"

echo "[4/4] Checking delivery structure"
run_step "Test directories present" "test -d unit_tests && test -d API_tests"

echo ""
echo "Test Summary"
echo "- Total: $TOTAL"
echo "- Passed: $PASSED"
echo "- Failed: $FAILED"

if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
