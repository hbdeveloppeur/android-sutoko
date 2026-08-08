This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*

You are an expert in UX/UI design.

# VisualNobel node fix required: the dialogs swap too fast creating a visual effect bug.

- DONE (branch `fix/visual-novel-dialog-swap`): dialogs now fade sequentially in
  `VisualNovelOverlay.DialogText` — the current dialog fades out fully (300 ms), a 100 ms beat
  holds on an empty dialog area, then the next dialog fades in. No more overlapping texts
  mid-transition. Validated with `:game:presentation:assembleDebug --no-build-cache` (success).

# VisualNobel node: sound assets must load from the story directory "assets" (new bundled mechanism).

- DONE (branch `fix/visual-novel-dialog-swap`): `parseVisualNovel` now resolves dialog
  `soundPath` and ambient `sounds` through `ChapterGraphParser.resolveSoundPath()` — the same
  mechanism as every other sound node: `<storyDir>/assets/<fileName>` first, legacy
  `medias/sounds/` fallback, audio-extension guessing, always a local path (no more remote
  URL fallback for sounds). Image/video layers keep the remote fallback. Stale "remote URL"
  comments in `GameEngineViewModel` reworded. Tests updated in
  `ChapterGraphParserVisualNovelTest` (8 tests). Validated with
  `:game:data:testDebugUnitTest --no-build-cache` and `:game:presentation:assembleDebug
  --no-build-cache` (both success).

# VisualNobel node: remove the dismiss button, add a localized "Continuer >>" button under the card.

- DONE (branch `fix/visual-novel-dialog-swap`): the top-right chevron `DismissButton` is gone.
  A "Continue >>" text button (exact same affordance as CinematicScreen's "Skip >>": white 50%
  alpha, 14sp, no ripple, 32x42dp touch padding, BottomCenter) now fades in under the card over
  600 ms once the dialogs are done AND the 8 s minimum display delay has elapsed. Localized as
  `game_presentation_visual_novel_continue` in en/fr/de/es. Scrim tap-to-dismiss unchanged.
  Validated with `:game:presentation:assembleDebug --no-build-cache` (success).