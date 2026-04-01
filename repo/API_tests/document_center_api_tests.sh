#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
PASS=0
FAIL=0

pass(){ echo "[PASS] $1"; PASS=$((PASS+1)); }
fail(){ echo "[FAIL] $1"; FAIL=$((FAIL+1)); }

csrf_token() {
  local jar="$1"
  local body="$TMP_DIR/$(basename "$jar").csrf.json"
  curl -fsS -c "$jar" -b "$jar" "$BASE_URL/api/auth/csrf" > "$body"
  python3 - <<'PY' "$body"
import json,sys
print(json.load(open(sys.argv[1]))['token'])
PY
}

json_request() {
  local method="$1"; local path="$2"; local jar="$3"; local body="$4"; local out="$5"
  local token="$(csrf_token "$jar")"
  curl -sS -o "$out" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $token" -c "$jar" -b "$jar" --data "$body"
}

multipart_request() {
  local method="$1"; local path="$2"; local jar="$3"; local out="$4"; shift 4
  local token="$(csrf_token "$jar")"
  curl -sS -o "$out" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "X-XSRF-TOKEN: $token" -c "$jar" -b "$jar" "$@"
}

assert_status(){ [ "$1" = "$2" ] && pass "$3" || fail "$3 (expected $2 got $1)"; }
contains(){ python3 - <<'PY' "$1" "$2"
import sys
content=open(sys.argv[1]).read()
sys.exit(0 if sys.argv[2] in content else 1)
PY
}

login(){
  local user="$1"; local jar="$2"; local out="$TMP_DIR/login-$user.json"
  local code
  code=$(json_request POST "/api/auth/login" "$jar" "{\"username\":\"$user\",\"password\":\"PortalAccess2026!\"}" "$out")
  [ "$code" = "200" ] || { cat "$out"; exit 1; }
}

buyer="$TMP_DIR/buyer.cookies"
quality="$TMP_DIR/quality.cookies"
admin="$TMP_DIR/admin.cookies"
fulfillment="$TMP_DIR/fulfillment.cookies"
login buyer1 "$buyer"
login quality1 "$quality"
login admin1 "$admin"
login fulfillment1 "$fulfillment"

pdf="$TMP_DIR/sample.pdf"
printf '%%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n' > "$pdf"
bad_pdf="$TMP_DIR/bad.pdf"
printf 'not a real pdf' > "$bad_pdf"
png="$TMP_DIR/sample.png"
python3 - <<'PY' "$png"
import sys
open(sys.argv[1], 'wb').write(bytes([137,80,78,71,13,10,26,10,0,0,0,0]))
PY
big="$TMP_DIR/big.pdf"
python3 - <<'PY' "$big"
import sys
with open(sys.argv[1], 'wb') as f:
    f.write(b'%PDF-1.4\n')
    f.write(b'a' * (25*1024*1024 + 1))
PY

code=$(json_request POST "/api/documents/templates" "$admin" '{"documentTypeId":1,"templateName":"SOP Master Template","templateBody":"Document body","active":true}' "$TMP_DIR/template.json")
assert_status "$code" "200" "Admin creates document template"

code=$(multipart_request POST "/api/documents" "$buyer" "$TMP_DIR/create-draft.json" -F 'payload={"documentTypeId":1,"title":"Cold Chain SOP","contentText":"Draft body","metadataTags":"sop,cold-chain","approvalRoles":["QUALITY_REVIEWER"]};type=application/json' -F "file=@$pdf;type=application/pdf")
assert_status "$code" "200" "Buyer creates draft with valid file"
DOC_ID=$(python3 - <<'PY' "$TMP_DIR/create-draft.json"
import json,sys
print(json.load(open(sys.argv[1]))['id'])
PY
)

code=$(multipart_request POST "/api/documents" "$buyer" "$TMP_DIR/invalid-signature.json" -F 'payload={"documentTypeId":1,"title":"Bad PDF","contentText":"x","metadataTags":"bad","approvalRoles":["QUALITY_REVIEWER"]};type=application/json' -F "file=@$bad_pdf;type=application/pdf")
assert_status "$code" "400" "Wrong MIME/signature rejection works"

code=$(multipart_request POST "/api/documents" "$buyer" "$TMP_DIR/oversized.json" -F 'payload={"documentTypeId":1,"title":"Huge","contentText":"x","metadataTags":"big","approvalRoles":["QUALITY_REVIEWER"]};type=application/json' -F "file=@$big;type=application/pdf")
assert_status "$code" "400" "Oversized file rejection works"

