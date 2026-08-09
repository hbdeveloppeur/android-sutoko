This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*

# Bug hunting - GamePreview.

- DONE (branch `fix/game-download-integrity`): the testers' hypothesis is half right.
  `Download already in progress` was the double-tap race already fixed in HEAD (v138/v139
  predate the `downloadJob` guard + benign `DownloadAlreadyInProgressException`) — shipping
  the next release closes it. The real open hole was trace #2: `GameFileManagerImpl`
  never validated the archive — a truncated download (`copied != Content-Length`) or a
  corrupt zip extracting zero entries was renamed into place and marked installed, so Play
  hit `No chapter languages found`. Fixes: (1) `downloadAndExtract` now verifies the byte
  count against `Content-Length` and requires `chapters/<lang>` to exist after extraction,
  else it fails honestly and rolls back the install row; (2) `ChapterGraphRepositoryImpl`
  self-heals installs already broken by older builds — when no chapter language is found
  and the game dir exists on disk, it deletes the broken dir + install row so the UI
  reverts to Download instead of failing on every play (skipped when the dir is absent, to
  avoid false positives from legacyId drift). Tests: 4 in `GameFileManagerImplTest`
  (truncated download, archive without chapters, path traversal, happy path) + 2 in
  `ChapterGraphRepositoryImplTest` (self-heal, keep-record-when-dir-absent). Validated with
  `:game:data:testDebugUnitTest --no-build-cache` and `:app:assembleDebug --no-build-cache`
  (both success).

We have the following stack trace and we think they are related - Maybe the user tries to download,
it doesn't work for a specific reason, he presses again because he is upset, it throws and later he
is able to play a partial not working archive. We may be wrong, let us know what you think

09/08/2026 00:23

Failed to load chapter 1a

java.lang.IllegalArgumentException: No chapter languages found in:
/data/user/0/fr.purpletear.sutoko/files/games/185/chapters
at ha.c.invokeSuspend(SourceFile:279)
at qd.a.resumeWith(SourceFile:9)
at Id.O.run(SourceFile:108)
at J2.a.run(SourceFile:1130)
at Pd.l.run(SourceFile:3)
at Pd.b.run(SourceFile:93)

---
exception: IllegalArgumentException

08/08/2026 22:34

Download failed for gameId=Er7e2kDZEy7

java.lang.IllegalStateException: Download already in progress
at ha.B.invokeSuspend(SourceFile:704)
at ha.B.invoke(SourceFile:13)
at Ld.f.e(SourceFile:47)
at Md.f.invokeSuspend(SourceFile:33)
at qd.a.resumeWith(SourceFile:9)
at Id.O.run(SourceFile:118)
at J2.a.run(SourceFile:1130)
at Pd.l.run(SourceFile:3)
at Pd.b.run(SourceFile:93)

---
exception: IllegalStateException