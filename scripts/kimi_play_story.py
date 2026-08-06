#!/usr/bin/env python3
"""
Kimi-cli story auto-player launcher.

Starts Sutoko's SmsGameActivity in auto-play mode on a connected Android device,
streams GameEngine / KIMI logcat markers, and reports when the story completes
or an error is detected.

Usage:
    python3 scripts/kimi_play_story.py --game-id <GAME_ID> [--chapter-code <CHAPTER_CODE>]

By default the whole story is played from the first chapter (1a); the auto-player
clicks through each chapter end until no next chapter is available.

Example:
    python3 scripts/kimi_play_story.py --game-id KwSEkz9VfM5

Intended for official/test stories only; not for user-created stories or
Friendzoned legacy games.
"""

import argparse
import json
import re
import shlex
import subprocess
import sys
import tempfile
import time
import urllib.request
import zipfile
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional, Tuple

PACKAGE = "fr.purpletear.sutoko"
ACTIVITY = "com.purpletear.game.presentation.game_play.SmsGameActivity"
API_BASE = "https://sutoko.com"

# Logcat tags we care about. KIMI markers from StoryAutoPlayer use the KIMI tag;
# GameEngineLogger emits via println (System.out tag); GameEngineViewModel uses the
# GameEngine tag directly; AndroidRuntime catches native crashes.
# Tag filters for `adb logcat -s` (silent default; only these tags are emitted).
LOGCAT_TAG_FILTERS = ["KIMI:D", "System.out:D", "GameEngine:D", "AndroidRuntime:E"]

# threadtime format: mm-dd HH:MM:SS.mmm pid tid priority tag: message
_LOGCAT_LINE = re.compile(
    r"^\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d+\s+\d+\s+\d+\s+(?P<level>\S)\s+(?P<tag>[^:]+):\s*(?P<msg>.*)$"
)

KIMI_PREFIX = re.compile(r"^SutokoGameEngine\s+\[KIMI\]\s+(.*)$")
ENGINE_PREFIX = re.compile(r"^(SutokoGameEngine|GameEngine)\s+(.*)$")
FATAL_PREFIX = re.compile(r"^AndroidRuntime:.*$")
ANSI_ESCAPE = re.compile(r"\x1b\[[0-9;]*m")


@dataclass
class SessionSummary:
    started: bool = False
    completed: bool = False
    choices: int = 0
    chapters: int = 0
    last_error: Optional[str] = None
    _seen_chapters: set = field(default_factory=set)


def run(args: list[str], check: bool = True, timeout: Optional[int] = None) -> subprocess.CompletedProcess:
    return subprocess.run(
        args,
        check=check,
        capture_output=True,
        text=True,
        timeout=timeout,
    )


def check_device() -> None:
    result = run(["adb", "devices", "-l"])
    lines = [line.strip() for line in result.stdout.splitlines() if line.strip() and not line.startswith("List")]
    if not lines:
        print("Error: no Android device connected.", file=sys.stderr)
        sys.exit(1)
    if len(lines) > 1:
        print("Warning: multiple devices detected; adb will use the default.", file=sys.stderr)


def ensure_package_installed() -> None:
    result = run(["adb", "shell", "pm", "list", "packages", PACKAGE])
    if PACKAGE not in result.stdout:
        print(f"Error: {PACKAGE} is not installed on the device.", file=sys.stderr)
        sys.exit(1)


def clear_logcat() -> None:
    run(["adb", "logcat", "-c"], check=False)


def fetch_json(url: str) -> dict:
    with urllib.request.urlopen(url, timeout=30) as response:
        return json.load(response)


def adb_shell(script: str, check: bool = True) -> subprocess.CompletedProcess:
    """Runs `script` as the app user via run-as.

    `adb shell` joins its argv with spaces on the device side, so the remote command
    must be a single shell-quoted argument or `sh -c` only receives its first word.
    """
    return run(
        ["adb", "shell", f"run-as {PACKAGE} sh -c {shlex.quote(script)}"],
        check=check,
        timeout=60,
    )


