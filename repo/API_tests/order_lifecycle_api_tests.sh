#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080"
PASS=0
FAIL=0
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

pass() { echo "[PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "[FAIL] $1"; FAIL=$((FAIL + 1)); }

csrf_token() {
  local jar="$1"
  local body="$TMP_DIR/$(basename "$jar").csrf.json"
  curl -fsS -c "$jar" -b "$jar" "$BASE_URL/api/auth/csrf" > "$body"
  python3 - <<'PY' "$body"
import json, sys
print(json.load(open(sys.argv[1], 'r', encoding='utf-8'))['token'])
PY
}

request() {
  local method="$1"
  local path="$2"
  local jar="$3"
  local body="${4:-}"
  local out="$5"
  local token="$(csrf_token "$jar")"

  if [ -n "$body" ]; then
    curl -sS -o "$out" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $token" -c "$jar" -b "$jar" --data "$body"
  else
    curl -sS -o "$out" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "X-XSRF-TOKEN: $token" -c "$jar" -b "$jar"
  fi
}

login() {
  local username="$1"
  local jar="$2"
  local out="$TMP_DIR/login-$username.json"
  local status
  status=$(request POST "/api/auth/login" "$jar" "{\"username\":\"$username\",\"password\":\"PortalAccess2026!\"}" "$out")
  [ "$status" = "200" ] || { cat "$out"; fail "Login failed for $username"; exit 1; }
}

contains() {
  python3 - <<'PY' "$1" "$2"
import sys
content = open(sys.argv[1], 'r', encoding='utf-8').read()
sys.exit(0 if sys.argv[2] in content else 1)
PY
}

assert_status() {
  if [ "$1" = "$2" ]; then pass "$3"; else fail "$3 (expected $2 got $1)"; fi
}

buyer="$TMP_DIR/buyer.cookies"
quality="$TMP_DIR/quality.cookies"
finance="$TMP_DIR/finance.cookies"
fulfillment="$TMP_DIR/fulfillment.cookies"
admin="$TMP_DIR/admin.cookies"

login buyer1 "$buyer"
login quality1 "$quality"
login finance1 "$finance"
login fulfillment1 "$fulfillment"
login admin1 "$admin"

status=$(request POST "/api/orders" "$buyer" '{"notes":"Urgent restock","items":[{"productId":1,"quantity":6},{"productId":2,"quantity":4}]}' "$TMP_DIR/create-order.json")
assert_status "$status" "200" "Buyer creates order"
ORDER_ID=$(python3 - <<'PY' "$TMP_DIR/create-order.json"
import json, sys
print(json.load(open(sys.argv[1], 'r', encoding='utf-8'))['id'])
PY
)

status=$(request POST "/api/orders/$ORDER_ID/submit-review" "$buyer" '' "$TMP_DIR/submit-review.json")
assert_status "$status" "200" "Buyer submits order for review"

status=$(request POST "/api/orders/$ORDER_ID/approve" "$quality" '{"decision":"APPROVED","comments":"Quality approved."}' "$TMP_DIR/approve.json")
assert_status "$status" "200" "Quality reviewer approves order"

status=$(request POST "/api/orders/$ORDER_ID/approve" "$buyer" '{"decision":"APPROVED","comments":"Unauthorized."}' "$TMP_DIR/approve-denied.json")
assert_status "$status" "403" "Wrong role cannot approve order"

status=$(request POST "/api/orders/$ORDER_ID/record-payment" "$finance" '{"referenceNumber":"PAY-9001","amount":120.00}' "$TMP_DIR/payment.json")
assert_status "$status" "200" "Finance records payment"

status=$(request POST "/api/orders/$ORDER_ID/shipments" "$fulfillment" '{"notes":"Partial shipment wave 1","items":[{"orderItemId":1,"quantity":3},{"orderItemId":2,"quantity":2}]}' "$TMP_DIR/ship-invalid.json")
assert_status "$status" "400" "Invalid transition rejects shipment before pick-pack"

status=$(request POST "/api/orders/$ORDER_ID/pick-pack" "$fulfillment" '' "$TMP_DIR/pick-pack.json")
assert_status "$status" "200" "Fulfillment starts pick-pack"

ORDER_ITEM_1=$(python3 - <<'PY' "$TMP_DIR/pick-pack.json"
import json, sys
print(json.load(open(sys.argv[1], 'r', encoding='utf-8'))['items'][0]['id'])
PY
)
ORDER_ITEM_2=$(python3 - <<'PY' "$TMP_DIR/pick-pack.json"
import json, sys
print(json.load(open(sys.argv[1], 'r', encoding='utf-8'))['items'][1]['id'])
PY
)

status=$(request POST "/api/orders/$ORDER_ID/shipments" "$fulfillment" "{\"notes\":\"Partial shipment wave 1\",\"items\":[{\"orderItemId\":$ORDER_ITEM_1,\"quantity\":3},{\"orderItemId\":$ORDER_ITEM_2,\"quantity\":2}]}" "$TMP_DIR/shipment.json")
assert_status "$status" "200" "Fulfillment creates partial shipment"

status=$(request POST "/api/orders/$ORDER_ID/receipts" "$buyer" "{\"notes\":\"Partial receipt\",\"discrepancyConfirmed\":false,\"items\":[{\"orderItemId\":$ORDER_ITEM_1,\"quantity\":2},{\"orderItemId\":$ORDER_ITEM_2,\"quantity\":1}]}" "$TMP_DIR/receipt.json")
assert_status "$status" "200" "Buyer records partial receipt"

status=$(request POST "/api/orders/$ORDER_ID/receipts" "$buyer" "{\"notes\":\"Discrepancy\",\"discrepancyConfirmed\":false,\"items\":[{\"orderItemId\":$ORDER_ITEM_1,\"quantity\":2,\"discrepancyReason\":\"Extra carton noted\"}]}" "$TMP_DIR/discrepancy-fail.json")
assert_status "$status" "400" "Discrepancy requires explicit confirmation"

status=$(request POST "/api/orders/$ORDER_ID/receipts" "$buyer" "{\"notes\":\"Discrepancy acknowledged\",\"discrepancyConfirmed\":true,\"items\":[{\"orderItemId\":$ORDER_ITEM_1,\"quantity\":1,\"discrepancyReason\":\"Short shipment recorded\"}]}" "$TMP_DIR/discrepancy-ok.json")
assert_status "$status" "200" "Buyer records discrepancy case"

status=$(request POST "/api/orders/$ORDER_ID/returns" "$buyer" "{\"reasonCode\":\"DAMAGED_GOODS\",\"comments\":\"One unit damaged\",\"items\":[{\"orderItemId\":$ORDER_ITEM_1,\"quantity\":1}]}" "$TMP_DIR/return.json")
assert_status "$status" "200" "Buyer creates return"

status=$(request GET "/api/orders/$ORDER_ID/traceability" "$buyer" '' "$TMP_DIR/traceability.json")
assert_status "$status" "200" "Traceability endpoint returns order detail"

if contains "$TMP_DIR/traceability.json" 'SHIPMENT_CREATED' && contains "$TMP_DIR/traceability.json" 'RETURN_CREATED'; then
  pass "Traceability includes shipment and return events"
else
  fail "Traceability includes shipment and return events"
fi

status=$(request POST "/api/orders" "$admin" '{"notes":"Admin-controlled order","items":[{"productId":1,"quantity":1}]}' "$TMP_DIR/admin-order.json")
assert_status "$status" "200" "Admin creates global-scope order"
ADMIN_ORDER_ID=$(python3 - <<'PY' "$TMP_DIR/admin-order.json"
import json, sys
print(json.load(open(sys.argv[1], 'r', encoding='utf-8'))['id'])
PY
)

status=$(curl -sS -o "$TMP_DIR/finance-admin-order.json" -w "%{http_code}" -b "$finance" -c "$finance" "$BASE_URL/api/orders/$ADMIN_ORDER_ID")
assert_status "$status" "403" "TEAM-scoped finance cannot access global-owned order"

status=$(curl -sS -o "$TMP_DIR/quality-admin-order.json" -w "%{http_code}" -b "$quality" -c "$quality" "$BASE_URL/api/orders/$ADMIN_ORDER_ID")
assert_status "$status" "200" "GLOBAL-scoped quality reviewer can access global-owned order"

echo "API order lifecycle summary: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