code=$(json_request POST "/api/documents/$DOC_ID/submit-approval" "$buyer" '{}' "$TMP_DIR/submitted.json")
assert_status "$code" "200" "Draft submits for approval"
if contains "$TMP_DIR/submitted.json" 'SOP-'; then pass "Numbering generation applied on submission"; else fail "Numbering generation applied on submission"; fi

code=$(json_request POST "/api/documents/$DOC_ID/approve" "$admin" '{"comments":"Bypass attempt."}' "$TMP_DIR/approve-admin-denied.json")
assert_status "$code" "403" "Wrong configured approver role is rejected"

code=$(json_request POST "/api/documents/$DOC_ID/approve" "$quality" '{"comments":"Reviewed and approved."}' "$TMP_DIR/approved.json")
assert_status "$code" "200" "Quality reviewer approves document"

code=$(json_request POST "/api/documents/$DOC_ID/archive" "$admin" '{}' "$TMP_DIR/archived.json")
assert_status "$code" "200" "Admin archives approved document"
if contains "$TMP_DIR/archived.json" 'signatureAlgorithm'; then pass "Archive response includes signature metadata"; else fail "Archive response includes signature metadata"; fi

code=$(curl -sS -o "$TMP_DIR/preview.json" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/documents/$DOC_ID/preview")
assert_status "$code" "200" "Preview metadata retrieval works"
if contains "$TMP_DIR/preview.json" 'watermarkText'; then pass "Preview metadata includes watermark"; else fail "Preview metadata includes watermark"; fi

code=$(curl -sS -o "$TMP_DIR/download.bin" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/documents/$DOC_ID/download")
assert_status "$code" "200" "Download endpoint works"

code=$(curl -sS -o "$TMP_DIR/detail.json" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/documents/$DOC_ID")
assert_status "$code" "200" "Document detail endpoint works"
if contains "$TMP_DIR/detail.json" 'DOWNLOADED'; then pass "Download action is audited"; else fail "Download action is audited"; fi

code=$(multipart_request POST "/api/documents" "$admin" "$TMP_DIR/admin-doc.json" -F 'payload={"documentTypeId":1,"title":"Admin Controlled SOP","contentText":"Admin doc","metadataTags":"admin","approvalRoles":["QUALITY_REVIEWER"]};type=application/json' -F "file=@$pdf;type=application/pdf")
assert_status "$code" "200" "Admin creates global-scope document"
ADMIN_DOC_ID=$(python3 - <<'PY' "$TMP_DIR/admin-doc.json"
import json,sys
print(json.load(open(sys.argv[1]))['id'])
PY
)

code=$(curl -sS -o "$TMP_DIR/fulfillment-admin-doc.json" -w "%{http_code}" -b "$fulfillment" -c "$fulfillment" "$BASE_URL/api/documents/$ADMIN_DOC_ID")
assert_status "$code" "403" "TEAM-scoped fulfillment cannot access global-owned document"

code=$(curl -sS -o "$TMP_DIR/quality-admin-doc.json" -w "%{http_code}" -b "$quality" -c "$quality" "$BASE_URL/api/documents/$ADMIN_DOC_ID")
assert_status "$code" "200" "GLOBAL-scoped quality reviewer can access global-owned document"

code=$(multipart_request POST "/api/documents" "$buyer" "$TMP_DIR/finance-route-doc.json" -F 'payload={"documentTypeId":1,"title":"Finance Route Doc","contentText":"Needs finance approval","metadataTags":"finance","approvalRoles":["FINANCE"]};type=application/json' -F "file=@$pdf;type=application/pdf")
assert_status "$code" "200" "Buyer creates finance-routed document"
FINANCE_ROUTE_DOC_ID=$(python3 - <<'PY' "$TMP_DIR/finance-route-doc.json"
import json,sys
print(json.load(open(sys.argv[1]))['id'])
PY
)
code=$(json_request POST "/api/documents/$FINANCE_ROUTE_DOC_ID/submit-approval" "$buyer" '{}' "$TMP_DIR/finance-route-submit.json")
assert_status "$code" "200" "Finance-routed document submits for approval"
code=$(json_request POST "/api/documents/$FINANCE_ROUTE_DOC_ID/approve" "$quality" '{"comments":"Wrong role attempt."}' "$TMP_DIR/finance-route-quality-denied.json")
assert_status "$code" "403" "Non-finance approver is rejected for finance-routed step"

echo "API document center summary: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -gt 0 ]; then exit 1; fi
