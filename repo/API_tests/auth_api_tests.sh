#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080"
PASS=0
FAIL=0
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

record_pass() {
  echo "[PASS] $1"
  PASS=$((PASS + 1))
}

record_fail() {
  echo "[FAIL] $1"
  FAIL=$((FAIL + 1))
}

csrf_token() {
  local jar="$1"
  local body="$TMP_DIR/csrf-$(basename "$jar").json"
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
  local output_file="$5"
  local token
  token="$(csrf_token "$jar")"

  if [ -n "$body" ]; then
    curl -sS -o "$output_file" -w "%{http_code}" -X "$method" "$BASE_URL$path" \
      -H "Content-Type: application/json" \
      -H "X-XSRF-TOKEN: $token" \
      -c "$jar" -b "$jar" \
      --data "$body"
  else
    curl -sS -o "$output_file" -w "%{http_code}" -X "$method" "$BASE_URL$path" \
      -H "X-XSRF-TOKEN: $token" \
      -c "$jar" -b "$jar"
  fi
}

assert_status() {
  local actual="$1"
  local expected="$2"
  local label="$3"
  if [ "$actual" = "$expected" ]; then
    record_pass "$label"
  else
    record_fail "$label (expected $expected got $actual)"
  fi
}

assert_body_contains() {
  local file="$1"
  local needle="$2"
  local label="$3"
  if python3 - "$file" "$needle" <<'PY'
import sys
content = open(sys.argv[1], 'r', encoding='utf-8').read()
sys.exit(0 if sys.argv[2] in content else 1)
PY
  then
    record_pass "$label"
  else
    record_fail "$label"
  fi
}

buyer_jar="$TMP_DIR/buyer.cookies"
admin_jar="$TMP_DIR/admin.cookies"
quality_jar="$TMP_DIR/quality.cookies"
fulfill_jar="$TMP_DIR/fulfill.cookies"
finance_jar="$TMP_DIR/finance.cookies"

status=$(curl -sS -o "$TMP_DIR/unauthorized.json" -w "%{http_code}" "$BASE_URL/api/admin/panel")
assert_status "$status" "401" "Unauthorized access is rejected"

status=$(request POST "/api/auth/login" "$buyer_jar" '{"username":"buyer1","password":"PortalAccess2026!"}' "$TMP_DIR/login-success.json")
assert_status "$status" "200" "Buyer can log in successfully"
assert_body_contains "$TMP_DIR/login-success.json" '"role":"BUYER"' "Successful login returns buyer role"

status=$(curl -sS -o "$TMP_DIR/me.json" -w "%{http_code}" -c "$buyer_jar" -b "$buyer_jar" "$BASE_URL/api/auth/me")
assert_status "$status" "200" "Current user endpoint works with session"

status=$(request GET "/api/orders/workspace" "$buyer_jar" '' "$TMP_DIR/orders.json")
assert_status "$status" "200" "Buyer can access orders workspace"

status=$(request GET "/api/admin/panel" "$buyer_jar" '' "$TMP_DIR/buyer-admin.json")
assert_status "$status" "403" "Buyer is forbidden from admin panel"

status=$(request POST "/api/auth/login" "$admin_jar" '{"username":"admin1","password":"PortalAccess2026!"}' "$TMP_DIR/admin-login.json")
assert_status "$status" "200" "Admin can log in successfully"
status=$(request GET "/api/admin/panel" "$admin_jar" '' "$TMP_DIR/admin-panel.json")
assert_status "$status" "200" "Admin can access admin panel"

status=$(request POST "/api/auth/login" "$finance_jar" '{"username":"finance1","password":"PortalAccess2026!"}' "$TMP_DIR/finance-login.json")
assert_status "$status" "200" "Finance can log in successfully"
status=$(request GET "/api/admin/panel" "$finance_jar" '' "$TMP_DIR/finance-admin-denied.json")
assert_status "$status" "403" "TEAM-scoped role is forbidden from global admin panel"

status=$(request POST "/api/auth/login" "$fulfill_jar" '{"username":"fulfillment1","password":"WrongPortalPassword!"}' "$TMP_DIR/invalid-login.json")
assert_status "$status" "401" "Invalid login is rejected"
assert_body_contains "$TMP_DIR/invalid-login.json" 'Invalid credentials' "Invalid login returns standard error message"

for attempt in 1 2; do
  request POST "/api/auth/login" "$quality_jar" '{"username":"quality2","password":"WrongPortalPassword!"}' "$TMP_DIR/quality-$attempt.json" >/dev/null
done

status=$(request POST "/api/auth/login" "$quality_jar" '{"username":"quality2","password":"WrongPortalPassword!"}' "$TMP_DIR/quality-3.json")
assert_status "$status" "401" "Third failed login triggers CAPTCHA requirement"
assert_body_contains "$TMP_DIR/quality-3.json" 'CAPTCHA_REQUIRED' "CAPTCHA requirement is returned after three failures"

curl -fsS -c "$quality_jar" -b "$quality_jar" "$BASE_URL/api/auth/captcha?username=quality2" > "$TMP_DIR/captcha.json"
challenge_id=$(python3 - <<'PY' "$TMP_DIR/captcha.json"
import json, sys
data=json.load(open(sys.argv[1], 'r', encoding='utf-8'))
print(data['challengeId'])
PY
)
answer=$(python3 - <<'PY' "$TMP_DIR/captcha.json"
import json, re, sys
data=json.load(open(sys.argv[1], 'r', encoding='utf-8'))
nums=[int(x) for x in re.findall(r'\d+', data['question'])]
print(sum(nums))
PY
)

request POST "/api/auth/login" "$quality_jar" "{\"username\":\"quality2\",\"password\":\"WrongPortalPassword!\",\"captchaChallengeId\":\"$challenge_id\",\"captchaAnswer\":\"$answer\"}" "$TMP_DIR/quality-4.json" >/dev/null

curl -fsS -c "$quality_jar" -b "$quality_jar" "$BASE_URL/api/auth/captcha?username=quality2" > "$TMP_DIR/captcha-2.json"
challenge_id=$(python3 - <<'PY' "$TMP_DIR/captcha-2.json"
import json, sys
data=json.load(open(sys.argv[1], 'r', encoding='utf-8'))
print(data['challengeId'])
PY
)
answer=$(python3 - <<'PY' "$TMP_DIR/captcha-2.json"
import json, re, sys
data=json.load(open(sys.argv[1], 'r', encoding='utf-8'))
nums=[int(x) for x in re.findall(r'\d+', data['question'])]
print(sum(nums))
PY
)

status=$(request POST "/api/auth/login" "$quality_jar" "{\"username\":\"quality2\",\"password\":\"WrongPortalPassword!\",\"captchaChallengeId\":\"$challenge_id\",\"captchaAnswer\":\"$answer\"}" "$TMP_DIR/quality-5.json")
assert_status "$status" "423" "Fifth failed login locks the account"
assert_body_contains "$TMP_DIR/quality-5.json" 'Account locked' "Lockout returns standard lock message"

status=$(request POST "/api/auth/login" "$quality_jar" '{"username":"quality2","password":"PortalAccess2026!"}' "$TMP_DIR/quality-locked.json")
assert_status "$status" "423" "Locked account cannot log in even with correct password"

status=$(request POST "/api/auth/logout" "$buyer_jar" '{}' "$TMP_DIR/logout.json")
assert_status "$status" "200" "Logout succeeds"

echo "API auth summary: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