def ensure_story_content(game_id: str) -> None:
    """
    Makes sure the story archive is extracted on the device at files/games/<dirName>,
    where dirName is the story legacyId when it has one (matching AndroidGamePathProvider).
    Downloads the same archive the app would, then streams it through `adb exec-in`
    (adb push cannot write to /data/data and run-as cannot read /sdcard).
    """
    story = fetch_json(f"{API_BASE}/portal/stories/{game_id}?languageCode=en-US")
    legacy_id = story.get("legacyId")
    dir_name = str(legacy_id) if legacy_id else game_id

    probe = adb_shell(f"test -d files/games/{dir_name}/chapters && echo present", check=False)
    if "present" in probe.stdout:
        print(f"Story content already on device: files/games/{dir_name}")
        return

    print(f"Story content missing on device; installing files/games/{dir_name} ...")
    link = fetch_json(f"{API_BASE}/api/story/{game_id}/download-link")["link"]

    with tempfile.TemporaryDirectory(prefix="kimi_story_") as tmp:
        archive = Path(tmp) / "story.zip"
        urllib.request.urlretrieve(link, archive)
        extract_dir = Path(tmp) / "extracted"
        with zipfile.ZipFile(archive) as zf:
            zf.extractall(extract_dir)

        # SMS engine layout is chapters/<lang>/<chapter>/nodes.json; legacy Friendzoned
        # archives use chapters/<chapter>/<lang>/ and cannot run in SmsGameActivity.
        chapters_root = extract_dir / "chapters"
        is_sms_layout = any(
            (chapter_dir / "nodes.json").exists()
            for lang_dir in chapters_root.glob("*/*") if lang_dir.is_dir()
            for chapter_dir in [lang_dir]
        ) if chapters_root.is_dir() else False
        if not is_sms_layout:
            print(
                "Error: archive has the legacy Friendzoned layout "
                "(chapters/<chapter>/<lang>/); only SMS-engine stories are supported.",
                file=sys.stderr,
            )
            sys.exit(1)

        tar = subprocess.Popen(
            ["tar", "cf", "-", "-C", str(extract_dir), "."],
            stdout=subprocess.PIPE,
        )
        receiver = subprocess.run(
            [
                "adb", "exec-in",
                f"run-as {PACKAGE} sh -c 'mkdir -p files/games/{dir_name} && tar xf - -C files/games/{dir_name}'",
            ],
            stdin=tar.stdout,
            capture_output=True,
            text=False,
            timeout=300,
        )
        tar.wait()
        if receiver.returncode != 0:
            print(
                f"Error: failed to stream story content to the device: {receiver.stderr!r}",
                file=sys.stderr,
            )
            sys.exit(1)

    probe = adb_shell(f"test -d files/games/{dir_name}/chapters && echo present", check=False)
    if "present" not in probe.stdout:
        print("Error: story content install verification failed.", file=sys.stderr)
        sys.exit(1)
    print(f"Story content installed: files/games/{dir_name}")


def launch_activity(game_id: str, chapter_code: str, is_trial: bool, auto_play: bool) -> None:
    # SmsGameActivity is singleTop: without a force-stop, the intent is delivered to a
    # stale instance via onNewIntent and no fresh ViewModel (hence no auto-play) starts.
    run(["adb", "shell", "am", "force-stop", PACKAGE], check=False)
    cmd = [
        "adb", "shell", "am", "start", "-W", "-n", f"{PACKAGE}/{ACTIVITY}",
        "--es", "gameId", game_id,
        "--es", "chapterCode", chapter_code,
        "--ez", "isTrial", str(is_trial).lower(),
        "--ez", "autoPlay", str(auto_play).lower(),
    ]
    print(f"Launching {PACKAGE}/{ACTIVITY}")
    print(f"  gameId={game_id} chapterCode={chapter_code} isTrial={is_trial} autoPlay={auto_play}")
    run(cmd, timeout=30)


def stream_logcat() -> subprocess.Popen:
    return subprocess.Popen(
        ["adb", "logcat", "-s", "-v", "threadtime", *LOGCAT_TAG_FILTERS],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )


