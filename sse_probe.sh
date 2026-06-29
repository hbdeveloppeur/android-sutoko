#!/usr/bin/env bash
#
# sse_probe.sh
# Detached SSE probe for the real-time story-testing endpoint.
# Runs outside the Android project so you can tell whether a dropped connection
# is caused by the server or by the client's read timeout.
#
# Usage:
#   export AUTH_TOKEN="<jwt from the app or your backend>"
#   ./sse_probe.sh -s <session_id> [-i <asset_inventory_token>] [-b <base_url>] [-d <seconds>] [-a]
#   ./sse_probe.sh -j <story_id>  [-i <asset_inventory_token>] [-b <base_url>] [-d <seconds>] [-a]
#
# Options:
#   -s  Session id (use this if you already have one)
#   -j  Story id: the script calls /test-session/join first to obtain a session id
#   -i  Asset-inventory token (optional, mirrors the Android query param)
#   -b  Base URL (default: https://canvas.sutoko.com/api)
#   -d  Max total duration in seconds (default: 60)
#   -a  Android-mode: abort the connection if no byte is received for 10s,
#       reproducing OkHttp's default 10-second read timeout.
#   -h  Show this help
#
# The script connects to /test-session/{sessionId}/events?clientType=phone,
# prints every SSE line with a timestamp, reports when the connection closes,
# and reconnects until the duration expires.

set -euo pipefail

BASE_URL="https://canvas.sutoko.com/api"
SESSION_ID=""
STORY_ID=""
INV_TOKEN=""
DURATION=60
ANDROID_MODE=false

urlencode() {
    python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1], safe=""))' "$1"
}

usage() {
    sed -n '2,25p' "$0"
}

while getopts "s:j:i:b:d:ah" opt; do
    case "$opt" in
        s) SESSION_ID="$OPTARG" ;;
        j) STORY_ID="$OPTARG" ;;
        i) INV_TOKEN="$OPTARG" ;;
        b) BASE_URL="${OPTARG%/}" ;;
        d) DURATION="$OPTARG" ;;
        a) ANDROID_MODE=true ;;
        h) usage; exit 0 ;;
        *) usage; exit 1 ;;
    esac
done

AUTH_TOKEN="${AUTH_TOKEN:-}"

if [[ -z "$AUTH_TOKEN" ]]; then
    echo "ERROR: set the AUTH_TOKEN environment variable to a valid JWT." >&2
    echo "       The Android app sends the value stored in UserRepository.token." >&2
    exit 1
fi

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

if [[ -n "$STORY_ID" && -z "$SESSION_ID" ]]; then
    JOIN_URL="${BASE_URL}/test-session/join"
    log "Joining test session for story $STORY_ID..."
    JOIN_RESPONSE=$(curl -s -S -X POST "$JOIN_URL" \
        -H "Authorization: Bearer ${AUTH_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{\"storyId\":\"${STORY_ID}\",\"deviceInfo\":\"sse-probe\"}")
    SESSION_ID=$(printf '%s' "$JOIN_RESPONSE" | python3 -c 'import sys, json; print(json.load(sys.stdin)["sessionId"])')
    log "Joined session $SESSION_ID"
fi

if [[ -z "$SESSION_ID" ]]; then
    echo "ERROR: provide a session id with -s or a story id with -j." >&2
    exit 1
fi

URL="${BASE_URL}/test-session/${SESSION_ID}/events?clientType=phone"
if [[ -n "$INV_TOKEN" ]]; then
    URL+="&assetInventoryToken=$(urlencode "$INV_TOKEN")"
fi

CURL_ARGS=(
    -N
    -s
    -S
    --connect-timeout 10
    -H "Accept: text/event-stream"
    -H "Cache-Control: no-cache"
    -H "Authorization: Bearer ${AUTH_TOKEN}"
)

if [[ "$ANDROID_MODE" == true ]]; then
    # If no data arrives for 10 seconds, curl exits, just like OkHttp's default read timeout.
    CURL_ARGS+=(--speed-time 10 --speed-limit 1)
    log "Android-mode enabled: connection will be killed after 10s of inactivity."
fi

END_TIME=$(($(date +%s) + DURATION))
HEADER_FILE="$(mktemp /tmp/sse_probe_headers.XXXXXX)"
trap 'rm -f "$HEADER_FILE"' EXIT

log "Starting SSE probe — URL: $URL"
log "Stop with Ctrl+C"

while [[ "$(date +%s)" -lt "$END_TIME" ]]; do
    log "Opening SSE connection..."
    START_TIME=$(date +%s)

    # Disable errexit around the streaming pipeline so we can capture curl's exit code ourselves.
    set +e
    # shellcheck disable=SC2094
    curl "${CURL_ARGS[@]}" -D "$HEADER_FILE" "$URL" 2>"$HEADER_FILE.err" | while IFS= read -r line || [[ -n "$line" ]]; do
        log "EVT  $line"
    done
    CURL_RC=$?
    set -e

    ELAPSED=$(($(date +%s) - START_TIME))

    if [[ "$CURL_RC" -ne 0 ]]; then
        log "Connection closed abnormally (curl exit $CURL_RC) after ${ELAPSED}s"
        if [[ -s "$HEADER_FILE.err" ]]; then
            log "Curl stderr: $(head -n 5 "$HEADER_FILE.err")"
        fi
        if [[ -s "$HEADER_FILE" ]]; then
            log "HTTP headers:"
            sed 's/^/     /' "$HEADER_FILE" || true
        fi
    else
        log "Connection closed cleanly after ${ELAPSED}s"
    fi

    REMAINING=$((END_TIME - $(date +%s)))
    if [[ "$REMAINING" -le 0 ]]; then
        break
    fi

    log "Reconnecting in 3s..."
    sleep 3
done

log "Probe finished."
