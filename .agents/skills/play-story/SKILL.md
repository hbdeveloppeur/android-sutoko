---
name: play-story
description: Auto-play an entire Sutoko story on a connected Android device using scripts/kimi_play_story.py, given a storyId (game id). Use when the user asks to play, test, or verify a story end-to-end on device, e.g. "play story 0AZY0NtFQKu", "auto-play this story", "verify story <id> completes", or passes a storyId for on-device validation.
---

# Play Story

Auto-play a Sutoko story from chapter 1A to its end on a connected device and report whether it completes.

## Prerequisites

- Android device connected via `adb` (`adb devices` shows it).
- Debug build of `fr.purpletear.sutoko` installed. If the current working tree contains
  engine/parser fixes, rebuild and install first (`./gradlew installDebug`) or the run
  tests the old APK.
- Story content is auto-downloaded from sutoko.com by the script unless `--no-install`
  is passed.

## Run

```bash
python3 -u scripts/kimi_play_story.py --game-id <storyId> --timeout <seconds> \
  > scripts/results/play_<storyId>_$(date +%Y%m%d_%H%M%S).log 2>&1
```

- `<storyId>` is the official/test game id (e.g. `0AZY0NtFQKu`).
- Budget ~1 minute per chapter; set `--timeout` generously (e.g. 900 for 12 chapters).
- Always run as a background Shell task with an explicit `timeout` parameter (the
  default background timeout is 60s and will kill the run). Redirect to a log file as
  shown and poll the file — do NOT pipe python through `tail` in a background task
  (breaks streaming). `python3 -u` keeps output unbuffered.

Useful options: `--chapter-code 6A` (start mid-story), `--trial`, `--no-install`
(content already on device), `--no-auto-play` (manual run with log tailing only).

## Interpret the result

- **Exit 0** = story completed (`STORY_COMPLETED` — last chapter reached, no next
  chapter available — or `STORY_FINISHED` — engine hit an `end` node).
- **Exit 1** = timeout, interruption, or error. Check the tail of the log:
  - `AndroidRuntime` → app crash.
  - `[ERR]` / `Node not found` → engine halt, usually a parser/graph issue (investigate
    the node id in the story's `nodes.json`/`edges.json` under
    `/data/data/fr.purpletear.sutoko/files/games/<legacyId>/` via `run-as`).
- The final "Session summary" block reports chapters seen and choices made. A story
  that stalls mid-chapter shows no new markers for minutes — the last marker indicates
  where it froze.

Note: a chapter can legitimately end with `nextChapterAvailable=false` when the next
chapter is not yet released (`available=0`, future releaseDate in the API) — that is a
successful completion, not a bug.
