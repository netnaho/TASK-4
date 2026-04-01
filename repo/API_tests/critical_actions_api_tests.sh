#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
PASS=0
FAIL=0

pass(){ echo "[PASS] $1"; PASS=$((PASS+1)); }
fail(){ echo "[FAIL] $1"; FAIL=$((FAIL+1)); }

csrf_token(){ local jar="$1"; local body="$TMP_DIR/$(basename "$jar").csrf.json"; curl -fsS -c "$jar" -b "$jar" "$BASE_URL/api/auth/csrf" > "$body"; python3 - <<'PY' "$body"
import json,sys
print(json.load(open(sys.argv[1]))['token'])
PY
}
json_request(){ local method="$1"; local path="$2"; local jar="$3"; local body="$4"; local out="$5"; local token="$(csrf_token "$jar")"; curl -sS -o "$out" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $token" -c "$jar" -b "$jar" --data "$body"; }
multipart_request(){ local method="$1"; local path="$2"; local jar="$3"; local out="$4"; shift 4; local token="$(csrf_token "$jar")"; curl -sS -o "$out" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "X-XSRF-TOKEN: $token" -c "$jar" -b "$jar" "$@"; }
assert_status(){ [ "$1" = "$2" ] && pass "$3" || fail "$3 (expected $2 got $1)"; }
contains(){ python3 - <<'PY' "$1" "$2"
import sys
content=open(sys.argv[1]).read()
sys.exit(0 if sys.argv[2] in content else 1)
PY
}
login(){ local user="$1"; local jar="$2"; local out="$TMP_DIR/login-$user.json"; local code; code=$(json_request POST "/api/auth/login" "$jar" "{\"username\":\"$user\",\"password\":\"PortalAccess2026!\"}" "$out"); [ "$code" = "200" ] || { cat "$out"; exit 1; }; }

buyer="$TMP_DIR/buyer.cookies"; quality="$TMP_DIR/quality.cookies"; finance="$TMP_DIR/finance.cookies"; admin="$TMP_DIR/admin.cookies"
login buyer1 "$buyer"; login quality1 "$quality"; login finance1 "$finance"; login admin1 "$admin"

# order cancellation after approval
code=$(json_request POST "/api/orders" "$buyer" '{"notes":"critical cancel","items":[{"productId":1,"quantity":2}]}' "$TMP_DIR/order-create.json")
assert_status "$code" "200" "Create order for critical action flow"
ORDER_ID=$(python3 - <<'PY' "$TMP_DIR/order-create.json"
import json,sys; print(json.load(open(sys.argv[1]))['id'])
PY
)
json_request POST "/api/orders/$ORDER_ID/submit-review" "$buyer" '{}' "$TMP_DIR/order-submit.json" >/dev/null
json_request POST "/api/orders/$ORDER_ID/approve" "$quality" '{"decision":"APPROVED","comments":"approved"}' "$TMP_DIR/order-approve.json" >/dev/null
code=$(json_request POST "/api/critical-actions" "$buyer" "{\"requestType\":\"ORDER_CANCELLATION_AFTER_APPROVAL\",\"targetType\":\"ORDER\",\"targetId\":$ORDER_ID,\"justification\":\"Buyer needs to cancel approved order\"}" "$TMP_DIR/ca-order.json")
assert_status "$code" "200" "Create protected order cancellation request"
REQ_ID=$(python3 - <<'PY' "$TMP_DIR/ca-order.json"
import json,sys; print(json.load(open(sys.argv[1]))['id'])
PY
)
code=$(json_request POST "/api/critical-actions/$REQ_ID/decision" "$quality" '{"decision":"APPROVE","comments":"First approval"}' "$TMP_DIR/ca-approve1.json")
assert_status "$code" "200" "First critical approval recorded"
code=$(json_request POST "/api/critical-actions/$REQ_ID/decision" "$quality" '{"decision":"APPROVE","comments":"Duplicate approval"}' "$TMP_DIR/ca-same-user.json")
assert_status "$code" "400" "Same-user second approval rejected"
code=$(json_request POST "/api/critical-actions/$REQ_ID/decision" "$finance" '{"decision":"APPROVE","comments":"Second approval"}' "$TMP_DIR/ca-approve2.json")
assert_status "$code" "200" "Second distinct approval executes request"
code=$(curl -sS -o "$TMP_DIR/order-detail.json" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/orders/$ORDER_ID")
assert_status "$code" "200" "Order detail available after execution"
if contains "$TMP_DIR/order-detail.json" '"status":"CANCELED"'; then pass "Approved order becomes canceled after dual approval"; else fail "Approved order becomes canceled after dual approval"; fi

# document destruction and retention override
pdf="$TMP_DIR/sample.pdf"; printf '%%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n' > "$pdf"
create_doc(){
  local out="$1"
  multipart_request POST "/api/documents" "$buyer" "$out" -F 'payload={"documentTypeId":1,"title":"Critical Doc","contentText":"Body","metadataTags":"regulated","approvalRoles":["QUALITY_REVIEWER"]};type=application/json' -F "file=@$pdf;type=application/pdf" >/tmp/ignore
}
create_doc "$TMP_DIR/doc1.json"
DOC1=$(python3 - <<'PY' "$TMP_DIR/doc1.json"
import json,sys; print(json.load(open(sys.argv[1]))['id'])
PY
)
json_request POST "/api/documents/$DOC1/submit-approval" "$buyer" '{}' "$TMP_DIR/doc1-submit.json" >/dev/null
json_request POST "/api/documents/$DOC1/approve" "$quality" '{"comments":"approved"}' "$TMP_DIR/doc1-approve.json" >/dev/null
json_request POST "/api/documents/$DOC1/archive" "$admin" '{}' "$TMP_DIR/doc1-archive.json" >/dev/null
code=$(json_request POST "/api/critical-actions" "$buyer" "{\"requestType\":\"DOCUMENT_DESTRUCTION\",\"targetType\":\"DOCUMENT\",\"targetId\":$DOC1,\"justification\":\"Document destruction request\"}" "$TMP_DIR/doc-destroy-req.json")
assert_status "$code" "200" "Create document destruction request"
DOC_DEST_REQ=$(python3 - <<'PY' "$TMP_DIR/doc-destroy-req.json"
import json,sys; print(json.load(open(sys.argv[1]))['id'])
PY
)
json_request POST "/api/critical-actions/$DOC_DEST_REQ/decision" "$quality" '{"decision":"APPROVE","comments":"quality ok"}' "$TMP_DIR/doc-d1.json" >/dev/null
code=$(json_request POST "/api/critical-actions/$DOC_DEST_REQ/decision" "$finance" '{"decision":"APPROVE","comments":"finance ok"}' "$TMP_DIR/doc-d2.json")
assert_status "$code" "200" "Dual approval destroys document"
code=$(curl -sS -o "$TMP_DIR/doc1-detail.json" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/documents/$DOC1")
assert_status "$code" "200" "Destroyed document detail still retrievable"
if contains "$TMP_DIR/doc1-detail.json" '"status":"DESTROYED"'; then pass "Document destruction final resolution state stored"; else fail "Document destruction final resolution state stored"; fi

create_doc "$TMP_DIR/doc2.json"
DOC2=$(python3 - <<'PY' "$TMP_DIR/doc2.json"
import json,sys; print(json.load(open(sys.argv[1]))['id'])
PY
)
json_request POST "/api/documents/$DOC2/submit-approval" "$buyer" '{}' "$TMP_DIR/doc2-submit.json" >/dev/null
json_request POST "/api/documents/$DOC2/approve" "$quality" '{"comments":"approved"}' "$TMP_DIR/doc2-approve.json" >/dev/null
json_request POST "/api/documents/$DOC2/archive" "$admin" '{}' "$TMP_DIR/doc2-archive.json" >/dev/null
code=$(json_request POST "/api/critical-actions" "$buyer" "{\"requestType\":\"RETENTION_OVERRIDE\",\"targetType\":\"DOCUMENT\",\"targetId\":$DOC2,\"justification\":\"Need retention override\"}" "$TMP_DIR/retention-req.json")
assert_status "$code" "200" "Create retention override request"
RET_REQ=$(python3 - <<'PY' "$TMP_DIR/retention-req.json"
import json,sys; print(json.load(open(sys.argv[1]))['id'])
PY
)
json_request POST "/api/critical-actions/$RET_REQ/decision" "$quality" '{"decision":"APPROVE","comments":"first"}' "$TMP_DIR/retention-approve1.json" >/dev/null
docker exec pharmaprocure-postgres psql -U pharmaprocure -d pharmaprocure -c "update critical_action_requests set expires_at = now() - interval '1 hour', status = 'PARTIALLY_APPROVED' where id = $RET_REQ;" >/dev/null
code=$(curl -sS -o "$TMP_DIR/retention-get.json" -w "%{http_code}" -b "$finance" -c "$finance" "$BASE_URL/api/critical-actions/$RET_REQ")
assert_status "$code" "200" "Expired critical action is still retrievable"
if contains "$TMP_DIR/retention-get.json" '"status":"EXPIRED"'; then pass "Critical action expires after 24 hours"; else fail "Critical action expires after 24 hours"; fi

# object-level access scope check (IDOR hardening)
code=$(json_request POST "/api/critical-actions" "$admin" "{\"requestType\":\"RETENTION_OVERRIDE\",\"targetType\":\"DOCUMENT\",\"targetId\":$DOC2,\"justification\":\"Admin-only verification\"}" "$TMP_DIR/admin-retention.json")
assert_status "$code" "200" "Admin creates cross-scope critical action"
ADMIN_REQ_ID=$(python3 - <<'PY' "$TMP_DIR/admin-retention.json"
import json,sys; print(json.load(open(sys.argv[1]))['id'])
PY
)
code=$(curl -sS -o "$TMP_DIR/buyer-cross-scope-get.json" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/critical-actions/$ADMIN_REQ_ID")
assert_status "$code" "403" "Buyer cannot retrieve out-of-scope critical action by ID"

echo "API critical actions summary: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -gt 0 ]; then exit 1; fi
