#!/usr/bin/env bash
# Docker-only test orchestrator.
#
# Runs every verification suite (smoke, backend unit, API integration tests)
# inside Docker containers so the host only needs:
#   - bash
#   - docker + docker compose
#
# No host python3 / curl / java / node / psql / maven is required.
set -Eeuo pipefail

PROJECT_NAME="pharmaprocure"
COMPOSE=(docker compose -p "$PROJECT_NAME" -f docker-compose.yml -f docker-compose.test.yml)
TEST_TOOLS_IMAGE="pharmaprocure-test-tools:latest"
TEST_NETWORK="${PROJECT_NAME}_default"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

TOTAL=0
PASSED=0
FAILED=0
FAILED_STEPS=()

log() {
  printf '[%s] %s\n' "$(date +'%H:%M:%S')" "$*"
}

run_step() {
  local label="$1"
  shift
  TOTAL=$((TOTAL + 1))
  log "Running: $label"
  if "$@"; then
    echo "[PASS] $label"
    PASSED=$((PASSED + 1))
    return 0
  else
    echo "[FAIL] $label"
    FAILED=$((FAILED + 1))
    FAILED_STEPS+=("$label")
    return 1
  fi
}

dump_logs() {
  log "Dumping compose service logs for diagnostics"
  "${COMPOSE[@]}" ps || true
  "${COMPOSE[@]}" logs --no-color --tail=200 || true
}

cleanup() {
  local exit_code=$?
  if [ "$exit_code" -ne 0 ] || [ "$FAILED" -gt 0 ]; then
    dump_logs
  fi
  log "Tearing down test stack"
  "${COMPOSE[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
  exit "$exit_code"
}

on_error() {
  local line="$1"
  echo "[ERROR] run_tests.sh failed on line $line" >&2
}

trap 'on_error $LINENO' ERR
trap cleanup EXIT

require_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required on PATH" >&2
    exit 2
  fi
  if ! docker compose version >/dev/null 2>&1; then
    echo "docker compose v2 is required" >&2
    exit 2
  fi
}

build_test_tools_image() {
  log "Building test-tools image ($TEST_TOOLS_IMAGE)"
  docker build --quiet -t "$TEST_TOOLS_IMAGE" -f API_tests/Dockerfile API_tests >/dev/null
}

compose_up() {
  log "Starting containers (clean slate)"
  "${COMPOSE[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
  "${COMPOSE[@]}" up -d --build
}

wait_for_healthy() {
  log "Waiting for postgres + backend + frontend to report healthy"
  local deadline=$(( $(date +%s) + 240 ))
  while true; do
    local healthy
    healthy=$("${COMPOSE[@]}" ps --format '{{.Health}}' 2>/dev/null | grep -c '^healthy$' || true)
    if [ "$healthy" -ge 3 ]; then
      log "All three services healthy"
      return 0
    fi
    if [ "$(date +%s)" -ge "$deadline" ]; then
      log "Timeout waiting for healthy services (saw $healthy of 3)"
      return 1
    fi
    sleep 3
  done
}

run_in_test_tools() {
  # Runs a bash command inside a disposable test-tools container joined to the
  # compose network so it can reach the backend at http://backend:8080 and
  # postgres at postgres:5432.
  docker run --rm \
    --network "$TEST_NETWORK" \
    -e BASE_URL=http://backend:8080 \
    -e BACKEND_URL=http://backend:8080 \
    -e FRONTEND_URL=http://frontend:80 \
    -e DB_HOST=postgres \
    -e DB_PORT=5432 \
    -e DB_USER=pharmaprocure \
    -e DB_PASSWORD=pharmaprocure \
    -e DB_NAME=pharmaprocure \
    -v "$ROOT_DIR":/workspace \
    -w /workspace \
    "$TEST_TOOLS_IMAGE" \
    bash -c "$1"
}

run_backend_unit_tests() {
  log "Running backend unit tests (containerized maven)"
  mkdir -p "$ROOT_DIR/.tmp/m2"
  docker run --rm \
    -v "$ROOT_DIR/backend":/workspace \
    -v "$ROOT_DIR/.tmp/m2":/root/.m2 \
    -w /workspace \
    maven:3.9.8-eclipse-temurin-17 \
    mvn -q -B clean test
}

############################################################
# Execution
############################################################
require_docker

log "[1/4] Building support images"
build_test_tools_image

log "[2/4] Starting service stack"
compose_up
wait_for_healthy

log "[3/4] Running verification suites"

run_step "Smoke tests (containerized)" \
  run_in_test_tools "bash /workspace/scripts/smoke_test.sh"

run_step "Backend unit tests (containerized maven)" \
  run_backend_unit_tests

run_step "Auth API tests" \
  run_in_test_tools "bash /workspace/API_tests/auth_api_tests.sh"

run_step "Order lifecycle API tests" \
  run_in_test_tools "bash /workspace/API_tests/order_lifecycle_api_tests.sh"

run_step "Document center API tests" \
  run_in_test_tools "bash /workspace/API_tests/document_center_api_tests.sh"

run_step "Check-ins API tests" \
  run_in_test_tools "bash /workspace/API_tests/checkins_api_tests.sh"

run_step "Critical actions API tests" \
  run_in_test_tools "bash /workspace/API_tests/critical_actions_api_tests.sh"

run_step "Coverage API tests (all remaining endpoints)" \
  run_in_test_tools "bash /workspace/API_tests/coverage_api_tests.sh"

log "[4/4] Checking delivery structure"
run_step "Test directories present" \
  bash -c "test -d unit_tests && test -d API_tests"

echo ""
echo "Test Summary"
echo "- Total:  $TOTAL"
echo "- Passed: $PASSED"
echo "- Failed: $FAILED"

if [ "$FAILED" -gt 0 ]; then
  echo "Failed steps:"
  for step in "${FAILED_STEPS[@]}"; do
    echo "  - $step"
  done
  exit 1
fi
exit 0