def parse_logcat_line(line: str) -> Optional[Tuple[str, str]]:
    match = _LOGCAT_LINE.match(line)
    if not match:
        return None
    return match.group("tag"), match.group("msg")


def process_line(line: str, summary: SessionSummary) -> bool:
    """Returns False when the session should stop."""
    print(line, end="")

    parsed = parse_logcat_line(line)
    if parsed is None:
        return True

    tag, raw_msg = parsed
    msg = ANSI_ESCAPE.sub("", raw_msg)

    if tag == "AndroidRuntime":
        summary.last_error = "FATAL/AndroidRuntime crash detected"
        return False

    engine = ENGINE_PREFIX.match(msg)
    if engine:
        if "[ERR" in msg or ("[NAV" in msg and "Navigation error" in msg):
            summary.last_error = msg.strip()
            # A missing node halts the engine for good; no point waiting for a timeout.
            if "Node not found" in msg:
                return False

    kimi = KIMI_PREFIX.match(msg)
    if not kimi:
        return True

    payload = kimi.group(1)
    tokens = payload.split()
    marker = tokens[0] if tokens else ""

    if marker == "STORY_STARTED":
        summary.started = True
    elif marker == "CHOICE_SUBMITTED":
        # Engine-level truth; CHOICE_SELECTED is the same event from the auto-player.
        summary.choices += 1
    elif marker == "CHAPTER_STARTED":
        # CHAPTER_STARTED is logged twice per chapter (Log.d + System.out); dedupe.
        code = next((t.split("=", 1)[1] for t in tokens[1:] if t.startswith("chapterCode=")), payload)
        if code not in summary._seen_chapters:
            summary._seen_chapters.add(code)
            summary.chapters += 1
    elif marker == "STORY_COMPLETED" or marker == "STORY_FINISHED":
        # STORY_COMPLETED: auto-player saw the last chapter end (no next chapter).
        # STORY_FINISHED: engine hit an `end` node (stories without a final chapter-change).
        summary.completed = True
        return False
    elif marker == "ERROR":
        summary.last_error = payload
        return False

    return True


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Launch Sutoko and auto-play an entire story while tailing GameEngine logs."
    )
    parser.add_argument("--game-id", required=True, help="Official/test story game id")
    parser.add_argument("--chapter-code", default="1a",
                        help="Chapter code to start from (default: 1a, i.e. the whole story)")
    parser.add_argument("--trial", action="store_true", help="Start in trial mode")
    parser.add_argument("--no-install", action="store_true",
                        help="Do not auto-install the story content on the device")
    parser.add_argument("--no-auto-play", action="store_true", help="Launch without auto-driving the UI")
    parser.add_argument("--timeout", type=int, default=0, help="Max seconds to wait (0 = unlimited)")
    args = parser.parse_args()

    check_device()
    ensure_package_installed()
    if not args.no_install:
        ensure_story_content(args.game_id)

    auto_play = not args.no_auto_play
    clear_logcat()
    launch_activity(args.game_id, args.chapter_code, args.trial, auto_play)

    print("\n--- Tailing logcat (press Ctrl+C to stop) ---\n")
    summary = SessionSummary()
    proc = stream_logcat()
    start = time.time()
    try:
        while proc.poll() is None:
            if args.timeout and time.time() - start > args.timeout:
                print("\nTimeout reached.", file=sys.stderr)
                break
            line = proc.stdout.readline()
            if not line:
                time.sleep(0.1)
                continue
            if not process_line(line, summary):
                break
    except KeyboardInterrupt:
        print("\nInterrupted by user.")
    finally:
        proc.terminate()
        try:
            proc.wait(timeout=5)
        except subprocess.TimeoutExpired:
            proc.kill()

    print("\n--- Session summary ---")
    print(f"Started:       {summary.started}")
    print(f"Chapters seen: {summary.chapters}")
    print(f"Choices made:  {summary.choices}")
    print(f"Completed:     {summary.completed}")
    if summary.last_error:
        print(f"Last error:    {summary.last_error}")
    sys.exit(0 if summary.completed else 1)


if __name__ == "__main__":
    main()
