#!/usr/bin/env bash
# home_screen_loop_test.sh
#
# Launches the Sutoko app N times (release build) on the connected device and
# checks whether HomeScreen is reached each time. Detects HomeScreen via the
# `home_screen` testTag exposed through uiautomator (resource-id).
#
# Usage: ./scripts/home_screen_loop_test.sh [iterations]   (default: 100)
#
# Outputs:
#   scripts/results/<timestamp>/results.csv   - one row per iteration
#   scripts/results/<timestamp>/fail_XX/      - logcat + screenshot per failure
#   prints a summary at the end; exit code 1 if any iteration failed.

set -u

PACKAGE="fr.purpletear.sutoko"
LAUNCH_ACTIVITY="${PACKAGE}/.screens.MainActivity"
HOME_MARKER='resource-id="home_screen"'
ITERATIONS="${1:-100}"
MAX_WAIT_S=25          # max seconds to wait for HomeScreen per iteration
POLL_INTERVAL_S=1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUN_DIR="${SCRIPT_DIR}/results/$(date +%Y%m%d_%H%M%S)"
mkdir -p "${RUN_DIR}"
CSV="${RUN_DIR}/results.csv"
echo "iteration,result,time_to_home_s,black_screen" > "${CSV}"

if ! adb get-state >/dev/null 2>&1; then
    echo "ERROR: no adb device connected" >&2
    exit 2
fi

# Returns 0 if the pulled screenshot is (almost) fully black, 1 otherwise.
is_screen_black() {
    local png="$1"
    python3 - "$png" <<'EOF'
import sys, zlib, struct

path = sys.argv[1]
try:
    data = open(path, "rb").read()
except OSError:
    sys.exit(1)

# Decode PNG via stdlib only: gather IDAT, unfilter scanlines (RGB/RGBA 8-bit).
pos, idat, w, h, bitdepth, colortype = 8, b"", 0, 0, 8, 6
while pos < len(data):
    length = struct.unpack(">I", data[pos:pos+4])[0]
    ctype = data[pos+4:pos+8]
    chunk = data[pos+8:pos+8+length]
    if ctype == b"IHDR":
        w, h, bitdepth, colortype = struct.unpack(">IIBB", chunk[:10])
    elif ctype == b"IDAT":
        idat += chunk
    pos += 12 + length
if bitdepth != 8 or colortype not in (2, 6) or w == 0 or h == 0:
    sys.exit(1)
channels = 3 if colortype == 2 else 4
stride = w * channels
raw = zlib.decompress(idat)
prev = bytearray(stride)
total = 0
count = 0
off = 0
for _ in range(h):
    f = raw[off]; off += 1
    line = bytearray(raw[off:off+stride]); off += stride
    if f == 1:
        for i in range(channels, stride):
            line[i] = (line[i] + line[i-channels]) & 0xFF
    elif f == 2:
        for i in range(stride):
            line[i] = (line[i] + prev[i]) & 0xFF
    elif f == 3:
        for i in range(stride):
            left = line[i-channels] if i >= channels else 0
            line[i] = (line[i] + ((left + prev[i]) >> 1)) & 0xFF
    elif f == 4:
        for i in range(stride):
            a = line[i-channels] if i >= channels else 0
            b = prev[i]
            c = prev[i-channels] if i >= channels else 0
            p = a + b - c
            pa, pb, pc = abs(p-a), abs(p-b), abs(p-c)
            pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
            line[i] = (line[i] + pr) & 0xFF
    prev = line
    for i in range(0, stride, channels):
        total += line[i] + line[i+1] + line[i+2]
        count += 3
mean = total / count if count else 255
sys.exit(0 if mean < 5.0 else 1)
EOF
}

pass=0
fail=0
black_count=0
times=()

for i in $(seq 1 "${ITERATIONS}"); do
    adb shell am force-stop "${PACKAGE}" >/dev/null 2>&1
    adb logcat -c >/dev/null 2>&1
    sleep 1

    start_ts=$(date +%s)
    adb shell am start -n "${LAUNCH_ACTIVITY}" >/dev/null 2>&1

    reached=0
    elapsed=0
    while [ "${elapsed}" -lt "${MAX_WAIT_S}" ]; do
        sleep "${POLL_INTERVAL_S}"
        elapsed=$(( $(date +%s) - start_ts ))
        if adb shell uiautomator dump /sdcard/__ui.xml >/dev/null 2>&1 \
           && adb shell cat /sdcard/__ui.xml 2>/dev/null | grep -q "${HOME_MARKER}"; then
            reached=1
            break
        fi
    done

    if [ "${reached}" -eq 1 ]; then
        pass=$((pass + 1))
        times+=("${elapsed}")
        echo "${i},PASS,${elapsed}," >> "${CSV}"
        echo "[${i}/${ITERATIONS}] PASS (${elapsed}s)"
    else
        fail_dir="${RUN_DIR}/fail_$(printf '%02d' "${i}")"
        mkdir -p "${fail_dir}"
        adb logcat -d > "${fail_dir}/logcat.txt" 2>&1
        adb shell screencap -p /sdcard/__screen.png >/dev/null 2>&1
        adb pull /sdcard/__screen.png "${fail_dir}/screen.png" >/dev/null 2>&1
        black="unknown"
        if [ -f "${fail_dir}/screen.png" ]; then
            if is_screen_black "${fail_dir}/screen.png"; then
                black="yes"
                black_count=$((black_count + 1))
            else
                black="no"
            fi
        fi
        fail=$((fail + 1))
        echo "${i},FAIL,${MAX_WAIT_S},${black}" >> "${CSV}"
        echo "[${i}/${ITERATIONS}] FAIL (black=${black}) artifacts: ${fail_dir}"
    fi
    adb shell rm -f /sdcard/__ui.xml /sdcard/__screen.png >/dev/null 2>&1
done

adb shell am force-stop "${PACKAGE}" >/dev/null 2>&1

echo "----------------------------------------"
echo "Iterations:      ${ITERATIONS}"
echo "HomeScreen OK:   ${pass} ($(( pass * 100 / ITERATIONS ))%)"
echo "Failed:          ${fail}"
echo "  of which black screen: ${black_count}"
if [ "${#times[@]}" -gt 0 ]; then
    avg=$(( $(IFS=+; echo "$(( ${times[*]} ))") / ${#times[@]} ))
    max=$(printf '%s\n' "${times[@]}" | sort -n | tail -1)
    echo "Time-to-home:    avg ${avg}s, max ${max}s"
fi
echo "Results:         ${CSV}"
echo "----------------------------------------"

[ "${fail}" -eq 0 ]
