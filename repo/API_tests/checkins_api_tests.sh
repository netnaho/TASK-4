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

buyer="$TMP_DIR/buyer.cookies"
admin="$TMP_DIR/admin.cookies"
fulfillment="$TMP_DIR/fulfillment.cookies"
login buyer1 "$buyer"
login admin1 "$admin"
login fulfillment1 "$fulfillment"

png="$TMP_DIR/photo.png"
python3 - <<'PY' "$png"
import sys
open(sys.argv[1], 'wb').write(bytes([137,80,78,71,13,10,26,10,0,0,0,0]))
PY
wav="$TMP_DIR/audio.wav"
python3 - <<'PY' "$wav"
import sys
open(sys.argv[1], 'wb').write(b'RIFF\x24\x00\x00\x00WAVEfmt ')
PY
bad="$TMP_DIR/bad.exe"
printf 'MZfake' > "$bad"
big="$TMP_DIR/big.wav"
python3 - <<'PY' "$big"
import sys
with open(sys.argv[1], 'wb') as f:
    f.write(b'RIFF\x24\x00\x00\x00WAVEfmt ')
    f.write(b'a' * (25*1024*1024 + 1))
PY

code=$(multipart_request POST "/api/check-ins" "$buyer" "$TMP_DIR/create-geo.json" -F 'payload={"commentText":"Delivered to clinic","deviceTimestamp":"2026-03-29T08:00:00Z","latitude":9.01,"longitude":38.76};type=application/json' -F "files=@$png;type=image/png" -F "files=@$wav;type=audio/wav")
assert_status "$code" "200" "Create check-in with geolocation"
CHECKIN_ID=$(python3 - <<'PY' "$TMP_DIR/create-geo.json"
import json,sys
print(json.load(open(sys.argv[1]))['id'])
PY
)

code=$(multipart_request POST "/api/check-ins" "$buyer" "$TMP_DIR/create-no-geo.json" -F 'payload={"commentText":"No coordinates provided","deviceTimestamp":"2026-03-29T09:00:00Z"};type=application/json')
assert_status "$code" "200" "Create check-in without geolocation"

code=$(multipart_request POST "/api/check-ins" "$buyer" "$TMP_DIR/invalid-type.json" -F 'payload={"commentText":"Invalid attachment","deviceTimestamp":"2026-03-29T10:00:00Z"};type=application/json' -F "files=@$bad;type=application/octet-stream")
assert_status "$code" "400" "Reject invalid attachment type"

code=$(multipart_request POST "/api/check-ins" "$buyer" "$TMP_DIR/oversized.json" -F 'payload={"commentText":"Oversized","deviceTimestamp":"2026-03-29T10:30:00Z"};type=application/json' -F "files=@$big;type=audio/wav")
assert_status "$code" "400" "Reject oversized attachment"

code=$(multipart_request PUT "/api/check-ins/$CHECKIN_ID" "$buyer" "$TMP_DIR/update.json" -F 'payload={"commentText":"Delivered to clinic and storage room","deviceTimestamp":"2026-03-29T08:10:00Z","latitude":9.02,"longitude":38.76};type=application/json' -F "files=@$png;type=image/png")
assert_status "$code" "200" "Update check-in creates revision"
if contains "$TMP_DIR/update.json" 'changedFields'; then pass "Revision trail includes changed fields"; else fail "Revision trail includes changed fields"; fi

code=$(curl -sS -o "$TMP_DIR/detail.json" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/check-ins/$CHECKIN_ID")
assert_status "$code" "200" "Revision trail retrieval works"

ATTACHMENT_ID=$(python3 - <<'PY' "$TMP_DIR/detail.json"
import json,sys
data=json.load(open(sys.argv[1]))
print(data['attachments'][0]['id'])
PY
)
code=$(curl -sS -o "$TMP_DIR/attachment.bin" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/check-ins/$CHECKIN_ID/attachments/$ATTACHMENT_ID/download")
assert_status "$code" "200" "Attachment download works"

code=$(curl -sS -o "$TMP_DIR/detail-after-download.json" -w "%{http_code}" -b "$buyer" -c "$buyer" "$BASE_URL/api/check-ins/$CHECKIN_ID")
assert_status "$code" "200" "Check-in detail still accessible after download"
if contains "$TMP_DIR/detail-after-download.json" 'DOWNLOADED'; then pass "Download action is audited"; else fail "Download action is audited"; fi

code=$(multipart_request POST "/api/check-ins" "$admin" "$TMP_DIR/admin-checkin.json" -F 'payload={"commentText":"Admin global check-in","deviceTimestamp":"2026-03-29T11:00:00Z"};type=application/json')
assert_status "$code" "200" "Admin creates global-scope check-in"
ADMIN_CHECKIN_ID=$(python3 - <<'PY' "$TMP_DIR/admin-checkin.json"
import json,sys
print(json.load(open(sys.argv[1]))['id'])
PY
)

code=$(curl -sS -o "$TMP_DIR/fulfillment-admin-checkin.json" -w "%{http_code}" -b "$fulfillment" -c "$fulfillment" "$BASE_URL/api/check-ins/$ADMIN_CHECKIN_ID")
assert_status "$code" "403" "TEAM-scoped fulfillment cannot access global-owned check-in"

code=$(curl -sS -o "$TMP_DIR/admin-own-checkin.json" -w "%{http_code}" -b "$admin" -c "$admin" "$BASE_URL/api/check-ins/$ADMIN_CHECKIN_ID")
assert_status "$code" "200" "GLOBAL-scoped admin can access global-owned check-in"

echo "API check-ins summary: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -gt 0 ]; then exit 1; fi
